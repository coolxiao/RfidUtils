//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.kingyun.som.rfid.rugged;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.pda.serialport.Tools;

public class CommendManager implements CmdManager {
    private InputStream in;
    private OutputStream out;
    private final byte HEAD = -69;
    private final byte END = 126;
    public static final byte RESPONSE_OK = 0;
    public static final byte ERROR_CODE_ACCESS_FAIL = 22;
    public static final byte ERROR_CODE_NO_CARD = 9;
    public static final byte ERROR_CODE_READ_SA_OR_LEN_ERROR = -93;
    public static final byte ERROR_CODE_WRITE_SA_OR_LEN_ERROR = -77;
    public static final int SENSITIVE_HIHG = 3;
    public static final int SENSITIVE_MIDDLE = 2;
    public static final int SENSITIVE_LOW = 1;
    public static final int SENSITIVE_VERY_LOW = 0;
    public static final int LOCK_TYPE_OPEN = 0;
    public static final int LOCK_TYPE_PERMA_OPEN = 1;
    public static final int LOCK_TYPE_LOCK = 2;
    public static final int LOCK_TYPE_PERMA_LOCK = 3;
    public static final int LOCK_MEM_KILL = 0;
    public static final int LOCK_MEM_ACCESS = 1;
    public static final int LOCK_MEM_EPC = 2;
    public static final int LOCK_MEM_TID = 3;
    public static final int LOCK_MEM_USER = 4;
    private byte[] selectEPC = null;

    public CommendManager(InputStream serialPortInput, OutputStream serialportOutput) {
        this.in = serialPortInput;
        this.out = serialportOutput;
    }

