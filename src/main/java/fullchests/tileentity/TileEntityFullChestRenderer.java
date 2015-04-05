package fullchests.tileentity;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Calendar;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelLargeChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class TileEntityFullChestRenderer extends TileEntitySpecialRenderer
{
    private static final ResourceLocation trappedDouble = new ResourceLocation("textures/entity/chest/trapped_double.png");
    private static final ResourceLocation christmasDouble = new ResourceLocation("textures/entity/chest/christmas_double.png");
    private static final ResourceLocation chestDouble = new ResourceLocation("textures/entity/chest/normal_double.png");
    private static final ResourceLocation trapped = new ResourceLocation("textures/entity/chest/trapped.png");
    private static final ResourceLocation christmas = new ResourceLocation("textures/entity/chest/christmas.png");
    private static final ResourceLocation normal = new ResourceLocation("textures/entity/chest/normal.png");
    private ModelChest modelChest = new ModelChest();
    private ModelChest modelLargeChest = new ModelLargeChest();
    private boolean isChristmas;
    private static final EnumSet<ForgeDirection> noRenderDir = EnumSet.of(ForgeDirection.WEST, ForgeDirection.NORTH);

    public TileEntityFullChestRenderer()
    {
        Calendar calendar = Calendar.getInstance();
        this.isChristmas = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DAY_OF_MONTH) >= 24 && calendar.get(Calendar.DAY_OF_MONTH) <= 26;
    }

    public void renderTileEntityAt(TileEntityFullChest chest, double x, double y, double z, float partialTick)
    {
        int metadata;

        if (!chest.hasWorldObj())
        {
            metadata = 0;
        }
        else
        {
            Block block = chest.getBlockType();
            metadata = chest.getBlockMetadata();

            if (block instanceof BlockChest && metadata == 0)
            {
                try
                {
                    ((BlockChest)block).func_149954_e(chest.getWorldObj(), chest.xCoord, chest.yCoord, chest.zCoord);
                }
                catch (ClassCastException e)
                {
                    FMLLog.severe("Attempted to render a chest at %d,  %d, %d that was not a chest", chest.xCoord, chest.yCoord, chest.zCoord);
                }
                metadata = chest.getBlockMetadata();
            }
            chest.checkForAdjacentChests();
        }

        if (!noRenderDir.contains(chest.direction))
        {
            ModelChest modelchest;
            if (chest.direction == ForgeDirection.UNKNOWN)
            {
                modelchest = this.modelChest;

                if (chest.getChestType() == 1)
                {
                    this.bindTexture(trapped);
                }
                else if (this.isChristmas)
                {
                    this.bindTexture(christmas);
                }
                else
                {
                    this.bindTexture(normal);
                }
            }
            else
            {
                modelchest = this.modelLargeChest;

                if (chest.getChestType() == 1)
                {
                    this.bindTexture(trappedDouble);
                }
                else if (this.isChristmas)
                {
                    this.bindTexture(christmasDouble);
                }
                else
                {
                    this.bindTexture(chestDouble);
                }
            }

            GL11.glPushMatrix();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslatef((float)x, (float)y + 1.0F, (float)z + 1.0F);
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            short rotation = 0;

            if (metadata == 2)
            {
                rotation = 180;
            }
            else if (metadata == 3)
            {
                rotation = 0;
            }
            else if (metadata == 4)
            {
                rotation = 90;
            }
            else if (metadata == 5)
            {
                rotation = -90;
            }

            if (metadata == 2 && chest.direction == ForgeDirection.EAST)
            {
                GL11.glTranslatef(1.0F, 0.0F, 0.0F);
            }
            else if (metadata == 5 && chest.direction == ForgeDirection.SOUTH)
            {
                GL11.glTranslatef(0.0F, 0.0F, -1.0F);
            }

            GL11.glRotatef((float)rotation, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            float f1 = chest.prevLidAngle + (chest.lidAngle - chest.prevLidAngle) * partialTick;
            float f2;

            if (chest.adjacent != null)
            {
                f2 = chest.adjacent.prevLidAngle + (chest.adjacent.lidAngle - chest.adjacent.prevLidAngle) * partialTick;

                if (f2 > f1)
                {
                    f1 = f2;
                }
            }

            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
            modelchest.chestLid.rotateAngleX = -(f1 * (float)Math.PI / 2.0F);
            modelchest.renderAll();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks)
    {
        this.renderTileEntityAt((TileEntityFullChest)tileEntity, x, y, z, partialTicks);
    }
}