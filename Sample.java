package com.srini.builder;

public class SampleTest {

	private static Table table;

	private SampleTest() {
		table = new Table();
	}

	public static SampleTest name(String name) {

		return new SampleTest();
	}

	public SampleTest as(String name) {

		return this;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	private static class Table {

	}
}
