package com.sleepace.p401msdk.demo;

import java.util.ArrayList;
import java.util.List;

import com.sleepace.p401msdk.demo.fragment.ControlFragment;
import com.sleepace.p401msdk.demo.fragment.DataFragment;
import com.sleepace.p401msdk.demo.fragment.DeviceFragment;
import com.sleepace.p401msdk.demo.fragment.ReportFragment;
import com.sleepace.p401msdk.demo.fragment.SettingFragment;
import com.sleepace.sdk.baseautopillow.domain.HistoryData;
import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IConnectionStateCallback;
import com.sleepace.sdk.interfs.IDeviceManager;
import com.sleepace.sdk.manager.CONNECTION_STATE;
import com.sleepace.sdk.p401m.P401MHelper;
import com.sleepace.sdk.p401m.P401MHelper.WorkStatusListener;
import com.sleepace.sdk.p401m.domain.WorkStatus;
import com.sleepace.sdk.util.SdkLog;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends BaseActivity {
	
	private RadioGroup rgTab;
	private RadioButton rbDevice, rbData, rbControl, rbSetting, rbReport;
	private FragmentManager fragmentMgr;
	private Fragment deviceFragment, dataFragment, controlFragment, settingFragment, reportFragment;
	private BleDevice device;
	private P401MHelper deviceHelper;
	private ProgressDialog upgradeDialog;
	
	private final int requestCode = 101;//权限请求码
    private List<String> unauthoPersssions = new ArrayList<String>();
    private String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.WRITE_EXTERNAL_STORAGE */};
	
	//缓存数据
	public static String deviceName, deviceId, power, version, temp, hum;
	public static boolean realtimeDataOpen;
	
	public static final int TAB_DEVICE = 0;
	public static final int TAB_DATA = 1;
	public static final int TAB_CONTROL = 2;
	public static final int TAB_SETTING = 3;
	public static final int TAB_REPORT = 4;
	private int tabIndex = -1;
	
	public static WorkStatus workStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		deviceHelper = P401MHelper.getInstance(this);
		findView();
		initListener();
		initUI();
		checkPermissions();
	}
	
	@Override
	protected void findView() {
		// TODO Auto-generated method stub
		super.findView();
		rgTab = (RadioGroup) findViewById(R.id.rg_tab);
		rbDevice = (RadioButton) findViewById(R.id.rb_device);
		rbData = (RadioButton) findViewById(R.id.rb_data);
		rbControl = (RadioButton) findViewById(R.id.rb_control);
		rbSetting = (RadioButton) findViewById(R.id.rb_setting);
		rbReport = (RadioButton) findViewById(R.id.rb_report);
	}


	@Override
	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		deviceHelper.addConnectionStateCallback(stateCallback);
		rgTab.setOnCheckedChangeListener(checkedChangeListener);
		deviceHelper.addWorkStatusListener(workStatusListener);
	}


	@Override
	protected void initUI() {
		// TODO Auto-generated method stub
		super.initUI();
		device = (BleDevice) getIntent().getSerializableExtra("device");
		fragmentMgr = getFragmentManager();
		deviceFragment = new DeviceFragment();
		dataFragment = new DataFragment();
		controlFragment = new ControlFragment();
		settingFragment = new SettingFragment();
		reportFragment = new ReportFragment();
//		rbDevice.setChecked(true);
		ivBack.setVisibility(View.GONE);
//		ivBack.setImageResource(R.drawable.tab_btn_scenes_home);
		
		upgradeDialog = new ProgressDialog(this);
		upgradeDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置进度条的形式为圆形转动的进度条
		upgradeDialog.setMessage(getString(R.string.fireware_updateing, ""));
		upgradeDialog.setCancelable(false);
		upgradeDialog.setCanceledOnTouchOutside(false);
		showTab(TAB_DEVICE, null);
	}
	
	public void showTab(int tabIndex, HistoryData historyData) {
		if(this.tabIndex != tabIndex) {
			this.tabIndex = tabIndex;
			switch(tabIndex) {
			case TAB_DEVICE:
				rbDevice.setChecked(true);
				break;
			case TAB_DATA:{
				rbData.setChecked(true);
				break;
			}
			case TAB_REPORT:{
				rgTab.setTag("ok");
				rbReport.setChecked(true);
				FragmentTransaction trans = fragmentMgr.beginTransaction();
				Bundle bundle = new Bundle();
				bundle.putSerializable("historyData", historyData);
				reportFragment.setArguments(bundle);
				trans.replace(R.id.content, reportFragment);
				trans.commit();
				break;
			}
			}
		}
	}
	
	public void setTitle(int res){
		tvTitle.setText(res);
	}
	
	public void setDevice(BleDevice device) {
		this.device = device;
	}
	
	public BleDevice getDevice() {
		return device;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == ivBack){
			exit();
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	public void exit(){
		deviceHelper.disconnect();
		clearCache();
//		Intent intent = new Intent(this, SplashActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
		finish();
	}
	
	
	private void clearCache(){
		realtimeDataOpen = false;
		deviceName = null;
		deviceId = null;
		power = null;
		version = null;
		temp = null;
		hum = null;
	}
	
	private IConnectionStateCallback stateCallback = new IConnectionStateCallback() {
		@Override
		public void onStateChanged(IDeviceManager manager, CONNECTION_STATE state) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG+" onStateChanged state:" + state);
			if(state == CONNECTION_STATE.DISCONNECT){
				realtimeDataOpen = false;
			}
		}
	};
	
	
	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			Object tag = group.getTag();
			if(tag != null) {
				group.setTag(null);
				return;
			}
			FragmentTransaction trans = fragmentMgr.beginTransaction();
			if(checkedId == R.id.rb_device){
				trans.replace(R.id.content, deviceFragment);
			}else if(checkedId == R.id.rb_data){
				trans.replace(R.id.content, dataFragment);
			}else if(checkedId == R.id.rb_control){
				trans.replace(R.id.content, controlFragment);
			}else if(checkedId == R.id.rb_setting){
				trans.replace(R.id.content, settingFragment);
			}else if(checkedId == R.id.rb_report){
				trans.replace(R.id.content, reportFragment);
			}
			trans.commit();
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		SdkLog.log(TAG+" onActivityResult req:" + requestCode+",res:" + resultCode+",data:" + data);
		if(resultCode == RESULT_OK){
			
		}
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		deviceHelper.removeConnectionStateCallback(stateCallback);
		deviceHelper.removeWorkStatusListener(workStatusListener);
	}
	
	public void showUpgradeDialog(){
		upgradeDialog.show();
	}
	
	public void setUpgradeProgress(int progress) {
		if(upgradeDialog != null && upgradeDialog.isShowing()) {
			upgradeDialog.setProgress(progress);
		}
	}
	
	public void hideUpgradeDialog(){
		upgradeDialog.dismiss();
	}
	
	
	private void checkPermissions() {
		if(Build.VERSION.SDK_INT >= 23) {
			unauthoPersssions.clear();
			//逐个判断你要的权限是否已经通过
			for (int i = 0; i < permissions.length; i++) {
				if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
					unauthoPersssions.add(permissions[i]);//添加还未授予的权限
				}
			}
			//申请权限
			if (unauthoPersssions.size() > 0) {//有权限没有通过，需要申请
				ActivityCompat.requestPermissions(this, new String[]{unauthoPersssions.get(0)}, requestCode);
			}
		}
    }
	
	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.requestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    break;
                }
            }
        }
    }
	
	private WorkStatusListener workStatusListener = new WorkStatusListener() {
		@Override
		public void onWorkStatusChanged(final WorkStatus workStatus) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG+" onWorkStatusChanged:" + workStatus);
			MainActivity.workStatus = workStatus;
		}
	};
}








































