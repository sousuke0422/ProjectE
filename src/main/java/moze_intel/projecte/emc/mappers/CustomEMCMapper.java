package moze_intel.projecte.emc.mappers;

import java.util.Map;

import net.minecraftforge.common.config.Configuration;

import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.emc.NormalizedSimpleStack;
import moze_intel.projecte.emc.collector.IMappingCollector;
import moze_intel.projecte.utils.PELogger;

public class CustomEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, Configuration config) {
        for (Map.Entry<NormalizedSimpleStack, Long> entry : CustomEMCParser.userValues.entrySet()) {
            PELogger.logInfo("Adding custom EMC value for {}: {}", entry.getKey(), entry.getValue());
            mapper.setValueBefore(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String getName() {
        return "CustomEMCMapper";
    }

    @Override
    public String getDescription() {
        return "Uses the `custom_emc.json` File to add EMC values.";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
