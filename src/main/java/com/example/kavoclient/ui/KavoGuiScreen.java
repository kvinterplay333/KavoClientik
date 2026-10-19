package com.example.kavoclient.ui;

import com.example.kavoclient.KavoClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class KavoGuiScreen extends Screen {

    public KavoGuiScreen() {
        super(Text.literal("KavoClient Menu"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Отключение мыльного блура 1.21: рисуем четкое легкое затемнение
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int panelW = 280;
        int panelH = 220;
        int x = (this.width - panelW) / 2;
        int y = (this.height - panelH) / 2;

        // Корпус панели (сплошной глубокий темно-серый, без прозрачного мыла)
        context.fill(x, y, x + panelW, y + panelH, 0xFF14141E);
        // Неоновая рамка
        context.fill(x, y, x + panelW, y + 2, 0xFF3B82F6);
        context.fill(x, y + panelH - 2, x + panelW, y + panelH, 0xFF3B82F6);
        context.fill(x, y, x + 2, y + panelH, 0xFF3B82F6);
        context.fill(x + panelW - 2, y, x + panelW, y + panelH, 0xFF3B82F6);

        // Заголовок
        context.drawText(this.textRenderer, "✦ KAVO CLIENT | МОДУЛИ И НАСТРОЙКИ", x + 12, y + 10, 0xFFE2E8F0, true);

        // 1. KillAura
        drawButton(context, x + 15, y + 30, 250, 18, "KillAura: " + (KavoClient.killauraEnabled ? "[ВКЛ]" : "[ВЫКЛ]"), KavoClient.killauraEnabled);

        // 2. AimBot
        drawButton(context, x + 15, y + 52, 250, 18, "AimBot: " + (KavoClient.aimbotEnabled ? "[ВКЛ]" : "[ВЫКЛ]"), KavoClient.aimbotEnabled);

        // 3. Выбор целей (Entity Target)
        String targetText = switch (KavoClient.targetMode) {
            case 0 -> "Цели: [ИГРОКИ]";
            case 1 -> "Цели: [ВРАЖДЕБНЫЕ МОБЫ]";
            default -> "Цели: [ВСЕ СУЩЕСТВА]";
        };
        drawOption(context, x + 15, y + 74, 250, 18, targetText);

        // 4. Плавность Aim (Smooth)
        String smoothText = "Плавность Aim: [" + (int)KavoClient.aimSmoothness + "x] (Клик: изменить)";
        drawOption(context, x + 15, y + 96, 250, 18, smoothText);

        // 5. Угол захвата FOV
        String fovText = "Радиус FOV: [" + (int)KavoClient.aimFov + "°] (Клик: изменить)";
        drawOption(context, x + 15, y + 118, 250, 18, fovText);

        // 6. Дистанция удара (Range)
        String rangeText = String.format("Дистанция удара: [%.1f м] (Клик: изменить)", KavoClient.attackRange);
        drawOption(context, x + 15, y + 140, 250, 18, rangeText);

        // 7. Круг FOV на экране
        drawButton(context, x + 15, y + 162, 250, 18, "Отображать круг FOV: " + (KavoClient.fovCircleEnabled ? "[ДА]" : "[НЕТ]"), KavoClient.fovCircleEnabled);

        // Подсказка снизу
        context.drawText(this.textRenderer, "Закрыть меню: клавиша F8", x + 15, y + 195, 0xFF94A3B8, true);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawButton(DrawContext context, int bx, int by, int bw, int bh, String text, boolean active) {
        int color = active ? 0xFF16A34A : 0xFFDC2626;
        context.fill(bx, by, bx + bw, by + bh, 0xFF1E293B);
        context.fill(bx, by, bx + 3, by + bh, color);
        context.drawText(this.textRenderer, text, bx + 10, by + 5, active ? 0xFF4ADE80 : 0xFFF87171, true);
    }

    private void drawOption(DrawContext context, int bx, int by, int bw, int bh, String text) {
        context.fill(bx, by, bx + bw, by + bh, 0xFF1E293B);
        context.fill(bx, by, bx + 3, by + bh, 0xFF3B82F6);
        context.drawText(this.textRenderer, text, bx + 10, by + 5, 0xFF93C5FD, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // ЛКМ
            int panelW = 280;
            int panelH = 220;
            int x = (this.width - panelW) / 2;
            int y = (this.height - panelH) / 2;

            // Клик KillAura
            if (isInside(mouseX, mouseY, x + 15, y + 30, 250, 18)) {
                KavoClient.killauraEnabled = !KavoClient.killauraEnabled;
                return true;
            }
            // Клик AimBot
            if (isInside(mouseX, mouseY, x + 15, y + 52, 250, 18)) {
                KavoClient.aimbotEnabled = !KavoClient.aimbotEnabled;
                return true;
            }
            // Переключение целей
            if (isInside(mouseX, mouseY, x + 15, y + 74, 250, 18)) {
                KavoClient.targetMode = (KavoClient.targetMode + 1) % 3;
                return true;
            }
            // Изменение плавности (цикл от 1 до 5)
            if (isInside(mouseX, mouseY, x + 15, y + 96, 250, 18)) {
                KavoClient.aimSmoothness += 1.0f;
                if (KavoClient.aimSmoothness > 5.0f) KavoClient.aimSmoothness = 1.0f;
                return true;
            }
            // Изменение угла FOV (цикл 45 -> 70 -> 90 -> 120 -> 160)
            if (isInside(mouseX, mouseY, x + 15, y + 118, 250, 18)) {
                if (KavoClient.aimFov == 45.0f) KavoClient.aimFov = 70.0f;
                else if (KavoClient.aimFov == 70.0f) KavoClient.aimFov = 90.0f;
                else if (KavoClient.aimFov == 90.0f) KavoClient.aimFov = 120.0f;
                else if (KavoClient.aimFov == 120.0f) KavoClient.aimFov = 160.0f;
                else KavoClient.aimFov = 45.0f;
                return true;
            }
            // Изменение дистанции (3.5 -> 4.0 -> 4.5 -> 5.0)
            if (isInside(mouseX, mouseY, x + 15, y + 140, 250, 18)) {
                KavoClient.attackRange += 0.5f;
                if (KavoClient.attackRange > 5.0f) KavoClient.attackRange = 3.5f;
                return true;
            }
            // Круг FOV
            if (isInside(mouseX, mouseY, x + 15, y + 162, 250, 18)) {
                KavoClient.fovCircleEnabled = !KavoClient.fovCircleEnabled;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInside(double mx, double my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }
}
