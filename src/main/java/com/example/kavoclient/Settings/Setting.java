package com.example.kavoclient.settings;

import java.util.List;

public abstract class Setting {
    private final String name;

    public Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Чекбокс (Вкл/Выкл)
    public static class BooleanSetting extends Setting {
        private boolean enabled;

        public BooleanSetting(String name, boolean defaultValue) {
            super(name);
            this.enabled = defaultValue;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public void toggle() { this.enabled = !this.enabled; }
    }

    // Слайдер (Числовые значения)
    public static class NumberSetting extends Setting {
        private double value;
        private final double min, max, increment;

        public NumberSetting(String name, double defaultValue, double min, double max, double increment) {
            super(name);
            this.value = defaultValue;
            this.min = min;
            this.max = max;
            this.increment = increment;
        }

        public double getValue() { return value; }
        public void setValue(double value) {
            double precision = 1.0 / this.increment;
            this.value = Math.round(Math.max(min, Math.min(max, value)) * precision) / precision;
        }
        public double getMin() { return min; }
        public double getMax() { return max; }
    }

    // Выбор режима (Mode / Enum)
    public static class ModeSetting extends Setting {
        private int index;
        private final List<String> modes;

        public ModeSetting(String name, String defaultMode, List<String> modes) {
            super(name);
            this.modes = modes;
            this.index = modes.indexOf(defaultMode);
            if (this.index == -1) this.index = 0;
        }

        public String getMode() { return modes.get(index); }
        public void cycle() {
            index = (index + 1) % modes.size();
        }
    }
}
