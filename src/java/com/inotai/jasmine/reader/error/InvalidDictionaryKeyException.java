package com.inotai.jasmine.reader.error;

public class InvalidDictionaryKeyException extends ParserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4516916823558327559L;

	public InvalidDictionaryKeyException(int line, int col) {
		super("Invalid dictionary key", line, col);
	}

}
