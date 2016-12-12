using System;
using System.Diagnostics;
using System.IO;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using NUnit.Framework;
using RedPrairie.MSBuild.Tasks;

namespace RPMSBuildTasksTests.Tests
{
    /// <summary>
    /// Unit tests for the <see cref="VersionProjectTask"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>04/22/2008</date>
    [TestFixture]
    public class VersionProjectTaskTests
    {
        /// <summary>
        /// Tests the VersionProjectTask class with a loader module configuration
        /// </summary>
        [Test]
        public void LoaderProjectTaskTest()
        {
            string outPath = Path.GetTempPath();

            ITaskItem[] compileItems = new TaskItem[2]
                    {new TaskItem("SLHooks.cs"), 
                     new TaskItem("Properties\\AssemblyInfo.cs")};

            VersionProjectTask task = new VersionProjectTask();
            task.BuildEngine = new MockBuildEngine();
            task.AssemblyName = "RedPrairie.Seamles.Hooks";
            task.ProjectType = "LoaderModule";
            task.OutputPath = outPath;
            task.RootNamespace = "RedPrairie.Seamles.Hooks";
            task.OutputType = "dll";
            task.ProjectName = "SLHooks";
            task.GroupName = "SEAMLES";
            task.Files = compileItems;


            task.Execute();

            Assert.IsNotNull(task.OutputFile.ItemSpec);

            string[] file = File.ReadAllLines(task.OutputFile.ItemSpec);
            File.Delete(task.OutputFile.ItemSpec);
            
            Assert.IsNotNull(file);
            Assert.AreEqual(2, file.Length);
        }
    }
}