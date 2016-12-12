using System;
using System.IO;
using System.Reflection;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    ///   Locates the product root based on the current directory of the assembly
    ///   and the defined relative path.
    /// </summary>
    public class GetProductRoot : Task
    {
        // Fields
        private ITaskItem _relativeDirectoryPath;
        private ITaskItem _rootDirectoryPath;

        // Properties
        /// <summary>
        ///   Gets or sets the relative directory path for the root from the location
        ///   of the current assembly.
        /// </summary>
        /// <value>The relative directory path.</value>
        [Required]
        public ITaskItem RelativeDirectoryPath
        {
            get { return _relativeDirectoryPath; }
            set { _relativeDirectoryPath = value; }
        }

        /// <summary>
        ///   Sets the root directory path as a path output.
        /// </summary>
        /// <value>The root directory path.</value>
        [Output]
        public ITaskItem RootDirectoryPath
        {
            get { return _rootDirectoryPath; }
            set { _rootDirectoryPath = value; }
        }

        /// <summary>
        ///   When overridden in a derived class, executes the task.
        /// </summary>
        /// <returns>
        ///   true if the task successfully executed; otherwise, false.
        /// </returns>
        public override bool Execute()
        {
            string fullPath =
                Path.GetFullPath(Path.GetDirectoryName(new Uri(Assembly.GetExecutingAssembly().CodeBase).LocalPath));

            if (!Directory.Exists(fullPath))
            {
                Log.LogError("Cannot locate directory defined by path: {0}", new object[] {fullPath});
                return false;
            }

            if ((_relativeDirectoryPath == null) || string.IsNullOrEmpty(_relativeDirectoryPath.ItemSpec))
            {
                _rootDirectoryPath = new TaskItem(fullPath);
                return true;
            }

            string[] strArray = _relativeDirectoryPath.ItemSpec.Split(new[] {'\\'});
            var parent = new DirectoryInfo(fullPath);
            foreach (var pathItem in strArray)
            {
                if (parent == null)
                {
                    return false;
                }
                    
                string path = pathItem.Trim();
                    
                if (string.IsNullOrEmpty(path) || path.Equals("#")) 
                    continue;

                if (path.Equals("##"))
                {
                    parent = parent.Parent;
                }
                else
                {
                    DirectoryInfo[] directories = parent.GetDirectories(path, SearchOption.TopDirectoryOnly);
                    if (directories.Length == 1)
                    {
                        parent = directories[0];
                    }
                }
            }
            if (parent != null)
            {
                _rootDirectoryPath = new TaskItem(parent.FullName);
                return true;
            }

            Log.LogError("Cannot locate directory defined by path: {0}", new object[] { _relativeDirectoryPath });
            return false;
        }
    }
}