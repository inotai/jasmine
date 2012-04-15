package com.inotai.jasmine.reader;

public class Flags {

	private boolean comma_trailing = false;
	private boolean comma_omit = false;
	private boolean reg_exp = false;
	private boolean symbols = false;
	private boolean string_single_quoted = false;
	private boolean string_line_endings_unify = false;
	private LineEnding string_line_endings = LineEnding.T_LF;

	// ------------------------------------------------------------------------
	// / Returns flags for JSON strict parser
	public static Flags getJsonStrict()
	// ------------------------------------------------------------------------
	{
		return new Flags();
	}

	// ------------------------------------------------------------------------
	// / Returns flags for JSON loose parser
	public static Flags getJsonLoose()
	// ------------------------------------------------------------------------
	{
		Flags flags = new Flags();
		flags.comma_trailing = true;
		return flags;
	}

	// ------------------------------------------------------------------------
	// / Returns flags for Jasmine parser
	public static Flags getJasmine()
	// ------------------------------------------------------------------------
	{
		Flags flags = new Flags();
		flags.comma_trailing = true;
		flags.comma_omit = true;
		flags.reg_exp = true;
		flags.symbols = true;
		flags.string_single_quoted = true;
		flags.string_line_endings_unify = true;
		flags.string_line_endings = LineEnding.T_LF;

		return flags;
	}

	public boolean isComma_trailing() {
		return comma_trailing;
	}

	public boolean isComma_omit() {
		return comma_omit;
	}

	public boolean isReg_exp() {
		return reg_exp;
	}

	public boolean isSymbols() {
		return symbols;
	}

	public boolean isString_single_quoted() {
		return string_single_quoted;
	}

	public boolean isString_line_endings_unify() {
		return string_line_endings_unify;
	}

	public LineEnding getString_line_endings() {
		return string_line_endings;
	}

}
