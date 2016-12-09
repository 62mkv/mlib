using System;
using System.Windows.Forms;
using RedPrairie.MOCA.Client;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlLogin : Form
	{
		#region Variables
        private readonly FullConnection connection;
		#endregion Variables

		#region Constructors

        public ctlLogin(FullConnection connection, string userName, bool userOverride, string loginMessage)
        {
            InitializeComponent();
            this.connection = connection;
            txtUserName.Text = userOverride ? "" : userName;
            lblHostURL.Text = (loginMessage == null) 
                                ? (userOverride ? "Supervisor Override Needed:" : lblHostURL.Text)
                                : loginMessage;
            txtUserName.Enabled = userOverride;
		}

		#endregion Constructors

		#region Event Hanlders

        /// <summary>
        /// Handles the Click event of the cmdCancel control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
		private void cmdCancel_Click(object sender, EventArgs e)
        {
		    DialogResult = DialogResult.Cancel;
		    Close();
        }

        /// <summary>
        /// Handles the Click event of the cmdOk control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void cmdOk_Click(object sender, EventArgs e)
        {
            try
            {
                connection.ConfirmDigitalSignature(txtUserName.Text, txtPassword.Text);
                DialogResult = DialogResult.OK;
                Close();
            }
            catch (LoginFailedException)
            {
                string exception = string.Format("User Login failed for {0}, try again?", txtUserName.Text);
                DialogResult result = MessageBox.Show(exception, Text, MessageBoxButtons.YesNo, MessageBoxIcon.Error);

                if (result == DialogResult.No)
                {
                    DialogResult = DialogResult.Cancel;
                    Close();
                }
                else if (result == DialogResult.Yes)
                {
                    DialogResult = DialogResult.None;
                    txtPassword.Focus();
                    txtPassword.SelectAll();
                    return;
                }
            } 
		}

        /// <summary>
        /// Handles the Enter event of the txtUsrNam control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void txtUserName_Enter(object sender, EventArgs e)
        {
            if (txtUserName.SelectionLength <= 0)
            {
                txtUserName.SelectionStart = 0;
                txtUserName.SelectionLength = txtUserName.Text.Length;
            }
        }

        /// <summary>
        /// Handles the Enter event of the txtPassword control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void txtPassword_Enter(object sender, EventArgs e)
        {
            if (txtPassword.SelectionLength <= 0)
            {
                txtPassword.SelectionStart = 0;
                txtPassword.SelectionLength = txtPassword.Text.Length;
            }
        }

        /// <summary>
        /// Handles the MouseClick event of the txtUsrNam control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.Windows.Forms.MouseEventArgs"/> instance containing the event data.</param>
        private void txtUsrNam_MouseClick(object sender, MouseEventArgs e)
        {
            if (txtUserName.SelectionLength <= 0)
            {
                txtUserName.SelectionStart = 0;
                txtUserName.SelectionLength = txtUserName.Text.Length;
            }
        }

        /// <summary>
        /// Handles the MouseClick event of the txtPswd control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.Windows.Forms.MouseEventArgs"/> instance containing the event data.</param>
        private void txtPswd_MouseClick(object sender, MouseEventArgs e)
        {
            if (txtPassword.SelectionLength <= 0)
            {
                txtPassword.SelectionStart = 0;
                txtPassword.SelectionLength = txtPassword.Text.Length;
            }
        }

        /// <summary>
        /// Handles the Shown event of the ctlLogin control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void ctlLogin_Shown(object sender, EventArgs e)
        {
            CenterToParent();

            if (txtUserName.Enabled)
                txtUserName.Focus();
            else
                txtPassword.Focus();

        }

       	#endregion Event Handlers
	}
}