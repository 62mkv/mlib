using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Xml;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// Configures the .csproj.user files with user settings or ones from the registry
    /// Generates a new file if one cannot be found.
    /// </summary>
    public class ConfigureUserFile : Task
    {
        #region Private Meta Data Constants
        private const string DEFAULT_ASSEMBLY = "Dlx.exe";
        private const string HOST = "host";
        private const string PORT = "port";
        private const string USER = "user";
        private const string PASSWORD = "password";
        private const string APPLICATION = "application";
        private const string STARTAPP = "startapp";
        #endregion

        #region Private Fields

        private readonly Dictionary<string, string> items =
            new Dictionary<string, string>(StringComparer.InvariantCultureIgnoreCase);

        private string artifactsDirectory;
        private string assemblyName;
        private ITaskItem settings;
        private string templateUserFile;
        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the artifacts directory.
        /// </summary>
        /// <value>The artifacts directory.</value>
        [Required]
        public string ArtifactsDirectory
        {
            get { return artifactsDirectory; }
            set { artifactsDirectory = value; }
        }

        /// <summary>
        /// Gets or sets the name of the assembly.
        /// </summary>
        /// <value>The name of the assembly.</value>
        [Required]
        public string AssemblyName
        {
            get { return assemblyName; }
            set { assemblyName = value; }
        }

        /// <summary>
        /// Gets or sets the settings tasks that contain config setting.
        /// </summary>
        /// <value>The config settings.</value>
        [Output]
        public ITaskItem Settings
        {
            get { return settings; }
            set { settings = value; }
        }

        /// <summary>
        /// Gets or sets the template user file.
        /// </summary>
        /// <value>The template user file.</value>
        [Required]
        public string TemplateUserFile
        {
            get { return templateUserFile; }
            set { templateUserFile = value; }
        }

        #endregion

        ///<summary>
        ///Executes the task.
        ///</summary>
        ///
        ///<returns>
        ///true if the task successfully executed; otherwise, false.
        ///</returns>
        ///
        public override bool Execute()
        {
            if (!CheckSettings())
                return false;

            return ChangeFiles();
        }

        #region Private Methods

        /// <summary>
        /// Appends the argument.
        /// </summary>
        /// <param name="builder">The builder.</param>
        /// <param name="argumentSwitch">The argument switch.</param>
        /// <param name="argumentName">Name of the argument.</param>
        private void AppendArgument(StringBuilder builder, string argumentSwitch, string argumentName)
        {
            if (items.ContainsKey(argumentName) && !String.IsNullOrEmpty(items[argumentName]))
                builder.AppendFormat(" {0}{1}", argumentSwitch, items[argumentName]);
        }

        /// <summary>
        /// Changes the file settings.
        /// </summary>
        /// <returns><c>true</c> if successful; otherwise <c>false</c>.</returns>
        private bool ChangeFiles()
        {
            string[] files = Directory.GetFiles(@".\", "*.csproj.user", SearchOption.TopDirectoryOnly);

            if (files == null || files.Length == 0)
            {
                files = CreateTemplateUserFiles();

                if (files == null || files.Length == 0)
                    return true;
            }

            foreach (string file in files)
            {
                XmlDocument doc = LoadXmlDocument(file);

                if (doc != null)
                {
                    ChangeNodes(doc, "ReferencePath", artifactsDirectory);

                    ChangeNodes(doc, "StartAction", "Program");
                    ChangeNodes(doc, "StartProgram", Path.Combine(artifactsDirectory, items[STARTAPP]));
                    ChangeNodes(doc, "StartArguments", CreateStartArguments());
                    ChangeNodes(doc, "StartWorkingDirectory", "");

                    doc.Save(file);
                }
            }

            UpdateSettingsTask();

            return true;
        }

        /// <summary>
        /// Creates the command line start arguments.
        /// </summary>
        /// <returns></returns>
        private string CreateStartArguments()
        {
            StringBuilder builder = new StringBuilder("-D");

            AppendArgument(builder, "-A", HOST);
            AppendArgument(builder, "-P", PORT);
            AppendArgument(builder, "-U", USER);
            AppendArgument(builder, "-W", PASSWORD);
            AppendArgument(builder, "-X", APPLICATION);

            return builder.ToString();

        }

        /// <summary>
        /// Creates the template user files.
        /// </summary>
        private string[] CreateTemplateUserFiles()
        {
            string[] files = Directory.GetFiles(@".\", "*.csproj", SearchOption.TopDirectoryOnly);

            if (files != null && files.Length > 0)
            {
                try
                {
                    Log.LogMessage(MessageImportance.Low, "File Template Location: '{0}'", templateUserFile);
                    string fileText = File.ReadAllText(templateUserFile);
                    for (int i = 0; i < files.Length; i++)
                    {
                        Log.LogMessage(MessageImportance.Low, "Creating .user file for file: '{0}'", files[i]);
                        files[i] = Path.ChangeExtension(files[i], ".csproj.user");
                        File.WriteAllText(files[i], fileText);
                    }
                }
                catch (SystemException e)
                {
                    Log.LogMessage(MessageImportance.High, "User file generation failed: {0}", e);
                }
            }

            return files;
        }

        /// <summary>
        /// Changes the nodes to a new value.
        /// </summary>
        /// <param name="doc">The doc.</param>
        /// <param name="nodeName">Name of the node.</param>
        /// <param name="newValue">The new value.</param>
        private void ChangeNodes(XmlDocument doc, string nodeName, string newValue)
        {
            XmlNodeList list = doc.GetElementsByTagName(nodeName);

            if (list == null) return;

            Log.LogMessage(MessageImportance.Low, "Changing {0} Node(s) '{1}' to '{2}'", list.Count,
                           nodeName, newValue);

            foreach (XmlNode node in list)
            {
                node.InnerText = newValue;
            }
        }

        /// <summary>
        /// Checks the settings to make sure meta attributes exist.
        /// </summary>
        /// <returns></returns>
        private bool CheckSettings()
        {
            if (settings == null)
                settings = new TaskItem(artifactsDirectory);

            //Check for specific ones
            CheckSetting(HOST, "Server Host", "localhost");
            CheckSetting(PORT, "Server Port", Environment.GetEnvironmentVariable("MOCA_PORTNUM"));
            CheckSetting(USER, "Login User Name", "super");
            CheckSetting(PASSWORD, "Login Password", "super");
            CheckSetting(APPLICATION, "ApplicationName", "");

            //Check the app startup
            string appName = DEFAULT_ASSEMBLY;
            if (Path.GetExtension(assemblyName).EndsWith("exe", StringComparison.InvariantCultureIgnoreCase))
                appName = assemblyName;

            if (!items.ContainsKey(STARTAPP))
                items.Add(STARTAPP, appName);
            else
                items[STARTAPP] = appName;

            return true;
        }

        /// <summary>
        /// Checks the setting.
        /// </summary>
        /// <param name="settingName">Name of the setting.</param>
        /// <param name="promptName">Name of the prompt.</param>
        /// <param name="defaultValue">The default value.</param>
        private void CheckSetting(string settingName, string promptName, string defaultValue)
        {
            foreach (string metadataName in settings.MetadataNames)
            {
                if (metadataName.Equals(settingName, StringComparison.InvariantCultureIgnoreCase))
                {
                    items.Add(settingName, settings.GetMetadata(metadataName));
                    break;
                }
            }
            
            
            if (!items.ContainsKey(settingName))
            {
                string result = PromptForInput(promptName, defaultValue);

                items.Add(settingName, result);
            }
        }

        /// <summary>
        /// Loads the XML document.
        /// </summary>
        /// <param name="file">The file.</param>
        /// <returns>An XML Document</returns>
        private XmlDocument LoadXmlDocument(string file)
        {
            XmlDocument doc = new XmlDocument();
            try
            {
                doc.Load(file);
            }
            catch (Exception e)
            {
                Log.LogError("Error occured while loading file '{0}'. Error: {1}", file, e);
                return null;
            }
            return doc;
        }

        /// <summary>
        /// Prompts for user input through the console.
        /// </summary>
        /// <param name="prompt">The prompt message.</param>
        /// <param name="defaultValue">The default value.</param>
        /// <returns>The resulting input</returns>
        private static string PromptForInput(string prompt, string defaultValue)
        {
            Console.Write("Enter {0} y=({1}): ", prompt, defaultValue);
            string input =  Console.ReadLine();
            if (input.Equals("y", StringComparison.InvariantCultureIgnoreCase))
                return defaultValue;
            else
                return input;
        }

        /// <summary>
        /// Updates the settings task with the new meta data info.
        /// </summary>
        private void UpdateSettingsTask()
        {
            foreach (KeyValuePair<string, string> item in items)
                settings.SetMetadata(item.Key, item.Value);
        }

        #endregion
    }
}
