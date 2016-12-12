using System;
using System.IO;
using Microsoft.Build.Framework;
using RedPrairie.MSBuild.Tasks.Versioning;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A task for versioning resource files
    /// </summary>
    public class VersionResourcesTask : VersionTaskBase
    {
        #region Private Fields
        private string version;
        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the revision version.
        /// </summary>
        /// <value>The revision version.</value>
        public string Version
        {
            get { return version; }
            set { version = value; }
        }

        #endregion

        #region Override Methods
        /// <summary>
        /// Gets the list of files to write. 
        /// This method is only called if the passed <see cref="VersionTaskBase.files"/> field has items.
        /// </summary>
        protected override bool GetFilesToWrite()
        {
            Log.LogMessage("Generating resouce file for {0} items to file '{1}'", files.Length, fileWriter.FilePath);
            Log.LogMessage(MessageImportance.Low, "Output Path: {0}", Path.GetFullPath(fileWriter.FilePath));

            Version outputVersion = Utils.GetFileVersion(version);
            Log.LogMessage(MessageImportance.Low, "Parsed File Version: {0}", outputVersion);

            for (int i = 0; i < files.Length; i++)
            {
                fileWriter.Items.Add(CreateVersionItem(files[i].ItemSpec, outputVersion));
            }

            return true;
        }
        #endregion

        #region Private Methods

        /// <summary>
        /// Creates a new version item.
        /// </summary>
        /// <param name="fileName">Name of the file.</param>
        /// <param name="versionInfo">The version information.</param>
        /// <returns>
        /// A new <see cref="VersionedItem"/> class.
        /// </returns>
        protected virtual VersionedItem CreateVersionItem(string fileName, Version versionInfo)
        {
            return new ImageVersionedItem(fileName, versionInfo, groupName);
        }

        #endregion
    }
}
