package com.kingyun.som.rfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryReceiver extends BroadcastReceiver {
    public static boolean LOW_POWER_MODEL = false;

    @Override public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
            int level = intent.getIntExtra("level", 0);
            LOW_POWER_MODEL = level < 20;
        }
    }
}