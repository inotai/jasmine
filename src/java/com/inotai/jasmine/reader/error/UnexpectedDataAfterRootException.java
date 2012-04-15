package com.inotai.jasmine.reader.error;

public class UnexpectedDataAfterRootException extends ParserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4898320547470591965L;

	public UnexpectedDataAfterRootException(int line, int col) {
		super("Uexpexted data afet root", line, col);
	}

}
