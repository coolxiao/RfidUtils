package com.kingyun.som.rfid.zf

/**
 * @Params:一个串口数据回调接口
 */
interface SerialCallBack {
  fun onSerialPortData(sb: StringBuffer)
}