using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Text;

namespace DevTreeConverter
{
    class ProjectConverter
    {
        private readonly BuildArguments buildArguments;
        private bool svnEnabled = false;

        /// <summary>
        /// Initializes a new instance of the <see cref="ProjectConverter"/> class.
        /// </summary>
        /// <param name="buildArguments">The build arguments.</param>
        public ProjectConverter(BuildArguments buildArguments)
        {
            this.buildArguments = buildArguments;
        }

        /// <summary>
        /// Converts the project.
        /// </summary>
        public void ConvertProject()
        {
            Dictionary<string, string> tokens = new Dictionary<string, string>();
            tokens.Add("!PRODNAME!", buildArguments.ProductName);
            tokens.Add("!PRODDIR!", buildArguments.DirectoryAlias);

            string clientPath = buildArguments.StartPathIsProductRoot
                                    ? Path.Combine(buildArguments.ProductRootPath, "client")
                                    : buildArguments.ProductRootPath;

            CheckSVNEnabled(clientPath);

            if (!buildArguments.MasterFileOnly)
            {
                if (buildArguments.CreateBuildFiles && buildArguments.StartPathIsProductRoot)
                    AddCoreBuildFiles(tokens);

                Console.WriteLine("\r\nConverting Product Tree");
                ProjectFilterFile projectConverter = new ProjectFilterFile(tokens);
                WalkProductTree(tokens, clientPath, projectConverter); 
            }

            if (buildArguments.StartPathIsProductRoot)
                ConvertRootBuildFile();
        }

        /// <summary>
        /// Converts the root build file to MSBuild and eliminates obsloete targets.
        /// </summary>
        private void ConvertRootBuildFile()
        {
            Console.WriteLine("\r\nUpdating Root Build File");
            string rootMakeFile = Path.Combine(buildArguments.ProductRootPath, "makefile.nt");

            if (!File.Exists(rootMakeFile))
            {
                Console.WriteLine("Root Makefile not found at path '{0}'... skipping", rootMakeFile);
                return;
            }

            string[] removeTargets = new string[2] { "reg:", "unreg:" };
            bool removeLines = false;
            StringBuilder builder = new StringBuilder();


            foreach (string line in File.ReadAllLines(rootMakeFile))
            {
                if (!removeLines)
                {
                    foreach (string removeTarget in removeTargets)
                    {
                        if (line.StartsWith(removeTarget))
                        {
                            Console.WriteLine("     Removing obsloete target '{0}'", removeTarget);
                            builder.AppendLine(string.Format("{0} FRC\r\n", removeTarget));
                            removeLines = true;
                            continue;
                        }
                    }

                    if (line.StartsWith("client:"))
                    {
                        Console.WriteLine("     Replacing 'client' target for MSBuild");
                        builder.AppendLine("client: FRC");
                        builder.AppendLine("\tcd \"$(MAKEDIR)\\client\"");
                        builder.AppendLine("\tmsbuild.exe msbuild.proj /t:install /property:Configuration=release\r\n");

                        removeLines = true;
                    }
                    else if (line.StartsWith("help:") && !removeLines)
                    {
                        Console.WriteLine("     Replacing help message");
                        string dirAlias = buildArguments.DirectoryAlias;
                        
                        builder.AppendLine("help:");
                        builder.AppendLine("\t@echo Creating a Major or Minor Release");
                        builder.AppendFormat("\t@echo ^ ^ ^	   edit ^%{0}^%\\config\\TaggedVersion\r\n", dirAlias);
                        builder.AppendFormat("\t@echo ^ ^ ^     cd ^%{0}^%\\client\r\n", dirAlias);
                        builder.AppendLine(  "\t@echo ^ ^ ^     make cut");
                        builder.AppendFormat("\t@echo ^ ^ ^     cd ^%{0}^%%\r\n", dirAlias);
                        builder.AppendLine(  "\t@echo ^ ^ ^     svn commit");
                        builder.AppendLine(  "\t@echo ^ ^ ^     svn tag (tagname)");
                        builder.AppendLine("\t@echo Creating a Revision or Service Pack Release");
                        builder.AppendFormat("\t@echo ^ ^ ^     edit ^%{0}^%\\config\\TaggedVersion\r\n", dirAlias);
                        builder.AppendFormat("\t@echo ^ ^ ^     cd ^%{0}^%\\client\r\n", dirAlias);
                        builder.AppendLine(  "\t@echo ^ ^ ^     make fix");
                        builder.AppendFormat("\t@echo ^ ^ ^     cd ^%{0}^%\\\r\n", dirAlias);
                        builder.AppendLine(  "\t@echo ^ ^ ^     svn commit");
                        builder.AppendLine(  "\t@echo ^ ^ ^     svn tag (tagname)");
                        
                        removeLines = true;
                    }

                    if (!removeLines)
                        builder.AppendLine(line);
                }
                else
                {
                    if (String.IsNullOrEmpty(line.Trim()))
                        removeLines = false;
                }
            }

            File.WriteAllText(rootMakeFile, builder.ToString());

        }

