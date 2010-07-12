namespace TestApplication
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
            System.Windows.Forms.SplitContainer splitContainer1;
            System.Windows.Forms.GroupBox groupBox4;
            System.Windows.Forms.Label label2;
            System.Windows.Forms.GroupBox groupBox3;
            System.Windows.Forms.GroupBox groupBox2;
            this.btnTestRadix = new System.Windows.Forms.Button();
            this.btnCheck = new System.Windows.Forms.Button();
            this.txbCheck = new System.Windows.Forms.TextBox();
            this.txbLogger = new System.Windows.Forms.TextBox();
            this.checkBox1 = new System.Windows.Forms.CheckBox();
            this.trvDictView = new System.Windows.Forms.TreeView();
            this.chbLoadMorphData = new System.Windows.Forms.CheckBox();
            this.button1 = new System.Windows.Forms.Button();
            this.btnLoadHSpellFolder = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            splitContainer1 = new System.Windows.Forms.SplitContainer();
            groupBox4 = new System.Windows.Forms.GroupBox();
            label2 = new System.Windows.Forms.Label();
            groupBox3 = new System.Windows.Forms.GroupBox();
            groupBox2 = new System.Windows.Forms.GroupBox();
            splitContainer1.Panel1.SuspendLayout();
            splitContainer1.Panel2.SuspendLayout();
            splitContainer1.SuspendLayout();
            groupBox4.SuspendLayout();
            groupBox3.SuspendLayout();
            groupBox2.SuspendLayout();
            this.SuspendLayout();
            // 
            // splitContainer1
            // 
            splitContainer1.Dock = System.Windows.Forms.DockStyle.Fill;
            splitContainer1.Location = new System.Drawing.Point(0, 0);
            splitContainer1.Name = "splitContainer1";
            // 
            // splitContainer1.Panel1
            // 
            splitContainer1.Panel1.Controls.Add(this.btnTestRadix);
            splitContainer1.Panel1.Controls.Add(groupBox4);
            splitContainer1.Panel1.Controls.Add(groupBox3);
            // 
            // splitContainer1.Panel2
            // 
            splitContainer1.Panel2.Controls.Add(groupBox2);
            splitContainer1.Panel2.Controls.Add(this.groupBox1);
            splitContainer1.Size = new System.Drawing.Size(907, 392);
            splitContainer1.SplitterDistance = 583;
            splitContainer1.TabIndex = 12;
            // 
            // btnTestRadix
            // 
            this.btnTestRadix.Location = new System.Drawing.Point(3, 3);
            this.btnTestRadix.Name = "btnTestRadix";
            this.btnTestRadix.Size = new System.Drawing.Size(145, 28);
            this.btnTestRadix.TabIndex = 12;
            this.btnTestRadix.Text = "Test Radix";
            this.btnTestRadix.UseVisualStyleBackColor = true;
            this.btnTestRadix.Click += new System.EventHandler(this.btnTestRadix_Click);
            // 
            // groupBox4
            // 
            groupBox4.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            groupBox4.Controls.Add(this.btnCheck);
            groupBox4.Controls.Add(this.txbCheck);
            groupBox4.Controls.Add(label2);
            groupBox4.Location = new System.Drawing.Point(3, 37);
            groupBox4.Name = "groupBox4";
            groupBox4.Size = new System.Drawing.Size(577, 143);
            groupBox4.TabIndex = 11;
            groupBox4.TabStop = false;
            groupBox4.Text = "Morphological engine";
            // 
            // btnCheck
            // 
            this.btnCheck.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.btnCheck.Location = new System.Drawing.Point(506, 113);
            this.btnCheck.Name = "btnCheck";
            this.btnCheck.Size = new System.Drawing.Size(65, 23);
            this.btnCheck.TabIndex = 8;
            this.btnCheck.Text = "Check";
            this.btnCheck.UseVisualStyleBackColor = true;
            this.btnCheck.Click += new System.EventHandler(this.btnCheck_Click);
            // 
            // txbCheck
            // 
            this.txbCheck.AcceptsReturn = true;
            this.txbCheck.AcceptsTab = true;
            this.txbCheck.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.txbCheck.Location = new System.Drawing.Point(9, 32);
            this.txbCheck.Multiline = true;
            this.txbCheck.Name = "txbCheck";
            this.txbCheck.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.txbCheck.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.txbCheck.Size = new System.Drawing.Size(562, 74);
            this.txbCheck.TabIndex = 7;
            // 
            // label2
            // 
            label2.AutoSize = true;
            label2.Location = new System.Drawing.Point(9, 16);
            label2.Name = "label2";
            label2.Size = new System.Drawing.Size(268, 13);
            label2.TabIndex = 9;
            label2.Text = "Type or paste text here to run morphologic profiling for it";
            // 
            // groupBox3
            // 
            groupBox3.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            groupBox3.Controls.Add(this.txbLogger);
            groupBox3.Location = new System.Drawing.Point(3, 186);
            groupBox3.Name = "groupBox3";
            groupBox3.Size = new System.Drawing.Size(577, 206);
            groupBox3.TabIndex = 10;
            groupBox3.TabStop = false;
            groupBox3.Text = "Logger";
            // 
            // txbLogger
            // 
            this.txbLogger.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.txbLogger.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.txbLogger.Location = new System.Drawing.Point(9, 19);
            this.txbLogger.Multiline = true;
            this.txbLogger.Name = "txbLogger";
            this.txbLogger.ReadOnly = true;
            this.txbLogger.ScrollBars = System.Windows.Forms.ScrollBars.Both;
            this.txbLogger.Size = new System.Drawing.Size(562, 181);
            this.txbLogger.TabIndex = 6;
            // 
            // groupBox2
            // 
            groupBox2.Controls.Add(this.checkBox1);
            groupBox2.Controls.Add(this.trvDictView);
            groupBox2.Controls.Add(this.chbLoadMorphData);
            groupBox2.Controls.Add(this.button1);
            groupBox2.Controls.Add(this.btnLoadHSpellFolder);
            groupBox2.Dock = System.Windows.Forms.DockStyle.Fill;
            groupBox2.Location = new System.Drawing.Point(0, 0);
            groupBox2.Name = "groupBox2";
            groupBox2.Size = new System.Drawing.Size(320, 392);
            groupBox2.TabIndex = 5;
            groupBox2.TabStop = false;
            groupBox2.Text = "Dictionary viewer";
            // 
            // checkBox1
            // 
            this.checkBox1.AutoSize = true;
            this.checkBox1.Checked = true;
            this.checkBox1.CheckState = System.Windows.Forms.CheckState.Checked;
            this.checkBox1.Location = new System.Drawing.Point(6, 62);
            this.checkBox1.Name = "checkBox1";
            this.checkBox1.Size = new System.Drawing.Size(136, 17);
            this.checkBox1.TabIndex = 2;
            this.checkBox1.Text = "Load morpholigcal data";
            this.checkBox1.UseVisualStyleBackColor = true;
            // 
            // trvDictView
            // 
            this.trvDictView.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.trvDictView.Location = new System.Drawing.Point(6, 90);
            this.trvDictView.Name = "trvDictView";
            this.trvDictView.Size = new System.Drawing.Size(308, 296);
            this.trvDictView.TabIndex = 4;
            this.trvDictView.BeforeExpand += new System.Windows.Forms.TreeViewCancelEventHandler(this.trvDictView_BeforeExpand);
            // 
            // chbLoadMorphData
            // 
            this.chbLoadMorphData.AutoSize = true;
            this.chbLoadMorphData.Checked = true;
            this.chbLoadMorphData.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbLoadMorphData.Location = new System.Drawing.Point(6, 62);
            this.chbLoadMorphData.Name = "chbLoadMorphData";
            this.chbLoadMorphData.Size = new System.Drawing.Size(136, 17);
            this.chbLoadMorphData.TabIndex = 2;
            this.chbLoadMorphData.Text = "Load morpholigcal data";
            this.chbLoadMorphData.UseVisualStyleBackColor = true;
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(6, 19);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(158, 37);
            this.button1.TabIndex = 1;
            this.button1.Text = "Load from HSpell folder";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.btnLoadHSpellFolder_Click);
            // 
            // btnLoadHSpellFolder
            // 
            this.btnLoadHSpellFolder.Location = new System.Drawing.Point(6, 19);
            this.btnLoadHSpellFolder.Name = "btnLoadHSpellFolder";
            this.btnLoadHSpellFolder.Size = new System.Drawing.Size(158, 37);
            this.btnLoadHSpellFolder.TabIndex = 1;
            this.btnLoadHSpellFolder.Text = "Load from HSpell folder";
            this.btnLoadHSpellFolder.UseVisualStyleBackColor = true;
            this.btnLoadHSpellFolder.Click += new System.EventHandler(this.btnLoadHSpellFolder_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Location = new System.Drawing.Point(25, 71);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(201, 340);
            this.groupBox1.TabIndex = 5;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Dictionary viewer";
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(907, 392);
            this.Controls.Add(splitContainer1);
            this.Name = "MainForm";
            this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Hide;
            this.Text = "HebMorph Test Application";
            splitContainer1.Panel1.ResumeLayout(false);
            splitContainer1.Panel2.ResumeLayout(false);
            splitContainer1.ResumeLayout(false);
            groupBox4.ResumeLayout(false);
            groupBox4.PerformLayout();
            groupBox3.ResumeLayout(false);
            groupBox3.PerformLayout();
            groupBox2.ResumeLayout(false);
            groupBox2.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button btnLoadHSpellFolder;
        private System.Windows.Forms.CheckBox chbLoadMorphData;
        private System.Windows.Forms.TreeView trvDictView;
        private System.Windows.Forms.TextBox txbLogger;
        private System.Windows.Forms.TextBox txbCheck;
        private System.Windows.Forms.Button btnCheck;
        private System.Windows.Forms.CheckBox checkBox1;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Button btnTestRadix;
    }
}

