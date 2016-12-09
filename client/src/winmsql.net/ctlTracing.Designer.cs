namespace RedPrairie.MCS.WinMSQL
{
    partial class ctlTracing
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
			this.lblTraceFile = new System.Windows.Forms.Label();
			this.txtFileName = new System.Windows.Forms.TextBox();
			this.lblTraceLvls = new System.Windows.Forms.Label();
			this.chkAppFlowMsgs = new System.Windows.Forms.CheckBox();
			this.chkSQLLibCalls = new System.Windows.Forms.CheckBox();
			this.chkConMgrMsgs = new System.Windows.Forms.CheckBox();
			this.chkSrvFlwMsgs = new System.Windows.Forms.CheckBox();
			this.chkSrvArgs = new System.Windows.Forms.CheckBox();
			this.chkPerfStat = new System.Windows.Forms.CheckBox();
			this.chkCmdProf = new System.Windows.Forms.CheckBox();
			this.cmdOk = new System.Windows.Forms.Button();
			this.cmdCancel = new System.Windows.Forms.Button();
			this.groupBox1 = new System.Windows.Forms.GroupBox();
			this.groupBox1.SuspendLayout();
			this.SuspendLayout();
			// 
			// lblTraceFile
			// 
			this.lblTraceFile.AutoSize = true;
			this.lblTraceFile.Location = new System.Drawing.Point(6, 16);
			this.lblTraceFile.Name = "lblTraceFile";
			this.lblTraceFile.Size = new System.Drawing.Size(54, 13);
			this.lblTraceFile.TabIndex = 0;
			this.lblTraceFile.Text = "File Name";
			// 
			// txtFileName
			// 
			this.txtFileName.Location = new System.Drawing.Point(9, 32);
			this.txtFileName.Name = "txtFileName";
			this.txtFileName.Size = new System.Drawing.Size(184, 20);
			this.txtFileName.TabIndex = 1;
			this.txtFileName.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.txtFileName_KeyPress);
			// 
			// lblTraceLvls
			// 
			this.lblTraceLvls.AutoSize = true;
			this.lblTraceLvls.Location = new System.Drawing.Point(6, 60);
			this.lblTraceLvls.Name = "lblTraceLvls";
			this.lblTraceLvls.Size = new System.Drawing.Size(69, 13);
			this.lblTraceLvls.TabIndex = 3;
			this.lblTraceLvls.Text = "Trace Levels";
			// 
			// chkAppFlowMsgs
			// 
			this.chkAppFlowMsgs.AutoSize = true;
			this.chkAppFlowMsgs.Checked = true;
			this.chkAppFlowMsgs.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkAppFlowMsgs.Location = new System.Drawing.Point(9, 77);
			this.chkAppFlowMsgs.Name = "chkAppFlowMsgs";
			this.chkAppFlowMsgs.Size = new System.Drawing.Size(154, 17);
			this.chkAppFlowMsgs.TabIndex = 4;
			this.chkAppFlowMsgs.Text = "Application Flow Messages";
			this.chkAppFlowMsgs.UseVisualStyleBackColor = true;
			// 
			// chkSQLLibCalls
			// 
			this.chkSQLLibCalls.AutoSize = true;
			this.chkSQLLibCalls.Checked = true;
			this.chkSQLLibCalls.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkSQLLibCalls.Location = new System.Drawing.Point(9, 91);
			this.chkSQLLibCalls.Name = "chkSQLLibCalls";
			this.chkSQLLibCalls.Size = new System.Drawing.Size(86, 17);
			this.chkSQLLibCalls.TabIndex = 5;
			this.chkSQLLibCalls.Text = "SQLLib Calls";
			this.chkSQLLibCalls.UseVisualStyleBackColor = true;
			// 
			// chkConMgrMsgs
			// 
			this.chkConMgrMsgs.AutoSize = true;
			this.chkConMgrMsgs.Checked = true;
			this.chkConMgrMsgs.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkConMgrMsgs.Location = new System.Drawing.Point(9, 105);
			this.chkConMgrMsgs.Name = "chkConMgrMsgs";
			this.chkConMgrMsgs.Size = new System.Drawing.Size(176, 17);
			this.chkConMgrMsgs.TabIndex = 6;
			this.chkConMgrMsgs.Text = "Manager Messages";
			this.chkConMgrMsgs.UseVisualStyleBackColor = true;
			// 
			// chkSrvFlwMsgs
			// 
			this.chkSrvFlwMsgs.AutoSize = true;
			this.chkSrvFlwMsgs.Checked = true;
			this.chkSrvFlwMsgs.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkSrvFlwMsgs.Location = new System.Drawing.Point(9, 119);
			this.chkSrvFlwMsgs.Name = "chkSrvFlwMsgs";
			this.chkSrvFlwMsgs.Size = new System.Drawing.Size(133, 17);
			this.chkSrvFlwMsgs.TabIndex = 7;
			this.chkSrvFlwMsgs.Text = "Server Flow Messages";
			this.chkSrvFlwMsgs.UseVisualStyleBackColor = true;
			// 
			// chkSrvArgs
			// 
			this.chkSrvArgs.AutoSize = true;
			this.chkSrvArgs.Checked = true;
			this.chkSrvArgs.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkSrvArgs.Location = new System.Drawing.Point(9, 133);
			this.chkSrvArgs.Name = "chkSrvArgs";
			this.chkSrvArgs.Size = new System.Drawing.Size(110, 17);
			this.chkSrvArgs.TabIndex = 8;
			this.chkSrvArgs.Text = "Server Arguments";
			this.chkSrvArgs.UseVisualStyleBackColor = true;
			// 
			// chkPerfStat
			// 
			this.chkPerfStat.AutoSize = true;
			this.chkPerfStat.Checked = true;
			this.chkPerfStat.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkPerfStat.Location = new System.Drawing.Point(9, 147);
			this.chkPerfStat.Name = "chkPerfStat";
			this.chkPerfStat.Size = new System.Drawing.Size(131, 17);
			this.chkPerfStat.TabIndex = 9;
			this.chkPerfStat.Text = "Performance Statistics";
			this.chkPerfStat.UseVisualStyleBackColor = true;
			// 
			// chkCmdProf
			// 
			this.chkCmdProf.AutoSize = true;
			this.chkCmdProf.Checked = true;
			this.chkCmdProf.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkCmdProf.Location = new System.Drawing.Point(9, 161);
			this.chkCmdProf.Name = "chkCmdProf";
			this.chkCmdProf.Size = new System.Drawing.Size(113, 17);
			this.chkCmdProf.TabIndex = 10;
			this.chkCmdProf.Text = "Command Profiling";
			this.chkCmdProf.UseVisualStyleBackColor = true;
			// 
			// cmdOk
			// 
			this.cmdOk.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.cmdOk.Location = new System.Drawing.Point(60, 208);
			this.cmdOk.Name = "cmdOk";
			this.cmdOk.Size = new System.Drawing.Size(75, 23);
			this.cmdOk.TabIndex = 11;
			this.cmdOk.Text = "&Ok";
			this.cmdOk.UseVisualStyleBackColor = true;
			this.cmdOk.Click += new System.EventHandler(this.cmdOk_Click);
			// 
			// cmdCancel
			// 
			this.cmdCancel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.cmdCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
			this.cmdCancel.Location = new System.Drawing.Point(141, 208);
			this.cmdCancel.Name = "cmdCancel";
			this.cmdCancel.Size = new System.Drawing.Size(75, 23);
			this.cmdCancel.TabIndex = 12;
			this.cmdCancel.Text = "&Cancel";
			this.cmdCancel.UseVisualStyleBackColor = true;
			this.cmdCancel.Click += new System.EventHandler(this.cmdCancel_Click);
			// 
			// groupBox1
			// 
			this.groupBox1.Controls.Add(this.chkCmdProf);
			this.groupBox1.Controls.Add(this.chkPerfStat);
			this.groupBox1.Controls.Add(this.chkSrvArgs);
			this.groupBox1.Controls.Add(this.chkSrvFlwMsgs);
			this.groupBox1.Controls.Add(this.chkConMgrMsgs);
			this.groupBox1.Controls.Add(this.chkSQLLibCalls);
			this.groupBox1.Controls.Add(this.lblTraceFile);
			this.groupBox1.Controls.Add(this.txtFileName);
			this.groupBox1.Controls.Add(this.lblTraceLvls);
			this.groupBox1.Controls.Add(this.chkAppFlowMsgs);
			this.groupBox1.Location = new System.Drawing.Point(12, 12);
			this.groupBox1.Name = "groupBox1";
			this.groupBox1.Size = new System.Drawing.Size(202, 190);
			this.groupBox1.TabIndex = 13;
			this.groupBox1.TabStop = false;
			this.groupBox1.Text = "Trace File Options";
			// 
			// ctlTracing
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.CancelButton = this.cmdCancel;
			this.ClientSize = new System.Drawing.Size(228, 243);
			this.Controls.Add(this.groupBox1);
			this.Controls.Add(this.cmdCancel);
			this.Controls.Add(this.cmdOk);
			this.MaximizeBox = false;
			this.MinimizeBox = false;
			this.Name = "ctlTracing";
			this.ShowInTaskbar = false;
			this.Text = "Tracing";
			this.Shown += new System.EventHandler(this.ctlTracing_Shown);
			this.groupBox1.ResumeLayout(false);
			this.groupBox1.PerformLayout();
			this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Label lblTraceFile;
		private System.Windows.Forms.TextBox txtFileName;
        private System.Windows.Forms.Label lblTraceLvls;
        private System.Windows.Forms.CheckBox chkAppFlowMsgs;
        private System.Windows.Forms.CheckBox chkSQLLibCalls;
        private System.Windows.Forms.CheckBox chkConMgrMsgs;
        private System.Windows.Forms.CheckBox chkSrvFlwMsgs;
        private System.Windows.Forms.CheckBox chkSrvArgs;
        private System.Windows.Forms.CheckBox chkPerfStat;
        private System.Windows.Forms.CheckBox chkCmdProf;
        private System.Windows.Forms.Button cmdOk;
        private System.Windows.Forms.Button cmdCancel;
		private System.Windows.Forms.GroupBox groupBox1;
    }
}
