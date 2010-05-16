using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

using Lucene.Net;
using Lucene.Net.Analysis;
using Lucene.Net.Analysis.Hebrew;
using Lucene.Net.Store;
using Lucene.Net.Index;
using Lucene.Net.Documents;
using Lucene.Net.Search;
using Lucene.Net.QueryParsers;

namespace LuceneNetHebrewTests
{
    public partial class MainForm : Form
    {
        public MainForm()
        {
            InitializeComponent();
        }

        private string SelectProjectFolder(string descriptionText, string pathToCombine)
        {
            FolderBrowserDialog fbd = new FolderBrowserDialog();
            fbd.Description = descriptionText;
            //fbd.ShowNewFolderButton = false;

            // Locate the hspell-data-files folder
            string exeFile = (new System.Uri(System.Reflection.Assembly.GetEntryAssembly().CodeBase)).AbsolutePath;
            string path = System.IO.Path.GetDirectoryName(exeFile);
            int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
            if (loc > -1)
            {
                path = path.Remove(loc + 1);
                if (!string.IsNullOrEmpty(pathToCombine))
                    path = System.IO.Path.Combine(path, pathToCombine);
                fbd.SelectedPath = path;
            }

            DialogResult dr = fbd.ShowDialog();
            if (dr != DialogResult.OK)
                return null;

            return fbd.SelectedPath;
        }

        private void btnRunAutoTests_Click(object sender, EventArgs e)
        {
            new Tests.BasicHebrewTests(analyzer).Run();
        }

        Analyzer analyzer;
        string tempPath = System.IO.Path.GetTempPath() + "hebMorphIndex" + System.IO.Path.DirectorySeparatorChar;

        private void btnInitAnalyzer_Click(object sender, EventArgs e)
        {
            using (new BusyObject(this))
            {
                if (analyzer == null)
                {
                    string hspellPath = SelectProjectFolder("Select a path to HSpell data files", "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
                    if (hspellPath == null)
                        return;

                    analyzer = new HebMorphAnalyzer(hspellPath);
                }

                // Recreate the index
                IndexWriter writer = new IndexWriter(tempPath, new SimpleAnalyzer(), true, new IndexWriter.MaxFieldLength(10));
                writer.Close();
                
            }
            btnIndexAddFolder.Enabled = true;
            btnRunAutoTests.Enabled = true;
            btnExecuteSearch.Enabled = true;
        }

        private void btnIndexAddFolder_Click(object sender, EventArgs e)
        {
            using (new BusyObject(this))
            {
                string hspellPath = SelectProjectFolder("Select a path to add to the index", null);
                if (hspellPath == null)
                    return;

                string[] files = System.IO.Directory.GetFiles(hspellPath, "*.txt");
                if (files != null)
                {
                    Directory indexDirectory = FSDirectory.Open(new System.IO.DirectoryInfo(tempPath));
                    IndexWriter writer = new IndexWriter(indexDirectory, analyzer, false, new IndexWriter.MaxFieldLength(int.MaxValue));

                    foreach (string f in files)
                    {
                        Document doc = new Document();
                        string text = System.IO.File.ReadAllText(f);
                        string title = f.Substring(f.LastIndexOf(System.IO.Path.DirectorySeparatorChar) + 1).Replace(".txt", "");
                        Fieldable titleField = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
                        titleField.SetBoost(5.0f);
                        doc.Add(titleField);
                        doc.Add(new Field("content", text, Field.Store.NO, Field.Index.ANALYZED));
                        doc.Add(new Field("path", f, Field.Store.YES, Field.Index.NO));
                        writer.AddDocument(doc);
                    }

                    writer.Close();
                    writer = null;

                    indexDirectory.Close();
                }
            }
        }

        private void btnExecuteSearch_Click(object sender, EventArgs e)
        {
            Directory indexDirectory = FSDirectory.Open(new System.IO.DirectoryInfo(tempPath));
            IndexSearcher searcher = new IndexSearcher(indexDirectory, true); // read-only=true

            // TODO: QueryParser support for Hebrew terms (most concerning issue is with acronyms - mid-word quotes)
            QueryParser qp = new QueryParser("content", analyzer);
            qp.SetDefaultOperator(QueryParser.Operator.AND);
            Query query = qp.Parse(txbSearchQuery.Text);

            ScoreDoc[] hits = searcher.Search(query, null, 1000).scoreDocs;

            // Iterate through the results:
            BindingList<SearchResult> l = new BindingList<SearchResult>();
            for (int i = 0; i < hits.Length; i++)
            {
                Document hitDoc = searcher.Doc(hits[i].doc);
                SearchResult sr = new SearchResult(hitDoc.GetField("title").StringValue(),
                    hitDoc.GetField("path").StringValue(), hits[i].score);
                l.Add(sr);
            }

            searcher.Close();
            indexDirectory.Close();

            dgvResults.DataSource = l;
        }

        private void dgvResults_CellContentDoubleClick(object sender, DataGridViewCellEventArgs e)
        {
            if (e.RowIndex > -1 && dgvResults.Rows[e.RowIndex].DataBoundItem != null)
            {
                SearchResult sr = dgvResults.Rows[e.RowIndex].DataBoundItem as SearchResult;
                if (sr != null)
                    System.Diagnostics.Process.Start(sr.Path);
            }
        }
    }
}