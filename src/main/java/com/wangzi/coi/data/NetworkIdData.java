package com.wangzi.coi.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class NetworkIdData extends SavedData {
    // 定义唯一键名
    private static final String DATA_NAME = "coi_network_id";

    // 设置默认初始值
    private int nextNetworkId = 1;

    // 实例获取入口
    public static NetworkIdData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(NetworkIdData::load, NetworkIdData::new, DATA_NAME);
    }

    // 写入 NBT
    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("NextNetworkId", nextNetworkId);
        return tag;
    }

    // 反序列化，从 NBT 读取
    public static NetworkIdData load(CompoundTag tag) {
        NetworkIdData data = new NetworkIdData();
        data.nextNetworkId = tag.getInt("NextNetworkId");

        // 防御性检查
        if (data.nextNetworkId < 1) data.nextNetworkId = 1;

        return data;
    }

    // 分配新 ID 并自动标记脏数据
    public int allocate() {
        int id = nextNetworkId++;
        setDirty(); // ← 关键！告诉 MC 需要保存到磁盘
        return id;
    }
}
