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
package hebmorph.datastructures;

import hebmorph.LookupTolerators;
import hebmorph.Reference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DictRadix<T> implements Iterable<T>
{
	public class DictNode
	{
		private DictNode[] children;
		private char[] key;
		private T value;
		public T getValue()
		{
			return value;
		}
		public void setValue(T value)
		{
			this.value = value;
		}
		public void setChildren(DictNode[] children)
		{
			this.children = children;
		}
		public DictNode[] getChildren()
		{
			return children;
		}
		public void setKey(char[] key)
		{
			this.key = key;
		}
		public char[] getKey()
		{
			return key;
		}
	}

	protected class TolerantLookupCrawler
	{
		protected class MatchCandidate
		{
			public MatchCandidate(byte _keyPos, String _word, float _score)
			{
				this.keyPos = _keyPos;
				this.word = _word;
				this.score = _score;
			}

			private byte keyPos;
			private String word;
			private float score = 1.0f;
			
			public byte getKeyPos()
			{
				return keyPos;
			}
			public void setKeyPos(byte keyPos)
			{
				this.keyPos = keyPos;
			}
			public String getWord()
			{
				return word;
			}
			public void setWord(String word)
			{
				this.word = word;
			}
			public float getScore()
			{
				return score;
			}
			public void setScore(float score)
			{
				this.score = score;
			}
		}

		public TolerantLookupCrawler(DictRadix<T> _enclosingInstance, LookupTolerators.ToleranceFunction[] _tolFuncs)
		{
			this.enclosingInstance = _enclosingInstance;
			this.toleranceFunctions = _tolFuncs;
		}

		private LookupTolerators.ToleranceFunction[] toleranceFunctions;
		private DictRadix<T> enclosingInstance;

		private char[] key;
		private final List<LookupResult> resultSet = new ArrayList<LookupResult>();

		public List<LookupResult> lookupTolerant(String strKey)
		{
			synchronized (resultSet)
			{
				key = strKey.toCharArray();
				resultSet.clear();
				lookupTolerantImpl(enclosingInstance.getRootNode(), new MatchCandidate((byte)0, "", 1.0f));
			}
			if (resultSet.size() > 0)
			{
				return resultSet;
			}
			return null;
		}

		private void lookupTolerantImpl(DictNode cur, MatchCandidate mc)
		{
			if (cur.getChildren() == null)
			{
				return;
			}

			//System.out.println("--------------------------");
			//System.out.println(String.format("Processing children for word %1$s", mc.Word));
			for (int childPos = 0; childPos < cur.getChildren().length; childPos++)
			{
				DictNode child = cur.getChildren()[childPos];
				doKeyMatching(child, (byte)0, mc);
			}
			//System.out.println(String.format("Completed processing node children for word %1$s", mc.Word));
			//System.out.println("--------------------------");
		}

		private void doKeyMatching(DictNode node, byte nodeKeyPos, MatchCandidate mc)
		{
			byte currentKeyPos = mc.keyPos, startingNodeKeyPos = nodeKeyPos;
			while ((nodeKeyPos < node.getKey().length) && (currentKeyPos < key.length))
			{
				// toleration
				for (LookupTolerators.ToleranceFunction tf : toleranceFunctions)
				{
					byte tmpKeyPos = mc.keyPos;
					float tmpScore = mc.getScore();
					Reference<Byte> tempRefObject = new Reference<Byte>(tmpKeyPos);
					Reference<Float> tempRefObject2 = new Reference<Float>(tmpScore);
					Integer tret = tf.tolerate(key, tempRefObject, mc.getWord(), tempRefObject2, node.getKey()[nodeKeyPos]);
					tmpKeyPos = tempRefObject.ref;
					tmpScore = tempRefObject2.ref;
					if (tret != null)
					{
						//System.out.println(String.format("%1$s tolerated a char, attempting word %2$s", tf.getClass().getName(), mc.Word + node.getKey()[nodeKeyPos]));

						String consumedLetters = "";
						if ((tret > 0) && (tret <= node.getKey().length))
						{
							consumedLetters = new String(node.getKey(), nodeKeyPos, tret);
						}
						MatchCandidate nmc = new MatchCandidate(tmpKeyPos, mc.getWord() + consumedLetters, tmpScore);
						if ((nodeKeyPos + tret) == node.getKey().length)
						{
							lookupTolerantImpl(node, nmc);
						}
						else
						{
							doKeyMatching(node, (byte)(nodeKeyPos + tret), nmc);
						}
					}
				}

				// standard key matching
				if (node.getKey()[nodeKeyPos] != key[currentKeyPos])
				{
					break;
				}

				//System.out.println(String.format("Matched char: %1$s", key[currentKeyPos]));
				currentKeyPos++;
				nodeKeyPos++;
			}

			if (nodeKeyPos == node.getKey().length)
			{
				if (currentKeyPos == key.length)
				{
					//System.out.println(String.format("Consumed the whole key"));
					if (node.getValue() != null)
					{
						resultSet.add(new LookupResult(mc.getWord() + new String(node.getKey(), startingNodeKeyPos, nodeKeyPos - startingNodeKeyPos), node.getValue(), mc.getScore()));
					}
				}
				else
				{
					MatchCandidate nmc = new MatchCandidate(currentKeyPos, mc.getWord() + new String(node.getKey(), startingNodeKeyPos, nodeKeyPos - startingNodeKeyPos), mc.getScore());
					lookupTolerantImpl(node, nmc);
				}
			}
		}
	}


	protected DictNode m_root;
	public DictNode getRootNode()
	{
		return m_root;
	}

	protected int m_nCount = 0;
	public int getCount()
	{
		return m_nCount;
	}
	
    private boolean  m_bAllowValueOverride = false;
    public boolean getAllowValueOverride() { return m_bAllowValueOverride; }
    public void setAllowValueOverride(boolean val) { m_bAllowValueOverride = val; }

	public DictRadix()
	{
		m_root = new DictNode();
	}

	public T lookup(String key)
	{
		return lookup(key.toCharArray());
	}

	public T lookup(char[] key)
	{
		DictNode dn = lookupImpl(key);
		if (dn == null)
		{
			return null;
		}
		return dn.getValue();
	}

	/**
	 Simple, efficient method for exact lookup in the radix

	 @param key
	 @return
	*/
	private DictNode lookupImpl(char[] key)
	{
		int keyPos = 0, n;
		int keyLength = getCharArrayLength(key);

		DictNode cur = m_root;
		while ((cur != null) && (cur.getChildren() != null))
		{
			for (int childPos = 0; ; childPos++)
			{
				DictNode child = cur.getChildren()[childPos];

				// Do key matching
				n = 0;
				while ((n < child.getKey().length) && (keyPos < keyLength) && (child.getKey()[n] == key[keyPos]))
				{
					keyPos++;
					n++;
				}

				if (n == child.getKey().length) // We consumed the child's key, and so far it matches our key
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
				else if ((n > 0) || (childPos + 1 == cur.getChildren().length)) // We looked at all the node's children -  Incomplete match to child's key (worths nothing)
				{
					return null;
				}
			}
		}

		return null;
	}

	public class LookupResult
	{
		public void setScore(float score)
		{
			this.score = score;
		}
		public float getScore()
		{
			return score;
		}
		public void setData(T data)
		{
			this.data = data;
		}
		public T getData()
		{
			return data;
		}
		public void setWord(String word)
		{
			this.word = word;
		}
		public String getWord()
		{
			return word;
		}
		public LookupResult(String _word, T _data, float _score)
		{
			setWord(_word);
			setData(_data);
			setScore(_score);
		}

		private String word;
		private T data;
		private float score;
	}

	public List<LookupResult> lookupTolerant(String strKey, LookupTolerators.ToleranceFunction[] tolFuncs)
	{
		TolerantLookupCrawler tlc = new TolerantLookupCrawler(this, tolFuncs);
		return tlc.lookupTolerant(strKey);
	}

	static private int getCharArrayLength(char[] ar)
	{
		int i = 0;
		while ((ar.length > i) && (ar[i] != '\0'))
		{
			i++;
		}
		return i;
	}

	public void addNode(String key, T data)
	{
		addNode(key.toCharArray(), data);
	}

	public void addNode(char[] key, T data)
	{
		// Since key might be a buffer array which is longer than the actual word in it, we can't
		// just use key.Length
		int keyLength = getCharArrayLength(key);

		int keyPos = 0;
		DictNode cur = m_root;
		while (cur != null)
		{
			// No children, but key is definitely a descendant
			if (cur.getChildren() == null)
			{
				// TODO: This assumes cur has a value and therefore is a leaf, hence we branch
				// instead of merging keys. Is this always the case?

				DictNode newChild = new DictNode();
				newChild.setKey(new char[keyLength - keyPos]);
				System.arraycopy(key, keyPos, newChild.getKey(), 0, newChild.getKey().length);
				newChild.setValue(data);

				cur.setChildren((DictNode[])Array.newInstance(DictNode.class,1));
				cur.getChildren()[0] = newChild;
				m_nCount++;
				return;
			}

			// Iterate through all children of the current node, and either switch node based on the
			// key, find a node to split into 2, or add a new child with the remaining path
			int childPos = 0;
			boolean bFoundChild = false;
			for (; childPos < cur.getChildren().length; childPos++)
			{
				DictNode child = cur.getChildren()[childPos];

				int n = 0;

				// By definition, there is no such thing as a null _Key
				while ((n < child.getKey().length) && (keyPos < keyLength) && (child.getKey()[n] == key[keyPos]) && (key[keyPos] != '\0'))
				{
					keyPos++;
					n++;
				}

				// If it was a match, even partial
				if (n > 0)
				{
					bFoundChild = true;

					// We consumed this child's key, but the key we are looking for isn't over yet
					if ((n == child.getKey().length) && (keyLength > keyPos))
					{
						cur = child;
						break;
					}
					// We consumed none of the keys
					else if ((child.getKey().length > n) && (keyLength > keyPos))
					{
						// split
						DictNode bridgeChild = new DictNode();
						bridgeChild.setKey(new char[n]);
						System.arraycopy(child.getKey(), 0, bridgeChild.getKey(), 0, n);

						int childNewKeyLen = child.getKey().length - n;
						char[] childNewKey = new char[childNewKeyLen];
						System.arraycopy(child.getKey(), n, childNewKey, 0, childNewKeyLen);
						child.setKey(childNewKey);

						bridgeChild.setChildren((DictNode[])Array.newInstance(DictNode.class,2));

						DictNode newNode = new DictNode();
						newNode.setKey(new char[keyLength - keyPos]);
						System.arraycopy(key, keyPos, newNode.getKey(), 0, newNode.getKey().length);
						newNode.setValue(data);

						if (child.getKey()[0] - newNode.getKey()[0] < 0)
						{
							bridgeChild.getChildren()[0] = child;
							bridgeChild.getChildren()[1] = newNode;
						}
						else
						{
							bridgeChild.getChildren()[0] = newNode;
							bridgeChild.getChildren()[1] = child;
						}

						cur.getChildren()[childPos] = bridgeChild;

						m_nCount++;

						return;
					}
					// We consumed the requested key, but the there's still more chars in the child's key
					else if ((child.getKey().length > n) && (keyLength == keyPos))
					{
						// split
						DictNode newChild = new DictNode();
						newChild.setKey(new char[n]);
						System.arraycopy(child.getKey(), 0, newChild.getKey(), 0, n);

						int childNewKeyLen = child.getKey().length - n;
						char[] childNewKey = new char[childNewKeyLen];
						System.arraycopy(child.getKey(), n, childNewKey, 0, childNewKeyLen);
						child.setKey(childNewKey);

						newChild.setChildren((DictNode[])Array.newInstance(DictNode.class,1));
						newChild.getChildren()[0] = child;
						newChild.setValue(data);

						cur.getChildren()[childPos] = newChild;

						m_nCount++;

						return;
					}
					// We consumed both the child's key and the requested key
					else if ((n == child.getKey().length) && (keyLength == keyPos))
					{
						if (child.getValue() == null)
						{
							child.setValue(data);
							m_nCount++;
						}
                        else if (m_bAllowValueOverride)
                        {
                            // Only override data if this radix object is configured to do this
                            child.value = data;
                        }
						return;
					}
				}
			}

			if (!bFoundChild)
			{
				// Dead end - add a new child and return
				DictNode newChild = new DictNode();
				newChild.setKey(new char[keyLength - keyPos]);
				System.arraycopy(key, keyPos, newChild.getKey(), 0, newChild.getKey().length);
				newChild.setValue(data);

				DictNode[] newArray  = (DictNode[])Array.newInstance(DictNode.class,cur.getChildren().length + 1);// new DictNode[cur.getChildren().length + 1]
				int curPos = 0;
				for (; curPos < cur.getChildren().length; ++curPos)
				{
					if(newChild.getKey()[0] - cur.getChildren()[curPos].getKey()[0] < 0)
						break;
					newArray[curPos] = cur.getChildren()[curPos];
				}
				newArray[curPos] = newChild;
				for (; curPos < cur.getChildren().length; ++curPos)
				{
					newArray[curPos + 1] = cur.getChildren()[curPos];
				}
				cur.setChildren(newArray);

				m_nCount++;

				return;
			}
		}
	}

    public void clear()
    {
        m_root = new DictNode();
        m_nCount = 0;
    }


	public class RadixEnumerator implements  java.util.Iterator<T>
	{
		private DictRadix<T> radix;
		private java.util.LinkedList<DictRadix<T>.DictNode> nodesPath;

		public RadixEnumerator(DictRadix<T> r)
		{
			this.radix = r;
			nodesPath = new java.util.LinkedList<DictRadix<T>.DictNode>();
			nodesPath.addLast(radix.m_root);
		}


        public String getCurrentKey()
        {
            StringBuilder sb = new StringBuilder();
            for(DictNode dn : nodesPath)
            {
                if(dn.key != null)
                    sb.append(dn.key);
                else
                    assert dn == radix.m_root;
            }
            return sb.toString();
        }


		/*public T getCurrent()
		{
			return nodesPath.getLast().getValue();
		}

		public void Reset()
		{
			nodesPath.clear();
			nodesPath.addLast(radix.m_root);
		}
		*/

		@Override
		public T next()
		{
            assert nodesPath.size() > 0;
            return nodesPath.getLast().getValue();
		}

		@Override
		public boolean hasNext()
		{
			boolean goUp = false;

			while (nodesPath.size() > 0)
			{
				DictRadix<T>.DictNode n = nodesPath.getLast();
				if (goUp || (n.getChildren() == null) || (n.getChildren().length == 0))
				{
					nodesPath.removeLast();
					if (nodesPath.isEmpty()) break;
					goUp = true;
					for (int i = 0; i < nodesPath.getLast().getChildren().length; i++)
					{
						// Move to the next child
						if ((nodesPath.getLast().getChildren()[i] == n) &&
                                (i + 1 < nodesPath.getLast().getChildren().length))
						{
							nodesPath.addLast(nodesPath.getLast().getChildren()[i + 1]);
							if (nodesPath.getLast().getValue() != null)
								return true;
							goUp = false;
							break;
						}
					}
				}
				else
				{
					nodesPath.addLast(n.getChildren()[0]);
					goUp = false;
					if (n.getChildren()[0].getValue() != null)
						return true;
				}
			}
			return false;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}


	public Iterator<T> iterator()
	{
		return new RadixEnumerator(this);
	}

}