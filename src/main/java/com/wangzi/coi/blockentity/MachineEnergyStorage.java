package com.wangzi.coi.blockentity;

import net.minecraftforge.energy.EnergyStorage;

public class MachineEnergyStorage extends EnergyStorage {

    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    // 供机器内部加工逻辑调用的能量消耗方法
    public boolean consumeEnergy(int amount, boolean simulate) {
        if (this.energy >= amount) {
            if (!simulate) this.energy -= amount;
            return true;
        }
        return false;
    }
}