package com.inotai.jasmine.value;

public class StringValue extends Value {
    private String value;
    private StringType stringType;

    public StringValue(String string) {
        this(string, StringType.DOUBLE_QUOTED);
    }

    public StringValue(String string, StringType stringType) {
        super(ValueType.STRING);
        this.value = string;
        this.stringType = stringType;
    }


    public String getAsString() {
        return this.value;
    }

    public StringType getStringType() {
        return this.stringType;
    }

    public Object clone() {
        return new StringValue( this.value, this.stringType );
    }
}