package com.wangzi.coi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public abstract class GeneralExtendedBlock extends GeneralBlock {
    // 长
    protected final int length;

    // 宽
    protected final int width;

    // 高
    protected final int height;

    // 标记长
    protected static final IntegerProperty LENGTH_INDEX = IntegerProperty.create("length", 0, 15);

    // 标记宽
    protected static final IntegerProperty WIDTH_INDEX = IntegerProperty.create("width", 0, 15);

    // 标记高
    protected static final IntegerProperty HEIGHT_INDEX = IntegerProperty.create("height", 0, 15);

    // 构造方法（拥有方块实体）
    public GeneralExtendedBlock(Properties pProperties, BiFunction<BlockPos, BlockState, ? extends BlockEntity> pEntityFactory, int length, int width, int height) {
        super(pProperties, pEntityFactory);

        this.length = length;
        this.width = width;
        this.height = height;

        BlockState defaultState = this.stateDefinition.any();
        defaultState = defaultState.setValue(LENGTH_INDEX, 0);
        defaultState = defaultState.setValue(WIDTH_INDEX, 0);
        defaultState = defaultState.setValue(HEIGHT_INDEX, 0);
        this.registerDefaultState(defaultState);
    }

    // 构造方法（没有方块实体）
    public GeneralExtendedBlock(Properties pProperties, int length, int width, int height) {
        super(pProperties);

        this.length = length;
        this.width = width;
        this.height = height;

        BlockState defaultState = this.stateDefinition.any();
        defaultState = defaultState.setValue(LENGTH_INDEX, 0);
        defaultState = defaultState.setValue(WIDTH_INDEX, 0);
        defaultState = defaultState.setValue(HEIGHT_INDEX, 0);
        this.registerDefaultState(defaultState);
    }

    // 向方块状态系统注册属性
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(LENGTH_INDEX);
        pBuilder.add(WIDTH_INDEX);
        pBuilder.add(HEIGHT_INDEX);
    }

    // 放置前检查空间
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        // 先拿父类的结果
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        Direction left = getLeft(state);
        Direction back = getBack(state);

        if (pos.above(this.height).getY() >= level.getMaxBuildHeight()) return null;

        for(int x = 0; x < this.length; x++) {
            for(int y = 0; y < this.width; y++) {
                for(int z = 0; z < this.height; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos checkPos = pos.relative(left, x).relative(back, y).above(z);
                    if (!level.getBlockState(checkPos).canBeReplaced(context)) return null;
                }
            }
        }

        state = state.setValue(LENGTH_INDEX, 0);
        state = state.setValue(WIDTH_INDEX, 0);
        state = state.setValue(HEIGHT_INDEX, 0);
        return state;
    }

    // 放置后自动填充
    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
        Direction left = getLeft(state);
        Direction back = getBack(state);

        // 底部放好后，自动在上方放置
        for (int x = 0; x < this.length; x++) {
            for (int y = 0; y < this.width; y++) {
                for (int z = 0; z < this.height; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos placePos = pos.relative(left, x).relative(back, y).above(z);
                    BlockState placeState = state;

                    placeState = placeState.setValue(LENGTH_INDEX, x);
                    placeState = placeState.setValue(WIDTH_INDEX, y);
                    placeState = placeState.setValue(HEIGHT_INDEX, z);

                    level.setBlock(placePos, placeState, Block.UPDATE_ALL_IMMEDIATE);
                }
            }
        }
    }

    // 联动破坏，破坏任意一格时，整个结构一起掉
    @Override
    public void playerWillDestroy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Player player) {
        Direction left = getLeft(pState);
        Direction back = getBack(pState);

        // 破坏的是底部
        if (pState.getValue(LENGTH_INDEX) == 0 && pState.getValue(WIDTH_INDEX) == 0 && pState.getValue(HEIGHT_INDEX) == 0) {
            // 移除所有非底部方块（不掉落物品）
            for (int x = 0; x < this.length; x++) {
                for (int y = 0; y < this.width; y++) {
                    for (int z = 0; z < this.height; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        BlockPos partPos = pPos.relative(left, x).relative(back, y).above(z);
                        BlockState partState = pLevel.getBlockState(partPos);
                        if (partState.is(this) && partState.getValue(FACING) == pState.getValue(FACING)) {
                            pLevel.removeBlock(partPos, false);
                        }
                    }
                }
            }
        }
        else {
            // 破坏的不是底部 → 找到底部，让底部统一处理
            BlockPos bottomPos = findBottomPos(pPos, pState);
            BlockState bottomState = pLevel.getBlockState(bottomPos);

            if (bottomState.is(this)) {
                // 递归调用底部的联动破坏
                playerWillDestroy(pLevel, bottomPos, bottomState, player);

                // 静默移除当前方块
                pLevel.removeBlock(pPos, false);

                // 静默移除底部方块
                pLevel.removeBlock(bottomPos, false);

                // 让底部方块正常掉落
                if (!player.isCreative()) {
                    Block.dropResources(bottomState, pLevel, bottomPos, null, player, player.getMainHandItem());
                }
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, player);
    }

    // 根据当前方块的位置和状态，反推顶部坐标
    public BlockPos findTopPos(BlockPos pos, BlockState state) {
        Direction left = getLeft(state);
        Direction back = getBack(state);

        int x = ((this.length + 1) / 2) - state.getValue(LENGTH_INDEX) - 1;
        int y = this.height - state.getValue(HEIGHT_INDEX) - 1;
        int z = ((this.width + 1) / 2) - state.getValue(WIDTH_INDEX) - 1;

        return pos.relative(left, x).relative(back, z).above(y);
    }

    // 根据当前方块的位置和状态，反推底部坐标
    private BlockPos findBottomPos(BlockPos pos, BlockState state) {
        Direction left = getLeft(state);
        Direction back = getBack(state);

        int x = state.getValue(LENGTH_INDEX);
        int y = state.getValue(HEIGHT_INDEX);
        int z = state.getValue(WIDTH_INDEX);

        return pos.relative(left, -x).relative(back, -z).above(-y);
    }

    // 禁用原版渲染
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    // 根据朝向实时计算左侧方向（避免实例字段被覆盖）
    private Direction getLeft(BlockState state) { return state.getValue(FACING).getCounterClockWise(); }

    // 根据朝向实时计算后方方向
    private Direction getBack(BlockState state) { return state.getValue(FACING).getOpposite(); }

    // ==================== 数据读取和修改 ====================
    // 获取 length
    public int getLength() { return this.length; }
    // 获取 width
    public int getWidth() { return this.width; }
    // 获取 height
    public int getHeight() { return this.height; }
    // 获取 LENGTH_INDEX
    public static IntegerProperty getLengthIndex() { return LENGTH_INDEX; }
    // 获取 WIDTH_INDEX
    public static IntegerProperty getWidthIndex() { return WIDTH_INDEX; }
    // 获取 HEIGHT_INDEX
    public static IntegerProperty getHeightIndex() { return HEIGHT_INDEX; }
}
