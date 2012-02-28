package com.inotai.jasmine.value;

public enum ValueType {
	NULL(0, "Null"), BOOLEAN(1, "Boolean"), INTEGER(2, "Integer"), REAL(3,
			"Real"), STRING(4, "String"), REGEXP(5, "RegExp"), LIST(6, "List"), DICTIONARY(
			7, "Dictionary");

	private final int value;
	private final String name;

	private ValueType(int value, String name) {
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