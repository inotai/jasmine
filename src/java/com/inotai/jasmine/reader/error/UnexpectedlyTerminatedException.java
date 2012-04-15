package com.inotai.jasmine.reader.error;

public class UnexpectedlyTerminatedException extends ParserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5065074367071040120L;

	public UnexpectedlyTerminatedException() {
		super("Unexpected end of file.");
	}

	public UnexpectedlyTerminatedException(int line, int col) {
		super("Unexpected end of token", line, col);
	}

}
