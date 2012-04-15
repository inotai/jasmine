package com.inotai.jasmine;

import java.util.Stack;

import com.inotai.jasmine.value.Value;
import com.inotai.jasmine.value.ValueType;

public class JasminePrettyFormatter extends JasmineFormatter {

	protected int indent;
	protected int line;

	public JasminePrettyFormatter() {
		this.indent = 0;
		this.line = 0;
	}

	protected void newLine(StringBuilder builder) {
		builder.append('\n');
		for (int i = 0; i < this.indent; i++) {
			builder.append('\t');
		}
		this.line++;
	}

	public void beforeValue(Stack<Value> stack, StringBuilder builder) {
		switch (stack.peek().getType()) {
		case DICTIONARY:
		case LIST:
			this.indent++;
			break;
		}
	}

	public void afterValue(Stack<Value> stack, StringBuilder builder) {
		switch (stack.peek().getType()) {
		case DICTIONARY:
		case LIST:
			this.indent--;
			this.newLine(builder);
			break;
		}
	}

	public void beforeListChild(Stack<Value> stack, Value value, int index,
			int last, StringBuilder builder) {
		this.newLine(builder);
	}

	public void afterListChild(Stack<Value> stack, Value value, int index,
			int last, StringBuilder builder) {

	}

	public void beforeDictionaryKey(Stack<Value> stack, Value value,
			String key, int index, int last, StringBuilder builder) {
		this.newLine(builder);
	}

	public void afterDictionaryKey(Stack<Value> stack, Value value, String key,
			int index, int last, StringBuilder builder) {
	}

	public void beforeDictionaryChild(Stack<Value> stack, Value value,
			String key, int index, int last, StringBuilder builder) {
		if (value.getType() == ValueType.DICTIONARY
				|| value.getType() == ValueType.LIST) {
			this.newLine(builder);
		} else {
			builder.append(' ');
		}
	}

	public void afterDictionaryChild(Stack<Value> stack, Value value,
			String key, int index, int last, StringBuilder builder) {
		// Nothing here
	}
}