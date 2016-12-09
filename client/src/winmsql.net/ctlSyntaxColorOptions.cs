using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlSyntaxColorOptions : Form
    {
        #region Constructor

        public ctlSyntaxColorOptions()
        {
            InitializeComponent();
        }

        #endregion Constructor

        #region Public Methods
        /// <summary>
        /// Gets the highlighting colors.
        /// </summary>
        /// <returns>Dictionary with the key type, color combination</returns>
        public Dictionary<string, Color> GetHighlightColors()        
        {
            Dictionary<string, Color> colors = new Dictionary<string, Color>();

            foreach (ListViewItem viewItem in lstColors.Items)
            {
                if (!colors.ContainsKey(viewItem.Text))
                    colors.Add(viewItem.Text, viewItem.SubItems[1].BackColor);
            }

            return colors;
        }
        /// <summary>
        /// Load the colors in the dictionary into the listview
        /// </summary>
        /// <param name="colors">Dictionary of keyword type, color pairs</param>        
        public void LoadColors(IDictionary<string, Color> colors)
        {
            lstColors.Items.Clear();
            foreach (KeyValuePair<string, Color> color in colors)
            {
                String[] args = new String[2] { color.Key, "" };
                lstColors.Items.Add(new ListViewItem(args)).UseItemStyleForSubItems = false;                
            }
            foreach (ListViewItem viewItem in lstColors.Items)
            {
                viewItem.SubItems[1].BackColor = colors[viewItem.Text];
            }
        }
        #endregion Public Methods

        #region Event Handlers
        /// <summary>
        /// Handles the Click event of the btnOK or btnCancel control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void button_Click(object sender, EventArgs e)
        {
            DialogResult = (sender == btnOK) ? DialogResult.OK : DialogResult.Cancel;

            Close();
        }

        /// <summary>
        /// Handles the DoubleClick event of the lstColors control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void lstColors_DoubleClick(object sender, EventArgs e)
        {
            if (lstColors.SelectedItems.Count == 1)
            {
                colorDialog.Color = lstColors.SelectedItems[0].SubItems[1].BackColor;
                DialogResult result = colorDialog.ShowDialog();

                if (result == DialogResult.OK)
                    lstColors.SelectedItems[0].SubItems[1].BackColor = colorDialog.Color;
            }
            

        }


        /// <summary>
        /// Handles the Click event of the btnAdd control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void btnAdd_click(object sender, EventArgs e)
        {
            String[] args = new String[2] {txtTypeName.Text.ToUpper(), ""};
            lstColors.Items.Add(new ListViewItem(args)).UseItemStyleForSubItems = false;
            lstColors.Items[lstColors.Items.Count - 1].Selected = true;

            DialogResult result = colorDialog.ShowDialog();

            if (result == DialogResult.OK)
                lstColors.SelectedItems[0].SubItems[1].BackColor = colorDialog.Color;
            
        }

        /// <summary>
        /// Handles the Click event of the btnRemove control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void btnRemove_Click(object sender, EventArgs e)
        {
            if (lstColors.SelectedItems.Count == 1)
            {
                if (lstColors.SelectedItems[0].Text != "SQL" && lstColors.SelectedItems[0].Text != "STRING" && lstColors.SelectedItems[0].Text != "CUSTOM")
                {
                    lstColors.Items.Remove(lstColors.SelectedItems[0]);
                }
                else
                {
                    MessageBox.Show("Cannot remove default colors.", "Invalid Remove", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                }
            }
        }

        /// <summary>
        /// Handles the textChanged event of the txtTypeName control.
        /// Enables the add button when text is entered.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void txtTypeName_TextChanged(object sender, EventArgs e)
        {
            if (txtTypeName.Text.Length <= 0)
            {
                btnAdd.Enabled = false;
            }
            else
            {
                btnAdd.Enabled = true;
            }
        }

        /// <summary>
        /// Handles the KeyDown(enter) event of the txtTypeName control.
        /// </summary>
        /// <param name="sender">The source of the event.</param>
        /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
        private void txtTypeName_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                String[] args = new String[2] { txtTypeName.Text.ToUpper(), "" };
                lstColors.Items.Add(new ListViewItem(args)).UseItemStyleForSubItems = false;
            }
        }
        #endregion Event Handlers
    }
}
