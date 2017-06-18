package com.srini.dataCleaner;

import java.io.Serializable;

public class StagingInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String dbname;
	private String tablename;
	private String partitionInfo;

	public StagingInfo(String dbname, String tablename, String partitionInfo) {
		this.dbname = dbname;
		this.tablename = tablename;
		this.partitionInfo = partitionInfo;
	}

	public String getDbname() {
		return dbname;
	}

	public String getTablename() {
		return tablename;
	}

	public String getPartitionInfo() {
		return partitionInfo;
	}
}
