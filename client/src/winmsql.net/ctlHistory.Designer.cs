namespace RedPrairie.MCS.WinMSQL
{
    partial class ctlHistory
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
            this.components = new System.ComponentModel.Container();
            this.lstView = new System.Windows.Forms.ListView();
            this.cmdClear = new System.Windows.Forms.Button();
            this.cmdCancel = new System.Windows.Forms.Button();
            this.panel1 = new System.Windows.Forms.Panel();
            this.contextMenuStrip1 = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.copyCommandToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.sendToCurrentTabToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.openInNewTabToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.panel1.SuspendLayout();
            this.contextMenuStrip1.SuspendLayout();
            this.SuspendLayout();
            // 
            // lstView
            // 
            this.lstView.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.lstView.FullRowSelect = true;
            this.lstView.Location = new System.Drawing.Point(0, 0);
            this.lstView.MultiSelect = false;
            this.lstView.Name = "lstView";
            this.lstView.Size = new System.Drawing.Size(642, 204);
            this.lstView.TabIndex = 0;
            this.lstView.UseCompatibleStateImageBehavior = false;
            this.lstView.View = System.Windows.Forms.View.Details;
            this.lstView.DoubleClick += new System.EventHandler(this.lstView_DoubleClick);
            this.lstView.MouseDown += new System.Windows.Forms.MouseEventHandler(this.lstView_MouseDown);
            // 
            // cmdClear
            // 
            this.cmdClear.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.cmdClear.Location = new System.Drawing.Point(3, 210);
            this.cmdClear.Name = "cmdClear";
            this.cmdClear.Size = new System.Drawing.Size(75, 23);
            this.cmdClear.TabIndex = 1;
            this.cmdClear.Text = "Clear";
            this.cmdClear.UseVisualStyleBackColor = true;
            this.cmdClear.Click += new System.EventHandler(this.cmdClear_Click);
            // 
            // cmdCancel
            // 
            this.cmdCancel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.cmdCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cmdCancel.Location = new System.Drawing.Point(564, 210);
            this.cmdCancel.Name = "cmdCancel";
            this.cmdCancel.Size = new System.Drawing.Size(75, 23);
            this.cmdCancel.TabIndex = 2;
            this.cmdCancel.Text = "Close";
            this.cmdCancel.UseVisualStyleBackColor = true;
            this.cmdCancel.Click += new System.EventHandler(this.cmdCancel_Click);
            // 
            // panel1
            // 
            this.panel1.Controls.Add(this.lstView);
            this.panel1.Controls.Add(this.cmdCancel);
            this.panel1.Controls.Add(this.cmdClear);
            this.panel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel1.Location = new System.Drawing.Point(0, 0);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(642, 236);
            this.panel1.TabIndex = 3;
            // 
            // contextMenuStrip1
            // 
            this.contextMenuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.copyCommandToolStripMenuItem,
            this.toolStripSeparator1,
            this.sendToCurrentTabToolStripMenuItem,
            this.openInNewTabToolStripMenuItem});
            this.contextMenuStrip1.Name = "contextMenuStrip1";
            this.contextMenuStrip1.ShowImageMargin = false;
            this.contextMenuStrip1.Size = new System.Drawing.Size(148, 76);
            // 
            // copyCommandToolStripMenuItem
            // 
            this.copyCommandToolStripMenuItem.Name = "copyCommandToolStripMenuItem";
            this.copyCommandToolStripMenuItem.Size = new System.Drawing.Size(147, 22);
            this.copyCommandToolStripMenuItem.Text = "Copy";
            this.copyCommandToolStripMenuItem.Click += new System.EventHandler(this.copyCommandToolStripMenuItem_Click);
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(144, 6);
            // 
            // sendToCurrentTabToolStripMenuItem
            // 
            this.sendToCurrentTabToolStripMenuItem.Name = "sendToCurrentTabToolStripMenuItem";
            this.sendToCurrentTabToolStripMenuItem.Size = new System.Drawing.Size(147, 22);
            this.sendToCurrentTabToolStripMenuItem.Text = "Send to Current Tab";
            this.sendToCurrentTabToolStripMenuItem.Click += new System.EventHandler(this.sendToCurrentTabToolStripMenuItem_Click);
            // 
            // openInNewTabToolStripMenuItem
            // 
            this.openInNewTabToolStripMenuItem.Name = "openInNewTabToolStripMenuItem";
            this.openInNewTabToolStripMenuItem.Size = new System.Drawing.Size(147, 22);
            this.openInNewTabToolStripMenuItem.Text = "Open in New Tab";
            this.openInNewTabToolStripMenuItem.Click += new System.EventHandler(this.openInNewTabToolStripMenuItem_Click);
            // 
            // ctlHistory
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.cmdCancel;
            this.ClientSize = new System.Drawing.Size(642, 236);
            this.Controls.Add(this.panel1);
            this.MinimumSize = new System.Drawing.Size(464, 270);
            this.Name = "ctlHistory";
            this.Text = "History";
            this.Shown += new System.EventHandler(this.ctlHistory_Shown);
            this.panel1.ResumeLayout(false);
            this.contextMenuStrip1.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListView lstView;
        private System.Windows.Forms.Button cmdClear;
        private System.Windows.Forms.Button cmdCancel;
        private System.Windows.Forms.Panel panel1;
		private System.Windows.Forms.ContextMenuStrip contextMenuStrip1;
		private System.Windows.Forms.ToolStripMenuItem copyCommandToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem sendToCurrentTabToolStripMenuItem;
		private System.Windows.Forms.ToolStripMenuItem openInNewTabToolStripMenuItem;
		private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
    }
}