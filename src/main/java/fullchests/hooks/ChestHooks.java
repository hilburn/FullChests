package fullchests.hooks;

import fullchests.asm.ChestsTransformer;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.List;

public class ChestHooks
{
    public static int CHEST_SIZE;
    public static int DOUBLE_CHEST_SIZE;
    public static final EnumSet<ForgeDirection> adjacentPos = EnumSet.of(ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST);
    public static final EnumSet<ForgeDirection> lowerDirs = EnumSet.of(ForgeDirection.UNKNOWN, ForgeDirection.EAST, ForgeDirection.SOUTH);

    static
    {
        CHEST_SIZE = ChestsTransformer.CHEST_SIZE;
        DOUBLE_CHEST_SIZE = CHEST_SIZE * 2;
    }

    public static int getInventorySize(TileEntityChest chest)
    {
        return hasAdjacentChest(chest) ? DOUBLE_CHEST_SIZE : CHEST_SIZE;
    }

    public static ItemStack getStackInSlot(TileEntityChest chest, int slot)
    {
        ForgeDirection direction = getSafeDirection(chest);
        TileEntityChest adjacent = getAdjacentChest(chest);
        boolean higher = slot >= CHEST_SIZE && adjacent != null;
        boolean lower = lowerDirs.contains(direction);
        TileEntityChest activeChest = adjacent == null? chest: higher && lower || !higher && !lower ? adjacent : chest;
        slot %= CHEST_SIZE;
        return activeChest.chestContents[slot];
    }

