using System;

namespace RedPrairie.MSBuild.Tasks.Versioning
{
    /// <summary>
    /// An override class for an image versioned item
    /// </summary>
    public class ImageVersionedItem : VersionedItem
    {
        #region Constructor
        /// <summary>
        /// Initializes a new instance of the <see cref="ImageVersionedItem"/> class.
        /// </summary>
        public ImageVersionedItem()
        {
            projectType = ProjectTypes.ResourceFile;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ImageVersionedItem"/> class.
        /// </summary>
        /// <param name="fileName">Name of the file.</param>
        /// <param name="versionInfo">The version info.</param>
        /// <param name="groupName">Name of the group.</param>
        public ImageVersionedItem(string fileName, Version versionInfo, string groupName): this()
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
            get { return base.FileNameNoExtension.ToLowerInvariant(); }
            set { base.ItemID = value; }
        }

        #endregion
    }
}
