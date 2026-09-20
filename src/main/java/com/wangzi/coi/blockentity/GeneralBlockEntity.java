package com.wangzi.coi.blockentity;

import com.wangzi.coi.Config.ModBlockEntityConfig;
import com.wangzi.coi.Config.EnergyStorageConfig;
import com.wangzi.coi.recipe.machine.GeneralRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

// 继承 BlockEntity 类，使其具备存储自定义数据和逻辑的能力
// 实现 MenuProvider 接口，使该方块能够被玩家右键打开 GUI 界面
public abstract class GeneralBlockEntity extends BlockEntity implements MenuProvider {
    // 方块实体的配置
    public final ModBlockEntityConfig modBlockEntityConfig;

    // 物品处理器
    public  final ItemStackHandler itemStackHandler;

    // 物品能力引用，用于对外暴露物品交互接口
    @Nullable
    private final LazyOptional<IItemHandler> itemCapability;
    @Nullable
    private final LazyOptional<IItemHandler> universalCapability;

    // 能量存储，最大容量 10000 FE，每次最多接收/提取 200 FE
    protected final EnergyStorage energyStorage;

    // 能量能力引用，用于对外暴露能量交互接口
    @Nullable
    private final LazyOptional<IEnergyStorage> energyCapability;

    // 用实例变量保存当前正在处理的配方
    protected GeneralRecipe currentRecipe = null;

