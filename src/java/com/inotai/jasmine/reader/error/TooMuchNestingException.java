package com.inotai.jasmine.reader.error;

public class TooMuchNestingException extends ParserException {

	private static final long serialVersionUID = 5591163906443376215L;

	public TooMuchNestingException(int line, int col) {
		super("Too much nesting", line, col);
	}

}
