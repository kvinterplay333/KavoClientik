package com.example.kavoclient.ui;

import com.example.kavoclient.KavoClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.awt.Color;

public class KavoGuiScreen extends Screen {
    private int x, y, width = 480, height = 320;
    private boolean dragging = false;
    private int dragX, dragY;

    public KavoGuiScreen() {
        super(Text.literal("Kavo GUI"));
    }

    @Override
    protected void init() {
        super.init();
        if (this.client != null) {
            this.x = (this.client.getWindow().getScaledWidth() - width) / 2;
            this.y = (this.client.getWindow().getScaledHeight() - height) / 2;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width * 3, this.height * 3, 0x80000000, 0xC0000000);
        context.fill(x, y, x + width, y + height, new Color(18, 18, 24, 235).getRGB());
        context.fill(x, y, x + width, y + 35, new Color(140, 240, 80, 35).getRGB());
        context.drawText(this.textRenderer, "✦ KAVO CLIENT v1.0 [F8 Menu]", x + 12, y + 11, 0x8CF050, true);

        drawToggle(context, "KillAura (Авто-атака)", x + 20, y + 55, KavoClient.killauraEnabled);
        drawToggle(context, "AimBot (Доводка прицела)", x + 20, y + 95, KavoClient.aimbotEnabled);
        drawToggle(context, "TargetBot (Преследование)", x + 250, y + 55, KavoClient.targetbotEnabled);
        drawToggle(context, "ESP (Подсветка игроков)", x + 250, y + 95, KavoClient.espEnabled);
        drawToggle(context, "Chams (Цветные модели)", x + 20, y + 135, KavoClient.chamsEnabled);
        drawToggle(context, "X-Ray (Просвет руд)", x + 250, y + 135, KavoClient.xrayEnabled);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext context, String label, int bx, int by, boolean state) {
        context.fill(bx, by, bx + 210, by + 34, new Color(30, 30, 48, 200).getRGB());
        context.drawText(this.textRenderer, label, bx + 10, by + 11, 0xD7D7EB, false);
        int dotColor = state ? 0x8CF050 : 0x6E6E96;
        context.fill(bx + 183, by + 12, bx + 195, by + 24, dotColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 35) {
                dragging = true;
                dragX = (int) mouseX - x;
                dragY = (int) mouseY - y;
            }

            if (isInside(mouseX, mouseY, x + 20, y + 55, 210, 34)) KavoClient.killauraEnabled = !KavoClient.killauraEnabled;
            if (isInside(mouseX, mouseY, x + 20, y + 95, 210, 34)) KavoClient.aimbotEnabled = !KavoClient.aimbotEnabled;
            if (isInside(mouseX, mouseY, x + 250, y + 55, 210, 34)) KavoClient.targetbotEnabled = !KavoClient.targetbotEnabled;
            if (isInside(mouseX, mouseY, x + 250, y + 95, 210, 34)) KavoClient.espEnabled = !KavoClient.espEnabled;
            if (isInside(mouseX, mouseY, x + 20, y + 135, 210, 34)) KavoClient.chamsEnabled = !KavoClient.chamsEnabled;
            if (isInside(mouseX, mouseY, x + 250, y + 135, 210, 34)) {
                KavoClient.xrayEnabled = !KavoClient.xrayEnabled;
                if (this.client != null && this.client.worldRenderer != null) {
                    this.client.worldRenderer.reload();
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            this.x = (int) mouseX - dragX;
            this.y = (int) mouseY - dragY;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInside(double mx, double my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
