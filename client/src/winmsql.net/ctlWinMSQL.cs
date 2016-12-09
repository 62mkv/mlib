using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Xml;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Xml.Linq;
using Infragistics.Win;
using Infragistics.Win.UltraWinTabControl;
using RedPrairie.MOCA.Client;
using DevExpress.XtraGrid;
using System.Text.RegularExpressions;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class ctlWinMSQL : Form
	{
		#region Constants

		private const string DefaultXMLHistoryName = "WinMSQLCommandHistory.xml";
        private const string DefaultXMLFavoriteName = "WinMSQLCommandFavorite.xml";
        private const string FAVORITE = "Favorite";
		private const string HISTORY = "History";
		private const string COMMAND = "Command";
		private const string TEXT = "Text";
		private const string ROWS = "Rows";
		private const string STATUS = "Status";
        private const string SERVICE = "Service";
        private const string DATE = "Date";
		private const string DefaultXMLConfigFileName = "WinMSQLConfig.xml";

		#endregion Constants

        #region Private Variables

        private readonly ArrayList commandHist = new ArrayList();
        private readonly ArrayList commandFave = new ArrayList();
        private ctlHistory historyDlg;
        private ctlFavorite faveDlg;
        private FileDialog dlg;
		//private LoginInfo lastGoodLogin = new LoginInfo();
        private ServerInfo lastGoodLogin = new ServerInfo("", "", "");
        private ServerCache ServerCache = new ServerCache();
        private readonly Hashtable configOptions = new Hashtable();
        private readonly Hashtable colorOptions = new Hashtable();
		private readonly Hashtable traceOptions = new Hashtable();
		private readonly ctlOptions options;
        private static Regex tokenizer = new Regex(@"(?<=[\(\[\s\=\|\]\)])");
		private ctlFind find;
		private Queue<string> FindHist;
		private Queue<string> ReplaceHist;

        private const string APP_HOST_ARG = "-A";
        private const string APP_PORT_ARG = "-P";
        private const string APP_ENV_ARG = "-E";
        private string hostArg = "";
        private string portArg = "";
        private string envArg = "";
		private int previousIndex;
		private float _fontSize;
        private bool isFindMatch = false;
        private string matchValue = string.Empty;
        private Dictionary<string, string> commands = new Dictionary<string, string>();

		#endregion Private Variables

        #region Public Variables

        public int commandIndex = -1;
        public List<string> listUpDownHist = new List<string>(0);
        [DllImport("user32.dll")]
        public static extern bool LockWindowUpdate(IntPtr hWndLock);

		#endregion Public Variables

        #region Constructors

        public ctlWinMSQL(): this(null) { }

        public ctlWinMSQL(string[] args)
        {
            InitializeComponent();

			ReadXMLOptions();

			options = new ctlOptions(configOptions, colorOptions);
			((winMSQLTab)tabDashBoard.Tabs[0].TabPage.Controls[0]).TraceOptions = traceOptions;
            ((winMSQLTab)tabDashBoard.Tabs[0].TabPage.Controls[0]).ConfigOptions = options.Options;
            if (options.HighlightColors.Count == 0)
            {
                options.HighlightColors = new ctlSyntaxColorOptions().GetHighlightColors();
            }
            ((winMSQLTab)tabDashBoard.Tabs[0].TabPage.Controls[0]).ColorOptions = options.HighlightColors;
			
			if (args != null)
				parseCommandLineArgs(args);

			// Default configuration
			lastGoodLogin.Host = "localhost";
			lastGoodLogin.Port = 4500;
			lastGoodLogin.Environment = "LOCALE_ID=US_ENGLISH";

            ReadXMLHistory();
            ReadXMLFavorite();

            tabDashBoard.SelectedTab = tabDashBoard.Tabs[0];
			_fontSize = currentTab.txtCmd.Font.Size;

            LoadSyntaxKeywords();
            
		}

		#endregion Constructors

		#region Properties

        public winMSQLTab currentTab
        {
			get
			{
				return (winMSQLTab)tabDashBoard.SelectedTab.TabPage.Controls[0];
			}
        }

		#endregion Properties

		#region Worker Methods

        public void addNewTab()
        {
            UltraTab newTab = tabDashBoard.Tabs.Add();

            newTab.TabPage.Controls.Add(new winMSQLTab(ServerCache, lastGoodLogin));
            newTab.TabPage.Controls[0].Dock = DockStyle.Fill;
            (newTab.TabPage.Controls[0]).AutoSize = true;
            ((winMSQLTab)newTab.TabPage.Controls[0]).AutoSizeMode = AutoSizeMode.GrowAndShrink;
            newTab.Text = "Not Connected";
            ((winMSQLTab)newTab.TabPage.Controls[0]).commandKeyDown += txtCmd_KeyDown;
            ((winMSQLTab)newTab.TabPage.Controls[0]).Connected += winMSQLTab1_Connected;
            ((winMSQLTab)newTab.TabPage.Controls[0]).CommandExecuted += ctlWinMSQL_CommandExecuted;
            ((winMSQLTab)newTab.TabPage.Controls[0]).textChanged += txtCmd_TextChanged;
            tabDashBoard.SuspendLayout();
            tabDashBoard.Tabs.RemoveAt(tabDashBoard.Tabs.IndexOf("tabBlank"));
            tabDashBoard.Tabs.Add("tabBlank", "");
            tabDashBoard.SelectedTab = newTab;
            tabDashBoard.ResumeLayout();
			currentTab.focusService();
			currentTab.setFontSize(_fontSize); // sets font to match other tabs
			currentTab.TraceOptions = traceOptions;
			currentTab.ConfigOptions = options.Options;
            currentTab.TabPage = newTab;
            currentTab.UpdateTabColor();
        }

        /// <summary>
        /// Clear the Ctrl-Up/Down history and default to not use match find
        /// </summary>
        public void ResetUpDownHistory()
        {
            listUpDownHist.Clear();
            isFindMatch = false;
            matchValue = string.Empty;
        }

		private void parseCommandLineArgs(string[] args)
        {
            int count = 0;
            // arguments may be in form of '-Alocalhost' or '-A localhost'
            foreach (string arg in args)
            {
                if (arg.Trim() == APP_HOST_ARG)
                    hostArg = args[count + 1];
                else if (arg.Trim() == APP_PORT_ARG)
                    portArg = args[count + 1];
                else if (arg.Trim() == APP_ENV_ARG)
                    envArg = args[count + 1];
                else if (arg.Length > 2)
                {
                    string tmp = arg.Substring(0, 2);

                    if (tmp == APP_HOST_ARG)
                        hostArg = arg.Substring(2, arg.Length - 2);
                    else if (tmp == APP_PORT_ARG)
                        portArg = arg.Substring(2, arg.Length - 2);
                    else if (tmp == APP_ENV_ARG)
                        envArg = arg.Substring(2, arg.Length - 2);
                }

                count++;
            }
        }

		private void WriteConfigOptions()
		{
			Hashtable tbl = options.Options;
            var color = options.HighlightColors;
            if (tbl.Count > 0)
            {
                XmlWriter writer = null;
                try
                {
                    writer = GetXMLWriter(DefaultXMLConfigFileName);
                    if (writer != null)
                    {
                        writer.WriteStartDocument();
                        writer.WriteStartElement("ConfigSettings");

                        WriteToFile(tbl, writer, "Options");
                        WriteColorsToFile(color, writer, "Colors");
                        tbl = ((winMSQLTab)tabDashBoard.Tabs[0].TabPage.Controls[0]).TraceOptions;
                        WriteToFile(tbl, writer, "TraceOptions");

                        writer.WriteEndElement(); // end ConfigSettings				
                        writer.WriteEndDocument();
                    }
                }
                catch (Exception ex)
                {
                    if (options.ExitWarnMsg)
                        MessageBox.Show(ex.Message, "Error writing config file", MessageBoxButtons.OK);
                }
                finally
                {
                    if (writer != null)
                    {
                        writer.Close();
                    }
                }
            }
		}

		private static void WriteToFile(IDictionary tbl, XmlWriter writer, string element)
		{
		    writer.WriteStartElement(element);

			if (tbl.Keys.Count <= 0)
			{
				// Need an empty element so we know there are no options when reading
				writer.WriteStartElement("Item");
				writer.WriteEndElement();
			}

			foreach (string ctlName in tbl.Keys)
			{
				if (!string.IsNullOrEmpty(ctlName.Substring(0, 3)))
				{
					bool write = false;
					switch (ctlName.Substring(0, 3))
					{
						case "chk": write = true;
							break;
						case "cbo": write = true;
							break;
						case "txt": write = true;
							break;
						default:
							break;
					}

					if (write)
					{
						writer.WriteStartElement("Item");
						writer.WriteAttributeString("name", ctlName);
						writer.WriteAttributeString("value", tbl[ctlName].ToString());
						writer.WriteEndElement();
					}
				}
			}

			writer.WriteEndElement();
		}

        private static void WriteColorsToFile(IDictionary<string, Color> tbl, XmlWriter writer, string element)
        {
            writer.WriteStartElement(element);

            if (tbl.Keys.Count <= 0)
            {
                // Need an empty element so we know there are no options when reading
                writer.WriteStartElement("Item");
                writer.WriteEndElement();
            }

            foreach (string keyType in tbl.Keys)
            {
                if (!string.IsNullOrEmpty(keyType))
                {
                        writer.WriteStartElement("Item");
                        writer.WriteAttributeString("name", keyType);
                        Color color = tbl[keyType];
                        writer.WriteAttributeString("value", color.ToArgb().ToString());
                        writer.WriteEndElement();
                    
                }
            }

            writer.WriteEndElement();
        }

		private void WriteXMLHistory()
		{
            if (commandHist.Count > 0)
            {
                XmlWriter xml = null;

                try
                {
                    xml = GetXMLWriter(DefaultXMLHistoryName);
                    if (xml != null)
                    {
                        xml.WriteStartDocument();
                        xml.WriteStartElement(HISTORY);

                        int count = 0;
                        foreach (Command cmd in commandHist)
                        {
                            // Limit the number of commands to be saved
                            if (count > 150)
                                break;

                            count++;
                            xml.WriteStartElement(COMMAND);
                            xml.WriteAttributeString(TEXT, cmd.CommandText);
                            xml.WriteAttributeString(STATUS, cmd.Status.ToString());
                            xml.WriteAttributeString(ROWS, cmd.RowCount.ToString());
                            xml.WriteAttributeString(SERVICE, cmd.Service);
                            xml.WriteAttributeString(DATE, cmd.Date.ToString());
                            xml.WriteEndElement();
                        }

                        xml.WriteEndElement();
                        xml.WriteEndDocument();
                    }
                }
                catch (Exception ex)
                {
                    if (options.ExitWarnMsg)
                        MessageBox.Show(ex.Message, "Error writing command history.", MessageBoxButtons.OK);
                }
                finally
                {
                    if (xml != null)
                        xml.Close();
                }
            }
		}

        private void WriteXMLFavorite()
        {
            if (commandFave.Count > 0)
            {
                XmlWriter xml = null;
                try
                {
                    xml = GetXMLWriter(DefaultXMLFavoriteName);
                    if (xml != null)
                    {
                        xml.WriteStartDocument();
                        xml.WriteStartElement(FAVORITE);

                        int count = 0;
                        foreach (string cmd in commandFave)
                        {
                            // Limit the number of commands to be saved
                            if (count > 150)
                                break;

                            count++;
                            xml.WriteStartElement(COMMAND);
                            xml.WriteAttributeString(TEXT, cmd);
                            xml.WriteEndElement();
                        }

                        xml.WriteEndElement();
                        xml.WriteEndDocument();
                    }
                }
                catch (Exception ex)
                {
                    if (options.ExitWarnMsg)
                        MessageBox.Show(ex.Message, "Error writing command favorite.", MessageBoxButtons.OK);
                }
                finally
                {
                    if (xml != null)
                        xml.Close();
                }
            }
        }

		private void ReadXMLOptions()
		{
            XmlReader read = GetXMLReader(DefaultXMLConfigFileName);

            if (read != null)
            {
                

                try
                {
                    var element = XElement.Load(read);
                    XElement settings;
                    if (TryGetElement(element, "ConfigSettings", out settings))
                    {
                        ReadFromFile(settings, configOptions, "Options");
                        ReadColorsFromFile(settings, colorOptions, "Colors");
                        ReadFromFile(settings, traceOptions, "TraceOptions");
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "Error reading xml command history file.", MessageBoxButtons.OK);
                }
                finally
                {
                    read.Close();
                }
            }
		}

        private static bool TryGetElement(XElement parent, XName name, out XElement child)
        {
            child = parent.DescendantsAndSelf(name).FirstOrDefault();
            return (child != null);
        }

		private static void ReadFromFile(XElement parent, IDictionary tbl, string element)
		{
		    ReadFromFile(parent, tbl, element, null);
		}

        private static void ReadFromFile(XElement parent, IDictionary tbl, string element, Func<string, object> converter)
        {
            XElement section;
            if (!TryGetElement(parent, element, out section))
                return;
            
            foreach (var descendant in section.Descendants("Item"))
            {
                var name = GetAttributeValue(descendant, "name");
                var value = GetAttributeValue(descendant, "value");

                if (String.IsNullOrEmpty(name) || String.IsNullOrEmpty(value)) 
                    continue;

                object finalValue = value;
                if (converter != null)
                {
                    finalValue = converter(value);

                    if (finalValue == null)
                        continue;
                }

                tbl.Add(name, finalValue);
            }
        }

        /// <summary>
        /// Ges the attribute value.
        /// </summary>
        /// <param name="descendant">The descendant.</param>
        /// <param name="name">The name.</param>
        /// <returns></returns>
        private static string GetAttributeValue(XElement descendant, string name)
        {
            var attribute = descendant.Attribute(name);
            return (attribute != null) ? attribute.Value : null;
        }

        private static void ReadColorsFromFile(XElement parent, IDictionary tbl, string element)
        {
            var converter = new Func<string, object>(value =>
                                                         {
                                                             int intValue;
                                                             if (!Int32.TryParse(value, out intValue))
                                                                 return null;

                                                             return Color.FromArgb(intValue);
                                                         } );

            ReadFromFile(parent, tbl, element, converter);
        }

		private void ReadXMLHistory()
		{
            XmlReader read = GetXMLReader(DefaultXMLHistoryName);

            if (read != null)
            {
                try
                {
                    read.ReadStartElement(HISTORY);
                    read.ReadToNextSibling(COMMAND);

                    do
                    {
                        read.MoveToAttribute(TEXT);
                        string cmdText = read.ReadContentAsString();
                        read.MoveToAttribute(STATUS);
                        int cmdStatus = read.ReadContentAsInt();
                        read.MoveToAttribute(ROWS);
                        int cmdRows = read.ReadContentAsInt();
                        read.MoveToAttribute(SERVICE);
                        string cmdService = read.ReadContentAsString();
                        read.MoveToAttribute(DATE);

                        // The old history file doesn't contain date/time info,
                        // so it would throw a FormatException.
                        // In this case, we use the current date/time instead.
                        DateTime cmdDate;
                        try
                        {
                            cmdDate = DateTime.Parse(read.ReadContentAsString());
                        }
                        catch
                        {
                            cmdDate = DateTime.Now;
                        }

                        commandHist.Add(new Command(cmdStatus, cmdText, cmdRows, cmdService, cmdDate));
                    } while (read.ReadToNextSibling(COMMAND));
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "Error reading xml command history file.", MessageBoxButtons.OK);
                }
                finally
                {
                    read.Close();
                }
            }
		}

        private void ReadXMLFavorite()
        {
            XmlReader read = GetXMLReader(DefaultXMLFavoriteName);

            if (read != null)
            {
                try
                {
                    read.ReadStartElement(FAVORITE);
                    read.ReadToNextSibling(COMMAND);

                    do
                    {
                        read.MoveToAttribute(TEXT);
                        string cmdText = read.ReadContentAsString();

                        commandFave.Add(cmdText);
                    } while (read.ReadToNextSibling(COMMAND));
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, "Error reading xml command favorite file.", MessageBoxButtons.OK);
                }
                finally
                {
                    read.Close();
                }
            }
        }

        private static XmlReader GetXMLReader(string filename)
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

            if (!File.Exists(string.Concat(path, filename)))
            {
                string MCSDIR = proc.StartInfo.EnvironmentVariables["MCSDIR"];

                if (!string.IsNullOrEmpty(MCSDIR))
                {
                    path = MCSDIR + "\\client\\bin\\";
                }

                if (!File.Exists(string.Concat(path, filename)))
                {
                    return null;
                }
            }

            try
            {
                return XmlReader.Create(string.Concat(path, filename));
            }
            catch
            {
                return null;
            }
        }

        private static XmlWriter GetXMLWriter(string filename)
        {
            string path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);

            if (!string.IsNullOrEmpty(path))
            {
                path = string.Concat(path, "\\RedPrairie\\WinMSQL\\");
                if (!Directory.Exists(path))
                {
                    Directory.CreateDirectory(path);
                }
            }
            else
            {
                path = Directory.GetCurrentDirectory();
                path = string.Concat(path, "\\");
            }

            XmlWriterSettings settings = new XmlWriterSettings { Indent = true, IndentChars = ("    ") };
            return XmlWriter.Create(path + filename, settings);
        }

		private void ShowOptionsDialog()
		{
			options.ShowDialog();

			foreach(UltraTab tab in tabDashBoard.Tabs)
			{
				if (tab.TabPage.Controls.Count > 0)
				{
                    winMSQLTab winTab = (winMSQLTab)tab.TabPage.Controls[0];
					winTab.ConfigOptions = options.Options;
                    winTab.ColorOptions = options.HighlightColors;
				}
			}
            //reload keyword list
            LoadSyntaxKeywords();
            UpdateSyntaxHighlighting();
		}

		private void ShowFindDialog(bool replace)
		{
			if (find == null || find.IsDisposed)
			{
				find = new ctlFind(replace, currentTab.txtCmd.SelectedText);
				find.Find += find_Find;
				find.FormClosing += find_FormClosing;
				find.FillHistoryCombos(FindHist, ReplaceHist);
			}

			find.Show();
			find.BringToFront();
		}

        /// <summary>
        /// Set match find mode
        /// </summary>
        /// <param name="matchCommand">The match command.</param>
        private void SetMatchFindMode(string matchCommand)
        {
            commandIndex = -1;
            isFindMatch = true;
            matchValue = matchCommand;
        }

        /// <summary>
        /// Set the display command based on the history trace
        /// </summary>
        /// <param name="isCtrlUp">Is Ctrl-Up or Down?</param>
        private void SetCommandBasedOnTrace(bool isCtrlUp)
        {
            // Search from the trace of the current command
            int upDownHistIndex = commandIndex > -1 ? listUpDownHist.IndexOf(((Command)commandHist[commandIndex]).CommandText) : -1;
            // Set next command
            if (isCtrlUp)
            {
                SetCommandByCtrlUp(upDownHistIndex);
            }
            else
            {
                SetCommandByCtrlDown(upDownHistIndex);
            }
        }

        /// <summary>
        /// Set the display command after Ctrl-Up
        /// </summary>
        /// <param name="upDownHistIndex">The index of the current command in the Up/Down trace list.</param>
        private void SetCommandByCtrlUp(int upDownHistIndex)
        {
            if (upDownHistIndex > -1 && upDownHistIndex < listUpDownHist.Count - 1)
            {
                // When the next command in the trace is empty, it means there's no unique command after the current command.
                if (!string.IsNullOrEmpty(listUpDownHist[upDownHistIndex + 1]))
                {
                    currentTab.Command = listUpDownHist[upDownHistIndex + 1];
                    // Need to sync the commandIndex
                    while (commandIndex < commandHist.Count - 1 &&
                        !currentTab.Command.Equals(((Command)commandHist[commandIndex + 1]).CommandText))
                    {
                        commandIndex++;
                    }
                    commandIndex++;
                }
            }
            else
            {
                // Not exist in the trace, need to find the next unique command
                // If is find match, then we need to use StartsWith to get the command
                SetNextUniqueCommand(true);
            }
        }

        /// <summary>
        /// Set the display command after Ctrl-Down
        /// </summary>
        /// <param name="upDownHistIndex">The index of the current command in the Up/Down trace list.</param>
        private void SetCommandByCtrlDown(int upDownHistIndex)
        {
            if (upDownHistIndex > 0)
            {
                // When the next command in the trace is empty, it means there's no unique command after the current command.
                if (!string.IsNullOrEmpty(listUpDownHist[upDownHistIndex - 1]))
                {
                    currentTab.Command = listUpDownHist[upDownHistIndex - 1];
                    // Need to sync the commandIndex
                    while (commandIndex > 0 &&
                        !currentTab.Command.Equals(((Command)commandHist[commandIndex - 1]).CommandText))
                    {
                        commandIndex--;
                    }
                    commandIndex--;
                }
            }
            else
            {
                // Not exist in the trace, need to find the next unique command
                // If is find match, then we need to use StartsWith to get the command
                SetNextUniqueCommand(false);
            }
        }

        /// <summary>
        /// Set next display unique command.
        /// </summary>
        /// <param name="isCtrlUp">Is Ctrl-Up or Down?</param>
        private void SetNextUniqueCommand(bool isCtrlUp)
        {
            int tmpIndex = commandIndex;
            if (isCtrlUp)
            {
                while (tmpIndex < commandHist.Count - 1 &&
                    ((isFindMatch && !((Command)commandHist[tmpIndex + 1]).CommandText.StartsWith(matchValue))
                    || listUpDownHist.Contains(((Command)commandHist[tmpIndex + 1]).CommandText)))
                {
                    tmpIndex++;
                }
            }
            else
            {
                while (tmpIndex > 0 &&
                    ((isFindMatch && !((Command)commandHist[tmpIndex - 1]).CommandText.StartsWith(matchValue))
                    || listUpDownHist.Contains(((Command)commandHist[tmpIndex - 1]).CommandText)))
                {
                    tmpIndex--;
                }
            }
            SetCommandForIndex(tmpIndex, isCtrlUp);
        }

        /// <summary>
        /// Set display command for the specific index
        /// </summary>
        /// <param name="index">The current command index.</param>
        /// <param name="isCtrlUp">Is Ctrl-Up or Down?</param>
        private void SetCommandForIndex(int index, bool isCtrlUp)
        {
            // If has unique command in the Up/Down direction, then need to add to trace.
            // Otherwise, no need to update commandIndex.
            if ((isCtrlUp && index < commandHist.Count - 1) || (!isCtrlUp && index > 0))
            {
                commandIndex = isCtrlUp ? ++index : --index;
                currentTab.Command = ((Command)commandHist[commandIndex]).CommandText;
                if (isCtrlUp)
                    listUpDownHist.Add(currentTab.Command);
                else
                    listUpDownHist.Insert(0, currentTab.Command);
            }
            else
            {
                // Add empty string when no unique command after the current command.
                if (isCtrlUp)
                    listUpDownHist.Add(string.Empty);
                else
                    listUpDownHist.Insert(0, string.Empty);
            }
        }

        private void UpdateTracingButtons(winMSQLTab tab)
        {
            if (tab.Tracing)
            {
                btnTrace.Text = "Stop Trace";
            }
            else
            {
                btnTrace.Text = "Start Trace";
            }
        }

        /// <summary>
        /// Tokenize the expression into individual words, removing brakets and parenthesis
        /// </summary>
        /// <param name="expression">The current text to tokenize.</param>
        private static String[] Tokenize(string expression)
        {
            return (tokenizer.Split(expression));
        }

        /// <summary>
        /// Load the base keywords and any custom keywords if a file is selected in the options
        /// </summary>
        private void LoadSyntaxKeywords()
        {
            commands.Clear();
            String[] keys = Properties.Resources.keywords.Split(';');
            foreach (String command in keys)
            {
                String[] pair = command.Trim().Split(',');
                if (pair.Length == 2)
                    this.commands.Add(pair[0].Trim().ToLower(), pair[1].Trim().ToUpper());


            }

            if (File.Exists(this.options.KeywordPath))
            {
                TextReader tr = new StreamReader(this.options.KeywordPath);
                
                String[] ExtraKeys = tr.ReadToEnd().Split(';');
                foreach (String command in ExtraKeys)
                {
                    String[] pair = command.Trim().Split(',');
                    if (pair.Length == 2)
                    {
                        if (!this.commands.ContainsKey(pair[0].Trim().ToLower()))
                            this.commands.Add(pair[0].Trim().ToLower(), pair[1].Trim().ToUpper());
                        else
                            this.commands[pair[0].Trim().ToLower()] = pair[1].Trim().ToUpper();
                    }
                }
            }
           
                
            
        }

        /// <summary>
        /// Update Highlighting after changes are made to the options
        /// </summary>
        private void UpdateSyntaxHighlighting()
        {
            try
            {
                //dont allow text to update until colors have changed
                LockWindowUpdate(currentTab.txtCmd.Handle);
                if (this.options.EnableHighlighting)
                {
                    //grab current text
                    string currentText = currentTab.txtCmd.Text;
                    //get current cursor position
                    int index;
                    index = currentTab.txtCmd.SelectionStart;
                    currentTab.txtCmd.SelectionStart = 0;
                    //clear old text
                    //currentTab.txtCmd.Text = "";

                    foreach (string token in Tokenize(currentText))
                    {

                        string word = token;
                        string braket = "";
                        //remove trailing brakets
                        if (token.EndsWith("]"))
                        {
                            word = token.Substring(0, token.Length - 1);
                            braket = "]";
                        }
                        if (token.EndsWith(")"))
                        {
                            word = token.Substring(0, token.Length - 1);
                            braket = ")";
                        }
                        if (token.EndsWith("|"))
                        {
                            word = token.Substring(0, token.Length - 1);
                            braket = "|";
                        }


                        currentTab.txtCmd.SelectionLength = word.Length;
                        //check if its a keyword
                        if (commands.ContainsKey(word.ToLower().Trim()))
                        {
                            string keyType;
                            commands.TryGetValue(word.ToLower().Trim(), out keyType);
                            if (options.HighlightColors.ContainsKey(keyType))
                                currentTab.txtCmd.SelectionColor = (Color)options.HighlightColors[keyType];
                            else
                                currentTab.txtCmd.SelectionColor = Color.Black;
                        }
                        //check if strings are highlighted
                        else if (this.options.HighlightStrings && word.Trim().StartsWith("\'") && word.Trim().EndsWith("\'"))
                        {
                            if (options.HighlightColors.ContainsKey("STRING"))
                            {
                                currentTab.txtCmd.SelectionColor = (Color)options.HighlightColors["STRING"];
                            }
                        }
                        else
                        {
                            currentTab.txtCmd.SelectionColor = Color.Black;
                        }

                        currentTab.txtCmd.SelectionLength = 0;
                        currentTab.txtCmd.SelectionStart += word.Length + braket.Length;
                        currentTab.txtCmd.SelectionColor = Color.Black;
                    }


                    //return to cursor position
                    currentTab.txtCmd.SelectionStart = index;
                    currentTab.txtCmd.SelectionColor = Color.Black;
                }
                else
                {
                    //grab current text
                    string currentText = currentTab.txtCmd.Text;
                    //get current cursor position
                    int index;
                    index = currentTab.txtCmd.SelectionStart;

                    //clear old text
                    currentTab.txtCmd.Text = "";
                    currentTab.txtCmd.SelectionColor = Color.Black;
                    currentTab.txtCmd.SelectedText = currentText;

                    //return to cursor position
                    currentTab.txtCmd.SelectionStart = index;
                    currentTab.txtCmd.SelectionColor = Color.Black;
                }
            }
            finally
            {
                //update text
                LockWindowUpdate(IntPtr.Zero);
            }
        }

        #endregion Worker Methods 

        #region Event Handlers

		void find_FormClosing(object sender, FormClosingEventArgs e)
		{
			FindHist = find.FindHistory;
			ReplaceHist = find.ReplaceHistory;
		}

        private void cmdFormat_Click(object sender, EventArgs e)
        {
            cFormat f = new cFormat(options);

			currentTab.txtCmd.Text = f.MSQL_Format(currentTab.Command);
            currentTab.focusCommand();
        }

        private void cmdHistory_Click(object sender, EventArgs e)
        {
            if (historyDlg == null || !historyDlg.Visible)
            {
                historyDlg = new ctlHistory(commandHist, this);
                historyDlg.Show();
            }
            else
            {
                historyDlg.Focus();
            }
        }

        private void cmdFave_Click(object sender, EventArgs e)
        {
            if (faveDlg == null || !faveDlg.Visible)
            {
                faveDlg = new ctlFavorite(commandFave, this);
                faveDlg.Show();
            }
            else
            {
                faveDlg.Focus();
            }
        }

        private void cmdAddFave_Click(object sender, EventArgs e)
        {
            if (String.IsNullOrEmpty(currentTab.Command))
                return;

            if (!commandFave.Contains(currentTab.Command))
            {
                if (faveDlg != null)
                    faveDlg.addFavorite(currentTab.Command);

                commandFave.Add(currentTab.Command);
            }
        }

        private void cmdExecute_Click(object sender, EventArgs e)
        {
            currentTab.Execute();
        }

        private void ctlWinMSQL_Shown(object sender, EventArgs e)
        {
            CenterToScreen();
            addNewTab();
            tabDashBoard.Tabs.RemoveAt(0);
			currentTab.focusService();
			bool host = !string.IsNullOrEmpty(hostArg);
			bool port = !string.IsNullOrEmpty(portArg);

			// Save these, setting manual tab will erase these values
			string currHost = currentTab.Service;

			if (host || port)
				currentTab.setManualServer();

			currentTab.Service = host ? hostArg : currHost;

			// This doesn't get erased by setting the server to manual
			if (!string.IsNullOrEmpty(envArg))
				currentTab.Environment = envArg;
        }

        private void openCommandToolStripMenuItem_Click(object sender, EventArgs e)
        {
            string cmd = "";
            dlg = new OpenFileDialog {Filter = "All Files (*.*)|*.*"};

            if (dlg.ShowDialog() != DialogResult.OK)
            {
                return;
            }

            String file = dlg.FileName;

            StreamReader read = new StreamReader(file);

            try
            {
                do
                {
                    cmd += read.ReadLine();
                    cmd += "\n";
                } while (read.Peek() != -1);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
            finally
            {
                read.Close();
            }

            currentTab.Command = cmd;
        }

        private void saveCommandToolStripMenuItem_Click(object sender, EventArgs e)
        {
            dlg = new SaveFileDialog { Filter = "MOCA Command, local syntax (*.mcmd)|*.mcmd|All Files (*.*)|*.*" };
            dlg.FilterIndex = 1;
            //dlg.InitialDirectory = Application.StartupPath;
            if (dlg.ShowDialog() != DialogResult.OK)
            {
                return;
            }

            string path = dlg.FileName;
			 
            StreamWriter write = new StreamWriter(path);

            if (dlg.FilterIndex == 1 || Path.GetExtension(path) == ".mcmd")
            {
                string fileName = Path.GetFileName(path).Split('.')[0].ToString();
                write.WriteLine(SaveMOCACommand(currentTab, fileName));
            }
            else
            {
                write.WriteLine(currentTab.Command);
            }

            write.Close();
        }

        /// <summary>
        /// Create the full featured command
        /// </summary>
        /// <param name="tab">The current tab</param>
        /// <param name="fileName">The name of the command</param>
        /// <returns>Full featured command</returns>
        private string SaveMOCACommand(winMSQLTab tab, string fileName)
        {
            StringBuilder sb = new StringBuilder();
            //Get name and description
            string [] nameArr = fileName.Split('_');
            for (int i = 0; i < nameArr.Length; i++)
            {
                sb.Append(nameArr[i]);
                if (i != nameArr.Length - 1)
                {
                    sb.Append(" ");
                }
            }
            string cmdNam = sb.ToString();
            sb.Length = 0;

            //Get return fields
            if (tab.GridView.Columns.Count > 0)
            {
                string retFldHead = "<retcol name=\"";
                string retFldMid1 = "\" type=\"";
                string retFldMid2 = "\">";
                string retFldTail = "</retcol>" + Environment.NewLine;
                for (int i = 0; i < tab.GridView.Columns.Count; i++)
                {
                    sb.Append(retFldHead);
                    sb.Append(tab.GridView.Columns[i].FieldName);
                    sb.Append(retFldMid1);
                    sb.Append(ConvRetFldDataType(
                        tab.GridView.Columns[i].ColumnType.Name.ToString()));
                    sb.Append(retFldMid2);
                    sb.Append(tab.GridView.Columns[i].FieldName);
                    sb.Append(retFldTail);
                }
            }
            string retFld = sb.ToString();
            sb.Length = 0;

            //Create full command
            sb.Append("<command>" + Environment.NewLine + "<name>");
            sb.Append(cmdNam);
            sb.Append("</name>" + Environment.NewLine
                      + "<description>TODO:  Add description here...");
            sb.Append("</description>" + Environment.NewLine
                      + "<type>Local Syntax</type>" + Environment.NewLine);
            sb.Append("<local-syntax>" + Environment.NewLine + "<![CDATA["
                      + Environment.NewLine);
            sb.Append(tab.Command);
            sb.Append(Environment.NewLine + "]]>" + Environment.NewLine
                      + "</local-syntax>" + Environment.NewLine + "<documentation>"
                      + Environment.NewLine + "<remarks>" + Environment.NewLine);
            sb.Append("<![CDATA[" + Environment.NewLine + "<p>" + Environment.NewLine
                      + "TODO:  This command will..." + Environment.NewLine + "</p>"
                      + Environment.NewLine + "]]>");
            sb.Append(Environment.NewLine + "</remarks>" + Environment.NewLine);
            sb.Append("<retrows>TODO:  Add return row information...</retrows>"
                      + Environment.NewLine);
            if (retFld.Length > 0)
            {
                sb.Append(retFld);
            }
            sb.Append("<exception value=\"eOK\">Success</exception>"
                      + Environment.NewLine);
            sb.Append("<exception value=\"eDB_NO_ROWS_AFFECTED\">No Data Found");
            sb.Append("</exception>" + Environment.NewLine + "<example>"
                      + Environment.NewLine);
            sb.Append("<p>" + Environment.NewLine + "TODO:  give a example here..."
                      + Environment.NewLine + "</p>" + Environment.NewLine + "</example>"
                      + Environment.NewLine);
            sb.Append("<seealso cref=\"TODO:  Add see also command\">"
                      + Environment.NewLine);
            sb.Append("</seealso>" + Environment.NewLine + "</documentation>"
                      + Environment.NewLine + "</command>");
            return sb.ToString();
        }

        /// <summary>
        /// Map the C# type to Local-Syntax type
        /// </summary>
        /// <param name="oritype">C# Type, e.g.Int32</param>
        /// <returns>Local-Syntax type, integer</returns>
        private string ConvRetFldDataType(string oritype)
        {
            string lstype = oritype.ToLower();
            switch (oritype.ToLower())
            {
                case "int16":
                case "int":
                case "int32":
                case "int64":
                case "short":
                case "sbyte":
                case "long":
                case "uint":
                case "ulong":
                case "ushort":
                    lstype = "integer";
                    break;
                case "bool":
                case "boolean":
                    lstype = "flag";
                    break;
                case "decimal":
                case "float":
                case "double":
                    lstype = "float";
                    break;
                case "byte[]":
                    lstype = "binary";
                    break;
                case "string":
                    lstype = "string";
                    break;
                case "datetime":
                    lstype = "datetime";
                    break;
                case "date":
                    lstype = "date";
                    break;
                default:
                    lstype = "string";
                    break;
            }
            return lstype;
        }

        private void loadResultsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            dlg = new OpenFileDialog
                      {
                          Filter = "XML-RS (*.rs)|*.rs|Excel (*.xls)|*.xls|CSV (*.csv)|*.csv|All Files (*.*)|*.*"
                      };
            string token = "";

            if (dlg.ShowDialog() != DialogResult.OK)
            {
                return;
            }

            string path = dlg.FileName;
			string wrkSht = dlg.FileName.Substring(path.LastIndexOf('\\') + 1, path.Length - (path.LastIndexOf('\\') + 5));
            StreamReader read = new StreamReader(path);

            DataSet ds = new DataSet();
            currentTab.Grid.DataSource = null;

			if (dlg.FilterIndex == 4)
			{
				string name = dlg.FileName;
				int idx = name.LastIndexOf(".");
				string sub = name.Substring(idx, name.Length - idx);

				switch (sub)
				{
					case ".rs": dlg.FilterIndex = 1; break;
					case ".xls": dlg.FilterIndex = 2; break;
					case ".csv": dlg.FilterIndex = 3; break;
					default: break;
				}
			}

            if (dlg.FilterIndex == 1)
            {
				// XML
                try
                {
					ds.ReadXml(read, XmlReadMode.ReadSchema);
                    currentTab.Grid.DataSource = ds.Tables[0];
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message, ex.Source, MessageBoxButtons.OK);
                }
            }
			else if (dlg.FilterIndex == 2)
			{
				// Excel
				string connStr = string.Format("Provider={0};Data Source={1};Extended Properties={2};",
											   "Microsoft.Jet.OLEDB.4.0", path, "Excel 8.0");
				System.Data.OleDb.OleDbConnection conn = new System.Data.OleDb.OleDbConnection(connStr);
				conn.Open();
				System.Data.OleDb.OleDbCommand cmd = new System.Data.OleDb.OleDbCommand("SELECT * FROM [" + wrkSht + "$]", conn);
				System.Data.OleDb.OleDbDataAdapter da = new System.Data.OleDb.OleDbDataAdapter {SelectCommand = cmd};
			    da.Fill(ds);
				conn.Close();
				currentTab.Grid.DataSource = ds.Tables[0];
			}
            else
            {
				// CSV
                string line = read.ReadLine();
				ds.Tables.Add();
				
                foreach (string tok in line.Split(','))
					ds.Tables[0].Columns.Add(tok, token.GetType());

                int rowCount = -1;

                while (read.Peek() != -1)
                {
                    int tokenCount = 0;
                    line = read.ReadLine();
                    ds.Tables[0].Rows.Add();
                    rowCount++;
					token = "";
					bool quote = false;
					
					foreach(char ch in line)
					{
						if (string.Compare(ch.ToString(), "\"") == 0)
						{
							quote = !quote;
						}
						else if (string.Compare(ch.ToString(), ",") == 0 && quote)
						{
							token += ch;
						}
						else if (string.Compare(ch.ToString(), ",") == 0 && !quote)
						{
							ds.Tables[0].Rows[rowCount][tokenCount] = token;
							token = "";
							tokenCount++;
						}
						else
						{
							token += ch;
						}
                    }

					// No comma at end of line so add the last token in
					ds.Tables[0].Rows[rowCount][tokenCount] = token;
                }
            }

			if (ds.Tables[0] != null)
				currentTab.Grid.DataSource = ds.Tables[0];

            read.Close();
        }

        private void saveResultsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            string txt = "";
            dlg = new SaveFileDialog
                      {
                          Filter = "XML-RS (*.rs)|*.rs|Excel (*.xls)|*.xls|CSV (*.csv)|*.csv"
                      };
            DataSet ds;

			if (currentTab.Grid.DataSource != null)
				ds = ((DataTable)currentTab.Grid.DataSource).DataSet;
			else
				return;

            if (dlg.ShowDialog() != DialogResult.OK)
            {
                return;
            }

            string path = dlg.FileName;

            if (dlg.FilterIndex == 1)
            {
				// XML
				StreamWriter writer = new StreamWriter(path);
				ds.WriteXml(writer, XmlWriteMode.WriteSchema);
				writer.Close();
            }
			else if (dlg.FilterIndex == 2)
			{
				// Excel
				currentTab.Grid.ExportToExcel(path);
			}
			else
			{
				// CSV
				StreamWriter writer = new StreamWriter(path);

				for (int x = 0; x < ds.Tables[0].Columns.Count; x++)
				{
					//Write out the header
					if (x != 0)
					{
						txt += ",";
					}

					txt += ds.Tables[0].Columns[x].ColumnName;
				}

				writer.WriteLine(txt);
				txt = "";

			    foreach (DataRow row in ds.Tables[0].Rows)
				{
					//Write out the data
					for (int x = 0; x < row.ItemArray.Length; x++)
					{
						if (x != 0)
						{
							txt += ",";
						}

						string tmp = row.ItemArray.GetValue(x).ToString();

						if (tmp.Contains(","))
							txt += "\"" + tmp + "\"";
						else
							txt += tmp;
					}

					writer.WriteLine(txt);
					txt = "";
				}

				writer.Close();
			}
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Close();
        }

        private void commandHistoryToolStripMenuItem_Click(object sender, EventArgs e)
        {
            cmdHistory_Click(sender, e);
        }

        private void expandedValueToolStripMenuItem_Click(object sender, EventArgs e)
        {
			if (currentTab.Grid.DataSource != null &&
				((DataTable)currentTab.Grid.DataSource).DataSet.Tables[0].Rows.Count > 0)
			{
				ctlValue value = new ctlValue(((DevExpress.XtraGrid.Views.Base.ColumnView)currentTab.Grid.MainView).GetFocusedValue().ToString());
				value.ShowDialog();
			}
			else
			{
				MessageBox.Show("No data to display");
			}
        }

        private void columnInfoToolStripMenuItem_Click(object sender, EventArgs e)
        {
			if (currentTab.Grid.DataSource != null)
			{
				ctlColumnInfo colInfo = new ctlColumnInfo(((DataTable)currentTab.Grid.DataSource).DataSet);
				colInfo.Show();
			}
			else
			{
				MessageBox.Show("No column info to display.");
			}
        }

        private void decreaseFontSizeToolStripMenuItem_Click(object sender, EventArgs e)
        {
			_fontSize -= 2;

            foreach (UltraTab page in tabDashBoard.Tabs)
            {
                if (page.TabPage.Controls.Count > 0)
                    ((winMSQLTab)page.TabPage.Controls[0]).setFontSize(_fontSize);
            }
        }

        private void increaseFontSizeToolStripMenuItem_Click(object sender, EventArgs e)
        {
			_fontSize += 2;

            foreach (UltraTab page in tabDashBoard.Tabs)
            {
                if (page.TabPage.Controls.Count > 0)
                    ((winMSQLTab)page.TabPage.Controls[0]).setFontSize(_fontSize);
            }
        }

        private void txtCmd_KeyDown(object sender, KeyEventArgs e)
        {
            if ((ModifierKeys & Keys.Control) == Keys.Control)
            {
                if (e.KeyCode == Keys.Up)
                {
                    if (commandIndex < commandHist.Count && commandHist.Count > 0)
                    {
                        if ((commandIndex == -1)
                            || (commandIndex > -1 && !currentTab.Command.Equals(((Command)commandHist[commandIndex]).CommandText)))
                        {
                            // Search match command like 4NT
                            ResetUpDownHistory();
                            SetMatchFindMode(currentTab.Command);
                        }
                        if (isFindMatch)
                        {
                            if (commandIndex > -1 && !currentTab.Command.Equals(((Command)commandHist[commandIndex]).CommandText))
                            {
                                // Do first match
                                int tmpIndex = commandIndex;
                                while (tmpIndex < commandHist.Count - 1 &&
                                    !((Command)commandHist[tmpIndex + 1]).CommandText.StartsWith(matchValue))
                                {
                                    tmpIndex++;
                                }
                                SetCommandForIndex(tmpIndex, true);
                            }
                            else
                            {
                                SetCommandBasedOnTrace(true);
                            }
                        }
                        else if (commandIndex <= commandHist.Count - 1)
                        {
                            SetCommandBasedOnTrace(true);
                        }
                    }
                    e.Handled = true;
                }
                else if (e.KeyCode == Keys.Down)
                {
                    if (commandIndex > 0 && commandHist.Count > 1)
                    {
                        if (!currentTab.Command.Equals(((Command)commandHist[commandIndex]).CommandText))
                        {
                            // Search match command like 4NT
                            ResetUpDownHistory();
                            SetMatchFindMode(currentTab.Command);
                        }
                        if (isFindMatch && commandIndex > 0 && !currentTab.Command.Equals(((Command)commandHist[commandIndex]).CommandText))
                        {
                            // Do first match
                            int tmpIndex = commandIndex;
                            while (tmpIndex > 0 && commandHist.Count > 1 &&
                                !((Command)commandHist[tmpIndex - 1]).CommandText.StartsWith(matchValue))
                            {
                                tmpIndex--;
                            }
                            SetCommandForIndex(tmpIndex, false);
                        }
                        else
                        {
                            SetCommandBasedOnTrace(false);
                        }
                    }
                    e.Handled = true;
                }
				else if (e.KeyCode == Keys.X)
				{
					Clipboard.SetText(currentTab.SelectedText);
					currentTab.SelectedText = "";
				}
				else if (e.KeyCode == Keys.C)
				{
					Clipboard.SetText(currentTab.SelectedText);
				}
				else if (e.KeyCode == Keys.V)
				{
					currentTab.SelectedText = Clipboard.GetText();
				}
				else if (e.KeyCode == Keys.F)
				{
					ShowFindDialog(false);
				}
                else if (e.KeyCode == Keys.W)
                {
                    removeTabToolStripMenuItem.PerformClick();
                }
            }
            else if (((ModifierKeys & Keys.Shift) != Keys.Shift) && 
                     e.KeyCode == Keys.Enter)
            {
				if (!options.EnterNewLine)
				{
					cmdExecute_Click(sender, null);
					e.Handled = true;
				}
            }
			else if (e.KeyCode == Keys.F5)
			{
				cmdExecute_Click(sender, e);
			}
			else if (e.KeyCode == Keys.Tab)
			{
				currentTab.txtCmd.Text += "\t";
			}           
      }
        

        private void txtCmd_TextChanged(object sender, EventArgs e)
        {
            try
            {           
                //dont allow window to update while changing colors
                LockWindowUpdate(currentTab.txtCmd.Handle);
                //if enabled, highlight syntax
                if (this.options.EnableHighlighting)
                {
                    //grab current text
                    string currentText = currentTab.txtCmd.Text;
                    //get current cursor position
                    int index;
                    index = currentTab.txtCmd.SelectionStart;
                    currentTab.txtCmd.SelectionStart = 0;
                    //clear old text
                    //currentTab.txtCmd.Text = "";

                    foreach (string token in Tokenize(currentText))
                    {

                        string word = token;
                        string braket = "";
                        //remove trailing brakets
                        if (token.EndsWith("]"))
                        {
                            word = token.Substring(0, token.Length - 1);
                            braket = "]";
                        }
                        if (token.EndsWith(")"))
                        {
                            word = token.Substring(0, token.Length - 1);
                            braket = ")";
                        }
                        if (token.EndsWith("|"))
                        {
                            word = token.Substring(0, token.Length - 1);
                            braket = "|";
                        }


                        currentTab.txtCmd.SelectionLength = word.Length;
                        //check if its a keyword
                        if (commands.ContainsKey(word.ToLower().Trim()))
                        {
                            string keyType;
                            commands.TryGetValue(word.ToLower().Trim(), out keyType);
                            if (options.HighlightColors.ContainsKey(keyType))
                                currentTab.txtCmd.SelectionColor = (Color)options.HighlightColors[keyType];
                            else
                                currentTab.txtCmd.SelectionColor = Color.Black;
                            
                        }
                        //check if strings are highlighted
                        else if (this.options.HighlightStrings && word.Trim().StartsWith("\'") && word.Trim().EndsWith("\'"))
                        {
                            if(options.HighlightColors.ContainsKey("STRING"))
                            {
                                currentTab.txtCmd.SelectionColor = (Color)options.HighlightColors["STRING"];
                            }
                        }
                        else
                        {
                            currentTab.txtCmd.SelectionColor = Color.Black;
                        }

                        currentTab.txtCmd.SelectionLength = 0;
                        currentTab.txtCmd.SelectionStart += word.Length + braket.Length;
                        currentTab.txtCmd.SelectionColor = Color.Black;
                    }


                    //return to cursor position
                    currentTab.txtCmd.SelectionStart = index;
                    currentTab.txtCmd.SelectionColor = Color.Black;
                }
            }
            finally
            {
                //update all text
                LockWindowUpdate(IntPtr.Zero);
            }

        }


        private void newTabToolStripMenuItem_Click(object sender, EventArgs e)
        {
            addNewTab();
        }

		private void executeToolStripMenuItem_Click(object sender, EventArgs e)
		{
			cmdExecute_Click(sender, e);
		}

		private void formatCommandToolStripMenuItem_Click(object sender, EventArgs e)
		{
			cmdFormat_Click(sender, e);
		}

		private void toolStripMenuItem1_Click(object sender, EventArgs e)
		{
			addNewTab();
		}

		private void removeTabToolStripMenuItem_Click(object sender, EventArgs e)
		{
            int nextTabPage;

            if (tabDashBoard.Tabs.Count < 3)
            {
                return;
            }

            if (tabDashBoard.SelectedTab.Index > 0)
                nextTabPage = tabDashBoard.SelectedTab.Index - 1;
            else
                nextTabPage = 0;

            tabDashBoard.Tabs.Remove(tabDashBoard.SelectedTab);
            tabDashBoard.SelectedTab = tabDashBoard.Tabs[nextTabPage];
		}

        private void closeOtherTabsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            for(int x=tabDashBoard.Tabs.Count-2; x>=0; x--)
            {
                if (tabDashBoard.SelectedTab.Index != x)
                    tabDashBoard.Tabs.RemoveAt(x);
            }
        }

        private void changeTabColorToolStripMenuItem_Click(object sender, EventArgs e)
        {
            ColorDialog dialog = new ColorDialog();
            if (dialog.ShowDialog() == DialogResult.OK)
            {
                currentTab.TabPage.Appearance.BackColor = dialog.Color;
                currentTab.TabPage.Appearance.BackColor2 = Color.White;
                currentTab.TabPage.SelectedAppearance.BackColor = dialog.Color;
                currentTab.TabPage.ClientAreaAppearance.BackColor = SystemColors.Control;
            }
        }

        private void winMSQLTab1_Connected(object sender, ConnectionEventArgs e)
		{
            if (currentTab.MocaClient != null && currentTab.MocaClient.Connected)
            {
                lastGoodLogin = (ServerInfo)e.ConnectInfo;

                // Enable the Start/View Trace buttons
                btnTrace.Enabled = true;
                btnViewTrace.Enabled = true;
            }
            else
            {
                // Disable the Start/View Trace buttons
                btnTrace.Enabled = false;
                btnViewTrace.Enabled = false;
            }
		}

		private void cmdOptions_Click(object sender, EventArgs e)
		{
			ShowOptionsDialog();
		}

		private void toolStripMenuItem3_Click(object sender, EventArgs e)
		{
			ShowOptionsDialog();
		}
						
		private void ctlWinMSQL_FormClosed(object sender, FormClosedEventArgs e)
		{
			// Save configuration options
			WriteConfigOptions();

			// Save command history
            WriteXMLHistory();
            WriteXMLFavorite();

			// Logout users on each tab.
			foreach (UltraTab tabPage in tabDashBoard.Tabs)
			{
				if (tabPage != null && tabPage.TabPage.Controls.Count > 0)
				{
                    winMSQLTab tab = (winMSQLTab)tabPage.TabPage.Controls[0];
					if (tab.MocaClient != null && tab.userConnected())
					{
						tab.MocaClient.LogOut();
					}
				}
			}				
		}

        private void tabDashBoard_MouseDown(object sender, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Right)
            {
                // Select a tab page on right mouse button click
                if (tabDashBoard.TabFromPoint(e.Location) != tabDashBoard.Tabs["tabBlank"] &&
                    tabDashBoard.TabFromPoint(e.Location) != null)
                {
                    tabDashBoard.SelectedTab = tabDashBoard.TabFromPoint(e.Location);
                    contextMenuStrip1.Show(MousePosition);
                }
            }
            else if (e.Button == MouseButtons.Left)
            {
                // Add new tab page if clicking on the add tab page tab
                if (tabDashBoard.TabFromPoint(e.Location) == tabDashBoard.Tabs["tabBlank"])
                    addNewTab();
            }
        }

        private void tabDashBoard_KeyDown(object sender, KeyEventArgs e)
        {
            if ((ModifierKeys & Keys.Control) == Keys.Control &&
                (ModifierKeys & Keys.Shift) == Keys.Shift)
            {
                if (e.KeyCode == Keys.Tab)
                {
                    // Previous index is used becuase this event isn't fired until after selected index is 
                    // updated to the index of the newly selected tabpage.
                    if (previousIndex == 0)
                        tabDashBoard.SelectedTab = tabDashBoard.Tabs[tabDashBoard.Tabs.Count - 2];
                }
            }
            // Move between tabs on an ctl-alt but don't select the add tab page tab
            // We don't want to add a new tab page, just scroll through the list of them.
            else if ((ModifierKeys & Keys.Control) == Keys.Control &&
                       (ModifierKeys & Keys.Control) != Keys.Shift)
            {
                if (e.KeyCode == Keys.Tab)
                {
                    if (tabDashBoard.SelectedTab.Index == tabDashBoard.Tabs.Count - 1)
                        tabDashBoard.SelectedTab = tabDashBoard.Tabs[0];
                }
            }
        }

        private void tabDashBoard_SelectedTabChanging(object sender, SelectedTabChangingEventArgs e)
        {
            if (tabDashBoard.SelectedTab == null)
                return;

            previousIndex = tabDashBoard.SelectedTab.Index;

            tabDashBoard.SelectedTab.Appearance.BackGradientStyle = GradientStyle.Vertical;
        }

        private void tabDashBoard_SelectedTabChanged(object sender, SelectedTabChangedEventArgs e)
        {
            if (tabDashBoard.SelectedTab == null)
                return;

            if (e.Tab == tabDashBoard.Tabs["tabBlank"])
            {
                tabDashBoard.SelectedTab = tabDashBoard.Tabs[tabDashBoard.Tabs.IndexOf(e.Tab) - 1];
            }
            else
            {
                currentTab.focusCommand();
                currentTab.SetCursor();
                currentTab.txtCmd.SelectionStart = currentTab.txtCmd.Text.Length;
            }

            tabDashBoard.SelectedTab.Appearance.BackGradientStyle = GradientStyle.None;
        }

		private void configureServersToolStripMenuItem_Click(object sender, EventArgs e)
		{
			System.Diagnostics.Process proc = new System.Diagnostics.Process
			                                      {
			                                          StartInfo =
			                                              {
			                                                  FileName = "dlxconfig.exe",
			                                                  UseShellExecute = false
			                                              }
			                                      };
		    proc.StartInfo.WorkingDirectory = proc.StartInfo.EnvironmentVariables["MCSDIR"] + "\\client\\bin";
			proc.EnableRaisingEvents = true;

			if (File.Exists(proc.StartInfo.WorkingDirectory + "\\" + proc.StartInfo.FileName))
				proc.Start();
			else
				MessageBox.Show("DlxConfig not in working directory,\n" + proc.StartInfo.WorkingDirectory);
		}

		private void cutToolStripMenuItem_Click(object sender, EventArgs e)
		{
			Control ctl = FromHandle(GetFocus());

            TextBox tbox = ctl as TextBox;
            if (tbox != null)
			{
				if (!string.IsNullOrEmpty(tbox.SelectedText))
				{
					Clipboard.SetText(tbox.SelectedText);
					tbox.SelectedText = "";
				}

			}
			else
            {
                RichTextBox rbox = ctl as RichTextBox;
                if (rbox != null)
                {
                    if (!string.IsNullOrEmpty(rbox.SelectedText))
                    {
                        Clipboard.SetText(rbox.SelectedText);
                        rbox.SelectedText = "";
                    }
                }
			}
		}

		private void copyToolStripMenuItem_Click(object sender, EventArgs e)
		{
			Control ctl = FromHandle(GetFocus());
            TextBox tbox = ctl as TextBox;

            if (tbox != null)
            {
                if (!string.IsNullOrEmpty(tbox.SelectedText))
                    Clipboard.SetText(tbox.SelectedText);
            }
            else
            {
                RichTextBox rbox = ctl as RichTextBox;
                if (rbox != null)
                {
                    if (!string.IsNullOrEmpty(rbox.SelectedText))
                        Clipboard.SetText(rbox.SelectedText);
                }
                else
                {
                    if (ctl is GridControl)
                    {
                        currentTab.CopySelectedGridRowsToClipboard();
                    }
                }
            }
		}

		private void pasteToolStripMenuItem_Click(object sender, EventArgs e)
		{
			Control ctl = FromHandle(GetFocus());

            TextBox tbox = ctl as TextBox;
            if (tbox != null)
            {
                tbox.SelectedText = Clipboard.GetText();
            }
            else
            {
                RichTextBox rbox = ctl as RichTextBox;
                if (rbox != null)
                {
                    rbox.SelectedText = Clipboard.GetText();
                }
            }
		}

		private void ctlWinMSQL_FormClosing(object sender, FormClosingEventArgs e)
		{
            if (tabDashBoard.Tabs.Count > 2 && options.ExitWarnMsg)
			{
				if (MessageBox.Show("Do you want to close all tabs?", "WinMSQL", MessageBoxButtons.OKCancel) == DialogResult.Cancel)
					e.Cancel = true;
			}
		}

		private void quickFindToolStripMenuItem_Click(object sender, EventArgs e)
		{
			ShowFindDialog(false);
		}

		private void quickReplaceToolStripMenuItem_Click(object sender, EventArgs e)
		{
			ShowFindDialog(true);
		}

		private void find_Find(object sender, FindEventArgs e)
		{
			// Do all the finds/replace in the tab directly
			currentTab.Find(e);
        }

        private void ctlWinMSQL_Activated(object sender, EventArgs e)
        {
            currentTab.txtCmd.SelectionStart = currentTab.txtCmd.Text.Length;
        }

        void ctlWinMSQL_CommandExecuted(object sender, CommandExcutedEventArgs e)
        {
            if (historyDlg != null)
            {
                historyDlg.addHistory(e.Status, e.Command, e.Rows, e.Service);
            }
            
            commandHist.Insert(0, new Command(e.Status, e.Command, e.Rows, e.Service, DateTime.Now));
            commandIndex = 0;
            // Reset the trace list of Ctrl-Up/Down.
            ResetUpDownHistory();
            // Add the selected command to the trace list
            listUpDownHist.Add(e.Command);
        }

        private void contextMenuStrip1_Opening(object sender, System.ComponentModel.CancelEventArgs e)
        {
            contextMenuStrip1.Items["closeOtherTabsToolStripMenuItem"].Enabled = tabDashBoard.Tabs.Count > 2;
            contextMenuStrip1.Items["toolStripMenuItem2"].Enabled = tabDashBoard.Tabs.Count > 2;
        }

        private void ctlWinMSQL_DoubleClick(object sender, EventArgs e)
        {
            addNewTab();
        }

        private void btnTrace_Click(object sender, EventArgs e)
        {

            tabDashBoard.ActiveTabChanged += new ActiveTabChangedEventHandler(tabDashBoard_ActiveTabChanged);
            currentTab.toggleTracing();
            UpdateTracingButtons(currentTab);
        }

        void tabDashBoard_ActiveTabChanged(object sender, ActiveTabChangedEventArgs e)
        {
            btnTrace.Enabled = false;
            btnViewTrace.Enabled = false;

            if (e.Tab.TabPage.Controls.Count > 0)
            {
                winMSQLTab tab = e.Tab.TabPage.Controls[0] as winMSQLTab;
                if (tab != null && tab.MocaClient != null && tab.MocaClient.Connected)
                {
                    btnTrace.Enabled = true;
                    btnViewTrace.Enabled = true;
                }
                UpdateTracingButtons(tab);
            }
        }

        private void btnViewTrace_Click(object sender, EventArgs e)
        {
            currentTab.ShowTrace();
        }

        #endregion Event Handlers

		#region Imported Methods

		[DllImport("user32.dll")]
		public static extern IntPtr GetFocus();
		
		#endregion Imported Method

        #region Event Classes

        public class CommandExcutedEventArgs : EventArgs
        {
            int _status;
            int _rows;
            string _command;
            string _service;

            public CommandExcutedEventArgs()
            {

            }

            public CommandExcutedEventArgs(int status, int rows, string command, string service)
            {
                _status = status;
                _rows = rows;
                _command = command;
                _service = service;
            }

            public int Status
            {
                get { return _status; }
                set { _status = value; }
            }

            public int Rows
            {
                get { return _rows; }
                set { _rows = value; }
            }

            public string Command
            {
                get { return _command; }
                set { _command = value; }
            }

            public string Service
            {
                get { return _service; }
                set { _service = value; }
            }
        }

        #endregion Event Classes
    }
}
