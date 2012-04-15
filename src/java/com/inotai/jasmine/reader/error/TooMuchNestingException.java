package com.inotai.jasmine.reader.error;

import com.inotai.jasmine.reader.LinePos;

public class TooMuchNestingException extends ParserException {

	private static final long serialVersionUID = 5591163906443376215L;

	public TooMuchNestingException(LinePos pos) {
		super("Too much nesting", pos);
	}

}
