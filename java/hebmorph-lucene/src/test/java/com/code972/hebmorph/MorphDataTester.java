package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Constants;
import com.code972.hebmorph.hspell.HSpellLoader;
import com.code972.hebmorph.hspell.HebLoader;
import com.code972.hebmorph.hspell.LingInfo;
import org.junit.Test;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Egozy on 12/11/2014.
 */
public class MorphDataTester {
    @Test
    public void lemmaTestEquals() {
        MorphData.Lemma lemma1 = new MorphData.Lemma("asd", 15), lemma2 = new MorphData.Lemma("asd", 15);
        assert (lemma1.equals(lemma2));
        lemma1 = new MorphData.Lemma("asd",5);
        assert (!lemma1.equals(lemma2));
        lemma2 = new MorphData.Lemma("asd",5);
        assert (lemma1.equals(lemma2));
        lemma1 = new MorphData.Lemma("a",5);
        assert (!lemma1.equals(lemma2));
        lemma2 = new MorphData.Lemma("a",5);
        assert (lemma1.equals(lemma2));
        lemma1 = new MorphData.Lemma(null,5);
        assert (!lemma1.equals(lemma2));
        lemma2 = new MorphData.Lemma(null,5);
        assert (lemma1.equals(lemma2));
    }
}