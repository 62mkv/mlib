using System;
using System.IO;
using Microsoft.Build.Framework;
using RedPrairie.MSBuild.Tasks.Versioning;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A task that creates CSV versioning files for .NET projects.
    /// </summary>
    public class VersionProjectTask: VersionTaskBase
    {
        #region Private Fields
        private string assemblyName;
        private string outputPath;
        private string outputType = "dll";
        private string outputLibType;
        private ProjectTypes projectType = ProjectTypes.Unknown;
        private string rootNamespace;
        #endregion

        #region Public Properties

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
        /// Gets or sets the output path of the assembly.
        /// </summary>
        /// <value>The output path.</value>
        [Required]
        public string OutputPath
        {
            get { return outputPath; }
            set { outputPath = value; }
        }

        /// <summary>
        /// Gets or sets the type of the output.
        /// </summary>
        /// <value>The type of the output.</value>
        [Required]
        public string OutputType
        {
            get { return outputLibType; }
            set
            {
                outputLibType = value;

                if (!String.IsNullOrEmpty(outputLibType))
                {
                    if (outputLibType.Equals("WinExe", StringComparison.InvariantCultureIgnoreCase))
                        outputType = "exe";
                }
            }
        }

        /// <summary>
        /// Gets or sets the type of the project.
        /// </summary>
        /// <value>The type of the project.</value>
        [Required]
        public string ProjectType
        {
            get { return projectType.Name; }
            set { projectType = ProjectTypes.Lookup(value); }
        }

        /// <summary>
        /// Gets or sets the root namespace.
        /// </summary>
        /// <value>The root namespace.</value>
        [Required]
        public string RootNamespace
        {
            get { return rootNamespace; }
            set { rootNamespace = value; }
        }

        #endregion

        #region Override Methods
        /// <summary>
        /// Gets the list of files to write. 
        /// This method is only called if the passed <see cref="VersionTaskBase.files"/> field has items.
        /// </summary>
        protected override bool GetFilesToWrite()
        {
            Log.LogMessage(MessageImportance.Low, "Output Path: {0}", Path.GetFullPath(fileWriter.FilePath));

            Version outputVersion = GetFileVersion(Path.Combine(outputPath, string.Format("{0}.{1}", assemblyName, outputType)));
            Log.LogMessage(MessageImportance.Low, "Parsed File Version: {0}", outputVersion);

            for (int i = 0; i < files.Length; i++)
            {
                FilterFileAppend(files[i], outputVersion);
            }

            Log.LogMessage("Generating component versions for {0} items to file '{1}'",
                           fileWriter.Items.Count, fileWriter.FilePath);
            return true;
        }
        #endregion

        #region Private Methods
        /// <summary>
        /// Adds the file to the list modifying parameters based on project type.
        /// </summary>
        /// <param name="className">Name of the class.</param>
        /// <param name="outputVersion">The output version.</param>
        private void AddFile(string className, Version outputVersion)
        {
            string itemID = string.Format("{0}.{1}", projectName, className);

            if (projectType == ProjectTypes.Executable || projectType == ProjectTypes.CoreComponent)
            {
                fileWriter.Items.Add(CreateVersionItem(assemblyName, className, outputVersion));

                //Add the App.config file if it exists
                if (File.Exists("App.config"))
                    fileWriter.Items.Add(new ConfigFileVersionedItem(assemblyName, outputVersion, groupName));
            }
            else
            {
                VersionedItem item = CreateVersionItem(itemID, className, outputVersion);
                
                if (projectType.Equals(ProjectTypes.LoaderModule))
                    item.ItemID = item.ClassID;

                fileWriter.Items.Add(item);    
            }
        }

        /// <summary>
        /// Creates the new version item.
        /// </summary>
        /// <param name="itemID">The item ID (base_prog_id).</param>
        /// <param name="className">Name of the class.</param>
        /// <param name="version">The version.</param>
        /// <returns>A new <see cref="VersionedItem"/> object.</returns>
        private VersionedItem CreateVersionItem(string itemID, string className, Version version)
        {
            VersionedItem item = new VersionedItem();
            item.ItemID = itemID;
            item.VersionInfo = version;
            item.FileName = string.Format("{0}.{1}", assemblyName, outputType);
            item.ClassID = string.Format("{0}.{1}", rootNamespace, className);
            item.FrameworkNeeded = true;
            item.ProjectType = projectType;
            item.GroupName = groupName;

            return item;
        }

        /// <summary>
        /// Adds a new file to the list filtering and replacing data based on specific rules.
        /// </summary>
        /// <param name="taskItem">The task item.</param>
        /// <param name="outputVersion">The output version.</param>
        private void FilterFileAppend(ITaskItem taskItem, Version outputVersion)
        {
            string fileName = Path.GetFileNameWithoutExtension(taskItem.ItemSpec);

            if ((fileWriter.Items.Count == 0 || projectType == ProjectTypes.Form || projectType == ProjectTypes.LoaderModule) &&
                 !fileName.Equals("AssemblyInfo", StringComparison.InvariantCultureIgnoreCase))
            {
                if (taskItem.MetadataCount > 0 && !String.IsNullOrEmpty(taskItem.GetMetadata("SubType")))
                {
                    switch (taskItem.GetMetadata("SubType").Trim())
                    {
                        case "Code":
                            AddFile(fileName, outputVersion);
                            break;
                        case "Component":
                        case "Form":
                        case "UserControl":
                            AddFile(fileName, outputVersion);
                            break;
                    }
                }
                else
                {
                    //Default case if no subtype exists
                    AddFile(fileName, outputVersion);
                }
            }
        }

        #endregion
    }
}
