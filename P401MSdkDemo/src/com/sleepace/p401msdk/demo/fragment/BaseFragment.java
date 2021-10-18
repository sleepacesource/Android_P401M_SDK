package com.sleepace.p401msdk.demo.fragment;

import com.sleepace.p401msdk.demo.MainActivity;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.p401m.P401MHelper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment implements OnClickListener{
	
	protected String TAG = getClass().getSimpleName();
	protected MainActivity mActivity;
	private P401MHelper deviceHelper;
	private boolean isFragmentVisible;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = (MainActivity) getActivity();
		deviceHelper = P401MHelper.getInstance(mActivity);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setFragmentVisible(true);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		setFragmentVisible(false);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
	    super.onHiddenChanged(hidden);
		if(hidden){
			 setFragmentVisible(false);
		} else {
			setFragmentVisible(true);
		}
	}

	public P401MHelper getDeviceHelper() {
		return deviceHelper;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
	}


	protected void initListener() {
		// TODO Auto-generated method stub
	}


	protected void initUI() {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	public void printLog(int strRes){
		
	}
	
	public boolean checkStatus(CallbackData cd){
		return mActivity.checkStatus(cd);
	}

	public boolean isFragmentVisible() {
		if(isAdded()) {
			return isFragmentVisible;
		}
		return false;
	}

	public void setFragmentVisible(boolean isFragmentVisible) {
		this.isFragmentVisible = isFragmentVisible;
	}
	
	
	public void showLoading() {
		if(isAdded()) {
			mActivity.showLoading();
		}
	}
	
	public void updateLoadingMsg(String msg) {
		if(isAdded()) {
			mActivity.updateLoadingMsg(msg);
		}
	}
	
	public void hideLoading() {
		if(isAdded()) {
			mActivity.hideLoading();
		}
	}
	
	
}



