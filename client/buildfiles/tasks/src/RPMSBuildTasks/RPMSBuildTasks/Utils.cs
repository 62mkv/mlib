using System;
using System.Diagnostics;
using System.Text.RegularExpressions;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A utility class for common functions
    /// </summary>
    public class Utils
    {
        /// <summary>
        /// Gets the file version from the string.
        /// </summary>
        /// <param name="version">The version as a string.</param>
        /// <returns>A <see cref="Version"/> class</returns>
        public static Version GetFileVersion(string version)
        {
            int[] versionNumbers = new int[4] { 1, 0, 0, 0 };

            Regex versionRegex = new Regex(@"([0-9]+)\.([0-9*]+)\.([0-9*]+)(\.([0-9*]+))?(a[0-9]+)?");
            Match matVersion = versionRegex.Match(version);

            if (matVersion.Success)
            {
                if (matVersion.Groups.Count >= 2)
                    versionNumbers[0] = GetNumberFromString(matVersion.Groups[1].Value, 1);

                if (matVersion.Groups.Count >= 3)
                    versionNumbers[1] = GetNumberFromString(matVersion.Groups[2].Value, 0);

                if (matVersion.Groups.Count >= 4)
                    versionNumbers[2] = GetNumberFromString(matVersion.Groups[3].Value, 0);

                if (matVersion.Groups.Count >= 5)
                    versionNumbers[3] = GetNumberFromString(matVersion.Groups[5].Value, 0);
            }

            return new Version(versionNumbers[0], versionNumbers[1], versionNumbers[2], versionNumbers[3]);
        }

        /// <summary>
        /// Gets the file version from the newly built assembly.
        /// </summary>
        /// <returns></returns>
        public static Version GetFileAssemblyVersion(string fileName)
        {
                FileVersionInfo fver = FileVersionInfo.GetVersionInfo(fileName);
                return new Version(fver.FileMajorPart, fver.FileMinorPart,
                                   fver.FileBuildPart, fver.FilePrivatePart);
        }

        /// <summary>
        /// Gets the number from specified string.
        /// </summary>
        /// <param name="value">The string value.</param>
        /// <param name="defaultValue">The defualt value to use.</param>
        /// <returns>A number of the string or the default</returns>
        public static int GetNumberFromString(String value, int defaultValue)
        {
            int parsedValue;
            if (!String.IsNullOrEmpty(value) && Int32.TryParse(value, out parsedValue))
                return parsedValue;
            else
                return defaultValue;
        }
    }
}
