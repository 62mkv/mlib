using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Reflection;
using System.Threading;
using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// An internal call-back sequence used for returning data to the client
    /// </summary>
    internal delegate void InternalCallBack(IMocaConnection sender, InternalCallBackEventArgs args);

    /// <summary>
    /// An event arguments base class for calling back between items
    /// </summary>
    internal class InternalCallBackEventArgs : EventArgs
    {
        #region Private Fields

        /// <summary>
        /// The callback to call when the command is complete
        /// </summary>
        private readonly Stack<InternalCallBack> callBacks = new Stack<InternalCallBack>();
        private readonly IInternalCommand command;
        private readonly SynchronizationContext syncContext;
        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="InternalCallBackEventArgs"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        public InternalCallBackEventArgs(IInternalCommand command)
        {
            this.command = command;
            syncContext = SynchronizationContext.Current;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="T:System.EventArgs"/> class.
        /// </summary>
        /// <param name="command">The command being executed.</param>
        /// <param name="nextCallers">The next callers.</param>
        public InternalCallBackEventArgs(IInternalCommand command, params InternalCallBack[] nextCallers)
            : this(command)
        {
            if (nextCallers != null)
            {
                foreach (InternalCallBack callBack in nextCallers)
                {
                    callBacks.Push(callBack);
                }
            }
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the command being executed.
        /// </summary>
        /// <value>The command.</value>
        public IInternalCommand Command
        {
            [DebuggerStepThrough]
            get { return command; }
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Adds a call back to the list.
        /// </summary>
        /// <param name="callBack">The call back.</param>
        public void AddCallBack(InternalCallBack callBack)
        {
            callBacks.Push(callBack);
        }

        /// <summary>
        /// Invokes the next command callback in the list. 
        /// </summary>
        public void InvokeCommandCallback(IMocaConnection sender)
        {
            //Raise callback method
            if (callBacks.Count > 0)
            {
                InternalCallBack nextCallback = callBacks.Pop();
                nextCallback.Invoke(sender, this);
            }
            else
            {
                CallBackOriginalCaller();
            }
        }
        #endregion

        #region Private Methods
        /// <summary>
        /// Calls the back original caller.
        /// </summary>
        private void CallBackOriginalCaller()
        {
            Delegate handler = command.CommandDelegate;
            object[] args = command.GetDelegateParameters();

            if (handler == null || args == null) return;

            if (syncContext != null)
            {
                syncContext.Send(delegate(object del)
                                 {
                                     try
                                     {
                                         ((Delegate)del).DynamicInvoke(args);
                                     }
                                     catch (TargetInvocationException)
                                     {
                                         //Log Message
                                     }
                                     catch(InvalidAsynchronousStateException)
                                     {
                                         //Log Message
                                     }
                                 }, handler);
            }
            else
            {
                try
                {
                    handler.DynamicInvoke(args);
                }
                catch (TargetInvocationException)
                {
                    //Log Message
                }
            }
        }

        #endregion
    }
}