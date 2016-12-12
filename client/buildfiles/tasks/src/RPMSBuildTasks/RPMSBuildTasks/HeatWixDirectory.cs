using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Xml.Linq;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A task that harvests a WIX directory with all of the 
    /// excluding specified files and ensures that components
    /// are named correctly.
    /// </summary>
    public class HeatWixDirectory : Task
    {
        private const string ID_NAME = "Id";
        private const string NAME_ATTR = "Name";
        private const string SOURCE_DIR = "SourceDir";
        private const string TARGET_DIR = "TARGETDIR";
        private const string WIXNS = "http://schemas.microsoft.com/wix/2006/wi";

        private static readonly MD5CryptoServiceProvider _hashProvider = new MD5CryptoServiceProvider();

        #region Public Properties

        /// <summary>
        /// Gets or sets the name of the component group.
        /// </summary>
        /// <value>The name of the component group.</value>
        public string ComponentGroupName { get; set; }

        /// <summary>
        /// Gets or sets the directory reference ID.
        /// </summary>
        /// <value>The directory reference ID.</value>
        public string DirectoryRefId { get; set; }

        /// <summary>
        /// Gets or sets the directory.
        /// </summary>
        /// <value>The directory.</value>
        [Required]
        public ITaskItem[] Directory { get; set; }

        /// <summary>
        /// Gets or sets the list of exclude files from the harvesting.
        /// </summary>
        /// <value>The exclude files.</value>
        public ITaskItem[] ExcludeFiles { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether to keep empty directories.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if empty directories should be kept; otherwise, <c>false</c>.
        /// </value>
        public bool KeepEmptyDirectories { get; set; }
        
        /// <summary>
        /// Gets or sets the output file.
        /// </summary>
        /// <value>The output file.</value>
        [Required]
        [Output]
        public string OutputFile { get; set; }

        /// <summary>
        /// Gets or sets the preprocessor variable.
        /// </summary>
        /// <value>The preprocessor variable.</value>
        public string PreprocessorVariable { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether to suppress the root directory as a listing.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if the root directory should be suppressed; otherwise, <c>false</c>.
        /// </value>
        public bool SuppressRootDirectory { get; set; }
                    
        /// <summary>
        /// Gets or sets a value indicating whether to use full path of the file as source.
        /// </summary>
        /// <value><c>true</c> if one should use the full path of the file as source; otherwise, <c>false</c>.</value>
        public bool UseFullPath { get; set; }

        #endregion

        #region Overrides of Task

        /// <summary>
        /// When overridden in a derived class, executes the task.
        /// </summary>
        /// <returns>
        /// true if the task successfully executed; otherwise, false.
        /// </returns>
        public override bool Execute()
        {
           

            var directoryRef = new XElement(WixXName("DirectoryRef"),
                                   WixXAttribute(ID_NAME, DirectoryRefId ?? TARGET_DIR));

            var wixElement = new XElement(WixXName("Wix"), 
                                new XElement(WixXName("Fragment"), directoryRef));

            //Process each listed directory
            var componentList = new List<String>();
            foreach (var item in
                Directory.Where(item => !String.IsNullOrEmpty(item.ItemSpec) && System.IO.Directory.Exists(item.ItemSpec)))
            {
                componentList.AddRange(ProcessDirectory(directoryRef, !SuppressRootDirectory, new DirectoryInfo(item.ItemSpec)));
            }

            if (!String.IsNullOrEmpty(ComponentGroupName))
            {
                var componentGroup = new XElement(WixXName("ComponentGroup"),
                                        WixXAttribute(ID_NAME, ComponentGroupName));

                wixElement.Add(new XElement(WixXName("Fragment"), componentGroup));

                //Add the components to the component group);
                foreach (var component in componentList)
                {
                    componentGroup.Add(new XElement(WixXName("ComponentRef"),
                                        WixXAttribute(ID_NAME, component)));
                }
            }

            try
            {
                wixElement.Save(OutputFile);
            }
            catch (SystemException ex)
            {
                Log.LogErrorFromException(ex);
                return false;
            }

            return true;
        }

        #endregion

        /// <summary>
        /// Computes the file id.
        /// </summary>
        /// <param name="fileName">Name of the file.</param>
        /// <returns></returns>
        private static string ComputeFileId(string fileName)
        {
            var hash = _hashProvider.ComputeHash(Encoding.UTF8.GetBytes(fileName));
            var result = new StringBuilder(36);

            for (var i = 0; i < Math.Min(16, hash.Length); i++)
            {
                if (i > 2 && i < 12 && i % 2 == 0)
                {
                    result.Append("-");
                }

                result.AppendFormat("{0:x2}", hash[i]);
            }

            return result.ToString();
        }

        /// <summary>
        /// Processes the directory.
        /// </summary>
        /// <param name="directoryElement">The directory parent element.</param>
        /// <param name="createDirectory">if set to <c>true</c> create the directory.</param>
        /// <param name="item">The directory item.</param>
        /// <returns></returns>
        private IEnumerable<string> ProcessDirectory(XElement directoryElement, bool createDirectory, DirectoryInfo item)
        {
            return ProcessDirectory(directoryElement, createDirectory, item, item);
        }

        /// <summary>
        /// Processes the directory.
        /// </summary>
        /// <param name="directoryElement">The directory parent element.</param>
        /// <param name="createDirectory">if set to <c>true</c> create the directory.</param>
        /// <param name="rootPath">The root path.</param>
        /// <param name="item">The directory item.</param>
        /// <returns></returns>
        private IEnumerable<string> ProcessDirectory(XElement directoryElement, bool createDirectory, DirectoryInfo rootPath, DirectoryInfo item)
        {
            var createdComponents = new List<String>();
            var parentElement = directoryElement;
            

            //Exit if no files and we don't keep empty directories
            var files = item.GetFiles("*.*", SearchOption.TopDirectoryOnly);
            if (files.Length == 0 && !KeepEmptyDirectories)
                return createdComponents;
            
            if (createDirectory)
            {
                var directoryId = MakeUniqueId(item, rootPath);
                var dirElement = new XElement(WixXName("Directory"),
                                              WixXAttribute(ID_NAME, directoryId),
                                              WixXAttribute(NAME_ATTR, item.Name));
                directoryElement.Add(dirElement);
                parentElement = dirElement;
            }

            foreach (var file in files)
            {
                if (IsFileExcluded(file.Name))
                    continue;

                var baseId = MakeUniqueId(file, rootPath);
                var compId = string.Format("C{0}", baseId);
                
                var sourcePath = UseFullPath
                                     ? file.FullName
                                     : GetFilePath(file, rootPath.FullName, true);

                parentElement.Add(new XElement(WixXName("Component"),
                                               WixXAttribute(ID_NAME, compId),
                                               WixXAttribute("Guid", "*"),
                                               new XElement(WixXName("File"),
                                                            WixXAttribute(ID_NAME,  string.Format("F{0}", baseId)),
                                                            WixXAttribute("KeyPath", "yes"),
                                                            WixXAttribute("Source", sourcePath))));
                createdComponents.Add(compId);
            }

            foreach (var directory in item.GetDirectories("*.*", SearchOption.TopDirectoryOnly)
                .Where(directory => directory.Attributes != FileAttributes.Hidden && directory.Name != ".svn"))
            {
                createdComponents.AddRange(ProcessDirectory(parentElement, true, rootPath, directory));
            }


            return createdComponents;
        }

        /// <summary>
        /// Gets the file path.
        /// </summary>
        /// <param name="file">The file.</param>
        /// <param name="rootPath">The root path.</param>
        /// <param name="usePreprocessor">if set to <c>true</c> use preprocessor variables.</param>
        /// <returns></returns>
        private string GetFilePath(FileSystemInfo file, string rootPath, bool usePreprocessor)
        {
            var prefix = usePreprocessor ? SOURCE_DIR : string.Empty;

            if (!String.IsNullOrEmpty(PreprocessorVariable) && usePreprocessor)
            {
                prefix = PreprocessorVariable.Trim();
                
                if (!prefix.StartsWith("var."))
                {
                    prefix = string.Format("var.{0}", prefix);
                }

                prefix = string.Format("$({0})", prefix);
            }
            
            var fullName = file.FullName;
            var rootIndex = fullName.IndexOf(rootPath) + rootPath.Length;
            var relativeDir = fullName.Substring(rootIndex);

            return string.Format(@"{0}\{1}", prefix, relativeDir);
        }

        /// <summary>
        /// Determines whether [is file excluded] [the specified file name].
        /// </summary>
        /// <param name="fileName">Name of the file.</param>
        /// <returns>
        /// 	<c>true</c> if [is file excluded] [the specified file name]; otherwise, <c>false</c>.
        /// </returns>
        private bool IsFileExcluded(string fileName)
        {
            return ExcludeFiles != null && ExcludeFiles.Any(file => String.Equals(file.ItemSpec, fileName, StringComparison.InvariantCultureIgnoreCase));
        }

        /// <summary>
        /// Creates an <see cref="XName"/> item with the specified local name.
        /// </summary>
        /// <param name="localName">The local name of the element.</param>
        /// <returns>The qualified <see cref="XName"/> for the item.</returns>
        private static XName WixXName(string localName)
        {
            return XName.Get(localName, WIXNS);
        }

        /// <summary>
        /// Creates an <see cref="XName"/> item with the specified local name.
        /// </summary>
        /// <param name="localName">The local name of the element.</param>
        /// <param name="value">The value.</param>
        /// <returns>
        /// The qualified <see cref="XName"/> for the item.
        /// </returns>
        private static XAttribute WixXAttribute(string localName, object value)
        {
            return new XAttribute(localName, value);
        }

        /// <summary>
        /// Makes the unique id.
        /// </summary>
        /// <param name="directory">The directory.</param>
        /// <param name="rootPath">The root path.</param>
        /// <returns>A Unique ID for the file.</returns>
        private string MakeUniqueId(DirectoryInfo directory, DirectoryInfo rootPath)
        {
            var rpath = (rootPath.Parent ?? rootPath).FullName;
            var fullName = directory.FullName;
            var rootIndex = fullName.IndexOf(rpath) + rpath.Length;
            var relativeDir = fullName.Substring(rootIndex);
            return MakeId(relativeDir, "D");
        }

        /// <summary>
        /// Makes the unique id.
        /// </summary>
        /// <param name="file">The file.</param>
        /// <param name="rootPath">The root path.</param>
        /// <returns>A Unique ID for the file.</returns>
        private string MakeUniqueId(FileSystemInfo file, DirectoryInfo rootPath)
        {
            var rpath = rootPath.Parent ?? rootPath;
            var path = GetFilePath(file, rpath.FullName, false);
            return MakeId(path, null);
        }

        /// <summary>
        /// Makes the id for the item.
        /// </summary>
        /// <param name="id">The id.</param>
        /// <param name="itemType">The post fix.</param>
        /// <returns>An ID replaced with the item</returns>
        private static string MakeId(string id, string itemType)
        {
            var genId = ComputeFileId(id).Replace("_", ".").Replace("-", ".");
            return String.IsNullOrEmpty(itemType) ? genId : String.Format("{0}{1}", itemType, genId);
        }
    }
}
