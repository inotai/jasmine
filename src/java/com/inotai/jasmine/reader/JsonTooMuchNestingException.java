package com.inotai.jasmine.reader;

import com.inotai.jasmine.JasmineReader.LinePos;

public class JsonTooMuchNestingException extends RuntimeException {

	private static final long serialVersionUID = 5591163906443376215L;

	private LinePos begin;

	private LinePos end;

	public JsonTooMuchNestingException(LinePos begin, LinePos end) {
		super();
		this.begin = begin;
		this.end = end;
	}

	public LinePos getBegin() {
		return begin;
	}

	public LinePos getEnd() {
		return end;
	}

}
