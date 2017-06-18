package com.srini.dataCleaner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface IDataCleanTracker {
	void add(StagingInfo info);

	StagingInfo get(int index);
}

public class DataCleanTracker implements Serializable, IDataCleanTracker {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<StagingInfo> infos = new ArrayList<>();

	@Override
	public void add(StagingInfo info) {
		infos.add(info);
	}

	@Override
	public StagingInfo get(int index) {
		return infos.get(index);
	}

}
