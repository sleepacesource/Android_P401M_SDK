package com.sleepace.p401msdk.demo;

import com.sleepace.p401msdk.demo.util.ActivityUtil;
import com.sleepace.p401msdk.demo.util.Utils;
import com.sleepace.p401msdk.demo.view.SelectTimeDialog;
import com.sleepace.p401msdk.demo.view.SelectTimeDialog.TimeSelectedListener;
import com.sleepace.p401msdk.demo.view.SelectValueDialog;
import com.sleepace.p401msdk.demo.view.SelectValueDialog.ValueSelectedListener;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.p401m.P401MHelper;
import com.sleepace.sdk.p401m.domain.AlarmInfo;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.util.TimeUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class EditAlarmActivity extends BaseActivity {

	private View vStartTime, vRepeat, vMusic, vWakeRange, vSnoozeDuration;
	private TextView tvStartTime, tvRepeat, tvMusic, tvWakeRange, tvSnoozeDuration;
	private EditText etVolume;
	private CheckBox cbWakeup;
	private Button btnSend, btnSave, btnPreview, btnDel;

	private SelectTimeDialog timeDialog;
	private Object[] wakeRangeItems = null, snoozeDurationItems = null;
	private SelectValueDialog valueDialog;
	private P401MHelper deviceHelper;

	private String action;
	private AlarmInfo alarm;
	private byte snoozeCount = 3;

	private boolean isPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_alarm);
		deviceHelper = P401MHelper.getInstance(this);
		findView();
		initListener();
		initUI();
	}

	public void findView() {
		super.findView();
		vStartTime = findViewById(R.id.layout_start_time);
		vRepeat = findViewById(R.id.layout_repeat);
		vMusic = findViewById(R.id.layout_music);
		cbWakeup = findViewById(R.id.cb_smart_wake);
		vWakeRange = findViewById(R.id.layout_smart_wake);
		vSnoozeDuration = findViewById(R.id.layout_snooze_duration);
		tvStartTime = (TextView) findViewById(R.id.tv_start_time);
		tvRepeat = (TextView) findViewById(R.id.tv_reply);
		tvMusic = (TextView) findViewById(R.id.tv_music);
		tvWakeRange = findViewById(R.id.tv_wake_range);
		tvSnoozeDuration = findViewById(R.id.tv_snooze_duration);
		etVolume = findViewById(R.id.et_volume);
		btnSend = (Button) findViewById(R.id.btn_send_volume);
		btnSave = (Button) findViewById(R.id.btn_save);
		btnPreview = (Button) findViewById(R.id.btn_preview);
		btnDel = (Button) findViewById(R.id.btn_delete);
	}

	public void initListener() {
		super.initListener();
		vStartTime.setOnClickListener(this);
		vRepeat.setOnClickListener(this);
		vMusic.setOnClickListener(this);
		vWakeRange.setOnClickListener(this);
		vSnoozeDuration.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		btnPreview.setOnClickListener(this);
		btnDel.setOnClickListener(this);
		cbWakeup.setOnCheckedChangeListener(onCheckedChangeListener);
	}

	public void initUI() {
		super.initUI();
		action = getIntent().getStringExtra("action");
		if ("add".equals(action)) {
			tvTitle.setText(R.string.add_alarm);
			// btnDel.setVisibility(View.INVISIBLE);
			byte newId = getIntent().getByteExtra("newId", (byte) 0);
			alarm = new AlarmInfo();
			alarm.setAlarmId(newId);
			alarm.setEnable((byte) 1);
			alarm.setHour((byte) 8);
			alarm.setRepeat((byte) 127);
			alarm.setMusicId((short) DemoApp.ALARM_MUSIC[0][0]);
			alarm.setSmartAwaken((byte) 1);
			alarm.setSmartOffset((byte) 15);
			alarm.setSnoozeCount(snoozeCount);
			alarm.setSnoozeDuration((byte) 5);
			alarm.setVolume((byte) 4);
			alarm.setTimestamp(TimeUtil.getCurrentTimeInt());
			alarm.setUseFul((byte) 1);
		} else {
			tvTitle.setText(R.string.edit_alarm);
			// btnDel.setVisibility(View.VISIBLE);
			alarm = (AlarmInfo) getIntent().getSerializableExtra("alarm");
			snoozeCount = alarm.getSnoozeCount();
		}

		timeDialog = new SelectTimeDialog(this, SelectTimeDialog.TYPE_START_TIME, "%02d");
		timeDialog.setTimeSelectedListener(timeListener);

		wakeRangeItems = new Object[3];
		wakeRangeItems[0] = 15;
		wakeRangeItems[1] = 20;
		wakeRangeItems[2] = 30;

		snoozeDurationItems = new Object[3];
		snoozeDurationItems[0] = 5;
		snoozeDurationItems[1] = 10;
		snoozeDurationItems[2] = 15;

		valueDialog = new SelectValueDialog(this);
		valueDialog.setValueSelectedListener(valueSelectedListener);

		initTimeView();
		initRepeatView();
		initMusicView();
		initSnoozeView();
		initWakeSwitchView();
		initWakeRangeView();
		initVolumeView();
	}

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			if (buttonView == cbWakeup) {
				alarm.setSmartAwaken((byte) (isChecked ? 1 : 0));
				if (isChecked) {
					vWakeRange.setVisibility(View.VISIBLE);
				} else {
					vWakeRange.setVisibility(View.GONE);
				}
			}
		}
	};

	private ValueSelectedListener valueSelectedListener = new ValueSelectedListener() {
		@Override
		public void onValueSelected(SelectValueDialog dialog, int type, int index, Object value) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG + " onValueSelected val:" + value);
			if (type == SelectValueDialog.TYPE_SNOOZE_TIME) {
				int snoozeDuration = (Integer) value;
				alarm.setSnoozeDuration((byte) snoozeDuration);
				initSnoozeView();
			} else if (type == SelectValueDialog.TYPE_WAKE_RANGE) {
				int wakeRange = (Integer) value;
				alarm.setSmartOffset((byte) wakeRange);
				initWakeRangeView();
			}
		}
	};

	private TimeSelectedListener timeListener = new TimeSelectedListener() {
		@Override
		public void onTimeSelected(int type, byte hour, byte minute) {
			// TODO Auto-generated method stub
			alarm.setHour(hour);
			alarm.setMinute(minute);
			initTimeView();
		}
	};

	private void initTimeView() {
		tvStartTime.setText(String.format("%02d:%02d", alarm.getHour(), alarm.getMinute()));
	}

	private void initRingTimeView() {
		// tvRingTime.setText(alarm.getAlarmMaxTime() + getString(R.string.min));
	}

	private void initRepeatView() {
		tvRepeat.setText(Utils.getSelectDay(this, alarm.getRepeat()));
	}

	private void initMusicView() {
		int res = Utils.getAlarmMusicName(alarm.getMusicId());
		// SdkLog.log(TAG+" initMusicView mid:" + alarm.getMusicID()+",res:" + res);
		if (res > 0) {
			tvMusic.setText(res);
		} else {
			tvMusic.setText("");
		}
	}
	
	private void initVolumeView() {
		 etVolume.setText(String.valueOf(alarm.getVolume()));
	}

	private void initSnoozeView() {
		tvSnoozeDuration.setText(alarm.getSnoozeDuration() + getString(R.string.unit_m));
	}
	
	private void initWakeSwitchView() {
		cbWakeup.setChecked(alarm.getSmartAwaken() == 1);
	}
	
	private void initWakeRangeView() {
		tvWakeRange.setText(alarm.getSmartOffset() + getString(R.string.unit_m));
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v == btnSend) {
			String volumeStr = etVolume.getText().toString().trim();
			if (!TextUtils.isEmpty(volumeStr)) {
				int volume = Integer.valueOf(volumeStr);
				if (volume >= 1 && volume <= 8) {
					deviceHelper.alarmPreview((byte) 2, alarm.getMusicId(), (byte) volume, new IResultCallback() {
						@Override
						public void onResultCallback(CallbackData cd) {
							// TODO Auto-generated method stub

						}
					});
				}
			}
		} else if (v == btnPreview) {
			deviceHelper.alarmPreview((byte) 1, alarm.getMusicId(), alarm.getVolume(), new IResultCallback() {
				@Override
				public void onResultCallback(CallbackData cd) {
					// TODO Auto-generated method stub

				}
			});
		} else if (v == btnSave) {
			String volumeStr = etVolume.getText().toString().trim();
			if (!TextUtils.isEmpty(volumeStr)) {
				int volume = Integer.valueOf(volumeStr);
				if (volume >= 1 && volume <= 8) {
					alarm.setVolume((byte)volume);
				}
			}
			
			showLoading();
			deviceHelper.alarmSet(alarm.getAlarmId(), alarm.getEnable(), alarm.getSmartAwaken(), alarm.getSmartOffset(), alarm.getHour(), alarm.getMinute(), alarm.getRepeat(), alarm.getSnoozeCount(), alarm.getSnoozeDuration(), alarm.getVolume(), alarm.getMusicId(), alarm.getTimestamp(),
					alarm.getUseFul(), new IResultCallback() {
						@Override
						public void onResultCallback(final CallbackData cd) {
							// TODO Auto-generated method stub
							if (!ActivityUtil.isActivityAlive(EditAlarmActivity.this)) {
								return;
							}

							runOnUiThread(new Runnable() {
								public void run() {
									hideLoading();
									if (cd.isSuccess()) {
										// Toast.makeText(EditAlarmActivity.this, R.string.save_succeed,
										// Toast.LENGTH_SHORT).show();
										finish();
									} else {
										// showErrTips(cd);
									}
								}
							});
						}
					});
		} else if (v == vStartTime) {
			timeDialog.initData(SelectTimeDialog.TYPE_START_TIME, getString(R.string.cancel), getString(R.string.time), getString(R.string.confirm), null, null);
			timeDialog.setDefaultValue(alarm.getHour(), alarm.getMinute());
			timeDialog.show();
		} else if (v == null) {
			// valueDialog.initData(SelectValueDialog.TYPE_RING_TIME,
			// getString(R.string.cancel), getString(R.string.alarmMaxTime),
			// getString(R.string.confirm), getString(R.string.min), ringTimeItems);
			// valueDialog.setDefaultIndex(alarm.getAlarmMaxTime() - 1);
			// valueDialog.show();
		} else if (v == vRepeat) {
			Intent intent = new Intent(this, DataListActivity.class);
			intent.putExtra("dataType", DataListActivity.DATA_TYPE_REPEAT);
			intent.putExtra("repeat", alarm.getRepeat());
			startActivityForResult(intent, 100);
		} else if (v == vSnoozeDuration) {
			valueDialog.initData(SelectValueDialog.TYPE_SNOOZE_TIME, getString(R.string.cancel), getString(R.string.snooze_duration), getString(R.string.confirm), getString(R.string.unit_m), snoozeDurationItems);
			valueDialog.setDefaultValue(alarm.getSnoozeDuration());
			valueDialog.show();
		} else if (v == vWakeRange) {
			valueDialog.initData(SelectValueDialog.TYPE_WAKE_RANGE, getString(R.string.cancel), getString(R.string.wake_range), getString(R.string.confirm), getString(R.string.unit_m), wakeRangeItems);
			valueDialog.setDefaultValue(alarm.getSmartOffset());
			valueDialog.show();
		} else if (v == vMusic) {
			int volume = alarm.getVolume();
			String volumeStr = etVolume.getText().toString().trim();
			if (!TextUtils.isEmpty(volumeStr)) {
				volume = Integer.valueOf(volumeStr);
			}
			
			Intent intent = new Intent(this, DataListActivity.class);
			intent.putExtra("dataType", DataListActivity.DATA_TYPE_ALARM_MUSIC);
			intent.putExtra("musicId", alarm.getMusicId());
			intent.putExtra("volume", volume);
			startActivityForResult(intent, 101);
		} else if (v == null) {
			// valueDialog.initData(SelectValueDialog.TYPE_VOLUME,
			// getString(R.string.cancel), getString(R.string.volume),
			// getString(R.string.confirm), null, volumeItems);
			// valueDialog.setDefaultValue(alarm.getVolume());
			// valueDialog.show();
		} else if (v == btnPreview) {
			// showLoading();
			if (!isPreview) {

				// SdkLog.log(TAG + " preview alarm:" + alarm);
				// mHelper.startAlarmPreview(alarm.getVolume(), alarm.getBrightness(),
				// alarm.getMusicID(), 3000, new IResultCallback() {
				// @Override
				// public void onResultCallback(final CallbackData cd) {
				// // TODO Auto-generated method stub
				// if (!ActivityUtil.isActivityAlive(mActivity)) {
				// return;
				// }
				//
				// runOnUiThread(new Runnable() {
				// public void run() {
				// hideLoading();
				// if (cd.isSuccess()) {
				// isPreview = true;
				// btnPreview.setText(R.string.preview_stop);
				// } else {
				// showErrTips(cd);
				// }
				// }
				// });
				// }
				// });
			} else {
				// mHelper.stopAlarmPreview(3000, new IResultCallback() {
				// @Override
				// public void onResultCallback(final CallbackData cd) {
				// // TODO Auto-generated method stub
				// if (!ActivityUtil.isActivityAlive(mActivity)) {
				// return;
				// }
				//
				// runOnUiThread(new Runnable() {
				// public void run() {
				// hideLoading();
				// if (cd.isSuccess()) {
				// isPreview = false;
				// btnPreview.setText(R.string.preview_alarm);
				// } else {
				// showErrTips(cd);
				// }
				// }
				// });
				// }
				// });
			}
		} else if (v == btnDel) {
			showLoading();
			alarm.setUseFul((byte) 0);
			deviceHelper.alarmSet(alarm.getAlarmId(), alarm.getEnable(), alarm.getSmartAwaken(), alarm.getSmartOffset(), alarm.getHour(), alarm.getMinute(), alarm.getRepeat(), alarm.getSnoozeCount(), alarm.getSnoozeDuration(), alarm.getVolume(), alarm.getVolume(), alarm.getTimestamp(),
					alarm.getUseFul(), new IResultCallback() {
						@Override
						public void onResultCallback(final CallbackData cd) {
							// TODO Auto-generated method stub
							if (!ActivityUtil.isActivityAlive(EditAlarmActivity.this)) {
								return;
							}

							runOnUiThread(new Runnable() {
								public void run() {
									hideLoading();
									if (cd.isSuccess()) {
										// Toast.makeText(mActivity, R.string.deleted, Toast.LENGTH_SHORT).show();
										finish();
									} else {
										// showErrTips(cd);
									}
								}
							});
						}
					});

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
			byte repeat = data.getByteExtra("repeat", (byte) 0);
			alarm.setRepeat(repeat);
			if (repeat != 0) {
				alarm.setTimestamp(0);
			}
			initRepeatView();
		} else if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
			short musicId = data.getShortExtra("musicId", (short)0);
			alarm.setMusicId(musicId);
			initMusicView();
		}
	}
}
