package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.LookupTolerators;
import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.Tokenizer;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HSpellLoader;
import com.code972.hebmorph.hspell.LingInfo;
import com.code972.hebmorph.lemmafilters.BasicLemmaFilter;
import com.code972.hebmorph.lemmafilters.LemmaFilterBase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public abstract class HebrewAnalyzer extends Analyzer {
    protected static final Version matchVersion = Version.LUCENE_4_9;
    private static final Byte dummyData = (byte) 0;

    //    protected final HashMap<String, Integer> prefixesTree = LingInfo.buildPrefixTree(false);
//    protected final DictRadix<MorphData> dictRadix;
    protected DictHebMorph dict;
    protected DictRadix<MorphData> customWords;
    protected final LemmaFilterBase lemmaFilter;
    protected final char originalTermSuffix = '$';
    protected DictRadix<Byte> SPECIAL_TOKENIZATION_CASES = null;


    protected CharArraySet commonWords = null;

/*    static {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try {
            Loader loader = new Loader(classloader, "hspell-data-files/", true);
            setDictRadix(loader.loadDictionaryFromHSpellData());
        } catch (IOException e) {
            final ESLogger logger = Loggers.getLogger(HebrewAnalyzer.class);
            logger.error("Unable to load the hspell dictionary files", e);
        }

        try {
            setCustomTokenizationCases(classloader.getResourceAsStream("special-tokenization-cases.txt"));
        } catch (IOException e) {
            final ESLogger logger = Loggers.getLogger(HebrewAnalyzer.class);
            logger.debug("Unable to load special tokenization cases", e);
        }

        try {
            setCustomWords(classloader.getResourceAsStream("custom-words.txt"));
        } catch (IOException e) {
            final ESLogger logger = Loggers.getLogger(HebrewAnalyzer.class);
            logger.debug("Unable to load custom dictionary", e);
        }
    }*/

    public DictRadix<Byte> setCustomTokenizationCases(InputStream input) throws IOException {
        if (input != null) {
            final CharArraySet wordsList = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(
                    input, StandardCharsets.UTF_8), matchVersion);

            final DictRadix<Byte> radix = new DictRadix<>(false);
            for (Object aWordsList : wordsList) {
                radix.addNode((char[]) aWordsList, dummyData);
            }
            SPECIAL_TOKENIZATION_CASES = radix;
        }
        return SPECIAL_TOKENIZATION_CASES;
    }

//    public void setDict(final DictHebMorph dict) {
//        this.dict = dict;
//    }

    public DictRadix<MorphData> setCustomWords(InputStream input) throws IOException {
        customWords = HSpellLoader.loadCustomWords(input, dict.getRadix());
        return customWords;
    }

    protected HebrewAnalyzer(DictHebMorph dict, DictRadix<MorphData> customWords) throws IOException {
        lemmaFilter = new BasicLemmaFilter();
        this.dict = dict;
        this.customWords = customWords;
    }

    public static boolean isHebrewWord(final CharSequence word) {
        for (int i = 0; i < word.length(); i++) {
            if (Tokenizer.isHebrewLetter(word.charAt(i)))
                return true;
        }
        return false;
    }

    public enum WordType {
        HEBREW,
        HEBREW_WITH_PREFIX,
        HEBREW_TOLERATED,
        HEBREW_TOLERATED_WITH_PREFIX,
        NON_HEBREW,
        UNRECOGNIZED,
        CUSTOM,
        CUSTOM_WITH_PREFIX,
    }

    public WordType isRecognizedWord(final String word, final boolean tolerate) {
        byte prefLen = 0;
        Integer prefixMask;
        MorphData md;
        HashMap<String, Integer> prefixesTree = dict.getPref();
        DictRadix<MorphData> dictRadix = dict.getRadix();

        if (customWords != null) {
            try {
                if (customWords.lookup(word) != null) return WordType.CUSTOM;
            } catch (IllegalArgumentException ignored_ex) {
            }

            while (true) {
                // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
                if (word.length() - prefLen < 2)
                    break;

                if ((prefixMask = prefixesTree.get(word.substring(0, ++prefLen))) == null)
                    break;

                try {
                    md = customWords.lookup(word.substring(prefLen));
                } catch (IllegalArgumentException ignored_ex) {
                    md = null;
                }
                if ((md != null) && ((md.getPrefixes() & prefixMask) > 0)) {
                    for (int result = 0; result < md.getLemmas().length; result++) {
                        if ((LingInfo.DMask2ps(md.getLemmas()[result].getDescFlag()) & prefixMask) > 0) {
                            return WordType.CUSTOM_WITH_PREFIX;
                        }
                    }
                }
            }
        }

        if (!isHebrewWord(word))
            return WordType.NON_HEBREW;

        try {
            if (dictRadix.lookup(word) != null) return WordType.HEBREW;
        } catch (IllegalArgumentException ignored_ex) {
        }

        if (word.endsWith("'")) { // Try ommitting closing Geresh
            try {
                if (dictRadix.lookup(word.substring(0, word.length() - 1)) != null) return WordType.HEBREW;
            } catch (IllegalArgumentException ignored_ex) {
            }
        }

        prefLen = 0;
        while (true) {
            // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
            if (word.length() - prefLen < 2)
                break;

            if ((prefixMask = prefixesTree.get(word.substring(0, ++prefLen))) == null)
                break;

            try {
                md = dictRadix.lookup(word.substring(prefLen));
            } catch (IllegalArgumentException e) {
                md = null;
            }
            if ((md != null) && ((md.getPrefixes() & prefixMask) > 0)) {
                for (int result = 0; result < md.getLemmas().length; result++) {
                    if ((LingInfo.DMask2ps(md.getLemmas()[result].getDescFlag()) & prefixMask) > 0) {
                        return WordType.HEBREW_WITH_PREFIX;
                    }
                }
            }
        }

        if (tolerate) {
            // Don't try tolerating long words. Longest Hebrew word is 19 chars long
            // http://en.wikipedia.org/wiki/Longest_words#Hebrew
            if (word.length() > 20) {
                return WordType.UNRECOGNIZED;
            }

            List<DictRadix<MorphData>.LookupResult> tolerated = dictRadix.lookupTolerant(word, LookupTolerators.TolerateEmKryiaAll);
            if (tolerated != null && tolerated.size() > 0) {
                return WordType.HEBREW_TOLERATED;
            }

            prefLen = 0;
            while (true) {
                // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
                if (word.length() - prefLen < 2)
                    break;

                if ((prefixMask = prefixesTree.get(word.substring(0, ++prefLen))) == null)
                    break;

                tolerated = dictRadix.lookupTolerant(word.substring(prefLen), LookupTolerators.TolerateEmKryiaAll);
                if (tolerated != null) {
                    for (DictRadix<MorphData>.LookupResult lr : tolerated) {
                        for (int result = 0; result < lr.getData().getLemmas().length; result++) {
                            if ((LingInfo.DMask2ps(lr.getData().getLemmas()[result].getDescFlag()) & prefixMask) > 0) {
                                return WordType.HEBREW_TOLERATED_WITH_PREFIX;
                            }
                        }
                    }
                }
            }
        }

        return WordType.UNRECOGNIZED;
    }
}
