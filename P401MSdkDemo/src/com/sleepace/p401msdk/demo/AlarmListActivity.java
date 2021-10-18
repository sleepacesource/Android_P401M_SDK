package com.sleepace.p401msdk.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sleepace.p401msdk.demo.util.ActivityUtil;
import com.sleepace.p401msdk.demo.util.Utils;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.ble.BleHelper;
import com.sleepace.sdk.p401m.P401MHelper;
import com.sleepace.sdk.p401m.P401MHelper.WorkStatusListener;
import com.sleepace.sdk.p401m.domain.AlarmInfo;
import com.sleepace.sdk.p401m.domain.OffPillowAlarmConfig;
import com.sleepace.sdk.p401m.domain.WorkStatus;
import com.sleepace.sdk.util.SdkLog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmListActivity extends BaseActivity {
	private View vOffPillowAlarm, vOffPillowAction;
	private CheckBox cbOffPillowAlarmEnable;
	private TextView tvOffPillowAlarmAction;
	private ListView listView;
	private Button btnAdd;
	private LayoutInflater inflater;
	private AlarmAdapter adapter;
	
	private P401MHelper deviceHelper;
	
	private static final Byte[] ALARM_IDS = new Byte[] {1, 2, 3, 4};
	private byte[] switcherValue = {1, 0};
	private List<AlarmInfo> list = new ArrayList<AlarmInfo>();
	private OffPillowAlarmConfig offPillowAlarmConfig = new OffPillowAlarmConfig();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		deviceHelper = P401MHelper.getInstance(this);
		findView();
		initListener();
		initUI();
	}

	public void findView() {
		super.findView();
		vOffPillowAlarm = findViewById(R.id.layout_offpillow_alarm);
		vOffPillowAction = findViewById(R.id.layout_offpillow_action);
		cbOffPillowAlarmEnable = findViewById(R.id.cb_alarm_switch);
		tvOffPillowAlarmAction = findViewById(R.id.tv_alarm_action);
		listView = (ListView) findViewById(R.id.list);
		btnAdd = findViewById(R.id.btn_add_alarm);
	}

	public void initListener() {
		super.initListener();
		listView.setOnItemClickListener(onItemClickListener);
		btnAdd.setOnClickListener(this);
		cbOffPillowAlarmEnable.setOnCheckedChangeListener(checkedChangeListener);
		vOffPillowAction.setOnClickListener(this);
		deviceHelper.addWorkStatusListener(workStatusListener);
	}

	public void initUI() {
		inflater = getLayoutInflater();
		tvTitle.setText(R.string.alarm);
		btnAdd.setVisibility(View.VISIBLE);
		
//		AlarmInfo alarm = new AlarmInfo();
//		alarm.setEnable((byte)1);
//		alarm.setHour((byte) 8);
//		alarm.setRepeat((byte) 127);
//		alarm.setMusicId((short)DemoApp.ALARM_MUSIC[0][0]);
//		alarm.setVolume((byte) 4);
//		alarm.setSnoozeDuration((byte) 0);
//		list.add(alarm);
		setPageEnable(deviceHelper.isConnected());
		
		adapter = new AlarmAdapter();
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		if(deviceHelper.isConnected()) {
			deviceHelper.offPillowAlarmGet(new IResultCallback<OffPillowAlarmConfig>() {
				@Override
				public void onResultCallback(final CallbackData<OffPillowAlarmConfig> cd) {
					// TODO Auto-generated method stub
					if(!ActivityUtil.isActivityAlive(AlarmListActivity.this)) {
						return;
					}
					
					runOnUiThread(new Runnable() {
						public void run() {
							hideLoading();
							if(cd.isSuccess()) {
								OffPillowAlarmConfig config = cd.getResult();
								cbOffPillowAlarmEnable.setChecked(config.getEnable() == 1);
								initOffPillowActionView(config.getAction());
							}else {
								//showErrTips(cd);
							}
						}
					});
				}
			});
		}
	}
	
	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			if(isChecked) {
				vOffPillowAction.setVisibility(View.VISIBLE);
			}else {
				vOffPillowAction.setVisibility(View.GONE);
			}
			offPillowAlarmConfig.setEnable((byte)(isChecked ? 1 : 0));
			deviceHelper.offPillowAlarmSet(offPillowAlarmConfig.getEnable(), offPillowAlarmConfig.getAction(), new IResultCallback() {
				@Override
				public void onResultCallback(CallbackData cd) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	};

	private void setPageEnable(boolean enable){
		btnAdd.setEnabled(enable);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(deviceHelper.isConnected()) {
			showLoading();
			deviceHelper.alarmGet((byte)0xff, new IResultCallback<List<AlarmInfo>>() {
				@Override
				public void onResultCallback(final CallbackData<List<AlarmInfo>> cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG+" alarmGet cd:" + cd);
					if(!ActivityUtil.isActivityAlive(AlarmListActivity.this)) {
						return;
					}
					
					runOnUiThread(new Runnable() {
						public void run() {
							hideLoading();
							if(cd.isSuccess()) {
								list.clear();
								List<AlarmInfo> temp = cd.getResult();
								for(int i=0; i<temp.size(); i++) {
									if(temp.get(i).getUseFul() == 1) {
										list.add(temp.get(i));
									}
								}
								adapter.notifyDataSetChanged();
							}else {
								//showErrTips(cd);
							}
						}
					});
				}
			});
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		deviceHelper.removeWorkStatusListener(workStatusListener);
	}
	
	private WorkStatusListener workStatusListener = new WorkStatusListener() {
		@Override
		public void onWorkStatusChanged(final WorkStatus workStatus) {
			// TODO Auto-generated method stub
			if(!ActivityUtil.isActivityAlive(AlarmListActivity.this)) {
				return;
			}
			
			runOnUiThread(new Runnable() {
				public void run() {
					
				}
			});
		}
	};
	
	private byte getNewAlarmId() {
		byte newId = 0;
		List<Byte> allIdList = Arrays.asList(ALARM_IDS);
		List<Byte> curIdList = new ArrayList<Byte>();
		for(AlarmInfo alarm : list) {
			curIdList.add(alarm.getAlarmId());
		}
		
		for(Byte id : allIdList) {
			if(!curIdList.contains(id)) {
				newId = id;
				break;
			}
		}
		
		SdkLog.log(TAG+" getNewAlarmId:" + newId+",curIdList:" + curIdList);
		return newId;
	}
	
	private AlarmInfo getAlarm(byte alarmId) {
		for(AlarmInfo alarm : list) {
			if(alarm.getAlarmId() == alarmId) {
				return alarm;
			}
		}
		return null;
	}
	
	private void closeSameAlarm(AlarmInfo alarm) {
		for(AlarmInfo info : list) {
			if(info.getRepeat() == 0 && info.getHour() == alarm.getHour() && info.getMinute() == alarm.getMinute()) {
//				info.setOpen(false);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == BleHelper.REQCODE_OPEN_BT) {

		}
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			AlarmInfo alarm = adapter.getItem(position);
			Intent intent = new Intent(AlarmListActivity.this, EditAlarmActivity.class);
			intent.putExtra("action", "edit");
			intent.putExtra("alarm", alarm);
			startActivity(intent);
		}
	};

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v == vOffPillowAction) {
			String[] items = new String[switcherValue.length];
    		items[0] = getString(R.string.close_alarm);
    		items[1] = getString(R.string.snooze_);
    		AlertDialog dialog = new AlertDialog.Builder(this)
    				.setTitle(R.string.smart_stop_music)
    				.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							final byte action = switcherValue[which];
							offPillowAlarmConfig.setAction(action);
							initOffPillowActionView(action);
							deviceHelper.offPillowAlarmSet(offPillowAlarmConfig.getEnable(), action, new IResultCallback() {
								@Override
								public void onResultCallback(final CallbackData cd) {
									// TODO Auto-generated method stub
								}
							});
						}
					})
    				.create();
    		dialog.show();
		}else if(v == btnAdd) {
			if(list.size() >= 5) {
				Toast.makeText(this, R.string.tips_add_alarm, Toast.LENGTH_SHORT).show();
			}else {
				Intent intent = new Intent(this, EditAlarmActivity.class);
				intent.putExtra("action", "add");
				intent.putExtra("newId", getNewAlarmId());
				startActivityForResult(intent, 100);
			}
		}
	}
	
	private void initOffPillowActionView(byte action) {
		tvOffPillowAlarmAction.setText(action == 1 ? R.string.close_alarm : R.string.snooze_);
	}
	
	class AlarmAdapter extends BaseAdapter {
		
		AlarmAdapter(){
		}

		class ViewHolder {
			TextView tvTime;
			TextView tvRepeat;
			CheckBox cbSwitch;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public AlarmInfo getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_clock_item, null);
				holder = new ViewHolder();
				holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tvRepeat = (TextView) convertView.findViewById(R.id.tv_continue);
				holder.cbSwitch = (CheckBox) convertView.findViewById(R.id.cb_swtich);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			final AlarmInfo item = getItem(position);
			
			if(item.getSmartAwaken() == 1) {//开启了智能唤醒功能
				String endTime = String.format("%02d:%02d", item.getHour(), item.getMinute());
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, item.getHour());
				calendar.set(Calendar.MINUTE, item.getMinute());
				calendar.set(Calendar.SECOND, 0);
				calendar.add(Calendar.MINUTE, -item.getSmartOffset());
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				String startTime = String.format("%02d:%02d", hour, minute);
				holder.tvTime.setText(startTime + "~" + endTime);
			}else {
				holder.tvTime.setText(String.format("%02d:%02d", item.getHour(), item.getMinute()));
			}
			
			holder.tvRepeat.setText(Utils.getSelectDay(AlarmListActivity.this, item.getRepeat()));
			holder.cbSwitch.setTag(item.getAlarmId());
			holder.cbSwitch.setChecked(item.getEnable() == 1);
			
			holder.cbSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
