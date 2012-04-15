package com.inotai.jasmine.reader.error;

public class ParserException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 678596718308179706L;

	public ParserException() {
		super("Unknown error");
	}

	public ParserException(int line, int col) {
		this("Error found", line, col);
	}

	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, int line, int col) {
		super(message + " at line" + line + " column " + col);
	}

}
