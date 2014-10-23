package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.RadixTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A test suites that includes all hebmorph tests
 *
 * @author itaifrenkel
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        LemmatizerTest.class, RadixTest.class, TokenizerTest.class, StreamLemmatizerTest.class
})
public class RegressionTestSuite {

}


