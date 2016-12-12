using System;
using NUnit.Framework;
using RedPrairie.MSBuild.Tasks.Versioning;

namespace RPMSBuildTasks.Tests
{
    /// <summary>
    /// Unit tests for the <see cref="ConfigFileVersionedItem"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>03/14/2008</date>
    [TestFixture]
    public class ConfigFileVersionedItemTests
    {
        /// <summary>
        /// Tests creation of a new instance of the ConfigFileVersionedItem class
        /// </summary>
        [Test]
        public void NewConfigFileVersionedItemTest()
        {
            ConfigFileVersionedItem configItem = new ConfigFileVersionedItem();

            Assert.AreEqual(ProjectTypes.ConfigFile, configItem.ProjectType);
        }

        /// <summary>
        /// Tests the ConfigFileVersionedItem class with all options defined
        /// </summary>
        [Test]
        public void ConfigFileVersionedItemTest()
        {
            Version testVersion = new Version(2007,1,0,0);
            string fileName = "DigitalLogistix";
            string groupName = "MCS";

            ConfigFileVersionedItem configItem = new ConfigFileVersionedItem(fileName, testVersion, groupName);

            Assert.AreEqual(groupName, configItem.GroupName);
            Assert.AreEqual(fileName, configItem.FileName);
            Assert.AreEqual("CFG", configItem.FileExtension);
            Assert.AreEqual(testVersion, configItem.VersionInfo); 
            Assert.AreEqual(string.Format("{0}.config", fileName), configItem.ItemID);
            Assert.AreEqual(fileName, configItem.ClassID); 
            Assert.AreEqual(fileName, configItem.FileNameNoExtension);
            Assert.AreEqual("1", configItem.NeedFramework); 
            Assert.AreEqual(ProjectTypes.ConfigFile, configItem.ProjectType);
        }
    }
}