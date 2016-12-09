using System;
using System.IO;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Forms;
using Infragistics.Win.UltraWinTabControl;
using RedPrairie.MOCA.Client;
using RedPrairie.MCS.Defines;
using RedPrairie.MCS.WinMSQL;
using DevExpress.Utils;

namespace RedPrairie.MCS.WinMSQL
{
    public partial class winMSQLTab : UserControl
    {
        #region Enums

        private enum ConnectionState
        {
            DisConnected,
            Connected,
            Connecting,
            FailedConnect
        }

        #endregion Enums

        #region Private Variables
        private const string CLIENT_KEY = "mcsframework#gokpmqoetfnsasqffhkofreoybugag";
        private const string historyLabel = "---- History ---- ";
        private const string DEFAULT_HOST = "localhost";
		private const int DEFAULT_PORT = 4500;
		public const string DEFAULT_ENVIRONMENT = "LOCALE_ID=US_ENGLISH";
        private bool blTrace;
		private string filename = "";
		private FullConnection mocaClient;
		private Hashtable _configOptions = new Hashtable();
        private IDictionary<string, Color> _colorOptions = new Dictionary<string, Color>();
        readonly DevExpress.XtraEditors.Repository.RepositoryItemDateEdit repItemDateEdit = new DevExpress.XtraEditors.Repository.RepositoryItemDateEdit();
		private readonly ctlTracing traceDlg;
        private ConnectionState connectionState = ConnectionState.DisConnected;
        private DateTime _startTime = DateTime.Now;
        private ExecuteCallBackEventArgs executeEventArg;
        private DateTime endTime;
        private TimeSpan tSpan;
        private double dbTime;
        private Thread workerThread;
        private UltraTab _tabPage;
        private bool _suppressMessages;
        private bool _isExecutingCommand;
        private string _errorText = "";
        private string service = "";
        private ServerCache serverCache;
        private bool overrideColor = false;
        private Color tabColor;
        #endregion Private Variables

		#region Delegates

		public delegate void CommandKeyDownHandler(object sender, KeyEventArgs e);
        public delegate void TextChangedHandler(object sender, EventArgs e);
		public delegate void ConnectionHandler(object sender, ConnectionEventArgs e);
        public delegate void CommandExecutedEventHandler(object sender, ctlWinMSQL.CommandExcutedEventArgs e);

		#endregion Delegates

        #region Events

        public event ConnectionHandler Connected;
        public event CommandKeyDownHandler commandKeyDown;
        public event TextChangedHandler textChanged;
        public event CommandExecutedEventHandler CommandExecuted;

        #endregion Events

        #region Constructor

        public winMSQLTab() : this(null) { }
        public winMSQLTab(ServerCache srvrCache) : this(srvrCache, null) { }
        public winMSQLTab(ServerCache srvrCache, ServerInfo currHost)
        {
            InitializeComponent();

			traceDlg = new ctlTracing();

			gridView2.CustomRowCellEdit += gridView2_CustomRowCellEdit;
			gridView2.BestFitMaxRowCount = 100;
			gridView2.OptionsBehavior.Editable = false;

			cmdContextMenu.Items["cutToolStripMenuItem"].Click += tmenuCut_Click;
			cmdContextMenu.Items["copyToolStripMenuItem"].Click += tmenuCopy_Click;
			cmdContextMenu.Items["pasteToolStripMenuItem"].Click += tmenuPaste_Click;

			gridContextMenu.Items["copyCellToolStripMenuItem1"].Click += gridContextMenuCopyCell_Click;
			gridContextMenu.Items["copyRowToolStripMenuItem"].Click += gridContextMenuCopyRow_Click;
            gridContextMenu.Items["copyPublishToolStripMenuItem"].Click += gridContextMenuCopyPublish_Click;

			repItemDateEdit.DisplayFormat.FormatType = FormatType.DateTime;
			repItemDateEdit.DisplayFormat.FormatString = "g";
			
			txtEnv.Text = DEFAULT_ENVIRONMENT;

			cbxServer.BeginUpdate();

            cbxServer.Items.Clear();

            serverCache = srvrCache;
            if (serverCache != null)
            {
                cbxServer.DataSource = serverCache.GetServerHistory(historyLabel);
                serverCache.CacheUpdated += new ServerCache.CacheUpdatedHandler(serverCache_CacheUpdated);
            }
            else
            {
                cbxServer.DataSource = new ArrayList();
            }

			// Set the login info for this tab to the info from the previous selected tab
			if (currHost != null)
			{
			    cbxServer.SelectedItem = currHost.Name;				
			}

			cbxServer.EndUpdate();
		}
        
		#endregion Constructor

		#region Properties

		public string Service
		{
			get 
			{
				return service;
			}
			set
			{
                cbxServer.Invoke(new updateCurrentServerDelegate(updateCurrentServer), value);
                service = value;
			}
		}

		
		public string Environment
		{
			get
			{
				return txtEnv.Text;
			}
			set
			{
				txtEnv.Text = value;
			}
		}

		public FullConnection MocaClient
        {
            get
            {
                return mocaClient;
            }
        }

		public string UserName
		{
			get
			{
				return txtUser.Text;
			}
			set
			{
				txtUser.Text = value;
			}
		}

		public string Password
		{
			get
			{
				return txtPswd.Text;
			}
			set
			{
				txtPswd.Text = value;
			}
		}

        public string Command
        {
            get
            {
                return txtCmd.Text;
            }
            set
            {
                txtCmd.Text = value;
            }
        }

        public bool Tracing
        {
            get
            {
                return blTrace;
            }
            set
            {
                blTrace = value;
            }
        }

        public DevExpress.XtraGrid.GridControl Grid
        {
            get
            {
                return gridMst;
            }
            set
            {
                gridMst = value;
            }
        }

