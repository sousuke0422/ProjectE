package moze_intel.projecte.emc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.math.Fraction;

import com.google.common.collect.Maps;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.arithmetics.HiddenDoubleArithmetic;
import moze_intel.projecte.emc.arithmetics.IValueArithmetic;
import moze_intel.projecte.emc.collector.DumpToFileCollector;
import moze_intel.projecte.emc.collector.IExtendedMappingCollector;
import moze_intel.projecte.emc.collector.LongToDoubleCollector;
import moze_intel.projecte.emc.generators.DoubleToLongGenerator;
import moze_intel.projecte.emc.generators.IValueGenerator;
import moze_intel.projecte.emc.mappers.*;
import moze_intel.projecte.emc.mappers.customConversions.CustomConversionMapper;
import moze_intel.projecte.emc.pregenerated.PregeneratedEMC;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.PELogger;
import moze_intel.projecte.utils.PrefixConfiguration;

public final class EMCMapper {

    public static Map<SimpleStack, Long> emc = new LinkedHashMap<>();
    public static Map<NormalizedSimpleStack, Long> graphMapperValues;

    public static double covalenceLoss = ProjectEConfig.covalenceLoss;

    public static void map() {
        List<IEMCMapper<NormalizedSimpleStack, Long>> emcMappers = Arrays.asList(
            new OreDictionaryMapper(),
            new LazyMapper(),
            new Chisel2Mapper(),
            APICustomEMCMapper.instance,
            new CustomConversionMapper(),
            new CustomEMCMapper(),
            new CraftingMapper(),
            new FluidMapper(),
            new SmeltingMapper(),
            new APICustomConversionMapper());
        SimpleGraphMapper<NormalizedSimpleStack, Double, IValueArithmetic<Double>> mapper = new SimpleGraphMapper(
            new HiddenDoubleArithmetic());
        IValueGenerator<NormalizedSimpleStack, Long> valueGenerator = new DoubleToLongGenerator(mapper);
        IExtendedMappingCollector<NormalizedSimpleStack, Long, IValueArithmetic<Fraction>> mappingCollector = new LongToDoubleCollector(
            mapper);

        Configuration config = new Configuration(new File(PECore.CONFIG_DIR, "mapping.cfg"));
        config.load();

        if (config.getBoolean(
            "dumpEverythingToFile",
            "general",
            false,
            "Want to take a look at the internals of EMC Calculation? Enable this to write all the conversions and setValue-Commands to config/ProjectE/mappingdump.json")) {
            mappingCollector = new DumpToFileCollector(
                new File(PECore.CONFIG_DIR, "mappingdump.json"),
                mappingCollector);
        }

        boolean shouldUsePregenerated = config.getBoolean(
            "pregenerate",
            "general",
            false,
            "When the next EMC mapping occurs write the results to config/ProjectE/pregenerated_emc.json and only ever run the mapping again"
                + " when that file does not exist, this setting is set to false, or an error occurred parsing that file.");

        if (shouldUsePregenerated && PECore.PREGENERATED_EMC_FILE.canRead()
            && PregeneratedEMC.tryRead(PECore.PREGENERATED_EMC_FILE, graphMapperValues = Maps.newHashMap())) {
            PELogger.logInfo(String.format("Loaded %d values from pregenerated EMC File", graphMapperValues.size()));
        } else {

            SimpleGraphMapper.setLogFoundExploits(
                config.getBoolean(
                    "logEMCExploits",
                    "general",
                    true,
                    "Log known EMC Exploits. This can not and will not find all possible exploits. "
                        + "This will only find exploits that result in fixed/custom emc values that the algorithm did not overwrite. "
                        + "Exploits that derive from conversions that are unknown to ProjectE will not be found."));

            PELogger.logInfo("Starting to collect Mappings...");
            for (IEMCMapper<NormalizedSimpleStack, Long> emcMapper : emcMappers) {
                try {
                    if (config.getBoolean(
                        emcMapper.getName(),
                        "enabledMappers",
                        emcMapper.isAvailable(),
                        emcMapper.getDescription()) && emcMapper.isAvailable()) {
                        DumpToFileCollector.currentGroupName = emcMapper.getName();
                        emcMapper.addMappings(
                            mappingCollector,
                            new PrefixConfiguration(config, "mapperConfigurations." + emcMapper.getName()));
                        PELogger.logInfo(
                            "Collected Mappings from " + emcMapper.getClass()
                                .getName());
                    }
                } catch (Exception e) {
                    PELogger.logFatal(
                        String.format(
                            "Exception during Mapping Collection from Mapper %s. PLEASE REPORT THIS! EMC VALUES MIGHT BE INCONSISTENT!",
                            emcMapper.getClass()
                                .getName()));
                    e.printStackTrace();
                }
            }
            DumpToFileCollector.currentGroupName = "NSSHelper";
            NormalizedSimpleStack.addMappings(mappingCollector);

            PELogger.logInfo("Mapping Collection finished");
            mappingCollector.finishCollection();

            PELogger.logInfo("Starting to generate Values:");

            config.save();

            graphMapperValues = valueGenerator.generateValues();
            PELogger.logInfo("Generated Values...");

            filterEMCMap(graphMapperValues);

            if (shouldUsePregenerated) {
                // Should have used pregenerated, but the file was not read => regenerate.
                try {
                    PregeneratedEMC.write(PECore.PREGENERATED_EMC_FILE, graphMapperValues);
                    PELogger.logInfo("Wrote Pregen-file!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<NormalizedSimpleStack, Long> entry : graphMapperValues.entrySet()) {
            if (entry.getKey() instanceof NormalizedSimpleStack.NSSItem) {
                NormalizedSimpleStack.NSSItem normStackItem = (NormalizedSimpleStack.NSSItem) entry.getKey();
                Object obj = Item.itemRegistry.getObject(normStackItem.itemName);
                if (obj != null) {
                    int id = Item.itemRegistry.getIDForObject(obj);
                    emc.put(new SimpleStack(id, 1, (int) normStackItem.damage), entry.getValue());
                } else {
                    PELogger.logWarn(
                        "Could not add EMC value for %s|%s. Can not get ItemID!",
                        normStackItem.itemName,
                        normStackItem.damage);
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new EMCRemapEvent());
        Transmutation.cacheFullKnowledge();
        FuelMapper.loadMap();
    }

    /**
     * Remove all entrys from the map, that are not {@link moze_intel.projecte.emc.NormalizedSimpleStack.NSSItem}s, have
     * a value < 0 or WILDCARD_VALUE as metadata.
     * 
     * @param map
     */
    static void filterEMCMap(Map<NormalizedSimpleStack, Long> map) {
        for (Iterator<Map.Entry<NormalizedSimpleStack, Long>> iter = graphMapperValues.entrySet()
            .iterator(); iter.hasNext();) {
            Map.Entry<NormalizedSimpleStack, Long> entry = iter.next();
            NormalizedSimpleStack normStack = entry.getKey();
            if (normStack instanceof NormalizedSimpleStack.NSSItem && entry.getValue() > 0) {
                NormalizedSimpleStack.NSSItem normStackItem = (NormalizedSimpleStack.NSSItem) normStack;
                if (normStackItem.damage != OreDictionary.WILDCARD_VALUE) {
                    continue;
                }
            }
            iter.remove();
        }
    }

    public static boolean mapContains(SimpleStack key) {
        SimpleStack copy = key.copy();
        copy.qnty = 1;

        return emc.containsKey(copy);
    }

    public static long getEmcValue(SimpleStack stack) {
        SimpleStack copy = stack.copy();
        copy.qnty = 1;

        return emc.get(copy);
    }

    public static void clearMaps() {
        emc.clear();
    }
}
