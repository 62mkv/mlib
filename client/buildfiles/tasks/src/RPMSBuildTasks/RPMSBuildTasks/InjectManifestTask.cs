using System;
using System.IO;
using System.Runtime.InteropServices;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A MSBuild Task that injects a manifest section into an
    /// existing executable.
    /// </summary>
    public class InjectManifestTask : Task
    {

        [DllImport("kernel32.dll", SetLastError = true)]
        static extern int BeginUpdateResource(string pFileName, bool bDeleteExistingResources);

        [DllImport("kernel32.dll", SetLastError = true)]
        static extern int EndUpdateResource(IntPtr hUpdate, bool fDiscard);

        [DllImport("kernel32.dll", SetLastError = true)]
        static extern int UpdateResource(IntPtr hUpdate, uint lpType, uint lpName, ushort wLanguage, byte[] lpData, uint cbData);

        #region Private Fields
        private ITaskItem manifestFile;
        private ITaskItem executableFile;
        #endregion

        #region Properties

        /// <summary>
        /// Gets or sets the executable file to be injected.
        /// </summary>
        /// <value>The executable file.</value>
        [Required]
        public ITaskItem ExecutableFile
        {
            get { return executableFile; }
            set { executableFile = value; }
        }

        /// <summary>
        /// Gets or sets the manifest file to inject.
        /// </summary>
        /// <value>The manifest file.</value>
        [Required]
        public ITaskItem ManifestFile
        {
            get { return manifestFile; }
            set { manifestFile = value; }
        }

        #endregion

        ///<summary>
        ///Executes the task.
        ///</summary>
        ///
        ///<returns>
        ///true if the task successfully executed; otherwise, false.
        ///</returns>
        ///
        public override bool Execute()
        {
            Log.LogMessage("Injecting manifest: '{0}' into assembly: '{1}'", 
                            manifestFile.ItemSpec, executableFile.ItemSpec);
            
            //Do the files exist ?
            if (!File.Exists(executableFile.ItemSpec))
            {
                Log.LogError("Manifest Injector: Executable '{0}' does not exist !", executableFile.ItemSpec);
                return false;
            }
            if (!File.Exists(manifestFile.ItemSpec))
            {
                Log.LogError("Manifest Injector: Manifest File '{0}' does not exist !", manifestFile.ItemSpec);
                return false;
            }

            //Inject the manifest:
            bool bolReturn = Inject(executableFile.ItemSpec, manifestFile.ItemSpec, 1);
            if (bolReturn)
            {
                Log.LogMessage("Injection succeeded!");
                return true;
            }
            else
            {
                Log.LogError("Manifest Injector: Injection Failed!");
                return false;
            }
        }

        #region Private Methods
        /// <summary>
        /// Injects the specified assembly path with the manifest file.
        /// </summary>
        /// <param name="assemblyPath">The assembly path.</param>
        /// <param name="manifestPath">The manifest path.</param>
        /// <param name="resourceName">Name of the resource.</param>
        /// <returns>true if successful; otherwise false.</returns>
        private bool Inject(string assemblyPath, string manifestPath, uint resourceName)
        {
            byte[] manifestByteArray;
            bool result = false;
            FileStream manifestStream = null;
            BinaryReader manifestReader = null;
            IntPtr updatePointer = IntPtr.Zero;

            try
            {
                // Read in the manifest as an array of byest to be injected to the 
                manifestStream = new FileStream(manifestPath, FileMode.Open, FileAccess.Read);
                manifestReader = new BinaryReader(manifestStream);
                manifestByteArray = manifestReader.ReadBytes((int)manifestStream.Length);

                // Begin the injection process
                updatePointer = (IntPtr)BeginUpdateResource(assemblyPath, false);

                if (updatePointer == IntPtr.Zero)
                {
                    // Throws an exception with a specific failure HRESULT value if no pointer is returned
                    Marshal.ThrowExceptionForHR(Marshal.GetHRForLastWin32Error());
                }

                // The second argument, 24 (RT_MANIFEST), specifies that the resource is a manifest.  Details are at
                // http://msdn.microsoft.com/library/default.asp?url=/library/en-us/winui/winui/windowsuserinterface/resources/introductiontoresources/resourcereference/resourcetypes.asp
                if (UpdateResource(updatePointer, 24, resourceName, 0, manifestByteArray, (uint)manifestByteArray.Length) != 1)
                {
                    // Throws an exception with a specific failure HRESULT value if the resulting update does not return 1
                    Marshal.ThrowExceptionForHR(Marshal.GetHRForLastWin32Error());
                }
            }
            catch (Exception ex)
            {
                Log.LogMessage("An exception occured while injecting the manifest: {0}", ex.Message);
                result = true;
            }
            finally
            {
                if (updatePointer != IntPtr.Zero)
                {
                    // Finalize the update
                    EndUpdateResource(updatePointer, result);
                }
                if (manifestReader != null)
                {
                    manifestReader.Close();
                }
                if (manifestStream != null)
                {
                    manifestStream.Close();
                }
            }

            return !result;
        }

        #endregion

    }
}

