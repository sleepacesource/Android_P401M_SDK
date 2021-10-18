package com.sleepace.p401msdk.demo.view;

import java.util.Arrays;

import com.sleepace.p401msdk.demo.R;
import com.sleepace.p401msdk.demo.view.wheelview.ArrayWheelAdapter;
import com.sleepace.p401msdk.demo.view.wheelview.NumericWheelAdapter;
import com.sleepace.p401msdk.demo.view.wheelview.OnItemSelectedListener;
import com.sleepace.p401msdk.demo.view.wheelview.WheelAdapter;
import com.sleepace.p401msdk.demo.view.wheelview.WheelView;
import com.sleepace.sdk.util.SdkLog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SelectValueDialog extends Dialog {
	private static final String TAG = SelectValueDialog.class.getSimpleName();
	private TextView tvCancel, tvTitle, tvOk, tvUnit;
	private WheelView wvValue;
    private WheelAdapter adapter;
    
    private String leftBtnLabel, title, rightBtnLabel;
    private String unit;
    
    public static final int TYPE_AID_DURATION = 1;
    public static final int TYPE_LOOP_MODE = 2;
    public static final int TYPE_VOLUME = 3;
    public static final int TYPE_SNOOZE_TIME = 4;
    public static final int TYPE_SNOOZE_COUNT = 5;
    public static final int TYPE_DELAY_CLOSE = 6;
    public static final int TYPE_TIMER_MODE = 7;
    public static final int TYPE_RING_TIME = 8;
    public static final int TYPE_MUSIC_DURATION = 9;
    public static final int TYPE_WAKE_RANGE = 10;
    private int type = 0;
    
    private ValueSelectedListener valueSelectedListener;
    private Object[] data;
    private int idx = -1;
    private Object value;

	public SelectValueDialog(Context context) {
		super(context, R.style.myDialog);
		// TODO Auto-generated constructor stub
	}
	
//	public SelectValueDialog(Context context, Object[] data) {
//		super(context, R.style.myDialog);
//		// TODO Auto-generated constructor stub
//		this.data = data;
//	}
	
	public void initData(int type, String leftBtnLabel, String title, String rightBtnLabel, String unit, Object[] data) {
		this.type = type;
		this.leftBtnLabel = leftBtnLabel;
		this.title = title;
		this.rightBtnLabel = rightBtnLabel;
		this.unit = unit;
		this.data = data;
		initView();
	}
	
	public void setDefaultIndex(int idx) {
		if(idx < 0) {
			idx = 0;
		}else if(idx >= data.length) {
			idx = data.length - 1;
		}
		this.idx = idx;
		this.value = data[idx];
		SdkLog.log(TAG+" setDefaultIndex idx:" + idx+",val:" + value);
		initView();
	}
	
	public void setDefaultValue(Object value) {
		this.value = value;
		this.idx = -1;
		SdkLog.log(TAG+" setDefaultValue:" + value);
		initView();
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		SdkLog.log(TAG+" show idx:" + idx);
		super.show();
	}
	
	public void setValueSelectedListener(ValueSelectedListener valueSelectedListener) {
		this.valueSelectedListener = valueSelectedListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_select_value);
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics d = getContext().getResources().getDisplayMetrics(); // 获取屏幕宽、高用
		lp.width = d.widthPixels;
		lp.gravity = Gravity.BOTTOM;
		dialogWindow.setAttributes(lp);
		
		tvCancel = (TextView) findViewById(R.id.tv_cancel);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvOk = (TextView) findViewById(R.id.tv_ok);
		tvUnit = (TextView) findViewById(R.id.tv_label);
		
		tvCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		
		tvOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
				SdkLog.log(TAG+" ok click type:" + type+",idx:" + idx+",val:" + value);
				if(valueSelectedListener != null) {
					valueSelectedListener.onValueSelected(SelectValueDialog.this, type, idx, value);
				}
			}
		});
		
		wvValue = (WheelView) findViewById(R.id.val);
        wvValue.setTextSize(20);
        wvValue.setCyclic(true);
//      wvValue.setRate(5 / 4.0f);
        
        initView();
	}
	
	private void initView() {
		if(tvCancel == null || tvTitle == null || tvOk == null) {
			return;
		}
		
		tvCancel.setText(leftBtnLabel);
		tvTitle.setText(title);
		tvOk.setText(rightBtnLabel);
		
		if(TextUtils.isEmpty(unit)) {
			tvUnit.setVisibility(View.INVISIBLE);
		}else {
			tvUnit.setVisibility(View.VISIBLE);
			tvUnit.setText(unit);
		}
		
		if(data != null && data.length > 0) {
			if(data[0] instanceof String) {
				String[] ss = new String[data.length];
				for(int i=0; i<data.length; i++) {
					ss[i] = (String) data[i];
					if(idx == -1 && value != null) {
						if(ss[i].equals(value)) {
							idx = i;
						}
					}
				}
				wvValue.setAdapter(new ArrayWheelAdapter<String>(ss));
			}else if(data[0] instanceof Number) {
				int[] nums = new int[data.length];
				for(int i=0; i<data.length; i++) {
					nums[i] = (int)(Integer) data[i];
					if(idx == -1 && value != null) {
						if(nums[i] == Integer.valueOf(value.toString())) {
							idx = i;
						}
					}
				}
				wvValue.setAdapter(new NumericWheelAdapter(nums, 0));
			}
			
			SdkLog.log(TAG+" initView idx:" + idx+",val:" + value+",data:" + Arrays.toString(data));
			if(idx >= 0) {
				wvValue.setCurrentItem(idx);
			}
			
			if(type == TYPE_DELAY_CLOSE && idx == 0) {
				tvUnit.setVisibility(View.INVISIBLE);
			}
//			wvValue.setCyclic(false);
			wvValue.setOnItemSelectedListener(onItemSelectedListener);
		}
	}
	
	 //更新控件快速滑动
    private OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(int index) {
        	idx =  index;
        	SdkLog.log(TAG+" onItemSelected idx:" + index+",data:" + Arrays.toString(data));
        	value = data[idx];
        	if(type == TYPE_DELAY_CLOSE && index == 0) {
        		tvUnit.setVisibility(View.INVISIBLE);
        	}else {
        		tvUnit.setVisibility(View.VISIBLE);
        	}
        }
    };
    
    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return TAG;
    }
    
    public interface ValueSelectedListener{
    	void onValueSelected(SelectValueDialog dialog, int type, int index, Object value);
    }

}
