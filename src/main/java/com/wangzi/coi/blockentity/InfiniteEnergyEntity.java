package com.wangzi.coi.blockentity;

import com.wangzi.coi.Config.EnergyStorageConfig;
import com.wangzi.coi.Config.ModBlockEntityConfig;
import com.wangzi.coi.Config.ModBlockType;
import com.wangzi.coi.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfiniteEnergyEntity extends GeneralIndustryBlockEntity {
    // ==================== 构建与配置常量 ====================
    // 显示在 GUI 中的名称
    private static final String DISPLAY_NAME = "";

    // 方块类型
    private static final ModBlockType MOD_BLOCK_TYPE = ModBlockType.ENERGY;

    //  物品处理器大小
    private static final int ITEM_STACK_HANDLER_SIZE = 0;

    //  输入槽最大下标
    private static final int INPUT_SLOT_IDX = -1;

    // 能量存储，最大容量 10 FE，每次最多接收/提取 200/20 FE
    private static final EnergyStorageConfig ENERGY_STORAGE = new EnergyStorageConfig(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    // 配方类型
    private static final RecipeType<?> RECIPE_TYPE = null;
    // ==================== 构建与配置常量 ====================

    // 构造函数
    public InfiniteEnergyEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.INFINITE_ENERGY.get(), pPos, pBlockState, new ModBlockEntityConfig(DISPLAY_NAME, MOD_BLOCK_TYPE, ITEM_STACK_HANDLER_SIZE, INPUT_SLOT_IDX, ENERGY_STORAGE, RECIPE_TYPE));
    }

    // 当玩家右键点击方块时，服务端调用此方法来生成 GUI 容器
    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return null;
    }

    // ==================== 核心 Tick 逻辑（双模式） ====================
    // 由 Block 类的 tick() 调用，每个游戏刻执行一次
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, InfiniteEnergyEntity pBlockEntity) {
        tickNetworkDiscovery(pLevel, pBlockEntity);

        // 【公共前置】确保内部能量始终是满的（模拟无尽能源）
        int currentEnergy = pBlockEntity.energyStorage.getEnergyStored();
        if (currentEnergy < Integer.MAX_VALUE) pBlockEntity.energyStorage.receiveEnergy(Integer.MAX_VALUE - currentEnergy, false);

        // ========== 模式 A：新版能量网络注入 ==========
        if(!pushEnergy(pLevel, pBlockEntity)) return;

        // ========== 模式 B：旧版相邻方块物理推电 ==========
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pPos.relative(dir);
            BlockEntity neighbor = pLevel.getBlockEntity(neighborPos);
            if (neighbor == null) continue;

            LazyOptional<IEnergyStorage> neighborCap = neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite());
            neighborCap.ifPresent(neighborStorage -> {
                // 先模拟对方能接收多少
                int accepted = neighborStorage.receiveEnergy(pBlockEntity.energyStorage.getEnergyStored(), true);
                if (accepted > 0) {
                    // 实际推送
                    neighborStorage.receiveEnergy(accepted, false);
                }
            });
        }
    }
}