    private void sendCMD(byte[] cmd) {
        try {
            this.out.write(cmd);
            this.out.flush();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

//    public boolean setBaudrate() {
//        byte[] cmd = new byte[0];
//        return false;
//    }

    public byte[] getFirmware() {
        byte[] cmd = new byte[]{-69, 0, 3, 0, 1, 0, 4, 126};
        byte[] version = null;
        this.sendCMD(cmd);
        byte[] response = this.read();
        if (response != null) {
            byte[] resolve = this.handlerResponse(response);
            if (resolve != null && resolve.length > 1) {
                version = new byte[resolve.length - 1];
                System.arraycopy(resolve, 1, version, 0, resolve.length - 1);
            }
        }

        return version;
    }

    public void setSensitivity(int value) {
        byte[] cmd = new byte[]{-69, 0, -16, 0, 4, 2, 6, 0, -96, -100, 126};
        cmd[5] = (byte)value;
        cmd[cmd.length - 2] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] response = this.read();
    }

    private byte[] read() {
        byte[] response = null;
        int available = 0;
        int index = 0;
        int headIndex = 0;

        try {
            while(index < 10) {
                Thread.sleep(50L);
                available = this.in.available();
                if (available > 7) {
                    break;
                }

                ++index;
            }

            if (available > 0) {
                byte[] responseData = new byte[available];
                this.in.read(responseData);

                for(int i = 0; i < available; ++i) {
                    if (responseData[i] == -69) {
                        headIndex = i;
                        break;
                    }
                }

                response = new byte[available - headIndex];
                System.arraycopy(responseData, headIndex, response, 0, response.length);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return response;
    }

    public boolean setOutputPower(int value) {
        byte mixer = 1;
        byte if_g = 3;
        short trd = 432;
        switch(value) {
        case 16:
            mixer = 1;
            if_g = 1;
            trd = 432;
            break;
        case 17:
            mixer = 1;
            if_g = 3;
            trd = 432;
            break;
        case 18:
            mixer = 2;
            if_g = 4;
            trd = 432;
            break;
        case 19:
            mixer = 2;
            if_g = 5;
            trd = 496;
        case 20:
            mixer = 2;
            if_g = 6;
            trd = 496;
        case 21:
            mixer = 2;
            if_g = 6;
            trd = 560;
            break;
        case 22:
            mixer = 3;
            if_g = 6;
            trd = 624;
            break;
        case 23:
            mixer = 4;
            if_g = 6;
            trd = 624;
            break;
        default:
            mixer = 6;
            if_g = 7;
            trd = 624;
        }

        return this.setRecvParam(mixer, if_g, trd);
    }

    public boolean setRecvParam(int mixer_g, int if_g, int trd) {
        byte[] cmd = new byte[]{-69, 0, -16, 0, 4, 3, 6, 1, -80, -82, 126};
        cmd[5] = (byte)mixer_g;
        cmd[6] = (byte)if_g;
        cmd[7] = (byte)(trd / 256);
        cmd[8] = (byte)(trd % 256);
        cmd[9] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if (recv != null) {
            byte[] content = this.handlerResponse(recv);
            if (content != null) {
                return true;
            }
        }

        return false;
    }

    public List<byte[]> inventoryMulti() {
        this.unSelectEPC();
        List<byte[]> list = new ArrayList();
        byte[] cmd = new byte[]{-69, 0, 39, 0, 3, 34, 39, 16, -125, 126};
        this.sendCMD(cmd);
        byte[] response = this.read();
        if (response != null) {
            int responseLength = response.length;
            int start = 0;
            if (responseLength > 15) {
                while(responseLength > 5) {
                    int paraLen = response[start + 4] & 255;
                    int singleCardLen = paraLen + 7;
                    if (singleCardLen > responseLength) {
                        break;
                    }

                    byte[] sigleCard = new byte[singleCardLen];
                    System.arraycopy(response, start, sigleCard, 0, singleCardLen);
                    byte[] resolve = this.handlerResponse(sigleCard);
                    if (resolve != null && paraLen > 5) {
                        byte[] epcBytes = new byte[paraLen - 5];
                        System.arraycopy(resolve, 4, epcBytes, 0, paraLen - 5);
                        list.add(epcBytes);
                    }

                    start += singleCardLen;
                    responseLength -= singleCardLen;
                }
            } else {
                this.handlerResponse(response);
            }
        }

        return list;
    }

    public void stopInventoryMulti() {
        byte[] cmd = new byte[]{-69, 0, 40, 0, 0, 40, 126};
        this.sendCMD(cmd);
        byte[] recv = this.read();
    }

    public List<byte[]> inventoryRealTime() {
        this.unSelectEPC();
        byte[] cmd = new byte[]{-69, 0, 34, 0, 0, 34, 126};
        this.sendCMD(cmd);
        List<byte[]> list = new ArrayList();
        byte[] response = this.read();
        if (response != null) {
            int responseLength = response.length;
            int start = 0;
            if (responseLength > 6) {
                while(responseLength > 5) {
                    int paraLen = response[start + 4] & 255;
                    int singleCardLen = paraLen + 7;
                    if (singleCardLen > responseLength) {
                        break;
                    }

                    byte[] sigleCard = new byte[singleCardLen];
                    System.arraycopy(response, start, sigleCard, 0, singleCardLen);
                    byte[] resolve = this.handlerResponse(sigleCard);
                    if (resolve != null && paraLen > 5) {
                        byte[] epcBytes = new byte[paraLen - 5];
                        System.arraycopy(resolve, 4, epcBytes, 0, paraLen - 5);
                        list.add(epcBytes);
                    }

                    start += singleCardLen;
                    responseLength -= singleCardLen;
                }
            } else {
                this.handlerResponse(response);
            }
        }

        return list;
    }

    public void selectEPC(byte[] epc) {
        byte[] cmd = new byte[]{-69, 0, 18, 0, 1, 0, 19, 126};
        this.selectEPC = epc;
        this.sendCMD(cmd);
        byte[] response = this.read();
    }

    public int unSelectEPC() {
        byte[] cmd = new byte[]{-69, 0, 18, 0, 1, 1, 20, 126};
        this.sendCMD(cmd);
        byte[] response = this.read();
        return 0;
    }

    private void setSelectPara() {
        if (this.selectEPC != null) {
            byte[] para = new byte[]{1, 0, 0, 0, 32, (byte)(this.selectEPC.length * 8), 0};
            byte[] cmd = new byte[14 + this.selectEPC.length];
            cmd[0] = -69;
            cmd[1] = 0;
            cmd[2] = 12;
            cmd[3] = 0;
            cmd[4] = (byte)(7 + this.selectEPC.length);
            System.arraycopy(para, 0, cmd, 5, para.length);
            System.arraycopy(this.selectEPC, 0, cmd, 12, this.selectEPC.length);
            cmd[cmd.length - 2] = this.checkSum(cmd);
            cmd[cmd.length - 1] = 126;
            this.sendCMD(cmd);
            byte[] var3 = this.read();
        }

    }

    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword) {
        this.setSelectPara();
        byte[] cmd = new byte[]{-69, 0, 57, 0, 9, 0, 0, 0, 0, 3, 0, 0, 0, 8, 77, 126};
        byte[] data = null;
        if (accessPassword != null && accessPassword.length == 4) {
            System.arraycopy(accessPassword, 0, cmd, 5, 4);
            cmd[9] = (byte)memBank;
            int lengH;
            int lengL;
            if (startAddr <= 255) {
                cmd[10] = 0;
                cmd[11] = (byte)startAddr;
            } else {
                lengH = startAddr / 256;
                lengL = startAddr % 256;
                cmd[10] = (byte)lengH;
                cmd[11] = (byte)lengL;
            }

            if (length <= 255) {
                cmd[12] = 0;
                cmd[13] = (byte)length;
            } else {
                lengH = length / 256;
                lengL = length % 256;
                cmd[12] = (byte)lengH;
                cmd[13] = (byte)lengL;
            }

            cmd[14] = this.checkSum(cmd);
            this.sendCMD(cmd);
            byte[] response = this.read();
            if (response != null) {
                byte[] resolve = this.handlerResponse(response);
                if (resolve != null) {
                    if (resolve[0] == 57) {
                        int lengData = resolve.length - resolve[1] - 2;
                        data = new byte[lengData];
                        System.arraycopy(resolve, resolve[1] + 2, data, 0, lengData);
                    } else {
                        data = new byte[]{resolve[1]};
                    }
                }
            }

            return data;
        } else {
            return null;
        }
    }

    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data) {
        this.setSelectPara();
        if (password != null && password.length == 4) {
            boolean writeFlag = false;
            int cmdLen = 16 + data.length;
            int parameterLen = 9 + data.length;
            byte[] cmd = new byte[cmdLen];
            cmd[0] = -69;
            cmd[1] = 0;
            cmd[2] = 73;
            int dataLenH;
            int dataLenL;
            if (parameterLen < 256) {
                cmd[3] = 0;
                cmd[4] = (byte)parameterLen;
            } else {
                dataLenH = parameterLen / 256;
                dataLenL = parameterLen % 256;
                cmd[3] = (byte)dataLenH;
                cmd[4] = (byte)dataLenL;
            }

            System.arraycopy(password, 0, cmd, 5, 4);
            cmd[9] = (byte)memBank;
            if (startAddr < 256) {
                cmd[10] = 0;
                cmd[11] = (byte)startAddr;
            } else {
                dataLenH = startAddr / 256;
                dataLenL = startAddr % 256;
                cmd[10] = (byte)dataLenH;
                cmd[11] = (byte)dataLenL;
            }

            if (dataLen < 256) {
                cmd[12] = 0;
                cmd[13] = (byte)dataLen;
            } else {
                dataLenH = dataLen / 256;
                dataLenL = dataLen % 256;
                cmd[12] = (byte)dataLenH;
                cmd[13] = (byte)dataLenL;
            }

            System.arraycopy(data, 0, cmd, 14, data.length);
            cmd[cmdLen - 2] = this.checkSum(cmd);
            cmd[cmdLen - 1] = 126;
            this.sendCMD(cmd);

            try {
                Thread.sleep(50L);
            } catch (InterruptedException var12) {
                var12.printStackTrace();
            }

            byte[] response = this.read();
            if (response != null) {
                byte[] resolve = this.handlerResponse(response);
                if (resolve != null && resolve[0] == 73 && resolve[resolve.length - 1] == 0) {
                    writeFlag = true;
                }
            }

            return writeFlag;
        } else {
            return false;
        }
    }

    public boolean lock6C(byte[] password, int memBank, int lockType) {
        this.setSelectPara();
        byte[] cmd = new byte[]{-69, 0, -126, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 126};
        int lockPay = 0;
        byte[] lockPara = new byte[3];
        if (lockType == 0) {
            lockPay = 1 << 19 - 2 * memBank;
        }

        if (lockType == 1) {
            lockPay = (1 << 19 - 2 * memBank) + (1 << 19 - 2 * memBank - 1) + (1 << 8 - 2 * memBank);
        }

        if (lockType == 2) {
            if (memBank == 1) {
                lockPay = 131200;
            } else {
                lockPay = (1 << 19 - 2 * memBank) + 131072 + (1 << 9 - 2 * memBank) + 128;
            }
        }

        if (lockType == 3) {
            lockPay = (1 << 19 - 2 * memBank) + (1 << 19 - 2 * memBank - 1) + (1 << 9 - 2 * memBank) + (1 << 8 - 2 * memBank);
        }

        lockPara = Tools.intToByte(lockPay);
        System.arraycopy(password, 0, cmd, 5, password.length);
        System.arraycopy(lockPara, 0, cmd, 9, lockPara.length);
        cmd[cmd.length - 2] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if (recv != null) {
            byte[] resolve = this.handlerResponse(recv);
            if (resolve != null && resolve[0] == -126 && resolve[resolve.length - 1] == 0) {
                return true;
            }
        }

        return false;
    }

    public boolean kill6C(byte[] password) {
        boolean killFlag = false;
        byte[] cmd = new byte[11];
        cmd[0] = -69;
        cmd[1] = 0;
        cmd[2] = 101;
        cmd[3] = 0;
        cmd[4] = 4;
        System.arraycopy(password, 0, cmd, 5, 4);
        cmd[9] = this.checkSum(cmd);
        cmd[10] = 126;
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if (recv != null) {
            if (recv.length == 6) {
                return killFlag;
            }

            byte[] resolve = this.handlerResponse(recv);
            if (resolve[resolve.length - 1] == 0) {
                killFlag = true;
            }
        }

        return killFlag;
    }

    public void close() {
        try {
            this.in.close();
            this.out.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public byte checkSum(byte[] data) {
        byte crc = 0;

        for(int i = 1; i < data.length - 2; ++i) {
            crc += data[i];
        }

        return crc;
    }

    private byte[] handlerResponse(byte[] response) {
        byte[] data = null;
        int responseLength = response.length;
        if (response[0] != -69) {
            return data;
        } else if (response[responseLength - 1] != 126) {
            return data;
        } else if (responseLength < 7) {
            return data;
        } else {
            int lengthHigh = response[3] & 255;
            int lengthLow = response[4] & 255;
            int dataLength = lengthHigh * 256 + lengthLow;
            byte crc = this.checkSum(response);
            if (crc != response[responseLength - 2]) {
                return data;
            } else {
                if (dataLength != 0 && responseLength == dataLength + 7) {
                    data = new byte[dataLength + 1];
                    data[0] = response[2];
                    System.arraycopy(response, 5, data, 1, dataLength);
                }

                return data;
            }
        }
    }

    public int setFrequency(int startFrequency, int freqSpace, int freqQuality) {
        int frequency = 1;
        if (startFrequency > 840125 && startFrequency < 844875) {
            frequency = (startFrequency - 840125) / 250;
        } else if (startFrequency > 920125 && startFrequency < 924875) {
            frequency = (startFrequency - 920125) / 250;
        } else if (startFrequency > 865100 && startFrequency < 867900) {
            frequency = (startFrequency - 865100) / 200;
        } else if (startFrequency > 902250 && startFrequency < 927750) {
            frequency = (startFrequency - 902250) / 500;
        }

        byte[] cmd = new byte[]{-69, 0, -85, 0, 1, 4, -80, 126};
        cmd[5] = (byte)frequency;
        cmd[6] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] recv = this.read();
        return 0;
    }

    public int setWorkArea(int area) {
        byte[] cmd = new byte[]{-69, 0, 7, 0, 1, 1, 9, 126};
        cmd[5] = (byte)area;
        cmd[6] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if (recv != null) {
            this.handlerResponse(recv);
        }

        return 0;
    }

    public int getFrequency() {
        byte[] cmd = new byte[]{-69, 0, -86, 0, 0, -86, 126};
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if (recv != null) {
            this.handlerResponse(recv);
        }

        return 0;
    }
}
