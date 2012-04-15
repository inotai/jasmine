package com.inotai.jasmine.reader.error;

import com.inotai.jasmine.reader.LinePos;

public class UnexpectedlyTerminatedException extends ParserException {

	private static final long serialVersionUID = -5065074367071040120L;

	public UnexpectedlyTerminatedException() {
		super("Unexpected end of file");
	}

	public UnexpectedlyTerminatedException(LinePos pos) {
		super("Unexpected end of token", pos);
	}

}
