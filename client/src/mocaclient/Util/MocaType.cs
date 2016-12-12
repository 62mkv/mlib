using System;
using System.Data;
using System.Diagnostics;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// Enumeration class to define the known data types that can be
    /// a part of a MOCA result set. 
    /// </summary>
    [Serializable]
    public class MocaType
    {
        /// <summary>
        /// A static moca type that defines an integer column implimented by <see cref="Int32"/>.
        /// </summary>
        public static readonly MocaType INTEGER = new MocaType('I', typeof (Int32), DbType.Int32, "INTEGER");
        /// <summary>
        /// A static moca type that defines an Integer reference column implimented by <see cref="Int32"/>.
        /// </summary>
        public static readonly MocaType INTEGER_REF = new MocaType('P', typeof (Int32), DbType.Int32, "INTEGER_REF");
        /// <summary>
        /// A static moca type that defines a double column implimented by <see cref="Double"/>.
        /// </summary>
        public static readonly MocaType DOUBLE = new MocaType('F', typeof (Double), DbType.Double, "DOUBLE");
        /// <summary>
        /// A static moca type that defines a double reference column implimented by <see cref="Double"/>.
        /// </summary>
        public static readonly MocaType DOUBLE_REF = new MocaType('X', typeof (Double), DbType.Double, "DOUBLE_REF");
        /// <summary>
        /// A static moca type that defines a string column implimented by <see cref="String"/>.
        /// </summary>
        public static readonly MocaType STRING = new MocaType('S', typeof (string), DbType.String, "STRING");
        /// <summary>
        /// A static moca type that defines a string reference column implimented by <see cref="String"/>.
        /// </summary>
        public static readonly MocaType STRING_REF = new MocaType('Z', typeof (string), DbType.String, "STRING_REF");
        /// <summary>
        /// A static moca type that defines a Date/Time column implimented by <see cref="DateTime"/>.
        /// </summary>
        public static readonly MocaType DATETIME = new MocaType('D', typeof (DateTime), DbType.DateTime, "DATETIME");
        /// <summary>
        /// A static moca type that defines a Boolean column implimented by <see cref="Boolean"/>.
        /// </summary>
        public static readonly MocaType BOOLEAN = new MocaType('O', typeof (Boolean), DbType.Boolean, "BOOLEAN");
        /// <summary>
        /// A static moca type that defines a binary column implimented by a <see cref="byte"/> array.
        /// </summary>
        public static readonly MocaType BINARY = new MocaType('V', typeof (byte[]), DbType.Binary, "BINARY");
        /// <summary>
        /// A static moca type that defines another Moca results column.
        /// </summary>
        public static readonly MocaType RESULTS;
        /// <summary>
        /// A static moca type that defines a generic object column implimented by an <see cref="object"/>.
        /// </summary>
        public static readonly MocaType OBJECT = new MocaType('J', typeof (Object), DbType.Object, "OBJ");
        /// <summary>
        /// A static moca type that defines an unknown column type.
        /// </summary>
        public static readonly MocaType UNKNOWN = new MocaType('?', typeof (Object), DbType.Object, "UNKNOWN");

        #region Private Fields

        private readonly char _code;
        [NonSerialized] private Type _valueClass;
        [NonSerialized] private DbType _sqlType;
        [NonSerialized] private string _text;

        #endregion

        #region Constructors

        /// <summary>
        /// Private Constructor
        /// </summary>
        /// <param name="code">The MOCA Type code</param>
        /// <param name="cls">The system data type</param>
        /// <param name="sqlType">The <see cref="DbType"/> </param>
        /// <param name="text">A description</param>
        private MocaType(char code, Type cls, DbType sqlType, string text)
        {
            _code = code;
            _valueClass = cls;
            _sqlType = sqlType;
            _text = text;
        }

        [DebuggerStepThrough]
        static MocaType()
        {
            RESULTS = new MocaType('R', typeof (DataTable), DbType.Object, "RESULTS");
        }

        #endregion

        #region Public Properties

        /// <summary> 
        /// Returns the MOCA type code (COMTYP_*) corresponding to this type.
        /// </summary>
        /// <returns> the MOCA type code for this type. </returns>
        public char TypeCode
        {
            get { return _code; }
        }

        /// <summary> 
        /// Returns the system type that represents this type.
        /// </summary>
        /// <returns> the system type that represents this type. </returns>
        public Type Class
        {
            get { return _valueClass; }
        }

        /// <summary> 
        /// Returns the <see cref="DbType"/> of the given MOCA type.  This is mainly useful
        /// for use with <see cref="DataSet"/> manipulation
        /// </summary>
        /// <returns> the <see cref="DbType"/> corresponding to this data type. </returns>
        public DbType SQLType
        {
            get { return _sqlType; }
        }

        #endregion

        #region Public Methods

        /// <summary> 
        /// Returns the <code>MocaType</code> corresponding to the given low-level
        /// MOCA type code.
        /// </summary>
        /// <param name="code">a MOCA type code (COMTYP_*).</param>
        /// <returns> the corresponding instance of this enumerated class, or
        /// <code>UNKNOWN</code> if the type code is unrecognized.
        /// </returns>
        public static MocaType Lookup(char code)
        {
            switch (code)
            {
                case 'I':
                    return INTEGER;

                case 'L':
                    return INTEGER;

                case 'P':
                    return INTEGER_REF;

                case 'F':
                    return DOUBLE;

                case 'X':
                    return DOUBLE_REF;

                case 'S':
                    return STRING;

                case 'Z':
                    return STRING_REF;

                case 'D':
                    return DATETIME;

                case 'O':
                    return BOOLEAN;

                case 'V':
                    return BINARY;

                case 'R':
                    return RESULTS;

                case 'J':
                    return OBJECT;

                default:
                    return UNKNOWN;
            }
        }

        /// <summary> 
        /// Determine what MOCA type is appropriate for the given class.  This
        /// method will never return reference types, as they are indistinguishable
        /// from their value counterparts.
        /// </summary>
        /// <param name="cls">the class to be used to look up a MOCA type object. </param>
        /// <returns> the <code>MocaType</code> object corresponding to the given
        /// class.  If there is no known type for <code>cls</code>,
        /// <code>UNKNOWN</code> is returned.
        /// </returns>
        public static MocaType LookupClass(Type cls)
        {
            if (cls.Equals(typeof (string)))
                return STRING;
            if (cls.Equals(typeof (Int32)) || cls.Equals(Type.GetType("System.Int32")))
                return INTEGER;
            if (cls.Equals(typeof (Double)) || cls.Equals(Type.GetType("System.Double")))
                return DOUBLE;
            if (cls.Equals(typeof (Boolean)) || cls.Equals(Type.GetType("System.Boolean")))
                return BOOLEAN;
            if (typeof (DateTime).IsAssignableFrom(cls))
                return DATETIME;
            if (typeof (ValueType).IsAssignableFrom(cls))
                return DOUBLE;
            if (cls.Equals(typeof (DataTable)))
                return RESULTS;
            if (cls.IsArray && cls.GetElementType().Equals(Type.GetType("System.Byte")))
                return BINARY;
            if (!cls.IsPrimitive && !cls.IsArray)
                return OBJECT;
            
            return UNKNOWN;
        }

        #endregion

        #region Public Method Overrides

        /// <summary>
        /// Determines if an object is equal to the current object.
        /// </summary>
        /// <param name="o">The object to compare.</param>
        /// <returns><c>true</c> if they are the same; otherwise <c>false</c>.</returns>
        public override bool Equals(Object o)
        {
            if (ReferenceEquals(this, o)) return true;

            MocaType mType = (MocaType) o;

            if (mType != null)
            {
                return (_code == mType._code);
            }
            
            return false;
        }

        /// <summary>
        /// Serves as a hash function for a particular type. <see cref="M:System.Object.GetHashCode"></see> is suitable for use in hashing algorithms and data structures like a hash table.
        /// </summary>
        /// <returns>
        /// A hash code for the current <see cref="T:System.Object"></see>.
        /// </returns>
        public override int GetHashCode()
        {
            return _code;
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </returns>
        public override string ToString()
        {
            return _text;
        }

        /// <summary>
        /// Implements the operator ==.
        /// </summary>
        /// <param name="a">The first object to compare.</param>
        /// <param name="b">The second object to compare.</param>
        /// <returns>The result of the operator.</returns>
        public static bool operator ==(MocaType a, MocaType b)
        {
            if (ReferenceEquals(a, b)) return true;

            if ((object) a == null && (object) b == null) return true;

            if ((object) a == null || (object) b == null) return false;

            return a.Equals(b);
        }

        /// <summary>
        /// Implements the operator !=.
        /// </summary>
        /// <param name="a">The first object to compare.</param>
        /// <param name="b">The second object to compare.</param>
        /// <returns>The result of the operator.</returns>
        public static bool operator !=(MocaType a, MocaType b)
        {
            return !(a == b);
        }

        #endregion
    }
}