    // 构造函数
    public GeneralBlockEntity(BlockEntityType pBlockEntityType, BlockPos pPos, BlockState pBlockState, ModBlockEntityConfig pModBlockEntityConfig) {
        super(pBlockEntityType, pPos, pBlockState);

        this.modBlockEntityConfig = pModBlockEntityConfig;

        // 初始化物品处理器
        final int inputSlotIdx = modBlockEntityConfig.inputSlotIdx();
        final int itemStackHandlerSize = modBlockEntityConfig.itemStackHandlerSize();
        this.itemStackHandler = modBlockEntityConfig.itemStackHandlerSize() != 0 ? new ItemStackHandler(modBlockEntityConfig.itemStackHandlerSize()) {
            // 当格子里的物品发生变化时，标记方块数据已修改，触发游戏自动保存
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        } : null;
        if (this.itemStackHandler != null) {
            this.itemCapability = LazyOptional.of(() -> itemStackHandler);

            // 外部输入输出引用
            this.universalCapability = LazyOptional.of(() ->
                    new RangedWrapper(itemStackHandler, 0, itemStackHandlerSize) {
                        // 拦截提取：如果外部尝试提取的物品在“输入槽”范围内，直接拒绝
                        @Override
                        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                            if (slot <= inputSlotIdx) {
                                return ItemStack.EMPTY; // 输入槽禁止提取
                            }
                            return super.extractItem(slot, amount, simulate);
                        }

                        // 拦截插入：如果外部尝试插入的物品目标位置在“输出槽”范围内，直接拒绝
                        @Override
                        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                            if (slot > inputSlotIdx) {
                                return stack; // 输出槽禁止插入
                            }
                            return super.insertItem(slot, stack, simulate);
                        }
                    }
            );
        }
        else {
            this.itemCapability = LazyOptional.empty();
            this.universalCapability = LazyOptional.empty();
        }

        // 初始化能量存储器
        final EnergyStorageConfig energyStorageConfig = this.modBlockEntityConfig.energyStorageConfig();
        if(energyStorageConfig != null) {
            int capacity = energyStorageConfig.capacity();
            int maxReceive = energyStorageConfig.maxReceive();
            int maxExtract = energyStorageConfig.maxExtract();
            int energy = energyStorageConfig.energy();

            switch (pModBlockEntityConfig.blockType()) {
                case MACHINE -> this.energyStorage = new MachineEnergyStorage(capacity,
                        maxReceive,
                        maxExtract,
                        energy);
                default -> this.energyStorage = new EnergyStorage(capacity,
                        maxReceive,
                        maxExtract,
                        energy);
            }

            this.energyCapability = LazyOptional.of(() -> energyStorage);
        }
        else {
            this.energyStorage = null;
            this.energyCapability = LazyOptional.empty();
        }
    }

    // 返回 GUI 界面顶部的显示名称
    @Override
    @NotNull
    public Component getDisplayName() {
        return Component.translatable(modBlockEntityConfig.displayName());
    }

    // 当玩家右键点击方块时，服务端调用此方法来生成 GUI 容器
    @Override
    @Nullable
    public abstract AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer);

    // 使菜单能够获取到物品处理器
    public ItemStackHandler getItemHandler() {
        return itemStackHandler;
    }

    // 对外暴露能力接口，让其他模组识别并与本方块交互
    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (!isRemoved()) {
            if (cap == ForgeCapabilities.ITEM_HANDLER && itemStackHandler != null) {
                // 优先判断方向（给漏斗/管道用）
                if (side != null) {
                    if (universalCapability != null) return universalCapability.cast();
                }

                // 如果没有方向（比如玩家打开 GUI、机器内部加工），返回原始处理器
                if (itemCapability != null) return itemCapability.cast();
            }

            // 只有子类配置了能量，才暴露能量能力
            if (energyStorage != null && cap == ForgeCapabilities.ENERGY) {
                if (energyCapability != null) return energyCapability.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    // 方块被破坏或世界卸载时自动调用，清理所有已暴露的能力引用
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (energyCapability != null) energyCapability.invalidate();
        if (itemCapability != null) itemCapability.invalidate();
        if (universalCapability != null) universalCapability.invalidate();
    }

    // 检查配方
    public static boolean checkRecipe(Level pLevel, GeneralBlockEntity pBlockEntity) {
        // 查找配方（直接传入 BlockEntity 本身，因为它实现了 Container 接口）
        Optional<GeneralRecipe> recipe = queryRecipe(pLevel, pBlockEntity);

        // 判断是否找到配方
        if(recipe.isPresent()) {
            pBlockEntity.currentRecipe = recipe.get();
        }
        else {
            pBlockEntity.currentRecipe = null;
            return false;
        }

        // 检查输出槽是否能容纳配方产出的所有物品
        return checkOutputAccept(pBlockEntity, recipe.get());
    }

    // 检查能量
    public static boolean checkEnergy(GeneralBlockEntity pBlockEntity) {
        if(pBlockEntity.energyStorage != null) {
            return ((MachineEnergyStorage) pBlockEntity.energyStorage).consumeEnergy(pBlockEntity.modBlockEntityConfig.energyStorageConfig().maxExtract(), true);
        }
        else {
            return false;
        }
    }

    // 查找配方
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static Optional<GeneralRecipe> queryRecipe(Level pLevel, GeneralBlockEntity pBlockEntity) {
        // 动态创建一个与机器输入槽数量匹配的“替身”容器
        int inputSlots = pBlockEntity.modBlockEntityConfig.inputSlotIdx() + 1;
        SimpleContainer tempContainer = new SimpleContainer(inputSlots);

        // 把 ItemStackHandler 里的输入槽物品，复制到替身容器中
        for (int i = 0; i < inputSlots; i++) {
            tempContainer.setItem(i, pBlockEntity.itemStackHandler.getStackInSlot(i));
        }

        // 让原版配方系统去检查这个“替身”容器
        return (Optional<GeneralRecipe>) (Optional<?>) pLevel.getRecipeManager()
                .getRecipeFor(
                        (RecipeType) pBlockEntity.modBlockEntityConfig.recipeType(),
                        tempContainer, // 传入替身容器，完美绕过类型检查
                        pLevel
                );
    }

    // 检查输出槽是否能容纳配方产出的所有物品
    protected static boolean checkOutputAccept(GeneralBlockEntity pBlockEntity, GeneralRecipe recipe) {
        // 获取配方定义的所有输出产物
        List<ItemStack> outputs = recipe.getOutputs();
        int outputStartIdx = pBlockEntity.modBlockEntityConfig.inputSlotIdx() + 1;

        // 遍历每一个产物，进行【模拟插入】
        for (int i = 0; i < outputs.size(); i++) {
            int targetSlot = outputStartIdx + i; // 计算目标输出槽的索引
            ItemStack output = outputs.get(i).copy();

            // 模拟插入（simulate = true）
            ItemStack remainder = pBlockEntity.itemStackHandler.insertItem(targetSlot, output, true);

            // 只要有任意一个产物放不进去，就直接返回 false
            if (!remainder.isEmpty()) {
                return false;
            }
        }
        return true; // 所有产物都能放进去，返回 true
    }

    // 保存数据：当区块卸载或游戏保存时，把物品存进存档
    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        // 先调用父类的保存方法
        super.saveAdditional(pTag);

        // 将物品处理器序列化为 NBT 标签，并以 "inventory" 为键名存入 pTag
        if (itemStackHandler != null) pTag.put("inventory", itemStackHandler.serializeNBT());

        // 保存能量值
        if(energyStorage != null) pTag.putInt("energy", energyStorage.getEnergyStored());
    }

    // 读取数据：当区块加载或读取存档时，把物品从存档里拿出来
    @Override
    public void load(@NotNull CompoundTag pTag) {
        // 先调用父类的读取方法
        super.load(pTag);

        // 检查存档中是否存在 "inventory" 这个键
        // 如果存在，就将保存的 NBT 数据反序列化回物品处理器中
        if (pTag.contains("inventory")) itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));

        // 读取能量值
        // 将存档中的能量注入存储
        if(pTag.contains("energy")) energyStorage.receiveEnergy(pTag.getInt("energy"), false);
    }
}