		public DevExpress.XtraGrid.Views.Grid.GridView GridView
		{
			get
			{
                return gridView2;
			}
			set
			{
				gridView2 = value;
			}
		}

        public string Returned_Rows
        {
            get
            {
                return lblRowsRet.Text;
            }
            set
            {
                lblRowsRet.Text = value;
            }
        }

        public string Elapsed_Time
        {
            get
            {
                return lblElpsdTime.Text;
            }
            set
            {
                lblElpsdTime.Text = value;
            }
        }

        public string Status
        {
            get
            {
                return lblSts.Text;
            }
            set
            {
                lblSts.Text = value;
            }
		}

		public string ExecuteTime
		{
			get
			{
				return lblExecTime.Text;
			}
			set
			{
				lblExecTime.Text = value;
			}
		}

		public bool AutoCommit
		{
			get
			{
				return chkAutoCommit.Checked;
			}
			set
			{
				chkAutoCommit.Checked = value;
			}
		}

		public string SelectedText
		{
			get
			{
				return txtCmd.SelectedText;
			}
			set
			{
				txtCmd.SelectedText = value;
			}
		}

		public Hashtable TraceOptions
		{
			get
			{
				return traceDlg.TraceOptions;
			}
			set
			{
				traceDlg.TraceOptions = value;
				traceDlg.LoadTraceOptions();
			}
		}

		public Hashtable ConfigOptions
		{
			get
			{
				return _configOptions;
			}
			set
			{
				_configOptions = value;
			}
		}

        public IDictionary<string, Color> ColorOptions
        {
            get
            {
                return _colorOptions;
            }
            set
            {
                _colorOptions = value;
            }
        }


        public bool IsExecuting
        {
            get { return _isExecutingCommand; }
            set { _isExecutingCommand = value; }
        }

		#endregion Properties

		#region Public Methods

        public void Execute()
        {
            if (_isExecutingCommand)
                return;

#if DEBUG
#else
				if (!userConnected())
				{
					MessageBox.Show("User is not connected");
					return;
				}
#endif
            string commandText = txtCmd.Text.Trim();
            if (commandText.Length <= 0 || mocaClient == null || !mocaClient.Connected)
            {
                return;
            }

            

            if (_configOptions.Contains("chkIntegrator"))
            {
                string opt = _configOptions["chkIntegrator"].ToString();

                if (opt.Equals("true", StringComparison.InvariantCultureIgnoreCase))
                {
                    commandText = SupportIntegratorBinding(commandText);
                }
            }

            //Check for file upload argument
            commandText = CheckBinaryUpload(commandText);

            _isExecutingCommand = true;

            _startTime = DateTime.Now;

            Cursor.Current = Cursors.WaitCursor;
            mocaClient.BeginExecute(commandText, "WinMSQL", MocaClientCallBack);
            EnableExecuteControls(false);
        }

        

        private void MocaClientCallBack(ExecuteCallBackEventArgs e)
        {
            endTime = DateTime.Now;

            // Calculate elapsed time
            tSpan = endTime - _startTime;
			dbTime += (double) tSpan.Minutes*60*1000;
            dbTime += (double) tSpan.Seconds*1000;
            dbTime +=  tSpan.Milliseconds;
            dbTime /= 1000;

            executeEventArg = e;

            UpdateCommandExecuteUIFromThread();

            _isExecutingCommand = false;
        }

        private void UpdateCommandExecuteUIFromThread()
        {
            if (InvokeRequired)
            {
                Invoke(new MethodInvoker(UpdateCommandExecuteUIFromThread));
            }
            else
            {
                UpdateCommandExecuteUI();
            }
        }

        private void UpdateCommandExecuteUI()
        {
            int rows;

            if (executeEventArg.TableData != null)
            {
                gridMst.DataSource = executeEventArg.TableData;
                rows = executeEventArg.TableData.Rows.Count;
            }
            else
            {
                gridMst.DataSource = null;
                rows = 0;
            }

            int statusCode = executeEventArg.StatusCode;
            if (executeEventArg.StatusCode != MOCA.Util.MocaErrors.eOK && executeEventArg.StatusCode != MOCA.Util.MocaErrors.eSRV_NO_ROWS_AFFECTED)
            {
                gridMst.DataSource = null;
                rows = 0;

                if (executeEventArg.Error.ErrorCode != MOCA.Util.MocaErrors.eSRV_AUTHENTICATE)
                {
                   MessageBox.Show(executeEventArg.Error.Message, "Error Code: " + executeEventArg.Error.ErrorCode, MessageBoxButtons.OK);
                }

                if (executeEventArg.Error.ErrorCode == MOCA.Util.MocaErrors.eMCC_FAILED_TO_CONNECT)
                    DisConnect();
            }

            Returned_Rows = rows.ToString();
            Status = executeEventArg.StatusCode.ToString();
            Elapsed_Time = dbTime.ToString("N3");
			ExecuteTime = _startTime.ToLongTimeString();

            ctlWinMSQL.CommandExcutedEventArgs eventArgs = new ctlWinMSQL.CommandExcutedEventArgs();
            eventArgs.Command = txtCmd.Text;
            eventArgs.Service = service;
            if (executeEventArg.TableData != null)
                eventArgs.Rows = executeEventArg.TableData.Rows.Count;
            eventArgs.Status = executeEventArg.StatusCode;
            CommandExecuted(this, eventArgs);

            try
            {
                // seen some errors out of these calls, not sure what's up
                gridMst.MainView.PopulateColumns();

                if (executeEventArg.TableData != null)
                {
                    if (executeEventArg.TableData.Columns.Count > 1)
                    {
                        gridView2.OptionsView.ColumnAutoWidth = false;
                        gridView2.BestFitColumns();    
                    }
                    else
                    {
                        gridView2.OptionsView.ColumnAutoWidth = true;
                    }
                }
            }
            catch { }

            EnableExecuteControls(true);

            if (statusCode == MOCA.Util.MocaErrors.eSRV_AUTHENTICATE)
            {
                txtPswd.Focus();
                chkAutoCommit.Enabled = false;
            }
            else
            {
                txtCmd.Focus();
            }
        }

