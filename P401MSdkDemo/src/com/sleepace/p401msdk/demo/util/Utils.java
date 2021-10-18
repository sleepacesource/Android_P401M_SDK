package com.sleepace.p401msdk.demo.util;

import com.sleepace.p401msdk.demo.DemoApp;
import com.sleepace.p401msdk.demo.R;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Utils {

	public static String getDuration(Context context, short duration) {
		String str = "";
		int hour = duration / 60;
		int min = duration % 60;

		if (hour > 0) {
			str = (hour + context.getString(R.string.unit_h));
		}

		if (hour == 0 || min > 0)
			str += (min + context.getString(R.string.unit_m));

		return str;
	}

	public static String getSelectDay(Context context, byte val) {
		StringBuilder sb = new StringBuilder();
		String[] week = context.getResources().getStringArray(R.array.arr_week);
		int[] repeat = getWeekRepeat(val);
		int lastDay = 0;
		for (int i = 0; i < 7; i++) {
			if (repeat[i] == 1) {
				lastDay++;
				sb.append(week[i] + "、");
			}
		}

		if (lastDay > 0) {
			int idx = sb.lastIndexOf("、");
			if (idx != -1) {
				sb.deleteCharAt(idx);
			}
		} else {
			sb.delete(0, sb.length());
			sb.append(context.getString(R.string.once));
		}
		return sb.toString();
	}

	public static int[] getWeekRepeat(byte val) {
		int[] week = new int[7];
		// String repeatStr = Integer.toBinaryString(weekday);
		for (int i = 0; i < week.length; i++) {
			week[i] = val >> i & 0x1;
		}
		return week;
	}
	
	public static byte getWeekRepeat(int[] repeat) {
        StringBuilder sb = new StringBuilder("0");
        for (int i = 6; i >= 0; i--) {
            sb.append(repeat[i]);
        }
        return Byte.valueOf(sb.toString(), 2);
    }
	
	public static int getAlarmMusicName(int musicId) {
//		LogUtil.log("getAlarmMusicName musicId:" + musicId);
		int len = DemoApp.ALARM_MUSIC.length;
		for(int i=0;i<len;i++) {
			if(DemoApp.ALARM_MUSIC[i][0] == musicId) {
				return DemoApp.ALARM_MUSIC[i][1];
			}
		}
		return 0;
	}
	
	public static int getSleepAidMusicName(int musicId) {
		int len = DemoApp.SLEEPAID_MUSIC.length;
		for(int i=0;i<len;i++) {
			if(DemoApp.SLEEPAID_MUSIC[i][0] == musicId) {
				return DemoApp.SLEEPAID_MUSIC[i][1];
			}
		}
		return 0;
	}

	public static void setRadioGroupEnable(RadioGroup rg, boolean enable) {
		if(rg != null) {
			int count = rg.getChildCount();
			for(int i=0;i<count;i++) {
				rg.getChildAt(i).setEnabled(enable);
			}
		}
	}
	
}
