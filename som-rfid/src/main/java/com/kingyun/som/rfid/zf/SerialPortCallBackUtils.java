package com.kingyun.som.rfid.zf;

public class SerialPortCallBackUtils {

    private static SerialCallBack mCallBack;

    public static void setCallBack(SerialCallBack callBack) {
        mCallBack = callBack;
    }

    public static void doCallBackMethod(/*byte[] info*/StringBuffer sb){
        mCallBack.onSerialPortData(sb);
    }

}