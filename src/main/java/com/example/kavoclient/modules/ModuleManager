package com.example.kavoclient.modules;

import com.example.kavoclient.settings.Setting.*;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        // Пример визуального модуля: FullBright (максимальная гамма)
        Module fullBright = new Module("FullBright", Module.Category.VISUAL, GLFW.GLFW_KEY_B) {
            @Override
            public void onTick() {
                if (mc.options != null && isEnabled()) {
                    mc.options.getGamma().setValue(100.0);
                }
            }
            @Override
            public void onDisable() {
                if (mc.options != null) {
                    mc.options.getGamma().setValue(1.0);
                }
            }
        };
        fullBright.addSetting(new ModeSetting("Режим", "Гамма", Arrays.asList("Гамма", "Ночное зрение")));
        modules.add(fullBright);

        // Пример информационного оверлея: HUD
        Module hud = new Module("HUD Overlay", Module.Category.VISUAL, GLFW.GLFW_KEY_H) {};
        hud.setEnabled(true);
        hud.addSetting(new BooleanSetting("Список модулей", true));
        hud.addSetting(new BooleanSetting("Координаты", true));
        hud.addSetting(new BooleanSetting("Счетчик FPS", true));
        modules.add(hud);

        // Каркас модулей интерфейса и визуализации
        Module tracers = new Module("Tracers", Module.Category.VISUAL, GLFW.GLFW_KEY_UNKNOWN) {};
        tracers.addSetting(new ModeSetting("Тип", "Стрелки", Arrays.asList("Стрелки", "Радар")));
        tracers.addSetting(new NumberSetting("Дистанция", 64.0, 10.0, 128.0, 5.0));
        modules.add(tracers);

        Module esp = new Module("ESP Entities", Module.Category.VISUAL, GLFW.GLFW_KEY_UNKNOWN) {};
        esp.addSetting(new ModeSetting("Стиль", "Контур", Arrays.asList("Контур", "2D Box")));
        esp.addSetting(new BooleanSetting("Только игроки", false));
        modules.add(esp);

        // Модуль передвижения
        Module sprint = new Module("AutoSprint", Module.Category.MOVEMENT, GLFW.GLFW_KEY_V) {
            @Override
            public void onTick() {
                if (mc.player != null && isEnabled() && mc.player.forwardSpeed > 0 && !mc.player.isSneaking()) {
                    mc.player.setSprinting(true);
                }
            }
        };
        modules.add(sprint);
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static List<Module> getModulesByCategory(Module.Category category) {
        List<Module> list = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) list.add(m);
        }
        return list;
    }

    public static void onKey(int key, int action) {
        if (action != GLFW.GLFW_PRESS) return;
        for (Module m : modules) {
            if (m.getKeyBind() == key && key != GLFW.GLFW_KEY_UNKNOWN) {
                m.toggle();
            }
        }
    }
}
