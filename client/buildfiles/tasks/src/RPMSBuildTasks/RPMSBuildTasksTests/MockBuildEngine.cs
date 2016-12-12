using System.Collections;
using System.Text;
using Microsoft.Build.Framework;

namespace RPMSBuildTasksTests.Tests
{
    internal class MockBuildEngine : IBuildEngine
    {
        private readonly bool continueOnError = false;
        private readonly int lineNumberOfTaskNode = 1;
        private readonly int columnNumberOfTaskNode = 1;
        private readonly string projectFileOfTaskNode = "test.cs";
        private readonly StringBuilder logger = new StringBuilder();

        /// <summary>
        /// Gets the log.
        /// </summary>
        /// <value>The log.</value>
        public string Log
        {
            get { return logger.ToString();  }
        }

        private void WriteLine(string message, string category)
        {
            logger.AppendLine(string.Format("{0}: {1}", category, message));
        }

        ///<summary>
        ///Raises an error event to all registered loggers.
        ///</summary>
        ///
        ///<param name="e">The event data.</param>
        public void LogErrorEvent(BuildErrorEventArgs e)
        {
            WriteLine(e.Message, "Error");
        }

        ///<summary>
        ///Raises a warning event to all registered loggers.
        ///</summary>
        ///
        ///<param name="e">The event data.</param>
        public void LogWarningEvent(BuildWarningEventArgs e)
        {
            WriteLine(e.Message, "Warning");
        }

        ///<summary>
        ///Raises a message event to all registered loggers.
        ///</summary>
        ///
        ///<param name="e">The event data.</param>
        public void LogMessageEvent(BuildMessageEventArgs e)
        {
            WriteLine(e.Message, "Message");
        }

        ///<summary>
        ///Raises a custom event to all registered loggers.
        ///</summary>
        ///
        ///<param name="e">The event data.</param>
        public void LogCustomEvent(CustomBuildEventArgs e)
        {
            WriteLine(e.Message, "Custom");
        }

        ///<summary>
        ///Initiates a build of a project file. If the build is successful, the outputs, if any, of the specified targets are returned.
        ///</summary>
        ///
        ///<returns>
        ///true if the build was successful; otherwise, false.
        ///</returns>
        ///
        ///<param name="projectFileName">The name of the project file to build.</param>
        ///<param name="targetOutputs">The outputs of each specified target.</param>
        ///<param name="targetNames">The names of the target in the project to build. Separate multiple targets with a semicolon (;).</param>
        ///<param name="globalProperties">An <see cref="T:System.Collections.IDictionary"></see> of additional global properties to apply to the project. The key and value must be String data types.</param>
        public bool BuildProjectFile(string projectFileName, string[] targetNames,
                                     IDictionary globalProperties, IDictionary targetOutputs)
        {
            return true;
        }

        ///<summary>
        ///Returns true if the ContinueOnError flag was set to true for this particular task in the project file.
        ///</summary>
        ///
        ///<returns>
        ///true if the ContinueOnError flag was set to true for this particular task in the project file.
        ///</returns>
        ///
        public bool ContinueOnError
        {
            get { return continueOnError; }
        }

        ///<summary>
        ///Gets the line number of the task node within the project file that called it.
        ///</summary>
        ///
        ///<returns>
        ///The line number of the task node within the project file that called it.
        ///</returns>
        ///
        public int LineNumberOfTaskNode
        {
            get { return lineNumberOfTaskNode; }
        }

        ///<summary>
        ///Gets the line number of the task node within the project file that called it.
        ///</summary>
        ///
        ///<returns>
        ///The line number of the task node within the project file that called it.
        ///</returns>
        ///
        public int ColumnNumberOfTaskNode
        {
            get { return columnNumberOfTaskNode; }
        }

        ///<summary>
        ///Gets the full path to the project file that contained the call to this task.
        ///</summary>
        ///
        ///<returns>
        ///The full path to the project file that contained the call to this task.
        ///</returns>
        ///
        public string ProjectFileOfTaskNode
        {
            get { return projectFileOfTaskNode; }
        }
    }
}