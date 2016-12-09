namespace RedPrairie.MCS.WinMSQL
{
    partial class ctlWinMSQL
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem openCommandToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem saveCommandToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripMenuItem loadResultsToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem saveResultsToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator2;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem viewToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem commandHistoryToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem expandedValueToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem columnInfoToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator3;
        private System.Windows.Forms.ToolStripMenuItem decreaseFontSizeToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem increaseFontSizeToolStripMenuItem;
		private System.Windows.Forms.Button cmdExecute;
		private System.Windows.Forms.Button cmdFormat;
		private System.Windows.Forms.Button cmdHistory;

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
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ctlWinMSQL));
            Infragistics.Win.Appearance appearance1 = new Infragistics.Win.Appearance();
            Infragistics.Win.Appearance appearance2 = new Infragistics.Win.Appearance();
            Infragistics.Win.Appearance appearance3 = new Infragistics.Win.Appearance();
            Infragistics.Win.Appearance appearance4 = new Infragistics.Win.Appearance();
            Infragistics.Win.UltraWinTabControl.UltraTab ultraTab1 = new Infragistics.Win.UltraWinTabControl.UltraTab();
            Infragistics.Win.UltraWinTabControl.UltraTab ultraTab2 = new Infragistics.Win.UltraWinTabControl.UltraTab();
            this.ultraTabPageControl1 = new Infragistics.Win.UltraWinTabControl.UltraTabPageControl();
            this.winMSQLTab1 = new RedPrairie.MCS.WinMSQL.winMSQLTab();
            this.ultraTabPageControl2 = new Infragistics.Win.UltraWinTabControl.UltraTabPageControl();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newTabToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.removeTabToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator4 = new System.Windows.Forms.ToolStripSeparator();
            this.openCommandToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.saveCommandToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.loadResultsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.saveResultsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator2 = new System.Windows.Forms.ToolStripSeparator();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.editToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.cutToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.copyToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.pasteToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator5 = new System.Windows.Forms.ToolStripSeparator();
            this.findAndReplaceToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.quickFindToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.quickReplaceToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.viewToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.commandHistoryToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.expandedValueToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.columnInfoToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripMenuItem3 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator3 = new System.Windows.Forms.ToolStripSeparator();
            this.decreaseFontSizeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.increaseFontSizeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.executeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.formatCommandToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.configureServersToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.cmdExecute = new System.Windows.Forms.Button();
            this.cmdFormat = new System.Windows.Forms.Button();
            this.cmdHistory = new System.Windows.Forms.Button();
            this.cmdOptions = new System.Windows.Forms.Button();
            this.pnlBtnFav = new System.Windows.Forms.Panel();
            this.cmdAddFave = new System.Windows.Forms.Button();
            this.cmdFave = new System.Windows.Forms.Button();
            this.btnTrace = new System.Windows.Forms.Button();
            this.btnViewTrace = new System.Windows.Forms.Button();
            this.tabDashBoard = new Infragistics.Win.UltraWinTabControl.UltraTabControl();
            this.ultraTabSharedControlsPage1 = new Infragistics.Win.UltraWinTabControl.UltraTabSharedControlsPage();
            this.contextMenuStrip1 = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.toolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator6 = new System.Windows.Forms.ToolStripSeparator();
            this.toolStripMenuItem2 = new System.Windows.Forms.ToolStripMenuItem();
            this.closeOtherTabsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripMenuItem5 = new System.Windows.Forms.ToolStripSeparator();
            this.changeTabColorToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.ultraTabPageControl1.SuspendLayout();
            this.menuStrip1.SuspendLayout();
            this.tableLayoutPanel1.SuspendLayout();
            this.pnlBtnFav.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.tabDashBoard)).BeginInit();
            this.tabDashBoard.SuspendLayout();
            this.contextMenuStrip1.SuspendLayout();
            this.SuspendLayout();
            // 
            // ultraTabPageControl1
            // 
            this.ultraTabPageControl1.Controls.Add(this.winMSQLTab1);
            this.ultraTabPageControl1.Location = new System.Drawing.Point(2, 24);
            this.ultraTabPageControl1.Name = "ultraTabPageControl1";
            this.ultraTabPageControl1.Size = new System.Drawing.Size(766, 435);
            // 
            // winMSQLTab1
            // 
            this.winMSQLTab1.AutoCommit = true;
            this.winMSQLTab1.AutoScroll = true;
            this.winMSQLTab1.AutoSize = true;
            this.winMSQLTab1.Command = "";
            this.winMSQLTab1.ConfigOptions = ((System.Collections.Hashtable)(resources.GetObject("winMSQLTab1.ConfigOptions")));
            this.winMSQLTab1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.winMSQLTab1.Elapsed_Time = "";
            this.winMSQLTab1.Environment = "";
            this.winMSQLTab1.ExecuteTime = "";
            this.winMSQLTab1.IsExecuting = false;
            this.winMSQLTab1.Location = new System.Drawing.Point(0, 0);
            this.winMSQLTab1.MinimumSize = new System.Drawing.Size(510, 392);
            this.winMSQLTab1.Name = "winMSQLTab1";
            this.winMSQLTab1.Password = "";
            this.winMSQLTab1.Returned_Rows = "";
            this.winMSQLTab1.SelectedText = "";
            this.winMSQLTab1.Size = new System.Drawing.Size(766, 435);
            this.winMSQLTab1.Status = "";
            this.winMSQLTab1.TabIndex = 0;
            this.winMSQLTab1.TabPage = null;
            this.winMSQLTab1.TraceOptions = ((System.Collections.Hashtable)(resources.GetObject("winMSQLTab1.TraceOptions")));
            this.winMSQLTab1.Tracing = false;
            this.winMSQLTab1.UserName = "";
            // 
            // ultraTabPageControl2
            // 
            this.ultraTabPageControl2.Location = new System.Drawing.Point(-10000, -10000);
            this.ultraTabPageControl2.Name = "ultraTabPageControl2";
            this.ultraTabPageControl2.Size = new System.Drawing.Size(766, 435);
            // 
            // menuStrip1
            // 
            this.menuStrip1.AllowDrop = true;
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fileToolStripMenuItem,
            this.editToolStripMenuItem,
            this.viewToolStripMenuItem,
            this.toolsToolStripMenuItem});
            this.menuStrip1.Location = new System.Drawing.Point(0, 0);
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Size = new System.Drawing.Size(770, 24);
            this.menuStrip1.TabIndex = 0;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.newTabToolStripMenuItem,
            this.removeTabToolStripMenuItem,
            this.toolStripSeparator4,
            this.openCommandToolStripMenuItem,
            this.saveCommandToolStripMenuItem,
            this.toolStripSeparator1,
            this.loadResultsToolStripMenuItem,
            this.saveResultsToolStripMenuItem,
            this.toolStripSeparator2,
            this.exitToolStripMenuItem});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(35, 20);
            this.fileToolStripMenuItem.Text = "&File";
            // 
            // newTabToolStripMenuItem
            // 
            this.newTabToolStripMenuItem.Name = "newTabToolStripMenuItem";
            this.newTabToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.T)));
            this.newTabToolStripMenuItem.Size = new System.Drawing.Size(217, 22);
            this.newTabToolStripMenuItem.Text = "New &Tab";
            this.newTabToolStripMenuItem.Click += new System.EventHandler(this.newTabToolStripMenuItem_Click);
            // 
            // removeTabToolStripMenuItem
            // 
            this.removeTabToolStripMenuItem.Name = "removeTabToolStripMenuItem";
            this.removeTabToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.R)));
            this.removeTabToolStripMenuItem.Size = new System.Drawing.Size(217, 22);
            this.removeTabToolStripMenuItem.Text = "&Remove Tab   Ctrl+W";
            this.removeTabToolStripMenuItem.Click += new System.EventHandler(this.removeTabToolStripMenuItem_Click);
            // 
            // toolStripSeparator4
            // 
            this.toolStripSeparator4.Name = "toolStripSeparator4";
            this.toolStripSeparator4.Size = new System.Drawing.Size(214, 6);
            // 
            // openCommandToolStripMenuItem
            // 
            this.openCommandToolStripMenuItem.Name = "openCommandToolStripMenuItem";
            this.openCommandToolStripMenuItem.Size = new System.Drawing.Size(217, 22);
            this.openCommandToolStripMenuItem.Text = "&Open Command";
            this.openCommandToolStripMenuItem.Click += new System.EventHandler(this.openCommandToolStripMenuItem_Click);
            // 
            // saveCommandToolStripMenuItem
            // 
            this.saveCommandToolStripMenuItem.Name = "saveCommandToolStripMenuItem";
            this.saveCommandToolStripMenuItem.Size = new System.Drawing.Size(217, 22);
            this.saveCommandToolStripMenuItem.Text = "&Save Command";
            this.saveCommandToolStripMenuItem.Click += new System.EventHandler(this.saveCommandToolStripMenuItem_Click);
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(214, 6);
            // 
            // loadResultsToolStripMenuItem
            // 
            this.loadResultsToolStripMenuItem.Name = "loadResultsToolStripMenuItem";
            this.loadResultsToolStripMenuItem.Size = new System.Drawing.Size(217, 22);
            this.loadResultsToolStripMenuItem.Text = "&Load Results";
            this.loadResultsToolStripMenuItem.Click += new System.EventHandler(this.loadResultsToolStripMenuItem_Click);
            // 
            // saveResultsToolStripMenuItem
            // 
            this.saveResultsToolStripMenuItem.Name = "saveResultsToolStripMenuItem";
            this.saveResultsToolStripMenuItem.Size = new System.Drawing.Size(217, 22);
            this.saveResultsToolStripMenuItem.Text = "S&ave Results";
            this.saveResultsToolStripMenuItem.Click += new System.EventHandler(this.saveResultsToolStripMenuItem_Click);
            // 
            // toolStripSeparator2
            // 
            this.toolStripSeparator2.Name = "toolStripSeparator2";
            this.toolStripSeparator2.Size = new System.Drawing.Size(214, 6);
            // 
            // exitToolStripMenuItem
            // 
            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            this.exitToolStripMenuItem.Size = new System.Drawing.Size(217, 22);
            this.exitToolStripMenuItem.Text = "E&xit";
            this.exitToolStripMenuItem.Click += new System.EventHandler(this.exitToolStripMenuItem_Click);
            // 
            // editToolStripMenuItem
            // 
            this.editToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.cutToolStripMenuItem,
            this.copyToolStripMenuItem,
            this.pasteToolStripMenuItem,
            this.toolStripSeparator5,
            this.findAndReplaceToolStripMenuItem});
            this.editToolStripMenuItem.Name = "editToolStripMenuItem";
            this.editToolStripMenuItem.Size = new System.Drawing.Size(37, 20);
            this.editToolStripMenuItem.Text = "&Edit";
            // 
            // cutToolStripMenuItem
            // 
            this.cutToolStripMenuItem.Name = "cutToolStripMenuItem";
            this.cutToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.X)));
            this.cutToolStripMenuItem.Size = new System.Drawing.Size(156, 22);
            this.cutToolStripMenuItem.Text = "C&ut";
            this.cutToolStripMenuItem.Click += new System.EventHandler(this.cutToolStripMenuItem_Click);
            // 
            // copyToolStripMenuItem
            // 
            this.copyToolStripMenuItem.Name = "copyToolStripMenuItem";
            this.copyToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.C)));
            this.copyToolStripMenuItem.Size = new System.Drawing.Size(156, 22);
            this.copyToolStripMenuItem.Text = "&Copy";
            this.copyToolStripMenuItem.Click += new System.EventHandler(this.copyToolStripMenuItem_Click);
            // 
            // pasteToolStripMenuItem
            // 
            this.pasteToolStripMenuItem.Name = "pasteToolStripMenuItem";
            this.pasteToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.V)));
            this.pasteToolStripMenuItem.Size = new System.Drawing.Size(156, 22);
            this.pasteToolStripMenuItem.Text = "&Paste";
            this.pasteToolStripMenuItem.Click += new System.EventHandler(this.pasteToolStripMenuItem_Click);
            // 
            // toolStripSeparator5
            // 
            this.toolStripSeparator5.Name = "toolStripSeparator5";
            this.toolStripSeparator5.Size = new System.Drawing.Size(153, 6);
            // 
            // findAndReplaceToolStripMenuItem
            // 
            this.findAndReplaceToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.quickFindToolStripMenuItem,
            this.quickReplaceToolStripMenuItem});
            this.findAndReplaceToolStripMenuItem.Name = "findAndReplaceToolStripMenuItem";
            this.findAndReplaceToolStripMenuItem.Size = new System.Drawing.Size(156, 22);
            this.findAndReplaceToolStripMenuItem.Text = "&Find and Replace";
            // 
            // quickFindToolStripMenuItem
            // 
            this.quickFindToolStripMenuItem.Name = "quickFindToolStripMenuItem";
            this.quickFindToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.F)));
            this.quickFindToolStripMenuItem.Size = new System.Drawing.Size(180, 22);
            this.quickFindToolStripMenuItem.Text = "Quick &Find";
            this.quickFindToolStripMenuItem.Click += new System.EventHandler(this.quickFindToolStripMenuItem_Click);
            // 
            // quickReplaceToolStripMenuItem
            // 
            this.quickReplaceToolStripMenuItem.Name = "quickReplaceToolStripMenuItem";
            this.quickReplaceToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.H)));
            this.quickReplaceToolStripMenuItem.Size = new System.Drawing.Size(180, 22);
            this.quickReplaceToolStripMenuItem.Text = "Quick &Replace";
            this.quickReplaceToolStripMenuItem.Click += new System.EventHandler(this.quickReplaceToolStripMenuItem_Click);
            // 
            // viewToolStripMenuItem
            // 
            this.viewToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.commandHistoryToolStripMenuItem,
            this.expandedValueToolStripMenuItem,
            this.columnInfoToolStripMenuItem,
            this.toolStripMenuItem3,
            this.toolStripSeparator3,
            this.decreaseFontSizeToolStripMenuItem,
            this.increaseFontSizeToolStripMenuItem});
            this.viewToolStripMenuItem.Name = "viewToolStripMenuItem";
            this.viewToolStripMenuItem.Size = new System.Drawing.Size(41, 20);
            this.viewToolStripMenuItem.Text = "&View";
            // 
            // commandHistoryToolStripMenuItem
            // 
            this.commandHistoryToolStripMenuItem.Name = "commandHistoryToolStripMenuItem";
            this.commandHistoryToolStripMenuItem.Size = new System.Drawing.Size(189, 22);
            this.commandHistoryToolStripMenuItem.Text = "Command &History";
            this.commandHistoryToolStripMenuItem.Click += new System.EventHandler(this.commandHistoryToolStripMenuItem_Click);
            // 
            // expandedValueToolStripMenuItem
            // 
            this.expandedValueToolStripMenuItem.Name = "expandedValueToolStripMenuItem";
            this.expandedValueToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.P)));
            this.expandedValueToolStripMenuItem.Size = new System.Drawing.Size(189, 22);
            this.expandedValueToolStripMenuItem.Text = "&Expanded Value";
            this.expandedValueToolStripMenuItem.Click += new System.EventHandler(this.expandedValueToolStripMenuItem_Click);
            // 
            // columnInfoToolStripMenuItem
            // 
            this.columnInfoToolStripMenuItem.Name = "columnInfoToolStripMenuItem";
            this.columnInfoToolStripMenuItem.Size = new System.Drawing.Size(189, 22);
            this.columnInfoToolStripMenuItem.Text = "&Column Info";
            this.columnInfoToolStripMenuItem.Click += new System.EventHandler(this.columnInfoToolStripMenuItem_Click);
            // 
            // toolStripMenuItem3
            // 
            this.toolStripMenuItem3.Name = "toolStripMenuItem3";
            this.toolStripMenuItem3.Size = new System.Drawing.Size(189, 22);
            this.toolStripMenuItem3.Text = "&Options";
            this.toolStripMenuItem3.Click += new System.EventHandler(this.toolStripMenuItem3_Click);
            // 
            // toolStripSeparator3
            // 
            this.toolStripSeparator3.Name = "toolStripSeparator3";
            this.toolStripSeparator3.Size = new System.Drawing.Size(186, 6);
            // 
            // decreaseFontSizeToolStripMenuItem
            // 
            this.decreaseFontSizeToolStripMenuItem.Name = "decreaseFontSizeToolStripMenuItem";
            this.decreaseFontSizeToolStripMenuItem.Size = new System.Drawing.Size(189, 22);
            this.decreaseFontSizeToolStripMenuItem.Text = "&Decrease Font Size";
            this.decreaseFontSizeToolStripMenuItem.Click += new System.EventHandler(this.decreaseFontSizeToolStripMenuItem_Click);
            // 
            // increaseFontSizeToolStripMenuItem
            // 
            this.increaseFontSizeToolStripMenuItem.Name = "increaseFontSizeToolStripMenuItem";
            this.increaseFontSizeToolStripMenuItem.Size = new System.Drawing.Size(189, 22);
            this.increaseFontSizeToolStripMenuItem.Text = "&Increase Font Size";
            this.increaseFontSizeToolStripMenuItem.Click += new System.EventHandler(this.increaseFontSizeToolStripMenuItem_Click);
            // 
            // toolsToolStripMenuItem
            // 
            this.toolsToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.executeToolStripMenuItem,
            this.formatCommandToolStripMenuItem,
            this.configureServersToolStripMenuItem});
            this.toolsToolStripMenuItem.Name = "toolsToolStripMenuItem";
            this.toolsToolStripMenuItem.Size = new System.Drawing.Size(44, 20);
            this.toolsToolStripMenuItem.Text = "&Tools";
            // 
            // executeToolStripMenuItem
            // 
            this.executeToolStripMenuItem.Name = "executeToolStripMenuItem";
            this.executeToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.E)));
            this.executeToolStripMenuItem.Size = new System.Drawing.Size(263, 22);
            this.executeToolStripMenuItem.Text = "&Execute";
            this.executeToolStripMenuItem.Click += new System.EventHandler(this.executeToolStripMenuItem_Click);
            // 
            // formatCommandToolStripMenuItem
            // 
            this.formatCommandToolStripMenuItem.Name = "formatCommandToolStripMenuItem";
            this.formatCommandToolStripMenuItem.ShortcutKeys = ((System.Windows.Forms.Keys)(((System.Windows.Forms.Keys.Control | System.Windows.Forms.Keys.Shift)
                        | System.Windows.Forms.Keys.F)));
            this.formatCommandToolStripMenuItem.Size = new System.Drawing.Size(263, 22);
            this.formatCommandToolStripMenuItem.Text = "&Format Command";
            this.formatCommandToolStripMenuItem.Click += new System.EventHandler(this.formatCommandToolStripMenuItem_Click);
            // 
            // configureServersToolStripMenuItem
            // 
            this.configureServersToolStripMenuItem.Name = "configureServersToolStripMenuItem";
            this.configureServersToolStripMenuItem.Size = new System.Drawing.Size(263, 22);
            this.configureServersToolStripMenuItem.Text = "&Configure Servers (Requires DlxConfig)";
            this.configureServersToolStripMenuItem.Click += new System.EventHandler(this.configureServersToolStripMenuItem_Click);
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.AutoSize = true;
            this.tableLayoutPanel1.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.tableLayoutPanel1.ColumnCount = 12;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel1.Controls.Add(this.cmdExecute, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.cmdFormat, 1, 0);
            this.tableLayoutPanel1.Controls.Add(this.cmdHistory, 3, 0);
            this.tableLayoutPanel1.Controls.Add(this.cmdOptions, 7, 0);
            this.tableLayoutPanel1.Controls.Add(this.pnlBtnFav, 4, 0);
            this.tableLayoutPanel1.Controls.Add(this.btnTrace, 10, 0);
            this.tableLayoutPanel1.Controls.Add(this.btnViewTrace, 11, 0);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Top;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 24);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.RowCount = 1;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.Size = new System.Drawing.Size(770, 29);
            this.tableLayoutPanel1.TabIndex = 9;
            // 
            // cmdExecute
            // 
            this.cmdExecute.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.cmdExecute.Location = new System.Drawing.Point(3, 3);
            this.cmdExecute.MinimumSize = new System.Drawing.Size(75, 23);
            this.cmdExecute.Name = "cmdExecute";
            this.cmdExecute.Size = new System.Drawing.Size(75, 23);
            this.cmdExecute.TabIndex = 3;
            this.cmdExecute.Text = "Execute";
            this.cmdExecute.UseVisualStyleBackColor = true;
            this.cmdExecute.Click += new System.EventHandler(this.cmdExecute_Click);
            // 
            // cmdFormat
            // 
            this.cmdFormat.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.cmdFormat.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cmdFormat.Location = new System.Drawing.Point(84, 3);
            this.cmdFormat.MinimumSize = new System.Drawing.Size(75, 23);
            this.cmdFormat.Name = "cmdFormat";
            this.cmdFormat.Size = new System.Drawing.Size(75, 23);
            this.cmdFormat.TabIndex = 5;
            this.cmdFormat.Text = "Format";
            this.cmdFormat.UseVisualStyleBackColor = true;
            this.cmdFormat.Click += new System.EventHandler(this.cmdFormat_Click);
            // 
            // cmdHistory
            // 
            this.cmdHistory.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.cmdHistory.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cmdHistory.Location = new System.Drawing.Point(165, 3);
            this.cmdHistory.MinimumSize = new System.Drawing.Size(75, 23);
            this.cmdHistory.Name = "cmdHistory";
            this.cmdHistory.Size = new System.Drawing.Size(75, 23);
            this.cmdHistory.TabIndex = 7;
            this.cmdHistory.Text = "History";
            this.cmdHistory.UseVisualStyleBackColor = true;
            this.cmdHistory.Click += new System.EventHandler(this.cmdHistory_Click);
            // 
            // cmdOptions
            // 
            this.cmdOptions.Location = new System.Drawing.Point(356, 3);
            this.cmdOptions.Name = "cmdOptions";
            this.cmdOptions.Size = new System.Drawing.Size(75, 23);
            this.cmdOptions.TabIndex = 11;
            this.cmdOptions.Text = "Options";
            this.cmdOptions.UseVisualStyleBackColor = true;
            this.cmdOptions.Click += new System.EventHandler(this.cmdOptions_Click);
            // 
            // pnlBtnFav
            // 
            this.pnlBtnFav.Controls.Add(this.cmdAddFave);
            this.pnlBtnFav.Controls.Add(this.cmdFave);
            this.pnlBtnFav.Location = new System.Drawing.Point(246, 3);
            this.pnlBtnFav.Name = "pnlBtnFav";
            this.pnlBtnFav.Size = new System.Drawing.Size(104, 23);
            this.pnlBtnFav.TabIndex = 9;
            // 
            // cmdAddFave
            // 
            this.cmdAddFave.Dock = System.Windows.Forms.DockStyle.Left;
            this.cmdAddFave.Image = global::RedPrairie.MCS.WinMSQL.Properties.Resources.AddBtn;
            this.cmdAddFave.Location = new System.Drawing.Point(75, 0);
            this.cmdAddFave.Name = "cmdAddFave";
            this.cmdAddFave.Size = new System.Drawing.Size(28, 23);
            this.cmdAddFave.TabIndex = 1;
            this.cmdAddFave.UseVisualStyleBackColor = true;
            this.cmdAddFave.Click += new System.EventHandler(this.cmdAddFave_Click);
            // 
            // cmdFave
            // 
            this.cmdFave.Dock = System.Windows.Forms.DockStyle.Left;
            this.cmdFave.Location = new System.Drawing.Point(0, 0);
            this.cmdFave.Name = "cmdFave";
            this.cmdFave.Size = new System.Drawing.Size(75, 23);
            this.cmdFave.TabIndex = 0;
            this.cmdFave.Text = "Favorites";
            this.cmdFave.UseVisualStyleBackColor = true;
            this.cmdFave.Click += new System.EventHandler(this.cmdFave_Click);
            // 
            // btnTrace
            // 
            this.btnTrace.Enabled = false;
            this.btnTrace.Location = new System.Drawing.Point(437, 3);
            this.btnTrace.Name = "btnTrace";
            this.btnTrace.Size = new System.Drawing.Size(75, 23);
            this.btnTrace.TabIndex = 12;
            this.btnTrace.Text = "Start Trace";
            this.btnTrace.UseVisualStyleBackColor = true;
            this.btnTrace.Click += new System.EventHandler(this.btnTrace_Click);
            // 
            // btnViewTrace
            // 
            this.btnViewTrace.Enabled = false;
            this.btnViewTrace.Location = new System.Drawing.Point(518, 3);
            this.btnViewTrace.Name = "btnViewTrace";
            this.btnViewTrace.Size = new System.Drawing.Size(75, 23);
            this.btnViewTrace.TabIndex = 13;
            this.btnViewTrace.Text = "View Trace";
            this.btnViewTrace.UseVisualStyleBackColor = true;
            this.btnViewTrace.Click += new System.EventHandler(this.btnViewTrace_Click);
            // 
            // tabDashBoard
            // 
            this.tabDashBoard.AllowTabMoving = true;
            appearance1.BackColor = System.Drawing.SystemColors.ControlLight;
            appearance1.BackColor2 = System.Drawing.SystemColors.Window;
            appearance1.BackGradientStyle = Infragistics.Win.GradientStyle.Vertical;
            this.tabDashBoard.Appearance = appearance1;
            appearance2.BackColor = System.Drawing.SystemColors.Control;
            this.tabDashBoard.ClientAreaAppearance = appearance2;
            this.tabDashBoard.Controls.Add(this.ultraTabSharedControlsPage1);
            this.tabDashBoard.Controls.Add(this.ultraTabPageControl1);
            this.tabDashBoard.Controls.Add(this.ultraTabPageControl2);
            this.tabDashBoard.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tabDashBoard.Location = new System.Drawing.Point(0, 53);
            this.tabDashBoard.Name = "tabDashBoard";
            appearance3.BackColor = System.Drawing.SystemColors.Window;
            this.tabDashBoard.SelectedTabAppearance = appearance3;
            this.tabDashBoard.SharedControlsPage = this.ultraTabSharedControlsPage1;
            this.tabDashBoard.Size = new System.Drawing.Size(770, 461);
            appearance4.BackColor = System.Drawing.SystemColors.Control;
            this.tabDashBoard.TabHeaderAreaAppearance = appearance4;
            this.tabDashBoard.TabIndex = 10;
            ultraTab1.TabPage = this.ultraTabPageControl1;
            ultraTab1.Text = "disc";
            ultraTab2.Key = "tabBlank";
            ultraTab2.TabPage = this.ultraTabPageControl2;
            ultraTab2.Text = " ";
            this.tabDashBoard.Tabs.AddRange(new Infragistics.Win.UltraWinTabControl.UltraTab[] {
            ultraTab1,
            ultraTab2});
            this.tabDashBoard.UseOsThemes = Infragistics.Win.DefaultableBoolean.False;
            this.tabDashBoard.SelectedTabChanging += new Infragistics.Win.UltraWinTabControl.SelectedTabChangingEventHandler(this.tabDashBoard_SelectedTabChanging);
            this.tabDashBoard.MouseDown += new System.Windows.Forms.MouseEventHandler(this.tabDashBoard_MouseDown);
            this.tabDashBoard.ActiveTabChanged += new Infragistics.Win.UltraWinTabControl.ActiveTabChangedEventHandler(this.tabDashBoard_ActiveTabChanged);
            this.tabDashBoard.KeyDown += new System.Windows.Forms.KeyEventHandler(this.tabDashBoard_KeyDown);
            this.tabDashBoard.SelectedTabChanged += new Infragistics.Win.UltraWinTabControl.SelectedTabChangedEventHandler(this.tabDashBoard_SelectedTabChanged);
            // 
            // ultraTabSharedControlsPage1
            // 
            this.ultraTabSharedControlsPage1.Location = new System.Drawing.Point(-10000, -10000);
            this.ultraTabSharedControlsPage1.Name = "ultraTabSharedControlsPage1";
            this.ultraTabSharedControlsPage1.Size = new System.Drawing.Size(766, 435);
            // 
            // contextMenuStrip1
            // 
            this.contextMenuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripMenuItem1,
            this.toolStripSeparator6,
            this.toolStripMenuItem2,
            this.closeOtherTabsToolStripMenuItem,
            this.toolStripMenuItem5,
            this.changeTabColorToolStripMenuItem});
            this.contextMenuStrip1.Name = "contextMenuStrip1";
            this.contextMenuStrip1.ShowImageMargin = false;
            this.contextMenuStrip1.Size = new System.Drawing.Size(136, 104);
            this.contextMenuStrip1.Opening += new System.ComponentModel.CancelEventHandler(this.contextMenuStrip1_Opening);
            // 
            // toolStripMenuItem1
            // 
            this.toolStripMenuItem1.Name = "toolStripMenuItem1";
            this.toolStripMenuItem1.Size = new System.Drawing.Size(135, 22);
            this.toolStripMenuItem1.Text = "Add Tab";
            this.toolStripMenuItem1.Click += new System.EventHandler(this.toolStripMenuItem1_Click);
            // 
            // toolStripSeparator6
            // 
            this.toolStripSeparator6.Name = "toolStripSeparator6";
            this.toolStripSeparator6.Size = new System.Drawing.Size(132, 6);
            // 
            // toolStripMenuItem2
            // 
            this.toolStripMenuItem2.Name = "toolStripMenuItem2";
            this.toolStripMenuItem2.Size = new System.Drawing.Size(135, 22);
            this.toolStripMenuItem2.Text = "Close Tab";
            this.toolStripMenuItem2.Click += new System.EventHandler(this.removeTabToolStripMenuItem_Click);
            // 
            // closeOtherTabsToolStripMenuItem
            // 
            this.closeOtherTabsToolStripMenuItem.Name = "closeOtherTabsToolStripMenuItem";
            this.closeOtherTabsToolStripMenuItem.Size = new System.Drawing.Size(135, 22);
            this.closeOtherTabsToolStripMenuItem.Text = "Close Other Tabs";
            this.closeOtherTabsToolStripMenuItem.Click += new System.EventHandler(this.closeOtherTabsToolStripMenuItem_Click);
            // 
            // toolStripMenuItem5
            // 
            this.toolStripMenuItem5.Name = "toolStripMenuItem5";
            this.toolStripMenuItem5.Size = new System.Drawing.Size(132, 6);
            // 
            // changeTabColorToolStripMenuItem
            // 
            this.changeTabColorToolStripMenuItem.Name = "changeTabColorToolStripMenuItem";
            this.changeTabColorToolStripMenuItem.Size = new System.Drawing.Size(135, 22);
            this.changeTabColorToolStripMenuItem.Text = "Change Tab Color";
            this.changeTabColorToolStripMenuItem.Click += new System.EventHandler(this.changeTabColorToolStripMenuItem_Click);
            // 
            // ctlWinMSQL
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(770, 514);
            this.Controls.Add(this.tabDashBoard);
            this.Controls.Add(this.tableLayoutPanel1);
            this.Controls.Add(this.menuStrip1);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MainMenuStrip = this.menuStrip1;
            this.MinimumSize = new System.Drawing.Size(778, 541);
            this.Name = "ctlWinMSQL";
            this.Text = "WinMSQL";
            this.Shown += new System.EventHandler(this.ctlWinMSQL_Shown);
            this.DoubleClick += new System.EventHandler(this.ctlWinMSQL_DoubleClick);
            this.Activated += new System.EventHandler(this.ctlWinMSQL_Activated);
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.ctlWinMSQL_FormClosed);
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.ctlWinMSQL_FormClosing);
            this.ultraTabPageControl1.ResumeLayout(false);
            this.ultraTabPageControl1.PerformLayout();
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.pnlBtnFav.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.tabDashBoard)).EndInit();
            this.tabDashBoard.ResumeLayout(false);
            this.contextMenuStrip1.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.ToolStripMenuItem newTabToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem removeTabToolStripMenuItem;
		private System.Windows.Forms.ToolStripSeparator toolStripSeparator4;
        private System.Windows.Forms.ToolStripMenuItem toolsToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem executeToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem formatCommandToolStripMenuItem;
        private System.Windows.Forms.ContextMenuStrip contextMenuStrip1;
        private System.Windows.Forms.ToolStripMenuItem toolStripMenuItem1;
		private System.Windows.Forms.ToolStripMenuItem toolStripMenuItem2;
		private System.Windows.Forms.Button cmdOptions;
        private System.Windows.Forms.ToolStripMenuItem toolStripMenuItem3;
		private winMSQLTab winMSQLTab1;
		private System.Windows.Forms.ToolStripMenuItem configureServersToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem editToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem cutToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem copyToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem pasteToolStripMenuItem;
		private System.Windows.Forms.ToolStripSeparator toolStripSeparator5;
		private System.Windows.Forms.ToolStripMenuItem findAndReplaceToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem quickFindToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem quickReplaceToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem closeOtherTabsToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator6;
        private System.Windows.Forms.Button cmdFave;
        private System.Windows.Forms.Panel pnlBtnFav;
        private System.Windows.Forms.Button cmdAddFave;
        private Infragistics.Win.UltraWinTabControl.UltraTabControl tabDashBoard;
        private Infragistics.Win.UltraWinTabControl.UltraTabSharedControlsPage ultraTabSharedControlsPage1;
        private Infragistics.Win.UltraWinTabControl.UltraTabPageControl ultraTabPageControl1;
        private Infragistics.Win.UltraWinTabControl.UltraTabPageControl ultraTabPageControl2;
        private System.Windows.Forms.ToolStripSeparator toolStripMenuItem5;
        private System.Windows.Forms.ToolStripMenuItem changeTabColorToolStripMenuItem;
        private System.Windows.Forms.Button btnTrace;
        private System.Windows.Forms.Button btnViewTrace;

    }
}

