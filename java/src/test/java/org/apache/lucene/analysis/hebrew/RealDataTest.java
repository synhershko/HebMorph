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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

public class RealDataTest extends TestBase {

    @Test
    public void testSequentially() throws IOException, InterruptedException {
        final Analyzer a = new HebrewIndexingAnalyzer(getDictionary());
        System.out.print("DictHebMorph initialized;");

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
        final Analyzer a = new HebrewIndexingAnalyzer(getDictionary());
        System.out.println("DictHebMorph initialized");

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

    public static class ExistsCollector extends SimpleCollector {

        private boolean exists;

        public void reset() {
            exists = false;
        }

        public boolean exists() {
            return exists;
        }

        @Override
        public void setScorer(Scorable scorer) throws IOException {
            this.exists = false;
        }

        @Override
        public void collect(int doc) throws IOException {
            exists = true;
        }

        @Override
        public ScoreMode scoreMode() {
            return ScoreMode.TOP_SCORES;
        }
    }
}