		public void connectMMDA(string host, int port, string env)
		{
			mocaClient = new FullConnection(host, port, env);
            mocaClient.DigitalSignatureChallenge += (mmda_DigitalSignatureChallenge);
            mocaClient.ServerTimeoutLogin += (mmda_ServerTimeoutLogin);
            mocaClient.Connect();
		}

		public bool userConnected()
		{
		    return mocaClient != null && mocaClient.LoggedIn;
		}

        public void focusService()
		{
			cbxServer.Focus();
			cbxServer.SelectAll();
		}

        public void focusCommand()
        {
            txtCmd.Focus();
            txtCmd.SelectAll();
        }

		public void focusUser()
		{
			txtUser.Focus();
			txtUser.SelectAll();
		}

        public void setFontSize(float size)
        {
            txtCmd.Font = new Font(txtCmd.Font.Name, size, txtCmd.Font.Style, txtCmd.Font.Unit);
            gridMst.Font = new Font(gridMst.Font.Name, gridMst.Font.Size + 2, gridMst.Font.Style, gridMst.Font.Unit);
            txtCmd.Refresh();
            gridMst.Refresh();
        }

        public bool setManualServer()
		{
			if (cbxServer.Items.Count > 0)
			{
				cbxServer.SelectedIndex = 0;
				return true;
			}

			return false;
		}

		public void Find(FindEventArgs e)
		{
			if (!e.LookInCommandText) // search results in grid
			{
				int selRow = gridView2.FocusedRowHandle;
				int selCol = gridView2.FocusedColumn.VisibleIndex;
				bool found;
				
				for(int colIdx=selCol; colIdx<gridView2.Columns.Count; colIdx++)
				{
					found = FindTextInRows(e, colIdx, selCol, selRow);
					
					if (found)
						return;
				}

				for (int colIdx = 0; colIdx < selCol; colIdx++)
				{
					found = FindTextInRows(e, colIdx, selCol, selRow);

					if (found)
						return;
				}
			}
			else if (e.Replace)
			{
				if (txtCmd.SelectionLength <= 0)
				{
					FindText(e.FindText, true, !e.MatchCase, e.MatchWholeWord);
				}
				else
				{
					if (string.Compare(e.FindText, txtCmd.SelectedText, !e.MatchCase, System.Globalization.CultureInfo.InvariantCulture) == 0)
						txtCmd.SelectedText = e.ReplaceText;

					FindText(e.FindText, true, !e.MatchCase, e.MatchWholeWord);
				}
			}
			else if (e.ReplaceAll)
			{
				int matches = FindText(e.FindText, false, !e.MatchCase, e.MatchWholeWord);

				if (matches > 0)
				{
					if (MessageBox.Show("Replace all " + matches + " matches?", "Replace",
						MessageBoxButtons.OKCancel) == DialogResult.OK)
					{
						for(int x = 0; x<matches; x++)
						{
							FindText(e.FindText, true, !e.MatchCase, e.MatchWholeWord);

							if (txtCmd.SelectionLength > 0)
								txtCmd.SelectedText = e.ReplaceText;
						}
					}
				}
				else
				{
					MessageBox.Show("No matches found");
				}
			}
			else
			{
				FindText(e.FindText, true, !e.MatchCase, e.MatchWholeWord);
			}
		}

        public void CopySelectedGridRowsToClipboard()
        {
            string clip = "";

            int[] rowHandles = gridView2.GetSelectedRows();
            DataRow row;

            foreach (int handle in rowHandles)
            {
                row = gridView2.GetDataRow(handle);
                bool startRow = true;

                foreach (DataColumn col in row.Table.Columns)
                {
                    if (!startRow)
                        clip += ",";

                    clip += row[col].ToString();
                    startRow = false;
                }

                clip += "\n";
            }

            if (!string.IsNullOrEmpty(clip))
                Clipboard.SetText(clip);
        }

        public void SetConnectingState()
        {
            connectionState = ConnectionState.Connecting;
            EnableProgressBar(true);
            UpdateUI();
        }

        public void CancelConnection()
        {
            if (workerThread != null)
                workerThread.Abort();

            DisconnectMocaClient();
            
            connectionState = ConnectionState.DisConnected;

            UpdateUI();
        }

		public void DisConnect()
		{
            // If we are tracing, turn it off
            if (Tracing)
            {
                toggleTracing();
            }

            DisconnectMocaClient();

            if (workerThread != null)
                workerThread.Abort();

            connectionState = ConnectionState.DisConnected;

            RaiseConnectionEvent();
            UpdateUI();
		}

        public void DisconnectMocaClient()
        {
            if (mocaClient != null && mocaClient.Connected && mocaClient.LoggedIn)
            {
                if (!string.IsNullOrEmpty(mocaClient.GetEnvironmentVariable("MOCA_APPL_ID")))
                    mocaClient.SetEnvironmentVariable("MOCA_APPL_ID", "");
                mocaClient.LogOut();
            }

            mocaClient = null;
        }

