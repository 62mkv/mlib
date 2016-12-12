using System;
using RedPrairie.MSBuild.Tasks.Versioning;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A task for versioning resource files
    /// </summary>
    public class VersionExternalDllsTask : VersionResourcesTask
    {
        /// <summary>
        /// Creates a new version item.
        /// </summary>
        /// <param name="fileName">Name of the file.</param>
        /// <param name="versionInfo">The version information.</param>
        /// <returns>
        /// A new <see cref="VersionedItem"/> class.
        /// </returns>
        protected override VersionedItem CreateVersionItem(string fileName, Version versionInfo)
        {
            VersionedItem item = new VersionedItem();
            item.FileName = fileName;
            item.ProjectType = ProjectTypes.DLLImport;
            item.ItemID = item.FileNameNoExtension;
            item.ClassID = item.FileNameNoExtension;
            item.FrameworkNeeded = true;
            item.GroupName = groupName;
            item.VersionInfo = GetFileVersion(fileName);

            return item;
        }
    }
}
