package com.srini.dataCleaner;

public class Demo {
	public static void main(String[] args) {
		DataCleanOriginator originator = new DataCleanOriginator();
		DataCleanTracker tracker = new DataCleanTracker();
		originator.setDbname("a");
		originator.setPartitionInfo("asdas");
		originator.setTablename("1");
		tracker.add(originator.saveStaingInfo());
		System.out.println(tracker.get(0).getDbname());
		originator.setDbname("b");
		tracker.add(originator.saveStaingInfo());
		System.out.println(tracker.get(1).getPartitionInfo());
	}
}
