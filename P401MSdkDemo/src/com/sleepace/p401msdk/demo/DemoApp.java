package com.sleepace.p401msdk.demo;

import com.sleepace.p401msdk.demo.util.CrashHandler;
import com.sleepace.sdk.util.SdkLog;

import android.app.Application;


public class DemoApp extends Application {

    private static DemoApp instance;
    
    public StringBuffer logBuf = new StringBuffer();

    public static DemoApp getInstance() {
        return instance;
    }
    
    public static final int[][] ALARM_MUSIC = { 
			{ 31166, R.string.alarm_list_1 }, 
			{ 31167, R.string.alarm_list_2 }, 
			{ 31170, R.string.alarm_list_3 }, 
			{ 31168, R.string.alarm_list_4 },
			{ 31169, R.string.alarm_list_5 } };

	public static final int[][] SLEEPAID_MUSIC = { 
			{ 31163, R.string.sleepaid_list_1 }, 
			{ 31164, R.string.sleepaid_list_2 }, 
			{ 31162, R.string.sleepaid_list_3 }, 
			{ 31155, R.string.sleepaid_list_4 },
			{ 31156, R.string.sleepaid_list_5 },
			{ 31157, R.string.sleepaid_list_6 },
			{ 31160, R.string.sleepaid_list_7 }
	};

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler.getInstance().init(this);
        SdkLog.setLogEnable(true);
        String logDir = getExternalFilesDir("log").getPath();
        SdkLog.setLogDir(logDir);
        SdkLog.setSaveLog(true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        super.onTrimMemory(level);

    }

}