    public static IInventory getIInventory(World world, int x, int y, int z)
    {
        TileEntityChest chest = (TileEntityChest)world.getTileEntity(x, y, z);
        if (chest == null)
            return null;
        else if (BlockChest.func_149953_o(world, x, y, z))
            return null;
        if (world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN))
            return null;
        checkForChests(chest);
        ForgeDirection dir = getSafeDirection(chest);
        if (world.isSideSolid(x + dir.offsetX, y + 1, dir.offsetY, ForgeDirection.DOWN))
            return null;
        return chest;
    }

    public static ItemStack decrStackSize(TileEntityChest chest, int slot, int amount)
    {
        ItemStack item = chest.getStackInSlot(slot);
        if (item != null)
        {
            if (item.stackSize <= amount)
            {
                chest.setInventorySlotContents(slot, null);
                chest.markDirty();
                return item;
            } else
            {
                ItemStack itemstack1 = item.splitStack(amount);
                if (item.stackSize == 0)
                {
                    chest.setInventorySlotContents(slot, null);
                } else
                {
                    chest.setInventorySlotContents(slot, item);
                }
                chest.markDirty();
                return itemstack1;
            }
        } else
        {
            return null;
        }
    }

    public static String getInventoryName(TileEntityChest chest)
    {
        TileEntityChest adj = getAdjacent(chest);
        return chest.hasCustomInventoryName() ? chest.customName : adj == null ? "container.chest" : adj.hasCustomInventoryName() ? chest.customName : "container.chestDouble";
    }

    public static void setInventorySlotContents(TileEntityChest chest, int slot, ItemStack stack)
    {
        ForgeDirection direction = getSafeDirection(chest);
        TileEntityChest adjacent = getAdjacentChest(chest);
        boolean higher = slot >= CHEST_SIZE && adjacent != null;
        boolean lower = lowerDirs.contains(direction);
        if (stack!=null && stack.stackSize > chest.getInventoryStackLimit()) stack.stackSize = chest.getInventoryStackLimit();
        TileEntityChest activeChest = adjacent == null? chest: higher && lower || !higher && !lower ? adjacent : chest;
        slot %= CHEST_SIZE;
        setStack(activeChest, slot, stack);
    }

    public static void updateEntity(TileEntityChest chest)
    {
        chest.checkForAdjacentChests();
        ++chest.ticksSinceSync;
        float f;

        if (!chest.getWorldObj().isRemote && chest.numPlayersUsing != 0 && (chest.ticksSinceSync + chest.xCoord + chest.yCoord + chest.zCoord) % 200 == 0)
        {
            chest.numPlayersUsing = 0;
            f = 5.0F;
            List list = chest.getWorldObj().getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox((double)((float)chest.xCoord - f), (double)((float)chest.yCoord - f), (double)((float)chest.zCoord - f), (double)((float)(chest.xCoord + 1) + f), (double)((float)(chest.yCoord + 1) + f), (double)((float)(chest.zCoord + 1) + f)));

            for (Object aList : list)
            {
                EntityPlayer entityplayer = (EntityPlayer)aList;

                if (entityplayer.openContainer instanceof ContainerChest)
                {
                    IInventory iinventory = ((ContainerChest)entityplayer.openContainer).getLowerChestInventory();

                    if (iinventory == chest || iinventory == getAdjacent(chest))
                    {
                        ++chest.numPlayersUsing;
                    }
                }
            }
        }

        chest.prevLidAngle = chest.lidAngle;
        f = 0.1F;
        double d2;

        if (chest.numPlayersUsing > 0 && chest.lidAngle == 0.0F && chest.adjacentChestZNeg == null && chest.adjacentChestXNeg == null)
        {
            double d1 = (double)chest.xCoord + 0.5D;
            d2 = (double)chest.zCoord + 0.5D;

            if (chest.adjacentChestZPos != null)
            {
                d2 += 0.5D;
            }

            if (chest.adjacentChestXPos != null)
            {
                d1 += 0.5D;
            }

            chest.getWorldObj().playSoundEffect(d1, (double)chest.yCoord + 0.5D, d2, "random.chestopen", 0.5F, chest.getWorldObj().rand.nextFloat() * 0.1F + 0.9F);
        }

        if (chest.numPlayersUsing == 0 && chest.lidAngle > 0.0F || chest.numPlayersUsing > 0 && chest.lidAngle < 1.0F)
        {
            float f1 = chest.lidAngle;

            if (chest.numPlayersUsing > 0)
            {
                chest.lidAngle += f;
            }
            else
            {
                chest.lidAngle -= f;
            }

            if (chest.lidAngle > 1.0F)
            {
                chest.lidAngle = 1.0F;
            }

            float f2 = 0.5F;

            if (chest.lidAngle < f2 && f1 >= f2 && chest.adjacentChestZNeg == null && chest.adjacentChestXNeg == null)
            {
                d2 = (double)chest.xCoord + 0.5D;
                double d0 = (double)chest.zCoord + 0.5D;

                if (chest.adjacentChestZPos != null)
                {
                    d0 += 0.5D;
                }

                if (chest.adjacentChestXPos != null)
                {
                    d2 += 0.5D;
                }

                chest.getWorldObj().playSoundEffect(d2, (double)chest.yCoord + 0.5D, d0, "random.chestclosed", 0.5F, chest.getWorldObj().rand.nextFloat() * 0.1F + 0.9F);
            }

            if (chest.lidAngle < 0.0F)
            {
                chest.lidAngle = 0.0F;
            }
        }
    }
    
    public static void openInventory(TileEntityChest chest)
    {
        open(chest);
        TileEntityChest adj = getAdjacentChest(chest);
        if (adj != null)
            open(adj);
    }

    public static void closeInventory(TileEntityChest chest)
    {
        close(chest);
        TileEntityChest adj = getAdjacentChest(chest);
        if (adj != null)
            close(adj);
    }

    private static void close(TileEntityChest chest)
    {
        if (chest.getWorldObj() != null && chest.getBlockType() instanceof BlockChest)
        {
            --chest.numPlayersUsing;
            chest.getWorldObj().addBlockEvent(chest.xCoord, chest.yCoord, chest.zCoord, chest.getBlockType(), 1, chest.numPlayersUsing);
            chest.getWorldObj().notifyBlocksOfNeighborChange(chest.xCoord, chest.yCoord, chest.zCoord, chest.getBlockType());
            chest.getWorldObj().notifyBlocksOfNeighborChange(chest.xCoord, chest.yCoord - 1, chest.zCoord, chest.getBlockType());
        }
    }

    private static void open(TileEntityChest chest)
    {
        if (chest.getWorldObj() != null)
        {
            if (chest.numPlayersUsing < 0)
            {
                chest.numPlayersUsing = 0;
            }
            ++chest.numPlayersUsing;
            chest.getWorldObj().addBlockEvent(chest.xCoord, chest.yCoord, chest.zCoord, chest.getBlockType(), 1, chest.numPlayersUsing);
            chest.getWorldObj().notifyBlocksOfNeighborChange(chest.xCoord, chest.yCoord, chest.zCoord, chest.getBlockType());
            chest.getWorldObj().notifyBlocksOfNeighborChange(chest.xCoord, chest.yCoord - 1, chest.zCoord, chest.getBlockType());
        }
    }

    private static void setStack(TileEntityChest chest, int slot, ItemStack stack)
    {
        chest.chestContents[slot] = stack;
        chest.markDirty();
    }

    private static TileEntityChest getAdjacentChest(TileEntityChest chest)
    {
        chest.checkForAdjacentChests();
        return getAdjacent(chest);
    }

    public static void checkForAdjacentChests(TileEntityChest chest)
    {
        if (!chest.adjacentChestChecked)
        {
            checkForChests(chest);
        }
    }

    private static void checkForChests(TileEntityChest chest)
    {
        if (chest.getWorldObj() == null)
        {
            setDirection(chest, ForgeDirection.UNKNOWN);
            setAdjacent(chest, null);
            return;
        }
        for (ForgeDirection dir : adjacentPos)
        {
            TileEntity te = chest.getWorldObj().getTileEntity(chest.xCoord + dir.offsetX, chest.yCoord, chest.zCoord + dir.offsetZ);
            if (te instanceof TileEntityChest && te.getBlockType() == chest.getBlockType())
            {
                TileEntityChest adj = (TileEntityChest)te;
                setDirection(chest, dir);
                setAdjacent(chest, adj);
                setDirection(adj, dir.getOpposite());
                setAdjacent(adj, chest);
                return;
            }
        }
        setDirection(chest, ForgeDirection.UNKNOWN);
        setAdjacent(chest, null);
    }

    public static ForgeDirection getSafeDirection(TileEntityChest chest)
    {
        ForgeDirection dir = getDirection(chest);
        if (dir == null)
        {
            checkForChests(chest);
            return getDirection(chest);
        }
        return dir;
    }

    private static ForgeDirection getDirection(TileEntityChest chest)
    {
        return ForgeDirection.UNKNOWN;
    }

    private static void setDirection(TileEntityChest chest, ForgeDirection dir)
    {
        chest.adjacentChestChecked = false;
    }

    private static TileEntityChest getAdjacent(TileEntityChest chest)
    {
        return new TileEntityChest();
    }

    private static void setAdjacent(TileEntityChest chest, TileEntityChest adj)
    {
        chest.adjacentChestXNeg = adj;
    }

    private static boolean hasAdjacentChest(TileEntityChest chest)
    {
        return getAdjacentChest(chest) != null;
    }
}
