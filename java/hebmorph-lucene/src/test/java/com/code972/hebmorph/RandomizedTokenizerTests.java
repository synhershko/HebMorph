package com.code972.hebmorph;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.TimeoutSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by synhershko on 27/06/14.
 */
@RunWith(RandomizedRunner.class)
@TimeoutSuite(millis = 10000000)
public class RandomizedTokenizerTests {

    String[] customWords = new String[] { "C++", "C++X0", "i-phone", "i-pad", ".NET",
            "VB.NET", "F#", "C#", "נביעות+", "Google+"};

    @Test
    @Repeat(iterations = 1000)
    public void testCustomWordsInStream() throws IOException {
        Random random = random();

        // Randomized custom words
        SortedSet<WordAndPosition> wordsPicked = new TreeSet<>();
        Set<Integer> positionsPicked = new HashSet<>();
        int highestPos = 0;
        for (int i = 0; i < random.nextInt(customWords.length * 2); i++) {
            int pos = random.nextInt(10000);
            while (positionsPicked.contains(pos)) {
                pos = random.nextInt(10000);
            }

            positionsPicked.add(pos);
            wordsPicked.add(new WordAndPosition(customWords[i % customWords.length], pos));
            if (pos > highestPos)
                highestPos = pos;
        }

        List<String> tokens = new ArrayList<>();

        final Tokenizer tokenizer = new Tokenizer(null);
        StringBuilder sb = new StringBuilder();
        int lastPos = 0;
        for (WordAndPosition wordAndPosition : wordsPicked) {
            for (int curPos = lastPos; curPos < wordAndPosition.getPosition(); curPos++) {
                tokens.add("booga");
                sb.append("booga");
                sb.append(' ');
            }

            tokens.add(wordAndPosition.getWord());
            sb.append(wordAndPosition.getWord());
            sb.append(' ');

            tokenizer.addSpecialCase(wordAndPosition.getWord());
            lastPos = wordAndPosition.getPosition() + 1;
        }

        Reference<String> test = new Reference<String>("");
        tokenizer.reset(new StringReader(sb.toString()));
        int i = 0, tokenType;
        while ((tokenType = tokenizer.nextToken(test)) > 0) {
            assertEquals(tokens.get(i), test.ref);
            i++;
        }
    }

    /**
     * Access to the current {@link com.carrotsearch.randomizedtesting.RandomizedContext}'s Random instance. It is safe to use
     * this method from multiple threads, etc., but it should be called while within a runner's
     * scope (so no static initializers). The returned {@link java.util.Random} instance will be
     * <b>different</b> when this method is called inside a {@link org.junit.BeforeClass} hook (static
     * suite scope) and within {@link org.junit.Before}/ {@link org.junit.After} hooks or test methods.
     *
     * <p>The returned instance must not be shared with other threads or cross a single scope's
     * boundary. For example, a {@link java.util.Random} acquired within a test method shouldn't be reused
     * for another test case.
     *
     * <p>There is an overhead connected with getting the {@link java.util.Random} for a particular context
     * and thread. It is better to cache the {@link java.util.Random} locally if tight loops with multiple
     * invocations are present or create a derivative local {@link java.util.Random} for millions of calls
     * like this:
     * <pre>
     * Random random = new Random(random().nextLong());
     * // tight loop with many invocations.
     * </pre>
     */
    public static Random random() {
        return RandomizedContext.current().getRandom();
    }

    private static class WordAndPosition implements Comparable {
        private final String word;
        private final int position;

        public WordAndPosition(String word, int position) {
            this.word = word;
            this.position = position;
        }

        @Override
        public boolean equals(Object obj){
            if (!(obj instanceof WordAndPosition))
                return false;

            WordAndPosition o = (WordAndPosition) obj;
            return o.getPosition() == this.getPosition() && this.getWord().equals(o.getWord());
        }

        @Override
        public int hashCode(){
            return 0;
        }

        public int getPosition() {
            return position;
        }

        public String getWord() {
            return word;
        }

        @Override
        public int compareTo(Object o) {
            return Integer.compare(this.getPosition(), ((WordAndPosition) o).getPosition());
        }
    }

    private static class WordAndPositionComparator implements Comparator<WordAndPosition> {
        @Override
        public int compare(WordAndPosition o1, WordAndPosition o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }
}
