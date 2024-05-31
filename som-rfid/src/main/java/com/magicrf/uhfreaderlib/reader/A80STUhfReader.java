//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.magicrf.uhfreaderlib.reader;

import com.magicrf.uhfreaderlib.readerInterface.CommendManager;
import com.rodinbell.uhf.serialport.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class A80STUhfReader implements CommendManager {
    private static NewSendCommendManager manager;
    private static SerialPort serialPort;
    private static String port = "/dev/ttySWK0";
    private static int baudRate = 115200;
    private static InputStream in;
    private static OutputStream os;
    private static A80STUhfReader reader;

    public A80STUhfReader() {
    }

    public static A80STUhfReader getInstance() {
        if (serialPort == null) {
            try {
                serialPort = new SerialPort(new File(port), baudRate, 0);
            } catch (Exception var1) {
                return null;
            }

            in = serialPort.getInputStream();
            os = serialPort.getOutputStream();
        }

        if (manager == null) {
            manager = new NewSendCommendManager(in, os);
        }

        if (reader == null) {
            reader = new A80STUhfReader();
        }

        return reader;
    }

    public static void setPortPath(String portPath) {
        port = portPath;
    }

    public String getPortPath() {
        return port;
    }

    public boolean setBaudrate(int baudrate) {
        return manager.setBaudrate(baudrate);
    }

    public byte[] getFirmware() {
        return manager.getFirmware();
    }

    public byte[] getHardwareVersion() {
        return manager.getHardwareVersion();
    }

    public byte[] getManufacturer() {
        return manager.getManufacturer();
    }

    public boolean setOutputPower(int value) {
        return manager.setOutputPower(value);
    }

    public List<byte[]> inventoryRealTime() {
        return manager.inventoryRealTime();
    }

    public List<Integer> getRssiList() {
        return manager.getRssiList();
    }

    public void setSelectMode(byte mode) {
        manager.setSelectMode(mode);
    }

    public void selectEpc(byte[] epc) {
        manager.selectEpc(epc);
    }

    public void setSelectPara(byte target, byte action, byte memBank, int pointer, byte maskLen, boolean truncated, byte[] mask) {
        manager.setSelectPara(target, action, memBank, pointer, maskLen, truncated, mask);
    }

    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword) {
        return manager.readFrom6C(memBank, startAddr, length, accessPassword);
    }

    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data) {
        return manager.writeTo6C(password, memBank, startAddr, dataLen, data);
    }

    public boolean setSensitivity(int value) {
        return manager.setSensitivity(value);
    }

    public boolean lock6C(byte[] password, int memBank, int lockType) {
        return manager.lock6C(password, memBank, lockType);
    }

    public boolean lock6CwithPayload(byte[] password, int payload) {
        return manager.lock6CwithPayload(password, payload);
    }

    public boolean kill6C(byte[] password) {
        return manager.kill6C(password);
    }

    public boolean kill6C(byte[] password, int rfu) {
        return manager.kill6C(password, rfu);
    }

    public void close() {
        if (manager != null) {
            manager.close();
            manager = null;
        }

        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }

        if (reader != null) {
            reader = null;
        }

    }

    public byte checkSum(byte[] paramArrayOfByte) {
        byte checksum = 0;
        byte[] var6 = paramArrayOfByte;
        int var5 = paramArrayOfByte.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            byte b = var6[var4];
            checksum += b;
        }

        return checksum;
    }

    public int setFrequency(int startFrequency, int freqSpace, int freqQuality) {
        return manager.setFrequency(startFrequency, freqSpace, freqQuality);
    }

    public boolean setFrequency(int index) {
        return manager.setFrequency(index);
    }

    public boolean setFHSS(boolean on) {
        return manager.setFHSS(on);
    }

    public void setDistance(int distance) {
    }

    public void close(InputStream input, OutputStream output) {
        if (manager != null) {
            manager = null;

            try {
                input.close();
                output.close();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

    }

    public boolean setWorkArea(int area) {
        return manager.setWorkArea(area);
    }

    public List<byte[]> inventoryMulti() {
        return manager.inventoryMulti();
    }

    public void stopInventoryMulti() {
        manager.stopInventoryMulti();
    }

    public int getFrequency() {
        return manager.getFrequency();
    }

    public boolean unSelect() {
        return manager.unSelect();
    }

    public void setRecvParam(int mixer_g, int if_g, int trd) {
        manager.setModemParam(mixer_g, if_g, trd);
    }

    public boolean setModemParam(int mixer_g, int if_g, int trd) {
        return manager.setModemParam(mixer_g, if_g, trd);
    }

    public byte[] getModemParam() {
        return manager.getModemParam();
    }
}
