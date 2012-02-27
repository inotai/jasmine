package com.inotai.jasmine;

class JasmineStringUtils {
    static boolean doubleQuotedEscapeChar(char c, StringBuilder builder) {
        // WARNING: if you add a new case here, you need to update the reader as well.
        // Note: \v is in the reader, but not here since the JSON spec doesn't
        // allow it.

        switch (c) {
            case '\b':
                builder.append("\\b");
                break;
            case '\f':
                builder.append("\\f");
                break;
            case '\n':
                builder.append("\\n");
                break;
            case '\r':
                builder.append("\\r");
                break;
            case '\t':
                builder.append("\\t");
                break;
            case '\\':
                builder.append("\\\\");
                break;
            case '\"':
                builder.append("\\\"");
                break;
            default:
                return false;
        }

        return true;
    }

    static public boolean isJasmineUnquotedCharacter(char c) {
        if (
                (c >= 'a' && c <= 'z') ||
                        (c >= 'A' && c <= 'Z') ||
                        (c >= '0' && c <= '9') ||
                        (c == '-' || c == '_' || c == '.')
                ) {
            return true;
        }
        return false;
    }

    static public void appendDoubleQuotedCharacterJSON(char c, StringBuilder builder) {

        if (doubleQuotedEscapeChar(c, builder) == false) {
            if (c < 32 || c > 126 || c == '<' || c == '>') {
                builder.append(String.format("\\u%04X", ((int) c)));
            } else {
                builder.append(c);
            }
        }
    }

    static public void appendDoubleQuotedCharacterJasmine(char c, StringBuilder builder) {
        if (doubleQuotedEscapeChar(c, builder) == false) {
            if (c < 32) {
                builder.append(String.format("\\u%04X", ((int) c)));
            } else {
                builder.append(c);
            }
        }
    }

    static public void appendSingleQuotedCharacterJasmine(char c, StringBuilder builder) {
        switch (c) {
            case '\'':
                builder.append("''");
                return;

            // Capture these before we run into uXXXX encoding
            case '\b':
            case '\f':
            case '\n':
            case '\r':
            case '\t':
                builder.append(c);
                return;
        }

        if (c < 32) {
            builder.append(String.format("\\u%04X", ((int) c)));
        } else {
            builder.append(c);
        }
    }

    static public void writeStringDoubleQuoted(String s, StringBuilder builder) {
        builder.append('"');
        for (int i = 0; i < s.length(); i++) {
            appendDoubleQuotedCharacterJasmine((char) s.charAt(i), builder);
        }
        builder.append('"');
    }

    static public void writeStringSingleQuoted(String s, StringBuilder builder) {
        builder.append("'");
        for (int i = 0; i < s.length(); i++) {
            appendSingleQuotedCharacterJasmine((char) s.charAt(i), builder);
        }
        builder.append("'");
    }


    static public void writeStringUnquoted(String s, StringBuilder builder) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isJasmineUnquotedCharacter(c)) {
                builder.append(c);
            }
        }
    }

    static public void writeRegEx(String regEx, String options, StringBuilder builder) {
        builder.append("/");
        for (int i = 0; i < regEx.length(); i++) {
            char c = regEx.charAt(i);
            if (c == '/') {
                builder.append("\\/");
            } else {
                builder.append(c);
            }
        }
        builder.append("/");

        builder.append(options);
    }

}