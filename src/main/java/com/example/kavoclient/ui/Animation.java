package com.example.kavoclient.ui;

public class Animation {
    private float value;
    private float target;
    private final float speed;

    public Animation(float initialValue, float speed) {
        this.value = initialValue;
        this.target = initialValue;
        this.speed = speed;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public void update(float delta) {
        this.value += (this.target - this.value) * Math.min(1.0f, this.speed * delta);
    }

    public float getValue() {
        return this.value;
    }
}
