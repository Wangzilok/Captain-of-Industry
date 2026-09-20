package com.wangzi.coi.block.machine;

import com.wangzi.coi.block.GeneralBlock;
import com.wangzi.coi.blockentity.machine.OreCrusherBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OreCrusherBlock extends GeneralBlock {
    // 构造方法
    public OreCrusherBlock(Properties pProperties) {
        super(pProperties, OreCrusherBlockEntity::new);
    }

    // 拥有 UI
    @Override
    protected boolean hasUI() {
        return true;
    }

    // 拥有内容物掉落
    @Override
    protected boolean hasContent(){
        return true;
    }

    // 调用 tick
    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> hasTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return (level, pos, state, blockEntity)
                -> OreCrusherBlockEntity.tick(level, pos, state, (OreCrusherBlockEntity)blockEntity);
    }
}