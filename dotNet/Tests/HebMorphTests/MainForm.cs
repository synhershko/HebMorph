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
        HebMorph.Lemmatizer m_analyzer = new Lemmatizer();

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
            if (!m_analyzer.IsInitialized)
            {
                string hspellPath = SelectHSpellFolderPath();
                if (hspellPath == null)
                    return;

                m_analyzer.InitFromHSpellFolder(hspellPath, true, false);
            }


            HebMorph.Tokenizer tk = new HebMorph.Tokenizer(new System.IO.StringReader(txbCheck.Text));

            string word;
            HebMorph.Tokenizer.TokenType tokenType;
            while ((tokenType = tk.NextToken(out word)) != 0)
            {
                if ((tokenType & Tokenizer.TokenType.Hebrew) == 0)
                {
                    LoggerWriteLine("{0}: Not a Hebrew word; detected as {1}{2}{3}", word, tokenType, Environment.NewLine, "------");
                    continue;
                }

                // Ignore "words" which are actually only prefixes in a single word.
                // This first case is easy to spot, since the prefix and the following word will be
                // separated by a dash marked as a construct (סמיכות) by the Tokenizer
                if ((tokenType & Tokenizer.TokenType.Construct) > 0)
                {
                    if (m_analyzer.IsLegalPrefix(word))
                        continue;
                }

                // This second case is a bit more complex. We take a risk of splitting a valid acronym or
                // abbrevated word into two, so we send it to an external function to analyze the word, and
                // get a possibly corrected word. Examples for words we expect to simplify by this operation
                // are ה"שטיח", ש"המידע.
                if ((tokenType & Tokenizer.TokenType.Acronym) > 0)
                    word = m_analyzer.TryStrippingPrefix(word);

                // TODO: Perhaps by easily identifying the prefixes above we can also rule out some of the
                // stem ambiguities retreived in the next lines...

                List<HebrewToken> res = m_analyzer.LemmatizeTolerant(word);
                if (res == null)
                {
                    LoggerWriteLine("{0}: No match found{1}{2}", word, Environment.NewLine, "------");
                    continue;
                }

                int curPrefix = -1;
                string curWord = string.Empty;
                foreach (HebrewToken r in res)
                {
                    if (curPrefix != r.PrefixLength || !curWord.Equals(r.Text))
                    {
                        curPrefix = r.PrefixLength;
                        curWord = r.Text;
                        if (curPrefix == 0)
                            LoggerWriteLine("Legal word: {0} (score: {1})", r.Text, r.Score);
                        else
                        {
                            LoggerWriteLine("Legal combination: {0}+{1} (score: {2})", r.Text.Substring(0, curPrefix), r.Text.Substring(curPrefix), r.Score);
                        }
                    }
                    LoggerWriteLine(r.ToString());
                }
                LoggerWriteLine("------");
            }
        }
        
        private void btnTestCoverage_Click(object sender, EventArgs e)
        {
            //DictionaryCoverageTester dct = new DictionaryCoverageTester();
            //dct.ShowDialog();
        }
    }
}