//					SdkLog.log(TAG+" alarm open changed:" + isChecked+",tag:" + buttonView.getTag()+",item:" + item);
					if(buttonView.getTag() != null) {
						long alarmId = Long.valueOf(buttonView.getTag().toString());
						if(alarmId != item.getAlarmId() || (isChecked && item.getEnable() == 1)) {
							return;
						}
					}
					
					SdkLog.log(TAG+" alarm open changed:" + isChecked);
					item.setEnable((byte) (isChecked ? 1 : 0));
					deviceHelper.alarmSet(item.getAlarmId(), item.getEnable(), item.getSmartAwaken(), item.getSmartOffset(), item.getHour(), item.getMinute(), item.getRepeat(), item.getSnoozeCount(), item.getSnoozeDuration(), item.getVolume(), item.getMusicId(), item.getTimestamp(), item.getUseFul(), new IResultCallback() {
						@Override
						public void onResultCallback(CallbackData cd) {
							// TODO Auto-generated method stub
							SdkLog.log(TAG+" alarmConfig cd:" + cd);
						}
					});
				}
			});
			
			return convertView;
		}
		
		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			if(getCount() > 0) {
				vOffPillowAlarm.setVisibility(View.VISIBLE);
				super.notifyDataSetChanged();
			}else {
				vOffPillowAlarm.setVisibility(View.GONE);
//				tvTips.setText(R.string.sa_no_alarm);
			}
		}
	}
	
	
	
	
}




