package com.kingyun.som.rfid.zf;

import android.util.Log;
import android_serialport_api_cw.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2018/5/31.
 */

public class SerialPortUtil {

    public static String TAG = "SerialPortUtil";

    /**
     * 标记当前串口状态(true:打开,false:关闭)
     **/
    public static boolean isFlagSerial = false;

    public static SerialPort serialPort = null;
    public static InputStream inputStream = null;
    public static OutputStream outputStream = null;
    public static Thread receiveThread = null;
    public static String strData = "";
    public static StringBuffer sb = new StringBuffer();
    /**
     * 打开串口
     */
    public static boolean open(String pathname, int baudrate, int flags) {
        boolean isopen = false;
        if (isFlagSerial) {
            return false;
        }
        try {
            serialPort = new SerialPort(new File(pathname), baudrate, flags);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            receive();
            isopen = true;
            isFlagSerial = true;
        } catch (Exception e) {
            e.printStackTrace();
            isopen = false;
        }
        return isopen;
    }

    public static  boolean closeCom(){
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            Log.e("chunlei", "closeCom closeCom");
        } catch (IOException e) {
            Log.e("chunlei", "closeCom IOException IOException");
            e.printStackTrace();
        }
        isFlagSerial = false;
        if(receiveThread!=null){
            receiveThread.interrupt();
         
        }
		   receiveThread = null;
        if(serialPort!=null){
            serialPort.close();
        }
        return  true;
    }
    /**
     * 关闭串口
     */
  /*  public static boolean close() {
        if (isFlagSerial) {
            return false;
        }
        boolean isClose = false;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            isClose = true;
            isFlagSerial = false;//关闭串口时，连接状态标记为false
        } catch (IOException e) {
            e.printStackTrace();
            isClose = false;
        }
        return isClose;
    }
*/


    /**
     * 发送串口指令
     */
    public static void sendString(String data) {
        if (!isFlagSerial) {
            return;
        }
        try {
            outputStream.write(ByteUtil.hex2byte(data));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收串口数据的方法
     */
    public static void receive() {
        if (receiveThread != null && !isFlagSerial) {
            return;
        }
        receiveThread = new Thread() {
            @Override
            public void run() {
                //     Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                while (isFlagSerial) {
                    try {
                        byte[] readData = new byte[512];
                        if (inputStream == null) {
                            return;
                        }
                        int size = inputStream.read(readData);
                        if (size > 0 && isFlagSerial) {
                            strData = ByteUtil.byteToStr(readData, size);//实际上已经处理了数据 00的数据已经被去掉
                            sb.append(strData);
                            if(strData.endsWith("7E")){
                                SerialPortCallBackUtils.doCallBackMethod(/*ByteUtil.hex2byte(msg)*/sb);
                                sb.setLength(0);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
        //  Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        receiveThread.setPriority(10);
        receiveThread.start();
    }
}