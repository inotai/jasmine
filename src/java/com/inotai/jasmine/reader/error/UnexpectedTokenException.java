package com.inotai.jasmine.reader.error;

import com.inotai.jasmine.reader.LinePos;

public class UnexpectedTokenException extends ParserException {

	private static final long serialVersionUID = 5537905344299790737L;

	public UnexpectedTokenException(LinePos pos) {
		super("Unexpected token", pos);
	}

}
