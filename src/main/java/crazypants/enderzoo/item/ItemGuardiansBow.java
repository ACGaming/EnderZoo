package crazypants.enderzoo.item;

import crazypants.enderzoo.EnderZooTab;
import crazypants.enderzoo.config.Config;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemGuardiansBow extends ItemBow {

  public static final String NAME = "guardiansBow";

  private int drawTime = Config.guardiansBowDrawTime;
  private float damageBonus = Config.guardiansBowDamageBonus;
  private float forceMultiplier = Config.guardiansBowForceMultiplier;
  private float fovMultiplier = Config.guardiansBowFovMultiplier;

  public static ItemGuardiansBow create() {
    ItemGuardiansBow res = new ItemGuardiansBow();
    res.init();
    MinecraftForge.EVENT_BUS.register(res);
    return res;
  }

  protected ItemGuardiansBow() {
    setUnlocalizedName(NAME);
    setCreativeTab(EnderZooTab.tabEnderZoo);
    setMaxDamage(800);
    setHasSubtypes(false);
  }

  protected void init() {
    GameRegistry.registerItem(this, NAME);    
  }

  public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
    if (! (entityLiving instanceof EntityPlayer)) {
      return;
    }
    EntityPlayer entityplayer = (EntityPlayer) entityLiving;
    boolean hasInfinateArrows = entityplayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.infinity, stack) > 0;
    ItemStack itemstack = getArrowsToShoot(entityplayer);
    int draw = getMaxItemUseDuration(stack) - timeLeft;
    draw = ForgeEventFactory.onArrowLoose(stack, worldIn, (EntityPlayer) entityLiving, draw, itemstack != null || hasInfinateArrows);
    if (draw < 0){
      return;
    }

    if(itemstack == null && hasInfinateArrows) {
      itemstack = new ItemStack(Items.arrow);
    }
    
    if (itemstack == null) {
      return;
    }

    float drawRatio = func_185059_b(draw);
    if (drawRatio >= 0.1) {
      boolean arrowIsInfinite = hasInfinateArrows && itemstack.getItem() instanceof ItemArrow;
      if (!worldIn.isRemote) {
        ItemArrow itemarrow = (ItemArrow) ((ItemArrow) (itemstack.getItem() instanceof ItemArrow ? itemstack.getItem() : Items.arrow));
        EntityArrow entityarrow = itemarrow.makeTippedArrow(worldIn, itemstack, entityplayer);
        entityarrow.func_184547_a(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, drawRatio * 3.0F * forceMultiplier, 1.0F);

        if (drawRatio == 1.0F) {
          entityarrow.setIsCritical(true);
        }
        int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.power, stack);
        if (powerLevel > 0) {
          entityarrow.setDamage(entityarrow.getDamage() + (double) powerLevel * 0.5D + 0.5D);
        }
        int knockBack = EnchantmentHelper.getEnchantmentLevel(Enchantments.punch, stack);
        if (knockBack > 0) {
          entityarrow.setKnockbackStrength(knockBack);
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.flame, stack) > 0) {
          entityarrow.setFire(100);
        }

        stack.damageItem(1, entityplayer);

        if (arrowIsInfinite) {
          entityarrow.canBePickedUp = EntityArrow.PickupStatus.CREATIVE_ONLY;
        }
        
        entityarrow.setDamage(entityarrow.getDamage() + damageBonus);
        
        worldIn.spawnEntityInWorld(entityarrow);
      }

      worldIn.playSound((EntityPlayer) null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.entity_arrow_shoot, SoundCategory.NEUTRAL,
          1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + drawRatio * 0.5F);

      if (!arrowIsInfinite) {
        --itemstack.stackSize;
        if (itemstack.stackSize == 0) {
          entityplayer.inventory.deleteStack(itemstack);
        }
      }
      entityplayer.addStat(StatList.func_188057_b(this));
    }

  }
  
