namespace RedPrairie.MCS.WinMSQL
{
    partial class ctlFavorite
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
            this.cmdCancel = new System.Windows.Forms.Button();
            this.cmdClear = new System.Windows.Forms.Button();
            this.cmdDelete = new System.Windows.Forms.Button();
            this.btnPost = new System.Windows.Forms.Button();
            this.btnPrev = new System.Windows.Forms.Button();
            this.cmsLstMenu = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.tsmiCopyCmd = new System.Windows.Forms.ToolStripMenuItem();
            this.tsmiDeleteCmd = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.tsmiSendToCur = new System.Windows.Forms.ToolStripMenuItem();
            this.tsmiOpenInNew = new System.Windows.Forms.ToolStripMenuItem();
            this.cmsLstMenu.SuspendLayout();
            this.SuspendLayout();
            // 
            // lstView
            // 
            this.lstView.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.lstView.FullRowSelect = true;
            this.lstView.HideSelection = false;
            this.lstView.Location = new System.Drawing.Point(1, -1);
            this.lstView.MultiSelect = false;
            this.lstView.Name = "lstView";
            this.lstView.Size = new System.Drawing.Size(542, 181);
            this.lstView.TabIndex = 1;
            this.lstView.UseCompatibleStateImageBehavior = false;
            this.lstView.View = System.Windows.Forms.View.Details;
            this.lstView.ItemMouseHover += new System.Windows.Forms.ListViewItemMouseHoverEventHandler(this.lstView_ItemMouseHover);
            this.lstView.DoubleClick += new System.EventHandler(this.lstView_DoubleClick);
            this.lstView.MouseUp += new System.Windows.Forms.MouseEventHandler(this.lstView_MouseUp);
            this.lstView.ItemDrag += new System.Windows.Forms.ItemDragEventHandler(this.lstView_ItemDrag);
            // 
            // cmdCancel
            // 
            this.cmdCancel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.cmdCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cmdCancel.Location = new System.Drawing.Point(464, 186);
            this.cmdCancel.Name = "cmdCancel";
            this.cmdCancel.Size = new System.Drawing.Size(75, 23);
            this.cmdCancel.TabIndex = 6;
            this.cmdCancel.Text = "Cl&ose";
            this.cmdCancel.UseVisualStyleBackColor = true;
            this.cmdCancel.Click += new System.EventHandler(this.cmdCancel_Click);
            // 
            // cmdClear
            // 
            this.cmdClear.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.cmdClear.Location = new System.Drawing.Point(153, 186);
            this.cmdClear.Name = "cmdClear";
            this.cmdClear.Size = new System.Drawing.Size(75, 23);
            this.cmdClear.TabIndex = 5;
            this.cmdClear.Text = "&Clear";
            this.cmdClear.UseVisualStyleBackColor = true;
            this.cmdClear.Click += new System.EventHandler(this.cmdClear_Click);
            // 
            // cmdDelete
            // 
            this.cmdDelete.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.cmdDelete.Location = new System.Drawing.Point(72, 186);
            this.cmdDelete.Name = "cmdDelete";
            this.cmdDelete.Size = new System.Drawing.Size(75, 23);
            this.cmdDelete.TabIndex = 4;
            this.cmdDelete.Text = "&Delete";
            this.cmdDelete.UseVisualStyleBackColor = true;
            this.cmdDelete.Click += new System.EventHandler(this.cmdDelete_Click);
            // 
            // btnPost
            // 
            this.btnPost.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.btnPost.Image = global::RedPrairie.MCS.WinMSQL.Properties.Resources.NextRecord;
            this.btnPost.Location = new System.Drawing.Point(38, 186);
            this.btnPost.Name = "btnPost";
            this.btnPost.Size = new System.Drawing.Size(28, 23);
            this.btnPost.TabIndex = 3;
            this.btnPost.UseVisualStyleBackColor = true;
            this.btnPost.Click += new System.EventHandler(this.btnPost_Click);
            // 
            // btnPrev
            // 
            this.btnPrev.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.btnPrev.Image = global::RedPrairie.MCS.WinMSQL.Properties.Resources.PrevRecord;
            this.btnPrev.Location = new System.Drawing.Point(4, 186);
            this.btnPrev.Name = "btnPrev";
            this.btnPrev.Size = new System.Drawing.Size(28, 23);
            this.btnPrev.TabIndex = 2;
            this.btnPrev.UseVisualStyleBackColor = true;
            this.btnPrev.Click += new System.EventHandler(this.btnPrev_Click);
            // 
            // cmsLstMenu
            // 
            this.cmsLstMenu.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.tsmiCopyCmd,
            this.tsmiDeleteCmd,
            this.toolStripSeparator1,
            this.tsmiSendToCur,
            this.tsmiOpenInNew});
            this.cmsLstMenu.Name = "contextMenuStrip1";
            this.cmsLstMenu.ShowImageMargin = false;
            this.cmsLstMenu.Size = new System.Drawing.Size(148, 98);
            this.cmsLstMenu.Opening += new System.ComponentModel.CancelEventHandler(this.cmsLstMenu_Opening);
            // 
            // tsmiCopyCmd
            // 
            this.tsmiCopyCmd.Name = "tsmiCopyCmd";
            this.tsmiCopyCmd.Size = new System.Drawing.Size(147, 22);
            this.tsmiCopyCmd.Text = "Copy";
            this.tsmiCopyCmd.Click += new System.EventHandler(this.tsmiCopyCmd_Click);
            // 
            // tsmiDeleteCmd
            // 
            this.tsmiDeleteCmd.Name = "tsmiDeleteCmd";
            this.tsmiDeleteCmd.Size = new System.Drawing.Size(147, 22);
            this.tsmiDeleteCmd.Text = "Delete";
            this.tsmiDeleteCmd.Click += new System.EventHandler(this.tsmiDeleteCmd_Click);
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(144, 6);
            // 
            // tsmiSendToCur
            // 
            this.tsmiSendToCur.Name = "tsmiSendToCur";
            this.tsmiSendToCur.Size = new System.Drawing.Size(147, 22);
            this.tsmiSendToCur.Text = "Send to Current Tab";
            this.tsmiSendToCur.Click += new System.EventHandler(this.tsmiSendToCur_Click);
            // 
            // tsmiOpenInNew
            // 
            this.tsmiOpenInNew.Name = "tsmiOpenInNew";
            this.tsmiOpenInNew.Size = new System.Drawing.Size(147, 22);
            this.tsmiOpenInNew.Text = "Open in New Tab";
            this.tsmiOpenInNew.Click += new System.EventHandler(this.tsmiOpenInNew_Click);
            // 
            // ctlFavorite
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(542, 216);
            this.Controls.Add(this.btnPost);
            this.Controls.Add(this.btnPrev);
            this.Controls.Add(this.cmdDelete);
            this.Controls.Add(this.cmdCancel);
            this.Controls.Add(this.cmdClear);
            this.Controls.Add(this.lstView);
            this.Name = "ctlFavorite";
            this.Text = "Favorites";
            this.Shown += new System.EventHandler(this.ctlFavorite_Shown);
            this.cmsLstMenu.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListView lstView;
        private System.Windows.Forms.Button cmdCancel;
        private System.Windows.Forms.Button cmdClear;
        private System.Windows.Forms.Button cmdDelete;
        private System.Windows.Forms.Button btnPrev;
        private System.Windows.Forms.Button btnPost;
        private System.Windows.Forms.ContextMenuStrip cmsLstMenu;
        private System.Windows.Forms.ToolStripMenuItem tsmiCopyCmd;
        private System.Windows.Forms.ToolStripMenuItem tsmiDeleteCmd;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripMenuItem tsmiSendToCur;
        private System.Windows.Forms.ToolStripMenuItem tsmiOpenInNew;
    }
}