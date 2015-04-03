package expansivechests.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import hilburnlib.asm.obfuscation.ASMString;
import net.minecraftforge.classloading.FMLForgePlugin;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({"expansivechests.asm."})
public class LoadingPlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[]{"expansivechests.asm.ChestsTransformer"};
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
