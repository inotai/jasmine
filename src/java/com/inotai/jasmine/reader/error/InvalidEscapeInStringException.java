package com.inotai.jasmine.reader.error;

import com.inotai.jasmine.reader.LinePos;

public class InvalidEscapeInStringException extends ParserException {

	private static final long serialVersionUID = 1080782564461115779L;

	public InvalidEscapeInStringException(char escape, LinePos pos) {
		super("Invalid escape '" + escape + "' in string", pos);
	}

}
