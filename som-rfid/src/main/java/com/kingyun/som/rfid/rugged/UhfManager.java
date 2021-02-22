package com.kingyun.som.rfid.rugged;

import com.rodinbell.uhf.serialport.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class UhfManager {
    public static int RESERVE = 0;//保留区（密码区）
    public static int EPC = 1;
    public static int TID = 2;
    public static int USER = 3;
    public static int WorkArea_China2 = 1;
    public static int WorkArea_USA = 2;//（默认 美标）
    public static int WorkArea_Europe = 3;//欧标
    public static int WorkArea_China1 = 4;//
    public static int WorkArea_Korea = 6;//
    public static final int SENSITIVE_HIHG = 3;
    public static final int SENSITIVE_MIDDLE = 2;
    public static final int SENSITIVE_LOW = 1;
    public static final int SENSITIVE_VERY_LOW = 0;
    private static CommendManager manager;
    private static SerialPort serialPort;//串口类
    public static String Port = "/dev/ttysWK2";//串口驱动
    public static int BaudRate = 115200;//波特率
    private static InputStream is;
    private static OutputStream os;
    private static UhfManager uhfManager;

//    public UhfManager() {
//    }
    //
    public static UhfManager getInstance() {
        try {
            serialPort = new SerialPort(new File(Port), BaudRate, 0);
        } catch (SecurityException var1) {
            var1.printStackTrace();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

        is = serialPort.getInputStream();
        os = serialPort.getOutputStream();
        manager = new CommendManager(is, os);
        if (uhfManager == null) {
            uhfManager = new UhfManager();
        }
        return uhfManager;
    }

    //获取硬件版本
    public byte[] getFirmware() {
        return manager.getFirmware();
    }
    //设置功率 16 - 26
    public boolean setOutputPower(int value) {
        return manager.setOutputPower(value);
    }
    //实时盘epc
    public List<byte[]> inventoryRealTime() {
        return manager.inventoryRealTime();
    }
    //选择标签 （epc）
    public void selectEPC(byte[] epc) {
        manager.selectEPC(epc);
    }
    //读取标签数据,参数 内存区域 RESERVE = 0; EPC = 1; TID = 2;USER = 3; 、起始地址、长度、认证密码;
    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword) {
        return manager.readFrom6C(memBank, startAddr, length, accessPassword);
    }
    //写标签，参数：密码、区域、起始地址、数据长度、数据
    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data) {
        return manager.writeTo6C(password, memBank, startAddr, dataLen, data);
    }

    public void setSensitivity(int value) {
        manager.setSensitivity(value);
    }
    //锁标签，用法:修改密码区密码，用新密码锁定密码区，方可用新密码锁定其他区域
    public boolean lock6C(byte[] password, int memBank, int lockType) {
        return manager.lock6C(password, memBank, lockType);
    }
    //关闭串口 既 uhf读写器
    public void close() {
        if (manager != null) {
            manager.close();
            manager = null;
        }

        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }

        if (uhfManager != null) {
            uhfManager = null;
        }

    }

    public byte checkSum(byte[] data) {
        return manager.checkSum(data);
    }
    //设置频率
    public int setFrequency(int startFrequency, int freqSpace, int freqQuality) {
        return manager.setFrequency(startFrequency, freqSpace, freqQuality);
    }
    //设置频段区域 参考 2美国 3欧标 1中国
    public int setWorkArea(int area) {
        return manager.setWorkArea(area);
    }

    public List<byte[]> inventoryMulti() {
        return manager.inventoryMulti();
    }

    public void stopInventoryMulti() {
        manager.stopInventoryMulti();
    }
    //获取频率
    public int getFrequency() {
        return manager.getFrequency();
    }

    //取消选择
    public int unSelect() {
        return manager.unSelectEPC();
    }

    public void setRecvParam(int mixer_g, int if_g, int trd) {
        manager.setRecvParam(mixer_g, if_g, trd);
    }
        //灭活标签，一般不用，灭活不可逆
    public boolean kill6C(byte[] killPassword) {
        return manager.kill6C(killPassword);
    }
}
