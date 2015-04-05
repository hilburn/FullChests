package fullchests.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.List;

public class TileEntityFullChest extends TileEntity implements IInventory
{
    public static int SIZE = 27;
    public TileEntityFullChest adjacent;
    protected ForgeDirection direction = ForgeDirection.UNKNOWN;
    protected ItemStack[] inventory = new ItemStack[SIZE];
    private String customName;
    public static final EnumSet<ForgeDirection> adjacentPos = EnumSet.of(ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST);
    public static final EnumSet<ForgeDirection> lowerDirs = EnumSet.of(ForgeDirection.UNKNOWN, ForgeDirection.EAST, ForgeDirection.SOUTH);
    private boolean adjacentChecked = false;
    private int cachedChestType = -1;
    private int numPlayersUsing = 0;
    public float lidAngle;
    private int ticksSinceSync;
    public float prevLidAngle;

    public TileEntityFullChest()
    {
        this.cachedChestType = -1;
    }

    @SideOnly(Side.CLIENT)
    public TileEntityFullChest(int type)
    {
        this.cachedChestType = type;
        this.blockType = type == 0? Blocks.chest : Blocks.trapped_chest;
        this.blockMetadata = 0;
    }

    @Override
    public int getSizeInventory()
    {
        checkForAdjacentChests();
        return adjacent != null ? SIZE * 2 : SIZE;
    }

    public void updateEntity()
    {
        super.updateEntity();
        this.checkForAdjacentChests();
        ++this.ticksSinceSync;
        float f;

        if (!this.worldObj.isRemote && this.numPlayersUsing != 0 && (this.ticksSinceSync + this.xCoord + this.yCoord + this.zCoord) % 200 == 0)
        {
            this.numPlayersUsing = 0;
            f = 5.0F;

            List list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox((double)((float)this.xCoord - f), (double)((float)this.yCoord - f), (double)((float)this.zCoord - f), (double)((float)(this.xCoord + 1) + f), (double)((float)(this.yCoord + 1) + f), (double)((float)(this.zCoord + 1) + f)));
            for (Object aList : list)
            {
                EntityPlayer entityplayer = (EntityPlayer)aList;
                if (entityplayer.openContainer instanceof ContainerChest)
                {
                    IInventory iinventory = ((ContainerChest)entityplayer.openContainer).getLowerChestInventory();

                    if (iinventory == this || iinventory == adjacent)
                    {
                        ++this.numPlayersUsing;
                    }
                }
            }
        }

