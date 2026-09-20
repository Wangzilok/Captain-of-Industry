package com.wangzi.coi.client;

import com.wangzi.coi.Coi;
import com.wangzi.coi.blockentity.GeneralIndustryBlockEntity;
import com.wangzi.coi.blockentity.GeneralTelegraphPoleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Collectors;

// @Mod.EventBusSubscriber(modid = Coi.MODID, value = Dist.CLIENT)
class ClientDebugRenderer {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !(mc.hitResult instanceof BlockHitResult bhr)) return;

        BlockEntity be = mc.level.getBlockEntity(bhr.getBlockPos());

        // ✅ 将渲染相关变量提取到公共作用域
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = mc.getWindow().getGuiScaledWidth() / 2;
        int y = mc.getWindow().getGuiScaledHeight() / 2 + 20;

        // 电线杆调试信息
        if (be instanceof GeneralTelegraphPoleBlockEntity pole) {
            String info = "NetID: " + pole.getNetWorkId()
                    + " | Links: " + pole.getConnectedTo().size();
            guiGraphics.drawString(mc.font, info, x, y, 0xFFFFFFFF, true);
            y += 10; // ✅ 换行，避免与下方机器信息重叠
        }

        // 机器所处网络查询
        if (be instanceof GeneralIndustryBlockEntity machine) {
            machine.queryNetWorkId();  // 渲染时查一次
            var networkIds = machine.getNearbyNetworkIds();
            String netInfo = networkIds.isEmpty()
                    ? "Network: None"
                    : "Network: " + networkIds.intStream()
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(", ", "[", "]"));
            guiGraphics.drawString(mc.font, netInfo, x, y, 0xFFAAAAFF, true);
        }
    }
}