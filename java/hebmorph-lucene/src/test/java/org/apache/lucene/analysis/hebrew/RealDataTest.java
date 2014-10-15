package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.ConstantsHandler;
import com.code972.hebmorph.hspell.LingInfo;
import com.code972.hebmorph.lemmafilters.BasicLemmaFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: synhershko
 * Date: 6/17/13
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class RealDataTest extends TestBase {
    public static class TestSimpleHebrewAnalyzer extends Analyzer {
        private final DictRadix<MorphData> dictRadix;
        private final HashMap<String, Integer> prefixes;
        private final DictRadix<Byte> specialTokenizationCases;
        private final CharArraySet commonWords;

        public TestSimpleHebrewAnalyzer(final DictRadix<MorphData> dictRadix, final HashMap<String, Integer> prefixes,
                                        final DictRadix<Byte> specialTokenizationCases, final CharArraySet commonWords) throws IOException {
            this.dictRadix = dictRadix;
            this.prefixes = prefixes;
            this.specialTokenizationCases = specialTokenizationCases;
            this.commonWords = commonWords;
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
            final StreamLemmasFilter src = new StreamLemmasFilter(reader, dictRadix, prefixes, specialTokenizationCases, commonWords, new BasicLemmaFilter());
            src.setKeepOriginalWord(true);

            return new TokenStreamComponents(src) {
                @Override
                protected void setReader(final Reader reader) throws IOException {
                    super.setReader(reader);
                }
            };
        }
    }

    @Test
    public void testSequentially() throws IOException, InterruptedException {
        final Analyzer a = new TestSimpleHebrewAnalyzer(getDictionary(), ConstantsHandler.readPrefixesFromFile(false), null, null);
        System.out.print("Dictionary initialized;");

        final HashSet<String> results = performSearch(a);
        for (int i = 0; i < 10; i++) {
            HashSet<String> tmp = performSearch(a);
            if (results.size() != tmp.size()) {
                fail("Got " + tmp.size() + " results, expected " + results.size());
            }
            System.out.print(" " + i);
        }
        System.out.println();
    }

    @Test
    public void testMultiThreaded() throws IOException {
        //final Analyzer a = new TestSimpleHebrewAnalyzer(getDictionary(), LingInfo.buildPrefixTree(false), null, null);
        final Analyzer a = new MorphAnalyzer(Version.LUCENE_46, getDictionary(), ConstantsHandler.readPrefixesFromFile(false));
        System.out.println("Dictionary initialized");

        final ExecutorService executorService = Executors.newFixedThreadPool(16);
        final HashSet<String> results = performSearch(a);
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Thread " + threadId + " started");
                    counter.incrementAndGet();
                    try {
                        HashSet<String> tmp = performSearch(a);
                        if (tmp.size() != results.size()) {
                            System.out.println("Go " + tmp.size() + " results, expected " + results.size());
                            fail("Go " + tmp.size() + " results, expected " + results.size());
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        fail(e.getMessage());
                    }
                    counter.decrementAndGet();
                    System.out.println("Thread " + threadId + " completed");
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            fail();
        }
        if (counter.intValue() != 0) fail("Not all threads finished in the allotted time");
    }

    private static HashSet<String> performSearch(Analyzer a) throws IOException {
        HashSet<String> results = new HashSet<>();
        for (File file : getTestFiles()) {
            MemoryIndex memoryIndex = new MemoryIndex(true);
            final List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));

            memoryIndex.addField("title", lines.get(0), a);
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line);
            }
            memoryIndex.addField("content", sb.toString(), a);

            IndexSearcher searcher = memoryIndex.createSearcher();
            ExistsCollector collector = new ExistsCollector();

            searcher.search(new TermQuery(new Term("content", "אני")), collector);
            if (collector.exists()) {
                results.add(file.getName());
            }
        }
        return results;
    }

    public static class ExistsCollector extends Collector {

        private boolean exists;

        public void reset() {
            exists = false;
        }

        public boolean exists() {
            return exists;
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.exists = false;
        }

        @Override
        public void collect(int doc) throws IOException {
            exists = true;
        }

        @Override
        public void setNextReader(AtomicReaderContext context) throws IOException {
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }
    }
}
