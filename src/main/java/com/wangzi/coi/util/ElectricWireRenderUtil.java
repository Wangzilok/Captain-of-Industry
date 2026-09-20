package com.wangzi.coi.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.wangzi.coi.Config.EightDirection;
import com.wangzi.coi.block.GeneralExtendedBlock;
import com.wangzi.coi.blockentity.GeneralTelegraphPoleBlockEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class ElectricWireRenderUtil {
    // 自定义渲染类型
    public static final RenderType WIRE_RENDER_TYPE = RenderType.create(
            // 渲染类型的唯一标识名，仅用于调试/日志
            "wire_render_type",
            // 顶点格式
            DefaultVertexFormat.POSITION_COLOR,
            // 图元类型
            VertexFormat.Mode.QUADS,
            // 顶点缓冲区的初始容量
            2048,
            // 是否参与方块破坏动画的渲染排序
            false,
            // 是否在上传GPU前按深度排序透明面片
            true,
            // 渲染管线状态集合
            RenderType.CompositeState.builder()
                    // 使用 MC 内置的着色器
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    // 关闭背面剔除
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    // 创建透明度状态
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "translucent_transparency",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                            },
                            RenderSystem::disableBlend
                    ))
                    // 透明渲染类型
                    .createCompositeState(true)
    );

    // 渲染向量计算
    public static void buildWireMesh(
            VertexConsumer consumer, Matrix4f matrix,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            double cameraX, double cameraY, double cameraZ,
            float baseSag, float width, int segments,
            int r, int g, int b, int a
    ) {
        // 定位中心点
        x0 = (float) (x0 + 0.5f - cameraX);
        y0 = (float) (y0 + 0.5f - cameraY);
        z0 = (float) (z0 + 0.5f - cameraZ);
        x1 = (float) (x1 + 0.5f - cameraX);
        y1 = (float) (y1 + 0.5f - cameraY);
        z1 = (float) (z1 + 0.5f - cameraZ);

        // 计算跨度
        float spanX = x1 - x0;
        float spanY = y1 - y0;
        float spanZ = z1 - z0;

        // 计算下垂量，最大不超过一格
        float sag = Math.max(0.3f, Math.min(baseSag, 1.0f));

        // 存储每个端点的光照值
        int n = segments + 1;

        // 弧线采样点坐标
        float[] ptsX = new float[n];
        float[] ptsY = new float[n];
        float[] ptsZ = new float[n];

        // right向量分量
        float[] rX = new float[n];
        float[] rY = new float[n];
        float[] rZ = new float[n];

        // up向量分量
        float[] uX = new float[n];
        float[] uY = new float[n];
        float[] uZ = new float[n];

        // 预计算所有端点坐标、正交基和光照
        for (int i = 0; i <= segments; i++) {
            // 归一化参数
            float t = (float) i / segments;

            // 二次贝塞尔下垂量
            float parabola = sag * 4.0f * t * (1.0f - t);

            // 线性插值
            float px = x0 + spanX * t;
            float py = y0 + spanY * t - parabola;
            float pz = z0 + spanZ * t;

            ptsX[i] = px;
            ptsY[i] = py;
            ptsZ[i] = pz;

            // 切线方向估算
            float dx, dy, dz;
            if (i == 0) {
                float tN = 1.0f / segments;
                dx = (x0 + spanX * tN) - px;
                dy = (y0 + spanY * tN - sag * 4.0f * tN * (1.0f - tN)) - py;
                dz = (z0 + spanZ * tN) - pz;
            } else if (i == segments) {
                float tP = (float) (segments - 1) / segments;
                dx = px - (x0 + spanX * tP);
                dy = py - (y0 + spanY * tP - sag * 4.0f * tP * (1.0f - tP));
                dz = pz - (z0 + spanZ * tP);
            } else {
                float tP = (float) (i - 1) / segments;
                float tN = (float) (i + 1) / segments;
                dx = (x0 + spanX * tN) - (x0 + spanX * tP);
                dy = (y0 + spanY * tN - sag * 4 * tN * (1 - tN)) - (y0 + spanY * tP - sag * 4 * tP * (1 - tP));
                dz = (z0 + spanZ * tN) - (z0 + spanZ * tP);
            }

            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1e-6f) continue;
            float dirX = dx / len, dirY = dy / len, dirZ = dz / len;

            // 计算right向量
            float refX = 0f, refY = 1f, refZ = 0f;
            if (Math.abs(dirY) > 0.99f) { refX = 1f; refY = 0f; refZ = 0f; }
            float crossX = dirY * refZ - dirZ * refY;
            float crossY = dirZ * refX - dirX * refZ;
            float crossZ = dirX * refY - dirY * refX;
            float cLen = (float) Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);
            if (cLen > 1e-6f) {
                rX[i] = (crossX / cLen) * width;
                rY[i] = (crossY / cLen) * width;
                rZ[i] = (crossZ / cLen) * width;
            }

            // 计算up向量
            float upX = dirY * rZ[i] - dirZ * rY[i];
            float upY = dirZ * rX[i] - dirX * rZ[i];
            float upZ = dirX * rY[i] - dirY * rX[i];
            float uLen = (float) Math.sqrt(upX * upX + upY * upY + upZ * upZ);
            if (uLen > 1e-6f) {
                uX[i] = (upX / uLen) * width;
                uY[i] = (upY / uLen) * width;
                uZ[i] = (upZ / uLen) * width;
            }
        }

        // 绘制主体Quad
        emitWireQuads(consumer, matrix, segments, ptsX, ptsY, ptsZ, rX, rY, rZ, uX, uY, uZ, r, g, b, a);
    }

    // 电线主体渲染
    public static void emitWireQuads(VertexConsumer consumer, Matrix4f matrix, int segments,
                                  float[] ptsX, float[] ptsY, float[] ptsZ,
                                  float[] rX, float[] rY, float[] rZ,
                                  float[] uX, float[] uY, float[] uZ,
                                  int r, int g, int b, int a) {
        for (int i = 0; i < segments; i++) {
            for (int j = 1; j < 256; j = j << 1) {

                int k1 = (j & 204) != 0 ? 1 : 0;
                int k2 = (j & 153) != 0 ? 1 : -1;
                int k3 = (j & 15) != 0 ? 1 : 0;
                int k4 = (j & 240) != 0 ? 1 : 0;

                consumer.vertex(matrix,
                                ptsX[i + k1] + k2 * k3 * rX[i + k1] + k2 * k4 * uX[i + k1],
                                ptsY[i + k1] + k2 * k3 * rY[i + k1] + k2 * k4 * uY[i + k1],
                                ptsZ[i + k1] + k2 * k3 * rZ[i + k1] + k2 * k4 * uZ[i + k1])
                        .color(r, g, b, a)
                        .endVertex();
            }
        }
    }

    // 连接偏移量计算
    public static float[] computeTerminalOffset(GeneralTelegraphPoleBlockEntity startBE, GeneralTelegraphPoleBlockEntity endBE) {
        float offsetX0 = 0;
        float offsetZ0 = 0;
        float offsetX1 = 0;
        float offsetZ1 = 0;

        float deltaX = endBE.getBlockPos().getX() - startBE.getBlockPos().getX();
        float deltaZ = endBE.getBlockPos().getZ() - startBE.getBlockPos().getZ();

        EightDirection startDirection = startBE.getBlockState().getValue(GeneralExtendedBlock.FACING8);
        EightDirection endDirection = endBE.getBlockState().getValue(GeneralExtendedBlock.FACING8);

        float startTerminalR = startBE.getTerminalR();
        float endTerminalR = endBE.getTerminalR();

        if (startDirection == EightDirection.NORTH || startDirection == EightDirection.SOUTH) {
            offsetX0 = startTerminalR;
            offsetZ0 = 0;
        } else if (startDirection == EightDirection.EAST || startDirection == EightDirection.WEST) {
            offsetX0 = 0;
            offsetZ0 = startTerminalR;
        } else if (startDirection == EightDirection.NORTH_EAST || startDirection == EightDirection.SOUTH_WEST) {
            offsetX0 = startTerminalR / (float) Math.sqrt(2);
            offsetZ0 = startTerminalR / (float) Math.sqrt(2);
        } else if (startDirection == EightDirection.NORTH_WEST || startDirection == EightDirection.SOUTH_EAST) {
            offsetX0 = -startTerminalR / (float) Math.sqrt(2);
            offsetZ0 = startTerminalR / (float) Math.sqrt(2);
        }

        if (endDirection == EightDirection.NORTH || endDirection == EightDirection.SOUTH) {
            if (((deltaX * deltaZ > 0) && (offsetZ0 != 0)) || (offsetX0 * offsetZ0 < 0)) {
                offsetX1 = -endTerminalR;
            } else {
                offsetX1 = endTerminalR;
            }
        } else if (endDirection == EightDirection.EAST || endDirection == EightDirection.WEST) {
            if (((deltaX * deltaZ > 0) && (offsetX0 != 0)) && (offsetX0 * offsetZ0 >= 0)) {
                offsetZ1 = -endTerminalR;
            } else {
                offsetZ1 = endTerminalR;
            }
        } else if (endDirection == EightDirection.NORTH_EAST || endDirection == EightDirection.SOUTH_WEST) {
            if ((deltaX == 0 && deltaZ != 0) && (offsetX0 * offsetZ0 < 0)) {
                offsetX1 = -endTerminalR / (float) Math.sqrt(2);
                offsetZ1 = -endTerminalR / (float) Math.sqrt(2);
            } else {
                offsetX1 = endTerminalR / (float) Math.sqrt(2);
                offsetZ1 = endTerminalR / (float) Math.sqrt(2);
            }
        } else if (endDirection == EightDirection.NORTH_WEST || endDirection == EightDirection.SOUTH_EAST) {
            if (((deltaX == 0 && deltaZ != 0) && (offsetX0 * offsetZ0 > 0)) || (offsetX0 != 0 && offsetZ0 == 0)) {
                offsetX1 = endTerminalR / (float) Math.sqrt(2);
                offsetZ1 = -endTerminalR / (float) Math.sqrt(2);
            } else {
                offsetX1 = -endTerminalR / (float) Math.sqrt(2);
                offsetZ1 = endTerminalR / (float) Math.sqrt(2);
            }
        }

        return new float[]{offsetX0, offsetZ0, offsetX1, offsetZ1};
    }
}
