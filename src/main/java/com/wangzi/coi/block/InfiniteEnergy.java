package com.wangzi.coi.block;

import com.wangzi.coi.blockentity.InfiniteEnergyEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InfiniteEnergy extends GeneralBlock {
    // 构造方法
    public InfiniteEnergy(Properties pProperties) {
        super(pProperties, InfiniteEnergyEntity::new);
    }

    // 调用 tick
    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> hasTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return (level, pos, state, blockEntity)
                -> InfiniteEnergyEntity.tick(level, pos, state, (InfiniteEnergyEntity)blockEntity);
    }
}