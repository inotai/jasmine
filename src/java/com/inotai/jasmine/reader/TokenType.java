package com.inotai.jasmine.reader;

public enum TokenType {
	T_ObjectBegin, // {
	T_ObjectEnd, // }
	T_ArrayBegin, // [
	T_ArrayEnd, // ]
	T_StringDoubleQuoted,
	T_Number,
	T_BoolTrue, // true
	T_BoolFalse, // false
	T_Null, // null
	T_ListSeparator, // ,
	T_ObjectPairSeparator, // :
	T_EndOfInput,
	T_Invalid,

	// Extended
	T_RegExp, // /.../...
	T_StringSingleQuoted,
	T_Symbol
}