        /// <summary>
        /// Checks to see if SVN is enabled for the project.
        /// </summary>
        /// <param name="clientPath">The client path.</param>
        private void CheckSVNEnabled(string clientPath)
        {
            DirectoryInfo directoryInfo = new DirectoryInfo(clientPath);

            DirectoryInfo[] svnInfo = directoryInfo.GetDirectories(".svn");
            svnEnabled = (svnInfo != null && svnInfo.Length > 0);
        }

        /// <summary>
        /// Does the SVN operation.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="fileName">Name of the file.</param>
        /// <param name="workingDir">The working dir.</param>
        private string DoSVNOperation(string command, string fileName, string workingDir)
        {
            if (svnEnabled)
            {
                string args = string.Format("{0} {1}", command, fileName);
                ProcessStartInfo info = new ProcessStartInfo("svn.exe", args);

                info.CreateNoWindow = true;
                info.WorkingDirectory = workingDir;
                info.RedirectStandardOutput = true;
                info.RedirectStandardError = true;
                info.UseShellExecute = false;

                Process proc = new Process();
                proc.StartInfo = info;
                proc.Start();
                proc.WaitForExit();

                if (proc.ExitCode != 0)
                {
                    Console.WriteLine("         SVN Operation {0} Errored. Message: {1}", command, proc.StandardError.ReadToEnd());
                }

                return proc.StandardOutput.ReadToEnd();
            }

            return "";
        }

