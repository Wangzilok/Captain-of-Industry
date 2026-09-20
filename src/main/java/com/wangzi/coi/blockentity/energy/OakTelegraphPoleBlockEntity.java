package com.wangzi.coi.blockentity.energy;

import com.wangzi.coi.Config.EnergyStorageConfig;
import com.wangzi.coi.Config.ModBlockEntityConfig;
import com.wangzi.coi.Config.ModBlockType;
import com.wangzi.coi.blockentity.GeneralTelegraphPoleBlockEntity;
import com.wangzi.coi.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class OakTelegraphPoleBlockEntity extends GeneralTelegraphPoleBlockEntity {
    // ==================== 构建与配置常量 ====================
    // 显示在 GUI 中的名称
    private static final String DISPLAY_NAME = "";

    // 方块类型
    private static final ModBlockType MOD_BLOCK_TYPE = ModBlockType.ENERGY_TRANSPORT;

    //  物品处理器大小
    private static final int ITEM_STACK_HANDLER_SIZE = 0;

    //  输入槽最大下标
    private static final int INPUT_SLOT_IDX = 0;

    // 能量存储：最大容量 10000 FE，每次最多接收/提取 200 FE
    private static final EnergyStorageConfig ENERGY_STORAGE = null;

    // 配方类型
    private static final RecipeType<?> RECIPE_TYPE = null;
    // ==================== 构建与配置常量 ====================

    // 中心到端子的距离
    protected float terminalR = 6f / 16;

    // 端子 Y 轴偏移
    protected float terminalY = (8.5f - 8) / 16;

    // 连接半径
    protected final int connectedR = 4;

    // 连接高度
    protected final int connectedH = 3;

    // 构造函数
    public OakTelegraphPoleBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.OAK_TELEGRAPH_POLE_ENTITY.get(), pPos, pBlockState, new ModBlockEntityConfig(DISPLAY_NAME, MOD_BLOCK_TYPE, ITEM_STACK_HANDLER_SIZE, INPUT_SLOT_IDX, ENERGY_STORAGE, RECIPE_TYPE));
    }

    // 数据获取
    @Override
    public float getTerminalR() {return this.terminalR;}
    @Override
    public float getTerminalY() {return this.terminalY;}
    @Override
    public int getConnectedR() {return this.connectedR;}
    @Override
    public int getConnectedH() {return this.connectedH;}
}