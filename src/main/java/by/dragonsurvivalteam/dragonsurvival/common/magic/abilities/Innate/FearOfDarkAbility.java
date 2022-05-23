package by.dragonsurvivalteam.dragonsurvival.common.magic.abilities.Innate;


import by.dragonsurvivalteam.dragonsurvival.common.magic.common.InnateDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FearOfDarkAbility extends InnateDragonAbility{
	public FearOfDarkAbility(DragonType type, String abilityId, String icon, int minLevel, int maxLevel){
		super(type, abilityId, icon, minLevel, maxLevel);
	}

	@Override
	public FearOfDarkAbility createInstance(){
		return new FearOfDarkAbility(type, id, icon, minLevel, maxLevel);
	}

	@Override
	public Component getDescription(){
		return new TranslatableComponent("ds.skill.description." + getId(), 3, ServerConfig.caveWaterDamage, 0.5);
	}

	@Override
	public int getLevel(){
		return ServerConfig.penalties && ServerConfig.forestStressTicks != 0.0 ? 1 : 0;
	}

	@OnlyIn( Dist.CLIENT )
	public boolean isDisabled(){
		return super.isDisabled() || !ServerConfig.penalties || ServerConfig.forestStressTicks == 0.0;
	}
}