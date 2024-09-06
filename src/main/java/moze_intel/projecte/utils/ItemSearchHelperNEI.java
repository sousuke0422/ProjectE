package moze_intel.projecte.utils;

import net.minecraft.item.ItemStack;

import codechicken.nei.SearchField;
import codechicken.nei.api.ItemFilter;

public class ItemSearchHelperNEI extends ItemSearchHelper {

    ItemFilter filter;

    public ItemSearchHelperNEI(String searchString) {
        super(searchString);
        filter = getFilter(searchString);
    }

    public ItemFilter getFilter(String s_filter) {
        // based on
        // https://github.com/Chicken-Bones/NotEnoughItems/blob/a1879a96548d17f5c4d95b40956d68f6f9db82f8/src/codechicken/nei/SearchField.java#L124-L139
        // update
        // https://github.com/GTNewHorizons/NotEnoughItems/compare/2.6.8-GTNH...2.6.11-GTNH#diff-225658e5eb6bea123b279ea478ad7bd18a6ba63448b58ba53aa774272496e701L162-L181
        return SearchField.searchParser.getFilter(s_filter);
    }

    @Override
    public boolean doesItemMatchFilter_(ItemStack itemStack) {
        return filter.matches(itemStack);
    }
}
