package com.wangzi.coi.entity;

import com.wangzi.coi.blockentity.GeneralTelegraphPoleBlockEntity;
import com.wangzi.coi.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ElectricWireEntity extends Entity {
    // ================= 数据定义 =================
    // 注册起点的方块坐标同步字段
    private static final EntityDataAccessor<BlockPos> START_POS = SynchedEntityData.defineId(ElectricWireEntity.class, EntityDataSerializers.BLOCK_POS);

    // 注册终点的方块坐标同步字段
    private static final EntityDataAccessor<BlockPos> END_POS = SynchedEntityData.defineId(ElectricWireEntity.class, EntityDataSerializers.BLOCK_POS);

    // ================= 构造器 =================
    // 主构造函数，注册时调用
    public ElectricWireEntity(EntityType<? extends ElectricWireEntity> type, Level level) {
        super(type, level);

        // 不参与物理碰撞
        this.noPhysics = true;

        // 不受重力影响
        this.setNoGravity(true);
    }

    // 便携构造函数，业务代码调用
    public ElectricWireEntity(Level level) {
        this(ModEntities.ELECTRIC_WIRE.get(), level);
    }

    // ================= 数据初始化与同步 =================
    // 注册数据同步表
    @Override
    protected void defineSynchedData() {
        // 注册所有需要同步到客户端的数据，并赋予默认值
        this.entityData.define(START_POS, BlockPos.ZERO);
        this.entityData.define(END_POS, BlockPos.ZERO);
    }

    // 实体位置初始化入口
    public void setup(BlockPos start, BlockPos end) {
        // 设置起止点坐标
        this.entityData.set(START_POS, start);
        this.entityData.set(END_POS, end);

        // 将实体位置设为起点电线杆
        this.setPos(start.getX() + 0.5, start.getY(), start.getZ() + 0.5);
    }

    // ==================== 数据读取 ====================
    public BlockPos getStartPos() { return this.entityData.get(START_POS); }
    public BlockPos getEndPos()   { return this.entityData.get(END_POS); }

    // ================= 存档与网络包 =================
    // 将实体状态序列化到 NBT（区块保存时调用）
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("StartPos", this.entityData.get(START_POS).asLong());
        tag.putLong("EndPos", this.entityData.get(END_POS).asLong());
    }

    // 从 NBT 还原实体状态（区块加载时调用）
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("StartPos")) this.entityData.set(START_POS, BlockPos.of(tag.getLong("StartPos")));
        if (tag.contains("EndPos")) this.entityData.set(END_POS, BlockPos.of(tag.getLong("EndPos")));
    }

    // ================= Tick周期 =================
    @Override
    public void tick() {
        super.tick();

        // 仅服务端使用
        if (!this.level().isClientSide) {
            // 判断起点是否还是电线杆
            boolean startValid = this.level().getBlockEntity(this.getStartPos()) instanceof GeneralTelegraphPoleBlockEntity;

            // 判断终点是否还是电线杆
            boolean endValid = this.level().getBlockEntity(this.getEndPos()) instanceof GeneralTelegraphPoleBlockEntity;

            // 如果不是则销毁该实体
            if (!startValid || !endValid) {
                this.discard();
            }
        }
    }

    // 重写为空方法，阻止被 kill 指令清除
    @Override
    public void kill() {}
}