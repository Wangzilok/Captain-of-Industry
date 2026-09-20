package com.wangzi.coi.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkEnergyManager extends SavedData {
    // 定义唯一键名
    private static final String DATA_NAME = "coi_network_energy";

    // 网络的共享能量存储
    private final ConcurrentHashMap<Integer, EnergyStorage> pools = new ConcurrentHashMap<>();

    // 实例获取入口
    public static NetworkEnergyManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                NetworkEnergyManager::load,
                NetworkEnergyManager::new,
                DATA_NAME
        );
    }

    // 获取或创建指定网络的能量存储
    public EnergyStorage getPool(int networkId, int capacity) {
        return pools.computeIfAbsent(networkId, id -> {
            setDirty();
            return new EnergyStorage(capacity, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        });
    }

    // 移除网络能量池
    public void remove(int networkId) {
        if (pools.remove(networkId) != null) {
            setDirty();
        }
    }

    // 检查能量池是否存在
    public EnergyStorage getPoolIfExists(int networkId) {
        return pools.get(networkId);
    }

    public ConcurrentHashMap<Integer, EnergyStorage> getPools() {
        return pools;
    }

    // ==================== 存档核心：写入 NBT ====================
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pTag) {
        CompoundTag poolsTag = new CompoundTag();
        for (Map.Entry<Integer, EnergyStorage> entry : pools.entrySet()) {
            CompoundTag poolTag = new CompoundTag();
            poolTag.putInt("Energy", entry.getValue().getEnergyStored());
            poolTag.putInt("Capacity", entry.getValue().getMaxEnergyStored());
            poolsTag.put(entry.getKey().toString(), poolTag);
        }
        pTag.put("Pools", poolsTag);
        return pTag;
    }

    // ==================== 存档核心：从 NBT 读取 ====================
    public static NetworkEnergyManager load(CompoundTag pTag) {
        NetworkEnergyManager data = new NetworkEnergyManager();
        if (pTag.contains("Pools")) {
            CompoundTag poolsTag = pTag.getCompound("Pools");
            for (String key : poolsTag.getAllKeys()) {
                int id = Integer.parseInt(key);
                CompoundTag poolTag = poolsTag.getCompound(key);
                int energy = poolTag.getInt("Energy");
                int capacity = poolTag.getInt("Capacity");

                EnergyStorage storage = new EnergyStorage(capacity, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
                storage.receiveEnergy(energy, false);
                data.pools.put(id, storage);
            }
        }
        return data;
    }
}
