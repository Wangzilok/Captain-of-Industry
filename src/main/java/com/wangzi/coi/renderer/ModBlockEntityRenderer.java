package com.wangzi.coi.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wangzi.coi.Config.EightDirection;
import com.wangzi.coi.block.GeneralExtendedBlock;
import com.wangzi.coi.blockentity.GeneralBlockEntity;
import com.wangzi.coi.util.RenderUtil;
import com.wangzi.coi.util.RotatedBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ModBlockEntityRenderer implements BlockEntityRenderer<GeneralBlockEntity> {
    // 固定种子
    private static final RandomSource RANDOM = RandomSource.create();

    // 构造函数
    public ModBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull GeneralBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 获取方块
        if (!(blockEntity.getBlockState().getBlock() instanceof GeneralExtendedBlock extendedBlock)) return;

        // 获取 Minecraft 客户端的全局单例，其持有所有客户端核心对象的引用
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 获取四向和八向
        EightDirection facing8 = blockEntity.getBlockState().getValue(GeneralExtendedBlock.FACING8);

        // 计算结构中心
        float centerX = extendedBlock.getLength() / 2.0f;
        float centerY = extendedBlock.getHeight() / 2.0f;
        float centerZ = extendedBlock.getWidth() / 2.0f;
        Vector3f center = new Vector3f(centerX, centerY, centerZ);

        // 自定义旋转角度
        float rotationX = 0f;
        float rotationY = facing8.getRotation();
        float rotationZ = 0f;
        Vector3f rotation = new Vector3f(rotationX, rotationY, rotationZ);

        // 获取方块的模型渲染器
        ModelBlockRenderer renderer = mc.getBlockRenderer().getModelRenderer();

        if(blockEntity.getLevel() != null) {
            // 计算方块的世界坐标
            BlockPos partPos = blockEntity.getBlockPos();

            // 构建方块状态
            BlockState partState = blockEntity.getLevel().getBlockState(partPos);

            // 获取模型
            BakedModel model = mc.getBlockRenderer().getBlockModel(partState);

            // 获取该方块在区块渲染中应当使用的渲染层
            RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(partState);

            // 将当前矩阵压入栈保存一份副本
            poseStack.pushPose();

            // 应用旋转
            RenderUtil.applyRotation(poseStack, center, rotation);

            // 设置世界旋转让 AO 应用
            BlockAndTintGetter rotatedGetter = new RotatedBlockGetter(
                    blockEntity.getLevel(),
                    partPos,
                    facing8.getRotation()
            );

            // 渲染模型
            renderer.tesselateBlock(
                    rotatedGetter,
                    model,
                    partState,
                    partPos,
                    poseStack,
                    bufferSource.getBuffer(renderType),
                    false,
                    RANDOM,
                    partPos.asLong(),
                    packedOverlay
            );

            poseStack.popPose();
        }
    }

    // 根据客户端区块渲染距离进行剔除
    @Override
    public int getViewDistance() {
        Minecraft mc = Minecraft.getInstance();
        return mc.options.renderDistance().get() * 16;
    }
}
