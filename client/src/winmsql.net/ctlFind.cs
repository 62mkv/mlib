using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
	public partial class ctlFind : Form
	{
		#region Private Variables

		private Point LookinLoc;
		private Point FindOptsLoc;
		private Point FindNextLoc;
		private int DefHeight;

		#endregion Private Variables

		#region Public Variables

		public Queue<string> FindHistory = new Queue<string>(20);
		public Queue<string> ReplaceHistory = new Queue<string>(20);		 

		#endregion Public Variables

		#region Delegates/Events

		public delegate void FindHandler(object sender, FindEventArgs args);
		public event FindHandler Find;

		#endregion Delegates/Events

		#region Constructor

		public ctlFind() : this(false){}

		public ctlFind(bool replace) : this(replace, "") { }

		public ctlFind(string defText) : this(false, defText) { }

		public ctlFind(bool replace, string defText)
		{
			InitializeComponent();

			LookinLoc = grpbxLookIn.Location;
			FindOptsLoc = grpbxFindOpts.Location;
			FindNextLoc = btnFindNext.Location;
			DefHeight = this.Height;

			if (!string.IsNullOrEmpty(defText))
				cboFind.Text = defText;
			LayoutForm(replace);		
		}

		#endregion Constructor

		#region Private Methods

		private void LayoutForm(bool replace)
		{
			if (replace)
			{
				lblReplaceWith.Enabled = true;
				lblReplaceWith.Visible = true;
				cboReplace.Enabled = chkCmdTxt.Checked;
				cboReplace.Visible = true;

				btnReplace.Enabled = chkCmdTxt.Checked;
				btnReplace.Visible = true;
				btnReplaceAll.Enabled = chkCmdTxt.Checked;
				btnReplaceAll.Visible = true;

				grpbxLookIn.Location = LookinLoc;
				grpbxFindOpts.Location = FindOptsLoc;
				btnFindNext.Location = new Point(this.Width - 265, FindNextLoc.Y);

				this.Height = DefHeight;
			}
			else
			{
				lblReplaceWith.Enabled = false;
				lblReplaceWith.Visible = false;
				cboReplace.Enabled = chkCmdTxt.Checked;
				cboReplace.Visible = false;

				btnReplace.Enabled = chkCmdTxt.Checked;
				btnReplace.Visible = false;
				btnReplaceAll.Enabled = chkCmdTxt.Checked;
				btnReplaceAll.Visible = false;

				grpbxLookIn.Location = new Point(LookinLoc.X, LookinLoc.Y - 43);
				grpbxFindOpts.Location = new Point(FindOptsLoc.X, FindOptsLoc.Y - 43);
				btnFindNext.Location = new Point(this.Width - 145, FindNextLoc.Y - 43);

				this.Height = DefHeight - 72;
			}
		}

		#endregion Private Methods

		#region Public Methods

		public void FillHistoryCombos(Queue<string> findHist, Queue<string> replaceHist)
		{
			if (findHist != null)
				FindHistory = findHist;
			if (replaceHist != null)
				ReplaceHistory = replaceHist;

			foreach (string str in FindHistory.ToArray())
				cboFind.Items.Insert(0, str);

			foreach (string str in ReplaceHistory.ToArray())
				cboReplace.Items.Insert(0, str);
		}

		#endregion Public Methods

		#region Event Handlers

		private void btnQuickFind_Click(object sender, EventArgs e)
		{
			LayoutForm(false);
		}

		private void btnQuickReplace_Click(object sender, EventArgs e)
		{
			LayoutForm(true);
		}

		private void btnFindNext_Click(object sender, EventArgs e)
		{
			if (cboFind.Text.Length <= 0)
				return;

			if (!cboFind.Items.Contains(cboFind.Text))
			{
				cboFind.Items.Insert(0, cboFind.Text);
				if (cboFind.Items.Count > 20)
					cboFind.Items.RemoveAt(20);

				FindHistory.Enqueue(cboFind.Text);
				if (FindHistory.Count > 20)
					FindHistory.Dequeue();
			}

			Find(this, new FindEventArgs(cboFind.Text, cboReplace.Text, chkCmdTxt.Checked,
				chkMatchCase.Checked, chkMatchWord.Checked, false, false));
		}

		private void btnReplace_Click(object sender, EventArgs e)
		{
			if (cboReplace.Text.Length <= 0)
				return;

			if (!cboReplace.Items.Contains(cboReplace.Text))
			{
				cboReplace.Items.Insert(0, cboReplace.Text);
				if (cboReplace.Items.Count > 20)
					cboReplace.Items.RemoveAt(20);

				ReplaceHistory.Enqueue(cboReplace.Text);
				if (ReplaceHistory.Count > 20)
					ReplaceHistory.Dequeue();
			}

			Find(this, new FindEventArgs(cboFind.Text, cboReplace.Text, chkCmdTxt.Checked,
				chkMatchCase.Checked, chkMatchWord.Checked, true, false));
		}

		private void btnReplaceAll_Click(object sender, EventArgs e)
		{
			if (cboReplace.Text.Length <= 0)
				return;

			if (!cboReplace.Items.Contains(cboReplace.Text))
			{
				cboReplace.Items.Insert(0, cboReplace.Text);
				if (cboReplace.Items.Count > 20)
					cboReplace.Items.RemoveAt(20);

				ReplaceHistory.Enqueue(cboReplace.Text);
				if (ReplaceHistory.Count > 20)
					ReplaceHistory.Dequeue();
			}

			Find(this, new FindEventArgs(cboFind.Text, cboReplace.Text, chkCmdTxt.Checked,
				chkMatchCase.Checked, chkMatchWord.Checked, false, true));
		}

		private void cboFind_TextChanged(object sender, EventArgs e)
		{
			if (cboFind.Text.Length <= 0)
			{
				btnFindNext.Enabled = false;
				btnReplace.Enabled = false;
				btnReplaceAll.Enabled = false;
			}
			else
			{
				btnFindNext.Enabled = true;
				btnReplace.Enabled = true;
				btnReplaceAll.Enabled = true;
			}
		}

		private void ctlFind_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.KeyCode == Keys.Enter)
			{
				btnFindNext_Click(this, null);
			}
			else if (e.KeyCode == Keys.Escape)
			{
				this.Close();
			}
		}

		private void cboFind_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.KeyCode == Keys.Enter)
			{
				btnFindNext_Click(this, null);
			}
		}

		private void cboReplace_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.KeyCode == Keys.Enter)
			{
				btnReplace_Click(this, null);
			}
		}

		private void chkCmdTxt_CheckedChanged(object sender, EventArgs e)
		{
			btnReplace.Enabled = chkCmdTxt.Checked;
			btnReplaceAll.Enabled = chkCmdTxt.Checked;
			cboReplace.Enabled = chkCmdTxt.Checked;
		}

		#endregion Event Handlers
	}

	#region Event Argument Classes

	public class FindEventArgs : EventArgs
	{
		#region Variables

		string findText;
		string replaceText;
		bool searchCmdText;
		bool matchCase;
		bool matchWholeWord;
		bool replace;
		bool replaceAll;

		#endregion Variables

		#region Constructor

		public FindEventArgs(string findText, string replaceText, bool searchCmdText,
							 bool matchCase, bool matchWholeWord, bool replace, bool replaceAll)
		{
			this.findText = findText;
			this.replaceText = replaceText;
			this.searchCmdText = searchCmdText;
			this.matchCase = matchCase;
			this.matchWholeWord = matchWholeWord;
			this.replace = replace;
			this.replaceAll = replaceAll;
		}

		#endregion Constructor

		#region Properties

		public string FindText
		{
			get
			{
				if (!string.IsNullOrEmpty(findText))
					return findText;
				else
					return "";
			}
		}

		public string ReplaceText
		{
			get
			{
				if (!string.IsNullOrEmpty(replaceText))
					return replaceText;
				else
					return "";
			}
		}

		public bool LookInCommandText
		{
			get { return searchCmdText; }
		}

		public bool MatchCase
		{
			get { return matchCase; }
		}

		public bool MatchWholeWord
		{
			get { return matchWholeWord; }
		}

		public bool Replace
		{
			get { return replace; }
		}

		public bool ReplaceAll
		{
			get { return replaceAll; }
		}

		#endregion Properties
	}

	#endregion Event Argument Classes
}