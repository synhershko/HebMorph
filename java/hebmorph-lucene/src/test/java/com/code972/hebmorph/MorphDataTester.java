package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HSpellLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Egozy on 12/11/2014.
 */
public class MorphDataTester {
    @Test
    public void lemmaTestEquals(){
        MorphData.Lemma lemma1 = new MorphData.Lemma("asd",15),lemma2 = new MorphData.Lemma("asd",15);
        assert(lemma1.equals(lemma2));
        lemma1.setDescFlag(5);
        assert(!lemma1.equals(lemma2));
        lemma2.setDescFlag(5);
        assert(lemma1.equals(lemma2));
        lemma1.setLemma("a");
        assert(!lemma1.equals(lemma2));
        lemma2.setLemma("a");
        assert(lemma1.equals(lemma2));
        lemma1.setLemma(null);
        assert(!lemma1.equals(lemma2));
        lemma2.setLemma(null);
        assert(lemma1.equals(lemma2));
    }
}
