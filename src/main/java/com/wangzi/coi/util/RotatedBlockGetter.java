package com.wangzi.coi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RotatedBlockGetter implements BlockAndTintGetter {
    private final BlockAndTintGetter delegate;
    private final BlockPos originPos;
    private final float cos;
    private final float sin;

    public RotatedBlockGetter(BlockAndTintGetter delegate, BlockPos originPos, float angleDegrees) {
        this.delegate = delegate;
        this.originPos = originPos;
        float rad = (float) Math.toRadians(-angleDegrees);  // 反向旋转
        this.cos = Mth.cos(rad);
        this.sin = Mth.sin(rad);
    }

    // 把"查询位置"反向旋转回世界坐标
    private BlockPos rotate(BlockPos pos) {
        int dx = pos.getX() - originPos.getX();
        int dy = pos.getY() - originPos.getY();
        int dz = pos.getZ() - originPos.getZ();
        int rx = Math.round(dx * cos - dz * sin);
        int rz = Math.round(dx * sin + dz * cos);
        return originPos.offset(rx, dy, rz);
    }

    // ==================== 需要旋转的方法 ====================

    @Override
    @NotNull
    public BlockState getBlockState(@NotNull BlockPos pos) {
        return delegate.getBlockState(rotate(pos));
    }

    @Override
    public int getBrightness(@NotNull LightLayer layer, @NotNull BlockPos pos) {
        return delegate.getBrightness(layer, rotate(pos));
    }

    // ==================== 直接委托的方法 ====================

    @Override
    @NotNull
    public FluidState getFluidState(@NotNull BlockPos pos) {
        return delegate.getFluidState(rotate(pos));
    }

    @Override
    public float getShade(@NotNull Direction dir, boolean shade) {
        return delegate.getShade(dir, shade);
    }

    @Override
    @NotNull
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return delegate.getMinBuildHeight();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull BlockPos pos) {
        return delegate.getBlockEntity(rotate(pos));
    }

    @Override
    public int getBlockTint(@NotNull BlockPos pos, @NotNull ColorResolver resolver) {
        return delegate.getBlockTint(rotate(pos), resolver);
    }
}