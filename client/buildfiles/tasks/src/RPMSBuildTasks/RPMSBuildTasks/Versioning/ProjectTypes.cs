using System;

namespace RedPrairie.MSBuild.Tasks.Versioning
{
    /// <summary>
    /// A class that contains project type definitions and converts between 
    /// the property for the build system and the comp_typ value for the 
    /// comp_ver data table.
    /// </summary>
    public class ProjectTypes : IEquatable<ProjectTypes>
    {
        #region Private Fields

        private readonly string codeName = "";
        private readonly string name = "";

        #endregion

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="ProjectTypes"/> class.
        /// </summary>
        /// <param name="name">The property name of the project type.</param>
        /// <param name="codeName">The code name of the project type.</param>
        private ProjectTypes(string name, string codeName)
        {
            this.name = name;
            this.codeName = codeName;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the name of the type.
        /// </summary>
        /// <value>The name.</value>
        public string Name
        {
            get { return name; }
        }

        /// <summary>
        /// Gets the name of the code.
        /// </summary>
        /// <value>The name of the code.</value>
        public string CodeName
        {
            get { return codeName; }
        }

        #endregion

        #region Overrides

        /// <summary>
        /// Implements the operator !=.
        /// </summary>
        /// <param name="projectTypes1">The project types1.</param>
        /// <param name="projectTypes2">The project types2.</param>
        /// <returns>The result of the operator.</returns>
        public static bool operator !=(ProjectTypes projectTypes1, ProjectTypes projectTypes2)
        {
            return !Equals(projectTypes1, projectTypes2);
        }

        /// <summary>
        /// Implements the operator ==.
        /// </summary>
        /// <param name="projectTypes1">The project types1.</param>
        /// <param name="projectTypes2">The project types2.</param>
        /// <returns>The result of the operator.</returns>
        public static bool operator ==(ProjectTypes projectTypes1, ProjectTypes projectTypes2)
        {
            return Equals(projectTypes1, projectTypes2);
        }

        /// <summary>
        /// Equalses the specified project types.
        /// </summary>
        /// <param name="projectTypes">The project types.</param>
        /// <returns></returns>
        public bool Equals(ProjectTypes projectTypes)
        {
            if (projectTypes == null) return false;
            return Equals(codeName, projectTypes.codeName) && Equals(name, projectTypes.name);
        }

        /// <summary>
        /// Determines whether the specified <see cref="T:System.Object"/> is equal to the current <see cref="T:System.Object"/>.
        /// </summary>
        /// <param name="obj">The <see cref="T:System.Object"/> to compare with the current <see cref="T:System.Object"/>.</param>
        /// <returns>
        /// true if the specified <see cref="T:System.Object"/> is equal to the current <see cref="T:System.Object"/>; otherwise, false.
        /// </returns>
        /// <exception cref="T:System.NullReferenceException">The <paramref name="obj"/> parameter is null.</exception>
        public override bool Equals(object obj)
        {
            if (ReferenceEquals(this, obj)) return true;
            return Equals(obj as ProjectTypes);
        }

        /// <summary>
        /// Serves as a hash function for a particular type.
        /// </summary>
        /// <returns>
        /// A hash code for the current <see cref="T:System.Object"/>.
        /// </returns>
        public override int GetHashCode()
        {
            return (codeName != null ? codeName.GetHashCode() : 0) + 29*(name != null ? name.GetHashCode() : 0);
        }

        #endregion

        /// <summary>
        /// Gets the resource file type (R).
        /// This should NOT be ever defined as a project type
        /// </summary>
        /// <value>The resource project type.</value>
        public static ProjectTypes ResourceFile
        {
            get { return new ProjectTypes("resource", "R"); }
        }

        /// <summary>
        /// Gets the help file type (U).
        /// </summary>
        /// <value>The help.</value>
        public static ProjectTypes Unknown
        {
            get { return new ProjectTypes(null, null); }
        }

        /// <summary>
        /// Gets the active X interop project type (A).
        /// This should NOT be ever defined as a project type
        /// </summary>
        /// <value>The active X interop.</value>
        public static ProjectTypes ActiveXInterop
        {
            get { return new ProjectTypes("ActiveXInterop", "A"); }
        }

        /// <summary>
        /// Gets the executable project type (E).
        /// </summary>
        /// <value>The executable.</value>
        public static ProjectTypes Executable
        {
            get { return new ProjectTypes("Executable", "E"); }
        }

        /// <summary>
        /// Gets the form project type (F).
        /// </summary>
        /// <value>The form.</value>
        public static ProjectTypes Form
        {
            get { return new ProjectTypes("Form", "W"); }
        }

        /// <summary>
        /// Gets the .NET core component project type (M).
        /// </summary>
        /// <value>The .NET core component.</value>
        public static ProjectTypes CoreComponent
        {
            get { return new ProjectTypes("CoreComponent", "M"); }
        }

        /// <summary>
        /// Gets the .NET component project type (N).
        /// </summary>
        /// <value>The .NET component.</value>
        public static ProjectTypes Component
        {
            get { return new ProjectTypes("Component", "N"); }
        }

        /// <summary>
        /// Gets the DLL import project type (T).
        /// </summary>
        /// <value>The DLL import.</value>
        public static ProjectTypes DLLImport
        {
            get { return new ProjectTypes("DLLImport", "T"); }
        }

        /// <summary>
        /// Gets the config file component type (G).
        /// </summary>
        /// <value>The config file.</value>
        public static ProjectTypes ConfigFile
        {
            get { return new ProjectTypes("ConfigFile", "G"); }
        }

        /// <summary>
        /// Gets the loader module project type (L).
        /// </summary>
        /// <value>The loader module.</value>
        public static ProjectTypes LoaderModule
        {
            get { return new ProjectTypes("LoaderModule", "L"); }
        }

        /// <summary>
        /// Lookups the specified item name and converts to a ProjectType.
        /// </summary>
        /// <param name="itemName">Name of the item.</param>
        /// <returns></returns>
        public static ProjectTypes Lookup(string itemName)
        {
            if (ItemsMatch(ActiveXInterop, itemName))
                return ActiveXInterop;
            else if (ItemsMatch(ActiveXInterop, itemName))
                return Executable;
            else if (ItemsMatch(Executable, itemName))
                return Executable;
            else if (ItemsMatch(Form, itemName))
                return Form;
            else if (ItemsMatch(CoreComponent, itemName))
                return CoreComponent;
            else if (ItemsMatch(Component, itemName))
                return Component;
            else if (ItemsMatch(DLLImport, itemName))
                return DLLImport;
            else if (ItemsMatch(ConfigFile, itemName))
                return ConfigFile;
            else if (ItemsMatch(LoaderModule, itemName))
                return LoaderModule;
            else
                //Unknown Type
                return new ProjectTypes(itemName, "");
        }

        #region Private Static Methods
        /// <summary>
        /// Compares the project type name to the lookup value to see if they match
        /// </summary>
        /// <param name="projectType">Type of the project.</param>
        /// <param name="lookupValue">The lookup value.</param>
        /// <returns><c>true</c> if they match; otherwise <c>false</c></returns>
        private static bool ItemsMatch(ProjectTypes projectType, string lookupValue)
        {
            return lookupValue.Equals(projectType.Name, StringComparison.InvariantCultureIgnoreCase);
        }
        #endregion
    }
}