package com.wangzi.coi.util;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RenderUtil {
    // 旋转工具
    public static void applyRotation(PoseStack poseStack, Vector3f center, Vector3f rotation) {
        // 平移到结构中心
        poseStack.translate(center.x(), center.y(), center.z());

        // 应用自定义旋转
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(rotation.x())));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(rotation.y())));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(rotation.z())));

        // 平移回去（抵消偏移）
        poseStack.translate(-center.x(), -center.y(), -center.z());
    }
}
