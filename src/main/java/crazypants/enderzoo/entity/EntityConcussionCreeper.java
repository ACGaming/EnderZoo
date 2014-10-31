package crazypants.enderzoo.entity;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.ReflectionHelper;
import crazypants.enderzoo.EnderZoo;
import crazypants.enderzoo.Log;
import crazypants.enderzoo.config.Config;

public class EntityConcussionCreeper extends EntityCreeper {

  public static final String NAME = "enderzoo.ConcussionCreeper";
  public static final int EGG_BG_COL = 0x56FF8E;
  public static final int EGG_FG_COL = 0xFF0A22;

  private Field fTimeSinceIgnited;
  private Field fFuseTime;

  public EntityConcussionCreeper(World world) {
    super(world);
    try {
      fTimeSinceIgnited = ReflectionHelper.findField(EntityCreeper.class, "timeSinceIgnited", "field_70833_d");
      fTimeSinceIgnited.setAccessible(true);
      fFuseTime = ReflectionHelper.findField(EntityCreeper.class, "fuseTime", "field_82225_f");
      fFuseTime.setAccessible(true);
    } catch (Exception e) {
      Log.error("Could not create ender creeper  logic as fields not found");
    }
  }

  @Override
  public void onUpdate() {

    if(this.isEntityAlive()) {
      int timeSinceIgnited = getTimeSinceIgnited();
      int fuseTime = getFuseTime();

      if(timeSinceIgnited >= fuseTime - 1) {
        setTimeSinceIgnited(0);

        int range = Config.concussionCreeperExplosionRange;
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range);
        List<EntityLivingBase> ents = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bb);
        for (EntityLivingBase ent : ents) {
          if(ent != this) {
            if(!worldObj.isRemote) {
              boolean done = false;
              for (int i = 0; i < 20 && !done; i++) {
                done = TeleportHelper.teleportRandomly(ent, Config.concussionCreeperMaxTeleportRange);                
              }
            }
            if(ent instanceof EntityPlayer) {
              worldObj.playSoundEffect(ent.posX, ent.posY, ent.posZ, "mob.endermen.portal", 1.0F, 1.0F);              
              EnderZoo.proxy.setInstantConfusionOnPlayer((EntityPlayer) ent, Config.concussionCreeperConfusionDuration);              
            }
          }
        }

        worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 4.0F,
            (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
        worldObj.spawnParticle("hugeexplosion", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
        setDead();
      }
    }

    super.onUpdate();

  }

  private void setTimeSinceIgnited(int i) {
    if(fTimeSinceIgnited == null) {
      return;
    }
    try {
      fTimeSinceIgnited.setInt(this, i);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private int getTimeSinceIgnited() {
    if(fTimeSinceIgnited == null) {
      return 0;
    }
    try {
      return fTimeSinceIgnited.getInt(this);
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private int getFuseTime() {
    if(fFuseTime == null) {
      return 0;
    }
    try {
      return fFuseTime.getInt(this);
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

}
