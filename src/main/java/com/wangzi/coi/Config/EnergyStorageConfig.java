package com.wangzi.coi.Config;

public record EnergyStorageConfig (    // 最大容量
                                       int capacity,
                                       // 每次最大接收量
                                       int maxReceive,
                                       // 每次最大提取量
                                       int maxExtract,
                                       // 初始能量
                                       int energy) {}
