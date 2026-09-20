package com.wangzi.coi.container.menu;

import com.wangzi.coi.blockentity.GeneralBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

// 继承 AbstractContainerMenu 类实现，作为 GUI 的后端逻辑核心，负责处理物品槽位和数据同步
public abstract class GeneralMenu extends AbstractContainerMenu {
    // 当前菜单绑定的方块实体
    protected final GeneralBlockEntity blockEntity;

    // 当前菜单所在的世界
    protected final Level level;

    // 同步数据的容器
    protected final ContainerData data;

    // 玩家背包
    protected final Inventory playerInventory;

    // 客户端构造函数
    public GeneralMenu(int id, Inventory pInv, FriendlyByteBuf buf, MenuType<?> type) {
        super(type, id);

        if (!(pInv.player.level().getBlockEntity(buf.readBlockPos()) instanceof GeneralBlockEntity generalEntity)) {
            throw new IllegalStateException(
                    "在坐标 " + buf.readBlockPos() + " 处未找到预期的方块实体！预期类型: " + GeneralBlockEntity.class.getSimpleName()
            );
        }

        this.blockEntity = generalEntity;
        this.level = pInv.player.level();
        this.data = new SimpleContainerData(8);
        this.playerInventory = pInv;

        addMachineSlots();

        // 注册数据同步通道，让 Forge 自动将服务端的数据同步到客户端中
        addDataSlots(data);
    }

    // 服务端构造函数
    public GeneralMenu(int id, Inventory pInv, BlockEntity entity, ContainerData data, MenuType<?> type) {
        super(type, id);

        if (!(entity instanceof GeneralBlockEntity generalEntity)) {
            throw new IllegalArgumentException(
                    "传入的方块实体必须是通用方块实体（GeneralBlockEntity）！但实际传入的类型是: " + (entity == null ? "null" : entity.getClass().getSimpleName())
            );
        }

        this.blockEntity = generalEntity;
        this.level = pInv.player.level();
        this.data = data;
        this.playerInventory = pInv;

        addMachineSlots();

        // 注册数据同步通道，让 Forge 自动将服务端的数据同步到客户端中
        addDataSlots(data);
    }

    // 处理 Shift+点击 时的快速移动逻辑
    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        // 边界检查：防止索引越界
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        // 动态获取配置
        ItemStack slotStack = slot.getItem();
        ItemStack copy = slotStack.copy();
        int inputSlotIdx = blockEntity.modBlockEntityConfig.inputSlotIdx();
        int itemStackHandlerSize = blockEntity.modBlockEntityConfig.itemStackHandlerSize();

        // 核心移动逻辑（完全动态化）
        boolean success = (index < itemStackHandlerSize)
                ? this.moveItemStackTo(slotStack, itemStackHandlerSize, itemStackHandlerSize + 36, true)   // 机器 -> 玩家
                : this.moveItemStackTo(slotStack, 0, inputSlotIdx + 1, false);            // 玩家 -> 机器

        // 如果没移动成功，直接返回空
        if (!success) return ItemStack.EMPTY;

        // 移动成功后的标准收尾
        slot.setChanged();
        if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
        slot.onTake(player, slotStack);

        return copy;
    }

    // 判断玩家是否仍在菜单有效范围内
    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    protected void addMachineSlots() {
        // 添加玩家的快捷栏槽位（9个快捷栏格子）
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 148));
        }

        // 添加玩家的背包槽位（27个主背包格子）
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 90 + i * 18));
            }
        }
    }
}