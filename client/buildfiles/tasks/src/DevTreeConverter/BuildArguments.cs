using System;
using System.IO;

namespace DevTreeConverter
{
    class BuildArguments
    {
        private string productName;
        private string directoryAlias;
        private string productRootPath;
        private bool removeMakeFiles = false;
        private bool noPromptMode = false;
        private bool masterFileOnly = false;
        private bool createBuildFiles = true;


        public string ProductName
        {
            get { return productName; }
            set { productName = value.ToUpper().Trim(); }
        }

        public string DirectoryAlias
        {
            get { return directoryAlias; }
            set { directoryAlias = value.ToUpper().Trim(); }
        }

        public string ProductRootPath
        {
            get { return productRootPath; }
            set
            {
                productRootPath = Path.GetFullPath(value);
            }
        }


        public bool RemoveMakeFiles
        {
            get { return removeMakeFiles; }
            set { removeMakeFiles = value; }
        }

        public bool IsDirectoryAliasValid()
        {
            return !String.IsNullOrEmpty(directoryAlias) &&
                   Directory.Exists(DirectoryAliasPath);
        }

        public bool IsProductRootValid()
        {
            return !String.IsNullOrEmpty(productRootPath) &&
                   Directory.Exists(productRootPath);
        }

        public string DirectoryAliasPath
        {
            get { return Environment.GetEnvironmentVariable(directoryAlias); }
        }

        public bool NoPromptMode
        {
            get { return noPromptMode; }
            set { noPromptMode = value; }
        }

        public bool MasterFileOnly
        {
            get { return masterFileOnly; }
            set
            {
                masterFileOnly = value;

                if (value)
                {
                    createBuildFiles = false;
                    removeMakeFiles = false;
                }

            }
        }

        public bool CreateBuildFiles
        {
            get { return createBuildFiles; }
            set { createBuildFiles = value; }
        }

        public bool StartPathIsProductRoot
        {
            get
            {
                if (IsDirectoryAliasValid() && IsProductRootValid())
                {
                    DirectoryInfo alias = new DirectoryInfo(DirectoryAliasPath);
                    DirectoryInfo root = new DirectoryInfo(productRootPath);

                    return alias.FullName.Equals(root.FullName, StringComparison.InvariantCultureIgnoreCase);

                }
                return false;
            }
        }
    }
}
