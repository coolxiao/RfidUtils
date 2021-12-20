package com.kingyun.som.rfid.zf;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public class FileWriter {
    public static void writeFile(String value){
        File file = new File("/sys/class/yt_gpio_ctl/yt_gpio_ctl/yt_gpio_ctl");
        java.io.FileWriter fr = null;
        try {
            fr = new java.io.FileWriter(file);
            fr.write(value);
            fr.close();
            fr = null;
        } catch (IOException e) {
            Log.e("FileWriter", "e===="+e.toString());
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
            }
        }
    }
}