package moze_intel.projecte.integration.NEI;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.EMCHelper;

/**
 * Shows what an energy collector turns each fuel into, and how much emc the upgrade takes. The fuels come straight
 * from {@link FuelMapper}, so fuels registered by other mods show up as well.
 */
public class NEICollectorFuelHandler extends TemplateRecipeHandler {

    private static final String ID = "collectorFuelUpgrade";

    public class CachedFuelUpgrade extends CachedRecipe {

        private final PositionedStack input;
        private final PositionedStack output;
        private final PositionedStack collectors;
        private final long cost;

        public CachedFuelUpgrade(ItemStack from, ItemStack to) {
            // Laid out on the furnace background: fuel on top, the collector where the furnace burns its fuel
            this.input = new PositionedStack(from, 51, 6);
            this.output = new PositionedStack(to, 111, 24);
            List<ItemStack> machines = new ArrayList<>();
            machines.add(new ItemStack(ObjHandler.energyCollector));
            machines.add(new ItemStack(ObjHandler.collectorMK2));
            machines.add(new ItemStack(ObjHandler.collectorMK3));
            this.collectors = new PositionedStack(machines, 51, 42);
            this.cost = EMCHelper.getEmcValue(to) - EMCHelper.getEmcValue(from);
        }

        @Override
        public PositionedStack getIngredient() {
            return input;
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }

        @Override
        public PositionedStack getOtherStack() {
            collectors.setPermutationToRender((cycleticks / 20) % collectors.items.length);
            return collectors;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("pe.nei.fuelupgrade");
    }

    @Override
    public String getGuiTexture() {
        return "textures/gui/container/furnace.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return ID;
    }

    @Override
    public void loadTransferRects() {
        transferRects.add(new RecipeTransferRect(new Rectangle(74, 23, 24, 18), ID));
    }

    @Override
    public void drawExtras(int recipe) {
        CachedFuelUpgrade upgrade = (CachedFuelUpgrade) arecipes.get(recipe);
        String text = StatCollector.translateToLocal("pe.emc.emc_tooltip_prefix") + " "
            + Constants.EMC_FORMATTER.format(upgrade.cost);
        Minecraft.getMinecraft().fontRenderer.drawString(text, 74, 46, 0x404040);
    }

    /** Every upgrade step: each fuel and what a collector turns it into. The best fuels have nowhere to go. */
    private List<CachedFuelUpgrade> allUpgrades() {
        List<CachedFuelUpgrade> upgrades = new ArrayList<>();
        for (ItemStack fuel : FuelMapper.getFuels()) {
            if (!FuelMapper.isStackMaxFuel(fuel)) {
                upgrades.add(new CachedFuelUpgrade(fuel, FuelMapper.getFuelUpgrade(fuel)));
            }
        }
        return upgrades;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (ID.equals(outputId)) {
            arecipes.addAll(allUpgrades());
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (CachedFuelUpgrade upgrade : allUpgrades()) {
            if (NEIServerUtils.areStacksSameTypeCrafting(upgrade.output.item, result)) {
                arecipes.add(upgrade);
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        boolean isCollector = false;
        for (ItemStack machine : new CachedFuelUpgrade(ingredient, ingredient).collectors.items) {
            isCollector |= NEIServerUtils.areStacksSameTypeCrafting(machine, ingredient);
        }
        for (CachedFuelUpgrade upgrade : allUpgrades()) {
            if (isCollector || NEIServerUtils.areStacksSameTypeCrafting(upgrade.input.item, ingredient)) {
                arecipes.add(upgrade);
            }
        }
    }
}
