package com.orc.projectcollector.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ProjectCollectorAllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for project collectors from different platforms");
		//$JUnit-BEGIN$		
		suite.addTestSuite(GithubProjectCollectorTest.class);
		suite.addTestSuite(GithubProjectDetailsCollectorTest.class);
		suite.addTestSuite(CodeplexProjectCollectorTest.class);
		suite.addTestSuite(CodeplexProjectDetailsCollectorTest.class);
		//$JUnit-END$
		return suite;
	}

}
