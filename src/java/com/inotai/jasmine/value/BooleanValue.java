package com.inotai.jasmine.value;

public class BooleanValue extends Value {
	private boolean value;

	public BooleanValue(boolean booleanValue) {
		super(ValueType.BOOLEAN);
		this.value = booleanValue;
	}

	public boolean getAsBoolean() {
		return this.value;
	}

	public Object clone() {
		return new BooleanValue(this.value);
	}
}