using System;
using RedPrairie.MOCA.Client.ObjectMapping;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    internal class MappingUtils
    {
        public static ColumnMap CreateColumnMap(string propertyName, Type classType)
        {
            ReflectionColumnMapper mapper = new ReflectionColumnMapper();
            
            MappingData data = mapper.GetMappingInformation(classType);

            return data.GetColumnMap(propertyName);
        }
    }
}
