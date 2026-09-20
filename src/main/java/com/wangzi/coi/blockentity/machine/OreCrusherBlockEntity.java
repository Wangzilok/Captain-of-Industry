package com.wangzi.coi.blockentity.machine;

import com.wangzi.coi.Config.ModBlockEntityConfig;
import com.wangzi.coi.Config.EnergyStorageConfig;
import com.wangzi.coi.Config.ModBlockType;
import com.wangzi.coi.blockentity.GeneralIndustryBlockEntity;
import com.wangzi.coi.blockentity.MachineEnergyStorage;
import com.wangzi.coi.container.menu.OreCrusherMenu;
import com.wangzi.coi.init.ModBlockEntities;
import com.wangzi.coi.init.ModRecipeTypes;
import com.wangzi.coi.recipe.machine.GeneralRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 继承 BlockEntity 类，使其具备存储自定义数据和逻辑的能力
// 实现 MenuProvider 接口，使该方块能够被玩家右键打开 GUI 界面
public class OreCrusherBlockEntity extends GeneralIndustryBlockEntity {
    // ==================== 构建与配置常量 ====================
    // 显示在 GUI 中的名称
    private static final String DISPLAY_NAME = "block.coi.title.ore_crusher";

    // 方块类型
    private static final ModBlockType MOD_BLOCK_TYPE = ModBlockType.MACHINE;

    //  物品处理器大小
    private static final int ITEM_STACK_HANDLER_SIZE = 2;

    //  输入槽最大下标
    private static final int INPUT_SLOT_IDX = 0;

    // 能量存储，最大容量 10 FE，每次最多接收/提取 200/20 FE
    private static final EnergyStorageConfig ENERGY_STORAGE = new EnergyStorageConfig(20, 20, 10, 0);

    // 配方类型
    private static final RecipeType<?> RECIPE_TYPE = ModRecipeTypes.ORE_CRUSHER_TYPE.get();
    // ==================== 构建与配置常量 ====================

    // 加工一个物品所需的总时间
    private static final int TOTAL_PROCESS_TIME = 200;

    // 每次加工消耗的能量（FE/tick）
    private static final int ENERGY_PER_TICK = 10;

    // 当前加工进度
    private int progress = 0;

    // 机器状态
    private int state = 0;

    // 用于 GUI 显示进度的数据容器（1 个整数，存储当前进度）
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> state;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> state = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    // 构造函数
    public OreCrusherBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ORE_CRUSHER_ENTITY.get(), pPos, pBlockState, new ModBlockEntityConfig(DISPLAY_NAME, MOD_BLOCK_TYPE, ITEM_STACK_HANDLER_SIZE, INPUT_SLOT_IDX, ENERGY_STORAGE, RECIPE_TYPE));
    }

    // 当玩家右键点击方块时，服务端调用此方法来生成 GUI 容器
    @Nullable
    @Override
    public  AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new OreCrusherMenu(pContainerId, pPlayerInventory, this, dataAccess);
    }

    // 保存数据：当区块卸载或游戏保存时，把物品存进存档
    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        // 先调用父类的保存方法
        super.saveAdditional(pTag);

        // 保存当前加工进度，防止区块卸载后进度丢失
        pTag.putInt("progress", progress);
    }

    // 读取数据：当区块加载或读取存档时，把物品从存档里拿出来
    @Override
    public void load(@NotNull CompoundTag pTag) {
        // 先调用父类的读取方法
        super.load(pTag);

        // 恢复加工进度
        if (pTag.contains("progress")) dataAccess.set(0, pTag.getInt("progress"));
    }

    // ==================== 核心 Tick 逻辑 ====================
    // 由 Block 类的 tick() 调用，每个游戏刻执行一次
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, OreCrusherBlockEntity pBlockEntity) {
        tickNetworkDiscovery(pLevel, pBlockEntity);

        // 查询能量和配方
        if(!pullEnergy(pLevel, pBlockEntity) || !checkRecipe(pLevel, pBlockEntity) || !checkEnergy(pBlockEntity)) {
            if (pBlockEntity.progress > 0) {
                pBlockEntity.progress = 0;
                pBlockEntity.dataAccess.set(0, 0);
            }
            pBlockEntity.state = 0;
            return;
        }

        // 有匹配配方且输出槽可容纳，开始或继续加工
        pBlockEntity.progress++;
        pBlockEntity.state = 1;
        pBlockEntity.dataAccess.set(0, pBlockEntity.progress);

        // 扣除能量
        ((MachineEnergyStorage) pBlockEntity.energyStorage).consumeEnergy(ENERGY_PER_TICK, false);

        // 加工完成
        if (pBlockEntity.progress >= TOTAL_PROCESS_TIME) {
            GeneralRecipe recipe = pBlockEntity.currentRecipe;

            // 1. 遍历配方定义的【所有输入】，扣除对应数量的物品
            List<Ingredient> inputs = recipe.getInputs();
            List<Integer> inputCounts = recipe.getInputCounts();
            for (int i = 0; i < inputs.size(); i++) {
                // extractItem 的第三个参数 false 表示真正扣除物品
                pBlockEntity.itemStackHandler.extractItem(i, inputCounts.get(i), false);
            }

            // 2. 遍历配方定义的【所有输出】，将产物放入对应的槽位
            List<ItemStack> outputs = recipe.getOutputs();
            int outputStartIdx = pBlockEntity.modBlockEntityConfig.inputSlotIdx() + 1;
            for (int i = 0; i < outputs.size(); i++) {
                ItemStack result = outputs.get(i).copy();
                pBlockEntity.itemStackHandler.insertItem(outputStartIdx + i, result, false);
            }

            // 3. 重置进度为 0，准备下一轮加工
            pBlockEntity.progress = 0;
            pBlockEntity.dataAccess.set(0, 0);
            // 4. 标记数据已修改，触发保存
            pBlockEntity.setChanged();
        }
    }
}