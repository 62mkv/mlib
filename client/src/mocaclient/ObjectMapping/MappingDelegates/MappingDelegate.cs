using System;
using RedPrairie.MOCA.Client;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// A class that contains the mapping method to set the value
    /// of a specific column into the property
    /// </summary>
    public abstract class MappingDelegate
    {
        #region Private Fields
        /// <summary>
        /// A column map information class used for mapping
        /// </summary>
        protected readonly ColumnMap map;
        private readonly ValueSettingDelegate setValue;
        #endregion

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="MappingDelegate"/> class.
        /// </summary>
        /// <param name="map">The property info for the mapper.</param>
        protected MappingDelegate(ColumnMap map)
        {
            this.map = map;
            setValue = CreateDelegateMethod();
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Clones the specified column map.
        /// </summary>
        /// <param name="columnMap">The column map.</param>
        /// <returns></returns>
        public virtual MappingDelegate Clone(ColumnMap columnMap)
        {
            return null;
        }

        /// <summary>
        /// Sets the defined value as null based on the <paramref name="target"/> object.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        public virtual void SetNull(object target)
        {
            if (!SetDefaultValue(target))
            {
                SetPropertyValue(target, null);
            }
        }

        /// <summary>
        /// Sets the value (<paramref name="data"/>) in the <paramref name="target"/> object.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        /// <param name="type">The type of the incoming data.</param>
        /// <param name="data">The data to convert it into.</param>
        public abstract void SetValue(object target, MocaType type, string data);

        /// <summary>
        /// Sets the (<paramref name="data"/>) in the <paramref name="target"/> object.
        /// This overload is only used for binary and result data types.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        /// <param name="data">The data in a binary array form.</param>
        public virtual void SetValue(object target, byte[] data)
        {
        }
        #endregion

        #region Protectected Methods

        /// <summary>
        /// Sets the default value if it exists.
        /// </summary>
        /// <param name="target">The target.</param>
        /// <returns></returns>
        protected bool SetDefaultValue(object target)
        {
            if (map.DefaultValue != null &&
                map.DefaultValue.GetType().IsAssignableFrom(map.DataType))
            {
                SetPropertyValue(target, map.DefaultValue);
                return true;
            }

            return false;
        }

        /// <summary>
        /// Sets the value in the specified obejct target.
        /// </summary>
        /// <param name="target">The target.</param>
        /// <param name="value">The value.</param>
        protected void SetPropertyValue(object target, object value)
        {
            if (setValue == null)
            {
                Console.WriteLine("Not setting value '{0}' for {1}, delegate method is null", value, target);
                return;
            }

            try
            {
                setValue(target, value);
            }
            catch (SystemException e)
            {
                Console.WriteLine("Exception at setting property {0}: {1}", map.PropertyName, e.Message);
            }
        }

        #endregion

        #region Private Methods
        /// <summary>
        /// Creates the delegate method.
        /// </summary>
        /// <returns>A <see cref="ValueSettingDelegate"/> or <c>null</c>.</returns>
        private ValueSettingDelegate CreateDelegateMethod()
        {
            if (map == null || map.PropertyInfo == null)
                return null;

            DynamicBuildPlanGenerationContext ilContext =
                    new DynamicBuildPlanGenerationContext(map.PropertyInfo.DeclaringType, map.PropertyName);
            ilContext.EmitResolveDependency(map.PropertyInfo.PropertyType, map.PropertyInfo.GetSetMethod());
            return ilContext.GetBuildMethod();
        }
        #endregion
    }
}
