namespace RedPrairie.MCS.WinMSQL
{
	partial class ctlOptions
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
            this.chkCommentFormat = new System.Windows.Forms.CheckBox();
            this.chkBrktNewLine = new System.Windows.Forms.CheckBox();
            this.chkPipeNewLine = new System.Windows.Forms.CheckBox();
            this.chkCommaNewLine = new System.Windows.Forms.CheckBox();
            this.txtColsPerLine = new System.Windows.Forms.TextBox();
            this.chkTrimSrc = new System.Windows.Forms.CheckBox();
            this.chkTrimExtra = new System.Windows.Forms.CheckBox();
            this.chkCvtLwrCase = new System.Windows.Forms.CheckBox();
            this.chkParseString = new System.Windows.Forms.CheckBox();
            this.cmdDefault = new System.Windows.Forms.Button();
            this.cmdCancel = new System.Windows.Forms.Button();
            this.cmdOk = new System.Windows.Forms.Button();
            this.lblColsPerLine = new System.Windows.Forms.Label();
            this.chkEnter = new System.Windows.Forms.CheckBox();
            this.chkDebug = new System.Windows.Forms.CheckBox();
            this.chkIntegrator = new System.Windows.Forms.CheckBox();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.chkWarnMsg = new System.Windows.Forms.CheckBox();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.lblArguments = new System.Windows.Forms.Label();
            this.lblExecutable = new System.Windows.Forms.Label();
            this.txtArgs = new System.Windows.Forms.TextBox();
            this.btnOpenFile = new System.Windows.Forms.Button();
            this.txtTraceViewer = new System.Windows.Forms.TextBox();
            this.chkExtTraceViewer = new System.Windows.Forms.CheckBox();
            this.openFileDialog1 = new System.Windows.Forms.OpenFileDialog();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.cmdEditColors = new System.Windows.Forms.Button();
            this.chkHighlightStrings = new System.Windows.Forms.CheckBox();
            this.btnOpenKeywordFile = new System.Windows.Forms.Button();
            this.txtKeywordPath = new System.Windows.Forms.TextBox();
            this.lblKeywordPath = new System.Windows.Forms.Label();
            this.chkHighlightSyntax = new System.Windows.Forms.CheckBox();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.SuspendLayout();
            // 
            // chkCommentFormat
            // 
            this.chkCommentFormat.AutoSize = true;
            this.chkCommentFormat.Checked = true;
            this.chkCommentFormat.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkCommentFormat.Location = new System.Drawing.Point(152, 39);
            this.chkCommentFormat.Name = "chkCommentFormat";
            this.chkCommentFormat.Size = new System.Drawing.Size(133, 17);
            this.chkCommentFormat.TabIndex = 14;
            this.chkCommentFormat.Text = "Keep Comment Format";
            this.chkCommentFormat.UseVisualStyleBackColor = true;
            // 
            // chkBrktNewLine
            // 
            this.chkBrktNewLine.AutoSize = true;
            this.chkBrktNewLine.Checked = true;
            this.chkBrktNewLine.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkBrktNewLine.Location = new System.Drawing.Point(6, 16);
            this.chkBrktNewLine.Name = "chkBrktNewLine";
            this.chkBrktNewLine.Size = new System.Drawing.Size(116, 17);
            this.chkBrktNewLine.TabIndex = 2;
            this.chkBrktNewLine.Text = "Put \'{\' on New Line";
            this.chkBrktNewLine.UseVisualStyleBackColor = true;
            // 
            // chkPipeNewLine
            // 
            this.chkPipeNewLine.AutoSize = true;
            this.chkPipeNewLine.Checked = true;
            this.chkPipeNewLine.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkPipeNewLine.Location = new System.Drawing.Point(6, 39);
            this.chkPipeNewLine.Name = "chkPipeNewLine";
            this.chkPipeNewLine.Size = new System.Drawing.Size(114, 17);
            this.chkPipeNewLine.TabIndex = 4;
            this.chkPipeNewLine.Text = "Put \'|\' on New Line";
            this.chkPipeNewLine.UseVisualStyleBackColor = true;
            // 
            // chkCommaNewLine
            // 
            this.chkCommaNewLine.AutoSize = true;
            this.chkCommaNewLine.Checked = true;
            this.chkCommaNewLine.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkCommaNewLine.Location = new System.Drawing.Point(6, 62);
            this.chkCommaNewLine.Name = "chkCommaNewLine";
            this.chkCommaNewLine.Size = new System.Drawing.Size(115, 17);
            this.chkCommaNewLine.TabIndex = 6;
            this.chkCommaNewLine.Text = "Put \',\' on New Line";
            this.chkCommaNewLine.UseVisualStyleBackColor = true;
            // 
            // txtColsPerLine
            // 
            this.txtColsPerLine.Location = new System.Drawing.Point(152, 106);
            this.txtColsPerLine.Name = "txtColsPerLine";
            this.txtColsPerLine.Size = new System.Drawing.Size(27, 20);
            this.txtColsPerLine.TabIndex = 20;
            this.txtColsPerLine.Text = "50";
            this.txtColsPerLine.TextChanged += new System.EventHandler(this.txtColsPerLine_TextChanged);
            // 
            // chkTrimSrc
            // 
            this.chkTrimSrc.AutoSize = true;
            this.chkTrimSrc.Location = new System.Drawing.Point(6, 85);
            this.chkTrimSrc.Name = "chkTrimSrc";
            this.chkTrimSrc.Size = new System.Drawing.Size(83, 17);
            this.chkTrimSrc.TabIndex = 8;
            this.chkTrimSrc.Text = "Trim Source";
            this.chkTrimSrc.UseVisualStyleBackColor = true;
            // 
            // chkTrimExtra
            // 
            this.chkTrimExtra.AutoSize = true;
            this.chkTrimExtra.Checked = true;
            this.chkTrimExtra.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkTrimExtra.Location = new System.Drawing.Point(6, 108);
            this.chkTrimExtra.Name = "chkTrimExtra";
            this.chkTrimExtra.Size = new System.Drawing.Size(142, 17);
            this.chkTrimExtra.TabIndex = 10;
            this.chkTrimExtra.Text = "Trim Extra Blank Spaces";
            this.chkTrimExtra.UseVisualStyleBackColor = true;
            // 
            // chkCvtLwrCase
            // 
            this.chkCvtLwrCase.AutoSize = true;
            this.chkCvtLwrCase.Location = new System.Drawing.Point(152, 16);
            this.chkCvtLwrCase.Name = "chkCvtLwrCase";
            this.chkCvtLwrCase.Size = new System.Drawing.Size(134, 17);
            this.chkCvtLwrCase.TabIndex = 12;
            this.chkCvtLwrCase.Text = "Convert to Lower Case";
            this.chkCvtLwrCase.UseVisualStyleBackColor = true;
            // 
            // chkParseString
            // 
            this.chkParseString.AutoSize = true;
            this.chkParseString.Checked = true;
            this.chkParseString.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkParseString.Location = new System.Drawing.Point(152, 62);
            this.chkParseString.Name = "chkParseString";
            this.chkParseString.Size = new System.Drawing.Size(118, 17);
            this.chkParseString.TabIndex = 16;
            this.chkParseString.Text = "Parse within Strings";
            this.chkParseString.UseVisualStyleBackColor = true;
            // 
            // cmdDefault
            // 
            this.cmdDefault.Location = new System.Drawing.Point(315, 409);
            this.cmdDefault.Name = "cmdDefault";
            this.cmdDefault.Size = new System.Drawing.Size(75, 23);
            this.cmdDefault.TabIndex = 17;
            this.cmdDefault.Text = "Default";
            this.cmdDefault.UseVisualStyleBackColor = true;
            this.cmdDefault.Click += new System.EventHandler(this.cmdDefault_Click);
            // 
            // cmdCancel
            // 
            this.cmdCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cmdCancel.Location = new System.Drawing.Point(234, 409);
            this.cmdCancel.Name = "cmdCancel";
            this.cmdCancel.Size = new System.Drawing.Size(75, 23);
            this.cmdCancel.TabIndex = 16;
            this.cmdCancel.Text = "Cancel";
            this.cmdCancel.UseVisualStyleBackColor = true;
            this.cmdCancel.Click += new System.EventHandler(this.cmdCancel_Click);
            // 
            // cmdOk
            // 
            this.cmdOk.Location = new System.Drawing.Point(153, 409);
            this.cmdOk.Name = "cmdOk";
            this.cmdOk.Size = new System.Drawing.Size(75, 23);
            this.cmdOk.TabIndex = 15;
            this.cmdOk.Text = "Ok";
            this.cmdOk.UseVisualStyleBackColor = true;
            this.cmdOk.Click += new System.EventHandler(this.cmdOk_Click);
            // 
            // lblColsPerLine
            // 
            this.lblColsPerLine.AutoSize = true;
            this.lblColsPerLine.Location = new System.Drawing.Point(185, 109);
            this.lblColsPerLine.Name = "lblColsPerLine";
            this.lblColsPerLine.Size = new System.Drawing.Size(136, 13);
            this.lblColsPerLine.TabIndex = 17;
            this.lblColsPerLine.Text = "Columns Per Comment Line";
            // 
            // chkEnter
            // 
            this.chkEnter.AutoSize = true;
            this.chkEnter.Checked = true;
            this.chkEnter.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkEnter.Location = new System.Drawing.Point(6, 13);
            this.chkEnter.Name = "chkEnter";
            this.chkEnter.Size = new System.Drawing.Size(160, 17);
            this.chkEnter.TabIndex = 2;
            this.chkEnter.Text = "\'Enter Key\' inserts a new line";
            this.chkEnter.UseVisualStyleBackColor = true;
            // 
            // chkDebug
            // 
            this.chkDebug.AutoSize = true;
            this.chkDebug.Location = new System.Drawing.Point(152, 85);
            this.chkDebug.Name = "chkDebug";
            this.chkDebug.Size = new System.Drawing.Size(58, 17);
            this.chkDebug.TabIndex = 18;
            this.chkDebug.Text = "Debug";
            this.chkDebug.UseVisualStyleBackColor = true;
            // 
            // chkIntegrator
            // 
            this.chkIntegrator.AutoSize = true;
            this.chkIntegrator.Checked = true;
            this.chkIntegrator.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkIntegrator.Location = new System.Drawing.Point(6, 36);
            this.chkIntegrator.Name = "chkIntegrator";
            this.chkIntegrator.Size = new System.Drawing.Size(109, 17);
            this.chkIntegrator.TabIndex = 6;
            this.chkIntegrator.Text = "Integrator Binding";
            this.chkIntegrator.UseVisualStyleBackColor = true;
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.chkTrimSrc);
            this.groupBox1.Controls.Add(this.chkBrktNewLine);
            this.groupBox1.Controls.Add(this.chkPipeNewLine);
            this.groupBox1.Controls.Add(this.chkDebug);
            this.groupBox1.Controls.Add(this.chkCommaNewLine);
            this.groupBox1.Controls.Add(this.chkCvtLwrCase);
            this.groupBox1.Controls.Add(this.lblColsPerLine);
            this.groupBox1.Controls.Add(this.chkTrimExtra);
            this.groupBox1.Controls.Add(this.txtColsPerLine);
            this.groupBox1.Controls.Add(this.chkParseString);
            this.groupBox1.Controls.Add(this.chkCommentFormat);
            this.groupBox1.Location = new System.Drawing.Point(12, 12);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(379, 131);
            this.groupBox1.TabIndex = 0;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Formatting Options";
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.chkWarnMsg);
            this.groupBox2.Controls.Add(this.chkIntegrator);
            this.groupBox2.Controls.Add(this.chkEnter);
            this.groupBox2.Location = new System.Drawing.Point(12, 149);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(379, 82);
            this.groupBox2.TabIndex = 2;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "General Options";
            // 
            // chkWarnMsg
            // 
            this.chkWarnMsg.AutoSize = true;
            this.chkWarnMsg.Location = new System.Drawing.Point(6, 59);
            this.chkWarnMsg.Name = "chkWarnMsg";
            this.chkWarnMsg.Size = new System.Drawing.Size(178, 17);
            this.chkWarnMsg.TabIndex = 8;
            this.chkWarnMsg.Text = "Warn when closing multiple tabs";
            this.chkWarnMsg.UseVisualStyleBackColor = true;
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.lblArguments);
            this.groupBox3.Controls.Add(this.lblExecutable);
            this.groupBox3.Controls.Add(this.txtArgs);
            this.groupBox3.Controls.Add(this.btnOpenFile);
            this.groupBox3.Controls.Add(this.txtTraceViewer);
            this.groupBox3.Controls.Add(this.chkExtTraceViewer);
            this.groupBox3.Location = new System.Drawing.Point(12, 237);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(379, 95);
            this.groupBox3.TabIndex = 18;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Trace Viewer";
            // 
            // lblArguments
            // 
            this.lblArguments.AutoSize = true;
            this.lblArguments.Enabled = false;
            this.lblArguments.Location = new System.Drawing.Point(9, 71);
            this.lblArguments.Name = "lblArguments";
            this.lblArguments.Size = new System.Drawing.Size(60, 13);
            this.lblArguments.TabIndex = 5;
            this.lblArguments.Text = "Arguments:";
            // 
            // lblExecutable
            // 
            this.lblExecutable.AutoSize = true;
            this.lblExecutable.Enabled = false;
            this.lblExecutable.Location = new System.Drawing.Point(6, 45);
            this.lblExecutable.Name = "lblExecutable";
            this.lblExecutable.Size = new System.Drawing.Size(63, 13);
            this.lblExecutable.TabIndex = 4;
            this.lblExecutable.Text = "Executable:";
            // 
            // txtArgs
            // 
            this.txtArgs.Enabled = false;
            this.txtArgs.Location = new System.Drawing.Point(73, 68);
            this.txtArgs.Name = "txtArgs";
            this.txtArgs.Size = new System.Drawing.Size(203, 20);
            this.txtArgs.TabIndex = 3;
            // 
            // btnOpenFile
            // 
            this.btnOpenFile.Enabled = false;
            this.btnOpenFile.Location = new System.Drawing.Point(282, 41);
            this.btnOpenFile.Name = "btnOpenFile";
            this.btnOpenFile.Size = new System.Drawing.Size(25, 23);
            this.btnOpenFile.TabIndex = 2;
            this.btnOpenFile.Text = "...";
            this.btnOpenFile.UseVisualStyleBackColor = true;
            this.btnOpenFile.Click += new System.EventHandler(this.btnOpenFile_Click);
            // 
            // txtTraceViewer
            // 
            this.txtTraceViewer.Enabled = false;
            this.txtTraceViewer.Location = new System.Drawing.Point(73, 42);
            this.txtTraceViewer.Name = "txtTraceViewer";
            this.txtTraceViewer.Size = new System.Drawing.Size(203, 20);
            this.txtTraceViewer.TabIndex = 1;
            // 
            // chkExtTraceViewer
            // 
            this.chkExtTraceViewer.AutoSize = true;
            this.chkExtTraceViewer.Location = new System.Drawing.Point(6, 19);
            this.chkExtTraceViewer.Name = "chkExtTraceViewer";
            this.chkExtTraceViewer.Size = new System.Drawing.Size(152, 17);
            this.chkExtTraceViewer.TabIndex = 0;
            this.chkExtTraceViewer.Text = "Use External Trace Viewer";
            this.chkExtTraceViewer.UseVisualStyleBackColor = true;
            this.chkExtTraceViewer.CheckedChanged += new System.EventHandler(this.chkTraceViewer_CheckedChanged);
            // 
            // openFileDialog1
            // 
            this.openFileDialog1.FileName = "openFileDialog1";
            // 
            // groupBox4
            // 
            this.groupBox4.Controls.Add(this.cmdEditColors);
            this.groupBox4.Controls.Add(this.chkHighlightStrings);
            this.groupBox4.Controls.Add(this.btnOpenKeywordFile);
            this.groupBox4.Controls.Add(this.txtKeywordPath);
            this.groupBox4.Controls.Add(this.lblKeywordPath);
            this.groupBox4.Controls.Add(this.chkHighlightSyntax);
            this.groupBox4.Location = new System.Drawing.Point(12, 338);
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size(378, 65);
            this.groupBox4.TabIndex = 19;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "Syntax Highlighting";
            // 
            // cmdEditColors
            // 
            this.cmdEditColors.Location = new System.Drawing.Point(269, 16);
            this.cmdEditColors.Name = "cmdEditColors";
            this.cmdEditColors.Size = new System.Drawing.Size(103, 20);
            this.cmdEditColors.TabIndex = 5;
            this.cmdEditColors.Text = "Edit Colors";
            this.cmdEditColors.UseVisualStyleBackColor = true;
            this.cmdEditColors.Click += new System.EventHandler(this.cmdEditColors_Click);
            // 
            // chkHighlightStrings
            // 
            this.chkHighlightStrings.AutoSize = true;
            this.chkHighlightStrings.Location = new System.Drawing.Point(171, 19);
            this.chkHighlightStrings.Name = "chkHighlightStrings";
            this.chkHighlightStrings.Size = new System.Drawing.Size(102, 17);
            this.chkHighlightStrings.TabIndex = 4;
            this.chkHighlightStrings.Text = "Highlight Strings";
            this.chkHighlightStrings.UseVisualStyleBackColor = true;
            // 
            // btnOpenKeywordFile
            // 
            this.btnOpenKeywordFile.Enabled = false;
            this.btnOpenKeywordFile.Location = new System.Drawing.Point(282, 35);
            this.btnOpenKeywordFile.Name = "btnOpenKeywordFile";
            this.btnOpenKeywordFile.Size = new System.Drawing.Size(25, 23);
            this.btnOpenKeywordFile.TabIndex = 3;
            this.btnOpenKeywordFile.Text = "...";
            this.btnOpenKeywordFile.UseVisualStyleBackColor = true;
            this.btnOpenKeywordFile.Click += new System.EventHandler(this.btnOpenKeywordFile_Click);
            // 
            // txtKeywordPath
            // 
            this.txtKeywordPath.Location = new System.Drawing.Point(105, 38);
            this.txtKeywordPath.Name = "txtKeywordPath";
            this.txtKeywordPath.Size = new System.Drawing.Size(171, 20);
            this.txtKeywordPath.TabIndex = 2;
            // 
            // lblKeywordPath
            // 
            this.lblKeywordPath.AutoSize = true;
            this.lblKeywordPath.Location = new System.Drawing.Point(6, 41);
            this.lblKeywordPath.Name = "lblKeywordPath";
            this.lblKeywordPath.Size = new System.Drawing.Size(95, 13);
            this.lblKeywordPath.TabIndex = 1;
            this.lblKeywordPath.Text = "Keyword File Path:";
            // 
            // chkHighlightSyntax
            // 
            this.chkHighlightSyntax.AutoSize = true;
            this.chkHighlightSyntax.Location = new System.Drawing.Point(6, 19);
            this.chkHighlightSyntax.Name = "chkHighlightSyntax";
            this.chkHighlightSyntax.Size = new System.Drawing.Size(152, 17);
            this.chkHighlightSyntax.TabIndex = 0;
            this.chkHighlightSyntax.Text = "Enable Syntax Highlighting";
            this.chkHighlightSyntax.UseVisualStyleBackColor = true;
            this.chkHighlightSyntax.CheckedChanged += new System.EventHandler(this.chkHighlightSyntax_CheckedChanged);
            // 
            // ctlOptions
            // 
            this.AcceptButton = this.cmdOk;
            this.CancelButton = this.cmdCancel;
            this.ClientSize = new System.Drawing.Size(403, 439);
            this.Controls.Add(this.groupBox4);
            this.Controls.Add(this.groupBox3);
            this.Controls.Add(this.groupBox2);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.cmdOk);
            this.Controls.Add(this.cmdDefault);
            this.Controls.Add(this.cmdCancel);
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.MinimumSize = new System.Drawing.Size(411, 410);
            this.Name = "ctlOptions";
            this.ShowInTaskbar = false;
            this.Text = "Options";
            this.Shown += new System.EventHandler(this.ctlOptions_Shown);
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            this.groupBox4.ResumeLayout(false);
            this.groupBox4.PerformLayout();
            this.ResumeLayout(false);

		}

		#endregion

		private System.Windows.Forms.CheckBox chkCommentFormat;
		private System.Windows.Forms.CheckBox chkBrktNewLine;
		private System.Windows.Forms.CheckBox chkPipeNewLine;
		private System.Windows.Forms.CheckBox chkCommaNewLine;
		private System.Windows.Forms.TextBox txtColsPerLine;
		private System.Windows.Forms.CheckBox chkTrimSrc;
		private System.Windows.Forms.CheckBox chkTrimExtra;
		private System.Windows.Forms.CheckBox chkCvtLwrCase;
		private System.Windows.Forms.CheckBox chkParseString;
		private System.Windows.Forms.Button cmdDefault;
		private System.Windows.Forms.Button cmdCancel;
		private System.Windows.Forms.Button cmdOk;
		private System.Windows.Forms.Label lblColsPerLine;
		private System.Windows.Forms.CheckBox chkEnter;
        private System.Windows.Forms.CheckBox chkDebug;
		private System.Windows.Forms.CheckBox chkIntegrator;
		private System.Windows.Forms.GroupBox groupBox1;
		private System.Windows.Forms.GroupBox groupBox2;
		private System.Windows.Forms.GroupBox groupBox3;
		private System.Windows.Forms.Button btnOpenFile;
		private System.Windows.Forms.TextBox txtTraceViewer;
		private System.Windows.Forms.CheckBox chkExtTraceViewer;
		private System.Windows.Forms.OpenFileDialog openFileDialog1;
		private System.Windows.Forms.TextBox txtArgs;
		private System.Windows.Forms.Label lblArguments;
        private System.Windows.Forms.Label lblExecutable;
        private System.Windows.Forms.CheckBox chkWarnMsg;
        private System.Windows.Forms.GroupBox groupBox4;
        private System.Windows.Forms.CheckBox chkHighlightSyntax;
        private System.Windows.Forms.TextBox txtKeywordPath;
        private System.Windows.Forms.Label lblKeywordPath;
        private System.Windows.Forms.Button btnOpenKeywordFile;
        private System.Windows.Forms.CheckBox chkHighlightStrings;
        private System.Windows.Forms.Button cmdEditColors;
	}
}