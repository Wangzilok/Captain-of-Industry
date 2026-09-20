package com.wangzi.coi.blockentity;

import com.wangzi.coi.Config.ModBlockEntityConfig;
import com.wangzi.coi.block.GeneralExtendedBlock;
import com.wangzi.coi.data.NetworkEnergyManager;
import com.wangzi.coi.data.NetworkIdData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GeneralTelegraphPoleBlockEntity extends GeneralBlockEntity{
    // 连接的目标位置
    private final List<BlockPos> connectedTo = new ArrayList<>();

    // 所有顶点方块实体（客户端）
    private static final Set<GeneralTelegraphPoleBlockEntity> TRACKED_POLES = ConcurrentHashMap.newKeySet();

    // 中心到端子的距离
    protected final float terminalR;

    // 端子 Y 轴偏移
    protected final float terminalY;

    // 连接半径
    protected final int connectedR;

    // 连接高度
    protected final int connectedH;
    {terminalR = 0; terminalY = 0; connectedR = 0; connectedH = 0;}

    // 网络 ID
    private int netWorkId = -1;

    // 构造函数
    public GeneralTelegraphPoleBlockEntity(BlockEntityType pBlockEntityType, BlockPos pPos, BlockState pBlockState, ModBlockEntityConfig pModBlockEntityConfig) {
        super(pBlockEntityType, pPos, pBlockState, pModBlockEntityConfig);
    }

    // 当玩家右键点击方块时，服务端调用此方法来生成 GUI 容器
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return null;
    }

    // ==================== 持久化 ====================
    // 保存数据
    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);

        // 保存连接对象
        long[] positions = connectedTo.stream().mapToLong(BlockPos::asLong).toArray();
        pTag.putLongArray("connected_to", positions);

        // 保存网络 ID
        pTag.putInt("network_id", netWorkId);
    }

    // 读取数据
    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);

        // 读取连接对象
        connectedTo.clear();
        if (pTag.contains("connected_to", Tag.TAG_LONG_ARRAY)) {
            for (long pos : pTag.getLongArray("connected_to")) {
                connectedTo.add(BlockPos.of(pos));
            }
        }

        // 读取网络 ID
        this.netWorkId = pTag.getInt("network_id");

        refreshTracking();
    }

    // ==================== 客户端同步 ====================
    // 生成发送给客户端的数据包
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // 接收服务端数据包时调用
    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(connection, packet);

        refreshTracking();
    }

    // 生成客户端初始同步标签
    @Override
    @NotNull
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putLongArray("connected_to", connectedTo.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("network_id", netWorkId);
        return tag;
    }

    // 客户端接收同步数据
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        connectedTo.clear();
        if (tag.contains("connected_to", Tag.TAG_LONG_ARRAY)) {
            for (long pos : tag.getLongArray("connected_to")) {
                connectedTo.add(BlockPos.of(pos));
            }
        }

        this.netWorkId = tag.getInt("network_id");
    }

    // ==================== 连接管理 ====================
    // 添加连接目标
    public boolean addConnection(BlockPos pos) {
        if (!connectedTo.contains(pos)) {
            connectedTo.add(pos);

            // 初始化网络 ID
            if (level != null && !level.isClientSide && this.netWorkId == -1) {
                ServerLevel serverLevel = (ServerLevel) level;
                this.netWorkId = NetworkIdData.get(serverLevel).allocate();
            }

            setChanged();
            // 触发客户端同步
            if (this.netWorkId != -1 && level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            return true;
        }
        return false;
    }

    // 移除连接
    public void removeConnection(BlockPos pos) {
        if (connectedTo.remove(pos)) {
            setChanged();
            // 触发客户端同步
            if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 获取所有连接
    public List<BlockPos> getConnectedTo() {
        return connectedTo;
    }

    // ==================== 数据读取和修改 ====================
    public static Set<GeneralTelegraphPoleBlockEntity> getTrackedPoles() {
        return new HashSet<>(TRACKED_POLES);
    }

    // 获取网络 ID
    public int getNetWorkId() {return this.netWorkId;}
    // 修改网络 ID
    public void setNetWorkId(int netWorkId, boolean firstNode) {
        // 如果 ID 相同则无需更改
        if (this.netWorkId == netWorkId) return;

        // 重新创建能量池
        if (firstNode && level != null && !level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            NetworkEnergyManager.get(serverLevel).getPool(netWorkId, 100);
            NetworkEnergyManager.get(serverLevel).remove(this.netWorkId);
        }

        // 修改网络 ID
        this.netWorkId = netWorkId;

        setChanged();
        // 触发客户端同步
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        // 遍历修改所有连接对象的 ID
        if(level != null && !level.isClientSide && !connectedTo.isEmpty()) {
            for (BlockPos pos : connectedTo) {
                if (level.getBlockEntity(pos) instanceof GeneralTelegraphPoleBlockEntity connectedPole) {
                    connectedPole.setNetWorkId(netWorkId, false);
                }
            }
        }
    }

    abstract public float getTerminalR();
    abstract public float getTerminalY();
    abstract public int getConnectedR();
    abstract public int getConnectedH();

    // ==================== 生命周期 ====================
    // 在 BlockEntity 被添加到 Level 之后，在任何 tick 或渲染发生之前调用
    @Override
    public void onLoad() {
        super.onLoad();

        if (level != null && !level.isClientSide && this.netWorkId == -1 && getBlockState().getValue(GeneralExtendedBlock.getHeightIndex()) == 3) {
            this.netWorkId = NetworkIdData.get((ServerLevel) level).allocate();
            setChanged();
        }
    }

    // 进入世界时（覆盖存档加载 + 首次放置）
    @Override
    public void setLevel(@NotNull Level level) {
        super.setLevel(level);

        refreshTracking();
    }

    // 离开世界时（覆盖破坏 + 区块卸载）
    @Override
    public void setRemoved() {
        super.setRemoved();
        TRACKED_POLES.remove(this);

        if (level == null) return;

        for (BlockPos pos : connectedTo) {
            // 检查加载状态
            if (!level.isLoaded(pos)) continue;

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GeneralTelegraphPoleBlockEntity targetPole) {
                targetPole.removeConnection(worldPosition);

                // 断开时重新分配 ID
                if (!level.isClientSide) {
                    if (targetPole.getNetWorkId() == this.netWorkId) {
                        ServerLevel serverLevel = (ServerLevel) level;
                        targetPole.setNetWorkId(NetworkIdData.get(serverLevel).allocate(), true);
                        NetworkEnergyManager.get(serverLevel).remove(this.netWorkId);
                    }
                }
            }
        }
    }

    // 刷新追踪状态
    private void refreshTracking() {
        if (level == null) return;

        boolean isTop = getBlockState().getValue(GeneralExtendedBlock.getHeightIndex()) == 3;

        if (isTop) {
            TRACKED_POLES.add(this);
        } else {
            TRACKED_POLES.remove(this);
        }
    }
}
