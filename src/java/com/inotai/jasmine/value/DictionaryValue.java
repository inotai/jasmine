package com.inotai.jasmine.value;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DictionaryValue extends Value {
	private LinkedHashMap<String, Value> map;

	public DictionaryValue() {
		super(ValueType.DICTIONARY);
		this.map = new LinkedHashMap<String, Value>();
	}

	public int size() {
		return this.map.size();
	}

	public Iterator<Map.Entry<String, Value>> iterator() {
		return this.map.entrySet().iterator();
	}

	/**
	 * Adds a value to the map.
	 * 
	 * @param name
	 *            Name of the value
	 * @param value
	 *            Value
	 */
	public Value add(String name, Value value) {
		this.map.put(name, value);
		return value;
	}

	public Value get(String name) {
		if (this.map.containsKey(name)) {
			return this.map.get(name);
		}
		return null;
	}

	public boolean has(String name) {
		return this.map.containsKey(name);
	}

	public Value remove(String name) {
		if (this.map.containsKey(name)) {
			return this.map.remove(name);
		}
		return null;
	}

	public Object clone() {
		DictionaryValue dictValue = new DictionaryValue();
		for (Iterator<Map.Entry<String, Value>> it = this.iterator(); it
				.hasNext();) {
			Map.Entry<String, Value> pair = (Map.Entry<String, Value>) it
					.next();
			dictValue.add(pair.getKey(), (Value) pair.getValue().clone());
		}
		return dictValue;
	}
}