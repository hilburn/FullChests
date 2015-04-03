package fullchests.asm;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import fullchests.reference.Metadata;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({"fullchests.asm."})
public class LoadingPlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[]{"fullchests.asm.ChestsTransformer"};
    }

    @Override
    public String getModContainerClass()
    {
        return FullChestsDummyContainer.class.getName();
    }

    @Override
    public String getSetupClass()
    {
        return FullChestsDummyContainer.class.getName();
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

    public static class FullChestsDummyContainer extends DummyModContainer implements IFMLCallHook
    {
        public FullChestsDummyContainer()
        {
            super(new ModMetadata());
            ModMetadata md = getMetadata();
            Metadata.init(md);
        }

        @Override
        public void injectData(Map<String, Object> data)
        {}

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {

            bus.register(this);
            return true;
        }

        @Override
        public Void call() throws Exception
        {
            return null;
        }
    }
}
