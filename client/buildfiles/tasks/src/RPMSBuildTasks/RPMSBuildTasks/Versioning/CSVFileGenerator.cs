using System;
using System.Collections.Generic;
using System.IO;

namespace RedPrairie.MSBuild.Tasks.Versioning
{
    /// <summary>
    /// A helper class used to generate the CSV files
    /// </summary>
    public class CSVFileGenerator
    {
        #region Constants
        private const string COMP_VER_HEAD = "base_prog_id,comp_maj_ver,comp_min_ver,comp_bld_ver,comp_rev_ver,comp_file_nam,comp_prog_id,comp_typ,comp_file_ext,comp_need_fw,lic_key,grp_nam";
        #endregion

        #region Private Fields
        private List<VersionedItem> items = new List<VersionedItem>();
        private string outputPath = null;
        private string fileName = null;
        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="CSVFileGenerator"/> class.
        /// </summary>
        public CSVFileGenerator()
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="CSVFileGenerator"/> class.
        /// </summary>
        /// <param name="outputPath">The output path.</param>
        /// <param name="fileName">The name of the file.</param>
        public CSVFileGenerator(string outputPath, string fileName)
        {
            this.fileName = fileName;
            this.outputPath = outputPath;
        }

        #endregion

        #region Public Properties

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
        /// Gets or sets the list of version items to write out.
        /// </summary>
        /// <value>The version items.</value>
        public List<VersionedItem> Items
        {
            get { return items; }
            set { items = value; }
        }

        /// <summary>
        /// Gets or sets the output path of the CSV file.
        /// </summary>
        /// <value>The output path.</value>
        public string OutputPath
        {
            get { return outputPath; }
            set { outputPath = value; }
        }

        /// <summary>
        /// Gets the final file output path.
        /// </summary>
        /// <value>The file path.</value>
        public string FilePath
        {
            get { return CreateFilePath(); }
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Writes the file.
        /// </summary>
        public void WriteFile()
        {
            string filePath = CreateFilePath();

            using (StreamWriter sw = new StreamWriter(filePath))
            {
                sw.WriteLine(COMP_VER_HEAD);

                foreach (VersionedItem versionItem in items)
                {
                    //"base_prog_id,comp_maj_ver,comp_min_ver,comp_bld_ver,comp_rev_ver,comp_file_nam,comp_prog_id,comp_typ,comp_file_ext,comp_need_fw,lic_key,grp_nam";
                    sw.WriteLine("{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},,{10}",
                                 versionItem.ItemID,
                                 versionItem.VersionInfo.Major,
                                 versionItem.VersionInfo.Minor,
                                 versionItem.VersionInfo.Build,
                                 versionItem.VersionInfo.Revision,
                                 versionItem.FileNameNoExtension,
                                 versionItem.ClassID,
                                 versionItem.ProjectType.CodeName,
                                 versionItem.FileExtension,
                                 versionItem.NeedFramework,
                                 versionItem.GroupName);
                }
                sw.Close();
            }
        }

        #endregion

        #region Private Methods
        /// <summary>
        /// Creates the file path based on the set data.
        /// </summary>
        /// <returns></returns>
        private string CreateFilePath()
        {
            if (String.IsNullOrEmpty(fileName))
                throw new ArgumentException("A file name was not provided for the CSV writer");

            if (!String.IsNullOrEmpty(outputPath) && !Directory.Exists(outputPath))
                throw new IOException(string.Format("CSV output path '{0}' does not exist.", outputPath));

            string fullFile = (!fileName.EndsWith(".csv")) ? string.Format("{0}.csv", fileName) : fileName;

            return (!String.IsNullOrEmpty(outputPath)) ? Path.Combine(outputPath, fullFile) : fullFile;
        }
        #endregion
    }
}