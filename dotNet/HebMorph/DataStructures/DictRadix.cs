/***************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
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
 ***************************************************************************/

namespace HebMorph.DataStructures
{
    using System;
    using System.Collections.Generic;
    using ToleranceFuncDelegate = HebMorph.LookupTolerators.ToleranceFuncDelegate;

    public class DictRadix<T>
    {
        public class DictNode
        {
            public char[] _Key;
            public T Value;
            public DictNode[] Children;
        }

        protected class TolerantLookupCrawler
        {
            public TolerantLookupCrawler()
            {
            }

            ToleranceFuncDelegate[] toleranceFunctions;
            DictRadix<T> enclosingInstance;
            char[] key;
            List<LookupResult> resultSet;

            public List<LookupResult> LookupTolerant(string strKey)
            {
                return resultSet;
            }

            private void LookupTolerantImpl(DictNode cur, byte keyPos, string word, float score)
            {
            }
        }

        protected DictNode m_root;
        public DictNode RootNode { get { return m_root; } }

        public DictRadix()
        {
            m_root = new DictNode();
        }

        public T Lookup(string key)
        {
            return Lookup(key.ToCharArray());
        }
        
        public T Lookup(char[] key)
        {
            DictNode dn = LookupImpl(key);
            if (dn == null) return default(T);
            return dn.Value;
        }
        
        /// <summary>
        /// Simple, efficient method for exact lookup in the radix
        /// </summary>
        /// <param name="key"></param>
        /// <returns></returns>
        private DictNode LookupImpl(char[] key)
        {
            byte keyPos = 0, n;
            int keyLength = GetCharArrayLength(key);

            DictNode cur = m_root;
            while (cur != null && cur.Children != null)
            {
                for (byte childPos = 0; ; childPos++)
                {
                    DictNode child = cur.Children[childPos];
                    
                    // Do key matching
                    n = 0;
                    while (n < child._Key.Length && keyPos < keyLength && child._Key[n] == key[keyPos])
                    {
                        keyPos++;
                        n++;
                    }

                    if (n == child._Key.Length) // We consumed the child's key, and so far it matches our key
                    {
                        // We consumed both the child's key and the requested key, meaning we found the requested node
                        if (keyLength == keyPos)
                        {
                            return child;
                        }
                        // We consumed this child's key, but the key we are looking for isn't over yet
                        else if (keyLength > keyPos)
                        {
                            cur = child;
                            break;
                        }
                    }
                    else if (n > 0 // Incomplete match to child's key (worths nothing)
                        || childPos + 1 == cur.Children.Length // We looked at all the node's children
                        )
                    {
                        return null;
                    }
                }
            }
            
            return null;
        }

        public class LookupResult
        {
            public LookupResult(string _word, T _data, float _score)
            {
                this.Word = _word;
                this.Data = _data;
                this.Score = _score;
            }

            public string Word;
            public T Data;
            public float Score;
        }

        public List<LookupResult> LookupTolerant(string strKey, ToleranceFuncDelegate tolFunc)
        {
            List<LookupResult> ret = new List<LookupResult>();
            LookupTolerantImpl(m_root, strKey.ToCharArray(), 0, string.Empty, 1.0f, tolFunc, ret);
            if (ret.Count > 0)
                return ret;
            return null;
        }

        private void LookupTolerantImpl(DictNode cur, char[] key, byte keyPos, string word, float score,
            ToleranceFuncDelegate tolFunc, List<LookupResult> resultsSet)
        {
            if (cur == null || cur.Children == null)
                return;

            for (byte childPos = 0; childPos < cur.Children.Length; childPos++)
            {
                DictNode child = cur.Children[childPos];

                // Utility variables
                byte? tmp;

                // Iteration-scope variables (per tolerator function)
                float iterationScore = score;
                byte iterationKeyPos = keyPos;
                string consumedLetters = string.Empty, iterationWord = word.Clone().ToString();

                bool tolerated = false;
                byte ownKeyPos = 0;

                while (ownKeyPos < child._Key.Length && iterationKeyPos < key.Length)
                {
                    float tmpScore = iterationScore;
                    tmp = tolFunc(key, ref iterationKeyPos, iterationWord, ref tmpScore, child._Key[ownKeyPos]);
                    if (!tolerated && tmp != null)
                    {
                        tolerated = true;
                        iterationScore = tmpScore;
                        if (tmp > 0)
                        {
                            if ((byte)tmp <= child._Key.Length)
                                consumedLetters += new string(child._Key, ownKeyPos, (byte)tmp);
                            ownKeyPos += (byte)tmp;
                        }
                    }
                    else
                    {
                        if (key[iterationKeyPos] != child._Key[ownKeyPos])
                        {
                            goto EscapeTag;
                        }
                        else
                        {
                            tolerated = false;
                            consumedLetters += child._Key[ownKeyPos];
                            ownKeyPos++;
                            iterationKeyPos++;
                        }
                    }
                }
                if (ownKeyPos >= child._Key.Length)
                {
                    iterationWord += consumedLetters;
                    //change iterationScore to be tmpScore
                    if (iterationKeyPos > 0)
                    {
                        if (iterationKeyPos == key.Length)
                        {
                            if (child.Value != null)
                                resultsSet.Add(new LookupResult(iterationWord, child.Value, iterationScore));
                        }
                        else
                            LookupTolerantImpl(child, key, iterationKeyPos, iterationWord, iterationScore, tolFunc, resultsSet);
                    }
                }

            EscapeTag:
                continue;
            }
        }

