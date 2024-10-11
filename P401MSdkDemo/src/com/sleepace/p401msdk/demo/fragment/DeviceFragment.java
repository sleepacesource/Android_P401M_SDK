package com.sleepace.p401msdk.demo.fragment;

import java.io.IOException;
import java.io.InputStream;

import com.sleepace.p401msdk.demo.MainActivity;
import com.sleepace.p401msdk.demo.R;
import com.sleepace.p401msdk.demo.SearchBleDeviceActivity;
import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IConnectionStateCallback;
import com.sleepace.sdk.interfs.IDeviceManager;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CONNECTION_STATE;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.DeviceType;
import com.sleepace.sdk.p401m.P401MManager.DeviceStateListener;
import com.sleepace.sdk.p401m.domain.BatteryBean;
import com.sleepace.sdk.p401m.domain.DeviceState;
import com.sleepace.sdk.util.SdkLog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceFragment extends BaseFragment {
	private EditText etUserId;
	private Button btnDeviceName, btnDeviceId, btnPower, /*btnEnvirData,*/ btnVersion, btnUpgrade;
	private TextView tvDeviceName, tvDeviceId, tvPower, /*tvEnvirData,*/ tvVersion;
	private Button btnConnect, btnWaitConfirm;
//	private View envirView;
	private boolean upgrading = false;
	private Handler handler = new Handler();
	private boolean receivedConfirm = false;
	private int timeout = 60; //设备确认超时时间60秒

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View root = inflater.inflate(R.layout.fragment_device, null);
//		LogUtil.log(TAG+" onCreateView-----------");
		findView(root);
		initListener();
		return root;
	}
	
	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		etUserId = (EditText) root.findViewById(R.id.et_userid);
		tvDeviceName = (TextView) root.findViewById(R.id.tv_device_name);
		tvDeviceId = (TextView) root.findViewById(R.id.tv_device_id);
		tvPower = (TextView) root.findViewById(R.id.tv_device_battery);
		tvVersion = (TextView) root.findViewById(R.id.tv_device_version);
		btnDeviceName = (Button) root.findViewById(R.id.btn_get_device_name);
		btnDeviceId = (Button) root.findViewById(R.id.btn_get_device_id);
		btnPower = (Button) root.findViewById(R.id.btn_get_device_battery);
//		btnEnvirData = (Button) root.findViewById(R.id.btn_get_envir_data);
//		envirView = root.findViewById(R.id.layout_envir_data);
//		tvEnvirData = (TextView) root.findViewById(R.id.tv_envir_data);
		btnVersion = (Button) root.findViewById(R.id.btn_device_version);
		btnUpgrade = (Button) root.findViewById(R.id.btn_upgrade_fireware);
		btnConnect = (Button) root.findViewById(R.id.btn_connect_device);
		btnWaitConfirm = (Button) root.findViewById(R.id.btn_wait_confirm);
	}


	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		getDeviceHelper().addConnectionStateCallback(stateCallback);
		btnDeviceName.setOnClickListener(this);
		btnDeviceId.setOnClickListener(this);
		btnPower.setOnClickListener(this);
//		btnEnvirData.setOnClickListener(this);
		btnVersion.setOnClickListener(this);
		btnUpgrade.setOnClickListener(this);
		btnConnect.setOnClickListener(this);
		btnWaitConfirm.setOnClickListener(this);
		getDeviceHelper().addDeviceStateListener(deviceStateListener);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SdkLog.log(TAG+" onResume connect:" + getDeviceHelper().isConnected());
		initUI();
	}

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.device_);
//		btnConnect.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); 
		setPageEnable(getDeviceHelper().isConnected());
		btnConnect.setText(getDeviceHelper().isConnected() ? R.string.disconnect : R.string.connect_device);
