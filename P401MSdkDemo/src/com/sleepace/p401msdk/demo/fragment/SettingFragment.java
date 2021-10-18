package com.sleepace.p401msdk.demo.fragment;

import com.sleepace.p401msdk.demo.AlarmListActivity;
import com.sleepace.p401msdk.demo.MainActivity;
import com.sleepace.p401msdk.demo.MusicSettingActivity;
import com.sleepace.p401msdk.demo.R;
import com.sleepace.sdk.util.SdkLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingFragment extends BaseFragment {
	
	private View vMusic, vAlarm;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_setting, null);
		// SdkLog.log(TAG+" onCreateView-----------");
		findView(view);
		initListener();
		initUI();
		return view;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		vMusic = root.findViewById(R.id.tv_music);
		vAlarm = root.findViewById(R.id.tv_alarm);
	}

	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		vMusic.setOnClickListener(this);
		vAlarm.setOnClickListener(this);
	}

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.setting);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SdkLog.log(TAG + " onResume realtimeDataOpen:" + MainActivity.realtimeDataOpen);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		SdkLog.log(TAG + " onDestroyView----------------");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if(v == vMusic) {
			Intent intent = new Intent(mActivity, MusicSettingActivity.class);
			startActivity(intent);
		}else if(v == vAlarm) {
			Intent intent = new Intent(mActivity, AlarmListActivity.class);
			startActivity(intent);
		}
	}

}
