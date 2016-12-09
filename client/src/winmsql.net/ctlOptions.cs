using Microsoft.Win32;
using System;
using System.Xml;
using System.IO;
using System.Collections;
using System.ComponentModel;
using System.Configuration;
using System.Data;
using System.Drawing;
using System.Windows.Forms;
using System.Collections.Generic;

namespace RedPrairie.MCS.WinMSQL
{
	public partial class ctlOptions : Form
	{
		#region Variables

		public Hashtable Options = new Hashtable();
        public IDictionary<string, Color> HighlightColors = new Dictionary<string, Color>();

		#endregion Variables

        #region Constructor

        public ctlOptions() : this(null, null) { }

        public ctlOptions(Hashtable fieldValues): this(fieldValues, null){}

		public ctlOptions(Hashtable fieldValues, Hashtable colorValues)
		{
			InitializeComponent();
			if (fieldValues != null)
				Options = fieldValues;
            if(colorValues != null)
                HighlightColors = LoadColors(colorValues);
			LoadOptions();
		}

		#endregion Constructor

        #region Private Methods

        private void LoadOptions()
		{
			LoadDefault();
            LoadFieldValues();
		}

		private void LoadFieldValues()
		{
			if (Options.Count <= 0)
				return;

			SetControlValues(groupBox1.Controls);
			SetControlValues(groupBox2.Controls);
			SetControlValues(groupBox3.Controls);
            SetControlValues(groupBox4.Controls);
		}

		private void SetControlValues(System.Windows.Forms.Control.ControlCollection ctls)
		{
			foreach (Control ctl in ctls)
			{
				if (Options.Contains(ctl.Name))
				{
					switch (ctl.Name.Substring(0, 3))
					{
						case "chk":
							((CheckBox)ctl).Checked = Options[ctl.Name].ToString() == "True" ? true : false;
							break;
						case "txt":
							((TextBox)ctl).Text = Options[ctl.Name].ToString();
							break;
						case "cbo":
							((ComboBox)ctl).SelectedIndex = Int32.Parse(Options[ctl.Name].ToString());
							break;
						default:
							break;
					}
				}
			}
		}
        
		private void LoadDefault()
		{
			chkCommentFormat.Checked = false;
			chkBrktNewLine.Checked = true;
			chkPipeNewLine.Checked = true;
			chkCommaNewLine.Checked = true;
			txtColsPerLine.Text = "50";
			chkTrimSrc.Checked = false;
			chkTrimExtra.Checked = true;
			chkCvtLwrCase.Checked = false;
			chkParseString.Checked = false;
            chkWarnMsg.Checked = false;
			chkEnter.Checked = false;
			chkIntegrator.Checked = false;
			chkDebug.Checked = false;
			chkExtTraceViewer.Checked = false;
            chkHighlightSyntax.Checked = false;
            chkHighlightStrings.Checked = false;
            chkHighlightStrings.Enabled = false;
            txtKeywordPath.Text = "";
            lblKeywordPath.Enabled = false;
            txtKeywordPath.Enabled = false;
            btnOpenKeywordFile.Enabled = false;
            
		}
        private static Dictionary<string, Color> LoadColors(IDictionary hashtable)
        {
            var colors = new Dictionary<string, Color>();
            foreach (string key in hashtable.Keys)
            {
                colors.Add(key, (Color)hashtable[key]);
            }

            return colors;
        }
		private void SaveSettings()
		{
			UpdateGroupBoxControls(groupBox1);
			UpdateGroupBoxControls(groupBox2);
			UpdateGroupBoxControls(groupBox3);
            UpdateGroupBoxControls(groupBox4);
		}

		private void UpdateGroupBoxControls(GroupBox grp)
		{
			foreach (Control ctl in grp.Controls)
			{
				if (!Options.ContainsKey(ctl.Name))
					Options.Add(ctl.Name, ""); // Make sure its in the list

				switch (ctl.Name.Substring(0, 3))
				{
					case "chk":
						Options[ctl.Name] = ((CheckBox)ctl).Checked;
						break;
					case "txt":
						Options[ctl.Name] = ((TextBox)ctl).Text;
						break;
					case "cbo":
						Options[ctl.Name] = ((ComboBox)ctl).SelectedIndex;
						break;
					default:
						break;
				}
			}
		}

        private void UpdateColorValues(Dictionary<string, Color> colors)
        {
            HighlightColors.Clear();
            foreach (KeyValuePair<string, Color> color in colors)
            {
                HighlightColors.Add(color.Key, color.Value);               
            }
        }

		#endregion Private methods

		#region Properties

		public bool KeepCommentFormat
		{
			get
			{
				return chkCommentFormat.Checked;
			}
		}

		public bool BracketOnNewLine
		{
			get
			{
				return chkBrktNewLine.Checked;
			}
		}

		public bool PipeOnNewLine
		{
			get
			{
				return chkPipeNewLine.Checked;
			}
		}

