package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.misc.PrincessTrades;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Horseless princess
 */
public class Princess extends Villager{
	private static final List<DyeColor> colors = Arrays.asList(DyeColor.RED, DyeColor.YELLOW, DyeColor.PURPLE, DyeColor.BLUE, DyeColor.BLACK, DyeColor.WHITE);
	public static EntityDataAccessor<Integer> color = SynchedEntityData.defineId(Princess.class, EntityDataSerializers.INT);

	public Princess(EntityType<? extends Villager> entityType, Level world){
		super(entityType, world);
	}

	public Princess(EntityType<? extends Villager> entityType, Level world, VillagerType villagerType){
		super(entityType, world, villagerType);
	}

	@Override
	public SoundEvent getNotifyTradeSound(){
		return null;
	}

	@Override
	protected SoundEvent getTradeUpdatedSound(boolean p_213721_1_){
		return null;
	}

	public void playCelebrateSound(){
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> p_213364_1_){
		return brainProvider().makeBrain(p_213364_1_);
	}

	public void refreshBrain(ServerLevel p_213770_1_){
	}

	protected void defineSynchedData(){
		super.defineSynchedData();
		this.entityData.define(color, 0);
	}

	public void addAdditionalSaveData(CompoundTag compoundNBT){
		super.addAdditionalSaveData(compoundNBT);
		compoundNBT.putInt("Color", getColor());
	}

	public int getColor(){
		return this.entityData.get(color);
	}

	public void readAdditionalSaveData(CompoundTag compoundNBT){
		super.readAdditionalSaveData(compoundNBT);
		setColor(compoundNBT.getInt("Color"));
	}

	public void setColor(int i){
		this.entityData.set(color, i);
	}

	@Override
	public boolean removeWhenFarAway(double p_213397_1_){
		return !this.hasCustomName() && tickCount >= Functions.minutesToTicks(ServerConfig.princessDespawnDelay);
	}

	@Nullable
	protected SoundEvent getAmbientSound(){
		return null;
	}

	protected SoundEvent getHurtSound(DamageSource p_184601_1_){
		return SoundEvents.GENERIC_HURT;
	}

	protected SoundEvent getDeathSound(){
		return SoundEvents.PLAYER_DEATH;
	}

	public void playWorkSound(){
	}

	@Override
	public void die(DamageSource damageSource){
		super.die(damageSource);
		Item flower = Items.AIR;
		DyeColor dyeColor = DyeColor.byId(getColor());
		switch(dyeColor){
			case BLUE:
				flower = Items.BLUE_ORCHID;
				break;
			case RED:
				flower = Items.RED_TULIP;
				break;
			case BLACK:
				flower = Items.WITHER_ROSE;
				break;
			case YELLOW:
				flower = Items.DANDELION;
				break;
			case PURPLE:
				flower = Items.LILAC;
				break;
			case WHITE:
				flower = Items.LILY_OF_THE_VALLEY;
				break;
		}
		if(!level.isClientSide){
			level.addFreshEntity(new ItemEntity(level, getX(), getY(), getZ(), new ItemStack(flower)));
		}
	}

	public boolean canBreed(){
		return false;
	}

	protected Component getTypeName(){
		return new TranslatableComponent(this.getType().getDescriptionId());
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverWorld, DifficultyInstance difficultyInstance, MobSpawnType reason,
		@Nullable
			SpawnGroupData livingEntityData,
		@Nullable
			CompoundTag compoundNBT){
		setColor(colors.get(this.random.nextInt(6)).getId());
		setVillagerData(getVillagerData().setProfession(DSEntities.PRINCESS_PROFESSION));
		return super.finalizeSpawn(serverWorld, difficultyInstance, reason, livingEntityData, compoundNBT);
	}

	public void thunderHit(ServerLevel p_241841_1_, LightningBolt p_241841_2_){
	}

	protected void updateTrades(){
		VillagerData villagerdata = getVillagerData();
		Int2ObjectMap<ItemListing[]> int2objectmap = PrincessTrades.colorToTrades.get(getColor());
		if(int2objectmap != null && !int2objectmap.isEmpty()){
			VillagerTrades.ItemListing[] trades = int2objectmap.get(villagerdata.getLevel());
			if(trades != null){
				MerchantOffers merchantoffers = getOffers();
				addOffersFromItemListings(merchantoffers, trades, 2);
			}
		}
	}

	public void gossip(ServerLevel p_242368_1_, Villager p_242368_2_, long p_242368_3_){
	}

	public void startSleeping(BlockPos p_213342_1_){
	}

	protected void pickUpItem(Item p_175445_1_){
	}

	protected void registerGoals(){
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5D){
			public boolean canUse(){
				return (!Princess.this.isTrading() && super.canUse());
			}
		});
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
		goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Player.class, 16, 1, 1, living -> {
			return DragonUtils.isDragon(living) && living.hasEffect(DragonEffects.EVIL_DRAGON);
		}));
		goalSelector.addGoal(7, new PanicGoal(this, 1));
	}
}