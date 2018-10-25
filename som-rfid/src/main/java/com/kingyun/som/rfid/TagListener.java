package com.kingyun.som.rfid;

/**
 * 用于接受RFID扫描后的数据
 * Created by houfukude on 16-2-18.
 */
public interface TagListener {

    /**
     * 扫描到RFID后的回调
     *
     * @param rfid RFID tag字符串
     * @param info 额外的信息<br/>
     *             对于 G71EX设备是载波频率和RSSI（单位dBm）<br/>
     *             对于 RMPC-M01 则是当前时间戳
     */
    void onSuccess(String rfid, String info);

    /**
     * 通过广播发布扫描过程中的LOG
     *
     * @param log    log字符串
     * @param action 来自 {@link ReaderHelper}中的 BROADCAST_* 类行的标识
     */
    void onLogReceive(String log, String action);

}
