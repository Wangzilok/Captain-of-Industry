package com.wangzi.coi.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wangzi.coi.blockentity.GeneralTelegraphPoleBlockEntity;
import com.wangzi.coi.entity.ElectricWireEntity;
import com.wangzi.coi.util.ElectricWireRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class ElectricWireRenderer extends EntityRenderer<ElectricWireEntity> {
    // 构造函数
    public ElectricWireRenderer(EntityRendererProvider.Context context) {
        super(context);

        // 禁用阴影投射
        this.shadowRadius = 0f;
    }

    // 实体渲染
    @Override
    public void render(@NotNull ElectricWireEntity entity, float yaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        // 获取 Minecraft 客户端的全局单例，其持有所有客户端核心对象的引用
        Minecraft mc = Minecraft.getInstance();

        // 获取客户端世界实例，若维度未加载完成或正在切换则跳过渲染
        ClientLevel level = mc.level;
        if (level == null) return;

        // 使用自定义渲染类型
        VertexConsumer vertexConsumer = bufferSource.getBuffer(ElectricWireRenderUtil.WIRE_RENDER_TYPE);

        // 提取栈顶的组合变换矩阵
        Matrix4f matrix = poseStack.last().pose();

        // 获取起点方块位置
        BlockPos startPos = entity.getStartPos();
        // 获取终点方块位置
        BlockPos endPos = entity.getEndPos();

        // 获取起点方块 BE
        if(!(level.getBlockEntity(startPos) instanceof GeneralTelegraphPoleBlockEntity startBE)) return;
        // 获取终点方块 BE
        if(!(level.getBlockEntity(endPos) instanceof GeneralTelegraphPoleBlockEntity endBE)) return;

        // 计算偏移量
        float[] offset = ElectricWireRenderUtil.computeTerminalOffset(startBE, endBE);
        float offsetX0 = offset[0];
        float offsetZ0 = offset[1];
        float offsetX1 = offset[2];
        float offsetZ1 = offset[3];

        // 实体位置作为原点
        double cameraPosX = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
        double cameraPosY = entity.yOld + (entity.getY() - entity.yOld) * partialTick;
        double cameraPosZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

        ElectricWireRenderUtil.buildWireMesh(
                vertexConsumer, matrix,
                startPos.getX() + offsetX0, startPos.getY() + startBE.getTerminalY(), startPos.getZ() + offsetZ0,
                endPos.getX() + offsetX1, endPos.getY() + endBE.getTerminalY(), endPos.getZ() + offsetZ1,
                cameraPosX, cameraPosY, cameraPosZ,
                0.5f, 0.03f, 16,
                0, 0, 0, 255
        );
        ElectricWireRenderUtil.buildWireMesh(
                vertexConsumer, matrix,
                startPos.getX() - offsetX0, startPos.getY() + startBE.getTerminalY(), startPos.getZ() - offsetZ0,
                endPos.getX() - offsetX1, endPos.getY() + endBE.getTerminalY(), endPos.getZ() - offsetZ1,
                cameraPosX, cameraPosY, cameraPosZ,
                0.5f, 0.03f, 16,
                0, 0, 0, 255
        );
    }

    // 绑定纹理
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ElectricWireEntity entity) {
        return new ResourceLocation("minecraft", "textures/misc/white.png");
    }

    // 禁用视锥体剔除
    @Override
    public boolean shouldRender(@NotNull ElectricWireEntity entity, @NotNull Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }
}