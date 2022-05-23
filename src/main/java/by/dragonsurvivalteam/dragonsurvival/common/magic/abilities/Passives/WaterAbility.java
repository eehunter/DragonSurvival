package by.dragonsurvivalteam.dragonsurvival.common.magic.abilities.Passives;


import by.dragonsurvivalteam.dragonsurvival.common.magic.common.PassiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class WaterAbility extends PassiveDragonAbility{
	public WaterAbility(DragonType type, String abilityId, String icon, int minLevel, int maxLevel){
		super(type, abilityId, icon, minLevel, maxLevel);
	}

	@Override
	public WaterAbility createInstance(){
		return new WaterAbility(type, id, icon, minLevel, maxLevel);
	}

	@Override

	public Component getDescription(){
		return new TranslatableComponent("ds.skill.description." + getId(), getDuration() + Functions.ticksToSeconds(ServerConfig.seaTicksWithoutWater));
	}

	public int getDuration(){
		return 60 * getLevel();
	}

	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(new TranslatableComponent("ds.skill.duration.seconds", "+60"));
		return list;
	}

	@Override
	public boolean isDisabled(){
		return super.isDisabled() || !ServerConfig.water;
	}
}