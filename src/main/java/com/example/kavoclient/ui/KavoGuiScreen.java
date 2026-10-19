package com.example.kavoclient.ui;

import com.example.kavoclient.modules.Module;
import com.example.kavoclient.modules.ModuleManager;
import com.example.kavoclient.settings.Setting;
import com.example.kavoclient.settings.Setting.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.List;

public class KavoGuiScreen extends Screen {

    private static Module.Category currentCategory = Module.Category.COMBAT;
    private final Animation sidebarSelector = new Animation(45.0f, 16.0f);

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
        sidebarSelector.update(delta);

        int mainW = 460;
        int mainH = 280;
        int mainX = (this.width - mainW) / 2;
        int mainY = (this.height - mainH) / 2;
        int sidebarW = 120;

        // Матовый фон и лаймовая полоса
        context.fill(mainX, mainY, mainX + mainW, mainY + mainH, 0xD9111418);
        context.fill(mainX, mainY, mainX + mainW, mainY + 2, 0xFFA3E635);

        // Боковая панель
        context.fill(mainX, mainY + 2, mainX + sidebarW, mainY + mainH, 0x40000000);
        context.drawText(this.textRenderer, "✦ KAVO CLIENT", mainX + 14, mainY + 14, 0xFFA3E635, false);

        Module.Category[] categories = Module.Category.values();
        for (int i = 0; i < categories.length; i++) {
            Module.Category cat = categories[i];
            int itemY = mainY + 45 + i * 28;

            if (cat == currentCategory) {
                sidebarSelector.setTarget((float) itemY);
            }

            int textColor = (cat == currentCategory) ? 0xFFFFFFFF : 0xFF71717A;
            context.drawText(this.textRenderer, cat.getLabel(), mainX + 22, itemY + 4, textColor, false);
        }

        // Лаймовый маркер выбранной вкладки
        int markerY = (int) sidebarSelector.getValue();
        context.fill(mainX + 4, markerY, mainX + 7, markerY + 16, 0xFFA3E635);

        // Разделитель
        context.fill(mainX + sidebarW, mainY + 2, mainX + sidebarW + 1, mainY + mainH, 0x20FFFFFF);

        // Список модулей выбранной категории
        int contentX = mainX + sidebarW + 16;
        int contentY = mainY + 16;
        int cardW = mainW - sidebarW - 32;

        context.drawText(this.textRenderer, currentCategory.getLabel().toUpperCase(), contentX, contentY, 0xFFA1A1AA, false);

        List<Module> categoryModules = ModuleManager.getModulesByCategory(currentCategory);
        int currentY = contentY + 18;

        for (Module m : categoryModules) {
            // Карточка модуля
            context.fill(contentX, currentY, contentX + cardW, currentY + 24, 0x25FFFFFF);
            context.drawText(this.textRenderer, m.getName(), contentX + 8, currentY + 7, 0xFFE4E4E7, false);

            int toggleColor = m.isEnabled() ? 0xFFA3E635 : 0xFF3F3F46;
            context.fill(contentX + cardW - 20, currentY + 6, contentX + cardW - 8, currentY + 18, toggleColor);

            currentY += 26;

            // Настройки внутри модуля
            for (Setting s : m.getSettings()) {
                context.fill(contentX + 12, currentY, contentX + cardW, currentY + 18, 0x15FFFFFF);

                if (s instanceof BooleanSetting bs) {
                    context.drawText(this.textRenderer, "• " + bs.getName(), contentX + 18, currentY + 5, 0xFF9CA3AF, false);
                    int sColor = bs.isEnabled() ? 0xFFA3E635 : 0xFF3F3F46;
                    context.fill(contentX + cardW - 16, currentY + 5, contentX + cardW - 8, currentY + 13, sColor);
                } else if (s instanceof NumberSetting ns) {
                    String valStr = String.format("• %s: %.1f", ns.getName(), ns.getValue());
                    context.drawText(this.textRenderer, valStr, contentX + 18, currentY + 5, 0xFFA3E635, false);
                } else if (s instanceof ModeSetting ms) {
                    context.drawText(this.textRenderer, "• " + ms.getName() + ": [" + ms.getMode() + "]", contentX + 18, currentY + 5, 0xFF38BDF8, false);
                }
                currentY += 20;
            }
            currentY += 4;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int mainW = 460;
            int mainH = 280;
            int mainX = (this.width - mainW) / 2;
            int mainY = (this.height - mainH) / 2;
            int sidebarW = 120;

            // Переключение вкладок слева
            Module.Category[] categories = Module.Category.values();
            for (int i = 0; i < categories.length; i++) {
                int itemY = mainY + 45 + i * 28;
                if (mouseX >= mainX && mouseX <= mainX + sidebarW && mouseY >= itemY && mouseY <= itemY + 24) {
                    currentCategory = categories[i];
                    return true;
                }
            }

            // Клики по модулям и настройкам справа
            int contentX = mainX + sidebarW + 16;
            int currentY = mainY + 34;
            int cardW = mainW - sidebarW - 32;

            for (Module m : ModuleManager.getModulesByCategory(currentCategory)) {
                // Клик по модулю (Вкл/Выкл)
                if (in(mouseX, mouseY, contentX, currentY, cardW, 24)) {
                    m.toggle();
                    return true;
                }
                currentY += 26;

                // Клики по параметрам модуля
                for (Setting s : m.getSettings()) {
                    if (in(mouseX, mouseY, contentX + 12, currentY, cardW - 12, 18)) {
                        if (s instanceof BooleanSetting bs) {
                            bs.toggle();
                        } else if (s instanceof NumberSetting ns) {
                            double next = ns.getValue() + 5.0;
                            if (next > ns.getMax()) next = ns.getMin();
                            ns.setValue(next);
                        } else if (s instanceof ModeSetting ms) {
                            ms.cycle();
                        }
                        return true;
                    }
                    currentY += 20;
                }
                currentY += 4;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
