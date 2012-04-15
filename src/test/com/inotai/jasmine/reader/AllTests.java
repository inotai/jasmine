package com.inotai.jasmine.reader;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(HelpersTest.class);
		suite.addTestSuite(SimpleValueParsingTest.class);
		suite.addTestSuite(CompositeValueParsingTest.class);
		suite.addTestSuite(ErrorHandlingTest.class);
		// $JUnit-END$
		return suite;
	}

}
