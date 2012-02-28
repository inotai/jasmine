package com.inotai.jasmine.value;

public class Value implements Cloneable {

	private ValueType type;

	public Value() {
		this(ValueType.NULL);
	}

	protected Value(ValueType type) {
		this.type = type;
	}

	public ValueType getType() {
		return this.type;
	}

	public boolean getAsBoolean() {
		throw new ValueException("Value is not boolean.");
	}

	public int getAsInteger() {
		throw new ValueException("Value is not integer number.");
	}

	public double getAsReal() {
		throw new ValueException("Value is not real number.");
	}

	public String getAsString() {
		throw new ValueException("Value is not string.");
	}

	public String getAsRegExp() {
		throw new ValueException("Value is not regexp.");
	}

	public Object clone() {
		return new Value(this.type);
	}
}