package com.sleepace.p401msdk.demo.fragment;

import com.sleepace.p401msdk.demo.DataListActivity;
import com.sleepace.p401msdk.demo.DemoApp;
import com.sleepace.p401msdk.demo.MainActivity;
import com.sleepace.p401msdk.demo.R;
import com.sleepace.p401msdk.demo.util.Utils;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.p401m.P401MHelper.WorkStatusListener;
import com.sleepace.sdk.p401m.domain.SleepAidConfig;
import com.sleepace.sdk.p401m.domain.WorkStatus;
import com.sleepace.sdk.util.SdkLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ControlFragment extends BaseFragment {
	private TextView tvMusic, tvLoopMode, tvDuration, tvSmartStop;
	private EditText etVolume;
	private Button btnPlay, btnSend, btnSave;
	public static SleepAidConfig config = new SleepAidConfig();
	private static boolean playing;
	
	private short[] durationValue = {15, 30, 45, 60};
    private byte[] switcherValue = {1, 0};
    private short durationVal;
    private byte smartFlag;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_control, null);
		// SdkLog.log(TAG+" onCreateView-----------");
		findView(view);
		initListener();
		initUI();
		return view;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		tvMusic = root.findViewById(R.id.tv_music);
		tvLoopMode = root.findViewById(R.id.tv_loop_mode);
		tvDuration = root.findViewById(R.id.tv_duration);
		tvSmartStop = root.findViewById(R.id.tv_smart_stop_flag);
		etVolume = root.findViewById(R.id.et_volume);
		btnPlay = root.findViewById(R.id.btn_play);
		btnSend = root.findViewById(R.id.btn_send_volume);
		btnSave = root.findViewById(R.id.btn_save);
	}

	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		tvMusic.setOnClickListener(this);
		tvLoopMode.setOnClickListener(this);
		tvDuration.setOnClickListener(this);
		tvSmartStop.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		getDeviceHelper().addWorkStatusListener(workStatusListener);
	}
	
	private WorkStatusListener workStatusListener = new WorkStatusListener() {
		@Override
		public void onWorkStatusChanged(final WorkStatus workStatus) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG+" onWorkStatusChanged:" + workStatus);
			if(isFragmentVisible()) {
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						playing = workStatus.getMusicStatus() == 1;
						config.setMusicId(workStatus.getMusicId());
						config.setCircleMode(workStatus.getCircleMode());
						config.setVolume(workStatus.getVolume());
						initMusicView();
						initLoopModeView();
						initPlayButton();
						initVolumeView();
					}
				});
			}
		}
	};

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.sleepaid_music);
		
		setPageEnable(getDeviceHelper().isConnected());
		
		config.setMusicId((short)DemoApp.SLEEPAID_MUSIC[0][0]);
		config.setCircleMode((byte)1);
		config.setVolume((byte)6);
		config.setSleepAidDuration((short)45);
		
		if(getDeviceHelper().isConnected()) {
			showLoading();
			getDeviceHelper().sleepAidConfigGet(new IResultCallback<SleepAidConfig>() {
				@Override
				public void onResultCallback(final CallbackData<SleepAidConfig> cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG+" sleepAidConfigGet cd:" + cd);
					if(isFragmentVisible()) {
						mActivity.runOnUiThread(new Runnable() {
							public void run() {
								hideLoading();
								if(cd.isSuccess()) {
									SleepAidConfig bean = cd.getResult();
									durationVal = bean.getSleepAidDuration();
									smartFlag = bean.getSmartFlag();
									
									config.setMusicFlag(bean.getMusicFlag());
									config.setMusicId(bean.getMusicId());
									config.setVolume(bean.getVolume());
									config.setCircleMode(bean.getCircleMode());
									config.setSmartFlag(bean.getSmartFlag());
									config.setSleepAidDuration(bean.getSleepAidDuration());
									
									initMusicView();
									initLoopModeView();
									initVolumeView();
									initDurationView();
									initSwitchView();
								}
								
								getDeviceHelper().workStatusGet(new IResultCallback<WorkStatus>() {
									@Override
									public void onResultCallback(final CallbackData<WorkStatus> cd) {
										// TODO Auto-generated method stub
										SdkLog.log(TAG+" workStatusGet cd:" + cd);
										if(isFragmentVisible()) {
											mActivity.runOnUiThread(new Runnable() {
												public void run() {
													if(cd.isSuccess()) {
														WorkStatus workStatus = cd.getResult();
														MainActivity.workStatus = workStatus;
														playing = workStatus.getMusicStatus() == 1;
														config.setMusicId(workStatus.getMusicId());
														config.setCircleMode(workStatus.getCircleMode());
														config.setVolume(workStatus.getVolume());
														
														initMusicView();
														initLoopModeView();
														initPlayButton();
														initVolumeView();
													}
												}
											});
										}
									}
								});
							}
						});
					}
				}
			});
		}else {
			initMusicView();
			initLoopModeView();
			initVolumeView();
			initDurationView();
			initSwitchView();
		}
	}
	
	private void setPageEnable(boolean enable){
		btnPlay.setEnabled(enable);
		btnSend.setEnabled(enable);
		btnSave.setEnabled(enable);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SdkLog.log(TAG + " onResume realtimeDataOpen:" + MainActivity.realtimeDataOpen+",playing:" + playing);
		initPlayButton();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		SdkLog.log(TAG + " onDestroyView----------------");
		getDeviceHelper().removeWorkStatusListener(workStatusListener);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if(v == tvDuration) {
    		String[] items = new String[durationValue.length];
    		for(int i=0; i<items.length; i++) {
    			items[i] = durationValue[i] + getString(R.string.unit_m);
    		}
    		AlertDialog dialog = new AlertDialog.Builder(mActivity)
    				.setTitle(R.string.timer_stop)
    				.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							durationVal = durationValue[which];
							initDurationView();
						}
					})
    				.create();
    		dialog.show();
    	}else if(v == tvSmartStop) {
    		String[] items = new String[switcherValue.length];
    		items[0] = getString(R.string.on);
    		items[1] = getString(R.string.off);
    		AlertDialog dialog = new AlertDialog.Builder(mActivity)
    				.setTitle(R.string.smart_stop_music)
    				.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							smartFlag = switcherValue[which];
							initSwitchView();
						}
					})
    				.create();
    		dialog.show();
    	}else if(v == tvMusic) {
			Intent intent = new Intent(mActivity, DataListActivity.class);
			intent.putExtra("dataType", DataListActivity.DATA_TYPE_SLEEPAID_MUSIC);
//			intent.putExtra("musicId", alarm.getMusicId());
			startActivityForResult(intent, 101);
		}else if(v == tvLoopMode) {
			String[] items = new String[] {getString(R.string.play_mode_list), getString(R.string.play_mode_single)};
    		AlertDialog dialog = new AlertDialog.Builder(mActivity)
    				.setTitle(R.string.loop_mode)
    				.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							byte circleMode = (byte) which;
							config.setCircleMode(circleMode);
							initLoopModeView();
							if(playing) {
								startSleepAid();
							}
						}
					})
    				.create();
    		dialog.show();
		}else if(v == btnPlay) {
			SdkLog.log(TAG+" onClick playing:" + playing);
			if(playing) {
				stopSleepAid();
			}else {
				startSleepAid();
			}
		}else if(v == btnSend) {
			String volumeStr = etVolume.getText().toString().trim();
			if(!TextUtils.isEmpty(volumeStr)) {
				int volume = Integer.valueOf(volumeStr);
				if(volume >=1 && volume <= 8) {
					config.setVolume((byte)volume);
					startSleepAid();
				}
			}
		}else if(v == btnSave) {
			config.setMusicFlag((byte)0);
			config.setSleepAidDuration(durationVal);
			config.setSmartFlag(smartFlag);
			showLoading();
			getDeviceHelper().sleepAidConfigSet(config.getMusicFlag(), config.getMusicId(), config.getVolume(), config.getCircleMode(), config.getSmartFlag(), config.getSleepAidDuration(), new IResultCallback<SleepAidConfig>() {
				@Override
				public void onResultCallback(final CallbackData<SleepAidConfig> cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG+" sleepAidConfigSet cd:" + cd);
					if(isFragmentVisible()) {
						mActivity.runOnUiThread(new Runnable() {
							public void run() {
								hideLoading();
								if(cd.isSuccess()) {
									
								}
							}
						});
					}
				}
			});
		}
	}
	
	private void startSleepAid() {
		config.setMusicFlag((byte)1);
		getDeviceHelper().sleepAidConfigSet(config.getMusicFlag(), config.getMusicId(), config.getVolume(), config.getCircleMode(), 
				config.getSmartFlag(), config.getSleepAidDuration(), new IResultCallback() {
					@Override
					public void onResultCallback(final CallbackData cd) {
						// TODO Auto-generated method stub
						SdkLog.log(TAG+" sleepAidConfigSet cd:" + cd);
						if(isFragmentVisible()) {
							mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									if(cd.isSuccess()) {
										playing = true;
										initPlayButton();
									}
								}
							});
						}
					}
		});
	}
	
	private void stopSleepAid() {
		config.setMusicFlag((byte)0);
		getDeviceHelper().sleepAidConfigSet(config.getMusicFlag(), config.getMusicId(), config.getVolume(), config.getCircleMode(), 
				config.getSmartFlag(), config.getSleepAidDuration(), new IResultCallback() {
					@Override
					public void onResultCallback(final CallbackData cd) {
						// TODO Auto-generated method stub
						SdkLog.log(TAG+" sleepAidConfigSet cd:" + cd);
						if(isFragmentVisible()) {
							mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									if(cd.isSuccess()) {
										playing = false;
										initPlayButton();
									}
								}
							});
						}
					}
		});
	}
	
	private void initMusicView() {
		tvMusic.setText(Utils.getSleepAidMusicName(config.getMusicId()));
	}

	private void initLoopModeView() {
		tvLoopMode.setText(config.getCircleMode() == 0 ? R.string.play_mode_list : R.string.play_mode_single);
	}
	
	private void initVolumeView() {
		etVolume.setText(String.valueOf(config.getVolume()));
	}
	
	private void initDurationView() {
		tvDuration.setText(durationVal + getString(R.string.unit_m));
	}
	
	private void initSwitchView() {
		tvSmartStop.setText(smartFlag == 1 ? R.string.on : R.string.off);
	}
	
	private void initPlayButton() {
		if(playing) {
			btnPlay.setText(R.string.pause);
		}else {
			btnPlay.setText(R.string.play);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
			short musicId = data.getShortExtra("musicId", (short)0);
			config.setMusicId(musicId);
			initMusicView();
			startSleepAid();
		}
	}
}










