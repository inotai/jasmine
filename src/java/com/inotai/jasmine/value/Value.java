package com.inotai.jasmine.value;

public class Value implements Cloneable {

	public static Value createIntegerValue(int v) {
		return new IntegerValue(v);
	}

	public static Value createRealValue(double v) {
		return new RealValue(v);
	}

	public static Value createStringValue(CharSequence str) {
		return new StringValue(str.toString());
	}

	public static Value createStringValue(CharSequence str, StringType type) {
		return new StringValue(str.toString(), type);
	}

	public static Value createRegExpValue(String exp, String opts) {
		return new RegExpValue(exp, opts);
	}

	private static final Value NULL = new Value(ValueType.NULL);

	public static Value createNullValue() {
		return NULL;
	}

	public static Value createBooleanValue(boolean v) {
		if (v) {
			return BooleanValue.TRUE;
		} else {
			return BooleanValue.FALSE;
		}
	}

	public static Value createListValue() {
		return new ListValue();
	}

	public static Value createDictionaryValue() {
		return new DictionaryValue();
	}

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