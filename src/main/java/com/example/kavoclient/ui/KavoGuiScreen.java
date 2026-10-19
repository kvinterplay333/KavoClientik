package com.example.kavoclient.ui;

import com.example.kavoclient.KavoClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class KavoGuiScreen extends Screen {

    public enum Tab {
        COMBAT("Бой"),
        VISUALS("Визуалы"),
        MOVEMENT("Движение"),
        SETTINGS("Настройки");

        private final String name;
        Tab(String name) { this.name = name; }
        public String getName() { return name; }
    }

    private static Tab selectedTab = Tab.VISUALS;
    private final Animation tabIndicatorAnim = new Animation(45.0f, 14.0f);

    public KavoGuiScreen() {
        super(Text.literal("KavoClient Menu"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        tabIndicatorAnim.update(delta);

        int mainW = 420;
        int mainH = 260;
        int mainX = (this.width - mainW) / 2;
        int mainY = (this.height - mainH) / 2;
        int sidebarW = 110;

        // Матовый графитовый фон и акцентная лаймовая полоса
        context.fill(mainX, mainY, mainX + mainW, mainY + mainH, 0xD012161A);
        context.fill(mainX, mainY, mainX + mainW, mainY + 2, 0xFFA3E635);

        // Боковая панель
        context.fill(mainX, mainY + 2, mainX + sidebarW, mainY + mainH, 0x50000000);
        context.drawText(this.textRenderer, "✦ KAVO", mainX + 16, mainY + 14, 0xFFA3E635, false);

        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            int itemY = mainY + 45 + i * 28;

            if (tab == selectedTab) {
                tabIndicatorAnim.setTarget((float) itemY);
            }

            int textColor = (tab == selectedTab) ? 0xFFFFFFFF : 0xFF71717A;
            context.drawText(this.textRenderer, tab.getName(), mainX + 24, itemY + 4, textColor, false);
        }

        // Лаймовый индикатор выбранной вкладки
        int indicatorY = (int) tabIndicatorAnim.getValue();
        context.fill(mainX + 4, indicatorY, mainX + 7, indicatorY + 16, 0xFFA3E635);

        // Разделитель панелей
        context.fill(mainX + sidebarW, mainY + 2, mainX + sidebarW + 1, mainY + mainH, 0x20FFFFFF);

        // Правая часть (содержимое)
        int contentX = mainX + sidebarW + 16;
        int contentY = mainY + 16;
        int cardW = mainW - sidebarW - 32;

        context.drawText(this.textRenderer, selectedTab.getName().toUpperCase(), contentX, contentY, 0xFFA1A1AA, false);

        switch (selectedTab) {
            case COMBAT -> {
                drawToggle(context, contentX, contentY + 20, cardW, 26, "KillAura", KavoClient.killauraEnabled);
                drawToggle(context, contentX, contentY + 52, cardW, 26, "AimBot", KavoClient.aimbotEnabled);
                String modeLabel = switch (KavoClient.targetMode) {
                    case 0 -> "Игроки";
                    case 1 -> "Враждебные мобы";
                    default -> "Все сущности";
                };
                drawOption(context, contentX, contentY + 84, cardW, 26, "Фильтр целей: [" + modeLabel + "]");
                drawOption(context, contentX, contentY + 116, cardW, 26, String.format("Дистанция атаки: [%.1f м]", KavoClient.attackRange));
            }
            case VISUALS -> {
                drawToggle(context, contentX, contentY + 20, cardW, 26, "Круг захвата FOV", KavoClient.fovCircleEnabled);
                drawToggle(context, contentX, contentY + 52, cardW, 26, "Индикатор дистанции", KavoClient.rangeIndicatorEnabled);
                drawToggle(context, contentX, contentY + 84, cardW, 26, "ChinaHat / Радар цели", KavoClient.chinaHatEnabled);
                drawToggle(context, contentX, contentY + 116, cardW, 26, "Траектории бросков", KavoClient.trajectoriesEnabled);
            }
            case MOVEMENT -> {
                drawOption(context, contentX, contentY + 20, cardW, 26, "AutoSprint: [ВСЕГДА ВКЛ]");
                drawOption(context, contentX, contentY + 52, cardW, 26, "SafeWalk: [ВЫКЛ]");
            }
            case SETTINGS -> {
                drawOption(context, contentX, contentY + 20, cardW, 26, "Тема GUI: [Лаймово-матовая]");
                drawOption(context, contentX, contentY + 52, cardW, 26, "Закрыть меню: [Клавиша F8]");
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext context, int x, int y, int w, int h, String label, boolean active) {
        context.fill(x, y, x + w, y + h, 0x30FFFFFF);
        context.drawText(this.textRenderer, label, x + 10, y + 8, 0xFFE4E4E7, false);
        int switchColor = active ? 0xFFA3E635 : 0xFF3F3F46;
        context.fill(x + w - 24, y + 7, x + w - 10, y + 19, switchColor);
    }

    private void drawOption(DrawContext context, int x, int y, int w, int h, String label) {
        context.fill(x, y, x + w, y + h, 0x30FFFFFF);
        context.drawText(this.textRenderer, label, x + 10, y + 8, 0xFFA3E635, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int mainW = 420;
            int mainH = 260;
            int mainX = (this.width - mainW) / 2;
            int mainY = (this.height - mainH) / 2;
            int sidebarW = 110;

            // Клик по боковым вкладкам
            Tab[] tabs = Tab.values();
            for (int i = 0; i < tabs.length; i++) {
                int itemY = mainY + 45 + i * 28;
                if (mouseX >= mainX && mouseX <= mainX + sidebarW && mouseY >= itemY && mouseY <= itemY + 24) {
                    selectedTab = tabs[i];
                    return true;
                }
            }

            int contentX = mainX + sidebarW + 16;
            int contentY = mainY + 16;
            int cardW = mainW - sidebarW - 32;

            if (selectedTab == Tab.COMBAT) {
                if (in(mouseX, mouseY, contentX, contentY + 20, cardW, 26)) KavoClient.killauraEnabled = !KavoClient.killauraEnabled;
                if (in(mouseX, mouseY, contentX, contentY + 52, cardW, 26)) KavoClient.aimbotEnabled = !KavoClient.aimbotEnabled;
                if (in(mouseX, mouseY, contentX, contentY + 84, cardW, 26)) KavoClient.targetMode = (KavoClient.targetMode + 1) % 3;
                if (in(mouseX, mouseY, contentX, contentY + 116, cardW, 26)) {
                    KavoClient.attackRange = (KavoClient.attackRange >= 5.0f) ? 3.5f : KavoClient.attackRange + 0.5f;
                }
            } else if (selectedTab == Tab.VISUALS) {
                if (in(mouseX, mouseY, contentX, contentY + 20, cardW, 26)) KavoClient.fovCircleEnabled = !KavoClient.fovCircleEnabled;
                if (in(mouseX, mouseY, contentX, contentY + 52, cardW, 26)) KavoClient.rangeIndicatorEnabled = !KavoClient.rangeIndicatorEnabled;
                if (in(mouseX, mouseY, contentX, contentY + 84, cardW, 26)) KavoClient.chinaHatEnabled = !KavoClient.chinaHatEnabled;
                if (in(mouseX, mouseY, contentX, contentY + 116, cardW, 26)) KavoClient.trajectoriesEnabled = !KavoClient.trajectoriesEnabled;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
