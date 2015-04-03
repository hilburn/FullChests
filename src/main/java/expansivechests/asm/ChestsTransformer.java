package expansivechests.asm;

import hilburnlib.asm.ASMHelper;
import hilburnlib.asm.Transformer;
import hilburnlib.asm.obfuscation.ASMString;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.util.ForgeDirection;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public class ChestsTransformer implements IClassTransformer, Opcodes
{
    private static ASMString chestHooks = new ASMString("expansivechests.hooks.ChestHooks");
    private static ASMString chestTile = new ASMString.ASMObfString("net.minecraft.tileentity.TileEntityChest","aow");
    private static ASMString chestBlock = new ASMString.ASMObfString("net.minecraft.block.BlockChest", "ajx");
    private static ASMString forgeDirection = new ASMString(ForgeDirection.class);
    private static ASMString itemStack = new ASMString.ASMObfString("net.minecraft.item.ItemStack", "add");
    private static ASMString world = new ASMString.ASMObfString("net.minecraft.world.World", "ahb");
    private static ASMString iInventory = new ASMString.ASMObfString("net.minecraft.inventory.IInventory", "rb");
    private static ASMString block = new ASMString.ASMObfString("net.minecraft.block.Block","aji");

    public static int CHEST_SIZE;

    private static Transformer.MethodTransformer getInventorySize = new Transformer.MethodTransformer("getSizeInventory", "func_70302_i_", "()I")
    {
        @Override
        protected void modify(MethodNode node)
        {
            int value = ((IntInsnNode)ASMHelper.findFirstInstructionWithOpcode(node, BIPUSH)).operand;
            CHEST_SIZE = value;
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD,0));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "getInventorySize", "("+chestTile.getASMTypeName()+")I",false));
            node.instructions.add(new InsnNode(IRETURN));
        }
    };

    private static Transformer.MethodTransformer getStackInSlot = new Transformer.MethodTransformer("getStackInSlot", "func_70301_a", "(I)"+itemStack.getASMTypeName())
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new VarInsnNode(ILOAD, 1));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "getStackInSlot", "("+chestTile.getASMTypeName()+"I)"+itemStack.getASMTypeName(),false));
            node.instructions.add(new InsnNode(ARETURN));
        }
    };
    private static Transformer.MethodTransformer decrStackSize = new Transformer.MethodTransformer("decrStackSize", "func_75209_a", "(II)"+itemStack.getASMTypeName())
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.localVariables = null;
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new VarInsnNode(ILOAD, 1));
            node.instructions.add(new VarInsnNode(ILOAD, 2));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "decrStackSize", "("+chestTile.getASMTypeName()+"II)"+itemStack.getASMTypeName(),false));
            node.instructions.add(new InsnNode(ARETURN));
        }
    };
    private static Transformer.MethodTransformer openInventory = new Transformer.MethodTransformer("openInventory", "()V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "openInventory", "("+chestTile.getASMTypeName()+ ")V",false));
            node.instructions.add(new InsnNode(RETURN));
        }
    };
    private static Transformer.MethodTransformer closeInventory = new Transformer.MethodTransformer("closeInventory", "()V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "closeInventory", "("+chestTile.getASMTypeName()+ ")V",false));
            node.instructions.add(new InsnNode(RETURN));
        }
    };
    private static Transformer.MethodTransformer setStackInSlot = new Transformer.MethodTransformer("setInventorySlotContents", "func_70299_a", "(I"+itemStack.getASMTypeName()+")V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new VarInsnNode(ILOAD, 1));
            node.instructions.add(new VarInsnNode(ALOAD, 2));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "setInventorySlotContents", "("+chestTile.getASMTypeName()+"I"+itemStack.getASMTypeName()+")V",false));
            node.instructions.add(new InsnNode(RETURN));
        }
    };
    private static Transformer.MethodTransformer checkForAdjacentChests = new Transformer.MethodTransformer("checkForAdjacentChests","func_145979_i","()V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode first = node.instructions.getFirst();
            node.instructions.insertBefore(first, new VarInsnNode(ALOAD, 0));
            node.instructions.insertBefore(first, new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "checkForAdjacentChests", "(" + chestTile.getASMTypeName() + ")V", false));
        }
    };
    private static Transformer.MethodTransformer updateEntity = new Transformer.MethodTransformer("updateEntity","func_145845_h","()V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode superCall = ASMHelper.findFirstInstructionWithOpcode(node, INVOKESPECIAL).getNext();
            node.localVariables = null;
            node.instructions.insertBefore(superCall, new VarInsnNode(ALOAD, 0));
            node.instructions.insertBefore(superCall, new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "updateEntity", "(" + chestTile.getASMTypeName() + ")V", false));
            AbstractInsnNode next;
            while (superCall != null)
            {
                next = superCall.getNext();
                node.instructions.remove(superCall);
                superCall = next;
            }
            node.instructions.add(new InsnNode(RETURN));
            return;
        }
    };
    private static Transformer.FieldTransformer adjacentChest = new Transformer.FieldTransformer(Transformer.NODE_ADD, Transformer.Access.PUBLIC, new ASMString("adjacentChest"), chestTile.getASMTypeName())
    {
        @Override
        protected FieldNode getNodeToAdd()
        {
            return new FieldNode(ACC_PUBLIC, name.getText(), chestTile.getASMTypeName(), null, null);
        }
    };
    private static Transformer.FieldTransformer direction = new Transformer.FieldTransformer(Transformer.NODE_ADD, Transformer.Access.PUBLIC, new ASMString("direction"), forgeDirection.getASMTypeName())
    {
        @Override
        protected FieldNode getNodeToAdd()
        {
            return new FieldNode(ACC_PUBLIC, name.getText(), forgeDirection.getASMTypeName(), null, null);
        }
    };
    private static Transformer.MethodTransformer getIInventory = new Transformer.MethodTransformer("func_149951_m", "("+world.getASMTypeName()+"III)"+iInventory.getASMTypeName())
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 1));
            node.instructions.add(new VarInsnNode(ILOAD, 2));
            node.instructions.add(new VarInsnNode(ILOAD, 3));
            node.instructions.add(new VarInsnNode(ILOAD, 4));
            node.instructions.add(new MethodInsnNode(INVOKESTATIC, chestHooks.getASMClassName(), "getIInventory", "("+world.getASMTypeName()+"III)"+iInventory.getASMTypeName(),false));
            node.instructions.add(new InsnNode(ARETURN));
        }
    };
    private static Transformer.MethodTransformer breakBlock = new Transformer.MethodTransformer("breakBlock", "("+world.getASMTypeName()+"III"+block.getASMTypeName()+"I)V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            AbstractInsnNode inject = ASMHelper.findFirstInstructionWithOpcode(node, IFNULL).getNext();
            node.instructions.insertBefore(inject, new VarInsnNode(ALOAD, 7));
            node.instructions.insertBefore(inject, new InsnNode(ACONST_NULL));
            node.instructions.insertBefore(inject, new FieldInsnNode(PUTFIELD, chestTile.getASMClassName(), "adjacentChest", chestTile.getASMTypeName()));
        }
    };
    private static Transformer.MethodTransformer getDirection = new Transformer.MethodTransformer("getDirection", "("+chestTile.getASMTypeName()+")"+forgeDirection.getASMTypeName())
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new FieldInsnNode(GETFIELD, chestTile.getASMClassName(), "direction", forgeDirection.getASMTypeName()));
            node.instructions.add(new InsnNode(ARETURN));
        }
    };
    private static Transformer.MethodTransformer setDirection = new Transformer.MethodTransformer("setDirection", "("+chestTile.getASMTypeName()+forgeDirection.getASMTypeName()+")V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new VarInsnNode(ALOAD, 1));
            node.instructions.add(new FieldInsnNode(PUTFIELD, chestTile.getASMClassName(), "direction", forgeDirection.getASMTypeName()));
            node.instructions.add(new InsnNode(RETURN));
        }
    };
    private static Transformer.MethodTransformer getAdjacent = new Transformer.MethodTransformer("getAdjacent", "("+chestTile.getASMTypeName()+")"+chestTile.getASMTypeName())
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new FieldInsnNode(GETFIELD, chestTile.getASMClassName(), "adjacentChest", chestTile.getASMTypeName()));
            node.instructions.add(new InsnNode(ARETURN));
        }
    };
    private static Transformer.MethodTransformer setAdjacent = new Transformer.MethodTransformer("setAdjacent", "("+chestTile.getASMTypeName()+chestTile.getASMTypeName()+")V")
    {
        @Override
        protected void modify(MethodNode node)
        {
            node.instructions.clear();
            node.instructions.add(new VarInsnNode(ALOAD, 0));
            node.instructions.add(new VarInsnNode(ALOAD, 1));
            node.instructions.add(new FieldInsnNode(PUTFIELD, chestTile.getASMClassName(), "adjacentChest", chestTile.getASMTypeName()));
            node.instructions.add(new InsnNode(RETURN));
        }
    };

    private enum ClassTransformers
    {
        TILE(new Transformer.ClassTransformer(chestTile, getInventorySize, getStackInSlot, setStackInSlot, openInventory, closeInventory, decrStackSize, adjacentChest, direction, checkForAdjacentChests, updateEntity)),
        BLOCK(new Transformer.ClassTransformer(chestBlock, getIInventory, breakBlock)),
        HOOKS(new Transformer.ClassTransformer(chestHooks, setAdjacent, setDirection, getAdjacent, getDirection));

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
        for (ClassTransformers classTransformer : ClassTransformers.values()) classMap.put(classTransformer.getClassName(), classTransformer.getTransformer());
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
