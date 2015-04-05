package fullchests;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fullchests.asm.ChestsTransformer;
import fullchests.reference.Metadata;
import fullchests.reference.Reference;
import hilburnlib.asm.Transformer;
import hilburnlib.asm.obfuscation.ASMString;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.objectweb.asm.tree.*;

import java.lang.ref.WeakReference;

@Mod(modid = Reference.ID, name = Reference.NAME, version = Reference.VERSION_FULL)
public class FullChests
{
    @Mod.Instance(Reference.ID)
    public static FullChests instance;

    @Mod.Metadata(Reference.ID)
    public static ModMetadata metadata;

    public static ASMString craftingStationLogic = new ASMString("tconstruct.tools.logic.CraftingStationLogic");
    public static ASMString craftingStationContainer = new ASMString("tconstruct.tools.inventory.CraftingStationContainer");
    public static ASMString craftingStationGui = new ASMString("tconstruct.tools.gui.CraftingStationGui");

    private static Transformer.MethodTransformer checkForChests = new Transformer.MethodTransformer("checkForChest", "("+ChestsTransformer.world.getASMTypeName()+"IIIII)V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new InsnNode(RETURN));
        }
    };
    private static Transformer.MethodTransformer fixInventorySize = new Transformer.MethodTransformer("<init>","(Lnet/minecraft/entity/player/InventoryPlayer;"+craftingStationLogic.getASMTypeName()+"III)V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            int local = 10;
            for (LocalVariableNode var : node.localVariables)
            {
                if (var.name.equals("firstChest"))
                {
                    local = var.index;
                    break;
                }
            }
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                if (insnNode instanceof IntInsnNode && insnNode.getOpcode() == BIPUSH && ((IntInsnNode)insnNode).operand == 27)
                {
                    node.instructions.insertBefore(insnNode, new VarInsnNode(ALOAD, local));
                    node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKEINTERFACE, ChestsTransformer.iInventory.getASMClassName(), "getSizeInventory", "()I", true));
                    insnNode = insnNode.getPrevious();
                    node.instructions.remove(insnNode.getNext());
                }
                insnNode = insnNode.getNext();
            }
        }
    };
    private static Transformer.MethodTransformer initGui = new Transformer.MethodTransformer("initGui", "func_73866_w_", "()V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                if (insnNode instanceof FieldInsnNode && insnNode.getOpcode() == GETFIELD && ((FieldInsnNode)insnNode).name.equals("doubleChest"))
                {
                    ((FieldInsnNode)insnNode).name = "chest";
                    insnNode = insnNode.getNext();
                    node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKEVIRTUAL, new ASMString(WeakReference.class).getASMClassName(), "get", "()Ljava/lang/Object;", false));
                    node.instructions.insertBefore(insnNode, new TypeInsnNode(CHECKCAST, ChestsTransformer.iInventory.getASMClassName()));
                    node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKEINTERFACE, ChestsTransformer.iInventory.getASMClassName(), "getSizeInventory", "()I", true));
                    node.instructions.insertBefore(insnNode, new IntInsnNode(BIPUSH, 28));
                    if (insnNode instanceof JumpInsnNode && insnNode.getOpcode() == IFNULL) ((JumpInsnNode)insnNode).setOpcode(IF_ICMPLE);
                    break;
                }
                insnNode = insnNode.getNext();
            }
        }
    };
    private static Transformer.MethodTransformer drawContainerBackground = new Transformer.MethodTransformer("drawGuiContainerBackgroundLayer", "func_146976_a_", "(FII)V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                if (insnNode instanceof FieldInsnNode && insnNode.getOpcode() == GETFIELD && ((FieldInsnNode)insnNode).name.equals("doubleChest"))
                {
                    ((FieldInsnNode)insnNode).name = "chest";
                    insnNode = insnNode.getNext();
                    node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKEVIRTUAL, new ASMString(WeakReference.class).getASMClassName(), "get", "()Ljava/lang/Object;", false));
                    node.instructions.insertBefore(insnNode, new TypeInsnNode(CHECKCAST, ChestsTransformer.iInventory.getASMClassName()));
                    node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKEINTERFACE, ChestsTransformer.iInventory.getASMClassName(), "getSizeInventory", "()I", true));
                    node.instructions.insertBefore(insnNode, new IntInsnNode(BIPUSH, 27));
                    if (insnNode instanceof JumpInsnNode && insnNode.getOpcode() == IFNONNULL) ((JumpInsnNode)insnNode).setOpcode(IF_ICMPGT);
                    break;
                }
                insnNode = insnNode.getNext();
            }
        }
    };
    private static Transformer.ClassTransformer craftingStation = new Transformer.ClassTransformer(craftingStationLogic, checkForChests, ChestsTransformer.cleanupChest);
    private static Transformer.ClassTransformer containerCraftingStation = new Transformer.ClassTransformer(craftingStationContainer, fixInventorySize);
    private static Transformer.ClassTransformer containerCraftingGui = new Transformer.ClassTransformer(craftingStationGui, initGui, drawContainerBackground);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        metadata = Metadata.init(metadata);
        if (Loader.isModLoaded("TConstruct"))
        {
            ChestsTransformer.register(craftingStation);
            ChestsTransformer.register(containerCraftingStation);
            ChestsTransformer.register(containerCraftingGui);
        }
        //MinecraftForge.EVENT_BUS.register(this);
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
