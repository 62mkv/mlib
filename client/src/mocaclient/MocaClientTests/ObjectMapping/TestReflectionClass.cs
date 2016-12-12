using System;
using System.Collections.Generic;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    public class TestReflectionClass
    {
        private int testInt;
        private string testString;
        private DateTime testDateTime;
        private const string readProperty = null;
        private readonly Dictionary<string, object> extraData = new Dictionary<string, object>();
        private string x;

        public string ReadProperty
        {
            get { return readProperty; }
        }

        public int TestInt
        {
            get { return testInt; }
            set { testInt = value; }
        }


        public string TestString
        {
            get { return testString; }
            set { testString = value; }
        }

        public DateTime TestDateTime
        {
            get { return testDateTime; }
            set { testDateTime = value; }
        }

        public string X
        {
            get { return x; }
            set { x = value; }
        }

        public Dictionary<string, object> ExtraData
        {
            get { return extraData; }
        }
    }
}