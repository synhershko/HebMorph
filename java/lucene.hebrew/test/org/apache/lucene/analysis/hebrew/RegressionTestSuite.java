package org.apache.lucene.analysis.hebrew;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A test suites that includes all hebmorph-analyzer tests
 * @author itaifrenkel
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	BasicHebrewTests.class, TermPositionVectorTest.class
})
public class RegressionTestSuite {

}
