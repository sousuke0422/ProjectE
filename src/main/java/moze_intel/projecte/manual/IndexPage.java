package moze_intel.projecte.manual;

import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class IndexPage extends AbstractPage {

    protected IndexPage() {
        super(PageCategory.INDEX);
        this.setIndexed(false);
    }

    @Override
    public String getBodyText() {
        return "";
    }

    @Override
    public String getHeaderText() {
        return StatCollector.translateToLocal("pe.manual.index");
    }
}
