package net.enderkitty;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FireHud implements ClientModInitializer {
	public static final String MOD_ID = "firehud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static FireHudConfig config;
    private static final Identifier FIRE_TINT = Identifier.fromNamespaceAndPath(MOD_ID, "fire_tint");
    private static final Identifier FIRE_METER = Identifier.fromNamespaceAndPath(MOD_ID, "fire_meter");
    private static final Identifier THERMOMETER = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/hud/thermometer.png");
    private static final Identifier THERMOMETER_TEMP = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/hud/thermometer_temp.png");
    private static final Identifier THERMOMETER_TEMP_SOUL = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/hud/thermometer_temp_soul.png");

	@Override
	public void onInitializeClient() {
		if (isClothConfigLoaded()) {
			ConfigHolder<FireHudConfig> configHolder = AutoConfig.register(FireHudConfig.class, GsonConfigSerializer::new);
			FireHud.config = configHolder.getConfig();
		}

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, FIRE_TINT, this::fireTint);
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, FIRE_METER, this::thermometer);

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
			if (player != null && client.level != null) {
				MobEffectInstance fireRes = player.getEffect(MobEffects.FIRE_RESISTANCE);

                // Fire res timer
				if (config.displayFireResTimer && player.hasEffect(MobEffects.FIRE_RESISTANCE) && fireRes != null && !fireRes.isInfiniteDuration()) {
					String styling = fireRes.getDuration() > 120 && fireRes.getDuration() <= 220 ? "§6" : fireRes.getDuration() <= 120 ? "§4" : "§f";

					Component durationSeconds = MobEffectUtil.formatDuration(fireRes, 1.0f, client.level.tickRateManager().tickrate());
					Component durationTicks = Component.literal(String.valueOf(player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration()));
					Component fireResText = Component.translatable("text.firehud.hud.fireResTimer");
					Component text = Component.literal(fireResText.getString() + styling + (config.fireResTimerAsTicks ? durationTicks : durationSeconds).getString());

					if (config.renderWithTimeLeft == 0) player.sendOverlayMessage(text);
					else if (config.renderWithTimeLeft > 0 && fireRes.getDuration() <= config.renderWithTimeLeft * 20) {
						player.sendOverlayMessage(text);
					}
				}

                // Thermometer
                if (config.thermometer) {
                    int clientFireTick = ((ClientFireTick) player).fireHud$clientFireTick();

                    if (clientFireTick > 0) {
                        if (player.isCreative() || !player.isOnFire()) { // Why the fuck doesn't this work?!!!!
                            ((ClientFireTick) player).fireHud$setClientFireTick(0);
                        }
                        if (client.getSingleplayerServer() == null || !client.getSingleplayerServer().isPaused()) {
                            if (player.fireImmune()) {
                                ((ClientFireTick) player).fireHud$setClientFireTick(clientFireTick - 4);
                            } else {
                                ((ClientFireTick) player).fireHud$setClientFireTick(clientFireTick - 1);
                            }
                        }
                    }
                }
            }

		});
	}

    private void thermometer(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();

        // For some reason it's giving me shit so copy/paste code here 'cause I don't wanna work on this anymore
        if (client.player != null) {
            int clientFireTick = ((ClientFireTick) client.player).fireHud$clientFireTick();
            if (clientFireTick > 0) {
                if (client.player.isCreative() || !client.player.isOnFire()) {
                    ((ClientFireTick) client.player).fireHud$setClientFireTick(0);
                }
            }
        }

        if (config.thermometer) {
            if (!config.onlyShowWhenOnFire) {
                renderTherm(graphics, client);
            } else if (client.player != null && client.player.isOnFire()) {
                renderTherm(graphics, client);
            }
        }
    }
    private void renderTherm(GuiGraphicsExtractor graphics, Minecraft client) {
        if (config.thermometer && client.player instanceof ClientFireTick player) {
            if (config.showFireTicks) {
                graphics.text(client.font, Component.literal(String.valueOf(player.fireHud$clientFireTick())),
                        thermNumPos(graphics, player), graphics.guiHeight() / 2 - 22 + 44, CommonColors.WHITE, true);
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, THERMOMETER,
                    config.onLeftSide ? 6 : graphics.guiWidth() - 16, graphics.guiHeight() / 2 - 22,
                    0.0f, 0.0f, 10, 44, 10, 44);
            if (client.player.isOnFire()) {
                int i = Mth.ceil(getThermProgress() * 43) + 1;
                graphics.blit(RenderPipelines.GUI_TEXTURED, thermSprite(),
                        config.onLeftSide ? 6 : graphics.guiWidth() - 16, graphics.guiHeight() / 2 - 22 + 44 - i,
                        0.0f, 44 - i, 10, i, 10, 44);
            }
        }
    }
    private Identifier thermSprite() {
        SoulFireEntityAccessor player = (SoulFireEntityAccessor) Minecraft.getInstance().player;
        return player != null && player.fireHud$isOnSoulFire() ? THERMOMETER_TEMP_SOUL : THERMOMETER_TEMP;
    }
    private int thermNumPos(GuiGraphicsExtractor graphics, ClientFireTick player) {
        int length = String.valueOf(player.fireHud$clientFireTick()).length();
        int value = 14;
        for (int i = 1; i < length; i++) {
            value += 6 * i - 6;
        }
        // The for loop was retarded
        return config.onLeftSide ? 9 : graphics.guiWidth() - switch (length) {
            case 1 -> 14;
            case 2 -> 20;
            case 3 -> 26;
            case 4 -> 32;
            case 5 -> 38;
            default -> value;
        };
    }
    public float getThermProgress() {
        LocalPlayer playerEntity = Minecraft.getInstance().player;
        if (playerEntity instanceof ClientFireTick player) {
            int max = 300;
            return Mth.clamp((float) player.fireHud$clientFireTick() / max, 0.0f, 1.0f);
        } else return 0.0f;
    }

	private void fireTint(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();

		if (player != null && player.isOnFire() && client.options.getCameraType().isFirstPerson() &&
				!(!config.renderFireInLava && player.isInLava()) && !(!config.renderWithFireResistance && player.hasEffect(MobEffects.FIRE_RESISTANCE))) {

			if (config.fireScreenTint && !((SoulFireEntityAccessor) player).fireHud$isOnSoulFire()) {
				graphics.fillGradient(0, 0, width, height, config.fireStartColor, config.fireEndColor);
			}
			if (config.fireScreenTint && config.renderSoulFire && ((SoulFireEntityAccessor) player).fireHud$isOnSoulFire()) {
				graphics.fillGradient(0, 0, width, height, config.soulFireStartColor, config.soulFireEndColor);
			}
		}
	}


	public static FireHudConfig getConfig() {
		return FireHud.config;
	}

	public static boolean isClothConfigLoaded() {
		return FabricLoader.getInstance().isModLoaded("cloth-config2");
	}
}
