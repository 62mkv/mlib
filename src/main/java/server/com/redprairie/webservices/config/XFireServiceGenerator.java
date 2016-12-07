/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.webservices.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates java Source files and descriptor file
 * 
 * <b>
 * 
 * <pre>
 *        Copyright (c) 2005 RedPrairie Corporation
 *        All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class XFireServiceGenerator {

    public XFireServiceGenerator(File sourcePath, File configPath) {
        _sourcePath = sourcePath;
        _configPath = configPath;
    }

    /**
     * Generates the output source and config file using ServiceConfiguration
     * object
     * 
     * @param ServiceConfiguration.
     * @throws ServiceConfigException
     */
    public void generate(ServiceConfiguration config) throws IOException {
        generate(new ServiceConfiguration[] {config});
    }
    
    public void generate(ServiceConfiguration[] configList)
            throws IOException  {
        StringBuilder descr = beginDescriptor();
        Service[] services = normalizeServiceList(configList);
        if (services.length > 0) {

            for (int i = 0; i < services.length; i++) {
                Service serviceObj = services[i];
                if (serviceObj == null) {
                    continue;
                }

                String serviceName = serviceObj.getName();
                
                String pkgName = serviceObj.getPackageName();
                if (pkgName == null) {
                    pkgName = defaultPackage();
                }
                
                String className = serviceObj.getClassName();
                if (className == null) {
                    className = serviceName.replaceAll("[./$]", "_") + "ServiceClass";
                }

                if (serviceObj.isGenerate()) {
                    String servStr = buildServiceClass(serviceObj);
                    if (servStr != null) {
                        generateOutputJavaFile(servStr, className, pkgName);
                    }
                }
                addToDescriptor(descr, serviceName, pkgName, className);
            }
        }
        String output = endDescriptor(descr);
        generateOutputDescriptor(output);
    }
    
    public static String defaultPackage() {
        return "com.redprairie.webservices.gen";
    }
    
    public static String defaultClassName(String serviceName) {
        return serviceName.replaceAll("[./$]", "_") + "ServiceClass";
    }
    
    //
    // Implementation
    //
    
    private Service[] normalizeServiceList(ServiceConfiguration[] configList) {
        Map<String, Service> serviceMap = new LinkedHashMap<String, Service>();
        for (ServiceConfiguration c : configList) {
            for (Service s : c.getServices()) {
                Service existing = serviceMap.get(s.getName());
                if (existing != null) {
                    existing.addAllOperations(s.getOperations());
                }
                else {
                    serviceMap.put(s.getName(), s);
                }
            }
        }
        return serviceMap.values().toArray(new Service[serviceMap.size()]);
    }

    /**
     * Builds the Service class String using Service Object
     * 
     * @param Service.
     * @return String built based on Service object
     * @throws ServiceConfigException
     */
    private String buildServiceClass(Service service) throws IOException {
        StringBuilder servBuff = new StringBuilder();
        Operation operation[] = service.getOperations();
        
        String packageName = service.getPackageName();
        if (packageName == null) {
            packageName = "com.redprairie.webservices.gen";
        }
        
        String className = service.getClassName();
        if (className == null) {
            className = service.getName().replaceAll("[./$]", "_") + "ServiceClass";
        }

        servBuff.append(ServiceConfigConstants.PACKAGE).append(packageName)
                .append(";").append(ServiceConfigConstants.NEW_LINE).append(
                        ServiceConfigConstants.NEW_LINE);
        servBuff.append(ServiceConfigConstants.IMPORTS).append(
                ServiceConfigConstants.NEW_LINE).append(
                ServiceConfigConstants.NEW_LINE);
        servBuff.append("@WebService ");
        servBuff.append(ServiceConfigConstants.PUBLIC_CLASS).append(
                className);
        servBuff.append(" { ").append(ServiceConfigConstants.NEW_LINE);
        StringBuilder oprBuff = new StringBuilder();
        if (operation.length > 0) {

            for (int i = 0; i < operation.length; i++) {
                Operation operationObj = operation[i];
                if (operationObj != null) {
                    String oprStr = buildOperationMethod(service.getName(),
                            operationObj,
                            packageName);
                    if (oprStr != null) {
                        oprBuff.append(oprStr).append(
                                ServiceConfigConstants.NEW_LINE);
                    }

                }

            }

        }
        servBuff.append(oprBuff);
        servBuff.append(ServiceConfigConstants.NEW_LINE).append(" }");

        return servBuff.toString();
    }

    /**
     * Builds the Operation method String using Operation Object and pakage name
     * 
     * @param Operation.
     * @param String  pakage name
     * @return String constructed based on Operation Object
     * @throws ServiceConfigException
     */
    private String buildOperationMethod(String serviceName,
                                        Operation operationObj,
                                        String packageName) throws IOException {
        OperationArgument oprArg[] = operationObj.getOperationArguments();
        String methodName = operationObj.getName();
        String commandRaw = operationObj.getCommand();
        StringBuilder argStr = new StringBuilder();
        StringBuilder cmdStr = new StringBuilder();
        int count = 0;
        ResultField fields[] = operationObj.getResultFields();

        String resClass = operationObj.getResultClassName();
        if (resClass == null) {
            resClass = operationObj.getName() + serviceName + "_" + operationObj.getName() + "Result";
        }
        
// start of ResultOutput Java Class generation 
        String outputResultStr = buildOutputClass(serviceName, fields, packageName,
                operationObj);

        generateOutputJavaFile(outputResultStr, resClass, packageName);
        // end of ResultOutput Java Class generation

        if (oprArg != null) {

            for (int l = 0; l < oprArg.length; l++) {
                OperationArgument oprArgObj = oprArg[l];
                String oprArgName = oprArgObj.getName();
                String oprColName = oprArgObj.getColumn();
                String oprArgType = oprArgObj.getType();
                boolean isNullable = oprArgObj.isNullable();
                oprArgType = getArgumentType(oprArgType, isNullable);
                argStr.append("@WebParam(");
                argStr.append("name=\"" + oprArgName + "\")");
                argStr.append(oprArgType).append(" ").append(oprArgName);
                if (count >= 0 && count < oprArg.length - 1) {
                    argStr.append(',');
                }

                if (count == 0) {
                    cmdStr.append(ServiceConfigConstants.COMMAND_APPEND)
                            .append(" where  ").append(oprColName);
                }
                else {
                    cmdStr.append(ServiceConfigConstants.EIGHT_SPACES);
                    cmdStr.append(ServiceConfigConstants.COMMAND_APPEND)
                            .append(" and ").append(oprColName);
                }
                
                if (oprArgType.equalsIgnoreCase("string")) {

                    cmdStr.append(" = \" + ServiceUtils.quote(").append(oprArgName).append(
                            ")+ \" \"  ); ").append(
                            ServiceConfigConstants.NEW_LINE);
                }
                else {
                    cmdStr.append(" = \" + ").append(oprArgName).append(
                            "+ \" \"  ); ").append(
                            ServiceConfigConstants.NEW_LINE);
                }

                count++;
            }
        }
        
        if (count > 0) {
            argStr.append(',');
        }
        argStr.append("@WebParam(name=\"UserToken\", header=true) UserToken __token");

        String result = "___resultArray";

        String retString = resClass;
        boolean isMultiRec = operationObj.isMultiRowResult();
        if (isMultiRec) {
            retString = retString + "[]";
        }
        
        // Need to cook the command string a little, to make it fit into a
        // Java string
        String commandStr = commandRaw.replace("\"", "\\\"");
        commandStr = commandStr.replaceAll("[\r\n]", " ");

        StringBuilder oprBuff = new StringBuilder();
        oprBuff.append(ServiceConfigConstants.FOUR_SPACES);
        oprBuff.append("@WebMethod(");
        oprBuff.append("operationName=\"" + methodName + "\", ");
        oprBuff.append("action=\"urn:" + methodName + "\") ");
        oprBuff.append(ServiceConfigConstants.PUBLIC).append(retString).append(
                ' ').append(methodName);
        oprBuff.append('(').append(argStr).append(") throws ServiceException { ")
                .append(ServiceConfigConstants.NEW_LINE).append(
                        ServiceConfigConstants.NEW_LINE);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        oprBuff.append(ServiceConfigConstants.COMMAND_STRING).append(
                ServiceConfigConstants.NEW_LINE);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        oprBuff.append(cmdStr);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        oprBuff.append(ServiceConfigConstants.COMMAND_APPEND).append(" | \");")
                .append(ServiceConfigConstants.NEW_LINE);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        oprBuff.append(ServiceConfigConstants.COMMAND_APPEND);
        oprBuff.append(commandStr).append("\");").append(
                ServiceConfigConstants.NEW_LINE);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        oprBuff.append(ServiceConfigConstants.MOCA_RESULT_STRING).append(
                ServiceConfigConstants.NEW_LINE);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        oprBuff.append(resClass).append("[]").append(' ').append(result)
                .append(" = ");
        oprBuff.append('(').append(resClass).append("[]").append(')');
        oprBuff.append("MocaUtils.createObjectArray(").append(resClass).append(
                ".class").append(',');
        oprBuff.append("___results );").append(ServiceConfigConstants.NEW_LINE);
        oprBuff.append(ServiceConfigConstants.EIGHT_SPACES);
        if (!isMultiRec) {
            result = result + "[0]";
        }

        oprBuff.append("return  ").append(result).append(';').append(
                ServiceConfigConstants.NEW_LINE);
        oprBuff.append(ServiceConfigConstants.FOUR_SPACES);
        oprBuff.append('}');
        return oprBuff.toString();
    }

    /**
     * Returns formated type String
     * 
     * @param String  type
     * @param boolean isNullable
     * @return String formated  type
     */
    private String getArgumentType(String type, boolean nullable) {
        String result;
        type = type.toLowerCase();
        if (type.startsWith("char")) {
            result = nullable ? "Character" : "char";
        }
        else if (type.startsWith("int")) {
            result = nullable ? "Integer" : "int";
        }
        else if (type.equals("string")) {
            result = "String";
        }
        else if (type.equals("byte")) {
            result = nullable ? "Byte" : "byte";
        }
        else if (type.equals("short")) {
            result = nullable ? "Short" : "short";
        }
        else if (type.equals("long")) {
            result = nullable ? "Long" : "long";
        }
        else if (type.equals("float")) {
            result = nullable ? "Float" : "float";
        }
        else if (type.equals("double")) {
            result = nullable ? "Double" : "double";
        }
        else if (type.equals("boolean")) {
            result = nullable ? "Boolean" : "boolean";
        }
        else {
            result = "Object";
        }
        return result;
    }

    /**
     * Builds Result Output Class using Result Field Collection, Operation
     * Object and pakage name
     * 
     * @param ArrayList  collection of Result Fields.
     * @param String  pakage name
     * @param Operation.
     * @return String built based on Result Fields and Operation Object     
     */
    private String buildOutputClass(String serviceName, ResultField resultField[], String pkgName,
            Operation oprObj) {
        String resultClassName = null;
        StringBuilder resultClsBuff = new StringBuilder();

        resultClsBuff.append(ServiceConfigConstants.PACKAGE).append(pkgName)
                .append(";").append(ServiceConfigConstants.NEW_LINE).append(
                        ServiceConfigConstants.NEW_LINE);
        if (oprObj != null) {
            resultClassName = oprObj.getResultClassName();
        }
        if (resultClassName == null) {
            resultClassName = oprObj.getName() + serviceName + "_" + oprObj.getName() + "Result";
        }

        resultClsBuff.append(ServiceConfigConstants.PUBLIC_CLASS).append(
                resultClassName).append(" { ").append(
                ServiceConfigConstants.NEW_LINE).append(
                ServiceConfigConstants.NEW_LINE);

        StringBuilder propDeclBuff = new StringBuilder();
        if (resultField != null) {

            for (int h = 0; h < resultField.length; h++) {
                ResultField resultFieldObj = resultField[h];
                if (resultFieldObj != null) {
                    String fieldName = resultFieldObj.getName();
                    String columnName = resultFieldObj.getColumn();
                    String fieldType = resultFieldObj.getType();
                    boolean isNullable = resultFieldObj.isNullable();
                    fieldType = getArgumentType(fieldType, isNullable);
                    resultClsBuff.append(ServiceConfigConstants.NEW_LINE)
                            .append(createGetMethod(fieldName, fieldType, columnName));
                    resultClsBuff.append(ServiceConfigConstants.NEW_LINE)
                            .append(createSetMethod(fieldName, fieldType, columnName));
                    propDeclBuff.append(ServiceConfigConstants.NEW_LINE)
                            .append(ServiceConfigConstants.FOUR_SPACES).append(
                                    ServiceConfigConstants.PRIVATE).append(
                                    fieldType).append(' ').append('_').append(
                                    fieldName).append(';').append(
                                    ServiceConfigConstants.NEW_LINE);
                }

            }
        }

        resultClsBuff.append(ServiceConfigConstants.NEW_LINE).append(
                propDeclBuff);
        resultClsBuff.append('}');
        return resultClsBuff.toString();

    }

    /**
     * Builds setter method for an attribute in Result Class using Field name
     * and return type
     * 
     * @param fieldName field name of Result Field
     * @param returnType return Type of Result Field
     * @param columnName column name of Result Field 
     * @return String built based name and type of Result Field
     */
    private String createGetMethod(String fieldName, String returnType,
                                   String columnName) {
        StringBuilder getBuff = new StringBuilder();
        
        if (columnName != null) {
            getBuff.append(ServiceConfigConstants.FOUR_SPACES);
            getBuff.append(ServiceConfigConstants.ANNOTATION_BEGIN);
            getBuff.append(columnName);
            getBuff.append(ServiceConfigConstants.ANNOTATION_END);
            getBuff.append(ServiceConfigConstants.NEW_LINE);
        }
        
        getBuff.append(ServiceConfigConstants.FOUR_SPACES);
        getBuff.append(ServiceConfigConstants.PUBLIC).append(returnType)
                .append(' ').append(ServiceConfigConstants.GET).append(
                        initCaps(fieldName)).append("()  { ").append(
                        ServiceConfigConstants.NEW_LINE);
        getBuff.append(ServiceConfigConstants.EIGHT_SPACES).append("return ")
                .append('_').append(fieldName).append(';').append(
                        ServiceConfigConstants.NEW_LINE);
        getBuff.append(ServiceConfigConstants.FOUR_SPACES);
        getBuff.append('}');
        return getBuff.toString();
    }

    /**
     * Builds getter method an attribute in Result Class using Field name and
     * return type
     * 
     * @param fieldName field name of Result Field
     * @param returnType return Type of Result Field
     * @param columnName column name of Result Field 
     * @return String built based name and type of Result Field
     */
    private String createSetMethod(String fieldName, String returnType,
                                   String columnName) {
        StringBuilder setBuff = new StringBuilder();
        
        if (columnName != null) {
            setBuff.append(ServiceConfigConstants.FOUR_SPACES);
            setBuff.append(ServiceConfigConstants.ANNOTATION_BEGIN);
            setBuff.append(columnName);
            setBuff.append(ServiceConfigConstants.ANNOTATION_END);
            setBuff.append(ServiceConfigConstants.NEW_LINE);
        }
        
        setBuff.append(ServiceConfigConstants.FOUR_SPACES);
        setBuff.append(ServiceConfigConstants.PUBLIC_VOID).append(
                ServiceConfigConstants.SET).append(initCaps(fieldName)).append(
                "( ").append(returnType).append(' ').append(fieldName).append(
                ')').append(" { ").append(ServiceConfigConstants.NEW_LINE);
        setBuff.append(ServiceConfigConstants.EIGHT_SPACES).append('_').append(
                fieldName).append(" = ").append(fieldName).append(';').append(
                ServiceConfigConstants.NEW_LINE);
        setBuff.append(ServiceConfigConstants.FOUR_SPACES);
        setBuff.append('}');
        return setBuff.toString();

    }

    /**
     * Generates a Java file for the given String, which is already built
     * 
     * @param String  built Class string
     * @param String  name of the class
     * @param String  name of the package
     * @throws ServiceConfigException
     */
    private void generateOutputJavaFile(String str, String className,
        String pkgName) throws IOException {
        // logger.info("_sourcePath.getPath() "+_sourcePath.getPath());
        String packagePath = pkgName.replace(".", "//");
        File fullDir = new File(_sourcePath.getPath() + "//" + packagePath);
        if (!fullDir.exists()) {
            if (!fullDir.mkdirs()) {
                throw new IOException("Could not create directories for : " + 
                        _configPath.getAbsolutePath());
            }
        }

        String filePath = _sourcePath.getPath() + "//" + packagePath + "//"
                + className + ".java";
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream(filePath);
            
            byte[] data = str.getBytes(Charset.forName("UTF-8"));
            fos.write(data);
        }
        finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    private StringBuilder beginDescriptor() {
        StringBuilder buf = new StringBuilder();
        buf.append("<beans xmlns=\"http://xfire.codehaus.org/config/1.0\">\n");
        return buf;
    }

    private String endDescriptor(StringBuilder buf) {
        buf.append("</beans>\n");
        return buf.toString();
    }

    /**
     * Builds Descriptor file for a service
     * 
     * @param String  service name
     * @param String  pakage name
     * @param String  class name
     * @return String built based on service name
     */
    private void addToDescriptor(StringBuilder buf,
            String serviceName, String pkgName, String className) {
        buf.append("  <service>\n");
        buf.append("    <name>").append(serviceName).append("</name>\n");
        buf.append("  <namespace>http://services.redprairie.com/</namespace>\n");
        buf.append("    <serviceClass>").append(pkgName).append('.').append(
                className).append("</serviceClass>\n");
        buf.append("    <serviceFactory>jsr181</serviceFactory>\n");
        buf.append("  </service>\n");
    }

    /**
     * Generates a Descriptor file for the given String, which is already built
     * 
     * @param String  built Descriptor string
     * @param String  file name
     * @throws ServiceConfigException
     */
    private void generateOutputDescriptor(String str)
            throws IOException {
        if (!_configPath.exists()) {
            if (!_configPath.mkdirs()) {
                throw new IOException("Could not create directories for : " + 
                        _configPath.getAbsolutePath());
            }
        }
        
        File outFile = new File(_configPath, "services.xml");
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream(outFile);
            byte[] data = str.getBytes(Charset.forName("UTF-8"));
            fos.write(data);
        }
        finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Utility method it changes first letter of string to upper case
     * 
     * @param String
     * @return String
     */
    private String initCaps(String str) {
        str = str.substring(0, 1).toUpperCase().concat(
                str.substring(1, str.length()));
        return str;
    }

    private File _sourcePath;
    private File _configPath;
}
