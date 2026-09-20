package com.wangzi.coi.container.menu;

import com.wangzi.coi.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OreCrusherMenu extends GeneralMenu {
    // 客户端构造函数
    public OreCrusherMenu(int id, Inventory pInv, FriendlyByteBuf buf) {
        super(id, pInv, buf, ModMenuTypes.ORE_CRUSHER_MENU.get());
    }

    // 服务端构造函数
    public OreCrusherMenu(int id, Inventory pInv, BlockEntity entity, ContainerData data) {
        super(id, pInv, entity, data, ModMenuTypes.ORE_CRUSHER_MENU.get());
    }

    @Override
    protected void addMachineSlots() {
        // 输入槽
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 24, 34));

        // 输出槽
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 136, 34) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false; // 输出槽禁止手动放入物品
            }
        });

        super.addMachineSlots();
    }

    // 给 Screen 获取进度条进度
    public int getProgress(){
        return data.get(0) * 35 / 200;
    }

    // 给 Screen 获取机器状态
    public int getState(){
        return data.get(1);
    }
}