        public void DoConnect(object info)
        {
            Connect((ServerInfo)info);
        }
        public void Connect(ServerInfo info)
		{
		    try
			{
                // Update the cache
                serverCache.UpdateLoginInfo(info);
                serverCache.WriteManualLoginInfo();
                
                Cursor.Current = Cursors.WaitCursor;
                bool loggedIn = false;

				if (mocaClient == null || !mocaClient.Connected)
                {

#if DEBUG
#else
					if (txtUser.Text.Length <= 0)
					{
						_errorText = "User name and password required to login";
                        DisplayErrorMessageFromThread();
                        connectionState = ConnectionState.FailedConnect;
						return;
					}
#endif
                    try
                    {
                        bool needRevert = false;
                        if (info.Port == 0 && !info.Host.StartsWith("http")) //this means we have a legacy instance
                        {
                            string[] split = info.Host.Split(new char[] { ':' });
                            if (split.Length != 2)
                                throw new ArgumentException(string.Format("Invalid host/port pairing: {0}", info.Host));

                            info.Host = split[0];
                            int port = 0;
                            int.TryParse(split[1], out port);
                            info.Port = port;
                            // need revert the host/port for legacy instance
                            needRevert = true;
                        }
                        mocaClient = new FullConnection(info.Host, info.Port, info.Environment); ;
                        if (needRevert)
                        {
                            info.Host = string.Format("{0}:{1}", info.Host, info.Port);
                            info.Port = 0;
                        }
                        mocaClient.DigitalSignatureChallenge += (mmda_DigitalSignatureChallenge);
                        mocaClient.ServerTimeoutLogin += (mmda_ServerTimeoutLogin); 
                        mocaClient.AutoCommit = chkAutoCommit.Checked;

					
                        mocaClient.Connect();
					}
					catch (Exception)
					{
                        if (!_suppressMessages)
                        {
                            _errorText = string.Format("Could not connect to server: {0}", info.Host);
                            DisplayErrorMessageFromThread();
                        }
                        connectionState = ConnectionState.FailedConnect;
						return;
					}

                    connectionState = mocaClient.Connected ? ConnectionState.Connected : ConnectionState.FailedConnect;

                    if (txtUser.Text.Length > 0 || txtPswd.Text.Length > 0)
                    {
                        loggedIn = mocaClient.Login(txtUser.Text, txtPswd.Text, CLIENT_KEY);

                        if (!loggedIn)
                        {
                            _errorText = "Invalid user login";
                            DisplayErrorMessageFromThread();
                            mocaClient = null;
                            connectionState = ConnectionState.FailedConnect;
                            return;
                        }
                    }

                    else
                    {
#if DEBUG
#else
                        _errorText = "User name and password required to login";
                        DisplayErrorMessageFromThread();
                        connectionState = ConnectionState.FailedConnect;
                        return;
#endif
                    }
#if DEBUG
#else
					if (loggedIn)
					{
						int sts = mocaClient.Execute(string.Format("get user privileges where usr_id = '{0}' " +
															 "and opt_typ = 'A' and opt_nam = 'SALSrvCmdOpr'", txtUser.Text));

						if (sts != 0)
						{
                            _errorText = "You do not have privileges to execute commands through WinMSQL";
                            DisplayErrorMessageFromThread();
							mocaClient = null;
                            connectionState = ConnectionState.FailedConnect;
							return;
						}
					}
#endif
                }
			}
			finally
			{
				Cursor.Current = Cursors.Default;
                UpdateUIFromThread();
                RaiseConnectionEventFromThread();
			}
		}

        public UltraTab TabPage
        {
            get { return _tabPage; }
            set { _tabPage = value; }
        }

        public void SetCursor()
        {
            Cursor.Current = _isExecutingCommand ? Cursors.WaitCursor : Cursors.Default;
        }

        public void EnableExecuteControls(bool enabled)
        {
            txtCmd.Enabled = enabled;
            gridMst.Enabled = enabled;
            btnConnect.Enabled = enabled;
            chkAutoCommit.Enabled = enabled;
            EnableProgressBar(!enabled);
        }

        public void EnableProgressBar(bool enable)
        {
            progressBar1.Enabled = enable;
            progressBar1.Visible = enable;
        }

        public void toggleTracing()
        {
            string options = "";

            if (Tracing)
            {
                // End trace
                Tracing = false;
                TabPage.Text = cbxServer.Text;

                mocaClient.DisableTracing();

                DialogResult res = MessageBox.Show("View trace now?", "Trace", MessageBoxButtons.YesNo);

                if (res == DialogResult.Yes)
                {
                    ShowTrace();
                }
            }
            else
            {
                traceDlg.ShowDialog();

                if (traceDlg.DialogResult == DialogResult.OK)
                {
                    traceDlg.getTraceOptions(ref filename, ref options);

                    if (string.IsNullOrEmpty(filename))
                    {
                        Focus();
                        return;
                    }

                    Tracing = true;
                    TabPage.Text = cbxServer.Text + " *";

                    mocaClient.EnableTracing(filename, options);

                }
            }

            focusCommand();
        }

