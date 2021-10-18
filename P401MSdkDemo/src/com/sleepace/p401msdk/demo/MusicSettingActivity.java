package com.sleepace.p401msdk.demo;

import com.sleepace.p401msdk.demo.util.ActivityUtil;
import com.sleepace.p401msdk.demo.view.SelectValueDialog;
import com.sleepace.p401msdk.demo.view.SelectValueDialog.ValueSelectedListener;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.p401m.P401MHelper;
import com.sleepace.sdk.p401m.domain.MusicConfig;
import com.sleepace.sdk.util.SdkLog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MusicSettingActivity extends BaseActivity {
	private TextView tvDuration, tvSmartStop;
    private Button btnSave;
    private P401MHelper deviceHelper;
    private short[] durationValue = {15, 30, 45, 60};
    private byte[] switcherValue = {1, 0};
    private short duration = 45;
    private byte onoff = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceHelper = P401MHelper.getInstance(this);
        setContentView(R.layout.activity_musicsetting);
        findView();
        initListener();
        initUI();
    }


    public void findView() {
    	super.findView();
    	tvDuration = (TextView) findViewById(R.id.tv_duration);
    	tvSmartStop = (TextView) findViewById(R.id.tv_smart_stop_flag);
        btnSave = (Button) findViewById(R.id.btn_save);
    }

    public void initListener() {
    	super.initListener();
    	btnSave.setOnClickListener(this);
    	tvDuration.setOnClickListener(this);
    	tvSmartStop.setOnClickListener(this);
    }

    public void initUI() {
    	super.initUI();
        tvTitle.setText(R.string.music);
//		valueDialog = new SelectValueDialog(this);
//		valueDialog.setValueSelectedListener(valueSelectedListener);
        setPageEnable(deviceHelper.isConnected());
        if(deviceHelper.isConnected()) {
        	showLoading();
        	deviceHelper.musicConfigGet(new IResultCallback<MusicConfig>() {
        		@Override
        		public void onResultCallback(final CallbackData<MusicConfig> cd) {
        			// TODO Auto-generated method stub
        			SdkLog.log(TAG+" musicConfigGet cd:" + cd);
        			if(ActivityUtil.isActivityAlive(MusicSettingActivity.this)) {
        				runOnUiThread(new Runnable() {
        					public void run() {
        						hideLoading();
        						if(cd.isSuccess()) {
        							MusicConfig config = cd.getResult();
        							duration = config.getDuration();
        							onoff = config.getOnoff();
        							initDurationView(duration);
        							initSwitchView(onoff);
        						}
        					}
        				});
        			}
        		}
        	});
        }else {
        	initDurationView(duration);
			initSwitchView(onoff);
        }
    }
    
    private void setPageEnable(boolean enable){
		btnSave.setEnabled(enable);
	}

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onClick(View v) {
    	super.onClick(v);
    	if(v == tvDuration) {
    		String[] items = new String[durationValue.length];
    		for(int i=0; i<items.length; i++) {
    			items[i] = durationValue[i] + getString(R.string.unit_m);
    		}
    		AlertDialog dialog = new AlertDialog.Builder(this)
    				.setTitle(R.string.timer_stop)
    				.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							duration = durationValue[which];
							initDurationView(duration);
						}
					})
    				.create();
    		dialog.show();
    	}else if(v == tvSmartStop) {
    		String[] items = new String[switcherValue.length];
    		items[0] = getString(R.string.on);
    		items[1] = getString(R.string.off);
    		AlertDialog dialog = new AlertDialog.Builder(this)
    				.setTitle(R.string.smart_stop_music)
    				.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							onoff = switcherValue[which];
							initSwitchView(onoff);
						}
					})
    				.create();
    		dialog.show();
    	}else if(v == btnSave){
    		showLoading();
    		deviceHelper.musicConfigSet(onoff, duration, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG+" musicConfigSet cd:" + cd);
					if(ActivityUtil.isActivityAlive(MusicSettingActivity.this)) {
						runOnUiThread(new Runnable() {
							public void run() {
								hideLoading();
								if(cd.isSuccess()) {
									finish();
								}
							}
						});
					}
				}
			});
    	}
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private ValueSelectedListener valueSelectedListener = new ValueSelectedListener() {
		@Override
		public void onValueSelected(SelectValueDialog dialog, int type, int index, Object value) {
			// TODO Auto-generated method stub
//			LogUtil.log(TAG+" onValueSelected val:" + value);
			if(type == SelectValueDialog.TYPE_MUSIC_DURATION) {
//				durationIndex = (byte) index;
//				int duration = Integer.valueOf(durationValue[durationIndex].toString());
//				initDurationView(duration);
			}
		}
	};

	private void initDurationView(int duration) {
		tvDuration.setText(duration + getString(R.string.unit_m));
	}
	
	private void initSwitchView(byte onoff) {
		tvSmartStop.setText(onoff == 1 ? R.string.on : R.string.off);
	}
}












