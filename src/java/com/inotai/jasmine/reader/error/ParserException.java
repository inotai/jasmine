package com.inotai.jasmine.reader.error;

import com.inotai.jasmine.reader.LinePos;

public class ParserException extends RuntimeException {

	private static final long serialVersionUID = 678596718308179706L;

	public ParserException() {
		this("Unknown error");
	}

	public ParserException(LinePos pos) {
		this("Error found", pos);
	}

	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, LinePos pos) {
		super(message + " at line " + pos.line + " column " + pos.column);
	}

}
