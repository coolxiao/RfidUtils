package com.kingyun.som.rfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.kingyun.som.rfid.base.ReaderBase;
import com.kingyun.som.rfid.helper.InventoryBuffer;
import com.kingyun.som.rfid.helper.ReaderSetting;
import com.rodinbell.uhf.serialport.SerialPort;
import com.rodinbell.uhf.serialport.SerialPortFinder;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by houfukude on 16-2-15.
 */
public class RFIDUtils {
    private static SerialPort mSerialPort;
    private static ReaderHelper mReaderHelper;
    private static InventoryBuffer mCurInventoryBuffer;
    private static ReaderSetting mCurReaderSetting;
    private static ReaderBase mReader;
    private static boolean isConnect = false;

    private static String[] mDevNameList;
    private static String[] mDevPathList;

    private static TagListener mListener;
    private static LocalBroadcastManager mLocalBroadcastManager;
    private static RFIDLogReceiver mRfidLogReceiver;

    private RFIDUtils() {

    }

    public static void preConnect() {
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        mDevNameList = mSerialPortFinder.getAllDevices();
        mDevPathList = mSerialPortFinder.getAllDevicesPath();
    }

    public static String[] getDevNameList() {
        return mDevNameList;
    }

    public static String[] getDevPathList() {
        return mDevPathList;
    }


    /**
     * 初始化连接
     */
    // FIX: 17-5-16 Exception直接抛出让外面去判断
    public static boolean connect(@NonNull Context context, @NonNull File device) throws Exception {
        if (isConnect) {
            return isConnect;
        }
        ReaderHelper.setContext(context);
//        // TODO: 16-6-14 移植自Table2.0 写死了设备和波特率
//        File device = new File("/dev/ttyMT1"); // 硬件设备
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            //绿皮旧平板适配
//            device = new File("/dev/ttyMT0");
//        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
//            device = new File("/dev/ttyMT2");
//        }
        mSerialPort = new SerialPort(
                device, // 硬件设备
                115200, // 波特率
                0
        );
        mReaderHelper = ReaderHelper.getDefaultHelper();
        mReaderHelper.setReader(mSerialPort.getInputStream(), mSerialPort.getOutputStream());
        isConnect = true;
        return isConnect;
    }

    public static boolean checkDevicePermission(File device) {
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        return device.canRead() && device.canWrite();
    }

//    public static boolean isConnect() {
//        return isConnect;
//    }


    public static void pause() {
        if (mReaderHelper != null) {
            mReaderHelper.setInventoryFlag(false);
        }
        if (mCurInventoryBuffer != null) {
            mCurInventoryBuffer.bLoopInventoryReal = false;
        }

        if (mLocalBroadcastManager != null) {
            mLocalBroadcastManager.unregisterReceiver(mRfidLogReceiver);
        }
    }

    /**
     * 断开模块连接，不能断开，会导致无法重连
     */
    public static void disconnect() {
        if (isConnect) {
            pause();
            if (mReader != null) {
                mReader.signOut();
            }
            mSerialPort.close();
            isConnect = false;
        }
    }

    public static void refresh() {
        mCurInventoryBuffer.clearInventoryRealResult();
    }

    public static void start(Context context, TagListener mListener) {
        RFIDUtils.mListener = mListener;
        if (!isConnect) {
            return;
        }
        try {
            mReaderHelper = ReaderHelper.getDefaultHelper();

            mReader = mReaderHelper.getReader();
            //开始循环接收数据线程。
            if (!mReader.IsAlive()) {
                mReader.StartWait();
            }

            mCurInventoryBuffer = mReaderHelper.getCurInventoryBuffer();

            mCurReaderSetting = mReaderHelper.getCurReaderSetting();

            mCurInventoryBuffer.clearInventoryPar();
            //添加所有的天线设置

            //Collections.addAll(mCurInventoryBuffer.lAntenna, TableApp.getAntenna());
            //如果设置为空，载入默认设置
            // TODO: 16-6-14 默认设置全天线
            if (mCurInventoryBuffer.lAntenna.size() <= 0) {
                Collections.addAll(mCurInventoryBuffer.lAntenna, (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0x04);
            }
            //设置为循环盘存
            mCurInventoryBuffer.bLoopInventoryReal = true;
            //设置每条命令的存盘数
            mCurInventoryBuffer.btRepeat = (byte) 1;
            //设置无自定义Session参数
            mCurInventoryBuffer.bLoopCustomizedSession = false;

            mCurInventoryBuffer.clearInventoryRealResult();

            mReaderHelper.setInventoryFlag(true);

            mReaderHelper.clearInventoryTotal();

            setWorkAntenna();
        } catch (Exception e) {
            e.printStackTrace();
        }


        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        mRfidLogReceiver = new RFIDLogReceiver(mListener);
        IntentFilter intentFilter = new IntentFilter();
        //添加所有ReaderHelper中的监听
        intentFilter.addAction(ReaderHelper.BROADCAST_ON_LOST_CONNECT);
        intentFilter.addAction(ReaderHelper.BROADCAST_WRITE_DATA);
        intentFilter.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
        intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
        intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY);
        intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
        intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH);
        intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG);
        intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B);
        //注册Log广播监听
        mLocalBroadcastManager.registerReceiver(mRfidLogReceiver, intentFilter);


        LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, blockingQueue);
        new AsyncTask<Void, InventoryBuffer.InventoryTagMap, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                while (mReaderHelper.getInventoryFlag()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mCurInventoryBuffer.bLoopInventoryReal) {
                        try {
                            setWorkAntenna();
                            List<InventoryBuffer.InventoryTagMap> data = mCurInventoryBuffer.lsTagList;
                            if (data != null && data.size() > 0) {
                                for (InventoryBuffer.InventoryTagMap tag : data) {
                                    //读取次数不位0
                                    if (tag.nReadCount != 0) {
                                        publishProgress(tag);
                                    }
                                }
                            }

                            mCurInventoryBuffer.clearTagMap();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(InventoryBuffer.InventoryTagMap... values) {
                super.onProgressUpdate(values);
                InventoryBuffer.InventoryTagMap current = values[0];
                String rfid = current.strEPC.replace(" ", "");
                String info = String.format("%s\n-%sdMm", current.strFreq, current.strRSSI);
                if (RFIDUtils.mListener != null) {
                    RFIDUtils.mListener.onSuccess(rfid, info);
                }
            }
        }.executeOnExecutor(exec);
    }

    public static void release() {
        mListener = null;
        mRfidLogReceiver = null;
        ReaderHelper.setContext(null);
    }

    private static void setWorkAntenna() {
        byte btWorkAntenna = mCurInventoryBuffer.lAntenna
                .get(mCurInventoryBuffer.nIndexAntenna);
        if (btWorkAntenna < 0) {
            btWorkAntenna = 0;
        }

        mReader.setWorkAntenna(mCurReaderSetting.btReadId, btWorkAntenna);

        //mCurReaderSetting.btWorkAntenna = btWorkAntenna;
    }

    static class RFIDLogReceiver extends BroadcastReceiver {

        private TagListener mListener;

        public RFIDLogReceiver(TagListener mListener) {
            this.mListener = mListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String log = intent.getStringExtra("log");
            int type = intent.getIntExtra("type", 0);
            byte cmd = intent.getByteExtra("cmd", (byte) 0x00);
            switch (action) {
                case ReaderHelper.BROADCAST_ON_LOST_CONNECT:
                    log = "端口异常断开,正在重连...";
                    break;
                case ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL:
                    log = "REFRESH REAL CODE：" + cmd;
                    break;
            }
            mListener.onLogReceive(log, action);

        }
    }
}
