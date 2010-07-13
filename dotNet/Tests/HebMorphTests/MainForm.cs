using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Diagnostics;

using HebMorph;
using HebMorph.DataStructures;

namespace TestApplication
{
    public partial class MainForm : Form
    {
        DictRadix<MorphData> m_dict;
        HebMorph.Lemmatizer m_lemmatizer;

        public MainForm()
        {
            InitializeComponent();
        }

        private void LoggerWriteLine(string str, params object[] args)
        {
            txbLogger.AppendText(string.Format(str, args) + Environment.NewLine);
        }

        private string SelectHSpellFolderPath()
        {
            FolderBrowserDialog fbd = new FolderBrowserDialog();

            // Locate the hspell-data-files folder
            string exeFile = (new System.Uri(System.Reflection.Assembly.GetEntryAssembly().CodeBase)).AbsolutePath;
            string path = System.IO.Path.GetDirectoryName(exeFile);
            int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
            if (loc > -1)
            {
                path = path.Remove(loc + 1);
                path = System.IO.Path.Combine(path, "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
                fbd.SelectedPath = path;
            }

            fbd.ShowNewFolderButton = false;
            DialogResult dr = fbd.ShowDialog();
            if (dr != DialogResult.OK)
                return null;

            return fbd.SelectedPath;
        }

        private void btnLoadHSpellFolder_Click(object sender, EventArgs e)
        {
            using (new BusyObject(this))
            {
                string hspellPath = SelectHSpellFolderPath();
                if (hspellPath == null)
                    return;

                LoggerWriteLine("Initializing Radix tree loading from HSpell data folder...");
                LoggerWriteLine("Configuration: Load morphology data = {0}", chbLoadMorphData.Checked);

                Stopwatch sw = Stopwatch.StartNew();
                m_dict = HebMorph.HSpell.Loader.LoadDictionaryFromHSpellFolder(hspellPath,
                    chbLoadMorphData.Checked);
                sw.Stop();

                LoggerWriteLine("Elapsed time: {0}ms", sw.ElapsedMilliseconds);
                LoggerWriteLine("-=-=-");

                ResetDictViewTree();
            }
        }

        private void ResetDictViewTree()
        {
            trvDictView.Nodes.Clear();
            PopulateDictViewTree(trvDictView.Nodes.Add("Root"), m_dict.RootNode, string.Empty);
        }

        private void PopulateDictViewTree(TreeNode parent, DictRadix<MorphData>.DictNode dn, string prefix)
        {
            if (dn != null && dn.Children != null)
            {
                foreach (DictRadix<MorphData>.DictNode child in dn.Children)
                {
                    TreeNode tn = new TreeNode(string.Format("{0}{1}", prefix, new string(child._Key)));
                    tn.Tag = child;
                    if (child.Value != null) tn.BackColor = Color.LightBlue; // Mark Morphology data available
                    if (child.Children != null) tn.Nodes.Add("..."); // Mark nodes with children
                    parent.Nodes.Add(tn);
                }
            }
        }

        private void trvDictView_BeforeExpand(object sender, TreeViewCancelEventArgs e)
        {
            if (e.Node.Nodes.Count == 1 && e.Node.Nodes[0].Text.Equals("..."))
            {
                e.Node.Nodes.Clear();
                PopulateDictViewTree(e.Node, e.Node.Tag as DictRadix<MorphData>.DictNode, e.Node.Text);
            }
        }

        private void btnCheck_Click(object sender, EventArgs e)
        {
            if (m_lemmatizer == null || !m_lemmatizer.IsInitialized || !(m_lemmatizer is StreamLemmatizer))
            {
                m_lemmatizer = new HebMorph.StreamLemmatizer(new System.IO.StringReader(txbCheck.Text));

                string hspellPath = SelectHSpellFolderPath();
                if (hspellPath == null)
                    return;

                m_lemmatizer.InitFromHSpellFolder(hspellPath, true, false);
            } else
            {
                (m_lemmatizer as StreamLemmatizer).SetStream(new System.IO.StringReader(txbCheck.Text));
            }

            string word = string.Empty;
            List<Token> tokens = new List<Token>();
            while ((m_lemmatizer as StreamLemmatizer).LemmatizeNextToken(out word, tokens) > 0)
            {
                if (tokens.Count == 0)
                {
                    LoggerWriteLine("{0}: Unrecognized word{1}{2}", word, Environment.NewLine, "------");
                    continue;
                }

                if (tokens.Count == 1 && !(tokens[0] is HebrewToken))
                {
                    LoggerWriteLine("{0}: Not a Hebrew word; detected as {1}{2}{3}", word,
                        tokens[0].IsNumeric ? "Numeric" : "NonHebrew", Environment.NewLine, "------");
                    continue;
                }

                int curPrefix = -1;
                string curWord = string.Empty;
                foreach (Token r in tokens)
                {
                    HebrewToken ht = r as HebrewToken;
                    if (ht == null)
                        continue;

                    if (curPrefix != ht.PrefixLength || !curWord.Equals(ht.Text))
                    {
                        curPrefix = ht.PrefixLength;
                        curWord = ht.Text;
                        if (curPrefix == 0)
                            LoggerWriteLine("Legal word: {0} (score: {1})", ht.Text, ht.Score);
                        else
                        {
                            LoggerWriteLine("Legal combination: {0}+{1} (score: {2})", ht.Text.Substring(0, curPrefix),
                                ht.Text.Substring(curPrefix), ht.Score);
                        }
                    }
                    LoggerWriteLine(ht.ToString());
                }
                LoggerWriteLine("------");
            }
        }

        private void btnTestRadix_Click(object sender, EventArgs e)
        {
            DictRadix<object> r = new DictRadix<object>(); ;
            r.AddNode("abcdef", 5);
            r.AddNode("ab", 11);
            r.AddNode("abcd", 115);
            r.AddNode("aaa", 41);
            r.AddNode("abc", 111);
            r.AddNode("a", 101);
            r.AddNode("bba", 22);
            r.AddNode("bbc", 22);
            r.AddNode("bb", 221);
            r.AddNode("def", 22);
            r.AddNode("deg", 33);

            DictRadix<object>.RadixEnumerator en = r.GetEnumerator() as DictRadix<object>.RadixEnumerator;
            while (en.MoveNext())
            {
                System.Diagnostics.Trace.WriteLine(string.Format("{0} {1}", en.CurrentKey, en.Current.ToString()));
            }
        }
    }
}