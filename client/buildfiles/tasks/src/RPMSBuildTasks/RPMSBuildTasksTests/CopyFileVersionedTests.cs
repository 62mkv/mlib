using System.IO;
using System.Text.RegularExpressions;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using NUnit.Framework;
using RedPrairie.MSBuild.Tasks;

namespace RPMSBuildTasksTests.Tests
{
    /// <summary>
    /// Unit tests for the <see cref="CopyFileVersioned"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>03/20/2008</date>
    [TestFixture]
    public class CopyFileVersionedTests
    {
        /// <summary>
        /// Tests the CopyFileVersioned class for a straight through file copy
        /// </summary>
        [Test]
        public void CopyFileUnversionedTest()
        {
            string testFileName = "TestDll.dll";
            CopyFileVersioned task = new CopyFileVersioned();
            task.BuildEngine = new MockBuildEngine();
            task.ProjectType = "Component";
            task.SourceFiles = new ITaskItem[1] { new TaskItem(testFileName) };

            bool result = task.Execute();

            Assert.IsTrue(result);
            Assert.IsNotNull(task.DestinationFiles);
            Assert.AreEqual(1, task.DestinationFiles.Length);
            Assert.AreEqual(testFileName, task.DestinationFiles[0].ItemSpec);
        }

        /// <summary>
        /// Tests the CopyFileVersioned class for a straight through file copy with a specified
        /// output directory.
        /// </summary>
        [Test]
        public void CopyFileUnversionedTestWithOutputDirectory()
        {
            string testFileName = "TestDll.dll";
            string outputPath = @"C:\foo";
            CopyFileVersioned task = new CopyFileVersioned();
            task.BuildEngine = new MockBuildEngine();
            task.ProjectType = "Component";
            task.DestinationFolder = outputPath;
            task.SourceFiles = new ITaskItem[1] { new TaskItem(testFileName) };

            bool result = task.Execute();

            Assert.IsTrue(result);
            Assert.IsNotNull(task.DestinationFiles);
            Assert.AreEqual(1, task.DestinationFiles.Length);
            Assert.AreEqual(Path.Combine(outputPath, testFileName), task.DestinationFiles[0].ItemSpec);
        }

        /// <summary>
        /// Tests the CopyFileVersioned task for versioned file copy.
        /// </summary>
        [Test]
        public void CopyFileVersionedTest()
        {
            string testFileName = @"RedPrairie.MSBuild.Tasks";
            CopyFileVersioned task = new CopyFileVersioned();
            task.BuildEngine = new MockBuildEngine();
            task.ProjectType = "Form";
            task.SourceFiles = new ITaskItem[1] { new TaskItem(string.Format(@".\{0}.dll", testFileName)) };

            bool result = task.Execute();

            Assert.IsTrue(result);
            Assert.IsNotNull(task.DestinationFiles);
            Assert.AreEqual(1, task.DestinationFiles.Length);
            Assert.IsTrue(ContainsVersion(testFileName, task.DestinationFiles[0].ItemSpec));
        }


        /// <summary>
        /// Tests the CopyFileVersioned task for versioned file copy where the assembly doesn't exist.
        /// </summary>
        [Test]
        public void CopyFileVersionedNoExistFailTest()
        {
            string testFileName = "TestDll.dll";
            CopyFileVersioned task = new CopyFileVersioned();
            task.BuildEngine = new MockBuildEngine();
            task.ProjectType = "Form";
            task.SourceFiles = new ITaskItem[1] { new TaskItem(testFileName) };

            bool result = task.Execute();

            Assert.IsFalse(result);
            Assert.IsNotNull(task.DestinationFiles);
            Assert.AreEqual(0, task.DestinationFiles.Length);
        }

        /// <summary>
        /// Determines whether the specified fileName contains version.
        /// </summary>
        /// <param name="baseFileName">The base file name to check for.</param>
        /// <param name="fileName">Name of the file.</param>
        /// <returns>
        /// 	<c>true</c> if the specified fileName contains version; otherwise, <c>false</c>.
        /// </returns>
        private static bool ContainsVersion(string baseFileName, string fileName)
        {
            Regex regex = new Regex(string.Format("{0}[0-9]+_[0-9]+_[0-9]+.dll", Regex.Escape(baseFileName)));

            return regex.Match(fileName).Success;
        }
    }
}