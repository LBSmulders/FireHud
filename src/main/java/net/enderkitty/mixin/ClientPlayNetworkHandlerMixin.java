package net.enderkitty.mixin;

import net.enderkitty.FireHud;
import net.enderkitty.SoulFireEntityAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientLevel level;
    
    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    public void entitySetsOnSoulFire(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        if (FireHud.getConfig().renderSoulFire && level != null) {
            Entity targetEntity = level.getEntity(packet.entityId());
            Entity sourceEntity = level.getEntity(packet.sourceDirectId());
            if (targetEntity != null && sourceEntity != null) {
                if ((sourceEntity instanceof Zombie || sourceEntity instanceof Arrow) && sourceEntity.displayFireAnimation()) {
                    ((SoulFireEntityAccessor) targetEntity).fireHud$setOnSoulFire(((SoulFireEntityAccessor) sourceEntity).fireHud$isOnSoulFire());
                }
            }
            if (targetEntity != null) {
                if (packet.getSource(level).is(DamageTypes.LIGHTNING_BOLT)) {
                    ((SoulFireEntityAccessor) targetEntity).fireHud$setOnSoulFire(false);
                }
            }
        }
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    public void clientTickEvents(CallbackInfo ci) {
        if (level != null && FireHud.getConfig().renderSoulFire) {
            level.entitiesForRendering().forEach(entity -> {
                AABB box = entity.getBoundingBox();
                BlockPos blockPos = new BlockPos(Mth.floor(box.minX + 0.001), Mth.floor(box.minY + 0.001), Mth.floor(box.minZ + 0.001));
                BlockPos blockPos2 = new BlockPos(Mth.floor(box.maxX - 0.001), Mth.floor(box.maxY - 0.001), Mth.floor(box.maxZ - 0.001));
                if (entity.level() != null && entity.level().hasChunksAt(blockPos, blockPos2)) {
                    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                    for (int i = blockPos.getX(); i <= blockPos2.getX(); ++i) {
                        for (int j = blockPos.getY(); j <= blockPos2.getY(); ++j) {
                            for (int k = blockPos.getZ(); k <= blockPos2.getZ(); ++k) {
                                mutable.set(i, j, k);
                                try {
                                    Block block = entity.level().getBlockState(mutable).getBlock();
                                    if (block instanceof SoulFireBlock) ((SoulFireEntityAccessor)entity).fireHud$setOnSoulFire(true);
                                    if (block instanceof FireBlock) ((SoulFireEntityAccessor)entity).fireHud$setOnSoulFire(false);
                                    if (entity.isInLava()) ((SoulFireEntityAccessor)entity).fireHud$setOnSoulFire(false);
                                } catch (Throwable throwable) {
                                    CrashReport crashReport = CrashReport.forThrowable(throwable, "Colliding entity with block");
                                    throw new ReportedException(crashReport);
                                }
                            }
                        }
                    }
                }
            });
        }
    }
    
}
