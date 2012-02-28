package com.inotai.jasmine.value;

import java.util.ArrayList;

public class ListValue extends Value {
	ArrayList<Value> list;

	public ListValue() {
		super(ValueType.LIST);
		this.list = new ArrayList<Value>();
	}

	public int size() {
		return this.list.size();
	}

	public Value add(Value value) {
		this.list.add(value);
		return value;
	}

	public Value remove(int index) {
		return this.list.remove(index);
	}

	public Value get(int index) {
		return this.list.get(index);
	}

	public Object clone() {
		ListValue listValue = new ListValue();
		for (int i = 0; i < this.size(); i++) {
			listValue.add((Value) this.get(i).clone());
		}

		return listValue;
	}
}