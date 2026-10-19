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
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class KavoClient implements ClientModInitializer {
    public static final String MOD_NAME = "✦ KAVO CLIENT";

    // Состояния переключателей
    public static boolean killauraEnabled = false;
    public static boolean aimbotEnabled = false;
    public static boolean fovCircleEnabled = true;
    public static boolean rangeIndicatorEnabled = true;
    public static boolean chinaHatEnabled = true;
    public static boolean trajectoriesEnabled = true;

    // Параметры боя и визуала
    public static int targetMode = 1;         // 0 - Игроки, 1 - Враждебные мобы, 2 - Все
    public static float attackRange = 3.8f;
    public static float aimFov = 75.0f;

    public static Entity currentTarget = null;
    private boolean f8PressedLastTick = false;

    @Override
    public void onInitializeClient() {
        System.out.println(MOD_NAME + " успешно запущен!");

        // Обработка логики на каждый тик клиента
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
            if (aimbotEnabled) runAimbot(client);
            else currentTarget = null;
        });

        // Отрисовка визуала на экране
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            int centerX = context.getScaledWindowWidth() / 2;
            int centerY = context.getScaledWindowHeight() / 2;
            int screenH = context.getScaledWindowHeight();

            // Лаймовый круг FOV
            if (aimbotEnabled && fovCircleEnabled) {
                int radius = (int) (aimFov * 1.5f);
                for (int a = 0; a < 360; a += 12) {
                    double rad = Math.toRadians(a);
                    int px = (int) (centerX + radius * Math.cos(rad));
                    int py = (int) (centerY + radius * Math.sin(rad));
                    context.fill(px, py, px + 2, py + 2, 0xFFA3E635);
                }
            }

            // Индикатор дистанции внизу экрана
            if (rangeIndicatorEnabled) {
                int barW = 140;
                int barX = centerX - barW / 2;
                int barY = screenH - 52;
                context.fill(barX - 2, barY - 2, barX + barW + 2, barY + 14, 0x9012161A);
                int fillW = (int) (barW * MathHelper.clamp(attackRange / 6.0f, 0.0f, 1.0f));
                context.fill(barX, barY, barX + fillW, barY + 12, 0xFFA3E635);
                context.drawText(client.textRenderer, String.format("Дистанция: %.1f м", attackRange), barX + 6, barY + 2, 0xFF000000, false);
            }

            // Индикатор траектории броска
            if (trajectoriesEnabled) {
                ItemStack stack = client.player.getMainHandStack();
                if (stack.getItem() instanceof EnderPearlItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
                    context.fill(centerX - 2, centerY + 22, centerX + 2, centerY + 26, 0xFFA3E635);
                    context.drawText(client.textRenderer, "✦ Траектория активна", centerX - 50, centerY + 32, 0xFFA3E635, true);
                }
            }

            // Маркер направления на цель
            if (chinaHatEnabled && currentTarget != null) {
                double dx = currentTarget.getX() - client.player.getX();
                double dz = currentTarget.getZ() - client.player.getZ();
                float angle = (float) Math.toDegrees(Math.atan2(dz, dx)) - client.player.getYaw() - 90;

                int radDist = 48;
                double rad = Math.toRadians(angle);
                int markerX = (int) (centerX + radDist * Math.sin(rad));
                int markerY = (int) (centerY - radDist * Math.cos(rad));

                context.fill(markerX - 3, markerY - 3, markerX + 3, markerY + 3, 0xFFA3E635);
                context.drawText(client.textRenderer, "TARGET", markerX - 14, markerY - 12, 0xFFA3E635, true);
            }
        });
    }

    public static boolean isValidTarget(Entity entity, MinecraftClient client) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || entity == client.player) return false;
        return switch (targetMode) {
            case 0 -> entity instanceof PlayerEntity;
            case 1 -> entity instanceof HostileEntity;
            case 2 -> true;
            default -> false;
        };
    }

    private void runKillAura(MinecraftClient client) {
        if (client.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        for (Entity entity : client.world.getEntities()) {
            if (isValidTarget(entity, client) && client.player.distanceTo(entity) <= attackRange) {
                client.interactionManager.attackEntity(client.player, entity);
                client.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }

    private void runAimbot(MinecraftClient client) {
        Entity target = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : client.world.getEntities()) {
            if (!isValidTarget(entity, client)) continue;
            double dist = client.player.distanceTo(entity);
            if (dist > 8.0) continue;

            double dx = entity.getX() - client.player.getX();
            double dy = entity.getEyeY() - client.player.getEyeY();
            double dz = entity.getZ() - client.player.getZ();
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float targetPitch = (float) Math.toDegrees(-Math.atan2(dy, distXZ));

            float diffYaw = MathHelper.wrapDegrees(targetYaw - client.player.getYaw());
            float diffPitch = MathHelper.wrapDegrees(targetPitch - client.player.getPitch());

            if (Math.abs(diffYaw) <= aimFov / 2.0f && Math.abs(diffPitch) <= aimFov / 2.0f) {
                if (dist < minDistance) {
                    minDistance = dist;
                    target = entity;
                }
            }
        }

        currentTarget = target;

        if (target != null) {
            double dx = target.getX() - client.player.getX();
            double dy = target.getEyeY() - client.player.getEyeY();
            double dz = target.getZ() - client.player.getZ();
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float targetPitch = (float) Math.toDegrees(-Math.atan2(dy, distXZ));

            float diffYaw = MathHelper.wrapDegrees(targetYaw - client.player.getYaw());
            float diffPitch = MathHelper.wrapDegrees(targetPitch - client.player.getPitch());

            // Плавное смещение углов
            client.player.setYaw(client.player.getYaw() + diffYaw * 0.2f);
            client.player.setPitch(client.player.getPitch() + diffPitch * 0.2f);
        }
    }
}