        public void ShowTrace()
        {
            if (string.IsNullOrEmpty(filename))
                return;

            System.Diagnostics.Process proc = new System.Diagnostics.Process();
            proc.EnableRaisingEvents = false;
            proc.StartInfo.UseShellExecute = false;
            proc.StartInfo.WorkingDirectory = System.IO.Directory.GetParent(Application.ExecutablePath).ToString();

            if (ConfigOptions.Count > 0 && ConfigOptions["chkExtTraceViewer"].ToString().Equals("True"))
            {
                DataTable dt;
                byte[] fileData;

                int status = mocaClient.Execute(string.Format(
                                                "get directory name where filename='$LESDIR/log'|" +
                                                "get file where filename=@dirname || '/log/{0}'",
                                                MCSDefines.ReplaceSingleQuotes(filename)), out dt);
                if (status == 0)
                {
                    string tempName = Path.GetTempFileName();
                    string args = "";
                    if (ConfigOptions.ContainsKey("txtArgs"))
                        args = ConfigOptions["txtArgs"].ToString();
                    proc.StartInfo.WorkingDirectory = Path.GetDirectoryName(tempName); // reset working dir

                    fileData = (byte[])dt.Rows[0]["data"];
                    FileStream fs = File.Create(tempName);
                    fs.Write(fileData, 0, fileData.Length);
                    fs.Close();

                    if (!string.IsNullOrEmpty(args))
                    {
                        if (args.Contains("%1"))
                            args = args.Replace("%1", tempName);
                        else
                            args += " " + tempName;
                    }
                    else
                    {
                        args = tempName;
                    }

                    try
                    {
                        System.Diagnostics.Process.Start(ConfigOptions["txtTraceViewer"].ToString(), args);
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show(ex.ToString(), ex.Message, MessageBoxButtons.OK);
                    }
                }
            }
            else
            {
                string serviceURL = serverCache.GetLoginInfo(cbxServer.Text).Host;
                proc.StartInfo.Arguments = "-" + MCSDefines.APP_TRACE_FILE_ARGUMENT + " " + mocaClient.TraceFilePath +
                                            " -" + MCSDefines.APP_HOST_ARGUMENT + " " + serviceURL +
                                            " -" + MCSDefines.APP_USER_ARGUMENT + " " + UserName +
                                            " -" + MCSDefines.APP_PASSWD_ARGUMENT + " " + Password;
                proc.StartInfo.FileName = "TraceAnalyzer.exe";

                try
                {
                    if (!string.IsNullOrEmpty(proc.StartInfo.WorkingDirectory) && !string.IsNullOrEmpty(proc.StartInfo.Arguments))
                        proc.Start();
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.Message);
                }
            }
        }

        public void UpdateTabColor()
        {
            Color color = Color.Empty;

            if (serverCache != null)
            {
                ServerInfo info = null;
                info = serverCache.GetLoginInfo(cbxServer.Text);
                if (info != null)
                {
                    color = info.TabColor;
                }
            }

            if (TabPage != null && !overrideColor)
            {
                // Remove the event handler temporarily (we don't want it to fire when we change color)
                TabPage.SubObjectPropChanged -=
                    new Infragistics.Shared.SubObjectPropChangeEventHandler(TabPage_SubObjectPropChanged);

                // Change the tab color
                TabPage.Appearance.BackColor = color;
                TabPage.Appearance.BackColor2 = Color.White;
                TabPage.SelectedAppearance.BackColor = color;
                TabPage.ClientAreaAppearance.BackColor = SystemColors.Control;

                // Add the event handler back
                TabPage.SubObjectPropChanged +=
                    new Infragistics.Shared.SubObjectPropChangeEventHandler(TabPage_SubObjectPropChanged);
            }
        }

		#endregion Public Methods

        #region Private Methods

        private void DisplayErrorMessageFromThread()
        {
            if (InvokeRequired)
            {
                Invoke(new MethodInvoker(DisplayErrorMessageFromThread));
            }
            else
            {
                DisplayErrorMessage();
            }
        }

        private void DisplayErrorMessage()
        {
            MessageBox.Show(_errorText, "Error Connecting", MessageBoxButtons.OK);
        }

        private static string ConvertInteratorBinding(string cmd)
        {
            // Replace ":i_c_", ":i_n_", ":i_d_" with "@" first.
            cmd = Regex.Replace(cmd, @":i_[cnd]_", "@", RegexOptions.IgnoreCase);

            if (cmd.Contains(":"))
            {
                MatchCollection args = Regex.Matches(cmd, @":\S+", RegexOptions.IgnoreCase);
                foreach (Match arg in args)
                {
                    // We should ignore the ":raw".
                    if (Regex.Matches(arg.Value, @":raw\b", RegexOptions.IgnoreCase).Count == 0)
                        cmd = cmd.Replace(arg.Value, "@" + arg.Value.Substring(1));
                }
            }

            return cmd;
        }

        private static string SupportIntegratorBinding(string cmd)
        {
            Hashtable ht = new Hashtable();
            int pos = 0;
            int index = 0;
            string ret = "";

            // Pick up the sub-string surrounded by ' ' or " ",
            // and add the others into a hash table.
            MatchCollection args = Regex.Matches(cmd, "\".*?\"|'.*?'", RegexOptions.Singleline);
            foreach (Match arg in args)
            {
                int argPos = cmd.IndexOf(arg.Value);

                // Check for duplicate Args and get the correct position.
                if (argPos < index)
                    argPos = index + cmd.Substring(index).IndexOf(arg.Value);

                ht.Add(pos++, cmd.Substring(index, argPos - index));
                index = argPos + arg.Length;
            }
            ht.Add(args.Count, cmd.Substring(index));

            // Replace integrator binding symbol with "@" except for that surrounded by quotes.
            for (pos = 0; pos < args.Count; pos++)
                ret += ConvertInteratorBinding(ht[pos].ToString()) + args[pos].Value;

            ret += ConvertInteratorBinding(ht[args.Count].ToString());

            return ret;
        }

        private static string CheckBinaryUpload(string commandText)
        {
            Regex uploadRegex = new Regex(@"\![Ff]ile\[(.+)\]\!", RegexOptions.Compiled);

            return uploadRegex.Replace(commandText,
                                       new MatchEvaluator(delegate(Match match)
                                                              {
                                                                  if (match.Groups.Count == 2 && 
                                                                      File.Exists(match.Groups[1].Value))
                                                                  {
                                                                      return 
                                                                          MCSDefines.GetUUEncodedString(match.Groups[1].Value);
                                                                  }
                                                                  return "b64decode([])";
                                                              }));

        }

