package fullchests.hooks;

import fullchests.tileentity.TileEntityFullChest;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class ChestHooks
{
    private static Random rand = new Random(System.currentTimeMillis());

    public static IInventory getIInventory(boolean ocelot, World world, int x, int y, int z)
    {
        TileEntityFullChest chest = (TileEntityFullChest)world.getTileEntity(x, y, z);
        if (chest == null || ocelot)
            return null;
        if (world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN))
            return null;
        chest.checkForAdjacentChests();
        ForgeDirection dir = chest.getDirection();
        if (dir != ForgeDirection.UNKNOWN && world.isSideSolid(x + dir.offsetX, y + 1, dir.offsetY, ForgeDirection.DOWN))
            return null;
        return chest;
    }
    
    public static void breakBlock(Block block, World world, int x, int y, int z)
    {
        TileEntityFullChest chest = (TileEntityFullChest)world.getTileEntity(x, y, z);
        if (chest != null)
        {
            chest.clearAdjacent();
            for (int i = 0; i < chest.getSizeInventory(); ++i)
            {
                ItemStack stack = chest.getStackInSlot(i);

                if (stack != null)
                {
                    float f = rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem;

                    for (float f2 = rand.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world.spawnEntityInWorld(entityitem))
                    {
                        int dropSize = Math.min(stack.stackSize, rand.nextInt(21) + 10);

                        stack.stackSize -= dropSize;
                        ItemStack spawn = stack.copy();
                        spawn.stackSize = dropSize;
                        entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), spawn);
                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)rand.nextGaussian() * f3);
                        entityitem.motionY = (double)((float)rand.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)rand.nextGaussian() * f3);
                    }
                }
            }
            world.func_147453_f(x, y, z, block);
            if (chest.adjacent != null) chest.adjacent.clearAdjacent();
        }
    }
}
