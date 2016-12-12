using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client.Encoding.Xml
{
    /// <summary>
    /// A request encoder that creates an XML document.
    /// </summary>
    public class XmlRequestEncoder
    {
        /// <summary>
        /// Encodes the request.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="sessionId">The session id.</param>
        /// <param name="envString">The environment string.</param>
        /// <param name="context">The context argument list.</param>
        /// <param name="args">The explicit argument list. This generally should not be set.</param>
        /// <param name="autoCommit">if set to <c>true</c> auto commit the transaction.</param>
        /// <param name="writer">The writer.</param>
        public static void EncodeRequest(string command, string sessionId, string envString, IEnumerable<IMocaArgument> context, IEnumerable<IMocaArgument> args, bool autoCommit, StringBuilder writer)
        {

            writer.AppendFormat("<moca-request autocommit=\"{0}\">", autoCommit);

            if (sessionId != null)
            {
                writer.Append("<session id=\"");
                writer.Append(sessionId);
                writer.Append("\"/>");
            }
            if (envString != null)
            {
                writer.Append("<environment>");

                writer.Append(envString);
                writer.Append("</environment>");
            }

            if (context != null)
            {
                writer.Append("<context>");
                WiteArguments(context, writer);
                writer.Append("</context>");
            }

            if (args != null)
            {
                writer.Append("<args>");
                WiteArguments(args, writer);
                writer.Append("</args>");
            }

            if (command != null)
            {
                writer.Append("<query>");
                EscapeXml(command, writer);
                writer.Append("</query>");
            }

            writer.Append("</moca-request>");
        }

        /// <summary>
        /// Builds the XML environment string.
        /// </summary>
        /// <param name="env">The environment variables to encode.</param>
        /// <returns>An encoded environment string.</returns>
        public static String BuildXmlEnvironmentString(IDictionary<string, string> env)
        {
            var buf = new StringBuilder();
            foreach (var entry in env)
            {
                var name = entry.Key;
                var value = entry.Value;

                try
                {
                    buf.Append("<var name=\"");
                    EscapeXml(name.ToUpperInvariant(), buf);
                    buf.Append("\" value=\"");
                    EscapeXml(value, buf);
                    buf.Append("\"/>");
                }
                catch (IOException)
                {
                    // Could be thrown by EscapeXml, but won't because we're writing
                    // to a StringBuilder.
                }
            }

            return buf.ToString();
        }

        /// <summary>
        /// Escapes the XML.
        /// </summary>
        /// <param name="s">The string to escape.</param>
        /// <param name="writer">The stream writer output.</param>
        private static void EscapeXml(String s, StringBuilder writer)
        {

            if (String.IsNullOrEmpty(s))
                return;

            for (var i = 0; i < s.Length; i++)
            {
                var c = s[i];
                switch (c)
                {
                    case '&':
                        writer.Append("&amp;");
                        break;
                    case '<':
                        writer.Append("&lt;");
                        break;
                    case '>':
                        writer.Append("&gt;");
                        break;
                    case '"':
                        writer.Append("&quot;");
                        break;
                    case '\'':
                        writer.Append("&apos;");
                        break;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        writer.Append(c);
                        break;
                    default:
                        if (Char.IsControl(c))
                        {
                            // Any non-whitespace control characters can appear as themselves, as long
                            // as they are escaped as character entities.  The exception, in Unicode 1.0,
                            // is that C0 control characters (0x00 .. 0x1f) cannot appear in any form.
                            // In order to allow strings that contain C0 control characters, we must move
                            // the entire protocol to XML 1.1.
                            if (c >= 0x00 && c <= 0x1f)
                            {
                                // Output the special "not a character" character
                                writer.Append('\ufffd');
                            }
                            else
                            {
                                writer.AppendFormat("&#x{0:x};", (int)c);
                            }
                        }
                        else
                        {
                            writer.Append(c);
                        }
                        break;
                }
            }
        }

        private static void WiteArguments(IEnumerable<IMocaArgument> args, StringBuilder writer)
        {
            foreach (var arg in args)
            {
                writer.Append("<field name=\"");
                EscapeXml(arg.Name, writer);
                writer.AppendFormat("\" type=\"{0}\" oper=\"{1}\"", 
                            arg.Type,  GetOperator(arg.Operator));

                object value = arg.Value;
                if (value == null)
                {
                    writer.Append(" null=\"true\"/>");
                }
                else
                {
                    writer.Append(">");
                    MocaType type = MocaType.LookupClass(value.GetType());

                    if (type == MocaType.BOOLEAN ||
                        type == MocaType.STRING ||
                        type == MocaType.INTEGER ||
                        type == MocaType.DOUBLE)
                    {
                        EscapeXml(arg.Value.ToString(), writer);  
                    }
                    else if (type == MocaType.DATETIME)
                    {
                        writer.Append(((DateTime) value).ToString(EncodingBase.DATE_TIME_FORMAT));
                    }
                    else if (type == MocaType.RESULTS ||
                             type == MocaType.BINARY)
                    {
                        //TODO: SUPPORT Results and Binary
                    }

                    writer.Append("</field>");
                }
            }
        }

        /// <summary>
        /// Gets the operator.
        /// </summary>
        /// <param name="mocaOperator">The moca operator.</param>
        /// <returns></returns>
        private static string GetOperator(MocaOperator mocaOperator)
        {
            switch (mocaOperator)
            {
                case MocaOperator.Equal:
                    return "EQ";
                case MocaOperator.NotEqual:
                    return "NE";
                case MocaOperator.GreaterThan:
                    return "GT";
                case MocaOperator.GreaterThanEqual:
                    return "GE";
                case MocaOperator.LessThan:
                    return "LT";
                case MocaOperator.LessThanEqual:
                    return "LE";
                case MocaOperator.Like:
                    return "LIKE";
                case MocaOperator.NotLike:
                    return "NOTLIKE";
                case MocaOperator.RawClause:
                    return "RAWCLAUSE";
                case MocaOperator.NamedClause:
                    return "NAMEDCLAUSE";
                default:
                    return "EQ";
            }
        }
    }
}