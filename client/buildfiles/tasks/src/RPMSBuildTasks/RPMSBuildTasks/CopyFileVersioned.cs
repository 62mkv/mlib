using System;
using System.Collections.Generic;
using System.IO;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using RedPrairie.MSBuild.Tasks.Versioning;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// Converts input files to an output list of files that need to be renamed while copying.
    /// The input and output lists are identical, to be used with a copy tasks, sourcefiles
    /// and DestinationFiles arguments.
    /// </summary>
    public class CopyFileVersioned: Task
    {
        #region Private fields
        private ITaskItem[] copiedFiles;
        private string destinationFolder;
        private ProjectTypes projectType;
        private ITaskItem[] sourceFiles;
        #endregion

        #region Public Properties

        /// <summary>
        /// Gets a list of destination files that will be copied to
        /// </summary>
        /// <value>The destination files.</value>
        [Output]
        public ITaskItem[] DestinationFiles
        {
            get { return copiedFiles; }
        }

        /// <summary>
        /// Gets or sets the destination folder to copy the file to.
        /// </summary>
        /// <value>The destination folder.</value>
        public string DestinationFolder
        {
            get { return destinationFolder; }
            set { destinationFolder = value; }
        }

        /// <summary>
        /// Gets or sets the source files to copy.
        /// </summary>
        /// <value>The source files.</value>
        [Required]
        public ITaskItem[] SourceFiles
        {
            get { return sourceFiles; }
            set { sourceFiles = value; }
        }

        /// <summary>
        /// Sets the type of the project.
        /// </summary>
        /// <value>The type of the project.</value>
        [Required]
        public string ProjectType
        {
            set { projectType = ProjectTypes.Lookup(value); }
        }

        #endregion

        #region Override Methods
        /// <summary>
        /// Executes the task.
        /// </summary>
        /// <returns>
        /// true if the task successfully executed; otherwise, false.
        /// </returns>
        public override bool Execute()
        {
            List<string> copyList = new List<string>();
            bool overallStatus = true;

            foreach (ITaskItem file in sourceFiles)
            {
                string destFileName = null;

                if (projectType != ProjectTypes.Form &&
                    projectType != ProjectTypes.LoaderModule)
                {
                    destFileName = Path.GetFileName(file.ItemSpec);
                }
                else
                {
                    Version fileVersion = GetFileVersion(file.ItemSpec);

                    if (fileVersion != null)
                    {
                        //Rename the file based on the version info
                        string fileName = Path.GetFileNameWithoutExtension(file.ItemSpec);
                        string fileExtension = Path.GetExtension(file.ItemSpec);
                        destFileName = string.Format("{0}{1}_{2}_{3}{4}",
                                                            fileName,
                                                            fileVersion.Major,
                                                            fileVersion.Minor,
                                                            fileVersion.Revision,
                                                            fileExtension);
                    }
                    else
                    {
                        overallStatus = false;
                        if (!BuildEngine.ContinueOnError)
                        {
                            copiedFiles = new ITaskItem[0];
                            return overallStatus;
                        }
                    }

                }

                //Add to list if something is found
                if (!String.IsNullOrEmpty(destFileName))
                {
                    if (!String.IsNullOrEmpty(destinationFolder))
                        copyList.Add(Path.Combine(destinationFolder, destFileName));
                    else
                        copyList.Add(destFileName);
                }
            }

            //Create the output list
            copiedFiles = new ITaskItem[copyList.Count];
            for (int i = 0; i < copyList.Count; i++)
                copiedFiles[i] = new TaskItem(copyList[i]);

            return overallStatus;
        }

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
                Log.LogError("Cannot find assembly '{0}' in directory: '{1}'. Version info may be incorrect.", fileName,
                               Path.GetFullPath(fileName));
                return null;
            }
        }
        #endregion
    }
}
