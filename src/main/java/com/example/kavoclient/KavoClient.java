package com.example.kavoclient;

import com.example.kavoclient.ui.KavoGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class KavoClient implements ClientModInitializer {
    public static final String MOD_NAME = "✦ KAVO CLIENT";

    // Состояния модулей
    public static boolean killauraEnabled = false;
    public static boolean aimbotEnabled = false;
    public static boolean fovCircleEnabled = true;

    // Режим выбора целей: 0 - Только игроки, 1 - Враждебные мобы, 2 - Все живые
    public static int targetMode = 1; 

    // Настройки боя
    public static float attackRange = 3.8f;      // Дистанция атаки
    public static float aimFov = 70.0f;          // Угол захвата FOV (в градусах)
    public static float aimSmoothness = 3.0f;    // Плавность: 1.0 (резко) - 10.0 (плавно)

    private boolean f8PressedLastTick = false;

    @Override
    public void onInitializeClient() {
        System.out.println(MOD_NAME + " инициализирован!");

        // Открытие GUI на F8
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            long window = client.getWindow().getHandle();
            boolean isF8Down = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F8);

            if (isF8Down && !f8PressedLastTick) {
                if (client.currentScreen == null) {
                    client.setScreen(new KavoGuiScreen());
                } else if (client.currentScreen instanceof KavoGuiScreen) {
                    client.setScreen(null);
                }
            }
            f8PressedLastTick = isF8Down;

            if (killauraEnabled) runKillAura(client);
            if (aimbotEnabled) runAimBot(client);
        });

        // Отрисовка круга FOV прямо в игре
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || !aimbotEnabled || !fovCircleEnabled) return;

            int centerX = context.getScaledWindowWidth() / 2;
            int centerY = context.getScaledWindowHeight() / 2;
            int radius = (int) (aimFov * 1.5f);

            // Точечная отрисовка круга FOV
            for (int angle = 0; angle < 360; angle += 12) {
                double rad = Math.toRadians(angle);
                int x = (int) (centerX + radius * Math.cos(rad));
                int y = (int) (centerY + radius * Math.sin(rad));
                context.fill(x, y, x + 2, y + 2, 0xAA00FFFF);
            }
        });
    }

    public static boolean isValidTarget(Entity entity, MinecraftClient client) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || entity == client.player) {
            return false;
        }
        return switch (targetMode) {
            case 0 -> entity instanceof PlayerEntity;
            case 1 -> entity instanceof HostileEntity;
            case 2 -> true;
            default -> false;
        };
    }

    private void runKillAura(MinecraftClient client) {
        // Проверка кулдауна удара (удар наносится только при 100% готовности оружия)
        if (client.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        for (Entity entity : client.world.getEntities()) {
            if (isValidTarget(entity, client)) {
                if (client.player.distanceTo(entity) <= attackRange) {
                    client.interactionManager.attackEntity(client.player, entity);
                    client.player.swingHand(Hand.MAIN_HAND);
                    break;
                }
            }
        }
    }

    private void runAimBot(MinecraftClient client) {
        Entity bestTarget = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : client.world.getEntities()) {
            if (!isValidTarget(entity, client)) continue;

            double dist = client.player.distanceTo(entity);
            if (dist > 8.0) continue;

            // Расчет углов
            double dx = entity.getX() - client.player.getX();
            double dy = (entity.getEyeY()) - client.player.getEyeY();
            double dz = entity.getZ() - client.player.getZ();
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float targetPitch = (float) Math.toDegrees(-Math.atan2(dy, distXZ));

            float diffYaw = MathHelper.wrapDegrees(targetYaw - client.player.getYaw());
            float diffPitch = MathHelper.wrapDegrees(targetPitch - client.player.getPitch());

            // Проверка попадания в круг FOV
            if (Math.abs(diffYaw) <= aimFov / 2.0f && Math.abs(diffPitch) <= aimFov / 2.0f) {
                if (dist < minDistance) {
                    minDistance = dist;
                    bestTarget = entity;
                }
            }
        }

        if (bestTarget != null) {
            double dx = bestTarget.getX() - client.player.getX();
            double dy = bestTarget.getEyeY() - client.player.getEyeY();
            double dz = bestTarget.getZ() - client.player.getZ();
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float targetPitch = (float) Math.toDegrees(-Math.atan2(dy, distXZ));

            float diffYaw = MathHelper.wrapDegrees(targetYaw - client.player.getYaw());
            float diffPitch = MathHelper.wrapDegrees(targetPitch - client.player.getPitch());

            // Плавное наведение
            float smooth = Math.max(1.0f, aimSmoothness);
            client.player.setYaw(client.player.getYaw() + diffYaw / smooth);
            client.player.setPitch(client.player.getPitch() + diffPitch / smooth);
        }
    }
}
