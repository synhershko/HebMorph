/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.queryparsers.HebrewQueryParserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A test suites that includes all hebmorph-analyzer tests
 *
 * @author itaifrenkel
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BasicHebrewTest.class, TermPositionVectorTest.class, HebrewQueryParserTest.class
})
public class RegressionTestSuite {

}
