using System;
using System.Collections;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlTracing : Form
	{
		#region Variables

		public Hashtable TraceOptions = new Hashtable();

		#endregion Variables

		#region Constructors

		public ctlTracing() : this(null){}

        public ctlTracing(Hashtable opts)
        {
            InitializeComponent();
			if (opts != null)
				TraceOptions = opts;
            txtFileName.Focus();
		}

		#endregion Constructors

		#region Public Methods

		public void LoadTraceOptions()
		{
			foreach (Control ctl in groupBox1.Controls)
			{
				switch (ctl.Name.Substring(0, 3))
				{
					case "chk":
						if (TraceOptions.ContainsKey(ctl.Name))
							((CheckBox)ctl).Checked = TraceOptions[ctl.Name].ToString() == "True" ? true : false;
						break;
					case "txt":
						if (TraceOptions.ContainsKey(ctl.Name))
							((TextBox)ctl).Text = TraceOptions[ctl.Name].ToString();
						break;
					default:
						break;
				}
			}
		}

        public void getTraceOptions(ref String filename, ref String options)
        {
            string opts = "";
            filename = txtFileName.Text;

            if (chkAppFlowMsgs.Checked)
                opts += "W";
            if (chkSQLLibCalls.Checked)
                opts += "S";
            if (chkConMgrMsgs.Checked)
                opts += "M";
            if (chkSrvFlwMsgs.Checked)
                opts += "X";
            if (chkSrvArgs.Checked)
                opts += "A";
            if (chkPerfStat.Checked)
                opts += "R";
            if (chkCmdProf.Checked)
                opts += "C";

            options = opts;
		}

		#endregion Public Methods

		#region Private Methods

		private void SaveTraceOptions()
		{
			foreach (Control ctl in groupBox1.Controls)
			{
				switch(ctl.Name.Substring(0, 3))
				{
					case "chk":
						if (!TraceOptions.ContainsKey(ctl.Name))
							TraceOptions.Add(ctl.Name, "");
						TraceOptions[ctl.Name] = ((CheckBox)ctl).Checked.ToString();
						break;
					case "txt":
						if (!TraceOptions.ContainsKey(ctl.Name))
							TraceOptions.Add(ctl.Name, "");
						TraceOptions[ctl.Name] = ((TextBox)ctl).Text;
						break;
					default:
						break;
				}
			}
		}

        private void cmdOk_Click(object sender, EventArgs e)
        {
            if (txtFileName.Text.Length <= 0)
            {
                MessageBox.Show("Must include a filename.",
                                "Server Trace",
                                MessageBoxButtons.OK);
                txtFileName.Focus();
            }
            else
            {
                DialogResult = DialogResult.OK;
				SaveTraceOptions();
				this.Hide();
            }
        }

        private void cmdCancel_Click(object sender, EventArgs e)
        {
			DialogResult = DialogResult.Cancel;
			this.Hide();
        }

        private void txtFileName_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == '\n' || e.KeyChar == '\r')
            {
                e.Handled = true;
                cmdOk_Click(sender, new EventArgs());
            }
        }

		private void ctlTracing_Shown(object sender, EventArgs e)
		{
			this.CenterToParent();
			LoadTraceOptions();
			txtFileName.Focus();
		}

		#endregion Private Methods
	}
}