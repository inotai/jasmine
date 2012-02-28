package com.inotai.jasmine.value;

public class RealValue extends Value {
	private double value;

	public RealValue(double real) {
		super(ValueType.REAL);
		this.value = real;
	}

	public double getAsReal() {
		return this.value;
	}

	public Object clone() {
		return new RealValue(this.value);
	}
}