package com.inotai.jasmine;

import com.inotai.jasmine.value.*;

import java.io.InputStream;
import java.io.InputStreamReader;
/*
    class JasmineReader
	{
		public enum ErrorCode
		{
			NO_ERROR( 0 ),
			UNEXPECTED_DATA_AFTER_ROOT( 1 ),
			SYNTAX_ERROR( 2 );
			
			private int value;
			
			ErrorCode( int value ) {
				this.value = value;
			}
		}
		
		public class Token
		{
			enum Type
			{
				OBJECT_BEGIN	( 0 ),
				OBJECT_END		( 1 ),
				ARRAY_BEGIN		( 2 ),
				ARRAY_END		( 3 ),
				STRING_DOUBLE_QUOTED (4),
				NUMBER			( 5 ),
				BOOL_TRUE		( 6 ),
				BOOL_FALSE		( 7 ),
				NULL			( 8 ),
				LIST_SEPARATOR	( 9 ),
				OBJECT_PAIR_SEPARATOR (10),
				END_OF_INPUT	( 11 ),
				INVALID			( 12 ),
				REG_EXP			( 13 ),
				STRING_SINGLE_QUOTED (14),
				SYMBOL			( 15 );

				private int value;
				Type( int value )
				{
					this.value = value;
				}
			}

			public Type type;

			Token( Type type ) {
				this.type = type;
				this.string = 0;
			}
		}

		private int stackDepth;
		private InputStreamReader reader;
		private ErrorCode errorCode;
		
		JasmineReader() {
		}
		
		public Value read( InputStream stream ) {
			this.reader = new InputStreamReader( reader );
			this.stackDepth = 0;
			this.errorCode = ErrorCode.NO_ERROR;
			
			Token token;
			try {
				Value root = buildValue();
			} finally {
				
			} 
		}
		
	}
*/