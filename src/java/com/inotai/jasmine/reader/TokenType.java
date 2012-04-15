package com.inotai.jasmine.reader;

public enum TokenType {
	OBJECT_BEGIN, // {
	OBJECT_END, // }
	ARRAY_BEGIN, // [
	ARRAY_END, // ]
	STRING_DOUBLE_QUOTED, // "xxx"
	STRING_SINGLE_QUOTED, // 'xxx'
	SYMBOL, // xxx
	NUMBER, // 1234
	BOOL_TRUE, // true
	BOOL_FALSE, // false
	NULL, // null
	LIST_SEPARATOR, // ,
	OBJECT_PAIR_SEPARATOR, // :
	END_OF_INPUT,
	INVALID,
	REG_EXP // /.../...
}
