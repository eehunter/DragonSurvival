package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, priority = 10000) // To make sure it's the last call in the method
public class MixinPlayerEnd {
    @Inject(method = "attack", at = @At("RETURN"))
    public void switchEnd(Entity target, CallbackInfo ci) {
        Object self = this;
        Player player = (Player) self;

        if (!DragonUtils.isDragon(player)) {
            return;
        }

        DragonStateHandler handler = DragonUtils.getHandler(player);

        if (handler.switchedItems) {
            ItemStack originalMainHand = handler.storedMainHand;
            ItemStack originalToolSlot = player.getItemInHand(InteractionHand.MAIN_HAND);

            player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);
            handler.getClawToolData().getClawsInventory().setItem(0, originalToolSlot);
            handler.switchedItems = false;
        }
    }
}