		private bool FindTextInRows(FindEventArgs e, int colIdx, int selCol, int selRow)
		{
		    bool found = false;

			DevExpress.XtraGrid.Columns.GridColumn col = gridView2.GetVisibleColumn(colIdx);
			int startRow = (gridView2.RowCount >= selRow + 2 && colIdx == selCol) ? selRow + 1 : 0;

			for (int rowIdx = startRow; rowIdx < gridView2.RowCount; rowIdx++)
			{
				object val = gridView2.GetRowCellValue(rowIdx, col);
				string str = val.ToString();

				if (e.MatchWholeWord)
				{
					foreach (string s in str.Split(' '))
					{
						if (string.Compare(s, e.FindText, !e.MatchCase) == 0)
							found = true;
					}
				}
				else if (!e.MatchCase)
				{ // Ignore case
					if (str.ToLower().IndexOf(e.FindText.ToLower(), 0, str.Length) >= 0)
						found = true;
				}
				else
				{
					if (str.IndexOf(e.FindText, 0, str.Length) >= 0)
						found = true;
				}

				if (found)
				{
					gridView2.ClearSelection();
					gridView2.FocusedRowHandle = rowIdx;
					gridView2.SelectRow(rowIdx);
					gridView2.FocusedColumn = col;

					return found;
				}
			}

			return found;
		}

        private void mmda_DigitalSignatureChallenge(object sender, DigitalSignatureEventArgs e)
        {
            ctlLogin loginDialog = new ctlLogin(mocaClient, txtUser.Text, e.OverrideRequired, e.ErrorMessage);

            DialogResult result = loginDialog.ShowDialog(ParentForm);

            if (result != DialogResult.OK)
                e.Cancel = true;
        }

        private void mmda_ServerTimeoutLogin(object sender, ServerTimeoutEventArgs e)
        {
            //On a server session timeout we will show the error text 
            //assocated with the timout exception, and then force a reconnect
            //with everything except the password defaulted.

            MessageBox.Show("Session has timed out. Reconnection required.", "Error Code: " + MOCA.Util.MocaErrors.eSRV_AUTHENTICATE, MessageBoxButtons.OK);

            txtPswd.Text = "";
            DisConnect();

            e.Cancel = true;
        }
		
		private int FindText(string searchText, bool select, bool ignoreCase, bool matchWord)
		{
			int matchesFound = 0;
			int index = 0, startIdx = 0;
			string txt = searchText;
			bool matching = false, foundMatch = false;
			string currWord = "";

			// Start at the cursor position
			for (int ch = txtCmd.SelectionStart + txtCmd.SelectionLength; ch < txtCmd.Text.Length; ch++)
			{
				if (string.Compare(" ", txtCmd.Text[ch].ToString()) == 0)
					currWord = "";
				else
					currWord += txtCmd.Text[ch].ToString();

				if (string.Compare(txtCmd.Text[ch].ToString(), txt[index].ToString(), ignoreCase, System.Globalization.CultureInfo.InvariantCulture) == 0)
				{
					index++; // We found a match so increment to the next letter in the text we're comparing

					if (!matching)
					{ // Do this on the first char match
						startIdx = ch;
						matching = true;
					}

					if (index == txt.Length)
					{
						if (matchWord && string.Compare(currWord, txt, ignoreCase) == 0)
						{
							// Make sure the next character is a space so we can match the whole word
							if (txtCmd.Text.Length > ch + 1 && string.Compare(" ", txtCmd.Text[ch + 1].ToString()) == 0)
								foundMatch = true;
						}
						else if (!matchWord)
						{
							foundMatch = true;
						}

						if (foundMatch)
						{ // Matched all the text, get out
							foundMatch = false;
							matchesFound++;

							if (select)
							{
								txtCmd.SelectionStart = startIdx;
								txtCmd.SelectionLength = index;
								return 0;
							}

						}

						index = 0;
					}
				}
				else
				{
					matching = false;
					index = 0;
					startIdx = 0;
				}
			}

			index = 0;
			currWord = "";

			// Continue from beginning
			for (int ch = 0; ch < txtCmd.SelectionStart + txtCmd.SelectionLength; ch++)
			{
				if (string.Compare(" ", txtCmd.Text[ch].ToString()) == 0)
					currWord = "";
				else
					currWord += txtCmd.Text[ch].ToString();

				if (string.Compare(txtCmd.Text[ch].ToString(), txt[index].ToString(), ignoreCase) == 0)
				{
					index++; // We found a match so increment to the next letter in the text we're comparing

					if (!matching)
					{ // Do this on the first char match
						startIdx = ch;
						matching = true;
					}

					if (index == txt.Length)
					{
						if (matchWord && string.Compare(currWord, txt, ignoreCase) == 0)
						{
							// Make sure the next character is a space so we can match the whole word
							if (txtCmd.Text.Length > ch + 1 && string.Compare(" ", txtCmd.Text[ch + 1].ToString()) == 0)
							foundMatch = true;
						}
						else if (!matchWord)
						{
							foundMatch = true;
						}

						if (foundMatch)
						{ // Matched all the text, get out
							foundMatch = false;
							matchesFound++;

							if (select)
							{
								txtCmd.SelectionStart = startIdx;
								txtCmd.SelectionLength = index;
								return 0;
							}

						}

						index = 0;
					}
				}
				else
				{
					matching = false;
					index = 0;
					startIdx = 0;
				}
			}

			return matchesFound;
		}

		private void setFieldsReadOnly(bool readOnly)
		{
			cbxServer.Enabled = !readOnly;
			txtEnv.ReadOnly = readOnly;
			txtUser.ReadOnly = readOnly;
			txtPswd.ReadOnly = readOnly;
            chkAutoCommit.Enabled = readOnly;
		}

        private void RaiseConnectionEventFromThread()
        {
            if (InvokeRequired)
            {
                Invoke(new MethodInvoker(RaiseConnectionEventFromThread));
            }
            else
            {
                RaiseConnectionEvent();
            }
        }

