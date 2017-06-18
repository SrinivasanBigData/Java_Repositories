package com.srini.dataCleaner;

import java.io.Serializable;

import com.srini.s1.DataCleanerContext.StagingInfo;

public class DataCleanerContext implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StagingInfo info;

	public DataCleanerContext(StagingInfo info) {
		this.info = info;
	}

	public StagingInfo getInfo() {
		return info;
	}
}
