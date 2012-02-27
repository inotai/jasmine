package com.inotai.jasmine.value;

public class RegExpValue extends Value {
    private String value;
    private String options;

    public RegExpValue(String regExp, String regExpOptions) {
        super(ValueType.REGEXP);
        this.value = regExp;
        this.options = regExpOptions;
    }

    public String getRegExp() {
        return this.value;
    }

    public String getRegExpOptions() {
        return this.options;
    }

    public Object clone() {
        return new RegExpValue( this.value, this.options );
    }
}