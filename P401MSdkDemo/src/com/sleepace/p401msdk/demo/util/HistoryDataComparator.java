package com.sleepace.p401msdk.demo.util;

import java.util.Comparator;

import com.sleepace.sdk.p401m.domain.HistoryData;

public class HistoryDataComparator implements Comparator<HistoryData> {

	@Override
	public int compare(HistoryData lhs, HistoryData rhs) {
		// TODO Auto-generated method stub
		if(lhs.getSummary().getStartTime() < rhs.getSummary().getStartTime()){
			return 1;
		}else if(lhs.getSummary().getStartTime() > rhs.getSummary().getStartTime()){
			return -1;
		}
		return 0;
	}

}
