using System.Collections.Generic;
using System.IO;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using NUnit.Framework;
using RedPrairie.MSBuild.Tasks;

namespace RPMSBuildTasksTests.Tests
{
    /// <summary>
    /// Unit tests for the <see cref="CheckAsmVersion"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>03/14/2008</date>
    [TestFixture]
    public class CheckAsmVersionTests
    {
        /// <summary>
        /// Tests the CheckAsmVersion class version cut
        /// </summary>
        [Test]
        public void CheckAsmVersionCutTest()
        {
            CopyCleanAssemblyInfo();
            
            CheckAsmVersion task = new CheckAsmVersion();
            task.BuildEngine = new MockBuildEngine();
            task.Version = "2008.1.0a13";
            task.NoFixCheck = true;

            bool result = task.Execute();

            Assert.IsTrue(result);
            CheckFileForLines("[assembly: AssemblyVersion(\"2008.1.0.0\")]");

        }

        /// <summary>
        /// Tests the CheckAsmVersion class version cut
        /// </summary>
        [Test]
        public void CheckAsmVersionCutByFixTest()
        {
            CopyCleanAssemblyInfo();
            ModifyAssemblyInfoToPreviousVersion();

            CheckAsmVersion task = new CheckAsmVersion();
            task.BuildEngine = new MockBuildEngine();
            task.Version = "2009.1.0a13";
            task.ForceBump = true;

            bool result = task.Execute();

            Assert.IsTrue(result);
            CheckFileForLines("[assembly: AssemblyVersion(\"2009.1.0.1\")]");

        }

        /// <summary>
        /// Tests the CheckAsmVersion class version bump
        /// </summary>
        [Test]
        public void CheckAsmVersionBumpForceTest()
        {
            CopyCleanAssemblyInfo();
            ModifyAssemblyInfoToValidVersion();

            CheckAsmVersion task = new CheckAsmVersion();
            task.BuildEngine = new MockBuildEngine();
            task.ForceBump = true;

            bool result = task.Execute();

            Assert.IsTrue(result);
            CheckFileForLines("[assembly: AssemblyVersion(\"2008.1.0.1\")]",
                              "[assembly: AssemblyFileVersion(\"2008.1.0.1\")]");
        }

        /// <summary>
        /// Tests the CheckAsmVersion class version bump when the developer has
        /// not set a correct initial version.
        /// </summary>
        [Test]
        public void CheckAsmVersionInvalidVersionTest()
        {
            CopyCleanAssemblyInfo();
            
            CheckAsmVersion task = new CheckAsmVersion();
            task.BuildEngine = new MockBuildEngine();
            task.ForceBump = true;

            bool result = task.Execute();

            Assert.IsFalse(result);
            CheckFileForLines("[assembly: AssemblyVersion(\"1.0.0.0\")]");
                              
        }

        /// <summary>
        /// Tests the CheckAsmVersion class version with a string replacement using the constant.
        /// </summary>
        [Test]
        public void CheckAsmVersionAssemblyReplaceTest()
        {
            //ForceBump is true since SVN doesn't exist to check this
            CopyCleanAssemblyInfo();
            ModifyAssemblyInfoToValidVersion();

            CheckAsmVersion task = new CheckAsmVersion();
            task.BuildEngine = new MockBuildEngine();
            task.ForceBump = true;
            Dictionary<string, string> metaData = new Dictionary<string, string>();
            metaData.Add("replace", "RedPrairie");
            metaData.Add("assemblyLine", "");
            task.AssemblyTags = new ITaskItem[1] {new TaskItem("AssemblyCompany", metaData)};


            bool result = task.Execute();

            Assert.IsTrue(result);
            CheckFileForLines("[assembly: AssemblyCompany(\"RedPrairie\")]");
        }

        /// <summary>
        /// Modifies the assembly info to valid version.
        /// </summary>
        private static void ModifyAssemblyInfoToValidVersion()
        {
            string fileText = File.ReadAllText(@".\AssemblyInfo.cs");

            fileText = fileText.Replace("[assembly: AssemblyVersion(\"1.0.0.0\")]",
                                        "[assembly: AssemblyVersion(\"2008.1.0.0\")]");
            
            fileText = fileText.Replace("[assembly: AssemblyFileVersion(\"1.0.0.0\")]",
                                        "[assembly: AssemblyFileVersion(\"2008.1.0.0\")]");

            File.WriteAllText(@".\AssemblyInfo.cs", fileText);
        }

        /// <summary>
        /// Modifies the assembly info to valid version.
        /// </summary>
        private static void ModifyAssemblyInfoToPreviousVersion()
        {
            string fileText = File.ReadAllText(@".\AssemblyInfo.cs");

            fileText = fileText.Replace("[assembly: AssemblyVersion(\"1.0.0.0\")]",
                                        "[assembly: AssemblyVersion(\"2008.2.0.0\")]");

            fileText = fileText.Replace("[assembly: AssemblyFileVersion(\"1.0.0.0\")]",
                                        "[assembly: AssemblyFileVersion(\"2008.2.0.0\")]");

            File.WriteAllText(@".\AssemblyInfo.cs", fileText);
        }

        /// <summary>
        /// Checks the file for the given check lines.
        /// </summary>
        /// <param name="checkStrings">The check strings.</param>
        private static void CheckFileForLines(params string[] checkStrings)
        {
            string fileText = File.ReadAllText(".\\AssemblyInfo.cs");
            foreach (string checkString in checkStrings)
            {
                if (!fileText.Contains(checkString))
                    Assert.Fail("Check String '{0}' not found in AssemblyInfo.cs", checkString);
            }
        }

        /// <summary>
        /// Copies a clean assembly info to the working directory.
        /// </summary>
        private static void CopyCleanAssemblyInfo()
        {
            File.Copy(@"..\..\Properties\AssemblyInfo.cs", @".\AssemblyInfo.cs", true);
        }
    }
}