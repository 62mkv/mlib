using System;
using System.IO;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using RedPrairie.MSBuild.Tasks.Versioning;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A base class for version generation items.
    /// </summary>
    public abstract class VersionTaskBase : Task
    {
        #region Protected Fields
        /// <summary>
        /// The actual CVS file generator for the task
        /// </summary>
        protected CSVFileGenerator fileWriter = new CSVFileGenerator();
        /// <summary>
        /// The files being manipulated by the task
        /// </summary>
        protected ITaskItem[] files;
        /// <summary>
        /// The name of the project
        /// </summary>
        protected string projectName;
        /// <summary>
        /// The name of the group creating this data
        /// </summary>
        protected string groupName; 
        #endregion

        #region Public Properties
        /// <summary>
        /// Gets or sets the files that will be processed by the task.
        /// </summary>
        /// <value>The files.</value>
        [Required]
        public ITaskItem[] Files
        {
            get { return files; }
            set { files = value; }
        }

        /// <summary>
        /// Gets the output file.
        /// </summary>
        /// <value>The output file.</value>
        [Output]
        public ITaskItem OutputFile
        {
            get { return new TaskItem(Path.GetFullPath(fileWriter.FilePath)); }
        }

        /// <summary>
        /// Gets or sets the name of the group owning the data.
        /// </summary>
        /// <value>The name of the group.</value>
        [Required]
        public string GroupName
        {
            get { return groupName; }
            set { groupName = value; }
        }

        /// <summary>
        /// Gets or sets the name of the project.
        /// </summary>
        /// <value>The name of the project.</value>
        [Required]
        public string ProjectName
        {
            get { return projectName; }
            set { projectName = value; }
        } 
        #endregion

        #region Public Methods
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
            //Setup the file writer
            fileWriter.FileName = projectName;

            if (files != null && files.Length > 0)
            {
                if (!GetFilesToWrite())
                    return false;
            }
            else
            {
                Log.LogMessage("No resources exist to version, generating shell file");
            }

            try
            {
                fileWriter.WriteFile();
            }
            catch (SystemException e)
            {
                Log.LogErrorFromException(e, false);
                return false;
            }

            return true;
        } 
        #endregion

        #region Protected Methods

        /// <summary>
        /// Gets the list of files to write. 
        /// This method is only called if the passed <see cref="files"/> field has items.
        /// </summary>
        protected abstract bool GetFilesToWrite();


        /// <summary>
        /// Gets the file version from the newly built assembly.
        /// </summary>
        /// <returns></returns>
        protected Version GetFileVersion(string fileName)
        {
            try
            {
                return Utils.GetFileAssemblyVersion(fileName);
            }
            catch (FileNotFoundException)
            {
                Log.LogWarning("Cannot find assembly '{0}' in directory: '{1}'. Version info may be incorrect.", fileName,
                               Path.GetFullPath(fileName));
                return new Version(1, 0, 0, 0);
            }
        }

        #endregion
    }
}
