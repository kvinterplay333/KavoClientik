package com.example.kavoclient;

import com.example.kavoclient.modules.Module;
import com.example.kavoclient.modules.ModuleManager;
import com.example.kavoclient.ui.KavoGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KavoClient implements ClientModInitializer {
    public static final String MOD_NAME = "✦ KAVO CLIENT";
    private boolean f8PressedLastTick = false;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        // Главный цикл тиков
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

            // Вызов тика для активных модулей
            for (Module m : ModuleManager.getModules()) {
                if (m.isEnabled()) m.onTick();
            }
        });

        // HUD Оверлей (Инфо-панель и список активных модулей)
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            // Водяной знак
            context.drawText(client.textRenderer, "✦ KAVO | 1.21", 6, 6, 0xFFA3E635, true);

            // FPS и Координаты
            int fps = client.getCurrentFps();
            String coords = String.format("XYZ: %d %d %d", (int)client.player.getX(), (int)client.player.getY(), (int)client.player.getZ());
            context.drawText(client.textRenderer, "FPS: " + fps + " | " + coords, 6, 18, 0xFF9CA3AF, true);

            // ArrayList (список активных модулей в правом верхнем углу)
            int screenWidth = context.getScaledWindowWidth();
            int offset = 6;
            for (Module m : ModuleManager.getModules()) {
                if (m.isEnabled()) {
                    int textWidth = client.textRenderer.getWidth(m.getName());
                    int x = screenWidth - textWidth - 8;
                    context.fill(x - 3, offset - 1, screenWidth - 2, offset + 10, 0x8012161A);
                    context.fill(screenWidth - 2, offset - 1, screenWidth, offset + 10, 0xFFA3E635);
                    context.drawText(client.textRenderer, m.getName(), x, offset, 0xFFFFFFFF, true);
                    offset += 12;
                }
            }
        });
    }
}
