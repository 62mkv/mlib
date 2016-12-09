namespace RedPrairie.MCS.WinMSQL
{
	partial class ctlFind
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
			this.lblFindWhat = new System.Windows.Forms.Label();
			this.cboFind = new System.Windows.Forms.ComboBox();
			this.grpbxLookIn = new System.Windows.Forms.GroupBox();
			this.chkResults = new System.Windows.Forms.RadioButton();
			this.chkCmdTxt = new System.Windows.Forms.RadioButton();
			this.grpbxFindOpts = new System.Windows.Forms.GroupBox();
			this.chkMatchWord = new System.Windows.Forms.CheckBox();
			this.chkMatchCase = new System.Windows.Forms.CheckBox();
			this.btnFindNext = new System.Windows.Forms.Button();
			this.lblReplaceWith = new System.Windows.Forms.Label();
			this.cboReplace = new System.Windows.Forms.ComboBox();
			this.btnReplace = new System.Windows.Forms.Button();
			this.btnReplaceAll = new System.Windows.Forms.Button();
			this.btnQuickReplace = new System.Windows.Forms.Button();
			this.btnQuickFind = new System.Windows.Forms.Button();
			this.grpbxLookIn.SuspendLayout();
			this.grpbxFindOpts.SuspendLayout();
			this.SuspendLayout();
			// 
			// lblFindWhat
			// 
			this.lblFindWhat.AutoSize = true;
			this.lblFindWhat.Location = new System.Drawing.Point(12, 44);
			this.lblFindWhat.Name = "lblFindWhat";
			this.lblFindWhat.Size = new System.Drawing.Size(53, 13);
			this.lblFindWhat.TabIndex = 0;
			this.lblFindWhat.Text = "Find what";
			// 
			// cboFind
			// 
			this.cboFind.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.cboFind.FormattingEnabled = true;
			this.cboFind.Location = new System.Drawing.Point(12, 60);
			this.cboFind.Name = "cboFind";
			this.cboFind.Size = new System.Drawing.Size(237, 21);
			this.cboFind.TabIndex = 1;
			this.cboFind.TextChanged += new System.EventHandler(this.cboFind_TextChanged);
			this.cboFind.KeyDown += new System.Windows.Forms.KeyEventHandler(this.cboFind_KeyDown);
			// 
			// grpbxLookIn
			// 
			this.grpbxLookIn.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.grpbxLookIn.Controls.Add(this.chkResults);
			this.grpbxLookIn.Controls.Add(this.chkCmdTxt);
			this.grpbxLookIn.Location = new System.Drawing.Point(12, 127);
			this.grpbxLookIn.Name = "grpbxLookIn";
			this.grpbxLookIn.Size = new System.Drawing.Size(237, 39);
			this.grpbxLookIn.TabIndex = 2;
			this.grpbxLookIn.TabStop = false;
			this.grpbxLookIn.Text = "Look in:";
			// 
			// chkResults
			// 
			this.chkResults.AutoSize = true;
			this.chkResults.Location = new System.Drawing.Point(109, 16);
			this.chkResults.Name = "chkResults";
			this.chkResults.Size = new System.Drawing.Size(60, 17);
			this.chkResults.TabIndex = 1;
			this.chkResults.TabStop = true;
			this.chkResults.Text = "Results";
			this.chkResults.UseVisualStyleBackColor = true;
			// 
			// chkCmdTxt
			// 
			this.chkCmdTxt.AutoSize = true;
			this.chkCmdTxt.Checked = true;
			this.chkCmdTxt.Location = new System.Drawing.Point(7, 16);
			this.chkCmdTxt.Name = "chkCmdTxt";
			this.chkCmdTxt.Size = new System.Drawing.Size(96, 17);
			this.chkCmdTxt.TabIndex = 0;
			this.chkCmdTxt.TabStop = true;
			this.chkCmdTxt.Text = "Command Text";
			this.chkCmdTxt.UseVisualStyleBackColor = true;
			this.chkCmdTxt.CheckedChanged += new System.EventHandler(this.chkCmdTxt_CheckedChanged);
			// 
			// grpbxFindOpts
			// 
			this.grpbxFindOpts.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.grpbxFindOpts.Controls.Add(this.chkMatchWord);
			this.grpbxFindOpts.Controls.Add(this.chkMatchCase);
			this.grpbxFindOpts.Location = new System.Drawing.Point(12, 172);
			this.grpbxFindOpts.Name = "grpbxFindOpts";
			this.grpbxFindOpts.Size = new System.Drawing.Size(237, 66);
			this.grpbxFindOpts.TabIndex = 3;
			this.grpbxFindOpts.TabStop = false;
			this.grpbxFindOpts.Text = "Find Options";
			// 
			// chkMatchWord
			// 
			this.chkMatchWord.AutoSize = true;
			this.chkMatchWord.Location = new System.Drawing.Point(7, 43);
			this.chkMatchWord.Name = "chkMatchWord";
			this.chkMatchWord.Size = new System.Drawing.Size(119, 17);
			this.chkMatchWord.TabIndex = 1;
			this.chkMatchWord.Text = "Match Whole Word";
			this.chkMatchWord.UseVisualStyleBackColor = true;
			// 
			// chkMatchCase
			// 
			this.chkMatchCase.AutoSize = true;
			this.chkMatchCase.Location = new System.Drawing.Point(6, 19);
			this.chkMatchCase.Name = "chkMatchCase";
			this.chkMatchCase.Size = new System.Drawing.Size(83, 17);
			this.chkMatchCase.TabIndex = 0;
			this.chkMatchCase.Text = "Match Case";
			this.chkMatchCase.UseVisualStyleBackColor = true;
			// 
			// btnFindNext
			// 
			this.btnFindNext.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
			this.btnFindNext.Location = new System.Drawing.Point(8, 244);
			this.btnFindNext.Name = "btnFindNext";
			this.btnFindNext.Size = new System.Drawing.Size(118, 23);
			this.btnFindNext.TabIndex = 4;
			this.btnFindNext.Text = "Find Next";
			this.btnFindNext.UseVisualStyleBackColor = true;
			this.btnFindNext.Click += new System.EventHandler(this.btnFindNext_Click);
			// 
			// lblReplaceWith
			// 
			this.lblReplaceWith.AutoSize = true;
			this.lblReplaceWith.Location = new System.Drawing.Point(12, 84);
			this.lblReplaceWith.Name = "lblReplaceWith";
			this.lblReplaceWith.Size = new System.Drawing.Size(72, 13);
			this.lblReplaceWith.TabIndex = 5;
			this.lblReplaceWith.Text = "Replace with:";
			// 
			// cboReplace
			// 
			this.cboReplace.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.cboReplace.FormattingEnabled = true;
			this.cboReplace.Location = new System.Drawing.Point(12, 100);
			this.cboReplace.Name = "cboReplace";
			this.cboReplace.Size = new System.Drawing.Size(237, 21);
			this.cboReplace.TabIndex = 6;
			this.cboReplace.KeyDown += new System.Windows.Forms.KeyEventHandler(this.cboReplace_KeyDown);
			// 
			// btnReplace
			// 
			this.btnReplace.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
			this.btnReplace.Location = new System.Drawing.Point(131, 244);
			this.btnReplace.Name = "btnReplace";
			this.btnReplace.Size = new System.Drawing.Size(118, 23);
			this.btnReplace.TabIndex = 7;
			this.btnReplace.Text = "Replace";
			this.btnReplace.UseVisualStyleBackColor = true;
			this.btnReplace.Click += new System.EventHandler(this.btnReplace_Click);
			// 
			// btnReplaceAll
			// 
			this.btnReplaceAll.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
			this.btnReplaceAll.Location = new System.Drawing.Point(131, 273);
			this.btnReplaceAll.Name = "btnReplaceAll";
			this.btnReplaceAll.Size = new System.Drawing.Size(118, 23);
			this.btnReplaceAll.TabIndex = 8;
			this.btnReplaceAll.Text = "Replace All";
			this.btnReplaceAll.UseVisualStyleBackColor = true;
			this.btnReplaceAll.Click += new System.EventHandler(this.btnReplaceAll_Click);
			// 
			// btnQuickReplace
			// 
			this.btnQuickReplace.FlatAppearance.BorderColor = System.Drawing.SystemColors.HotTrack;
			this.btnQuickReplace.FlatAppearance.MouseDownBackColor = System.Drawing.SystemColors.HotTrack;
			this.btnQuickReplace.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.MenuHighlight;
			this.btnQuickReplace.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
			this.btnQuickReplace.Location = new System.Drawing.Point(136, 12);
			this.btnQuickReplace.Name = "btnQuickReplace";
			this.btnQuickReplace.Size = new System.Drawing.Size(118, 23);
			this.btnQuickReplace.TabIndex = 10;
			this.btnQuickReplace.Text = "Quick Replace";
			this.btnQuickReplace.UseVisualStyleBackColor = true;
			this.btnQuickReplace.Click += new System.EventHandler(this.btnQuickReplace_Click);
			// 
			// btnQuickFind
			// 
			this.btnQuickFind.FlatAppearance.BorderColor = System.Drawing.SystemColors.HotTrack;
			this.btnQuickFind.FlatAppearance.MouseDownBackColor = System.Drawing.SystemColors.HotTrack;
			this.btnQuickFind.FlatAppearance.MouseOverBackColor = System.Drawing.SystemColors.Highlight;
			this.btnQuickFind.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
			this.btnQuickFind.Location = new System.Drawing.Point(12, 12);
			this.btnQuickFind.Name = "btnQuickFind";
			this.btnQuickFind.Size = new System.Drawing.Size(118, 23);
			this.btnQuickFind.TabIndex = 9;
			this.btnQuickFind.Text = "Quick Find";
			this.btnQuickFind.UseVisualStyleBackColor = true;
			this.btnQuickFind.Click += new System.EventHandler(this.btnQuickFind_Click);
			// 
			// ctlFind
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(259, 302);
			this.Controls.Add(this.btnQuickReplace);
			this.Controls.Add(this.btnQuickFind);
			this.Controls.Add(this.btnReplaceAll);
			this.Controls.Add(this.btnReplace);
			this.Controls.Add(this.cboReplace);
			this.Controls.Add(this.lblReplaceWith);
			this.Controls.Add(this.btnFindNext);
			this.Controls.Add(this.grpbxFindOpts);
			this.Controls.Add(this.grpbxLookIn);
			this.Controls.Add(this.cboFind);
			this.Controls.Add(this.lblFindWhat);
			this.KeyPreview = true;
			this.MaximizeBox = false;
			this.MinimizeBox = false;
			this.Name = "ctlFind";
			this.ShowIcon = false;
			this.ShowInTaskbar = false;
			this.Text = "Find and Replace";
			this.KeyDown += new System.Windows.Forms.KeyEventHandler(this.ctlFind_KeyDown);
			this.grpbxLookIn.ResumeLayout(false);
			this.grpbxLookIn.PerformLayout();
			this.grpbxFindOpts.ResumeLayout(false);
			this.grpbxFindOpts.PerformLayout();
			this.ResumeLayout(false);
			this.PerformLayout();

		}

		#endregion

		private System.Windows.Forms.Label lblFindWhat;
		private System.Windows.Forms.ComboBox cboFind;
		private System.Windows.Forms.GroupBox grpbxLookIn;
		private System.Windows.Forms.GroupBox grpbxFindOpts;
		private System.Windows.Forms.CheckBox chkMatchWord;
		private System.Windows.Forms.CheckBox chkMatchCase;
		private System.Windows.Forms.Button btnFindNext;
		private System.Windows.Forms.Label lblReplaceWith;
		private System.Windows.Forms.ComboBox cboReplace;
		private System.Windows.Forms.Button btnReplace;
		private System.Windows.Forms.Button btnReplaceAll;
		private System.Windows.Forms.Button btnQuickReplace;
		private System.Windows.Forms.Button btnQuickFind;
		private System.Windows.Forms.RadioButton chkResults;
		private System.Windows.Forms.RadioButton chkCmdTxt;
	}
}