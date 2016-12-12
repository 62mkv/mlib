using System;
using System.Diagnostics.CodeAnalysis;
using System.Reflection;
using System.Reflection.Emit;
using System.Security;
using System.Security.Permissions;

namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// A delegate method used to set the values of the properties
    /// </summary>
    internal delegate void ValueSettingDelegate(object target, object value);

    /// <summary>
    /// This object tracks the current state of the build plan generation,
    /// accumulates the IL, provides the preamble &amp; postamble for the dynamic
    /// method, and tracks things like local variables in the generated IL
    /// so that they can be reused across IL generation strategies.
    /// </summary>
    public class DynamicBuildPlanGenerationContext
    {
        private readonly Type typeToBuild;
        private readonly string propertyName;
        private DynamicMethod buildMethod;
        private readonly ILGenerator il;
        
        /// <summary>
        /// Create a <see cref="DynamicBuildPlanGenerationContext"/> that is initialized
        /// to handle creation of a dynamic method to build the given type.
        /// </summary>
        /// <param name="typeToBuild">Type that we're trying to create a build plan for.</param>
        /// <param name="propertyName">Name of the property.</param>
        public DynamicBuildPlanGenerationContext(Type typeToBuild, String propertyName)
        {
            //Guard.ArgumentNotNull(typeToBuild, "typeToBuild");
            this.typeToBuild = typeToBuild;
            this.propertyName = propertyName;

            // Check for full trust. We can't add the method to the
            // built up type without it.

            try
            {
                PermissionSet fullTrust = new PermissionSet(PermissionState.Unrestricted);
                fullTrust.Demand();
                if (typeToBuild.IsInterface)
                {
                    CreateMethodOnModule(typeToBuild.Module);    
                }
                else
                {
                    CreateMethodOnBuiltUpType();
                }
            }
            catch (SecurityException)
            {
                // Not in full trust, add IL to this module instead.
                CreateMethodOnModule(GetType().Module);
            }

            il = buildMethod.GetILGenerator();
        }

        /// <summary>
        /// The underlying <see cref="ILGenerator"/> that can be used to
        /// emit IL into the generated dynamic method.
        /// </summary>
        public ILGenerator IL
        {
            get { return il; }
        }

        /// <summary>
        /// Completes generation of the dynamic method and returns the
        /// generated dynamic method delegate.
        /// </summary>
        /// <returns>The created <see cref="ValueSettingDelegate"/></returns>
        internal ValueSettingDelegate GetBuildMethod()
        {
            buildMethod.DefineParameter(1, ParameterAttributes.In, "target");
            buildMethod.DefineParameter(2, ParameterAttributes.In, "value");
            return (ValueSettingDelegate)buildMethod.CreateDelegate(typeof(ValueSettingDelegate));
        }

        /// <summary>
        /// Creates the new dynamic method on the built up type
        /// </summary>
        private void CreateMethodOnBuiltUpType()
        {
            buildMethod = new DynamicMethod(BuildMethodName(),
                                            typeof(void),
                                            Types(typeof(object), typeof(object)),
                                            typeToBuild);
        }

        /// <summary>
        /// Creates the method on module of the built up type.
        /// </summary>
        /// <param name="module">The module.</param>
        private void CreateMethodOnModule(Module module)
        {
            buildMethod = new DynamicMethod(BuildMethodName(),
                                            typeof(void),
                                            Types(typeof(object), typeof(object)),
                                            module);
        }

        /// <summary>
        /// Creates the name of the dynamic method.
        /// </summary>
        /// <returns></returns>
        private string BuildMethodName()
        {
            return string.Format("BuildUp_{0}_{1}", propertyName, new Random().Next(1, 3000));
        }

        /// <summary>
        /// Sepcifies the types to add to the dynamic method
        /// </summary>
        /// <param name="types">The types.</param>
        /// <returns></returns>
        private static Type[] Types(params Type[] types)
        {
            return types;
        }

        #region IL Generation helper methods

        /// <summary>
        /// Emit the IL needed to look up an call it to get a value.
        /// </summary>
        /// <param name="dependencyType">Type of the dependency to resolve.</param>
        /// <param name="methodInfo">The method info.</param>
        [SuppressMessage("Microsoft.Design", "CA1062:ValidateArgumentsOfPublicMethods",
            Justification = "Validation is done via Guard class.")]
        public void EmitResolveDependency(Type dependencyType, MethodInfo methodInfo)
        {
            IL.Emit(OpCodes.Ldarg_0);
            IL.Emit(OpCodes.Castclass, typeToBuild);
            IL.Emit(OpCodes.Ldarg_1);
            
            if (dependencyType.IsValueType)
            {
                IL.Emit(OpCodes.Unbox_Any, dependencyType);
            }
            else
            {
                IL.Emit(OpCodes.Castclass, dependencyType);
            }
            
            IL.EmitCall(OpCodes.Callvirt, methodInfo, null);
            IL.Emit(OpCodes.Ret);
        }

        #endregion
    }
}