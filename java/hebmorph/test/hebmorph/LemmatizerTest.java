/**************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com>							   *
 *                                                                         *
 *   Distributed under the GNU General Public License, Version 2.0.        *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation (v2).                                    *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Steet, Fifth Floor, Boston, MA  02111-1307, USA.          *
 **************************************************************************/
package hebmorph;

import static org.junit.Assert.fail;
import hebmorph.datastructures.DictRadix;
import hebmorph.hspell.Loader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class LemmatizerTest
{	
    private static DictRadix<MorphData> m_dict;
    private static StreamLemmatizer m_lemmatizer;
	
    // override default with an "hspellPath" system property or "-DhspellPath" jvm argument.
    private static final String DEFAULT_HSPELL_PATH = "hspell-data-files";
	private static String hspellPath;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		hspellPath = System.getProperty("hspellPath",DEFAULT_HSPELL_PATH);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Ignore
	public void testGetIsInitialized()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testLemmatizer() throws IOException
	{
		String text = "רפאל ולדן הוא פרופסור לרפואה ישראלי, מלמד באוניברסיטת תל אביב, סגן מנהל בית החולים שיבא ופעיל חברתי. מתמחה בכירוגיה כללית ובכלי דם."+
						"ולדן נולד בצרפת ועלה לישראל בגיל 9. הוא שימש בבית החולים שיבא כמנהל האגף לכירורגיה ומנהל היחידה לכלי דם."+
						"ולדן פעיל וחבר בהנהלה בעמותת רופאים לזכויות אדם וכמו כן חבר בהנהלת ארגון לתת. ולדן זכה באות לגיון הכבוד הצרפתי (Légion d'Honneur) של ממשלת צרפת בזכות על פעילותו במסגרת רופאים לזכויות אדם לקידום שיתוף הפעולה בין פלסטינים לישראלים. האות הוענק לו על ידי שר החוץ של צרפת, ברנאר קושנר, בטקס בשגרירות צרפת בתל אביב."+
						"נשוי לבלשנית צביה ולדן, בתו של שמעון פרס והוא משמש כרופאו האישי של פרס.";
		//StringReader reader = new StringReader("להישרדות בהישרדות ההישרדות מהישרדות ניסיון הניסיון הביטוח  בביטוח לביטוח שביטוח מביטוחים");
		int expectedNumberOfNonHebrewWords = 0;
		StringReader reader = new StringReader(text);
        DictRadix<MorphData> dict = Loader.loadDictionaryFromDefaultClasspath(true);
		m_lemmatizer = new StreamLemmatizer(reader, dict, false);

        String word = "";
        List<Token> tokens = new ArrayList<Token>();
        while (m_lemmatizer.getLemmatizeNextToken(new Reference<String>(word), tokens) > 0)
        {
            if (tokens.size() == 0)
            {
                System.out.println(word+" Unrecognized word");
                continue;
            }

            if ((tokens.size() == 1) && !(tokens.get(0) instanceof HebrewToken))
            {
            	System.out.println(String.format("%s Not a Hebrew word; detected as %s", word, tokens.get(0).isNumeric() ? "Numeric" : "NonHebrew"));
            	
            	if (!tokens.get(0).isNumeric() && !word.isEmpty()) {
            		expectedNumberOfNonHebrewWords--;
            		Assert.assertTrue("Wrong number of non hebrew words. Check encoding issues.",expectedNumberOfNonHebrewWords>=0);
            	}
            	
                continue;
            }

            int curPrefix = -1;
            String curWord = "";
            for(Token  r : tokens)
            {
                if (!(r instanceof HebrewToken))
                    continue;
                HebrewToken ht = (HebrewToken)r;

                if ((curPrefix != ht.getPrefixLength()) || !curWord.equals(ht.getText()))
                {
                    curPrefix = ht.getPrefixLength();
                    curWord = ht.getText();
                    if (curPrefix == 0)
                    	System.out.println(String.format("Legal word: %s (score: %f)", ht.getText(), ht.getScore()));
                    else
                    {
                    	System.out.println(String.format("Legal combination: %s+%s (score: %f)", ht.getText().substring(0, curPrefix),ht.getText().substring(curPrefix), ht.getScore()));
                    }
                }
                System.out.println(ht.toString());
            }
        }


	}

	@Ignore
	public void testLemmatizerStringBooleanBoolean()
	{
		fail("Not yet implemented");
	}

	@Ignore
	public void testInitFromHSpellFolder()
	{
		fail("Not yet implemented");
	}

	@Ignore
	public void testIsLegalPrefix()
	{
		fail("Not yet implemented");
	}

	@Ignore
	public void testTryStrippingPrefix()
	{
		fail("Not yet implemented");
	}

	@Ignore
	public void testRemoveNiqqud()
	{
		fail("Not yet implemented");
	}

	@Ignore
	public void testLemmatize()
	{
		fail("Not yet implemented");
	}

	@Ignore
	public void testLemmatizeTolerant()
	{
		fail("Not yet implemented");
	}

}
