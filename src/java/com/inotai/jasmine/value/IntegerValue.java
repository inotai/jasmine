package com.inotai.jasmine.value;

public class IntegerValue extends Value {

	private int value;

	public IntegerValue(int integer) {
		super(ValueType.INTEGER);
		this.value = integer;
	}

	public int getAsInteger() {
		return this.value;
	}

	public Object clone() {
		return new IntegerValue(this.value);
	}
}