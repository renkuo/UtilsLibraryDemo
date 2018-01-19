package com.renkuo.personal.utilslibrary.deviceutils.model;

import java.util.List;

/**
 * Cpu模型
 */
public class CpuInfo {
    //cpu 型号
    private String mode;
    //cpu核数
    private int corenum;
    //最大主频\\单位kHz
    private int maxfreq;
    //最小主频\\单位kHz
    private int minfreq;
    //cpu温度
    private int temperature;
    //cpu位数
    private String bits;
    //cpu利用率
    private float useage = 0f;
    //cpu平均利用率
    private float avguseage = 0;
    //cpu单核基本信息
    private List<CpuCoreInfo> data;

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setCorenum(int corenum) {
        this.corenum = corenum;
    }

    public int getCorenum() {
        return corenum;
    }

    public void setMaxfreq(int maxfreq) {
        this.maxfreq = maxfreq;
    }

    public void setMinfreq(int minfreq) {
        this.minfreq = minfreq;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setBits(String bits) {
        this.bits = bits;
    }

    public void setUseage(float useage) {
        this.useage = useage;
    }

    public float getUseage() {
        return useage;
    }

    public <T> void setData(List<T> data) {
        this.data = (List<CpuCoreInfo>) data;
    }

    /**
     * Cpu单核基本信息
     */
    public static class CpuCoreInfo {
        //cpu单核名称
        private String name;
        //单核当前主频\\单位kHz,为0时表示当前cpu处于休眠状态
        private int curfreq;
        //最大主频\\单位kHz
        private int maxfreq;
        //最小主频\\单位kHz
        private int minfreq;

        public CpuCoreInfo(String name, int maxfreq, int minfreq, int curfreq) {
            this.name = name;
            this.maxfreq = maxfreq;
            this.minfreq = minfreq;
            this.curfreq = curfreq;
        }
    }
}