        if (worldObj.isRemote)
        {
            this.prevLidAngle = this.lidAngle;
            f = 0.1F;
            double xMid = (double)this.xCoord + 0.5D + direction.offsetX/2;
            double zMid = (double)this.zCoord + 0.5D + direction.offsetZ/2;

            if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F && lowerDirs.contains(direction))
            {
                this.worldObj.playSoundEffect(xMid, (double)this.yCoord + 0.5D, zMid, "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F)
            {
                float f1 = this.lidAngle;

                if (this.numPlayersUsing > 0)
                {
                    this.lidAngle += f;
                } else
                {
                    this.lidAngle -= f;
                }

                if (this.lidAngle > 1.0F)
                {
                    this.lidAngle = 1.0F;
                }

                float f2 = 0.5F;

                if (this.lidAngle < f2 && f1 >= f2 && lowerDirs.contains(direction))
                {
                    this.worldObj.playSoundEffect(xMid, (double)this.yCoord + 0.5D, zMid, "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
                }

                if (this.lidAngle < 0.0F)
                {
                    this.lidAngle = 0.0F;
                }
            }
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return getActiveChest(slot).inventory[slot % SIZE];
    }
    
    private TileEntityFullChest getActiveChest(int slot)
    {
        boolean higher = slot >= SIZE && adjacent != null;
        boolean lower = lowerDirs.contains(direction);
        return adjacent == null? this: higher && lower || !higher && !lower ? adjacent : this;
    }
    
    @Override
    public ItemStack decrStackSize(int slot, int amount)
    {
        TileEntityFullChest chest = getActiveChest(slot);
        slot %= SIZE;
        ItemStack item = chest.inventory[slot];
        if (item != null)
        {
            if (item.stackSize <= amount)
            {
                chest.inventory[slot] = null;
                markDirty();
                return item;
            } else
            {
                ItemStack itemstack1 = item.splitStack(amount);
                if (item.stackSize == 0)
                {
                    chest.inventory[slot] = null;
                } else
                {
                    chest.inventory[slot] =  item;
                }
                markDirty();
                return itemstack1;
            }
        } else
        {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        ItemStack stack = this.inventory[slot];
        this.inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        TileEntityFullChest chest = getActiveChest(slot);
        slot %= SIZE;
        chest.inventory[slot] = stack;
    }

    @Override
    public String getInventoryName()
    {
        return hasCustomName() ? getName() : adjacent == null ? "container.chest" : adjacent.hasCustomName() ? adjacent.getName() : "container.chestDouble";
    }

    private String getName()
    {
        return customName;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return hasCustomName() || (adjacent != null && adjacent.hasCustomName());
    }

    private boolean hasCustomName()
    {
        return customName != null;
    }

    public void setCustomName(String name)
    {
        customName = name;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public void markDirty()
    {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return isUsable(player) && (adjacent == null || adjacent.isUsable(player));
    }

    private boolean isUsable(EntityPlayer player)
    {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public boolean receiveClientEvent(int id, int val)
    {
        if (id == 1)
        {
            this.numPlayersUsing = val;
            return true;
        }
        else
        {
            return super.receiveClientEvent(id, val);
        }
    }

    @Override
    public void openInventory()
    {
        if (this.numPlayersUsing < 0)
        {
            this.numPlayersUsing = 0;
        }

        ++this.numPlayersUsing;
        this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numPlayersUsing);
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
    }

    @Override
    public void closeInventory()
    {
        if (this.getBlockType() instanceof BlockChest)
        {
            --this.numPlayersUsing;
            this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numPlayersUsing);
            this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return true;
    }

    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        NBTTagList nbttaglist = tagCompound.getTagList("Items", 10);
        this.inventory = new ItemStack[SIZE];

        if (tagCompound.hasKey("CustomName", 8))
        {
            this.customName = tagCompound.getString("CustomName");
        }

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound slotTag = nbttaglist.getCompoundTagAt(i);
            int slot = slotTag.getByte("Slot") & 255;

            if (slot >= 0 && slot < this.inventory.length)
            {
                this.inventory[slot] = ItemStack.loadItemStackFromNBT(slotTag);
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.inventory.length; ++i)
        {
            if (this.inventory[i] != null)
            {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setByte("Slot", (byte)i);
                this.inventory[i].writeToNBT(slotTag);
                nbttaglist.appendTag(slotTag);
            }
        }
        tagCompound.setTag("Items", nbttaglist);
        if (this.hasCustomName())
        {
            tagCompound.setString("CustomName", this.customName);
        }
    }

    public void checkForAdjacentChests()
    {
        if (!adjacentChecked)
        {
            if (worldObj == null)
            {
                direction =  ForgeDirection.UNKNOWN;
                adjacent = null;
                return;
            }
            for (ForgeDirection dir : adjacentPos)
            {
                TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord, zCoord + dir.offsetZ);
                if (te instanceof TileEntityFullChest && ((TileEntityFullChest)te).getChestType() == getChestType())
                {
                    TileEntityFullChest adj = (TileEntityFullChest)te;
                    direction =  dir;
                    adjacent = adj;
                    adjacentChecked = true;
                    adj.direction =  dir.getOpposite();
                    adj.adjacent = this;
                    adj.adjacentChecked = true;
                    return;
                }
            }
            direction =  ForgeDirection.UNKNOWN;
            adjacent = null;
            adjacentChecked = true;
        }
    }

    public int getChestType()
    {
        if (this.cachedChestType == -1)
        {
            if (this.worldObj == null || !(this.getBlockType() instanceof BlockChest))
            {
                return 0;
            }

            this.cachedChestType = ((BlockChest)this.getBlockType()).field_149956_a;
        }

        return this.cachedChestType;
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        this.updateContainingBlockInfo();
    }

    @Override
    public void updateContainingBlockInfo()
    {
        super.updateContainingBlockInfo();
        this.clearAdjacent();
        this.adjacentChecked = false;
    }

    public ForgeDirection getDirection()
    {
        return direction;
    }

    public void clearAdjacent()
    {
        if (adjacent!=null)
        {
            adjacent.adjacentChecked = false;
            adjacent.direction = ForgeDirection.UNKNOWN;
            adjacent.adjacent = null;
            direction = ForgeDirection.UNKNOWN;
            adjacent = null;
        }
    }
}
