package com.inotai.jasmine.value;

public class BooleanValue extends Value {

	public static final BooleanValue TRUE = new BooleanValue(true);

	public static final BooleanValue FALSE = new BooleanValue(false);

	private final boolean value;

	private BooleanValue(boolean booleanValue) {
		super(ValueType.BOOLEAN);
		this.value = booleanValue;
	}

	public boolean getAsBoolean() {
		return this.value;
	}

	public Object clone() {
		return this;
	}
}