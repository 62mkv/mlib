namespace RedPrairie.MCS.WinMSQL
{
    partial class ctlColumnInfo
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
            this.gridMst = new System.Windows.Forms.DataGridView();
            ((System.ComponentModel.ISupportInitialize)(this.gridMst)).BeginInit();
            this.SuspendLayout();
            // 
            // gridMst
            // 
            this.gridMst.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.gridMst.Dock = System.Windows.Forms.DockStyle.Fill;
            this.gridMst.Location = new System.Drawing.Point(0, 0);
            this.gridMst.Name = "gridMst";
            this.gridMst.Size = new System.Drawing.Size(338, 190);
            this.gridMst.TabIndex = 0;
            // 
            // ctlColumnInfo
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(338, 190);
            this.Controls.Add(this.gridMst);
            this.Name = "ctlColumnInfo";
            this.ShowInTaskbar = false;
            this.Text = "Column Info";
            this.Shown += new System.EventHandler(this.ctlColumnInfo_Shown);
            ((System.ComponentModel.ISupportInitialize)(this.gridMst)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.DataGridView gridMst;
    }
}