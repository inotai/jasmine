package com.inotai.jasmine.reader.error;

import com.inotai.jasmine.reader.LinePos;

public class UnexpectedDataAfterRootException extends ParserException {

	private static final long serialVersionUID = -4898320547470591965L;

	public UnexpectedDataAfterRootException(LinePos pos) {
		super("Uexpexted data afet root", pos);
	}

}
