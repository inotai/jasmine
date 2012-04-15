package com.inotai.jasmine.reader.error;

public class UnexpectedTokenException extends ParserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5537905344299790737L;

	public UnexpectedTokenException(int line, int col) {
		super("Unexpected token.", line, col);
	}

}
