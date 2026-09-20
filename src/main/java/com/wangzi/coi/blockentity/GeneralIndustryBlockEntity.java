package com.wangzi.coi.blockentity;

import com.wangzi.coi.Config.ModBlockEntityConfig;
import com.wangzi.coi.data.NetworkEnergyManager;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;

public abstract class GeneralIndustryBlockEntity extends GeneralBlockEntity {
    // 范围内的网络 ID
    protected final IntLinkedOpenHashSet nearbyNetworkIds = new IntLinkedOpenHashSet(4);

    // 构造函数
    public GeneralIndustryBlockEntity(BlockEntityType pBlockEntityType, BlockPos pPos, BlockState pBlockState, ModBlockEntityConfig pModBlockEntityConfig) {
        super(pBlockEntityType, pPos, pBlockState, pModBlockEntityConfig);
    }

    // 查询网络 ID
    public void queryNetWorkId() {
        nearbyNetworkIds.clear();

        // 机器自己的位置坐标
        int mx = this.worldPosition.getX();
        int my = this.worldPosition.getY();
        int mz = this.worldPosition.getZ();

        for(GeneralTelegraphPoleBlockEntity poleBE : GeneralTelegraphPoleBlockEntity.getTrackedPoles()) {
            int px = poleBE.getBlockPos().getX();
            int py = poleBE.getBlockPos().getY();
            int pz = poleBE.getBlockPos().getZ();

            // AABB 范围判定
            boolean inRangeX = mx >= px - poleBE.getConnectedR() && mx <= px + poleBE.getConnectedR();
            boolean inRangeZ = mz >= pz - poleBE.getConnectedR() && mz <= pz + poleBE.getConnectedR();
            boolean inRangeY = my >= py - poleBE.getConnectedH() && my <= py;

            if (inRangeX && inRangeZ && inRangeY && poleBE.getNetWorkId() != -1) {
                nearbyNetworkIds.add(poleBE.getNetWorkId());
            }
        }
    }

    // ==================== 持久化 ====================
    // 保存数据：当区块卸载或游戏保存时，把物品存进存档
    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        // 先调用父类的保存方法
        super.saveAdditional(pTag);

        // 存储范围内的网络 ID
        if (!nearbyNetworkIds.isEmpty()) {pTag.putIntArray("nearby_network_ids", nearbyNetworkIds.toIntArray());}
    }

    // 读取数据：当区块加载或读取存档时，把物品从存档里拿出来
    @Override
    public void load(@NotNull CompoundTag pTag) {
        // 先调用父类的读取方法
        super.load(pTag);

        // 读取范围内的网络 ID
        nearbyNetworkIds.clear();
        if (pTag.contains("nearby_network_ids", Tag.TAG_INT_ARRAY)) {
            for (int id : pTag.getIntArray("nearby_network_ids")) {
                nearbyNetworkIds.add(id);
            }
        }
    }

    // ==================== Tick 逻辑方法 ====================
    public static void tickNetworkDiscovery(Level pLevel, GeneralIndustryBlockEntity pBlockEntity) {
        // ID 列表为空时每3秒查询一次
        if (pBlockEntity.nearbyNetworkIds.isEmpty() && pLevel.getGameTime() % 60 == 0) {
            pBlockEntity.queryNetWorkId();
        }
    }

    // ==================== 数据读取和修改 ====================
    public IntLinkedOpenHashSet getNearbyNetworkIds() {return this.nearbyNetworkIds;}

    // ==================== 能量池交互 ====================
    // 拉取能量
    public static boolean pullEnergy(Level pLevel, GeneralIndustryBlockEntity pBlockEntity) {
        if (pLevel.isClientSide()) return false;

        // 强制转换获取服务端世界对象
        ServerLevel serverLevel = (ServerLevel) pLevel;

        // 获取能量池管理器
        NetworkEnergyManager energyManager = NetworkEnergyManager.get(serverLevel);

        // 计算机器内部还能缓存多少电
        int spaceAvailable = pBlockEntity.energyStorage.getMaxEnergyStored() - pBlockEntity.energyStorage.getEnergyStored();

        // 池子数据是否修改
        boolean poolModified = false;

        // 查询能量池
        for(int networkId : pBlockEntity.nearbyNetworkIds) {
            // 如果机器能量已满则跳过
            if (spaceAvailable <= 0) break;

            // 获取ID指向的能量池
            EnergyStorage energyPool = energyManager.getPoolIfExists(networkId);

            // 查看该能量池是否存在
            if(energyPool != null) {
                // 查询能量池内是否还有能量
                if (energyPool.getEnergyStored() > 0) {
                    // 先模拟抽取，确认能抽出多少能量
                    int simulated = energyPool.extractEnergy(spaceAvailable, true);

                    if (simulated > 0) {
                        // 充入机器内部缓存，并计算剩余容量
                        spaceAvailable -= pBlockEntity.energyStorage.receiveEnergy(
                                energyPool.extractEnergy(simulated, false),
                                false);

                        poolModified = true;
                    }
                }
            }
            else {
                pBlockEntity.queryNetWorkId();
                return false;
            }
        }

        if (poolModified) {
            energyManager.setDirty();
        }

        return true;
    }

    // 推送能量
    public static boolean pushEnergy(Level pLevel, GeneralIndustryBlockEntity pBlockEntity) {
        if (pLevel.isClientSide()) return false;

        // 强制转换获取服务端世界对象
        ServerLevel serverLevel = (ServerLevel) pLevel;

        // 获取能量池管理器
        NetworkEnergyManager energyManager = NetworkEnergyManager.get(serverLevel);

        // 池子数据是否修改
        boolean poolModified = false;

        for (int networkId : pBlockEntity.nearbyNetworkIds) {
            // 获取ID指向的能量池
            EnergyStorage energyPool = energyManager.getPoolIfExists(networkId);

            // 查看该能量池是否存在
            if (energyPool != null) {
                // 计算能量池内部还能缓存多少电
                int spaceAvailable = energyPool.getMaxEnergyStored() - energyPool.getEnergyStored();

                // 查询能量池内是否还能存能量
                if (spaceAvailable > 0) {
                    // 先模拟抽取，确认能充入多少能量
                    int simulated = pBlockEntity.energyStorage.extractEnergy(spaceAvailable, true);

                    if (simulated > 0) {
                        energyPool.receiveEnergy(
                                pBlockEntity.energyStorage.extractEnergy(simulated, false),
                                false);

                        poolModified = true;
                    }
                }
            }
            else {
                pBlockEntity.queryNetWorkId();
                break;
            }
        }

        if (poolModified) {
            energyManager.setDirty();
        }

        return true;
    }
}
