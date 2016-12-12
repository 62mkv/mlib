using System;
using System.IO;

namespace RedPrairie.MSBuild.Tasks.Versioning
{
    /// <summary>
    /// A base class for versioned items 
    /// </summary>
    public class VersionedItem
    {
        #region Protected Fields
        /// <summary>
        /// The class identifier of the versioned item
        /// </summary>
        protected string classID;
        /// <summary>
        /// The file name of the versioned item
        /// </summary>
        protected string fileName;
        /// <summary>
        /// The project type of the versioned item
        /// </summary>
        protected ProjectTypes projectType;
        /// <summary>
        /// Indicates if the framework is needed to run this component
        /// </summary>
        protected bool frameworkNeeded = false;
        /// <summary>
        /// The group name of the component
        /// </summary>
        protected string groupName;
        /// <summary>
        /// The version item identifier
        /// </summary>
        protected string itemID;
        /// <summary>
        /// The current version of the item
        /// </summary>
        protected Version versionInfo = new Version(1, 0, 0, 0);
        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the class ID (comp_prog_id).
        /// </summary>
        /// <value>The class ID.</value>
        public virtual string ClassID
        {
            get { return classID; }
            set { classID = value; }
        }

        /// <summary>
        /// Gets or sets the name of the file.
        /// </summary>
        /// <value>The name of the file.</value>
        public string FileName
        {
            get { return fileName; }
            set { fileName = value; }
        }

        /// <summary>
        /// Gets or sets the name of the file without the extension.
        /// </summary>
        /// <value>The name of the file.</value>
        public virtual string FileNameNoExtension
        {
            get
            {
                if (String.IsNullOrEmpty(fileName))
                    return String.Empty;
                else
                    return Path.GetFileNameWithoutExtension(fileName);
            }
        }

        /// <summary>
        /// Gets the file extension.
        /// </summary>
        /// <value>The file extension.</value>
        public virtual string FileExtension
        {
            get
            {
                if (String.IsNullOrEmpty(fileName))
                {
                    return String.Empty;
                }
                else
                {
                    string extension = Path.GetExtension(fileName);
                    if (extension.StartsWith(".", StringComparison.InvariantCultureIgnoreCase))
                    {
                        extension = extension.Substring(1);
                    }
                    return extension;
                }
            }
        }

        /// <summary>
        /// Gets or sets the type of the file.
        /// </summary>
        /// <value>The type of the file.</value>
        public ProjectTypes ProjectType
        {
            get { return projectType; }
            set { projectType = value; }
        }

        /// <summary>
        /// Gets or sets a value indicating whether the framework is needed.
        /// </summary>
        /// <value><c>true</c> if the framework is needed; otherwise, <c>false</c>.</value>
        public bool FrameworkNeeded
        {
            get { return frameworkNeeded; }
            set { frameworkNeeded = value; }
        }

        /// <summary>
        /// Gets or sets the name of the group.
        /// </summary>
        /// <value>The name of the group.</value>
        public string GroupName
        {
            get { return groupName; }
            set { groupName = value; }
        }

        /// <summary>
        /// Gets or sets the item ID.
        /// </summary>
        /// <value>The item ID.</value>
        public virtual string ItemID
        {
            get { return itemID; }
            set { itemID = value; }
        }

        /// <summary>
        /// Gets the need framework setting (comp_need_fw).
        /// </summary>
        /// <value>The need framework.</value>
        public string NeedFramework
        {
            get { return frameworkNeeded ? "1" : "0"; }
        }

        /// <summary>
        /// Gets or sets the version info.
        /// </summary>
        /// <value>The version info.</value>
        public Version VersionInfo
        {
            get { return versionInfo; }
            set { versionInfo = value; }
        }

        
        #endregion

    }
}