using System;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
    internal class Command
    {
        private int sts;
        private string cmd;
        private int row;
        private string service;
        private DateTime date;

        public Command(int sts, string cmd, int row, string service, DateTime date)
        {
            this.sts = sts;
            this.cmd = cmd;
            this.row = row;
            this.service = service;
            this.date = date;
        }

        public ListViewItem getListViewItem()
        {
            ListViewItem lvi = new ListViewItem();
            lvi.Text = Status.ToString();
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, RowCount.ToString()));
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, CommandText));
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi,Service));
            lvi.SubItems.Add(new ListViewItem.ListViewSubItem(lvi, Date.ToString()));
            return lvi;
        }

        public int Status
        {
            get { return sts; }
            set { sts = value; }
        }

        public string CommandText
        {
            get { return cmd; }
            set { cmd = value; }
        }

        public int RowCount
        {
            get { return row; }
            set { row = value; }
        }


        public string Service
        {
            get { return service; }
            set { service = value; }
        }

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }
    }
}