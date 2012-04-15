package com.inotai.jasmine.reader.error;

public class InvalidEscapeInStringException extends ParserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1080782564461115779L;

	public InvalidEscapeInStringException(char escape, int line, int col) {
		super("Invalid escape '" + escape + "' in string", line, col);
	}

}
