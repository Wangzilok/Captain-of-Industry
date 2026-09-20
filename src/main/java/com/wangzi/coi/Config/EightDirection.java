package com.wangzi.coi.Config;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum EightDirection implements StringRepresentable {
    NORTH("north", 0f),
    NORTH_EAST("north_east", 315f),
    EAST("east", 270f),
    SOUTH_EAST("south_east", 225f),
    SOUTH("south", 180f),
    SOUTH_WEST("south_west", 135f),
    WEST("west", 90f),
    NORTH_WEST("north_west", 45f);

    private final String name;
    private final float rotation;

    EightDirection(String name, float rotation) {
        this.name = name;
        this.rotation = rotation;
    }

    public String getName() { return name; }

    // 获取旋转角
    public float getRotation() { return rotation; }

    @Override
    @NotNull
    public String getSerializedName() { return name; }
}
