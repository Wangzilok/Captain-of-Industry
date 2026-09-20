package com.wangzi.coi.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wangzi.coi.init.ModItems;
import com.wangzi.coi.util.ElectricWireRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "coi", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PreviewElectricWireRenderer {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // 判断当前渲染阶段是否在天气渲染之后
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        // 获取 Minecraft 客户端的全局单例，其持有所有客户端核心对象的引用
        Minecraft mc = Minecraft.getInstance();

        // 获取玩家实例，若尚未进入世界或处于加载过渡态则跳过渲染
        Player player = mc.player;
        if (player == null) return;

        // 获取客户端世界实例，若维度未加载完成或正在切换则跳过渲染
        ClientLevel level = mc.level;
        if (level == null) return;

        // 获取当前帧的局部刻进度
        float partialTick = event.getPartialTick();

        // 获取相机当前位置
        Vec3 cameraPos = event.getCamera().getPosition();

        // 渲染事件开头
        PoseStack poseStack = event.getPoseStack();

        // 将当前矩阵压入栈保存一份副本
        poseStack.pushPose();

        // 提取栈顶的组合变换矩阵
        Matrix4f matrix = poseStack.last().pose();

        // 创建独立缓冲区
        MultiBufferSource.BufferSource wireBuffer = MultiBufferSource.immediate(new BufferBuilder(256));

        // 指定使用 lines（线条）渲染类型
        // VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        // 指定使用 solid（不透明实体）渲染类型
        // VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        // 使用自定义渲染类型
        VertexConsumer vertexConsumer = wireBuffer.getBuffer(ElectricWireRenderUtil.WIRE_RENDER_TYPE);

        // 获取主手物品
        ItemStack stack = player.getMainHandItem();

        // 如果主手持有电线
        if (stack.is(ModItems.ELECTRIC_WIRE.get())) {

            // 获取该电线的NBT标签
            CompoundTag tag = stack.getTagElement("wire_link");

            // 如果存在连接
            if (tag != null && tag.contains("WireStartPos")) {
                // 起始电线杆位置
                BlockPos startPos = BlockPos.of(tag.getLong("WireStartPos"));

                Vec3 eyePos = player.getEyePosition(partialTick);

                double endX = eyePos.x - 0.7;
                double endY = eyePos.y - 1.2;
                double endZ = eyePos.z - 0.4;

                // 玩家位置
                BlockPos endPos = player.getOnPos();

                if(Math.sqrt(startPos.distSqr(endPos)) <= 40 ) {
                    ElectricWireRenderUtil.buildWireMesh(
                            vertexConsumer, matrix,
                            startPos.getX(), startPos.getY(), startPos.getZ(),
                            (float) endX, (float) endY, (float) endZ,
                            cameraPos.x, cameraPos.y, cameraPos.z,
                            0.5f, 0.03f, 16,
                            0, 255, 0, 127
                    );
                }
                else {
                    ElectricWireRenderUtil.buildWireMesh(
                            vertexConsumer, matrix,
                            startPos.getX(), startPos.getY(), startPos.getZ(),
                            (float) endX, (float) endY, (float) endZ,
                            cameraPos.x, cameraPos.y, cameraPos.z,
                            0.5f, 0.03f, 16,
                            255, 0, 0, 127
                    );
                }
            }
        }

        // 提交缓冲区
        wireBuffer.endBatch(ElectricWireRenderUtil.WIRE_RENDER_TYPE);

        // 恢复矩阵状态
        poseStack.popPose();
    }
}