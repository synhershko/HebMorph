namespace LuceneNetHebrewTests
{
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.Windows.Forms.Button btnInitAnalyzer;
            this.btnIndexAddFolder = new System.Windows.Forms.Button();
            this.btnRunAutoTests = new System.Windows.Forms.Button();
            this.btnExecuteSearch = new System.Windows.Forms.Button();
            this.txbSearchQuery = new System.Windows.Forms.TextBox();
            this.dgvResults = new System.Windows.Forms.DataGridView();
            btnInitAnalyzer = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.dgvResults)).BeginInit();
            this.SuspendLayout();
            // 
            // btnInitAnalyzer
            // 
            btnInitAnalyzer.Location = new System.Drawing.Point(12, 12);
            btnInitAnalyzer.Name = "btnInitAnalyzer";
            btnInitAnalyzer.Size = new System.Drawing.Size(162, 43);
            btnInitAnalyzer.TabIndex = 0;
            btnInitAnalyzer.Text = "Initialize analyzer";
            btnInitAnalyzer.UseVisualStyleBackColor = true;
            btnInitAnalyzer.Click += new System.EventHandler(this.btnInitAnalyzer_Click);
            // 
            // btnIndexAddFolder
            // 
            this.btnIndexAddFolder.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btnIndexAddFolder.Enabled = false;
            this.btnIndexAddFolder.Location = new System.Drawing.Point(563, 12);
            this.btnIndexAddFolder.Name = "btnIndexAddFolder";
            this.btnIndexAddFolder.Size = new System.Drawing.Size(108, 31);
            this.btnIndexAddFolder.TabIndex = 0;
            this.btnIndexAddFolder.Text = "Add folder to index";
            this.btnIndexAddFolder.UseVisualStyleBackColor = true;
            this.btnIndexAddFolder.Click += new System.EventHandler(this.btnIndexAddFolder_Click);
            // 
            // btnRunAutoTests
            // 
            this.btnRunAutoTests.Enabled = false;
            this.btnRunAutoTests.Location = new System.Drawing.Point(12, 61);
            this.btnRunAutoTests.Name = "btnRunAutoTests";
            this.btnRunAutoTests.Size = new System.Drawing.Size(162, 42);
            this.btnRunAutoTests.TabIndex = 1;
            this.btnRunAutoTests.Text = "Run automated tests";
            this.btnRunAutoTests.UseVisualStyleBackColor = true;
            this.btnRunAutoTests.Click += new System.EventHandler(this.btnRunAutoTests_Click);
            // 
            // btnExecuteSearch
            // 
            this.btnExecuteSearch.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btnExecuteSearch.Enabled = false;
            this.btnExecuteSearch.Location = new System.Drawing.Point(443, 12);
            this.btnExecuteSearch.Name = "btnExecuteSearch";
            this.btnExecuteSearch.Size = new System.Drawing.Size(114, 31);
            this.btnExecuteSearch.TabIndex = 1;
            this.btnExecuteSearch.Text = "Execute search";
            this.btnExecuteSearch.UseVisualStyleBackColor = true;
            this.btnExecuteSearch.Click += new System.EventHandler(this.btnExecuteSearch_Click);
            // 
            // txbSearchQuery
            // 
            this.txbSearchQuery.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.txbSearchQuery.Location = new System.Drawing.Point(190, 18);
            this.txbSearchQuery.Name = "txbSearchQuery";
            this.txbSearchQuery.Size = new System.Drawing.Size(247, 20);
            this.txbSearchQuery.TabIndex = 2;
            // 
            // dgvResults
            // 
            this.dgvResults.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.dgvResults.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvResults.Location = new System.Drawing.Point(195, 55);
            this.dgvResults.Name = "dgvResults";
            this.dgvResults.Size = new System.Drawing.Size(476, 267);
            this.dgvResults.TabIndex = 3;
            this.dgvResults.CellContentDoubleClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvResults_CellContentDoubleClick);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(683, 335);
            this.Controls.Add(this.dgvResults);
            this.Controls.Add(this.txbSearchQuery);
            this.Controls.Add(this.btnExecuteSearch);
            this.Controls.Add(this.btnRunAutoTests);
            this.Controls.Add(btnInitAnalyzer);
            this.Controls.Add(this.btnIndexAddFolder);
            this.Name = "MainForm";
            this.Text = "Lucene.Net Hebrew Searches Tester Application";
            ((System.ComponentModel.ISupportInitialize)(this.dgvResults)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnIndexAddFolder;
        private System.Windows.Forms.Button btnRunAutoTests;
        private System.Windows.Forms.Button btnExecuteSearch;
        private System.Windows.Forms.TextBox txbSearchQuery;
        private System.Windows.Forms.DataGridView dgvResults;
    }
}

