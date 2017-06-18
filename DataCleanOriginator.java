package com.srini.dataCleaner;

import java.io.Serializable;

interface IDataCleanOriginator {
	StagingInfo saveStaingInfo();

	void getSatgingInfo(StagingInfo info);
}

public class DataCleanOriginator implements Serializable, IDataCleanOriginator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String dbname;
	private String tablename;
	private String partitionInfo;

	@Override
	public StagingInfo saveStaingInfo() {
		return new StagingInfo(dbname, tablename, partitionInfo);
	}

	@Override
	public void getSatgingInfo(StagingInfo info) {
		dbname = info.getDbname();
		tablename = info.getTablename();
		partitionInfo = info.getPartitionInfo();
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public void setPartitionInfo(String partitionInfo) {
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