//  @Override
//  public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase elb, int timeLeft) {
//    if (! (elb instanceof EntityPlayer)) {
//      return;
//    }
//    EntityPlayer player = (EntityPlayer)elb;
//    
//    int drawDuration = getMaxItemUseDuration(stack) - timeLeft;
//    boolean infiniteArrows = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.infinity, stack) > 0;
//    if(infiniteArrows || player.inventory.hasItemStack(new ItemStack(Items.arrow))) {
//
//      ItemStack itemstack = func_185060_a(player);
//      int i = this.getMaxItemUseDuration(stack) - timeLeft;
//      i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, player, i, itemstack != null || flag);
//      if (i < 0) return;
//      
//      float force = drawDuration / (float) getDrawTime();
//      force = (force * force + force * 2.0F) / 3.0F;
//
//      if(force < 0.2D) {
//        return;
//      }
//      if(force > 1.0F) {
//        force = 1.0F;
//      }
//
//      EntityArrow entityarrow = new EntityArrow(world, player, force * forceMultiplier);
//      if(force == 1.0F) {
//        entityarrow.setIsCritical(true);
//      }
//
//      entityarrow.setDamage(entityarrow.getDamage() + damageBonus);
//
//      int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
//      if(powerLevel > 0) {
//        entityarrow.setDamage(entityarrow.getDamage() + powerLevel * 0.5D + 0.5D);
//      }
//
//      int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
//      if(punchLevel > 0) {
//        entityarrow.setKnockbackStrength(punchLevel);
//      }
//
//      if(EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0) {
//        entityarrow.setFire(100);
//      }
//      stack.damageItem(1, player);
//      world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + force * 0.5F);
//
//      if(infiniteArrows) {
//        entityarrow.canBePickedUp = 2;
//      } else {
//        player.inventory.consumeInventoryItem(Items.arrow);
//      }
//      if(!world.isRemote) {
//        world.spawnEntityInWorld(entityarrow);
//      }
//    }
//  }

  private ItemStack getArrowsToShoot(EntityPlayer player) {
    if (isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
      return player.getHeldItem(EnumHand.OFF_HAND);
    } else if (isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
      return player.getHeldItem(EnumHand.MAIN_HAND);
    } else {
      for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
        ItemStack itemstack = player.inventory.getStackInSlot(i);
        if (isArrow(itemstack)) {
          return itemstack;
        }
      }
      return null;
    }
  }

  private boolean isArrow(ItemStack stack) {
    return stack != null && stack.getItem() instanceof ItemArrow;
  }
  
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void onFovUpdateEvent(FOVUpdateEvent fovEvt) {
    ItemStack currentItem = fovEvt.getEntity().getHeldItemMainhand();
    if(currentItem == null || currentItem.getItem() != this || fovEvt.getEntity().getItemInUseCount() <= 0) {
      return;
    }

    int drawDuration = getMaxItemUseDuration(currentItem) - fovEvt.getEntity().getItemInUseCount();
    float ratio = drawDuration / (float) getDrawTime();

    if(ratio > 1.0F) {
      ratio = 1.0F;
    } else {
      ratio *= ratio;
    }
    fovEvt.setNewfov((1.0F - ratio * fovMultiplier));

  }

  @Override
  public int getMaxItemUseDuration(ItemStack p_77626_1_) {
    return 72000;
  }

  /**
   * returns the action that specifies what animation to play when the items is
   * being used
   */
  @Override
  public EnumAction getItemUseAction(ItemStack p_77661_1_) {    
    return EnumAction.BOW;
  }

  //TODO: 1.9 Still needed?
//  @Override
//  public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
//    ArrowNockEvent event = new ArrowNockEvent(player, stack, hand, world, bFull3D);
//    MinecraftForge.EVENT_BUS.post(event);
//    if(event.isCanceled()) {
//      return event.getAction();
//    }
//    if(player.capabilities.isCreativeMode || player.inventory.hasItemStack(new ItemStack(Items.arrow))) {
//      player.setItemInUse(stack, getMaxItemUseDuration(stack));
//    }
//    return stack;
//  }

  @Override
  public int getItemEnchantability() {
    return 1;
  }

  //TODO: 1.9
//  @Override
//  @SideOnly(Side.CLIENT)
//  public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining) {
//    return GuardiansBowModelLoader.getModel(stack, player, useRemaining);
//  }

  public int getDrawTime() {
    return drawTime;
  }

  public void setDrawTime(int drawTime) {
    this.drawTime = drawTime;
  }

}
