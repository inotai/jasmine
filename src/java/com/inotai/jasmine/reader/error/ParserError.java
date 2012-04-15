package com.inotai.jasmine.reader.error;

public class ParserError extends RuntimeException {

	private static final long serialVersionUID = -4277203841762793860L;

	public ParserError(String message) {
		super(message);
	}

	public ParserError(String message, Throwable cause) {
		super(message, cause);
	}

}
