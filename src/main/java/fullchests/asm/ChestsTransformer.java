package fullchests.asm;

import hilburnlib.asm.Transformer;
import hilburnlib.asm.obfuscation.ASMString;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public class ChestsTransformer implements IClassTransformer, Opcodes
{
    private static ASMString chestHooks = new ASMString("fullchests.hooks.ChestHooks");
    private static ASMString fullChest = new ASMString("fullchests.tileentity.TileEntityFullChest");
    private static ASMString chestTile = new ASMString.ASMObfString("net.minecraft.tileentity.TileEntityChest", "aow");
    private static ASMString chestBlock = new ASMString.ASMObfString("net.minecraft.block.BlockChest", "ajx");
    private static ASMString itemStack = new ASMString.ASMObfString("net.minecraft.item.ItemStack", "add");
    private static ASMString entityLivingBase = new ASMString.ASMObfString("net.minecraft.entity.EntityLivingBase", "sv");
    public static ASMString world = new ASMString.ASMObfString("net.minecraft.world.World", "ahb");
    public static ASMString iInventory = new ASMString.ASMObfString("net.minecraft.inventory.IInventory", "rb");
    private static ASMString block = new ASMString.ASMObfString("net.minecraft.block.Block", "aji");
    private static ASMString tileEntity = new ASMString.ASMObfString("net.minecraft.tileentity.TileEntity", "aor");
    private static ASMString dispatcher = new ASMString.ASMObfString("net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher", "bmk");
    private static ASMString ocelotAISit = new ASMString.ASMObfString("net.minecraft.entity.ai.EntityAIOcelotSit", "uw");
    private static ASMString chestRenderer = new ASMString.ASMObfString("net.minecraft.client.renderer.tileentity.TileEntityChestRenderer", "bmm");
    private static ASMString chestRenderHelper = new ASMString.ASMObfString("net.minecraft.client.renderer.tileentity.TileEntityRendererChestHelper", "bls");
    private static ASMString fullChestRenderer = new ASMString("fullchests.tileentity.TileEntityFullChestRenderer");

    private static void overwrite(AbstractInsnNode node, ASMString find, ASMString replace)
    {
        String className = find.getObfASMClassName();
        if (node instanceof TypeInsnNode && (((TypeInsnNode)node).desc.equals(className)))
        {
            ((TypeInsnNode)node).desc = replace.getObfASMClassName();
        } else if (node instanceof FieldInsnNode)
        {
            if (((FieldInsnNode)node).owner.equals(className))
                ((FieldInsnNode)node).owner = replace.getObfASMClassName();
            else if (((FieldInsnNode)node).desc.equals(find.getObfASMTypeName()))
                ((FieldInsnNode)node).desc = replace.getASMTypeName();
        } else if (node instanceof MethodInsnNode)
        {
            if (((MethodInsnNode)node).owner.equals(className))
            {
                ((MethodInsnNode)node).owner = replace.getASMClassName();
            } else if (((MethodInsnNode)node).name.equals(find.getText()))
            {
                ((MethodInsnNode)node).name = replace.getText();
            }
        }
    }

    private static Transformer.MethodTransformer tileEntityInit = new Transformer.MethodTransformer("<clinit>", "()V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                if (insnNode instanceof LdcInsnNode && ((LdcInsnNode)insnNode).cst instanceof Type)
                {
                    if (((Type)((LdcInsnNode)insnNode).cst).getDescriptor().equals(chestTile.getObfASMTypeName()))
                    {
                        ((LdcInsnNode)insnNode).cst = Type.getType(fullChest.getASMTypeName());
                        return;
                    }
                }
                insnNode = insnNode.getNext();
            }
        }
    };
    private static Transformer.MethodTransformer rendererDispatcherInit = new Transformer.MethodTransformer("<init>", "()V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                if (insnNode instanceof LdcInsnNode)
                {
                    if (((Type)((LdcInsnNode)insnNode).cst).getDescriptor().equals(chestTile.getObfASMTypeName()))
                    {
                        ((LdcInsnNode)insnNode).cst = Type.getType(fullChest.getASMTypeName());
                    }
                } else
                {
                    overwrite(insnNode, chestRenderer, fullChestRenderer);
                }
                insnNode = insnNode.getNext();
            }
        }
    };
    private static Transformer.MethodTransformer getTileEntity = new Transformer.MethodTransformer("func_149951_m", "m", "(" + world.getObfASMTypeName() + "III)" + iInventory.getObfASMTypeName())
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 1));
            node.instructions.add(new VarInsnNode(ILOAD, 2));
            node.instructions.add(new VarInsnNode(ILOAD, 3));
            node.instructions.add(new VarInsnNode(ILOAD, 4));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestBlock.getObfASMClassName(), new ASMString.ASMObfString("func_149953_o", "o").getText(), "(" + world.getObfASMTypeName() + "III)Z", false));
            node.instructions.add(new VarInsnNode(ALOAD, 1));
            node.instructions.add(new VarInsnNode(ILOAD, 2));
            node.instructions.add(new VarInsnNode(ILOAD, 3));
            node.instructions.add(new VarInsnNode(ILOAD, 4));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "getIInventory", "(Z" + world.getASMTypeName() + "III)" + iInventory.getASMTypeName(), false));
            node.instructions.add(new InsnNode(ARETURN));
        }
    };
    private static Transformer.MethodTransformer onBlockPlacedBy = new Transformer.MethodTransformer("onBlockPlacedBy", "a", "(" + world.getObfASMTypeName() + "III" + entityLivingBase.getObfASMTypeName() + itemStack.getObfASMTypeName() + ")V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                if (insnNode instanceof MethodInsnNode && insnNode.getOpcode() == INVOKEVIRTUAL)
                {
                    if (((MethodInsnNode)insnNode).owner.equals(chestTile.getObfASMClassName()))
                    {
                        node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKEVIRTUAL, fullChest.getASMClassName(), "setCustomName", "(Ljava/lang/String;)V", false));
                        node.instructions.remove(insnNode);
                    }
                } else
                {
                    overwrite(insnNode, chestTile, fullChest);
                }
                insnNode = insnNode.getNext();
            }
            node.localVariables = null;
        }
    };
    private static Transformer.MethodTransformer onNeighbourBlockChanged = new Transformer.MethodTransformer("onNeighborBlockChange", "a", "(" + world.getObfASMTypeName() + "III" + block.getObfASMTypeName() + ")V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                if (insnNode instanceof MethodInsnNode && insnNode.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insnNode).owner.equals(chestTile.getObfASMClassName()))
                {
                    node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKEVIRTUAL, fullChest.getASMClassName(), new ASMString.ASMObfString("updateContainingBlockInfo", "func_145836_u").getText(), "()V", false));
                    node.instructions.remove(insnNode);
                    break;
                }
                insnNode = insnNode.getNext();
            }
            node.localVariables = null;
        }
    };
    public static Transformer.MethodTransformer cleanupChest = new Transformer.MethodTransformer("", "")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                overwrite(insnNode, chestTile, fullChest);
                insnNode = insnNode.getNext();
            }
            node.localVariables = null;
        }

        @Override
        public boolean transform(ClassNode classNode)
        {
            for (MethodNode node : classNode.methods)
            {
                modify(node);
            }
            return true;
        }

        @Override
        protected void log()
        {
        }
    };
    private static Transformer.MethodTransformer breakBlock = new Transformer.MethodTransformer("breakBlock", "a", "(" + world.getObfASMTypeName() + "III" + block.getObfASMTypeName() + "I)V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getLast();
            while (insnNode != null)
            {
                if (insnNode instanceof VarInsnNode && ((VarInsnNode)insnNode).var == 0)
                {
                    insnNode = insnNode.getPrevious();
                    break;
                }
                insnNode = insnNode.getPrevious();
            }
            while (insnNode != null)
            {
                AbstractInsnNode next = insnNode.getPrevious();
                node.instructions.remove(insnNode);
                insnNode = next;
            }
            insnNode = node.instructions.getFirst();
            node.instructions.insertBefore(insnNode, new VarInsnNode(ALOAD, 0));
            node.instructions.insertBefore(insnNode, new VarInsnNode(ALOAD, 1));
            node.instructions.insertBefore(insnNode, new VarInsnNode(ILOAD, 2));
            node.instructions.insertBefore(insnNode, new VarInsnNode(ILOAD, 3));
            node.instructions.insertBefore(insnNode, new VarInsnNode(ILOAD, 4));
            node.instructions.insertBefore(insnNode, new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "breakBlock", "(" + block.getASMTypeName() + world.getASMTypeName() + "III)V", false));

        }
    };
    private static Transformer.MethodTransformer ocelotSit = new Transformer.MethodTransformer("func_151486_a", "a", "(" + world.getObfASMTypeName() + "III)Z")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode insnNode = node.instructions.getFirst();
            while (insnNode != null)
            {
                overwrite(insnNode, chestTile, fullChest);
                insnNode = insnNode.getNext();
            }
            node.localVariables = null;
        }
    };

    private enum ClassTransformers
    {
        //TILE(new Transformer.ClassTransformer(chestTile, getInventorySize, getStackInSlot, decrStackSize, setStackInSlot, openInventory, closeInventory, getInventoryName, checkForAdjacentChests, updateEntity, adjacentChest, direction)),
        BLOCK(new Transformer.ClassTransformer(chestBlock, getTileEntity, breakBlock, onBlockPlacedBy, onNeighbourBlockChanged, cleanupChest)),
        TILE_ENTITY(new Transformer.ClassTransformer(tileEntity, tileEntityInit)),
        DISPATCHER(new Transformer.ClassTransformer(dispatcher, rendererDispatcherInit)),
        OCELOT_SIT(new Transformer.ClassTransformer(ocelotAISit, ocelotSit)),
        RENDER_HELPER(new Transformer.ClassTransformer(chestRenderHelper)
        {
            @Override
            public boolean transform(ClassNode classNode)
            {
                for (FieldNode field : classNode.fields)
                {
                    if (field.desc.equals(chestTile.getObfASMTypeName()))
                    {
                        field.desc = fullChest.getASMTypeName();
                    }
                }
                for (MethodNode methodNode : classNode.methods)
                {
                    AbstractInsnNode node = methodNode.instructions.getFirst();
                    while (node != null)
                    {
                        overwrite(node, chestTile, fullChest);
                        node = node.getNext();
                    }
                    methodNode.localVariables = null;
                }
                return true;
            }
        });

        private Transformer.ClassTransformer transformer;

        ClassTransformers(Transformer.ClassTransformer transformer)
        {
            this.transformer = transformer;
        }

        public Transformer.ClassTransformer getTransformer()
        {
            return transformer;
        }

        public String getClassName()
        {
            return transformer.getClassName();
        }
    }

    private static Map<String, Transformer.ClassTransformer> classMap = new HashMap<String, Transformer.ClassTransformer>();

    static
    {
        for (ClassTransformers classTransformer : ClassTransformers.values())
            classMap.put(classTransformer.getClassName(), classTransformer.getTransformer());
        Transformer.log.info((ASMString.OBFUSCATED ? "O" : "Deo") + "bfuscated environment detected");
    }

    public static void register(Transformer.ClassTransformer transformer)
    {
        classMap.put(transformer.getClassName(), transformer);
    }

    @Override
    public byte[] transform(String className, String className2, byte[] bytes)
    {
        Transformer.ClassTransformer clazz = classMap.get(className);
        if (clazz != null)
        {
            bytes = clazz.transform(bytes);
            classMap.remove(className);
        }
        return bytes;
    }
}
