package com.wangzi.coi.block;

import com.wangzi.coi.Config.EightDirection;
import com.wangzi.coi.blockentity.GeneralBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

// 继承 HorizontalDirectionalBlock 类实现方块转向
// 实现 EntityBlock 接口实现，使方块能够拥有自己的方块实体
public abstract class GeneralBlock extends HorizontalDirectionalBlock implements EntityBlock {
    // 存储方块自己的方块实体
    private BiFunction<BlockPos, BlockState, ? extends BlockEntity> entityFactory = null;

    // 存储八向旋转时的方向
    public static final EnumProperty<EightDirection> FACING8 = EnumProperty.create("facing8", EightDirection.class);

    // 构造方法（拥有方块实体）
    public GeneralBlock(Properties pProperties, BiFunction<BlockPos, BlockState, ? extends BlockEntity> pEntityFactory) {
        super(pProperties);
        this.entityFactory = pEntityFactory;
    }

    // 构造方法（没有方块实体）
    public GeneralBlock(Properties pProperties) {
        super(pProperties);
    }

    // ==================== 钩子方法 ====================
    // 钩子方法：是否需要调动tick，调动需要按用法重写
    @Nullable
    protected <T extends BlockEntity> BlockEntityTicker<T> hasTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return null;
    }

    // 钩子方法：是否拥有UI
    protected boolean hasUI(){
        return false;
    }

    // 钩子方法：是否拥有内容物掉落
    protected boolean hasContent(){
        return false;
    }

    // ==================== 其他 ====================
    // 当方块被放置到世界中时，创建并返回对应的方块实体
    @Override
    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState){
        return entityFactory != null ? entityFactory.apply(pPos, pState) : null;
    }

    // 获取方块实体的 tick 方法
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel,
                                                                  @NotNull BlockState pState,
                                                                  @NotNull BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide()
                ? null
                : hasTicker(pLevel, pState, pBlockEntityType);
    }

    // 当玩家右键点击方块时触发，用于打开 GUI 界面
    @Override
    @NotNull
    public InteractionResult use(@NotNull BlockState pState,
                                 @NotNull Level pLevel,
                                 @NotNull BlockPos pPos,
                                 @NotNull Player pPlayer,
                                 @NotNull InteractionHand pHand,
                                 @NotNull BlockHitResult pHit) {
        // 仅在服务端执行打开界面的逻辑
        if (hasUI() && !pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof MenuProvider menuProvider) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, menuProvider, pPos);
                // 返回交互成功的结果，阻止游戏执行默认的交互逻辑
                return InteractionResult.sidedSuccess(pLevel.isClientSide());
            }
        }

        return InteractionResult.PASS;
    }

    // 玩家放置方块时调用，确定最终的方块状态
    @Override
    @Nullable
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        BlockState state = this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());

        float yaw = 0;
        if (pContext.getPlayer() != null) {
            yaw = pContext.getPlayer().getYRot();
        }
        int index = (Mth.floor(yaw / 45.0f + 0.5f) & 7);
        EightDirection dir = EightDirection.values()[index];
        state = state.setValue(FACING8, dir);

        return state;
    }

    // 向方块状态系统注册属性
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        pBuilder.add(FACING8);
    }

    // 方块被破坏后掉落内容物
    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        // 仅在方块被彻底破坏时，执行物品掉落逻辑
        if (hasContent() && !pState.is(pNewState.getBlock())) {
            // 获取方块实体
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof GeneralBlockEntity crusher) {
                // 创建一个与机器槽位数量一致的原版临时容器，作为适配器
                SimpleContainer tempContainer = new SimpleContainer(crusher.itemStackHandler.getSlots());

                // 将机器内部物品处理器的数据，一比一复制到临时容器中
                for (int i = 0; i < crusher.itemStackHandler.getSlots(); i++) tempContainer.setItem(i, crusher.itemStackHandler.getStackInSlot(i));
                // 调用原版掉落方法，将临时容器中的物品生成掉落物
                Containers.dropContents(pLevel, pPos, tempContainer);

                // 清空原机器中的物品数据，防止方块破坏后物品被复制
                for (int i = 0; i < crusher.itemStackHandler.getSlots(); i++) crusher.itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                // 通知周围方块更新信号状态
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}