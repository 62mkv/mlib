using System;
using System.Collections.Generic;
using System.Text;

namespace RedPrairie.MCS.WinMSQL
{
	class LoginInfo
	{
		#region Private Variables

		private string host = "";
		private int port = -1;
		private string user = "";
		private string pswd = "";
		private string env = "";

		#endregion Private Variables

		#region Constructors

		public LoginInfo():this(null) {}

		public LoginInfo(string host):this(host, -1){}

		public LoginInfo(string host, int port):this(host, port, ""){}

		public LoginInfo(string host, int port, string user):this(host, port, user, ""){}

		public LoginInfo(string host, int port, string user, string password):this(host, port, user, password, "") {}

		public LoginInfo(string host, int port, string user, string password, string environment)
		{
			this.host = host;
			this.port = port;
			this.user = user;
			this.pswd = password;
			this.env = environment;
		}

		#endregion Constructors

		#region Properties

		public string User
		{
			get
			{
				return user;
			}
			set
			{
				user = value;
			}
		}

		public string Password
		{
			get
			{
				return pswd;
			}
			set
			{
				pswd = value;
			}
		}

		public string Host
		{
			get
			{
				return host;
			}
			set
			{
				host = value;
			}
		}

		public int Port
		{
			get
			{
				return port;
			}
			set
			{
				port = value;
			}
		}

		public string Environment
		{
			get
			{
				return env;
			}
			set
			{
				env = value;
			}
		}

		#endregion Properties
	}
}