//		etUserId.setText("1");
		tvDeviceName.setText(MainActivity.deviceName);
		tvDeviceId.setText(MainActivity.deviceId);
		tvPower.setText(MainActivity.power);
		tvVersion.setText(MainActivity.version);
	}
	
	private void setPageEnable(boolean enable){
		btnDeviceName.setEnabled(enable);
		btnDeviceId.setEnabled(enable);
		btnPower.setEnabled(enable);
//		btnEnvirData.setEnabled(enable);
		btnVersion.setEnabled(enable);
		btnUpgrade.setEnabled(enable);
		btnWaitConfirm.setEnabled(enable);
	}
	
	private DeviceStateListener deviceStateListener = new DeviceStateListener() {
		@Override
		public void onDeviceStateChanged(final DeviceState deviceState) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG+" onDeviceStateChanged-----------"+deviceState);
			if(!isFragmentVisible()){
				return;
			}
			
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(deviceState.getType() == (byte)0xF0){
						hideLoading();
						receivedConfirm = true;
						Toast.makeText(mActivity, R.string.receive_device_confirm, Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	};
	
	private IConnectionStateCallback stateCallback = new IConnectionStateCallback() {
		@Override
		public void onStateChanged(IDeviceManager manager, final CONNECTION_STATE state) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG+" onStateChanged------------state:" + state);
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(!isFragmentVisible()){
						return;
					}
					
					if(state == CONNECTION_STATE.DISCONNECT){
						setPageEnable(false);
						btnConnect.setText(R.string.connect_device);
						if(upgrading){
							upgrading = false;
							mActivity.hideUpgradeDialog();
							//printLog(R.string.update_completed);
							//tvUpgrade.setText(R.string.update_completed);
							Toast.makeText(mActivity, R.string.update_success, Toast.LENGTH_LONG).show();
						}
						
						//printLog(R.string.connection_broken);
						
					}else if(state == CONNECTION_STATE.CONNECTED){
						setPageEnable(true);
						btnConnect.setText(R.string.disconnect);
						if(upgrading){
							upgrading = false;
							btnUpgrade.setEnabled(true);
							mActivity.hideUpgradeDialog();
							//printLog(R.string.update_completed);
							//tvUpgrade.setText(R.string.update_completed);
							Toast.makeText(mActivity, R.string.update_success, Toast.LENGTH_LONG).show();
						}
						
					}
				}
			});
		}
	};
	

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		getDeviceHelper().removeConnectionStateCallback(stateCallback);
		getDeviceHelper().removeDeviceStateListener(deviceStateListener);
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		SdkLog.log(TAG+" onActivityResult req:" + requestCode+",res:" + resultCode+",data:" + data);
		if(requestCode == 100 && resultCode == Activity.RESULT_OK) {
			BleDevice device = (BleDevice) data.getSerializableExtra("device");
			mActivity.setDevice(device);
		}
	}
	
	private Runnable checkTimeoutTask = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(timeout > 0) {
				timeout--;
				updateLoadingMsg(timeout+" S");
				handler.postDelayed(this, 1000);
			}else {
				if(!receivedConfirm) {
					hideLoading();
					Toast.makeText(mActivity, R.string.not_receive_device_confirm, Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if(v == btnUpgrade){
			FirmwareBean bean = getFirmwareBean();
			if(bean == null){
				return;
			}
				
			btnUpgrade.setEnabled(false);
			mActivity.showUpgradeDialog();
			upgrading = true;
			getDeviceHelper().stopRealTimeData(3000, new IResultCallback<Void>() {
				@Override
				public void onResultCallback(CallbackData<Void> cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG+" upgrade stopRealTimeData cd:"+cd);
				}
			});
			getDeviceHelper().upgradeDevice(bean.pKey, bean.hashCode, bean.is, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(!isFragmentVisible()){
								return;
							}
							
							if(checkStatus(cd)){
								int progress = (Integer) cd.getResult();
								mActivity.setUpgradeProgress(progress);
								if(progress == 100){
									//tvUpgrade.setText(getString(R.string.reboot_device, getString(R.string.device_name)));
								}
							}else{
								upgrading = false;
								btnUpgrade.setEnabled(true);
								mActivity.hideUpgradeDialog();
								Toast.makeText(mActivity, R.string.update_failed, Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			});
		}else if(v == btnDeviceName){
			//printLog(R.string.getting_device_name);
			MainActivity.deviceName = mActivity.getDevice().getDeviceName();
			tvDeviceName.setText(MainActivity.deviceName);
			//printLog(getString(R.string.receive_device_name, mActivity.getDevice().getDeviceName()));
		}else if(v == btnDeviceId){
			//printLog(R.string.getting_device_id);
			MainActivity.deviceId = mActivity.getDevice().getDeviceId();
			tvDeviceId.setText(MainActivity.deviceId);
			//printLog(getString(R.string.get_device_id, mActivity.getDevice().getDeviceId()));
		}else if(v == btnPower){
			//printLog(R.string.getting_power);
			getDeviceHelper().getBattery(1000, new IResultCallback<BatteryBean>() {
				@Override
				public void onResultCallback(final CallbackData<BatteryBean> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(!isFragmentVisible()){
								return;
							}
							
							if(checkStatus(cd)){
								BatteryBean bean =  cd.getResult();
								MainActivity.power = bean.getQuantity() + "%";
								tvPower.setText(MainActivity.power);
								//printLog(getString(R.string.get_power, MainActivity.power));
							}
						}
					});
				}
			});
		}/*else if(v == btnEnvirData){
			printLog(R.string.getting_envir_data);
			getPillowHelper().getEnvironmentalData(1000, new IResultCallback<EnvironmentData>() {
				@Override
				public void onResultCallback(final CallbackData<EnvironmentData> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							SdkLog.log(TAG+" getEnvironmentalData cd:" + cd);
							if(checkStatus(cd)){
								EnvironmentData bean =  cd.getResult();
								MainActivity.temp = bean.getTemperature()/100 + "℃";
								MainActivity.hum = bean.getHumidity() + "%";
								tvEnvirData.setText(getString(R.string.temp)+":" + MainActivity.temp + "  " + getString(R.string.hum) +":" + MainActivity.hum);
								printLog(getString(R.string.get_envir_data)+ ":" + tvEnvirData.getText().toString());
							}
						}
					});
				}
			});
		}*/else if(v == btnVersion){
			//printLog(R.string.getting_current_version);
			getDeviceHelper().getDeviceVersion(1000, new IResultCallback<String>() {
				@Override
				public void onResultCallback(final CallbackData<String> cd) {
					// TODO Auto-generated method stub
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(!isFragmentVisible()){
								return;
							}
							
							if(checkStatus(cd)){
								MainActivity.version =  cd.getResult();
								tvVersion.setText(MainActivity.version);
								//printLog(getString(R.string.get_the_current_version, MainActivity.version));
							}
						}
					});
				}
			});
		}else if(v == btnConnect){
			if(getDeviceHelper().isConnected()) {
				MainActivity.deviceName = "";
				MainActivity.deviceId = "";
				MainActivity.power = "";
				MainActivity.version = "";
				tvDeviceName.setText(null);
				tvDeviceId.setText(null);
				tvPower.setText(null);
				tvVersion.setText(null);
				getDeviceHelper().disconnect();
			}else {
				String uid = etUserId.getText().toString().trim();
				if(!TextUtils.isEmpty(uid)){
					//btnConnect.setEnabled(false);
//        			mSetting.edit().putString("uid", uid).commit();
					int userId = Integer.valueOf(uid);
					Intent intent = new Intent(mActivity, SearchBleDeviceActivity.class);
					intent.putExtra("userId", userId);
					startActivityForResult(intent, 100);
				}else {
					Toast.makeText(mActivity, R.string.toast_user_id, Toast.LENGTH_SHORT).show();
				}
			}
		}else if(v == btnWaitConfirm) {
			showLoading();
			getDeviceHelper().waitConfirm(new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					if(!isFragmentVisible()){
						return;
					}
					
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(cd.isSuccess()){
								timeout = 60;
								receivedConfirm = false;
								handler.postDelayed(checkTimeoutTask, 1000);
							}else {
								hideLoading();
							}
						}
					});
				}
			});
		}
	}
	
	
	class FirmwareBean{
		InputStream is;
		String pKey;
		String hashCode;
	}
	
	
	private FirmwareBean getFirmwareBean(){
		DeviceType deviceType = mActivity.getDevice().getDeviceType();
		InputStream is = null;
		String pKey = null, hashCode = null;
		
		try {
			switch(deviceType){
			case DEVICE_TYPE_P401M:
				is = getResources().getAssets().open("P401M-v1.52r(v2.0.4b)-g-20210924.img");
				pKey = "e5d55010c63ffd4383acd9559cafaf49d6e424da108cec679224264dcfc948143f30e2b8fab8b341058d307ce761863baa17b426c1a89bb4e4159d85398b4e21";
				hashCode = "bdc0903d2e47edefc453f4a8b588af60680abec935e9d6484cbdc0c5fa4d4f57";
				break;
			default:
				break;
			}
			
			FirmwareBean bean = new FirmwareBean();
			bean.is = is;
			bean.pKey = pKey;
			bean.hashCode = hashCode;
			return bean;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}










