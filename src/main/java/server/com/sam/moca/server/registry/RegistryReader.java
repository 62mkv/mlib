/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.sam.moca.server.registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaRegistry;
import com.sam.moca.MocaRuntimeException;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.util.MocaUtils;
import com.sam.util.Base64;
import com.sam.util.StringReplacer;
import com.sam.util.VarStringReplacer;

/**
 * A class that reads the registry information into
 * memory and provides it through the SystemContext interface.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class RegistryReader implements SystemContext {
    
    public RegistryReader(File regFile) throws SystemConfigurationException {
        Reader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(regFile), "UTF-8");
            init(in);
        }
        catch (FileNotFoundException e) {
            throw new SystemConfigurationException(
                "Unable to read configuration file: " + regFile, e);
        }
        catch (UnsupportedEncodingException e) {
            throw new SystemConfigurationException(
                "Trouble reading configuration file: " + regFile, e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ignore) { }
            }
        }
    }
    
    public RegistryReader(Reader regReader) throws SystemConfigurationException {
        init(regReader);
    }

    /**
     * Looks up the given registry element.  Embedded variables are expanded from the
     * <code>[environment]</code> section.
     */
    @Override
    public String getConfigurationElement(final String key) {
    	return getConfigurationElement(key, true);
    }
    
    /**
     * Looks up the given registry element.  Embedded variables are expanded from the
     * <code>[environment]</code> section if <code>expand</code> is true.
     */
    @Override
    public String getConfigurationElement(final String entry, boolean expand) {
        String[] temp = parseEntry(entry);
        String section = temp[0];
        String key = temp[1];
        
        synchronized (_registry) {
            // Look up 
            Map<String, String> sectionMap = _registry.get(section.toLowerCase());
            if (sectionMap != null) {
                String rawValue = sectionMap.get(key.toLowerCase());
                rawValue = decodeValue(rawValue);
                if (expand) {
                    return expand(rawValue, key, section);
                }
                else {
                	return rawValue;
                }
            }
            else {
                return null;
            }
        }
    }
    
    /**
     * Looks up the given registry element.  Embedded variables are expanded from the
     * <code>[environment]</code> section.
     */
    @Override
    public String getConfigurationElement(final String key, final String defaultValue) {
    	return getConfigurationElement(key, defaultValue, true);
    }
    
    /**
     * Looks up the given registry element.  Embedded variables are expanded from the
     * <code>[environment]</code> section if <code>expand</code> is true.
     */
    @Override
    public String getConfigurationElement(final String entry, final String defaultValue, boolean expand) {
        String value = getConfigurationElement(entry, expand);
        
        if (value == null) {
            if (expand) {
                String[] temp = parseEntry(entry);
                String section = temp[0];
                String key = temp[1];

                return expand(defaultValue, key, section);
            }
            else {
                return defaultValue;
            }
        }
        
        return value;
    }

    @Override
    public Map<String, String> getConfigurationSection(String section, boolean expand) {
        Map<String, String> out = new LinkedHashMap<String, String>();
        synchronized (_registry) {
            Map<String, String> sectionMap = _registry.get(
                    section.toLowerCase());
            if (sectionMap != null) {
                for (Map.Entry<String, String> entry : sectionMap.entrySet()) {
                    if (expand)
                        out.put(entry.getKey().toUpperCase(), expand(
                                entry.getValue(), entry.getKey(), section));
                    else
                        out.put(entry.getKey().toUpperCase(), entry.getValue());
                }
            }
        }

        return out;
    }

    // @see com.sam.moca.server.exec.SystemContext#getVariable(java.lang.String)
    @Override
    public String getVariable(String name) {
        String envValue = System.getenv(name.toUpperCase());
        if (envValue != null) {
            return envValue;
        }
        else {
            String entry = ENVIRONMENT_SECTION + "." + name;
            String value = this.getConfigurationElement(entry);
            
            if (value == null && (name.equalsIgnoreCase("JAVA") || name.equalsIgnoreCase("JAVA32"))) {
                if (name.equalsIgnoreCase("JAVA32")) {
                    value = this.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VM32);
                }
            
                if (value == null) {
                    value = this.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VM);
                }
                
                if (value == null) {
                    value = "java";
                }
                
                // We need to add -Xrs to all Windows versions < 6.0, which equates to 
                // Windows 7 and Windows Server 2008.             
                try {
                    String osName = System.getProperty("os.name").toLowerCase(); 
                    Double osVersion = Double.parseDouble(System.getProperty("os.version"));   
                    
                    if (osName.contains("windows") && osVersion < 6.0)
                        value += " -Xrs";
                }
                catch (NumberFormatException ignore) {
                    // Ignore exceptions raised parsing the OS version.
                    ;
                }
            }

            return value;
        }
    }

    // @see com.sam.moca.server.exec.SystemContext#isVariableMapped(java.lang.String)
    @Override
    public boolean isVariableMapped(String name) {
        if (System.getenv(name.toUpperCase()) != null) {
            return true;
        }
        else {
            synchronized (_registry) {
                Map<String, String> envMap = _registry.get(ENVIRONMENT_SECTION);
                if (envMap == null) {
                    return false;
                }
                else {
                    return (envMap.containsKey(name.toLowerCase()));
                }
            }
        }
    }
    
    // @see com.sam.moca.server.exec.SystemContext#overrideConfigurationElement(java.lang.String, java.lang.String, java.lang.String)
    @Override
    public void overrideConfigurationElement(String key, String value) {
        String[] tmp = parseEntry(key);
        String section = tmp[0];
        String name = tmp[1];
        
        synchronized (_registry) {
            Map<String, String> sectionMap = _registry.get(section.toLowerCase());
            if (sectionMap != null) {
                sectionMap.put(name.toLowerCase(), value);
            }
            else {
                sectionMap = new LinkedHashMap<String, String>();
                sectionMap.put(name.toLowerCase(), value);
                _registry.put(section.toLowerCase(), sectionMap);
            }
        }
    }
    
    //
    // Implementation
    //
    private String expand(final String value, final String key, final String section) {
        return new VarStringReplacer(new StringReplacer.ReplacementStrategy() {
            @Override
            public String lookup(String varName) {
                // Special case to avoid recursive lookups
                if (varName.equalsIgnoreCase(key) && section.equals(ENVIRONMENT_SECTION)) {
                    return null;
                }
                return getVariable(varName);
            }
        }).translate(value);
    }
    
    private void init(Reader regReader) throws SystemConfigurationException{
        init(regReader, new HashSet<String>());
    }
    
    private void init(Reader regReader, Set<String> filenames) throws SystemConfigurationException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(regReader);
            String section = null;
            String origLine;
            while ((origLine = in.readLine()) != null) {

                String line = origLine.trim();
                Matcher matcher = INCLUDE_PATTERN.matcher(line);
                // Section identifier is contained in brackets
                // Comment lines start with #
                // The rest of the file contains key/value pairs separated by =
                
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length() - 1).trim().toLowerCase();
                }
                //We found an include, lets load that one first before doing the
                //rest. We could possibly override the include.
                else if (matcher.matches()){;
                    String fileInclude = matcher.group(1);
                    fileInclude = expand(fileInclude, "", "");
                    if(!filenames.add(fileInclude)){
                        throw new IllegalStateException("MOCA registry recursive include detected.");
                    }
                    Reader reader = new InputStreamReader(new FileInputStream(fileInclude), "UTF-8");
                    init(reader, filenames);
                }
                else if (line.startsWith("#")) {
                    // Ignore comments
                }
                else if (section != null) {
                    int eqPos = line.indexOf('=');
                    if (eqPos != -1) {
                        String key = line.substring(0, eqPos).trim().toLowerCase();
                        String value = line.substring(eqPos + 1).trim();
                        Map<String, String> sectionMap = _registry.get(section);
                        if (sectionMap == null) {
                            sectionMap = new LinkedHashMap<String, String>();
                            _registry.put(section, sectionMap);
                        }
                        sectionMap.put(key, value);
                    }
                }
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new SystemConfigurationException("Error reading registry file: " + regReader, e);
        }
        finally {
            if (in != null) {
                try { in.close(); } catch (IOException ignore) {}
            }
        }
    }
    
    // Split apart a registry entry of the form "section.key". 
    private String[] parseEntry(String entry)
    {   
        if (entry == null)
            return null;
        
        String[] sectionKey = entry.split("\\.", 2);
        
        if (sectionKey.length == 2) {
            return sectionKey;
        }
        else {
            return new String[] {"GLOBAL", entry};
        }
    }
    
    // Decode blowfish-encoded values
    // Decoding Algorithm:
    //
    //     encoded password e1 = "|B|<b64>"
    //     base-64 text b64 = e1 + 3 characters
    //     concatenated text c1 = B64Decode(b64)
    //     blowfish key k1 = First BLOWFISH_KEY_LENGTH bytes of c1
    //     blowfish text b1 = Remaining bytes of c1
    //     blowfish text b1 = BlowfishDecode(key = k1, text = c1)
    //     password p1 = BlowfishDecode(b1)
    private String decodeValue(String encoded) {
        
        // Blowfish encoding is indicated by a prefix |B|
        if (encoded == null || !encoded.startsWith("|B|")) {
            return encoded;
        }
        else {
            byte[] raw = Base64.decode(encoded.substring(3));
            Cipher blowfish;
            try {
                blowfish = Cipher.getInstance("Blowfish");
                byte[] keyBytes = new byte[16];
                System.arraycopy(raw, 0, keyBytes, 0, 16);
                SecretKeySpec key = new SecretKeySpec(keyBytes, "Blowfish");
                byte[] dataBytes = new byte[raw.length - 16];
                System.arraycopy(raw, 16, dataBytes, 0, raw.length - 16);

                blowfish.init(Cipher.DECRYPT_MODE, key);
                byte[] clearBytes = blowfish.doFinal(dataBytes);
                
                // Special case -- if the string contains a 0 as the last byte,
                // remove it.  This is a remnant of the C convention of using
                // null-terminated strings and encoding them with the null for
                // convenience of memory allocation.
                try {
                    if (clearBytes.length > 0
                            && clearBytes[clearBytes.length - 1] == (byte) 0) {
                        return new String(clearBytes, 0, clearBytes.length - 1,
                            "UTF-8");
                    }
                    else {
                        return new String(clearBytes, "UTF-8");

                    }
                }
                catch (UnsupportedEncodingException e) {
                    // Should never happen.
                    throw new MocaRuntimeException(502, e.getMessage());
                }
            }
            catch (GeneralSecurityException e) {
                throw new IllegalArgumentException("unable to decode key", e);
            }
        }
    }

    // @see com.sam.moca.server.exec.SystemContext#getDataFile(java.io.FilenameFilter)
    @Override
    public File getDataFile(FilenameFilter filter) throws IllegalArgumentException {
        return getDataFile(filter, false);
    }
    
    // @see com.sam.moca.server.exec.SystemContext#getDataFile(java.io.FilenameFilter, boolean)
    @Override
    public File getDataFile(FilenameFilter filter, boolean reverseOrder) 
            throws IllegalArgumentException {
        String pathSeparatedConfigDir = getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_PROD_DIRS);
        
        if (pathSeparatedConfigDir == null || 
                pathSeparatedConfigDir.trim().length() == 0) {
            throw new IllegalArgumentException("The prod-dirs registry value is not configured!");
        }
        
        String[] configDirs = pathSeparatedConfigDir.split(File.pathSeparator);
        
        // Now loop through all the configuration directories in reverse order
        for (int i = 0; i < configDirs.length; i++) {
            int pos = i;
            // If it is reverse order then we have to go from the back
            if (reverseOrder) {
                pos = configDirs.length - i - 1;
            }
            String configDir = configDirs[pos];
            String expandedDirectory = MocaUtils.expandEnvironmentVariables(
                    this, configDir);
            
            File configFile = new File(expandedDirectory + DATA);
            
            File[] foundFiles = configFile.listFiles(filter);
            
            // If there was any file found then return the first one
            if (foundFiles != null && foundFiles.length > 0) {
                return foundFiles[0];
            }
        }
        
        return null;
    }

    // @see com.sam.moca.server.exec.SystemContext#getDataFiles(java.io.FilenameFilter)
    @Override
    public File[] getDataFiles(FilenameFilter filter) throws IllegalArgumentException {
        return getDataFiles(filter, false);
    }
    
    // @see com.sam.moca.server.exec.SystemContext#getDataFiles(java.io.FilenameFilter)
    @Override
    public File[] getDataFiles(FilenameFilter filter, boolean reverseOrder) throws IllegalArgumentException {
        String pathSeparatedConfigDir = getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_PROD_DIRS);
        
        if (pathSeparatedConfigDir == null || 
                pathSeparatedConfigDir.trim().length() == 0) {
            throw new IllegalArgumentException("The prod-dirs registry value is not configured!");
        }
        
        String[] prodDirs = pathSeparatedConfigDir.split(File.pathSeparator);
        
        List<File> actualConfigFiles = new ArrayList<File>();
        
        // Now loop through all the configuration directories, so we can find
        // which ones contain the xml and add it to our list
        for (String prodDir : prodDirs) {
            String expandedDirectory = MocaUtils.expandEnvironmentVariables(
                    this, prodDir);
            
            File configFile = new File(expandedDirectory + DATA);
            
            File[] foundFiles = configFile.listFiles(filter);
            
            if (foundFiles != null && foundFiles.length > 0) {
                // Now add all the files into the list
                Collections.addAll(actualConfigFiles, foundFiles);
            }
        }
        
        // If we want it reversed do that before return.
        if (reverseOrder) {
            Collections.reverse(actualConfigFiles);
        }
        
        return actualConfigFiles.toArray(new File[actualConfigFiles.size()]);
    }
    
    @Override
    public Object getAttribute(String name) {
        synchronized (_attrs) {
            return _attrs.get(name.toUpperCase());
        }
    }
    
    @Override
    public void putAttribute(String name, Object value) {
        synchronized (_attrs) {
            _attrs.put(name.toUpperCase(), value);
        }
    }

    @Override
    public Object removeAttribute(String name) {
        synchronized (_attrs) {
            return _attrs.remove(name.toUpperCase());
        }
    }
    
    // @see com.sam.moca.server.exec.SystemContext#clearAttributes()
    @Override
    public void clearAttributes() {
        synchronized (_attrs) {
            _attrs.clear();
        }
    }
     
    // @see com.sam.moca.server.exec.SystemContext#toString()
    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        
        synchronized (_registry) {
            // Iterate through each registry section.
            for (Map.Entry<String, Map<String, String>> entry : 
                _registry.entrySet()) {
                String section = entry.getKey();
    
                // Append the section name to the string buffer.
                content.append('[');
                content.append(section.toUpperCase());
                content.append(']');
                content.append('\n');
    
                // Iterate through each registry key within this section.
                Map<String, String> sectionMap = entry.getValue();
                
                for (Map.Entry<String, String> key : sectionMap.entrySet()) {
                    String name = key.getKey();
                    String value = key.getValue();
                    String expandedValue = expand(value, name, section);
    
                    // Append the name/value pair to the string buffer.
                    content.append(name);
                    content.append('=');
                    content.append(expandedValue);
                    content.append('\n');
                }
    
                content.append('\n');
            }
        }

        return content.toString();
    }
    
    
    private final Map<String, Map<String, String>> _registry = new LinkedHashMap<String, Map<String, String>>();
    private final Map<String, Object> _attrs = new LinkedHashMap<String, Object>();
    private static final String ENVIRONMENT_SECTION = "environment";
    private static final String DATA = File.separator + "data";
    private static final Pattern INCLUDE_PATTERN = Pattern.compile("\\s*#\\s*include<(.*)>\\s*");
}
