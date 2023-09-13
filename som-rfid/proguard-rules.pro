# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#rfid
 -keep class com.kingyun.som.rfid.** { *; }
 -keep class com.pda.hf.** { *; }
 -keep class com.rodinbell.uhf.serialport.** { *; }
 -keep class cn.pda.** { *; }
 -keep class android.serialport.** { *; }
 -keep class com.speedata.** { *; }
 -keep class android.devkit.** { *; }
 -keep class android_serialport_api.** { *; }
 -keep class com.BRMicro.** { *; }
 -keep class com.device.** { *; }
 -keep class com.Isc.** { *; }
 -keep class com.pow.** { *; }
 -keep class com.power.** { *; }
 -keep class com.zhsim.** { *; }
 -keep class rfid.lib.** { *; }
 -keep class com.uhf.api.cls.** { *; }
 -keep class com.uhf_sdk.** { *; }
 -keep class com.uhf.** { *; }
 -keep class com.android.** { *; }
 -keep class com.cetc7.** { *; }
 -keep class uhf.** { *; }
 -keep class android_serialport_api_xy.** { *; }
 -keep class datahandle.** { *; }
 -keep class otg.** { *; }
 -keep class serialport.** { *; }
 -keep class tcp.** { *; }
 -keep class typehelper.** { *; }
 -keep class utils.** { *; }