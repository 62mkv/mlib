using System;
using System.Collections.Generic;
using System.Data;
using System.Runtime.Serialization;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary>
    /// An exception class that encapsulates a MOCA error.
    /// </summary>
    [Serializable]
    public class MocaException : Exception, ISerializable
    {
        #region Private Fields

        private Dictionary<string, Args> _args = new Dictionary<string, Args>();
        private int _errorCode;
        /// <summary>
        /// The results that the command returned even if it failed
        /// </summary>
        protected DataSet _results;

        #endregion

        #region Constructors

        /// <summary>
        /// Creates a new MocaException
        /// </summary>
        /// <param name="errorCode">The error code number</param>
        /// <param name="message">The message of the error</param>
        public MocaException(int errorCode, string message)
            : base(message)
        {
            _errorCode = errorCode;
        }

        /// <summary> 
        /// Wraps another exception in a MOCA exception.
        /// </summary>
        /// <param name="errorCode">
        /// </param>
        /// <param name="message">
        /// </param>
        /// <param name="innerException">
        /// </param>
        public MocaException(int errorCode, string message, Exception innerException)
            : base(message, innerException)
        {
            _errorCode = errorCode;
        }

        /// <summary> 
        /// Wraps another exception in a MOCA exception.
        /// </summary>
        /// <param name="errorCode">The error code of the exception</param>
        public MocaException(int errorCode)
            : base("Error " + errorCode)
        {
            _errorCode = errorCode;
        }

        /// <summary>
        /// Creates a new MocaException
        /// </summary>
        /// <param name="errorCode">The error code number</param>
        /// <param name="message">The message of the error</param>
        /// <param name="results">A <see cref="DataTable"/> of results</param>
        public MocaException(int errorCode, string message, DataTable results)
            : this(errorCode, message)
        {
            _results = new DataSet();
            _results.EnforceConstraints = false;
            _results.Tables.Add(results);
        }

        #endregion

        #region Public Properties

        /// <summary> 
        /// Returns the error code associated with this error.  This error code
        /// will be returned to callers in other languages (C, local syntax, COM,
        /// clients) as an error code.
        /// </summary>
        public virtual int ErrorCode
        {
            get { return _errorCode; }
        }

        /// <summary> 
        /// Gets the argument list associated with this exception condition.
        /// </summary>
        /// <returns> the argument list for error message parameterization. </returns>
        public virtual Args[] ArgList
        {
            get
            {
                if (_args == null)
                    return new Args[0];

                Args[] returnValue = new Args[_args.Count];
                _args.Values.CopyTo(returnValue, 0);

                return returnValue;
            }
        }


        /// <summary> 
        /// Get the results from this exception.  Some commands will
        /// throw exceptions containing results information. Typically,
        /// this usage is restricted to certain well-known subclasses,
        /// such as NotFoundException.
        /// </summary>
        /// <returns> the MocaResults object associated with this exception
        /// condition.
        /// </returns>
        public virtual DataSet Results
        {
            get { return _results; }
            set { _results = value; }
        }


        /// <summary> 
        /// Returns the value of a named exception argument. It is the caller's
        /// responsibility to know the type of the given argument by convention,
        /// or to determine the type through reflection on the returned object.
        /// </summary>
        /// <param name="name">the name of an argument to look up.
        /// </param>
        /// <returns> the argument's value.  If the named argument refers to a "lookup"
        /// argument, only the ID to be looked up will be returned (i.e. no catalog
        /// lookup will occur).  If the named argument has not been set in this
        /// exception, then <code>null</code> is returned.
        /// </returns>
        public virtual Object GetArgValue(string name)
        {
            if (_args == null)
                return null;
            Args arg = _args[name];
            if (arg == null)
                return null;
            return arg.Value;
        }

        #endregion

        #region Public Methods

        /// <summary> 
        /// Add an argument to the error message.  This allows for parameterized,
        /// localized error messages.
        /// </summary>
        /// <param name="name">the name of the argument to add.
        /// </param>
        /// <param name="argValue">the value of the argument to add.
        /// </param>
        public virtual void AddArg(string name, Object argValue)
        {
            if (_args == null)
            {
                _args = new Dictionary<string, Args>();
            }
            _args[name.ToLower()] = new Args(name, argValue, false);
        }

        /// <summary> 
        /// Add an argument to the error message.  The value is used as the key
        /// in an application-specfic lookup.
        /// </summary>
        /// <param name="name">the name of the argument to add.</param>
        /// <param name="argValue">the value of the argument.  This is actually the name of
        /// an application-specific lookup key. </param>
        public virtual void AddLookupArg(string name, string argValue)
        {
            if (_args == null)
            {
                _args = new Dictionary<string, Args>();
            }
            _args[name.ToLower()] = new Args(name, argValue, true);
        }

        /// <summary>
        /// Overrides the default <see cref="System.Exception.ToString()"/> method to do parameter
        /// substitution, based on the arguments given to this exception. 
        /// </summary>
        public override String ToString()
        {
            // Use a replacement strategy to look up replacement values using
            // our Args list.
            IReplacementStrategy strat = new AnonymousClassReplacementStrategy(this);

            return new StringReplacer('^', strat).Translate(base.ToString());
        }

        #endregion

        #region ISerializable Members

        private class AnonymousClassReplacementStrategy : IReplacementStrategy
        {
            private MocaException innerException;

            public AnonymousClassReplacementStrategy(MocaException innerException)
            {
                this.innerException = innerException;
            }

            public MocaException InnerException
            {
                get { return innerException; }
            }

            public virtual string Lookup(string key)
            {
                Args arg = InnerException._args[key];
                if (arg == null)
                    return null;
                Object value = arg.Value;
                if (value == null)
                    return null;
                return Convert.ToString(value);
            }
        }

        #endregion

        /// <summary> 
        /// A class describing MOCA exception arguments.
        /// </summary>
        [Serializable]
        public class Args
        {
            #region Private Fields

            private string _name;
            private Object _value;
            private bool _lookup;

            #endregion

            #region Contstructor

            /// <summary>
            /// Initializes a new instance of the <see cref="Args"/> class.
            /// </summary>
            /// <param name="name">The argument name.</param>
            /// <param name="argValue">The argument value.</param>
            /// <param name="lookup">if set to <c>true</c> the argumet is a lookup.</param>
            internal Args(string name, Object argValue, bool lookup)
            {
                _name = name;
                _value = argValue;
                _lookup = lookup;
            }

            #endregion

            #region Public Methods

            /// <summary>
            /// Gets the argument name.
            /// </summary>
            /// <value>The argument name.</value>
            public virtual string Name
            {
                get { return _name; }
            }

            /// <summary>
            /// Gets the argument value.
            /// </summary>
            /// <value>The argument value.</value>
            public virtual Object Value
            {
                get { return _value; }
            }

            /// <summary>
            /// Gets a value indicating whether this argument is lookup.
            /// </summary>
            /// <value><c>true</c> if it is a lookup; otherwise, <c>false</c>.</value>
            public virtual bool Lookup
            {
                get { return _lookup; }
            }

            #endregion
        }
    }
}