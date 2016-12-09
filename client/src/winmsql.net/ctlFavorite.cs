using System;
using System.Collections;
using System.Drawing;
using System.IO;
using System.Text;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlFavorite : Form
    {
        #region Private Variables

        private const string DefaultXMLFavoriteName = "WinMSQLCommandFavorite.xml";
        private ctlWinMSQL WinMSQL;
        private ArrayList commandFave;

        private ListViewItem itemDraged = null;
        private ListViewItem itemSelected = null;
        bool isdrag = false;

        #endregion Private Variables

        #region Property

        public ArrayList CommandFave
        {
            get
            {
                return this.commandFave;
            }
        }

        #endregion Property

        #region Constructor

        public ctlFavorite(ArrayList commandFave, ctlWinMSQL WinMSQL)
        {
            InitializeComponent();

            lstView.Columns.Add("Index", 45, HorizontalAlignment.Center);
            lstView.Columns.Add("Command", 480, HorizontalAlignment.Left);

            this.WinMSQL = WinMSQL;
            this.commandFave = commandFave;

            fillFavoriteList();
        }

        #endregion Constructror

        #region Public Methods

        public void addFavorite(string cmd)
        {
            ListViewItem lvi = new ListViewItem();
            lvi.Text = (lstView.Items.Count + 1).ToString();
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, cmd));

            lvi.ToolTipText = cmd;
            lstView.Items.Add(lvi);
            resetListButtons();
        }

        public void fillFavoriteList()
        {
            lstView.Items.Clear();
            foreach (string cmd in commandFave)
            {
                ListViewItem lvi = new ListViewItem();
                lvi.Text = (lstView.Items.Count + 1).ToString();
                lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, cmd));

                lstView.Items.Add(lvi);
            }
            resetListButtons();
        }

        #endregion Public Methods

        #region Private Methods

        private void SendCmdToCurrentTab()
        {
            if (lstView.SelectedItems.Count > 0)
            {
                WinMSQL.currentTab.Command = lstView.SelectedItems[0].SubItems[1].Text;
                WinMSQL.commandIndex = lstView.SelectedItems[0].Index;
                WinMSQL.currentTab.focusCommand();
            }
        }

        private void SendToNewTab()
        {
            if (lstView.SelectedItems.Count <= 0)
                return;

            WinMSQL.addNewTab();
            SendCmdToCurrentTab();

            WinMSQL.currentTab.focusUser();
        }

        private void DeleteCurCmd()
        {
            if (lstView.SelectedItems != null && lstView.SelectedItems.Count > 0)
            {
                lstView.Items.Remove(lstView.SelectedItems[0]);
                resetItemIndex();
            }

            // if the last command is deleted, clear XML file as well
            if (lstView.Items.Count == 0)
                ClearXMLFavorite();

            resetListButtons();
        }

        private void resetItemIndex()
        {
            int index = 1;
            commandFave.Clear();
            foreach (ListViewItem item in lstView.Items)
            {
                item.Text = (index++).ToString();
                commandFave.Add(item.SubItems[1].Text);
            }
        }

        private void resetListButtons()
        {
            if (lstView.Items.Count <= 0)
            {
                btnPrev.Enabled = false;
                btnPost.Enabled = false;
                cmdDelete.Enabled = false;
                cmdClear.Enabled = false;
                cmsLstMenu.Enabled = false;
            }
            else
            {
                btnPrev.Enabled = true;
                btnPost.Enabled = true;
                cmdDelete.Enabled = true;
                cmdClear.Enabled = true;
                cmsLstMenu.Enabled = true;
            }
        }

        private void ClearXMLFavorite()
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

            if (!File.Exists(string.Concat(path, DefaultXMLFavoriteName)))
            {
                // If all previous attempts to find the config file fail, fall back on the original method of finding the file
                string MCSDIR = proc.StartInfo.EnvironmentVariables["MCSDIR"];

                if (!string.IsNullOrEmpty(MCSDIR))
                    path = string.Concat(MCSDIR, "\\client\\bin\\");

                if (!File.Exists(string.Concat(path, DefaultXMLFavoriteName)))
                    return;
            }

            try
            {
                File.Delete(path + DefaultXMLFavoriteName);
            }
            catch (IOException)
            {
                MessageBox.Show("File is in use", "IOException", MessageBoxButtons.OK);
            }
            catch (UnauthorizedAccessException)
            {
                MessageBox.Show("You do not have permissions to remove WinMSQL favorite file.", "Unauthorized Access", MessageBoxButtons.OK);
            }
            catch { }
        }

        #endregion Private Methods

        #region Events  

        private void lstView_ItemDrag(object sender, ItemDragEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                itemDraged = (ListViewItem)e.Item;
                this.Cursor = Cursors.Hand;
                isdrag = true;
            }
        }

        private void lstView_ItemMouseHover(object sender, ListViewItemMouseHoverEventArgs e)
        {
            Graphics g = lstView.CreateGraphics();
            itemSelected = e.Item;
            if (isdrag)
            {
                g.DrawLine(new Pen(Brushes.Black, 2),
                           new Point(itemSelected.Bounds.X, itemSelected.Bounds.Y),
                           new Point(itemSelected.Bounds.X + lstView.Bounds.Width, itemSelected.Bounds.Y));
                g.FillPolygon(Brushes.Black,
                              new Point[] {new Point(itemSelected.Bounds.X, itemSelected.Bounds.Y - 5),
                                           new Point(itemSelected.Bounds.X + 5, itemSelected.Bounds.Y), 
                                           new Point(itemSelected.Bounds.X, itemSelected.Bounds.Y + 5)});
                g.FillPolygon(Brushes.Black,
                              new Point[] {new Point(lstView.Bounds.Width - 4, itemSelected.Bounds.Y - 5),
                                           new Point(lstView.Bounds.Width - 9, itemSelected.Bounds.Y), 
                                           new Point(lstView.Bounds.Width - 4, itemSelected.Bounds.Y + 5)});
            }
        }

        private void lstView_MouseUp(object sender, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                isdrag = false;
                if ((itemSelected != null) && (itemDraged != null))
                {
                    if (itemDraged.Index != itemSelected.Index)
                    {
                        this.lstView.Items.RemoveAt(itemDraged.Index);
                        this.lstView.Items.Insert(itemSelected.Index, itemDraged);

                        resetItemIndex();
                        itemDraged.Selected = true;
                        itemDraged = null;
                        itemSelected = null;
                    }
                }
                this.Cursor = Cursors.Default;
            }
            else if (e.Button == MouseButtons.Right)
            {
                cmsLstMenu.Show(PointToScreen(e.Location));
            }
        }

        private void ctlFavorite_Shown(object sender, EventArgs e)
        {
            this.CenterToParent();
        }

        private void btnPrev_Click(object sender, EventArgs e)
        {
            if (lstView.SelectedItems == null || lstView.SelectedItems.Count == 0)
                return;

            ListViewItem item = (ListViewItem)lstView.SelectedItems[0].Clone();
            int index = lstView.SelectedItems[0].Index;

            if (index > 0)
            {
                lstView.Items.RemoveAt(index);
                lstView.Items.Insert(index - 1, item);

                resetItemIndex();
                lstView.Items[index - 1].Selected = true;
            }
        }

        private void btnPost_Click(object sender, EventArgs e)
        {
            if (lstView.SelectedItems == null || lstView.SelectedItems.Count == 0)
                return;

            ListViewItem item = (ListViewItem)lstView.SelectedItems[0].Clone();
            int index = lstView.SelectedItems[0].Index;

            if (index < lstView.Items.Count - 1)
            {
                lstView.Items.RemoveAt(index);
                lstView.Items.Insert(index + 1, item);

                resetItemIndex();
                lstView.Items[index + 1].Selected = true;
            }
        }

        private void cmdDelete_Click(object sender, EventArgs e)
        {
            DeleteCurCmd();
        }

        private void cmdCancel_Click(object sender, EventArgs e)
        {
            Hide();
        }

        private void cmdClear_Click(object sender, EventArgs e)
        {
            lstView.Items.Clear();
            commandFave.Clear();
            ClearXMLFavorite();
            resetListButtons();
        }

        private void lstView_DoubleClick(object sender, EventArgs e)
        {
            SendCmdToCurrentTab();
        }

        private void cmsLstMenu_Opening(object sender, System.ComponentModel.CancelEventArgs e)
        {
            if (lstView.SelectedItems == null || lstView.SelectedItems.Count == 0)
                cmsLstMenu.Enabled = false;
            else
                cmsLstMenu.Enabled = true;
        }

        private void tsmiCopyCmd_Click(object sender, EventArgs e)
        {
            if (lstView.SelectedItems.Count > 0)
                Clipboard.SetText(lstView.SelectedItems[0].SubItems[1].Text);
        }

        private void tsmiDeleteCmd_Click(object sender, EventArgs e)
        {
            DeleteCurCmd();
        }

        private void tsmiSendToCur_Click(object sender, EventArgs e)
        {
            SendCmdToCurrentTab();
        }

        private void tsmiOpenInNew_Click(object sender, EventArgs e)
        {
            SendToNewTab();
        }

        #endregion Events
    }
}
