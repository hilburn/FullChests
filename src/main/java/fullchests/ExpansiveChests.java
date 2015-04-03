package fullchests;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fullchests.reference.Metadata;
import fullchests.reference.Reference;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

//@Mod(modid = Reference.ID, name = Reference.NAME, version = Reference.VERSION_FULL)
public class ExpansiveChests
{
    @Mod.Instance(Reference.ID)
    public static ExpansiveChests instance;

    @Mod.Metadata(Reference.ID)
    public static ModMetadata metadata;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        metadata = Metadata.init(metadata);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {
    }

    @SubscribeEvent
    public void interact(PlayerInteractEvent e)
    {
        if (!e.world.isRemote)
        {
            if (e.entityPlayer.getCurrentEquippedItem() != null && new ItemStack(Items.stick).isItemEqual(e.entityPlayer.getCurrentEquippedItem()))
            {
//            e.entityPlayer.addChatComponentMessage(new ChatComponentText("Metadata: " + e.world.getBlockMetadata(e.x, e.y, e.z)));
                TileEntity te = e.world.getTileEntity(e.x, e.y, e.z);
                if (te instanceof IInventory)
                {
                    IInventory inventory = (IInventory) te;
                    for (int i = 0; i<inventory.getSizeInventory(); i++)
                    {
                        ItemStack stack = inventory.getStackInSlot(i);
                        if (stack != null)
                        {
                            e.entityPlayer.addChatComponentMessage(new ChatComponentText("Slot: "+ i + ": "+stack.getDisplayName()));
                        }
                    }
                }
//                if (te instanceof TileEntityChest)
//                {
//                    e.entityPlayer.addChatComponentMessage(new ChatComponentText("Direction: " + ChestHooks.getSafeDirection((TileEntityChest)te)));
//                }
//                e.setCanceled(true);
            }
        }
    }
}
