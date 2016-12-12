using System;
using System.Data;
using System.Diagnostics;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// A class that contains information about the currently logged in user and
    /// instance.
    /// </summary>
    public class LoginInfo
    {
        /// <summary>
        /// The user ID
        /// </summary>
        protected string _userName;
        /// <summary>
        /// The locale ID of the user
        /// </summary>
        protected string _localeID;
        /// <summary>
        /// The session key
        /// </summary>
        protected string _sessionKey;
        /// <summary>
        /// The default customization level for the user
        /// </summary>
        protected int _customizationLevel;
        /// <summary>
        /// The default add-on ID for the user
        /// </summary>
        protected string _addOnID;
        /// <summary>
        /// Indicates if a user needs to change their password
        /// </summary>
        protected bool _passwordChangeFlag;
        /// <summary>
        /// The number of days to password expiration
        /// </summary>
        protected int _daysToPasswordExpiration;
        /// <summary>
        /// The password expiration date
        /// </summary>
        protected DateTime _passwordExpirationDate;
        /// <summary>
        /// Indicates if a password expiration warning should be shown
        /// </summary>
        protected bool _showExpirationWarning;
        /// <summary>
        /// Indicates the number of days to account disable
        /// </summary>
        protected int _daysToDisable;
        /// <summary>
        /// Indicates the number of days to license expiration
        /// </summary>
        protected int _daysToLicenseExpiration;
        /// <summary>
        /// Indicates whether a license expiration warning should be shown
        /// </summary>
        protected bool _showLicenseExpirationWarning;
        /// <summary>
        /// Holds the time zone offset value from GMT
        /// </summary>
        protected int _timeZoneOffset;
        /// <summary>
        /// Indicates if a user is a super user
        /// </summary>
        protected bool _isSuper;
        /// <summary>
        /// Indicates if a user was externally authenticated
        /// </summary>
        protected bool _externalAuthentication;

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="LoginInfo"/> class.
        /// </summary>
        public LoginInfo() 
        {}

        /// <summary>
        /// Initializes a new instance of the <see cref="LoginInfo"/> class.
        /// </summary>
        /// <param name="dtLogin">The <see cref="DataTable"/> used to get the login info.</param>
        /// <param name="localeID">The locale ID.</param>
        internal protected LoginInfo(DataTable dtLogin, string localeID)
        {
            try
            {
                DataRow row = dtLogin.Rows[0];
                
                _addOnID = ConnectionUtils.GetStringValue(row, "addon_id", Constants.GlobalDataID);
                _customizationLevel = ConnectionUtils.GetIntValue(row, "cust_lvl");
                _daysToDisable = ConnectionUtils.GetIntValue(row, "pswd_disable");
                _daysToLicenseExpiration = ConnectionUtils.GetIntValue(row, "lic_expir_days");
                _daysToPasswordExpiration = ConnectionUtils.GetIntValue(row, "pswd_expir");
                _externalAuthentication = (ConnectionUtils.GetBooleanValue(row, "ext_ath_flg"));
                _passwordChangeFlag = (ConnectionUtils.GetBooleanValue(row, "pswd_chg_flg"));
                _passwordExpirationDate = ConnectionUtils.GetDateValue(row, "pswd_expir_dte");
                _sessionKey = ConnectionUtils.GetStringValue(row, "session_key");
                _showExpirationWarning = (ConnectionUtils.GetBooleanValue(row, "pswd_warn_flg"));
                _showLicenseExpirationWarning = (ConnectionUtils.GetBooleanValue(row, "lic_expir_warn"));
                _timeZoneOffset = ConnectionUtils.GetIntValue(row, "tim_zn_offset", 0xFFFFFF);
                _userName = ConnectionUtils.GetStringValue(row, "usr_id");
                
                //Override locale id with the new one if set
                _localeID = (!String.IsNullOrEmpty(localeID))
                                ? localeID
                                : ConnectionUtils.GetStringValue(row, "locale_id");
                _isSuper = ConnectionUtils.GetBooleanValue(row, "super_usr_flg");
            }
            catch (Exception ex)
            {
                throw new System.Security.Authentication.AuthenticationException(
                                      "Could not retrieve login information", ex);
            }
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the session key.
        /// </summary>
        /// <value>The session key.</value>
        public string SessionKey
        {
            [DebuggerStepThrough()]
            get { return _sessionKey; }
        }

        /// <summary>
        /// Gets the customization level.
        /// </summary>
        /// <value>The customization level.</value>
        public int CustomizationLevel
        {
            [DebuggerStepThrough()]
            get { return _customizationLevel; }
        }

        /// <summary>
        /// Gets the add on ID.
        /// </summary>
        /// <value>The add on ID.</value>
        public string AddOnID
        {
            [DebuggerStepThrough()]
            get { return _addOnID; }
        }

        /// <summary>
        /// Gets a value indicating whether password should be changed.
        /// </summary>
        /// <value><c>true</c> if the password should change; otherwise, <c>false</c>.</value>
        public bool PasswordChangeFlag
        {
            [DebuggerStepThrough()]
            get { return _passwordChangeFlag; }
        }

        /// <summary>
        /// Gets the days to password expiration.
        /// </summary>
        /// <value>The days to password expiration.</value>
        public int DaysToPasswordExpiration
        {
            [DebuggerStepThrough()]
            get { return _daysToPasswordExpiration; }
        }

        /// <summary>
        /// Gets the password expiration date.
        /// </summary>
        /// <value>The password expiration date.</value>
        public DateTime PasswordExpirationDate
        {
            [DebuggerStepThrough()]
            get { return _passwordExpirationDate; }
        }

        /// <summary>
        /// Gets a value indicating whether and expiration warning should be shown.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if the expiration warning should be shown; otherwise, <c>false</c>.
        /// </value>
        public bool ShowExpirationWarning
        {
            [DebuggerStepThrough()]
            get { return _showExpirationWarning; }
        }

        /// <summary>
        /// Gets the days to disable.
        /// </summary>
        /// <value>The days to disable.</value>
        public int DaysToDisable
        {
            [DebuggerStepThrough()]
            get { return _daysToDisable; }
        }

        /// <summary>
        /// Gets the days to license expiration.
        /// </summary>
        /// <value>The days to license expiration.</value>
        public int daysToLicenseExpiration
        {
            [DebuggerStepThrough()]
            get { return _daysToLicenseExpiration; }
        }

        /// <summary>
        /// Gets a value indicating whether a license expiration warning should be shown.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if a license expiration warning should be shown; otherwise, <c>false</c>.
        /// </value>
        public bool ShowLicenseExpirationWarning
        {
            [DebuggerStepThrough()]
            get { return _showLicenseExpirationWarning; }
        }

        /// <summary>
        /// Gets or sets the locale ID.
        /// </summary>
        /// <value>The locale ID.</value>
        public string LocaleID
        {
            [DebuggerStepThrough()]
            get { return _localeID; }
            set { _localeID = value; }
        }

        /// <summary>
        /// Gets the user ID.
        /// </summary>
        /// <value>The user ID.</value>
        public string UserID
        {
            [DebuggerStepThrough()]
            get { return _userName; }
        }

        /// <summary>
        /// Gets the time zone offset.
        /// </summary>
        /// <value>The time zone offset as a +- offset relative to GMT.</value>
        public int TimeZoneOffset
        {
            [DebuggerStepThrough()]
            get { return _timeZoneOffset; }
        }
        /// <summary>
        /// Gets a value indicating whether the user is a super user.
        /// </summary>
        /// <value><c>true</c> if the user is a super user; otherwise, <c>false</c>.</value>
        public bool IsSuper
        {
            [DebuggerStepThrough()]
            get { return _isSuper; }
        }

        /// <summary>
        /// Gets a value indicating whether the user was externally authenticated.
        /// </summary>
        /// <value><c>true</c> if the user was externally authenticated; otherwise, <c>false</c>.</value>
        public bool ExternalAuthentication
        {
            [DebuggerStepThrough()]
            get { return _externalAuthentication; }
        }

        #endregion
    }
}
