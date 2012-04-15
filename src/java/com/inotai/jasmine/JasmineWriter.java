package com.inotai.jasmine;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import com.inotai.jasmine.value.DictionaryValue;
import com.inotai.jasmine.value.ListValue;
import com.inotai.jasmine.value.RegExpValue;
import com.inotai.jasmine.value.StringValue;
import com.inotai.jasmine.value.Value;

public class JasmineWriter {

	protected JasmineFormatter formatter;
	protected Stack<Value> stack;

	public JasmineWriter(JasmineFormatter formatter) {
		this.formatter = formatter == null ? new JasmineFormatter() : formatter;
		this.stack = new Stack<Value>();
	}

	public void write(Value value, StringBuilder builder) {
		this.stack.push(value);

		switch (value.getType()) {
		case NULL:
			this.formatter.beforeValue(this.stack, builder);
			builder.append("null");
			this.formatter.afterValue(this.stack, builder);
			break;

		case BOOLEAN:
			this.formatter.beforeValue(this.stack, builder);
			builder.append(value.getAsBoolean() == true ? "true" : "false");
			this.formatter.afterValue(this.stack, builder);
			break;

		case INTEGER:
			this.formatter.beforeValue(this.stack, builder);
			builder.append(value.getAsInteger());
			this.formatter.afterValue(this.stack, builder);
			break;

		case REAL:
			this.formatter.beforeValue(this.stack, builder);
			builder.append(value.getAsReal());
			this.formatter.afterValue(this.stack, builder);
			break;

		case STRING:
			this.formatter.beforeValue(this.stack, builder);
			this.writeString((StringValue) value, builder);
			this.formatter.afterValue(this.stack, builder);
			break;

		case REGEXP:
			this.formatter.beforeValue(this.stack, builder);
			RegExpValue reValue = (RegExpValue) value;
			JasmineStringUtils.writeRegEx(reValue.getRegExp(),
					reValue.getRegExpOptions(), builder);
			this.formatter.afterValue(this.stack, builder);
			break;

		case LIST:
			this.writeList((ListValue) value, builder);
			break;

		case DICTIONARY:
			this.writeDictionary((DictionaryValue) value, builder);
			break;
		}

		this.stack.pop();
	}

	protected void writeString(StringValue value, StringBuilder builder) {
		switch (value.getStringType()) {

		case DOUBLE_QUOTED:
			JasmineStringUtils.writeStringDoubleQuoted(value.getAsString(),
					builder);
			break;

		case SINGLE_QUOTED:
			JasmineStringUtils.writeStringSingleQuoted(value.getAsString(),
					builder);
			break;

		case UNQUOTED:
			JasmineStringUtils
					.writeStringUnquoted(value.getAsString(), builder);
			break;

		}
	}

	public void writeList(ListValue value, StringBuilder builder) {
		builder.append('[');
		this.formatter.beforeValue(this.stack, builder);

		int last = value.size() - 1;
		for (int i = 0; i < value.size(); i++) {
			Value v = value.get(i);
			this.formatter.beforeListChild(this.stack, v, i, last, builder);
			this.write(v, builder);
			this.formatter.afterListChild(this.stack, v, i, last, builder);
		}

		this.formatter.afterValue(this.stack, builder);
		builder.append(']');
	}

	public void writeDictionary(DictionaryValue value, StringBuilder builder) {
		builder.append('{');
		this.formatter.beforeValue(this.stack, builder);
		DictionaryValue dictionaryValue = value;
		int last = dictionaryValue.size() - 1;
		int i = 0;
		for (Iterator<Map.Entry<String, Value>> it = dictionaryValue.iterator(); it
				.hasNext();) {
			Map.Entry<String, Value> pair = it.next();

			this.formatter.beforeDictionaryKey(this.stack, pair.getValue(),
					pair.getKey(), i, last, builder);
			JasmineStringUtils.writeStringUnquoted(pair.getKey(), builder);
			this.formatter.afterDictionaryKey(this.stack, pair.getValue(),
					pair.getKey(), i, last, builder);
			builder.append(':');

			this.formatter.beforeDictionaryChild(this.stack, pair.getValue(),
					pair.getKey(), i, last, builder);
			this.write(pair.getValue(), builder);
			this.formatter.afterDictionaryChild(this.stack, pair.getValue(),
					pair.getKey(), i, last, builder);
			i++;
		}
		this.formatter.afterValue(this.stack, builder);
		builder.append('}');
	}
}