        private int GetCharArrayLength(char[] ar)
        {
            int i = 0;
            while (ar.Length > i && ar[i] != '\0') i++;
            return i;
        }

        public void AddNode(string key, T data)
        {
            AddNode(key.ToCharArray(), data);
        }

        public void AddNode(char[] key, T data)
        {
            // Since key might be a buffer array which is longer than the actual word in it, we can't
            // just use key.Length
            int keyLength = GetCharArrayLength(key);

            int keyPos = 0;
            DictNode cur = m_root;
            while (cur != null)
            {
                // No children, but key is definately a descendant
                if (cur.Children == null)
                {
                    DictNode newChild = new DictNode();
                    newChild._Key = new char[keyLength - keyPos];
                    Array.Copy(key, keyPos, newChild._Key, 0, newChild._Key.Length);
                    newChild.Value = data;

                    cur.Children = new DictNode[1];
                    cur.Children[0] = newChild;
                    return;
                }

                // Iterate through all children of the current node, and either switch node based on the
                // key, find a node to split into 2, or add a new child with the remaining path
                int childPos = 0;
                bool bFoundChild = false;
                for (; childPos < cur.Children.Length; childPos++)
                {
                    DictNode child = cur.Children[childPos];

                    int n = 0;

                    // By definition, there is no such thing as a null _Key
                    while (n < child._Key.Length && keyPos < keyLength && child._Key[n] == key[keyPos] && key[keyPos] != '\0')
                    {
                        keyPos++;
                        n++;
                    }

                    // If it was a match, even partial
                    if (n > 0)
                    {
                        bFoundChild = true;

                        // We consumed this child's key, but the key we are looking for isn't over yet
                        if (n == child._Key.Length && keyLength > keyPos)
                        {
                            cur = child;
                            break;
                        }
                        // We consumed none of the keys
                        else if (child._Key.Length > n && keyLength > keyPos)
                        {
                            // split
                            DictNode bridgeChild = new DictNode();
                            bridgeChild._Key = new char[n];
                            Array.Copy(child._Key, 0, bridgeChild._Key, 0, n);

                            int childNewKeyLen = child._Key.Length - n;
                            char[] childNewKey = new char[childNewKeyLen];
                            Array.Copy(child._Key, n, childNewKey, 0, childNewKeyLen);
                            child._Key = childNewKey;

                            bridgeChild.Children = new DictNode[2];
                            bridgeChild.Children[0] = child;

                            DictNode newNode = new DictNode();
                            newNode._Key = new char[keyLength - keyPos];
                            Array.Copy(key, keyPos, newNode._Key, 0, newNode._Key.Length);
                            newNode.Value = data;
                            bridgeChild.Children[1] = newNode;

                            cur.Children[childPos] = bridgeChild;

                            return;
                        }
                        // We consumed the requested key, but the there's still more chars in the child's key
                        else if (child._Key.Length > n && keyLength == keyPos)
                        {
                            // split
                            DictNode newChild = new DictNode();
                            newChild._Key = new char[n];
                            Array.Copy(child._Key, 0, newChild._Key, 0, n);

                            int childNewKeyLen = child._Key.Length - n;
                            char[] childNewKey = new char[childNewKeyLen];
                            Array.Copy(child._Key, n, childNewKey, 0, childNewKeyLen);
                            child._Key = childNewKey;

                            newChild.Children = new DictNode[1];
                            newChild.Children[0] = child;
                            newChild.Value = data;

                            cur.Children[childPos] = newChild;

                            return;
                        }
                        // We consumed both the child's key and the requested key
                        else if (n == child._Key.Length && keyLength == keyPos)
                        {
                            // TODO: Do we allow overriding data? perhaps have compile switches for this?
                            return;
                        }
                    }
                }

                if (!bFoundChild)
                {
                    // Dead end - add a new child and return
                    DictNode newChild = new DictNode();
                    newChild._Key = new char[keyLength - keyPos];
                    Array.Copy(key, keyPos, newChild._Key, 0, newChild._Key.Length);
                    newChild.Value = data;

                    Array.Resize<DictNode>(ref cur.Children, cur.Children.Length + 1);
                    cur.Children[cur.Children.Length - 1] = newChild;
                    return;
                }
            }
        }
    }
}
