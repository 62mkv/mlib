namespace RedPrairie.MCS.WinMSQL
{
    partial class winMSQLTab
    {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components;

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

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.splitContainer1 = new System.Windows.Forms.SplitContainer();
            this.panel1 = new System.Windows.Forms.Panel();
            this.txtCmd = new System.Windows.Forms.RichTextBox();
            this.cmdContextMenu = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.cutToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.copyToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.pasteToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.pnlConnection = new System.Windows.Forms.Panel();
            this.btnConnect = new System.Windows.Forms.Button();
            this.lblServerEnv = new System.Windows.Forms.Label();
            this.cbxServer = new System.Windows.Forms.ComboBox();
            this.chkAutoCommit = new System.Windows.Forms.CheckBox();
            this.txtPswd = new System.Windows.Forms.TextBox();
            this.lblPswd = new System.Windows.Forms.Label();
            this.txtUser = new System.Windows.Forms.TextBox();
            this.lblUser = new System.Windows.Forms.Label();
            this.txtEnv = new System.Windows.Forms.TextBox();
            this.lblEnv = new System.Windows.Forms.Label();
            this.gridMst = new DevExpress.XtraGrid.GridControl();
            this.gridContextMenu = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.copyCellToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.copyRowToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.copyAllRowsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.copyPublishToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.gridView2 = new DevExpress.XtraGrid.Views.Grid.GridView();
            this.toolTipController = new DevExpress.Utils.ToolTipController(this.components);
            this.flowLayoutPanel1 = new System.Windows.Forms.FlowLayoutPanel();
            this.lblStatus = new System.Windows.Forms.Label();
            this.lblSts = new System.Windows.Forms.Label();
            this.lblRowsReturned = new System.Windows.Forms.Label();
            this.lblRowsRet = new System.Windows.Forms.Label();
            this.lblElapsedTime = new System.Windows.Forms.Label();
            this.lblElpsdTime = new System.Windows.Forms.Label();
            this.lblExecuteTime = new System.Windows.Forms.Label();
            this.lblExecTime = new System.Windows.Forms.Label();
            this.lblSrv = new System.Windows.Forms.Label();
            this.lblServer = new System.Windows.Forms.Label();
            this.progressBar1 = new System.Windows.Forms.ProgressBar();
            this.toolTip1 = new System.Windows.Forms.ToolTip(this.components);
            this.statusPanel = new System.Windows.Forms.Panel();
            this.splitContainer1.Panel1.SuspendLayout();
            this.splitContainer1.Panel2.SuspendLayout();
            this.splitContainer1.SuspendLayout();
            this.panel1.SuspendLayout();
            this.cmdContextMenu.SuspendLayout();
            this.pnlConnection.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.gridMst)).BeginInit();
            this.gridContextMenu.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.gridView2)).BeginInit();
            this.flowLayoutPanel1.SuspendLayout();
            this.statusPanel.SuspendLayout();
            this.SuspendLayout();
            // 
            // splitContainer1
            // 
            this.splitContainer1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer1.Location = new System.Drawing.Point(0, 0);
            this.splitContainer1.Name = "splitContainer1";
            this.splitContainer1.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainer1.Panel1
            // 
            this.splitContainer1.Panel1.Controls.Add(this.panel1);
            this.splitContainer1.Panel1.Controls.Add(this.pnlConnection);
            // 
            // splitContainer1.Panel2
            // 
            this.splitContainer1.Panel2.AutoScroll = true;
            this.splitContainer1.Panel2.Controls.Add(this.gridMst);
            this.splitContainer1.Panel2.Controls.Add(this.flowLayoutPanel1);
            this.splitContainer1.Size = new System.Drawing.Size(751, 421);
            this.splitContainer1.SplitterDistance = 231;
            this.splitContainer1.TabIndex = 19;
            // 
            // panel1
            // 
            this.panel1.AutoSize = true;
            this.panel1.Controls.Add(this.txtCmd);
            this.panel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel1.Location = new System.Drawing.Point(0, 53);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(751, 178);
            this.panel1.TabIndex = 1;
            // 
            // txtCmd
            // 
            this.txtCmd.CausesValidation = false;
            this.txtCmd.ContextMenuStrip = this.cmdContextMenu;
            this.txtCmd.Dock = System.Windows.Forms.DockStyle.Fill;
            this.txtCmd.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.txtCmd.HideSelection = false;
            this.txtCmd.Location = new System.Drawing.Point(0, 0);
            this.txtCmd.MaxLength = 100000;
            this.txtCmd.Name = "txtCmd";
            this.txtCmd.Size = new System.Drawing.Size(751, 178);
            this.txtCmd.TabIndex = 0;
            this.txtCmd.Text = "";
            this.txtCmd.WordWrap = false;
            this.txtCmd.KeyDown += new System.Windows.Forms.KeyEventHandler(this.txtCmd_KeyDown);
            this.txtCmd.Enter += new System.EventHandler(this.txtCmd_Enter);
            this.txtCmd.TextChanged += new System.EventHandler(this.txtCmd_TextChanged);
            // 
            // cmdContextMenu
            // 
            this.cmdContextMenu.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.cutToolStripMenuItem,
            this.copyToolStripMenuItem,
            this.pasteToolStripMenuItem});
            this.cmdContextMenu.Name = "contextMenuStrip1";
            this.cmdContextMenu.ShowImageMargin = false;
            this.cmdContextMenu.Size = new System.Drawing.Size(78, 70);
            // 
            // cutToolStripMenuItem
            // 
            this.cutToolStripMenuItem.Name = "cutToolStripMenuItem";
            this.cutToolStripMenuItem.Size = new System.Drawing.Size(77, 22);
            this.cutToolStripMenuItem.Text = "Cut";
            // 
            // copyToolStripMenuItem
            // 
            this.copyToolStripMenuItem.Name = "copyToolStripMenuItem";
            this.copyToolStripMenuItem.Size = new System.Drawing.Size(77, 22);
            this.copyToolStripMenuItem.Text = "Copy";
            // 
            // pasteToolStripMenuItem
            // 
            this.pasteToolStripMenuItem.Name = "pasteToolStripMenuItem";
            this.pasteToolStripMenuItem.Size = new System.Drawing.Size(77, 22);
            this.pasteToolStripMenuItem.Text = "Paste";
            // 
            // pnlConnection
            // 
            this.pnlConnection.BackColor = System.Drawing.SystemColors.Control;
            this.pnlConnection.Controls.Add(this.btnConnect);
            this.pnlConnection.Controls.Add(this.lblServerEnv);
            this.pnlConnection.Controls.Add(this.cbxServer);
            this.pnlConnection.Controls.Add(this.chkAutoCommit);
            this.pnlConnection.Controls.Add(this.txtPswd);
            this.pnlConnection.Controls.Add(this.lblPswd);
            this.pnlConnection.Controls.Add(this.txtUser);
            this.pnlConnection.Controls.Add(this.lblUser);
            this.pnlConnection.Controls.Add(this.txtEnv);
            this.pnlConnection.Controls.Add(this.lblEnv);
            this.pnlConnection.Dock = System.Windows.Forms.DockStyle.Top;
            this.pnlConnection.Location = new System.Drawing.Point(0, 0);
            this.pnlConnection.Name = "pnlConnection";
            this.pnlConnection.Size = new System.Drawing.Size(751, 53);
            this.pnlConnection.TabIndex = 20;
            // 
            // btnConnect
            // 
            this.btnConnect.Location = new System.Drawing.Point(642, 3);
            this.btnConnect.Name = "btnConnect";
            this.btnConnect.Size = new System.Drawing.Size(75, 23);
            this.btnConnect.TabIndex = 3;
            this.btnConnect.Text = "Connect";
            this.toolTip1.SetToolTip(this.btnConnect, "Connect to Server");
            this.btnConnect.UseVisualStyleBackColor = true;
            this.btnConnect.Click += new System.EventHandler(this.btnConnect_Click);
            // 
            // lblServerEnv
            // 
            this.lblServerEnv.AutoSize = true;
            this.lblServerEnv.Location = new System.Drawing.Point(31, 8);
            this.lblServerEnv.Name = "lblServerEnv";
            this.lblServerEnv.Size = new System.Drawing.Size(46, 13);
            this.lblServerEnv.TabIndex = 29;
            this.lblServerEnv.Text = "Service:";
            // 
            // cbxServer
            // 
            this.cbxServer.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.SuggestAppend;
            this.cbxServer.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.ListItems;
            this.cbxServer.FormattingEnabled = true;
            this.cbxServer.Location = new System.Drawing.Point(78, 4);
            this.cbxServer.Name = "cbxServer";
            this.cbxServer.Size = new System.Drawing.Size(343, 21);
            this.cbxServer.TabIndex = 0;
            this.cbxServer.SelectedIndexChanged += new System.EventHandler(this.cbxServer_SelectedIndexChanged);
            this.cbxServer.KeyUp += new System.Windows.Forms.KeyEventHandler(this.cbxServer_KeyUp);
            this.cbxServer.KeyDown += new System.Windows.Forms.KeyEventHandler(this.cbxServer_KeyDown);
            // 
            // chkAutoCommit
            // 
            this.chkAutoCommit.AutoSize = true;
            this.chkAutoCommit.Checked = true;
            this.chkAutoCommit.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chkAutoCommit.Enabled = false;
            this.chkAutoCommit.Location = new System.Drawing.Point(642, 32);
            this.chkAutoCommit.MaximumSize = new System.Drawing.Size(0, 23);
            this.chkAutoCommit.Name = "chkAutoCommit";
            this.chkAutoCommit.Size = new System.Drawing.Size(91, 17);
            this.chkAutoCommit.TabIndex = 5;
            this.chkAutoCommit.Text = "Auto Commit?";
            this.chkAutoCommit.UseVisualStyleBackColor = true;
            this.chkAutoCommit.CheckedChanged += new System.EventHandler(this.chkAutoCommit_CheckedChanged);
            // 
            // txtPswd
            // 
            this.txtPswd.Location = new System.Drawing.Point(489, 30);
            this.txtPswd.Name = "txtPswd";
            this.txtPswd.PasswordChar = '*';
            this.txtPswd.Size = new System.Drawing.Size(147, 20);
            this.txtPswd.TabIndex = 2;
            this.txtPswd.KeyDown += new System.Windows.Forms.KeyEventHandler(this.txtPswd_KeyDown);
            this.txtPswd.Enter += new System.EventHandler(this.txtPswd_Enter);
            // 
            // lblPswd
            // 
            this.lblPswd.AutoSize = true;
            this.lblPswd.Location = new System.Drawing.Point(427, 33);
            this.lblPswd.Name = "lblPswd";
            this.lblPswd.Size = new System.Drawing.Size(56, 13);
            this.lblPswd.TabIndex = 27;
            this.lblPswd.Text = "Password:";
            // 
            // txtUser
            // 
            this.txtUser.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.txtUser.Location = new System.Drawing.Point(489, 5);
            this.txtUser.Name = "txtUser";
            this.txtUser.Size = new System.Drawing.Size(147, 20);
            this.txtUser.TabIndex = 1;
            this.txtUser.KeyDown += new System.Windows.Forms.KeyEventHandler(this.txtUser_KeyDown);
            this.txtUser.Enter += new System.EventHandler(this.txtUser_Enter);
            // 
            // lblUser
            // 
            this.lblUser.AutoSize = true;
            this.lblUser.Location = new System.Drawing.Point(451, 8);
            this.lblUser.Name = "lblUser";
            this.lblUser.Size = new System.Drawing.Size(32, 13);
            this.lblUser.TabIndex = 25;
            this.lblUser.Text = "User:";
            // 
            // txtEnv
            // 
            this.txtEnv.Location = new System.Drawing.Point(78, 29);
            this.txtEnv.Name = "txtEnv";
            this.txtEnv.Size = new System.Drawing.Size(343, 20);
            this.txtEnv.TabIndex = 4;
            this.txtEnv.KeyDown += new System.Windows.Forms.KeyEventHandler(this.txtEnv_KeyDown);
            this.txtEnv.Enter += new System.EventHandler(this.txtEnv_Enter);
            // 
            // lblEnv
            // 
            this.lblEnv.AutoSize = true;
            this.lblEnv.Location = new System.Drawing.Point(3, 32);
            this.lblEnv.Name = "lblEnv";
            this.lblEnv.Size = new System.Drawing.Size(69, 13);
            this.lblEnv.TabIndex = 23;
            this.lblEnv.Text = "Environment:";
            // 
            // gridMst
            // 
            this.gridMst.ContextMenuStrip = this.gridContextMenu;
            this.gridMst.Dock = System.Windows.Forms.DockStyle.Fill;
            this.gridMst.EmbeddedNavigator.Appearance.BackColor = System.Drawing.SystemColors.Control;
            this.gridMst.EmbeddedNavigator.Appearance.BackColor2 = System.Drawing.SystemColors.Control;
            this.gridMst.EmbeddedNavigator.Appearance.BorderColor = System.Drawing.Color.White;
            this.gridMst.EmbeddedNavigator.Appearance.ForeColor = System.Drawing.SystemColors.Control;
            this.gridMst.EmbeddedNavigator.Appearance.Options.UseBackColor = true;
            this.gridMst.EmbeddedNavigator.Appearance.Options.UseBorderColor = true;
            this.gridMst.EmbeddedNavigator.Appearance.Options.UseForeColor = true;
            this.gridMst.EmbeddedNavigator.Name = "";
            this.gridMst.ImeMode = System.Windows.Forms.ImeMode.NoControl;
            this.gridMst.Location = new System.Drawing.Point(0, 15);
            this.gridMst.LookAndFeel.UseDefaultLookAndFeel = false;
            this.gridMst.LookAndFeel.UseWindowsXPTheme = false;
            this.gridMst.MainView = this.gridView2;
            this.gridMst.Name = "gridMst";
            this.gridMst.Size = new System.Drawing.Size(751, 171);
            this.gridMst.TabIndex = 0;
            this.gridMst.ToolTipController = this.toolTipController;
            this.gridMst.ViewCollection.AddRange(new DevExpress.XtraGrid.Views.Base.BaseView[] {
            this.gridView2});
            this.gridMst.KeyDown += new System.Windows.Forms.KeyEventHandler(this.gridMst_KeyDown);
            // 
            // gridContextMenu
            // 
            this.gridContextMenu.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.copyCellToolStripMenuItem1,
            this.copyRowToolStripMenuItem,
            this.copyAllRowsToolStripMenuItem,
            this.copyPublishToolStripMenuItem});
            this.gridContextMenu.Name = "gridContextMenu";
            this.gridContextMenu.ShowImageMargin = false;
            this.gridContextMenu.Size = new System.Drawing.Size(212, 92);
            this.gridContextMenu.Opening += new System.ComponentModel.CancelEventHandler(this.gridContextMenu_Opening);
            // 
            // copyCellToolStripMenuItem1
            // 
            this.copyCellToolStripMenuItem1.Name = "copyCellToolStripMenuItem1";
            this.copyCellToolStripMenuItem1.Size = new System.Drawing.Size(211, 22);
            this.copyCellToolStripMenuItem1.Text = "Copy Cell";
            // 
            // copyRowToolStripMenuItem
            // 
            this.copyRowToolStripMenuItem.Name = "copyRowToolStripMenuItem";
            this.copyRowToolStripMenuItem.Size = new System.Drawing.Size(211, 22);
            this.copyRowToolStripMenuItem.Text = "Copy Row";
            // 
            // copyAllRowsToolStripMenuItem
            // 
            this.copyAllRowsToolStripMenuItem.Name = "copyAllRowsToolStripMenuItem";
            this.copyAllRowsToolStripMenuItem.Size = new System.Drawing.Size(211, 22);
            this.copyAllRowsToolStripMenuItem.Text = "Copy All Rows";
            this.copyAllRowsToolStripMenuItem.Click += new System.EventHandler(this.copyAllRowsToolStripMenuItem_Click);
            // 
            // copyPublishToolStripMenuItem
            // 
            this.copyPublishToolStripMenuItem.Name = "copyPublishToolStripMenuItem";
            this.copyPublishToolStripMenuItem.Size = new System.Drawing.Size(211, 22);
            this.copyPublishToolStripMenuItem.Text = "Copy Selected Rows as Publish";
            // 
            // gridView2
            // 
            this.gridView2.BestFitMaxRowCount = 50;
            this.gridView2.GridControl = this.gridMst;
            this.gridView2.Name = "gridView2";
            this.gridView2.OptionsSelection.MultiSelect = true;
            // 
            // toolTipController
            // 
            this.toolTipController.ToolTipLocation = DevExpress.Utils.ToolTipLocation.BottomCenter;
            // 
            // flowLayoutPanel1
            // 
            this.flowLayoutPanel1.Controls.Add(this.lblStatus);
            this.flowLayoutPanel1.Controls.Add(this.lblSts);
            this.flowLayoutPanel1.Controls.Add(this.lblRowsReturned);
            this.flowLayoutPanel1.Controls.Add(this.lblRowsRet);
            this.flowLayoutPanel1.Controls.Add(this.lblElapsedTime);
            this.flowLayoutPanel1.Controls.Add(this.lblElpsdTime);
            this.flowLayoutPanel1.Controls.Add(this.lblExecuteTime);
            this.flowLayoutPanel1.Controls.Add(this.lblExecTime);
            this.flowLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Top;
            this.flowLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.flowLayoutPanel1.Name = "flowLayoutPanel1";
            this.flowLayoutPanel1.Padding = new System.Windows.Forms.Padding(1);
            this.flowLayoutPanel1.Size = new System.Drawing.Size(751, 15);
            this.flowLayoutPanel1.TabIndex = 16;
            // 
            // lblStatus
            // 
            this.lblStatus.AutoSize = true;
            this.lblStatus.Location = new System.Drawing.Point(1, 1);
            this.lblStatus.Margin = new System.Windows.Forms.Padding(0);
            this.lblStatus.Name = "lblStatus";
            this.lblStatus.Size = new System.Drawing.Size(43, 13);
            this.lblStatus.TabIndex = 0;
            this.lblStatus.Text = "Status: ";
            // 
            // lblSts
            // 
            this.lblSts.AutoSize = true;
            this.lblSts.Location = new System.Drawing.Point(44, 1);
            this.lblSts.Margin = new System.Windows.Forms.Padding(0);
            this.lblSts.Name = "lblSts";
            this.lblSts.Size = new System.Drawing.Size(0, 13);
            this.lblSts.TabIndex = 2;
            // 
            // lblRowsReturned
            // 
            this.lblRowsReturned.AutoSize = true;
            this.lblRowsReturned.Location = new System.Drawing.Point(47, 1);
            this.lblRowsReturned.Name = "lblRowsReturned";
            this.lblRowsReturned.Size = new System.Drawing.Size(87, 13);
            this.lblRowsReturned.TabIndex = 3;
            this.lblRowsReturned.Text = "Rows Returned: ";
            // 
            // lblRowsRet
            // 
            this.lblRowsRet.AutoSize = true;
            this.lblRowsRet.Location = new System.Drawing.Point(137, 1);
            this.lblRowsRet.Margin = new System.Windows.Forms.Padding(0);
            this.lblRowsRet.Name = "lblRowsRet";
            this.lblRowsRet.Size = new System.Drawing.Size(0, 13);
            this.lblRowsRet.TabIndex = 4;
            // 
            // lblElapsedTime
            // 
            this.lblElapsedTime.AutoSize = true;
            this.lblElapsedTime.Location = new System.Drawing.Point(140, 1);
            this.lblElapsedTime.Name = "lblElapsedTime";
            this.lblElapsedTime.Size = new System.Drawing.Size(77, 13);
            this.lblElapsedTime.TabIndex = 5;
            this.lblElapsedTime.Text = "Elapsed Time: ";
            // 
            // lblElpsdTime
            // 
            this.lblElpsdTime.AutoSize = true;
            this.lblElpsdTime.Location = new System.Drawing.Point(220, 1);
            this.lblElpsdTime.Margin = new System.Windows.Forms.Padding(0);
            this.lblElpsdTime.Name = "lblElpsdTime";
            this.lblElpsdTime.Size = new System.Drawing.Size(0, 13);
            this.lblElpsdTime.TabIndex = 6;
            // 
            // lblExecuteTime
            // 
            this.lblExecuteTime.AutoSize = true;
            this.lblExecuteTime.Location = new System.Drawing.Point(223, 1);
            this.lblExecuteTime.Name = "lblExecuteTime";
            this.lblExecuteTime.Size = new System.Drawing.Size(75, 13);
            this.lblExecuteTime.TabIndex = 7;
            this.lblExecuteTime.Text = "Execute Time:";
            // 
            // lblExecTime
            // 
            this.lblExecTime.AutoSize = true;
            this.lblExecTime.Location = new System.Drawing.Point(301, 1);
            this.lblExecTime.Margin = new System.Windows.Forms.Padding(0);
            this.lblExecTime.Name = "lblExecTime";
            this.lblExecTime.Size = new System.Drawing.Size(0, 13);
            this.lblExecTime.TabIndex = 8;
            // 
            // lblSrv
            // 
            this.lblSrv.AutoSize = true;
            this.lblSrv.Location = new System.Drawing.Point(72, 3);
            this.lblSrv.Name = "lblSrv";
            this.lblSrv.Size = new System.Drawing.Size(73, 13);
            this.lblSrv.TabIndex = 14;
            this.lblSrv.Text = "Disconnected";
            // 
            // lblServer
            // 
            this.lblServer.AutoSize = true;
            this.lblServer.Location = new System.Drawing.Point(3, 3);
            this.lblServer.Name = "lblServer";
            this.lblServer.Size = new System.Drawing.Size(74, 13);
            this.lblServer.TabIndex = 13;
            this.lblServer.Text = "Server Status:";
            // 
            // progressBar1
            // 
            this.progressBar1.Dock = System.Windows.Forms.DockStyle.Right;
            this.progressBar1.Enabled = false;
            this.progressBar1.Location = new System.Drawing.Point(588, 0);
            this.progressBar1.Margin = new System.Windows.Forms.Padding(0);
            this.progressBar1.Name = "progressBar1";
            this.progressBar1.Size = new System.Drawing.Size(163, 15);
            this.progressBar1.Style = System.Windows.Forms.ProgressBarStyle.Marquee;
            this.progressBar1.TabIndex = 14;
            this.progressBar1.Visible = false;
            // 
            // statusPanel
            // 
            this.statusPanel.Controls.Add(this.progressBar1);
            this.statusPanel.Controls.Add(this.lblSrv);
            this.statusPanel.Controls.Add(this.lblServer);
            this.statusPanel.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.statusPanel.Location = new System.Drawing.Point(0, 421);
            this.statusPanel.Name = "statusPanel";
            this.statusPanel.Size = new System.Drawing.Size(751, 15);
            this.statusPanel.TabIndex = 20;
            // 
            // winMSQLTab
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSize = true;
            this.Controls.Add(this.splitContainer1);
            this.Controls.Add(this.statusPanel);
            this.Name = "winMSQLTab";
            this.Size = new System.Drawing.Size(751, 436);
            this.splitContainer1.Panel1.ResumeLayout(false);
            this.splitContainer1.Panel1.PerformLayout();
            this.splitContainer1.Panel2.ResumeLayout(false);
            this.splitContainer1.ResumeLayout(false);
            this.panel1.ResumeLayout(false);
            this.cmdContextMenu.ResumeLayout(false);
            this.pnlConnection.ResumeLayout(false);
            this.pnlConnection.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.gridMst)).EndInit();
            this.gridContextMenu.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.gridView2)).EndInit();
            this.flowLayoutPanel1.ResumeLayout(false);
            this.flowLayoutPanel1.PerformLayout();
            this.statusPanel.ResumeLayout(false);
            this.statusPanel.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.SplitContainer splitContainer1;
        public System.Windows.Forms.Panel panel1;
        protected internal System.Windows.Forms.RichTextBox txtCmd;
        private System.Windows.Forms.Label lblSts;
        private System.Windows.Forms.Label lblRowsRet;
        private System.Windows.Forms.Label lblStatus;
        private System.Windows.Forms.Label lblElpsdTime;
        private System.Windows.Forms.Label lblElapsedTime;
        private System.Windows.Forms.Label lblRowsReturned;
		private System.Windows.Forms.Panel pnlConnection;
        private DevExpress.XtraGrid.GridControl gridMst;
        private System.Windows.Forms.Button btnConnect;
		private System.Windows.Forms.Label lblEnv;
		private System.Windows.Forms.TextBox txtPswd;
		private System.Windows.Forms.Label lblPswd;
		private System.Windows.Forms.TextBox txtUser;
		private System.Windows.Forms.Label lblUser;
		private System.Windows.Forms.TextBox txtEnv;
		private System.Windows.Forms.Label lblSrv;
		private System.Windows.Forms.Label lblServer;
		private System.Windows.Forms.Label lblServerEnv;
		private System.Windows.Forms.ComboBox cbxServer;
		private System.Windows.Forms.Label lblExecTime;
        private System.Windows.Forms.Label lblExecuteTime;
		private System.Windows.Forms.ContextMenuStrip cmdContextMenu;
		private System.Windows.Forms.ToolStripMenuItem cutToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem copyToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem pasteToolStripMenuItem;
		private System.Windows.Forms.ContextMenuStrip gridContextMenu;
		private System.Windows.Forms.ToolStripMenuItem copyCellToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem copyRowToolStripMenuItem;
		private System.Windows.Forms.ToolTip toolTip1;
        private System.Windows.Forms.ToolStripMenuItem copyAllRowsToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem copyPublishToolStripMenuItem;
        private System.Windows.Forms.ProgressBar progressBar1;
        private DevExpress.XtraGrid.Views.Grid.GridView gridView2;
        private System.Windows.Forms.Panel statusPanel;
        private System.Windows.Forms.FlowLayoutPanel flowLayoutPanel1;
        private DevExpress.Utils.ToolTipController toolTipController;
        private System.Windows.Forms.CheckBox chkAutoCommit;

    }
}
