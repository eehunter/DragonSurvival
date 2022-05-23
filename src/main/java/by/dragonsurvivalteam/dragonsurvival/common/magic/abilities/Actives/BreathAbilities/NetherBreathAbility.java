package by.dragonsurvivalteam.dragonsurvival.common.magic.abilities.Actives.BreathAbilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.LargeFireParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.SmallFireParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.FireBreathSound;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.SoundRegistry;
import by.dragonsurvivalteam.dragonsurvival.common.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.GenericCapability;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.GenericCapabilityProvider;
import by.dragonsurvivalteam.dragonsurvival.common.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;

import java.util.ArrayList;

public class NetherBreathAbility extends BreathAbility{
	@OnlyIn( Dist.CLIENT )
	private SoundInstance startingSound;
	@OnlyIn( Dist.CLIENT )
	private TickableSoundInstance loopingSound;
	@OnlyIn( Dist.CLIENT )
	private SoundInstance endSound;

	public NetherBreathAbility(DragonType type, String id, String icon, int minLevel, int maxLevel, int manaCost, int castTime, int cooldown, Integer[] requiredLevels){
		super(type, id, icon, minLevel, maxLevel, manaCost, castTime, cooldown, requiredLevels);
	}

	@Override
	public NetherBreathAbility createInstance(){
		return new NetherBreathAbility(type, id, icon, minLevel, maxLevel, manaCost, castTime, abilityCooldown, requiredLevels);
	}

	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(new TranslatableComponent("ds.skill.damage", "+" + ServerConfig.fireBreathDamage));
		return list;
	}

	@Override
	public boolean isDisabled(){
		return super.isDisabled() || !ServerConfig.fireBreath;
	}	@Override
	public int getManaCost(){
		return player != null && player.hasEffect(DragonEffects.SOURCE_OF_MAGIC) ? 0 : (firstUse ? ServerConfig.fireBreathInitialMana : ServerConfig.fireBreathOvertimeMana);
	}




	@Override
	public void onBlock(BlockPos pos, BlockState blockState, Direction direction){
		if(!player.level.isClientSide){
			if(ServerConfig.fireBreathSpreadsFire){
				BlockPos blockPos = pos.relative(direction);

				if(FireBlock.canBePlacedAt(player.level, blockPos, direction)){
					boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(player.level, player);

					if(flag){
						if(player.level.random.nextInt(100) < 50){
							BlockState blockstate1 = FireBlock.getState(player.level, blockPos);
							player.level.setBlock(blockPos, blockstate1, 3);
						}
					}
				}
			}
			DragonStateHandler handler = DragonUtils.getHandler(player);

			if(player.level.random.nextInt(100) < (handler.getMagic().getAbilityLevel(DragonAbilities.BURN) * 15)){
				BlockState blockAbove = player.level.getBlockState(pos.above());

				if(blockAbove.getBlock() == Blocks.AIR){
					AreaEffectCloud entity = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, player.level);
					entity.setWaitTime(0);
					entity.setPos(pos.above().getX(), pos.above().getY(), pos.above().getZ());
					entity.setPotion(new Potion(new MobEffectInstance(DragonEffects.BURN, Functions.secondsToTicks(10) * 4))); //Effect duration is divided by 4 normaly
					entity.setDuration(Functions.secondsToTicks(2));
					entity.setRadius(1);
					entity.setParticle(new SmallFireParticleData(37, false));
					player.level.addFreshEntity(entity);
				}
			}
		}


		if(player.level.isClientSide){
			for(int z = 0; z < 4; ++z){
				if(player.level.random.nextInt(100) < 20){
					player.level.addParticle(ParticleTypes.LAVA, pos.above().getX(), pos.above().getY(), pos.above().getZ(), 0, 0.05, 0);
				}
			}
		}

		if(player.level.isClientSide){
			if(blockState.getBlock() == Blocks.WATER){
				for(int z = 0; z < 4; ++z){
					if(player.level.random.nextInt(100) < 90){
						player.level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, pos.above().getX(), pos.above().getY(), pos.above().getZ(), 0, 0.05, 0);
					}
				}
			}
		}
	}

	public void tickCost(){
		if(firstUse || player.tickCount % ServerConfig.fireBreathManaTicks == 0){
			consumeMana(player);
			firstUse = false;
		}
	}


	@Override
	public void stopCasting(){
		if(castingTicks > 1){
			if(player.level.isClientSide){
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (SafeRunnable)() -> stopSound());
			}
		}

		super.stopCasting();
	}


	@Override
	public void onActivation(Player player){
		tickCost();
		super.onActivation(player);

		if(player.isInWaterRainOrBubble() || player.level.isRainingAt(player.blockPosition())){
			if(player.level.isClientSide){
				if(player.tickCount % 10 == 0){
					player.playSound(SoundEvents.LAVA_EXTINGUISH, 0.25F, 1F);
				}

				for(int i = 0; i < 12; i++){
					double xSpeed = speed * 1f * xComp;
					double ySpeed = speed * 1f * yComp;
					double zSpeed = speed * 1f * zComp;
					player.level.addParticle(ParticleTypes.SMOKE, dx, dy, dz, xSpeed, ySpeed, zSpeed);
				}
			}
			return;
		}

		if(player.level.isClientSide){
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (SafeRunnable)() -> sound());
		}

		if(player.level.isClientSide){
			for(int i = 0; i < 24; i++){
				double xSpeed = speed * 1f * xComp;
				double ySpeed = speed * 1f * yComp;
				double zSpeed = speed * 1f * zComp;
				player.level.addParticle(new SmallFireParticleData(37, true), dx, dy, dz, xSpeed, ySpeed, zSpeed);
			}

			for(int i = 0; i < 10; i++){
				double xSpeed = speed * xComp + (spread * 0.7 * (player.level.random.nextFloat() * 2 - 1) * (Math.sqrt(1 - xComp * xComp)));
				double ySpeed = speed * yComp + (spread * 0.7 * (player.level.random.nextFloat() * 2 - 1) * (Math.sqrt(1 - yComp * yComp)));
				double zSpeed = speed * zComp + (spread * 0.7 * (player.level.random.nextFloat() * 2 - 1) * (Math.sqrt(1 - zComp * zComp)));
				player.level.addParticle(new LargeFireParticleData(37, false), dx, dy, dz, xSpeed, ySpeed, zSpeed);
			}
		}

		hitEntities();

		if(player.tickCount % 10 == 0){
			hitBlocks();
		}
	}


	@OnlyIn( Dist.CLIENT )
	public void sound(){
		if(castingTicks == 2){
			if(startingSound == null){
				startingSound = SimpleSoundInstance.forAmbientAddition(SoundRegistry.fireBreathStart);
			}
			Minecraft.getInstance().getSoundManager().play(startingSound);
			loopingSound = new FireBreathSound(this);

			Minecraft.getInstance().getSoundManager().stop(new ResourceLocation(DragonSurvivalMod.MODID, "fire_breath_loop"), SoundSource.PLAYERS);
			Minecraft.getInstance().getSoundManager().play(loopingSound);
		}
	}

	@OnlyIn( Dist.CLIENT )
	public void stopSound(){
		castingTicks = 0;

		if(SoundRegistry.fireBreathEnd != null){
			if(endSound == null){
				endSound = SimpleSoundInstance.forAmbientAddition(SoundRegistry.fireBreathEnd);
			}

			Minecraft.getInstance().getSoundManager().play(endSound);
		}

		Minecraft.getInstance().getSoundManager().stop(new ResourceLocation(DragonSurvivalMod.MODID, "fire_breath_loop"), SoundSource.PLAYERS);
	}

	@Override
	public boolean canHitEntity(LivingEntity entity){
		return (!(entity instanceof Player) || player.canHarmPlayer(((Player)entity))) && !entity.fireImmune();
	}

	@Override
	public void onEntityHit(LivingEntity entityHit){
		//Short enough fire duration to not cause fire damage but still drop cooked items
		if(!entityHit.isOnFire()){
			entityHit.setRemainingFireTicks(1);
		}

		super.onEntityHit(entityHit);

		if(!entityHit.level.isClientSide){
			DragonStateHandler handler = DragonUtils.getHandler(player);

			if(entityHit.level.random.nextInt(100) < (handler.getMagic().getAbilityLevel(DragonAbilities.BURN) * 15)){
				GenericCapability cap = GenericCapabilityProvider.getGenericCapability(entityHit).orElse(null);
				cap.lastAfflicted = player != null ? player.getId() : -1;

				entityHit.addEffect(new MobEffectInstance(DragonEffects.BURN, Functions.secondsToTicks(10), 0, false, true));
			}
		}
	}

	@Override
	public void onDamage(LivingEntity entity){
		entity.setSecondsOnFire(30);
	}


	public static float getDamage(int level){
		return (float)(ServerConfig.fireBreathDamage * level);
	}

	public float getDamage(){
		return getDamage(getLevel());
	}
}