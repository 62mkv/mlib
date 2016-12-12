using System;

namespace RedPrairie.MSBuild.Tasks.Versioning
{
    /// <summary>
    /// An override class of <see cref="VersionedItem"/> with settings
    /// specific to a config file.
    /// </summary>
    public class ConfigFileVersionedItem: VersionedItem
    {
         #region Constructor
        /// <summary>
        /// Initializes a new instance of the <see cref="ImageVersionedItem"/> class.
        /// </summary>
        public ConfigFileVersionedItem()
        {
            projectType = ProjectTypes.ConfigFile;
            frameworkNeeded = true;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ImageVersionedItem"/> class.
        /// </summary>
        /// <param name="fileName">Name of the file.</param>
        /// <param name="versionInfo">The version info.</param>
        /// <param name="groupName">Name of the group.</param>
        public ConfigFileVersionedItem(string fileName, Version versionInfo, string groupName)
            : this()
        {
            this.fileName = fileName;
            this.versionInfo = versionInfo;
            this.groupName = groupName;
        }

        #endregion

        #region Override Properties

        /// <summary>
        /// Gets or sets the class ID (comp_prog_id).
        /// </summary>
        /// <value>The class ID.</value>
        public override string ClassID
        {
            get { return base.FileNameNoExtension; }
            set { base.ClassID = value; }
        }

        /// <summary>
        /// Gets or sets the item ID.
        /// </summary>
        /// <value>The item ID.</value>
        public override string ItemID
        {
            get { return string.Format("{0}.config", fileName); }
            set { base.ItemID = value; }
        }


        /// <summary>
        /// Gets the name of the file without the extension.
        /// </summary>
        /// <value>The name of the file.</value>
        public override string FileNameNoExtension
        {
            get { return fileName; }
        }

        /// <summary>
        /// Gets the file extension.
        /// </summary>
        /// <value>The file extension.</value>
        public override string FileExtension
        {
            get
            {
                return "CFG";
            }
        }

        #endregion
    }
}
