namespace RedPrairie.MCS.WinMSQL
{
    partial class ctlLogin
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ctlLogin));
            this.lblUsrNam = new System.Windows.Forms.Label();
            this.lblPswd = new System.Windows.Forms.Label();
            this.txtUserName = new System.Windows.Forms.TextBox();
            this.txtPassword = new System.Windows.Forms.TextBox();
            this.cmdOk = new System.Windows.Forms.Button();
            this.cmdCancel = new System.Windows.Forms.Button();
            this.lblHostURL = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // lblUsrNam
            // 
            this.lblUsrNam.AutoSize = true;
            this.lblUsrNam.Location = new System.Drawing.Point(17, 49);
            this.lblUsrNam.Name = "lblUsrNam";
            this.lblUsrNam.Size = new System.Drawing.Size(60, 13);
            this.lblUsrNam.TabIndex = 3;
            this.lblUsrNam.Text = "User Name";
            // 
            // lblPswd
            // 
            this.lblPswd.AutoSize = true;
            this.lblPswd.Location = new System.Drawing.Point(24, 75);
            this.lblPswd.Name = "lblPswd";
            this.lblPswd.Size = new System.Drawing.Size(53, 13);
            this.lblPswd.TabIndex = 4;
            this.lblPswd.Text = "Password";
            // 
            // txtUserName
            // 
            this.txtUserName.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.txtUserName.Location = new System.Drawing.Point(83, 46);
            this.txtUserName.Name = "txtUserName";
            this.txtUserName.Size = new System.Drawing.Size(167, 20);
            this.txtUserName.TabIndex = 10;
            this.txtUserName.MouseClick += new System.Windows.Forms.MouseEventHandler(this.txtUsrNam_MouseClick);
            this.txtUserName.Enter += new System.EventHandler(this.txtUserName_Enter);
            // 
            // txtPassword
            // 
            this.txtPassword.Location = new System.Drawing.Point(83, 72);
            this.txtPassword.Name = "txtPassword";
            this.txtPassword.Size = new System.Drawing.Size(167, 20);
            this.txtPassword.TabIndex = 11;
            this.txtPassword.UseSystemPasswordChar = true;
            this.txtPassword.MouseClick += new System.Windows.Forms.MouseEventHandler(this.txtPswd_MouseClick);
            this.txtPassword.Enter += new System.EventHandler(this.txtPassword_Enter);
            // 
            // cmdOk
            // 
            this.cmdOk.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.cmdOk.DialogResult = System.Windows.Forms.DialogResult.OK;
            this.cmdOk.Location = new System.Drawing.Point(94, 100);
            this.cmdOk.Name = "cmdOk";
            this.cmdOk.Size = new System.Drawing.Size(75, 23);
            this.cmdOk.TabIndex = 12;
            this.cmdOk.Text = "Ok";
            this.cmdOk.UseVisualStyleBackColor = true;
            this.cmdOk.Click += new System.EventHandler(this.cmdOk_Click);
            // 
            // cmdCancel
            // 
            this.cmdCancel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.cmdCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cmdCancel.Location = new System.Drawing.Point(175, 100);
            this.cmdCancel.Name = "cmdCancel";
            this.cmdCancel.Size = new System.Drawing.Size(75, 23);
            this.cmdCancel.TabIndex = 13;
            this.cmdCancel.Text = "Cancel";
            this.cmdCancel.UseVisualStyleBackColor = true;
            this.cmdCancel.Click += new System.EventHandler(this.cmdCancel_Click);
            // 
            // lblHostURL
            // 
            this.lblHostURL.AutoSize = true;
            this.lblHostURL.Location = new System.Drawing.Point(17, 19);
            this.lblHostURL.Name = "lblHostURL";
            this.lblHostURL.Size = new System.Drawing.Size(163, 13);
            this.lblHostURL.TabIndex = 0;
            this.lblHostURL.Text = "Password Confirmation Required:";
            // 
            // ctlLogin
            // 
            this.AcceptButton = this.cmdOk;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.cmdCancel;
            this.ClientSize = new System.Drawing.Size(278, 135);
            this.Controls.Add(this.cmdCancel);
            this.Controls.Add(this.cmdOk);
            this.Controls.Add(this.txtPassword);
            this.Controls.Add(this.txtUserName);
            this.Controls.Add(this.lblPswd);
            this.Controls.Add(this.lblUsrNam);
            this.Controls.Add(this.lblHostURL);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.MaximumSize = new System.Drawing.Size(294, 235);
            this.MinimizeBox = false;
            this.Name = "ctlLogin";
            this.ShowInTaskbar = false;
            this.Text = "Login";
            this.Shown += new System.EventHandler(this.ctlLogin_Shown);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label lblUsrNam;
        private System.Windows.Forms.Label lblPswd;
        private System.Windows.Forms.TextBox txtUserName;
        private System.Windows.Forms.TextBox txtPassword;
        private System.Windows.Forms.Button cmdOk;
        private System.Windows.Forms.Button cmdCancel;
        private System.Windows.Forms.Label lblHostURL;
    }
}