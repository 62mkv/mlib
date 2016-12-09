using System;
using System.Collections;
using System.IO;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlHistory : Form
	{
		#region Private Variables

		private const string DefaultXMLHistoryName = "WinMSQLCommandHistory.xml";
        private ctlWinMSQL WinMSQL;
        private ArrayList commandHist;

		#endregion Private Variables

		#region Constructor

		public ctlHistory(ArrayList commandHist, ctlWinMSQL WinMSQL)
        {
            InitializeComponent();

			lstView.Columns.Add("Status", 45, HorizontalAlignment.Center);
            lstView.Columns.Add("Rows", 45, HorizontalAlignment.Center);
            lstView.Columns.Add("Command", 350, HorizontalAlignment.Left);
            lstView.Columns.Add("Service", 120, HorizontalAlignment.Center);
            lstView.Columns.Add("Date/Time", 120, HorizontalAlignment.Center);

            this.WinMSQL = WinMSQL;
            this.commandHist = commandHist;

            fillHistoryList();
		}

		#endregion Constructro

		#region Public Methods

        public void addHistory(int sts, string cmd,int rows, string service)
        {
            ListViewItem lvi = new ListViewItem();
            lvi.Text = sts.ToString();
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, rows.ToString()));
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, cmd));
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, service));
			lvi.ToolTipText = cmd;
            lstView.Items.Insert(0, lvi);
        }

        public void fillHistoryList()
        {
            foreach (Command cmd in commandHist)
            {
                lstView.Items.Add(cmd.getListViewItem());
            }
		}

		#endregion Public Methods

		#region Private Methods

		private void SendCmdToCurrentTab()
		{
			if (lstView.SelectedItems.Count > 0)
			{
				WinMSQL.currentTab.Command = lstView.SelectedItems[0].SubItems[2].Text;
				WinMSQL.commandIndex = lstView.SelectedItems[0].Index;
                // Reset the trace list of Ctrl-Up/Down.
                WinMSQL.ResetUpDownHistory();
                // Add the selected command to the trace list
                WinMSQL.listUpDownHist.Add(WinMSQL.currentTab.Command);
				WinMSQL.currentTab.focusCommand();
			}
		}

		private void SendToNewTab()
		{
			if (lstView.SelectedItems.Count <= 0)
				return;

			WinMSQL.addNewTab();
			SendCmdToCurrentTab();

			WinMSQL.currentTab.Service = lstView.SelectedItems[0].SubItems[3].Text;

			WinMSQL.currentTab.focusUser();
		}

		private void cmdCancel_Click(object sender, EventArgs e)
        {
            Hide();
        }

        private void cmdClear_Click(object sender, EventArgs e)
        {
            lstView.Items.Clear();
            commandHist.Clear();
			ClearXMLHistory();
        }

        private void lstView_DoubleClick(object sender, EventArgs e)
        {
			SendCmdToCurrentTab();
        }

		private void ClearXMLHistory()
		{
            System.Diagnostics.Process proc = new System.Diagnostics.Process();
            string path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);

            if (!string.IsNullOrEmpty(path))
            {
                path = string.Concat(path, "\\RedPrairie\\WinMSQL\\");
                if (!Directory.Exists(path))
                {
                    path = Directory.GetCurrentDirectory();
                    path = string.Concat(path, "\\");
                }
            }
            else
            {
                path = Directory.GetCurrentDirectory();
                path = string.Concat(path, "\\");
            }

            if (!File.Exists(string.Concat(path, DefaultXMLHistoryName)))
            {
                // If all previous attempts to find the config file fail, fall back on the original method of finding the file
                string MCSDIR = proc.StartInfo.EnvironmentVariables["MCSDIR"];

                if (!string.IsNullOrEmpty(MCSDIR))
                    path = string.Concat(MCSDIR, "\\client\\bin\\");

                if (!File.Exists(string.Concat(path, DefaultXMLHistoryName)))
                    return;
            }
			
			try
			{
				File.Delete(path + DefaultXMLHistoryName);
			}
			catch (IOException)
			{
				MessageBox.Show("File is in use", "IOException", MessageBoxButtons.OK);
			}
			catch (UnauthorizedAccessException)
			{
				MessageBox.Show("You do not have permissions to remove WinMSQL history file.", "Unauthorized Access", MessageBoxButtons.OK);
			}
			catch { }
		}

		private void ctlHistory_Shown(object sender, EventArgs e)
		{
			this.CenterToParent();
		}

		private void lstView_MouseDown(object sender, MouseEventArgs e)
		{
			if (e.Button == MouseButtons.Right)
			{
				contextMenuStrip1.Show(PointToScreen(e.Location));
			}
		}

		private void copyCommandToolStripMenuItem_Click(object sender, EventArgs e)
		{
			if (lstView.SelectedItems.Count > 0)
				Clipboard.SetText(lstView.SelectedItems[0].SubItems[2].Text);
		}

		private void sendToCurrentTabToolStripMenuItem_Click(object sender, EventArgs e)
		{
			SendCmdToCurrentTab();
		}

		private void openInNewTabToolStripMenuItem_Click(object sender, EventArgs e)
		{
			SendToNewTab();
		}

		#endregion Private Methods
	}
}