        /// <summary>
        /// Checks the SVN properties for obj and bin directory excudes.
        /// </summary>
        /// <param name="workingDir">The working dir.</param>
        private void CheckSVNProperties(string workingDir)
        {
            if (!svnEnabled) { return; }

            bool needsUpdate = false;
            string[] checkItems = new string[2] { "*obj**", "*bin**" };
            string excludes = DoSVNOperation("propget", "svn:ignore", workingDir);
            StringBuilder builder = new StringBuilder(excludes);

            foreach (string exclude in checkItems)
            {
                if (!excludes.Contains(exclude))
                {
                    builder.AppendLine(exclude);
                    needsUpdate = true;
                }
            }

            if (needsUpdate)
            {
                Console.WriteLine("     Updating SVN Ingore properties for project");
                string tempFileName = "tempPropertiesFile.tmp";
                File.WriteAllText(Path.Combine(workingDir, tempFileName), builder.ToString());
                DoSVNOperation("propset", string.Format(@"svn:ignore -F {0} .\", tempFileName), workingDir);
                File.Delete(Path.Combine(workingDir, tempFileName));
            }
        }

        /// <summary>
        /// Walks the product tree to create project file.
        /// </summary>
        /// <param name="tokens">The global replacement tokens.</param>
        /// <param name="parentDirectory">The parent directory.</param>
        /// <param name="projectConverter">The project converter class.</param>
        /// <returns><c>true</c> if project files were created for this folder, otherwise <c>false</c></returns>
        private bool WalkProductTree(Dictionary<string, string> tokens, string parentDirectory, ProjectFilterFile projectConverter)
        {
            DirectoryInfo directoryInfo = new DirectoryInfo(parentDirectory);
            DirectoryFilterFile dirFile = new DirectoryFilterFile(tokens, directoryInfo);
            bool containsProject = false;

            //Exit if the directiry is hidden, starts with a . (SVN) or is the "buildfiles" directory
            if (directoryInfo.Attributes == FileAttributes.Hidden || directoryInfo.Name.StartsWith(".") ||
                directoryInfo.Name.Equals("buildfiles", StringComparison.InvariantCultureIgnoreCase))
                return false;

            FileInfo[] files = directoryInfo.GetFiles("*.csproj", SearchOption.TopDirectoryOnly);
            if (files != null && files.Length > 0)
            {
                Console.WriteLine("     Detected Project in directory, creating project file");
                containsProject = true;
                projectConverter.ConvertFile(directoryInfo.FullName);
                DoSVNOperation("add", projectConverter.OutputFileName, parentDirectory);
                CheckSVNProperties(parentDirectory);
            }
            else if (directoryInfo.Name.Equals("bin", StringComparison.InvariantCultureIgnoreCase) ||
                     directoryInfo.Name.Equals("templates", StringComparison.InvariantCultureIgnoreCase))
            {
                Console.WriteLine("     Folder is a bin or template directory...skipping");
                containsProject = true;
            }
            else if (directoryInfo.Name.Equals("res", StringComparison.InvariantCultureIgnoreCase))
            {
                Console.WriteLine("     Folder is an image directory");
                FilterFile imageProject = new FilterFile(tokens, "msbuild-image.proj", "msbuild.proj");
                imageProject.ConvertFile(directoryInfo.FullName);
                DoSVNOperation("add", imageProject.OutputFileName, parentDirectory);
                containsProject = true;
            }
            else if (directoryInfo.Name.Equals("help", StringComparison.InvariantCultureIgnoreCase))
            {
                Console.WriteLine("     Folder is a help directory");
                FilterFile helpProject = new FilterFile(tokens, "msbuild-help.proj", "msbuild.proj");
                helpProject.ConvertFile(directoryInfo.FullName);
                DoSVNOperation("add", helpProject.OutputFileName, parentDirectory);
                containsProject = true;
            }
            else
            {
                Console.WriteLine("\r\nCreating build file in directory '{0}'", directoryInfo.Name);
                dirFile.ConvertFile(parentDirectory);
                DoSVNOperation("add", dirFile.OutputFileName, parentDirectory);
            }

            //Check to see if the makefile should be removed
            if (buildArguments.RemoveMakeFiles)
            {
                string makeFilePath = Path.Combine(parentDirectory, "makefile.nt");
                if (File.Exists(makeFilePath))
                {
                    DoSVNOperation("remove", "makefile.nt", parentDirectory);
                    File.Delete(makeFilePath);
                }

                string fixTargetPath = Path.Combine(parentDirectory, "fixtarget.txt");
                if (File.Exists(fixTargetPath))
                {
                    DoSVNOperation("remove", "fixtarget.txt", parentDirectory);
                    File.Delete(fixTargetPath);
                }
            }

            //No special cases have been encountered in the folder
            if (!containsProject)
            {
                string[] directories = Directory.GetDirectories(parentDirectory);
                bool childrenHaveProjects = false;

                if (directories != null && directories.Length > 0)
                {
                    Console.WriteLine("     No projects found in '{0}', getting child directories", directoryInfo.Name);
                    foreach (string directory in directories)
                    {
                        childrenHaveProjects |= WalkProductTree(tokens, directory, projectConverter);
                    }

                    //Remove build file if children don't have projects
                    if (!childrenHaveProjects)
                    {
                        Console.WriteLine("     No projects found under '{0}', removing build file", directoryInfo.Name);
                        dirFile.RemoveFile(parentDirectory);
                        DoSVNOperation("revert", dirFile.OutputFileName, parentDirectory);
                    }
                }
                return childrenHaveProjects;
            }
            else
            {
                return true;
            }
        }

        /// <summary>
        /// Adds the core build files to the client directory.
        /// </summary>
        /// <param name="tokens">The common replacement tokens.</param>
        private void AddCoreBuildFiles(Dictionary<string, string> tokens)
        {
            Console.WriteLine("\r\nCreating Master Build Files");
            Console.WriteLine("Creating Core Directory");
            String outputPath = Path.Combine(buildArguments.ProductRootPath, "client\\buildfiles");
            if (!Directory.Exists(outputPath))
            {
                Console.WriteLine("Creating Build Files Directory: '{0}'", outputPath);
                Directory.CreateDirectory(outputPath);
            }

            DoSVNOperation("add", "buildfiles", Path.Combine(buildArguments.ProductRootPath, "client"));

            string assemblyFileLocation =
                Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);

            foreach (string file in Directory.GetFiles(assemblyFileLocation, "*.targets"))
            {
                FileInfo info = new FileInfo(file);
                TargetFilterFile filterFile = new TargetFilterFile(tokens, file, info.Name);

                Console.WriteLine("     Copying Targets File: '{0}'", info.Name);
                filterFile.ConvertFile(outputPath);
                DoSVNOperation("add", filterFile.OutputFileName, outputPath);
            }
        }
    }
}
