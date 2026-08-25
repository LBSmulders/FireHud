package net.enderkitty.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireRenderStateAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(FlameFeatureRenderer.class)
public abstract class FlameFeatureRendererMixin {
    @Unique private static final FireHudConfig config = FireHud.getConfig();
    @Unique private static final SpriteId SOUL_FIRE_0 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("soul_fire_0");
    @Unique private static final SpriteId SOUL_FIRE_1 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("soul_fire_1");

    /**
     * 26.2 hoists the two fire sprites out of the per entity loop in {@code buildGroup}, so they can no longer be
     * swapped with a captured local without recolouring every burning entity at once. Wrapping the per submit
     * {@code prepare} call keeps the swap (and the cancel) scoped to a single entity.
     */
    @WrapOperation(method = "buildGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer;prepare(Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer$Submit;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void fireHud$prepare(FlameFeatureRenderer instance, FlameFeatureRenderer.Submit submit, VertexConsumer buffer,
                                 TextureAtlasSprite fire1, TextureAtlasSprite fire2, Operation<Void> original,
                                 @Local(argsOnly = true) FeatureFrameContext context) {
        Minecraft client = Minecraft.getInstance();

        if (client.player != null && client.player.isOnFire()) {
            if (!config.renderThirdPersonFireInLava && client.player.isInLava()) return;
            if (!config.renderWithFireResistance && client.player.hasEffect(MobEffects.FIRE_RESISTANCE)) return;
            if (!config.renderThirdPersonFire) return;
        }

        if (config.renderSoulFire && ((SoulFireRenderStateAccessor) submit.entityRenderState()).fireHud$onSoulFire()) {
            original.call(instance, submit, buffer, context.atlasManager().get(SOUL_FIRE_0), context.atlasManager().get(SOUL_FIRE_1));
        } else {
            original.call(instance, submit, buffer, fire1, fire2);
        }
    }
}
