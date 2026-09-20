package com.wangzi.coi.event;

import com.wangzi.coi.block.GeneralExtendedBlock;
import com.wangzi.coi.blockentity.GeneralTelegraphPoleBlockEntity;
import com.wangzi.coi.entity.ElectricWireEntity;
import com.wangzi.coi.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "coi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElectricWireEvent {
    // 右键时给电线创建 NBT（服务端事件）
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 获取触发事件的玩家
        Player player = event.getEntity();

        // 获取玩家当前使用的物品
        ItemStack stack = player.getItemInHand(event.getHand());

        // 获取交互发生的世界/维度
        Level level = event.getLevel();

        // 获取交互发生的具体坐标
        BlockPos pos = event.getPos();

        // 检测环境是否为客户端，是则退出，确保在服务端运行
        if (level.isClientSide()) return;

        // 检测手持物品是否为电线，以及交互对象是否为电线杆
        if (!stack.is(ModItems.ELECTRIC_WIRE.get()) || !(level.getBlockState(pos).getBlock() instanceof GeneralExtendedBlock startBlock)) return;

        // 计算出该电线杆顶部位置
        BlockPos topPos = startBlock.findTopPos(pos, level.getBlockState(pos));

        // 获取当前点击电线杆的 BlockEntity
        BlockEntity be = level.getBlockEntity(topPos);
        if (!(be instanceof GeneralTelegraphPoleBlockEntity poleBE)) {
            player.displayClientMessage(Component.literal("找不到电线杆实体！"), true);
            return;
        }

        // 添加NBT子标签
        CompoundTag tag = stack.getOrCreateTagElement("wire_link");

        // 第二次点击：建立连接
        if (tag.contains("WireStartPos")) {
            // 获取第一次连接的位置
            BlockPos startPos = BlockPos.of(tag.getLong("WireStartPos"));

            if(Math.sqrt(startPos.distSqr(topPos)) >= 40 ) {
                player.displayClientMessage(Component.literal("超出电线最大距离！"), true);
                stack.setTag(null);
                return;
            }

            // 检测连接对象是否为自己
            if(startPos.equals(topPos)) {
                player.displayClientMessage(Component.literal("无法连接自身！"), true);
                stack.setTag(null);
                return;
            }

            // 获取起点电线杆的 BlockEntity
            BlockEntity startBE = level.getBlockEntity(startPos);
            if (!(startBE instanceof GeneralTelegraphPoleBlockEntity startPoleBE)) {
                player.displayClientMessage(Component.literal("起点电线杆已被破坏！"), true);
                stack.setTag(null);
                return;
            }

            // 使用返回 boolean 的 addConnection
            boolean addedToStart = startPoleBE.addConnection(topPos);
            boolean addedToEnd = poleBE.addConnection(startPos);

            if (addedToStart && addedToEnd) {
                player.displayClientMessage(Component.literal("电线连接成功！"), true);
                stack.shrink(1);  // 消耗一个电线

                // 创建电线实体
                ElectricWireEntity wire = new ElectricWireEntity(level);

                // 传入两端信息
                wire.setup(startPos, topPos);

                // 必须在服务端调用，客户端会自动同步
                level.addFreshEntity(wire);

                int unifiedID = Math.min(startPoleBE.getNetWorkId(), poleBE.getNetWorkId());
                startPoleBE.setNetWorkId(unifiedID, true);
                poleBE.setNetWorkId(unifiedID, true);
            }
            else if (!addedToStart && !addedToEnd) {
                player.displayClientMessage(Component.literal("这两根电线杆已连接！"), true);
            }
            else {
                if (addedToStart) startPoleBE.removeConnection(topPos);
                if (addedToEnd) poleBE.removeConnection(startPos);
                player.displayClientMessage(Component.literal("连接异常！"), true);
            }

            // 清除 NBT
            stack.setTag(null);
        }
        // 第一次点击：保存起点
        else {
            tag.putLong("WireStartPos", topPos.asLong());
            player.displayClientMessage(Component.literal("已选择起点"), true);
        }
    }

    // 清除未完成的连接
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 只在服务端的 Tick 结束时执行
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        // 获取触发事件的玩家
        Player player = event.player;

        // 获取玩家主手持有的物品
        ItemStack stack = player.getMainHandItem();

        // 检查主手物品是否是电线
        if (stack.is(ModItems.ELECTRIC_WIRE.get())) {
            // 获取 NBT 标签
            CompoundTag tag = stack.getTagElement("wire_link");

            // 如果存在未完成的连线 NBT
            if (tag != null && tag.contains("WireStartPos")) {
                // 获取保存的起始坐标
                BlockPos startPos = BlockPos.of(tag.getLong("WireStartPos"));

                // 检测起始坐标的方块是否还在
                boolean isPoleStillThere = player.level().getBlockState(startPos).getBlock() instanceof GeneralExtendedBlock;

                // 如果电线杆被破坏了就清除 NBT
                if (!isPoleStillThere) {
                    stack.setTag(null);
                }
            }
        }
    }

    // Shift 加鼠标右键取消连接
    @SubscribeEvent
    public static void onRightClickAir(PlayerInteractEvent.RightClickItem event) {
        // 获取触发事件的玩家
        Player player = event.getEntity();

        // 获取玩家当前使用的物品
        ItemStack stack = player.getItemInHand(event.getHand());

        // 获取交互发生的世界/维度
        Level level = event.getLevel();

        // 检测环境是否为客户端，是则退出，确保在服务端运行
        if (level.isClientSide()) return;

        // 只在手持电线且 Shift 按下时取消
        if (stack.is(ModItems.ELECTRIC_WIRE.get()) && player.isShiftKeyDown()) {
            CompoundTag tag = stack.getTagElement("wire_link");
            if (tag != null && tag.contains("WireStartPos")) {
                stack.setTag(null);
                player.displayClientMessage(Component.literal("已取消连接！"), true);
            }
        }
    }
}