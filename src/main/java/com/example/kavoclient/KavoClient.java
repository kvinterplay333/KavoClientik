package com.example.kavoclient;

import com.example.kavoclient.ui.KavoGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KavoClient implements ClientModInitializer {
    public static final String MOD_NAME = "✦ KAVO CLIENT";
    
    public static boolean killauraEnabled = false;
    public static boolean aimbotEnabled = false;
    public static boolean targetbotEnabled = false;
    public static boolean chamsEnabled = true;
    public static boolean espEnabled = true;
    public static boolean xrayEnabled = false;

    private boolean f8PressedLastTick = false;

    @Override
    public void onInitializeClient() {
        System.out.println(MOD_NAME + " успешно инициализирован!");

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
    }

    private void runKillAura(net.minecraft.client.MinecraftClient client) {
        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
            if (entity instanceof net.minecraft.entity.player.PlayerEntity && entity != client.player) {
                if (client.player.distanceTo(entity) <= 4.0f) {
                    client.interactionManager.attackEntity(client.player, entity);
                    client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                    break;
                }
            }
        }
    }

    private void runAimBot(net.minecraft.client.MinecraftClient client) {
        net.minecraft.entity.Entity target = null;
        double minDistance = Double.MAX_VALUE;
        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
            if (entity instanceof net.minecraft.entity.player.PlayerEntity && entity != client.player) {
                double dist = client.player.distanceTo(entity);
                if (dist < minDistance) {
                    minDistance = dist;
                    target = entity;
                }
            }
        }
        if (target != null) {
            client.player.lookAt(net.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, target.getPos());
        }
    }
}
