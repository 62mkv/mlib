using System;
using NUnit.Framework;
using RedPrairie.MSBuild.Tasks.Versioning;

namespace RPMSBuildTasks.Tests
{
    /// <summary>
    /// Unit tests for the <see cref="ImageVersionedItem"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>03/14/2008</date>
    [TestFixture]
    public class ImageVersionedItemTests
    {
        /// <summary>
        /// Tests creation of a new instance of the ImageVersionedItem class
        /// </summary>
        [Test]
        public void NewImageVersionedItemTest()
        {
            ImageVersionedItem configItem = new ImageVersionedItem();

            Assert.AreEqual(ProjectTypes.ResourceFile, configItem.ProjectType);
        }

        /// <summary>
        /// Tests the ImageVersionedItem class with all options defined
        /// </summary>
        [Test]
        public void ImageVersionedItemTest()
        {
            Version testVersion = new Version(2007,1,0,0);
            string fileName = "PROCESS.ico";
            string groupName = "MCS";

            ImageVersionedItem configItem = new ImageVersionedItem(fileName, testVersion, groupName);

            Assert.AreEqual(groupName, configItem.GroupName);
            Assert.AreEqual(fileName, configItem.FileName);
            Assert.AreEqual("ico", configItem.FileExtension);
            Assert.AreEqual(testVersion, configItem.VersionInfo); 
            Assert.AreEqual("process", configItem.ItemID);
            Assert.AreEqual("PROCESS", configItem.ClassID);
            Assert.AreEqual("PROCESS", configItem.FileNameNoExtension);
            Assert.AreEqual("0", configItem.NeedFramework); 
            Assert.AreEqual(ProjectTypes.ResourceFile, configItem.ProjectType);
        }
    }
}