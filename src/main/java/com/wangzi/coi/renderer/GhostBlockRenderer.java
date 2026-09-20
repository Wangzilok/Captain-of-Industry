package com.wangzi.coi.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wangzi.coi.Config.EightDirection;
import com.wangzi.coi.block.GeneralExtendedBlock;
import com.wangzi.coi.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GhostBlockRenderer {
    // 自定义渲染类型
    // 使用原生半透明渲染类型绑定纹理图集
    private static final RenderType GHOST_RENDER_TYPE = RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS);

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 如果不在半透明方块渲染阶段，则返回
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        // 获取 Minecraft 客户端的全局单例，其持有所有客户端核心对象的引用
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 获取主手物品
        ItemStack mainHand = mc.player.getMainHandItem();
        // 过滤方块物品
        if (!(mainHand.getItem() instanceof BlockItem blockItem)) return;
        // 过滤拓展方块类
        if (!(blockItem.getBlock() instanceof GeneralExtendedBlock extendedBlock)) return;

        // 获取命中目标
        HitResult hitResult = mc.hitResult;
        // 过滤命中目标是否为方块
        if (!(hitResult instanceof BlockHitResult blockHitResult)) return;
        // 过滤命中目标是否为实际方块
        if (blockHitResult.getType() != HitResult.Type.BLOCK) return;
        // 过滤是否命中方块上表面
        if (blockHitResult.getDirection() != Direction.UP) return;

        // 获取命中物品顶面上方一格
        BlockPos placePos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        // 检查该格的方块是否可被替换
        if (!mc.level.getBlockState(placePos).canBeReplaced()) return;

        // 获取四向
        Direction facing = mc.player.getDirection();
        Direction left = facing.getCounterClockWise();
        Direction back = facing.getOpposite();

        // 获取八向
        float yaw = mc.player.getYRot();
        int index = ((Mth.floor(yaw / 45.0f + 0.5f) + 4) & 7);
        EightDirection facing8 = EightDirection.values()[index];

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

        // 获取相机（玩家视角）的世界坐标
        var cameraPos = event.getCamera().getPosition();

        // 固定种子
        RandomSource random = RandomSource.create();

        // 渲染事件开头
        PoseStack poseStack = event.getPoseStack();

        // 将当前矩阵压入栈保存一份副本
        poseStack.pushPose();

        // 定义 RGBA
        float r, g, b;
        float alpha = 0.7f;

        // 获取方块放置上下文
        BlockPlaceContext context = new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, mc.player.getMainHandItem(), blockHitResult);

        // 检查是否拥有放置空，有则渲染为绿色，没有则渲染为红色
        if (extendedBlock.getStateForPlacement(context) != null) {
            r = 0f; g = 1f;
        } else {
            r = 1f; g = 0f;
        }
        b = 0f;

        // 获取渲染缓冲区
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // 使用自定义渲染类型
        VertexConsumer vertexConsumer = bufferSource.getBuffer(GHOST_RENDER_TYPE);

        for (int x = 0; x < extendedBlock.getLength(); x++) {
            for (int y = 0; y < extendedBlock.getWidth(); y++) {
                for (int z = 0; z < extendedBlock.getHeight(); z++) {
                    // 计算子方块的世界坐标
                    BlockPos partPos = placePos.relative(left, x).relative(back, y).above(z);

                    // 构建子方块的 BlockState
                    BlockState partState = blockItem.getBlock().defaultBlockState()
                            .setValue(GeneralExtendedBlock.FACING, facing)
                            .setValue(GeneralExtendedBlock.FACING8, facing8)
                            .setValue(GeneralExtendedBlock.getLengthIndex(), x)
                            .setValue(GeneralExtendedBlock.getWidthIndex(), y)
                            .setValue(GeneralExtendedBlock.getHeightIndex(), z);

                    // 获取模型
                    BakedModel model = mc.getBlockRenderer().getBlockModel(partState);

                    // 将当前矩阵压入栈保存一份副本
                    poseStack.pushPose();

                    // 计算矩阵偏移（偏移基于摄像机位置）
                    poseStack.translate(
                            partPos.getX() - cameraPos.x,
                            partPos.getY() - cameraPos.y,
                            partPos.getZ() - cameraPos.z
                    );

                    // 应用旋转
                    RenderUtil.applyRotation(poseStack, center, rotation);

                    // 获取变换矩阵快照
                    PoseStack.Pose pose = poseStack.last();

                    // 固定光照
                    int light = 0xF000F0;

                    // 遍历模型的六个面
                    for (Direction dir : Direction.values()) {
                        // 遍历方向面修改覆盖颜色
                        for (BakedQuad quad : model.getQuads(partState, dir, random)) {
                            vertexConsumer.putBulkData(
                                    pose, quad,
                                    r, g, b, alpha,
                                    light,
                                    OverlayTexture.NO_OVERLAY,
                                    true
                            );
                        }
                    }

                    // 遍历通用面修改覆盖颜色
                    for (BakedQuad quad : model.getQuads(partState, null, random)) {
                        vertexConsumer.putBulkData(
                                pose, quad,
                                r, g, b, alpha,
                                light,
                                OverlayTexture.NO_OVERLAY,
                                true
                        );
                    }

                    // 恢复矩阵状态
                    poseStack.popPose();
                }
            }
        }

        // 提交缓冲区
        bufferSource.endBatch(GHOST_RENDER_TYPE);

        // 恢复矩阵状态
        poseStack.popPose();
    }
}