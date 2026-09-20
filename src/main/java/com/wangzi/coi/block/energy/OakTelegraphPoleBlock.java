package com.wangzi.coi.block.energy;

import com.wangzi.coi.block.GeneralExtendedBlock;
import com.wangzi.coi.blockentity.GeneralTelegraphPoleBlockEntity;
import com.wangzi.coi.blockentity.energy.OakTelegraphPoleBlockEntity;
import com.wangzi.coi.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class OakTelegraphPoleBlock extends GeneralExtendedBlock {
    // 长
    protected static final int LENGTH = 1;

    // 宽
    protected static final int WIDTH = 1;

    // 高
    protected static final int HEIGHT = 4;

    // 构造函数
    public OakTelegraphPoleBlock(Properties pProperties) {
        super(pProperties, OakTelegraphPoleBlockEntity::new, LENGTH, WIDTH, HEIGHT );
    }

    // 碰撞箱
    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if(state.getValue(HEIGHT_INDEX) == 3)
        {
            return Block.box(5, 0, 5, 11, 14, 11);
        }

        return Block.box(6, 0, 6, 10, 16, 10);
    }

    @Override
    public void playerWillDestroy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player player) {
        // 服务端处理
        if (pLevel.isClientSide) {
            super.playerWillDestroy(pLevel, pPos, pState, player);
            return;
        }

        // 仅底部方块执行
        if (pState.getValue(LENGTH_INDEX) == 0 && pState.getValue(WIDTH_INDEX) == 0 && pState.getValue(HEIGHT_INDEX) == 0) {
            // 获取顶部方块实体
            BlockPos topPos = findTopPos(pPos, pState);
            BlockEntity topBe = pLevel.getBlockEntity(topPos);

            if (topBe instanceof GeneralTelegraphPoleBlockEntity pole) {
                int wireCount = pole.getConnectedTo().size();

                if (wireCount > 0) {
                    ItemStack wires = new ItemStack(ModItems.ELECTRIC_WIRE.get(), wireCount);
                    popResource(pLevel, pPos, wires);
                }
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, player);
    }
}