        private void RaiseConnectionEvent()
        {
            ServerInfo info = serverCache.GetLoginInfo(cbxServer.Text);
            if (info == null)
            {
                info = new ServerInfo(cbxServer.Text, cbxServer.Text, txtEnv.Text);
                info.UserID = txtUser.Text;
                info.Password = txtPswd.Text;
            }
            switch (connectionState)
            {
                case ConnectionState.Connected:
                    {
                        Connected(this, new ConnectionEventArgs(ConnectionEventState.Connected, info));
                    }
                    break;
                case ConnectionState.FailedConnect:
                case ConnectionState.DisConnected:
                    {
                        Connected(this, new ConnectionEventArgs(ConnectionEventState.Disconnected, info));
                    }
                    break;
            }
        }

        private void UpdateUIFromThread()
        {
            if (InvokeRequired)
            {
                Invoke(new MethodInvoker(UpdateUIFromThread));
            }
            else
            {
                UpdateUI();
            }
        }

        private void UpdateUI()
        {
            if (connectionState == ConnectionState.Connected)
            {
                lblSrv.Text = "Connected";
                setFieldsReadOnly(true);
                btnConnect.Text = "Disconnect";
                TabPage.Text = cbxServer.Text;
                EnableProgressBar(false);
                txtCmd.Focus();
            }
            else if (connectionState == ConnectionState.Connecting)
            {
                setFieldsReadOnly(true);
                btnConnect.Text = "Cancel";
                lblSrv.Text = "Disconnected";
                TabPage.Text = "Connecting...";
            }
            else
            {
                setFieldsReadOnly(false);
                btnConnect.Text = "Connect";
                lblSrv.Text = "Disconnected";
                TabPage.Text = "Not Connected";
                EnableProgressBar(false);
                TabPage.Appearance.ForeColor = Color.Empty;
                txtCmd.BackColor = Color.Empty;
            }
        }

        // This function is used to assign colors when configuring servers using dlxconfig
        private string LoadColorFromSrvConfig(string strSrvConfig)
        {
            string env = strSrvConfig;
            string[] args = env.Trim().Split(':');
            string colorArg = "";

            // Pick up color argument if exsit
            foreach (string arg in args)
            {
                if (arg.StartsWith("COLOR"))
                {
                    colorArg = arg;
                    break;
                }
            }

            if (!String.IsNullOrEmpty(colorArg))
            {
                string colorNam = colorArg.Substring(colorArg.IndexOf('=') + 1);
                if (!String.IsNullOrEmpty(colorNam.Trim()))
                {
                    // Set as tab header's background color
                    Color color = Color.FromName(colorNam.Trim());
                    TabPage.Appearance.BackColor = color;
                    TabPage.Appearance.BackColor2 = Color.White;
                    TabPage.SelectedAppearance.BackColor = color;
                    TabPage.ClientAreaAppearance.BackColor = SystemColors.Control;
                }

                // Get rid of color property, we don't like to see it.
                env = env.Remove(env.IndexOf(colorArg), colorArg.Length).Trim(':');
            }

            return env;
        }

		#endregion Private Methods

		#region Event Handlers
		
		private void btnConnect_Click(object sender, EventArgs e)
		{
            if (connectionState == ConnectionState.Connected)
            {
                DisConnect();
            }
            else if (connectionState == ConnectionState.Connecting)
            {
                _suppressMessages = true;
                CancelConnection();
            }
            else
            {
                _suppressMessages = false;
                workerThread = new Thread(DoConnect);

                ServerInfo info = null;
                if (serverCache != null)
                {
                    info = serverCache.GetLoginInfo(cbxServer.Text);
                }
                if (info == null)
                {
                    info = new ServerInfo(cbxServer.Text.Trim(), cbxServer.Text.Trim(), txtEnv.Text.Trim());
                }
                info.Environment = txtEnv.Text.Trim();
                info.UserID = txtUser.Text;
                info.Password = txtPswd.Text;
                info.Host = info.Host.Trim();

                workerThread.Start(info);
                SetConnectingState();
            }
		}

		private void txtCmd_KeyDown(object sender, KeyEventArgs e)
        {
            commandKeyDown(sender, e);
        }

        private void txtCmd_TextChanged(object sender, EventArgs e)
        {
            textChanged(sender, e);
        }

		private void txtEnv_Enter(object sender, EventArgs e)
		{
			txtEnv.SelectAll();
		}

		private void txtUser_Enter(object sender, EventArgs e)
		{
			txtUser.SelectAll();
		}

		private void txtPswd_Enter(object sender, EventArgs e)
		{
			txtPswd.SelectAll();
		}		

