package com.inotai.jasmine.reader.error;

import com.inotai.jasmine.reader.LinePos;

public class InvalidDictionaryKeyException extends ParserException {

	private static final long serialVersionUID = -4516916823558327559L;

	public InvalidDictionaryKeyException(LinePos pos) {
		super("Invalid dictionary key", pos);
	}

}