		public bool CommaOnNewLine
		{
			get
			{
				return chkCommaNewLine.Checked;
			}
		}

		public int ColumnsPerComment
		{
			get
			{
				return Int32.Parse(txtColsPerLine.Text);
			}
		}

		public bool TrimSource
		{
			get
			{
				return chkTrimSrc.Checked;
			}
		}

		public bool TrimExtraSpaces
		{
			get
			{
				return chkTrimExtra.Checked;
			}
		}

		public bool ConvertToLower
		{
			get
			{
				return chkCvtLwrCase.Checked;
			}
		}

		public bool ParseInStrings
		{
			get
			{
				return chkParseString.Checked;
			}
		}

		public bool EnterNewLine
		{
			get
			{
				return chkEnter.Checked;
			}
		}

		public bool Debug
		{
			get
			{
				return chkDebug.Checked;
			}
		}

		public bool IntegratorBinding
		{
			get
			{
				return chkIntegrator.Checked;
			}
		}

        public bool ExitWarnMsg
        {
            get
            {
                return chkWarnMsg.Checked;
            }
        }

        public bool EnableHighlighting
        {
            get
            {
                return chkHighlightSyntax.Checked;
            }
        }

        public bool HighlightStrings
        {
            get
            {
                return chkHighlightStrings.Checked;
            }
        }

        public string KeywordPath
        {
            get
            {
                return txtKeywordPath.Text;
            }
        }
                       
		#endregion Properties

		#region Event Handlers

		private void cmdCancel_Click(object sender, EventArgs e)
		{
			this.LoadFieldValues(); // Reset any values changed by the user
			this.Hide();
		}

		private void cmdOk_Click(object sender, EventArgs e)
		{
			SaveSettings();
			this.Hide();
		}

		private void cmdDefault_Click(object sender, EventArgs e)
		{
			LoadDefault();
		}

		private void txtColsPerLine_TextChanged(object sender, EventArgs e)
		{
			int num = 0;

			if (!Int32.TryParse(txtColsPerLine.Text, out num))
			{
				MessageBox.Show("Must enter valid integer.", "Invalid entry", MessageBoxButtons.OK);
				txtColsPerLine.Text = "50";
			}
		}

		private void txtIndent_TextChanged(object sender, EventArgs e)
		{
			int num = 0;

			if (!Int32.TryParse(txtColsPerLine.Text, out num))
			{
				MessageBox.Show("Must enter valid integer.", "Invalid entry", MessageBoxButtons.OK);
				txtColsPerLine.Text = "4";
			}
		}

		private void ctlOptions_Shown(object sender, EventArgs e)
		{
			this.CenterToParent();
			this.LoadFieldValues();
		}

		private void chkTraceViewer_CheckedChanged(object sender, EventArgs e)
		{
			if (chkExtTraceViewer.Checked)
			{
				lblExecutable.Enabled = true;
				txtTraceViewer.Enabled = true;
				btnOpenFile.Enabled = true;
				lblArguments.Enabled = true;
				txtArgs.Enabled = true;
			}
			else
			{
				lblExecutable.Enabled = false;
				txtTraceViewer.Enabled = false;
				btnOpenFile.Enabled = false;
				lblArguments.Enabled = false;
				txtArgs.Enabled = false;
			}
		}

        private void chkHighlightSyntax_CheckedChanged(object sender, EventArgs e)
        {
            if (chkHighlightSyntax.Checked)
            {
                lblKeywordPath.Enabled = true;
                txtKeywordPath.Enabled = true;
                btnOpenKeywordFile.Enabled = true;
                chkHighlightStrings.Enabled = true;
            }
            else
            {
                lblKeywordPath.Enabled = false;
                txtKeywordPath.Enabled = false;
                btnOpenKeywordFile.Enabled = false;
                chkHighlightStrings.Enabled = false;
            }
        }

		private void btnOpenFile_Click(object sender, EventArgs e)
		{
			openFileDialog1.Filter = "exe files (*.exe)|*.exe|All files (*.*)|*.*";
			openFileDialog1.ShowDialog();
			txtTraceViewer.Text = openFileDialog1.FileName;
		}

        private void btnOpenKeywordFile_Click(object sender, EventArgs e)
        {
            openFileDialog1.Filter = "txt files (*.txt)|*.txt|All files (*.*)|*.*";
            openFileDialog1.ShowDialog();
            txtKeywordPath.Text = openFileDialog1.FileName;
        }

        private void cmdEditColors_Click(object sender, EventArgs e)
        {
            ctlSyntaxColorOptions colorOptions = new ctlSyntaxColorOptions();

            if (HighlightColors.Count > 0)
                colorOptions.LoadColors(HighlightColors);
            DialogResult results = colorOptions.ShowDialog(this);
            if (results == DialogResult.OK)
            {
                HighlightColors = colorOptions.GetHighlightColors();
            }
        }

		#endregion Event Handlers        
        
	}

}