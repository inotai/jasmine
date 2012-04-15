package com.inotai.jasmine.value;

public enum StringType {
	DOUBLE_QUOTED(0, "DoubleQuoted"),
	SINGLE_QUOTED(1, "SingleQuoted"),
	UNQUOTED(2, "Unquoted");

	private final int value;
	private final String name;

	private StringType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	int getValue() {
		return this.value;
	}

	String getName() {
		return this.name;
	}

}