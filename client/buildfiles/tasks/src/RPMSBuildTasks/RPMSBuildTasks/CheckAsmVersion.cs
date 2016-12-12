using System;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;
using System.Xml;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// An extended task that checks and bumps the assembly version based on the file information.
    /// </summary>
    public class CheckAsmVersion : Task
    {
        #region Private Fields
        private ITaskItem[] assemblyTags;
        private string version;
        private bool forceBump;
        private bool noFixCheck;
        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the list of assembly tags 
        /// that can be replaced in the AssemblyInfo file.
        /// </summary>
        /// <value>The assembly tag collection as a <see cref="ITaskItem"/> array.</value>
        public ITaskItem[] AssemblyTags
        {
            get { return assemblyTags; }
            set { assemblyTags = value; }
        }

        /// <summary>
        /// Gets or sets a value indicating whether a version bump is forced.
        /// </summary>
        /// <value><c>true</c> if a version bump is forced; otherwise, <c>false</c>.</value>
        public bool ForceBump
        {
            get { return forceBump; }
            set { forceBump = value; }
        }

        /// <summary>
        /// Gets or sets a value indicating whether to skip the fix check. This
        /// is used to maintain legacy "cut" behavior.
        /// </summary>
        /// <value><c>true</c> to skip the fix check; otherwise, <c>false</c>.</value>
        public bool NoFixCheck
        {
            get { return noFixCheck; }
            set { noFixCheck = value; }
        }

        /// <summary>
        /// Gets or sets the version to set when cutting a new version.
        /// If version is not set, the existing version is checked and bumped.
        /// </summary>
        /// <value>The version to use with a cut.</value>
        public string Version
        {
            get { return version; }
            set { version = value; }
        }

        #endregion

        #region Public Methods
        ///<summary>
        ///Executes the task.
        ///</summary>
        ///<returns>
        ///true if the task successfully executed; otherwise, false.
        ///</returns>
        public override bool Execute()
        {
            //Get the assembly path
            string assemblyPath = GetAssemblyInfoPath();
            if (assemblyPath == null)
                return false;

            Log.LogMessage(MessageImportance.Low, "AssemblyInfo Path: {0}", assemblyPath);

            //If a version is specified, check to see the assembly start matches that version.
            Version newVersion = null;
            if (!String.IsNullOrEmpty(version))
            {
                newVersion = Utils.GetFileVersion(version);
                if (newVersion.Major == 1)
                {
                    Log.LogError("The product's TaggedVersion file does not contain a valid version. Passed version: {0}", version);
                    return false;
                }
                
                Log.LogMessage(MessageImportance.Low, "Tagged version is: {0}", newVersion);
            }

            //A cut is being performed
            if (noFixCheck)
            {
                Log.LogMessage(MessageImportance.Low, "Performing major version cut");
                return ChangeVersion(newVersion, assemblyPath, false, false);
            }


            //Force a version bump
            if (forceBump)
            {
                Log.LogMessage(MessageImportance.High, "Version bump is being forced!");
                return ChangeVersion(newVersion, assemblyPath, true, true);
            }
            
            //Check to see if one is needed
            Log.LogMessage(MessageImportance.Low, "Performing version bump check");
            return CheckBumpVersion(newVersion, assemblyPath);
        }
        #endregion

        #region Private Methods

        /// <summary>
        /// Bumps the assembly version to the next number.
        /// </summary>
        /// <param name="newVersion">The new version. If <c>null</c>, the version is bumped.</param>
        /// <param name="assemblyInfoPath">The assembly info path.</param>
        /// <param name="bumpRevision">if set to <c>true</c> bump the revision number.</param>
        /// <param name="replaceTags">if set to <c>true</c> replace the copyrght and other info tags.</param>
        /// <returns></returns>
        private bool ChangeVersion(Version newVersion, string assemblyInfoPath, bool bumpRevision, bool replaceTags)
        {
            Regex assemblyRegex = new Regex("\\[assembly: *Assembly(File)?Version *\\(\"([0-9\\.\\*]*)\"\\)\\]");
            string assemblyFile = File.ReadAllText(assemblyInfoPath);
            bool errored = false;
            
            assemblyFile = assemblyRegex.Replace(assemblyFile,
                delegate(Match match)
                {
                    if (match.Groups.Count < 3 && newVersion == null)
                    {
                        Log.LogError("The Assembly property in the AssemblyInfo.cs file is blank, please enter a version.");
                        errored = true;
                        return match.Groups[0].Value;
                    }

                    Version parsedVersion = Utils.GetFileVersion(match.Groups[2].Value);

                    if (parsedVersion.Major == 1 && newVersion == null)
                    {
                        Log.LogError("The Assembly property in the AssemblyInfo.cs file has never been set. Please enter a version.");
                        errored = true;
                        return match.Groups[0].Value;
                    }

                    //Check and bump the revision if a new value is not specified
                    int revision = (newVersion != null &&
                                    (newVersion.Major != parsedVersion.Major ||
                                     newVersion.Minor != parsedVersion.Minor))
                                       ? newVersion.Revision
                                       : parsedVersion.Revision;

                    if (bumpRevision)
                        revision++;

                    int majorVersion = (newVersion != null) 
                                           ? newVersion.Major 
                                           : parsedVersion.Major;

                    int minorVersion = (newVersion != null)
                                           ? newVersion.Minor
                                           : parsedVersion.Minor;

                    if (majorVersion == parsedVersion.Major &&
                        minorVersion == parsedVersion.Minor &&
                        revision == parsedVersion.Revision)
                        return match.Groups[0].Value;

                    Log.LogMessage("Assembly{0} version changed to {1}.{2}.{3}.{4}",
                                         match.Groups[1].Value,
                                         majorVersion, minorVersion,
                                         parsedVersion.Build, revision);

                    return string.Format("[assembly: Assembly{0}Version(\"{1}.{2}.{3}.{4}\")]",
                                         match.Groups[1].Value,
                                         majorVersion, minorVersion,
                                         parsedVersion.Build, revision);


                });

            //Replace the tags if they exist
            if (replaceTags)
                assemblyFile = ReplaceTags(assemblyFile, null);
            
            File.WriteAllText(assemblyInfoPath, assemblyFile);

            return !errored;
        }

        /// <summary>
        /// Checks whether or not a version bump is needed.
        /// </summary>
        /// <param name="newVersion">The new version.</param>
        /// <param name="assemblyInfoPath">The assembly info path.</param>
        /// <returns><c>true</c> if the operation is successful; otherwise <c>false</c>.</returns>
        private bool CheckBumpVersion(Version newVersion, string assemblyInfoPath)
        {
            DateTime dateProject = DateTime.MinValue;
            DateTime dateAsm = DateTime.MinValue;
            try
            {
                Log.LogMessage("Directory: {0}", Directory.GetCurrentDirectory());
                Log.LogMessage("Assembly Path: {0}", assemblyInfoPath);

                string projectCommit = GetCommit(null).Trim();
                string asmCommit = GetCommit(assemblyInfoPath).Trim();
                dateProject = GetLogDateTime(projectCommit);
                dateAsm = GetLogDateTime(asmCommit);

                bool bumpVersion = false;
                if (dateProject != DateTime.MinValue && dateAsm != DateTime.MinValue &&
                    dateProject > dateAsm)
                {
                    Log.LogMessage("Different dates identified, Project:{0} Assembly:{1}",
                                   dateProject, dateAsm);

                    bumpVersion = true;
                }
                else
                {
                    Log.LogMessage("Project is up-to-date, Project:{0} Assembly:{1}",
                               dateProject, dateAsm);
                }

                //The bump will only be performed if it's out of date but this will
                //implicitly perform a "cut" each time to ensure the major version matches.
                return ChangeVersion(newVersion, assemblyInfoPath, bumpVersion, true);
            }
            catch (Exception e)
            {
                Log.LogError(
                    "Could not get Git version info:\r\nProject Date:{0}\r\nAsm Date:{1}\r\nError:{2}",
                    dateProject, dateAsm, e.Message);

                return false;
            }
        }

        /// <summary>
        /// Gets the assembly info path from either the project root or the properties folder.
        /// </summary>
        /// <returns></returns>
        private string GetAssemblyInfoPath()
        {
            string path = GetFilePathCorrectCase("AssemblyInfo.cs");
            if (path != null)
                return path;

            path = GetFilePathCorrectCase(@".\Properties\AssemblyInfo.cs");
            if (path != null)
                return path;
            
            Log.LogError(
                "Could not find the AssemblyInfo.cs file in the project directory or Properties sub-directory");
            return null;
        }

        /// <summary>
        /// Gets the full path for the given path with the correct case. This overcomes
        /// an issue where the .NET APIs are all case insensitive. Note the case is only
        /// accurate for the file name not the entire path (if the directory case is wrong).
        /// </summary>
        /// <returns>The full path with correct case for the file name or null if no file exists at the given path.</returns>
        private string GetFilePathCorrectCase(string path)
        {
            FileInfo info = new FileInfo(path);
            string foundPath = null;
            if (info.Exists)
            {
                // Hack to basically query the file again to get the correct case
                foundPath = Directory.GetFiles(info.Directory.FullName, info.Name)[0];
            }

            return foundPath;
        }

        /// <summary>
        /// Gets the Git commit date from a hash.
        /// </summary>
        /// <param name="commit">The hash of the Git commit to get the date from.</param>
        /// <returns>A <see cref="DateTime"/> or <see cref="DateTime.MinValue"/> if nothing is found.</returns>
        private DateTime GetLogDateTime(string commit)
        {
            // Since % is a reserved character in Windows we have to escape it with ^
            string args = String.Format("show -s --oneline --format=^%ci {0}", commit);
            string output = "";
            string err = "";
            string cd = Directory.GetCurrentDirectory();

            ProcessStartInfo info = new ProcessStartInfo("git.exe", args);

            info.CreateNoWindow = true;
            info.RedirectStandardOutput = true;
            info.RedirectStandardError = true;
            info.UseShellExecute = false;
            info.WorkingDirectory = cd;

            Process proc = new Process();
            proc.StartInfo = info;
            proc.Start();
            output = proc.StandardOutput.ReadToEnd();
            err = proc.StandardError.ReadToEnd();
            proc.WaitForExit();

            Log.LogMessage("Directory: {0}", cd);
            Log.LogMessage("Executing: {0} -> {1}", "git.exe " + args, output);
            if (proc.ExitCode == 0)
            {
                DateTime result;

                // trim garbage returned by git
                output = output.Trim().TrimStart('^');

                if (DateTime.TryParse(output, out result))
                {
                    return result;
                }
                else
                {
                    Log.LogError("Could not parse date from Git commit. Date: {0}", output);
                }
            }
            else
            {
                Log.LogError("Could not run git successfully. Arguments: {0}. Output: {1}. Err: {2}.", args, output, err);
            }

            return DateTime.MinValue;
        }

        /// <summary>
        /// Gets the latest Git commit hash of a path. 
        /// If the path represents an object, then the last hash of the object is returned.
        /// If the path represents a directory, then the latest commit hash of all contents is returned. 
        /// If the path is empty or null, then latest commit hash of current directory is returned.
        /// </summary>
        /// <param name="path">The path to log</param>
        /// <returns>The commit hash</returns>
        private string GetCommit(string path)
        {
            string dir = null;
            string parentDir = null;

            // if null is passed that means we are looking for information about current directory
            // to do this, we must back the process up one directory and ask about the name
            if (path == null || path.Equals(""))
            {
                string cd = Directory.GetCurrentDirectory();
                int bSlashIndex = cd.LastIndexOf('\\');
                parentDir = cd.Substring(0, bSlashIndex);
                dir = cd.Substring(bSlashIndex + 1, cd.Length - bSlashIndex - 1);
            }
            else
            {
                dir = path;
            }

            string args = string.Format("rev-list HEAD -1 -- {0}", dir);
            string output = "";
            string err = "";

            ProcessStartInfo info = new ProcessStartInfo("git.exe", args);
            info.CreateNoWindow = true;
            info.RedirectStandardOutput = true;
            info.RedirectStandardError = true;
            info.UseShellExecute = false;

            if (parentDir != null)
            {
                info.WorkingDirectory = parentDir;
            }

            Process proc = new Process();
            proc.StartInfo = info;
            proc.Start();
            output = proc.StandardOutput.ReadToEnd();
            err = proc.StandardError.ReadToEnd();
            proc.WaitForExit();

            Log.LogMessage("Executing: {0} -> {1}", "git.exe " + args, output);
            if (proc.ExitCode == 0)
            {
                return output;
            }
            else
            {
                Log.LogError("Could not run git successfully. Arguments: {0}. Output: {1}. Err: {2}.", args, output, err);
            }

            return output;
        }

        /// <summary>
        /// Replaces the tags for pre-defined items if necessary. 
        /// If <paramref name="assemblyFile"/> is null; the path sepcified in 
        /// <paramref name="assemblyInfoPath"/> is used to load in the file's content.
        /// </summary>
        /// <param name="assemblyFile">The assembly file content as a string.</param>
        /// <param name="assemblyInfoPath">The assembly info path.</param>
        /// <returns>The resulting text of the assembly file</returns>
        private string ReplaceTags(string assemblyFile, string assemblyInfoPath)
        {
            bool noFile = (assemblyFile == null && !String.IsNullOrEmpty(assemblyInfoPath));

            //Exit if none are found
            if (assemblyTags != null && assemblyTags.Length > 0)
            {
                //Check for no file and load it to the text
                if (noFile)
                    assemblyFile = File.ReadAllText(assemblyInfoPath);

                foreach (ITaskItem item in assemblyTags)
                {
                    try
                    {
                        string findText = item.ItemSpec;
                        string replacement = null;
                        bool isRegex = false;
                        bool assemblyLine = false;
                        bool replaceExisting = false;

                        foreach (string metaName in item.MetadataNames)
                        {
                            if (metaName.Equals("replace", StringComparison.InvariantCultureIgnoreCase))
                            {
                                replacement = item.GetMetadata(metaName);
                            }
                            else if (metaName.Equals("regex", StringComparison.InvariantCultureIgnoreCase))
                            {
                                isRegex = true;
                            }
                            else if (metaName.Equals("assemblyLine", StringComparison.InvariantCultureIgnoreCase))
                            {
                                assemblyLine = true;
                                replaceExisting = !String.IsNullOrEmpty(item.GetMetadata(metaName));
                            }
                        }

                        if (!String.IsNullOrEmpty(findText) && replacement != null)
                        {
                            if (assemblyLine && !replaceExisting)
                            {
                                Log.LogMessage(MessageImportance.Low,
                                               "Item is marked as an empty assembly line replace");

                                replacement = string.Format("[assembly: {0}(\"{1}\")]", findText, replacement);
                                findText = string.Format("[assembly: {0}(\"\")]", findText);
                            }

                            if (assemblyLine && replaceExisting)
                            {
                                Log.LogMessage(MessageImportance.Low,
                                               "Item is marked as a matching assembly line replace");

                                replacement = string.Format("[assembly: {0}(\"{1}\")]", findText, replacement);
                                findText = string.Format("\\[assembly: ?{0}\\(\".+\"\\)\\]", findText);
                                isRegex = true;
                            }

                            Log.LogMessage(MessageImportance.Low,
                                           "Finding '{0}' to replace with '{1}' as {2}.", findText,
                                           replacement, (isRegex ? "a regex" : "plain text"));
                            if (isRegex)
                            {

                                Regex reReplace = new Regex(findText);
                                assemblyFile = reReplace.Replace(assemblyFile, replacement);
                            }
                            else
                            {
                                assemblyFile = assemblyFile.Replace(findText, replacement);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Log.LogErrorFromException(new Exception("Assembly tag replacement failed.", e), true);
                    }
                }

                if (noFile)
                    File.WriteAllText(assemblyInfoPath, assemblyFile);
            }

            return assemblyFile;
        }

        #endregion
    }
}
