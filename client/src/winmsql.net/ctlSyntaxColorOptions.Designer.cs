namespace RedPrairie.MCS.WinMSQL
{
    partial class ctlSyntaxColorOptions
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ctlSyntaxColorOptions));
            this.btnOK = new System.Windows.Forms.Button();
            this.btnCancel = new System.Windows.Forms.Button();
            this.panel1 = new System.Windows.Forms.Panel();
            this.lstColors = new System.Windows.Forms.ListView();
            this.keyType = new System.Windows.Forms.ColumnHeader();
            this.color = new System.Windows.Forms.ColumnHeader();
            this.colorDialog = new System.Windows.Forms.ColorDialog();
            this.label1 = new System.Windows.Forms.Label();
            this.txtTypeName = new System.Windows.Forms.TextBox();
            this.btnAdd = new System.Windows.Forms.Button();
            this.btnRemove = new System.Windows.Forms.Button();
            this.panel1.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnOK
            // 
            resources.ApplyResources(this.btnOK, "btnOK");
            this.btnOK.Name = "btnOK";
            this.btnOK.UseVisualStyleBackColor = true;
            this.btnOK.Click += new System.EventHandler(this.button_Click);
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            resources.ApplyResources(this.btnCancel, "btnCancel");
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.button_Click);
            // 
            // panel1
            // 
            this.panel1.Controls.Add(this.lstColors);
            resources.ApplyResources(this.panel1, "panel1");
            this.panel1.Name = "panel1";
            // 
            // lstColors
            // 
            this.lstColors.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.keyType,
            this.color});
            resources.ApplyResources(this.lstColors, "lstColors");
            this.lstColors.FullRowSelect = true;
            this.lstColors.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.Nonclickable;
            this.lstColors.Items.AddRange(new System.Windows.Forms.ListViewItem[] {
            ((System.Windows.Forms.ListViewItem)(resources.GetObject("lstColors.Items"))),
            ((System.Windows.Forms.ListViewItem)(resources.GetObject("lstColors.Items1"))),
            ((System.Windows.Forms.ListViewItem)(resources.GetObject("lstColors.Items2")))});
            this.lstColors.MultiSelect = false;
            this.lstColors.Name = "lstColors";
            this.lstColors.UseCompatibleStateImageBehavior = false;
            this.lstColors.View = System.Windows.Forms.View.Details;
            this.lstColors.DoubleClick += new System.EventHandler(this.lstColors_DoubleClick);
            // 
            // keyType
            // 
            resources.ApplyResources(this.keyType, "keyType");
            // 
            // color
            // 
            resources.ApplyResources(this.color, "color");
            // 
            // colorDialog
            // 
            this.colorDialog.AnyColor = true;
            // 
            // label1
            // 
            resources.ApplyResources(this.label1, "label1");
            this.label1.Name = "label1";
            // 
            // txtTypeName
            // 
            resources.ApplyResources(this.txtTypeName, "txtTypeName");
            this.txtTypeName.Name = "txtTypeName";
            this.txtTypeName.TextChanged += new System.EventHandler(this.txtTypeName_TextChanged);
            this.txtTypeName.KeyDown += new System.Windows.Forms.KeyEventHandler(this.txtTypeName_KeyDown);
            // 
            // btnAdd
            // 
            resources.ApplyResources(this.btnAdd, "btnAdd");
            this.btnAdd.Name = "btnAdd";
            this.btnAdd.UseVisualStyleBackColor = true;
            this.btnAdd.Click += new System.EventHandler(this.btnAdd_click);
            // 
            // btnRemove
            // 
            resources.ApplyResources(this.btnRemove, "btnRemove");
            this.btnRemove.Name = "btnRemove";
            this.btnRemove.UseVisualStyleBackColor = true;
            this.btnRemove.Click += new System.EventHandler(this.btnRemove_Click);
            // 
            // ctlSyntaxColorOptions
            // 
            this.AcceptButton = this.btnOK;
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.Controls.Add(this.btnRemove);
            this.Controls.Add(this.btnAdd);
            this.Controls.Add(this.txtTypeName);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.btnOK);
            this.Controls.Add(this.btnCancel);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "ctlSyntaxColorOptions";
            this.panel1.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnOK;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.ListView lstColors;
        private System.Windows.Forms.ColumnHeader keyType;
        private System.Windows.Forms.ColumnHeader color;
        private System.Windows.Forms.ColorDialog colorDialog;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox txtTypeName;
        private System.Windows.Forms.Button btnAdd;
        private System.Windows.Forms.Button btnRemove;

    }
}