		private void cbxServer_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.KeyCode == Keys.Enter)
				btnConnect_Click(this, null);
		}
        private void cbxServer_KeyUp(object sender, KeyEventArgs e)
        {
            if (e.Control)
            {
                if (e.KeyCode == (Keys.C))
                    Clipboard.SetText(cbxServer.SelectedText);
                if (e.KeyCode == (Keys.V))
                    cbxServer.Text = Clipboard.GetText();
            }
        }

		private void txtEnv_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.KeyCode == Keys.Enter)
				btnConnect_Click(this, null);
		}

		private void txtUser_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.KeyCode == Keys.Enter)
				btnConnect_Click(this, null);
		}

		private void txtPswd_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.KeyCode == Keys.Enter)
				btnConnect_Click(this, null);
		}

		private void cbxServer_SelectedIndexChanged(object sender, EventArgs e)
		{
            ServerInfo info = null;
            if (serverCache != null)
            {
                info = serverCache.GetLoginInfo(cbxServer.Text);
            }
            if (info != null)
            {
                txtUser.Text = info.UserID;
                txtPswd.Text = info.Password;
                bool useDefault = false;
                if(ConfigOptions.ContainsKey("chkDefaultEnv") && 
                    ConfigOptions["chkDefaultEnv"].ToString().Equals("True"))
                {
                    useDefault = true;
                }
                if (string.IsNullOrEmpty(info.Environment) && useDefault)
                {
                    txtEnv.Text = DEFAULT_ENVIRONMENT;
                }
                else
                {
                    txtEnv.Text = info.Environment;
                }
            }
            else
            {
                cbxServer.SelectedIndex = -1;
                txtUser.Text = "";
                txtPswd.Text = "";
                txtEnv.Text = DEFAULT_ENVIRONMENT;
            }

            UpdateTabColor();
            service = cbxServer.Text;
		}

		private void txtCmd_Enter(object sender, EventArgs e)
		{
			Focus();
		}

		private void gridView2_CustomRowCellEdit(object sender,
						DevExpress.XtraGrid.Views.Grid.CustomRowCellEditEventArgs e)
		{
			if (e.Column.ColumnType == typeof(DateTime))
			{
				e.RepositoryItem = repItemDateEdit;
			}
		}

		private void chkAutoCommit_CheckedChanged(object sender, EventArgs e)
		{
			mocaClient.AutoCommit = chkAutoCommit.Checked;
		}

		private void tmenuCut_Click(object sender, EventArgs e)
        {
            string clip = "";

            if (txtCmd.SelectedText != null)
                clip = txtCmd.SelectedText;

            if (!string.IsNullOrEmpty(clip))
            {
                Clipboard.SetText(clip);
                txtCmd.SelectedText = "";
            }
        }

		private void tmenuCopy_Click(object sender, EventArgs e)
		{
			string clip = "";

			if (txtCmd.SelectedText != null)
				clip = txtCmd.SelectedText;

			if (!string.IsNullOrEmpty(clip))
				Clipboard.SetText(clip);
		}

		private void tmenuPaste_Click(object sender, EventArgs e)
		{
			string clip = Clipboard.GetText();

			if (!string.IsNullOrEmpty(clip))
				txtCmd.SelectedText = clip;
		}

		private void gridMst_KeyDown(object sender, KeyEventArgs e)
		{
			// This doesn't work, not sure why, trying for cut/copy via ctrl-x and ctrl-c
			if ((ModifierKeys & Keys.Control) == Keys.Control)
			{
				if (e.KeyCode == Keys.C || e.KeyCode == Keys.X)
				{
                    CopySelectedGridRowsToClipboard();
				}
			}
		}

		private void gridContextMenuCopyCell_Click(object sender, EventArgs e)
		{
			string clip = "";

			if (gridView2.FocusedValue != null)
				clip = gridView2.FocusedValue.ToString();

			if (!string.IsNullOrEmpty(clip))
				Clipboard.SetText(clip);
		}

		private void gridContextMenuCopyRow_Click(object sender, EventArgs e)
		{
			string clip = "";

			DataRow row = gridView2.GetDataRow(gridView2.FocusedRowHandle);

			foreach (DataColumn col in row.Table.Columns)
			{
				if (clip.Length <= 0)
				{
					clip = row[col].ToString();
				}
				else
				{
					clip += "," + row[col];
				}
			}

			if (!string.IsNullOrEmpty(clip))
				Clipboard.SetText(clip);
		}

        private void gridContextMenuCopyPublish_Click(object sender, EventArgs e)
        {
            string clip = "";

            int[] rowHandles = gridView2.GetSelectedRows();
            DataRow row;

            foreach (int handle in rowHandles)
            {
                if (string.IsNullOrEmpty(clip))
                {
                    clip = "publish data where ";
                }
                else
                {
                    clip += ("\n&\npublish data where ");
                }

                row = gridView2.GetDataRow(handle);
                bool startRow = true;

                foreach (DataColumn col in row.Table.Columns)
                {
                    if (!startRow)
                    {
                        clip += " and ";
                    }

                    clip += (col.ColumnName + " = '" + MCSDefines.ReplaceSingleQuotes(row[col].ToString()) + "'");
                    startRow = false;
                }
            }

            if (!string.IsNullOrEmpty(clip))
            {
                Clipboard.SetText(clip);
            }
        }

        private void gridContextMenu_Opening(object sender, CancelEventArgs e)
        {
            if (gridView2.SelectedRowsCount == 0)
            {
                // No need to show the grid menu when no rows selected
                e.Cancel = true;
                return;
            }

            if (gridView2.SelectedRowsCount > 1)
            {
                gridContextMenu.Items[2].Enabled = true;
                gridContextMenu.Items[2].Visible = true;
            }
            else
            {
                gridContextMenu.Items[2].Enabled = false;
                gridContextMenu.Items[2].Visible = false;
            }
        }

        private void copyAllRowsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            CopySelectedGridRowsToClipboard();
        }

        private void serverCache_CacheUpdated(object sender, EventArgs e)
        {
            cbxServer.Invoke(new refreshServerDelegate(refreshServerCombo));
        }

        void TabPage_SubObjectPropChanged(Infragistics.Shared.PropChangeInfo propChange)
        {
            // Check to see if the tabpage's appearance is changing
            if (propChange.Source == TabPage && propChange.PropId is UltraTabControlPropertyId &&
                ((UltraTabControlPropertyId)propChange.PropId) == UltraTabControlPropertyId.Appearance)
            {
                overrideColor = true;
                tabColor = TabPage.Appearance.BackColor;
            }
        }

        private delegate void refreshServerDelegate();
        private delegate void updateCurrentServerDelegate(string newServer);

        private void refreshServerCombo()
        {
            string currServer = cbxServer.Text;
            cbxServer.DataSource = serverCache.GetServerHistory(historyLabel);
            if (string.IsNullOrEmpty(currServer))
            {
                cbxServer.SelectedIndex = -1;
            }
            else
            {
                cbxServer.SelectedItem = currServer;
            }
        }

        private void updateCurrentServer(string newServer)
        {
            cbxServer.Text = newServer;
        }

		#endregion Event Handlers

        
	}
}
