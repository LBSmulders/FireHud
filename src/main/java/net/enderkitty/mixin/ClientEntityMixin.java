package net.enderkitty.mixin;

import net.enderkitty.ClientFireTick;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireEntityAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class ClientEntityMixin implements SoulFireEntityAccessor {
    @Shadow public abstract boolean fireImmune();

    @Unique private boolean soulFire;
    
    @Override
    public boolean fireHud$isOnSoulFire() {
        return soulFire;
    }
    
    @Override
    public void fireHud$setOnSoulFire(boolean onSoulFire) {
        this.soulFire = onSoulFire;
    }
    
    @Inject(method = "lavaIgnite", at = @At(value = "HEAD"))
    private void lavaIgnite(CallbackInfo ci) {
        Entity thisObject = (Entity) (Object) this;
        if (FireHud.getConfig().thermometer && thisObject instanceof LocalPlayer player && !this.fireImmune()) {
            ((ClientFireTick) player).fireHud$setClientFireFor(15.0f);
        }
    }
    @Inject(method = "thunderHit", at = @At(value = "HEAD"))
    private void thunderHit(ServerLevel level, LightningBolt lightningBolt, CallbackInfo ci) {
        Entity thisObject = (Entity) (Object) this;
        if (FireHud.getConfig().thermometer && thisObject instanceof LocalPlayer player) {
            ((ClientFireTick) player).fireHud$setClientFireTick(((ClientFireTick) player).fireHud$clientFireTick() + 1);
            if (((ClientFireTick) player).fireHud$clientFireTick() == 0) {
                ((ClientFireTick) player).fireHud$setClientFireFor(8.0f);
            }
        }
    }
}
