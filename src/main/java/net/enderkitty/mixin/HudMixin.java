package net.enderkitty.mixin;

import net.enderkitty.EnchantTags;
import net.enderkitty.FireHud;
import net.enderkitty.SoulFireEntityAccessor;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public abstract class HudMixin {
    @Unique private static final Identifier FIRE_VIGNETTE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "textures/fire/fire_vignette.png");
    @Unique private static final Identifier SOUL_FIRE_VIGNETTE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "textures/fire/soul_fire_vignette.png");

    @Unique private static final Identifier FIRE_HEART_FULL_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_full");
    @Unique private static final Identifier FIRE_HEART_FULL_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_full_blinking");
    @Unique private static final Identifier FIRE_HEART_HALF_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_half");
    @Unique private static final Identifier FIRE_HEART_HALF_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_half_blinking");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_FULL_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_hardcore_full");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_hardcore_full_blinking");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_HALF_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_hardcore_half");
    @Unique private static final Identifier FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/fire_hardcore_half_blinking");

    @Unique private static final Identifier SOUL_FIRE_HEART_FULL_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_full");
    @Unique private static final Identifier SOUL_FIRE_HEART_FULL_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_full_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HALF_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_half");
    @Unique private static final Identifier SOUL_FIRE_HEART_HALF_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_half_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_FULL_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_full");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_full_blinking");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_HALF_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_half");
    @Unique private static final Identifier SOUL_FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE = Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "hud/heart/soul_fire_hardcore_half_blinking");

    @Unique FireHudConfig config = FireHud.getConfig();


    @Inject(method = "extractHeart", at = @At("HEAD"), cancellable = true)
    private void drawHeart(GuiGraphicsExtractor graphics, Hud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo ci) {
        if (Minecraft.getInstance().getCameraEntity() instanceof Player playerEntity && !(!config.renderWithFireResistance && playerEntity.hasEffect(MobEffects.FIRE_RESISTANCE))) {
            if (config.renderFireHearts && type == Hud.HeartType.NORMAL) {
                boolean hasFrostWalkerOnBoots = false;
                for (Holder<Enchantment> enchantment : playerEntity.getItemBySlot(EquipmentSlot.FEET).getEnchantments().keySet()) {
                    if (ClientTags.isInWithLocalFallback(EnchantTags.FROST_WALKER, enchantment)) {
                        hasFrostWalkerOnBoots = true;
                    }
                }
                boolean isOnSoulFire = ((SoulFireEntityAccessor) playerEntity).fireHud$isOnSoulFire();
                if (playerEntity.isOnFire() || (!hasFrostWalkerOnBoots && ((playerEntity.getBlockStateOn().getBlock() == Blocks.MAGMA_BLOCK && !playerEntity.isSteppingCarefully()) ||
                        playerEntity.getBlockStateOn().getBlock() instanceof CampfireBlock && playerEntity.getBlockStateOn().getValue(BlockStateProperties.LIT)))) {
                    if (config.renderSoulFire && (isOnSoulFire || playerEntity.getBlockStateOn().getBlock() == Blocks.SOUL_CAMPFIRE)) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getSoulFireHeartTexture(hardcore, half, blinking), x, y, 9, 9);
                        ci.cancel();
                    } else {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getFireHeartTexture(hardcore, half, blinking), x, y, 9, 9);
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Unique
    private boolean scaleHelper(int scale) {
        int hudScale = config.vignetteScale;
        int guiScale = Minecraft.getInstance().options.guiScale().get();
        return hudScale == scale || hudScale == 0 && guiScale == scale;
    }

    @Inject(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    private void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        Identifier texture = player != null && ((SoulFireEntityAccessor) player).fireHud$isOnSoulFire() ? SOUL_FIRE_VIGNETTE : FIRE_VIGNETTE;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int var1 = scaleHelper(4) ? 1 : scaleHelper(3) ? 2 : scaleHelper(2) ? 3 : scaleHelper(1) ? 4 : 1;
        int var2 = scaleHelper(4) ? 2 : scaleHelper(3) ? 4 : scaleHelper(2) ? 6 : scaleHelper(1) ? 8 : 2;
        int var3 = scaleHelper(4) ? 1 : scaleHelper(3) ? 3 : scaleHelper(2) ? 5 : scaleHelper(1) ? 7 : 3;

        if (player != null) {
            if (!(!config.renderFireInLava && player.isInLava())) {
                if (!(!config.renderWithFireResistance && player.hasEffect(MobEffects.FIRE_RESISTANCE))) {
                    if (player.isOnFire() && client.options.getCameraType().isFirstPerson()) {
                        if (config.fireVignette == FireHudConfig.VignetteOptions.FULL) {
                            renderTopLeftCorner(texture, graphics, width, height, var1, var2);
                            renderTopRightCorner(texture, graphics, width, height, var1, var2, var3);
                            renderBottomLeftCorner(texture, graphics, width, height, var1, var2, var3);
                            renderBottomRightCorner(texture, graphics, width, height, var1, var2, var3);
                        }
                        if (config.fireVignette == FireHudConfig.VignetteOptions.UPPER) {
                            renderTopLeftCorner(texture, graphics, width, height, var1, var2);
                            renderTopRightCorner(texture, graphics, width, height, var1, var2, var3);
                        }
                        if (config.fireVignette == FireHudConfig.VignetteOptions.LOWER) {
                            renderBottomLeftCorner(texture, graphics, width, height, var1, var2, var3);
                            renderBottomRightCorner(texture, graphics, width, height, var1, var2, var3);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private void renderTopLeftCorner(Identifier texture, GuiGraphicsExtractor graphics, int width, int height, int var1, int var2) {
        renderOverlay(graphics, texture, config.vignetteOpacity, 0, 0, 0, 0, width / var2, height / var2, width / var1, height / var1);
    }
    @Unique
    private void renderTopRightCorner(Identifier texture, GuiGraphicsExtractor graphics, int width, int height, int var1, int var2, int var3) {
        renderOverlay(graphics, texture, config.vignetteOpacity, (width / var2) * var3, 0, width / var2, 0, width, height / var2, width / var1, height / var1);
    }
    @Unique
    private void renderBottomLeftCorner(Identifier texture, GuiGraphicsExtractor graphics, int width, int height, int var1, int var2, int var3) {
        renderOverlay(graphics, texture, config.vignetteOpacity, 0, (height / var2) * var3, 0, height / var2, width / var2, height, width / var1, height / var1);
    }
    @Unique
    private void renderBottomRightCorner(Identifier texture, GuiGraphicsExtractor graphics, int width, int height, int var1, int var2, int var3) {
        renderOverlay(graphics, texture, config.vignetteOpacity, (width / var2) * var3, (height / var2) * var3, width / var2, height / var2, width, height, width / var1, height / var1);
    }

    @Unique
    private void renderOverlay(GuiGraphicsExtractor graphics, Identifier texture, float opacity, int xPos, int yPos, int uStart, int vStart, int uEnd, int vEnd, int textureWidth, int textureHeight) {
        int i = ARGB.white(opacity);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, xPos, yPos, uStart, vStart, uEnd, vEnd, textureWidth, textureHeight, i);
    }

    @Unique
    public Identifier getFireHeartTexture(boolean hardcore, boolean half, boolean blinking) {
        if (!hardcore) {
            if (half) return blinking ? FIRE_HEART_HALF_BLINKING_TEXTURE : FIRE_HEART_HALF_TEXTURE;
            return blinking ? FIRE_HEART_FULL_BLINKING_TEXTURE : FIRE_HEART_FULL_TEXTURE;
        }
        if (half) return blinking ? FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE : FIRE_HEART_HARDCORE_HALF_TEXTURE;
        return blinking ? FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE : FIRE_HEART_HARDCORE_FULL_TEXTURE;
    }
    @Unique
    public Identifier getSoulFireHeartTexture(boolean hardcore, boolean half, boolean blinking) {
        if (!hardcore) {
            if (half) return blinking ? SOUL_FIRE_HEART_HALF_BLINKING_TEXTURE : SOUL_FIRE_HEART_HALF_TEXTURE;
            return blinking ? SOUL_FIRE_HEART_FULL_BLINKING_TEXTURE : SOUL_FIRE_HEART_FULL_TEXTURE;
        }
        if (half) return blinking ? SOUL_FIRE_HEART_HARDCORE_HALF_BLINKING_TEXTURE : SOUL_FIRE_HEART_HARDCORE_HALF_TEXTURE;
        return blinking ? SOUL_FIRE_HEART_HARDCORE_FULL_BLINKING_TEXTURE : SOUL_FIRE_HEART_HARDCORE_FULL_TEXTURE;
    }
}
