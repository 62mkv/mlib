using System;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlValue : Form
    {
        public ctlValue(string val)
        {
            InitializeComponent();
            txtValue.Text = val;
        }

        private void cmdClose_Click(object sender, EventArgs e)
        {
            Close();
        }

		private void ctlValue_Shown(object sender, EventArgs e)
		{
			this.CenterToParent();
		}
    }
}