using System;
using System.Data;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlColumnInfo : Form
    {
        private DataSet ds;

        public ctlColumnInfo(DataSet ds)
        {
            InitializeComponent();
            this.ds = ds;
        }

        private void ctlColumnInfo_Shown(object sender, EventArgs e)
        {
			this.CenterToParent();

            DataTable dt = new DataTable();
            dt.Columns.Add("Field Name");
			dt.Columns.Add("Field Type");
            dt.Columns.Add("Max Defined Width");
            dt.Columns.Add("Max Actual Width");

			try
			{
				foreach (DataColumn dc in ds.Tables[0].Columns)
				{
					dt.Rows.Add(dc.ColumnName,
								dc.DataType.ToString(),
								dc.ExtendedProperties["DefinedWidth"],
								dc.ExtendedProperties["ActualWidth"]);
				}
			}
			catch { }

            gridMst.DataSource = dt;
        }
    }
}