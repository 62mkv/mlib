/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.applications.cobertura;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.javancss.FunctionMetric;
import net.sourceforge.cobertura.javancss.Javancss;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Source;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Allows complexity computing for source files, packages and a whole project. Average
 * McCabe's number for methods contained in the specified entity is returned. This class
 * depends on FileFinder which is used to map source file names to existing files.
 * 
 * <p>One instance of this class should be used for the same set of source files - an 
 * object of this class can cache computed results.</p>
 * 
 * @author Grzegorz Lukasik
 */
public class CoberturaComplexityCalculator implements ComplexityCalculator {
        private static final Logger logger = LogManager.getLogger(ComplexityCalculator.class);

        public static final Complexity ZERO_COMPLEXITY = new Complexity();
        
        // Finder used to map source file names to existing files
        private final FileFinder finder;
        
        // Contains pairs (String sourceFileName, Complexity complexity)
        @SuppressWarnings("rawtypes")
        private Map sourceFileCNNCache = new HashMap();

        // Contains pairs (String packageName, Complexity complexity)
        @SuppressWarnings("rawtypes")
        private Map packageCNNCache = new HashMap();

        /**
         * Creates new calculator. Passed {@link FileFinder} will be used to 
         * map source file names to existing files when needed. 
         * 
         * @param finder {@link FileFinder} that allows to find source files
         * @throws NullPointerException if finder is null
         */
        public CoberturaComplexityCalculator( FileFinder finder) {
                if( finder==null)
                        throw new NullPointerException();
                this.finder = finder;
        }
        
        /**
         * Calculates the code complexity number for an input stream.
         * "CCN" stands for "code complexity number."  This is
         * sometimes referred to as McCabe's number.  This method
         * calculates the average cyclomatic code complexity of all
         * methods of all classes in a given directory.  
         *
         * @param file The input stream for which you want to calculate
         *        the complexity
         * @return average complexity for the specified input stream 
         */
        @SuppressWarnings("rawtypes")
        private Complexity getAccumlatedCCNForSource(String sourceFileName, Source source) {
                if (source == null)
                {
                        return ZERO_COMPLEXITY;
                }
                if (!sourceFileName.endsWith(".java"))
                {
                        return ZERO_COMPLEXITY;
                }
                Javancss javancss = new Javancss(source.getInputStream());

                if (javancss.getLastErrorMessage() != null)
                {
                        //there is an error while parsing the java file. log it
                        logger.warn("JavaNCSS got an error while parsing the java " + source.getOriginDesc() + "\n" 
                                                + javancss.getLastErrorMessage());
                }

                List methodMetrics = javancss.getFunctionMetrics();
                int classCcn = 0;
        for( Iterator method = methodMetrics.iterator(); method.hasNext();)
        {
                FunctionMetric singleMethodMetrics = (FunctionMetric)method.next();
                classCcn += singleMethodMetrics.ccn;
        }
                
                return new Complexity( classCcn, methodMetrics.size());
        }

        /**
         * Calculates the code complexity number for single source file.
         * "CCN" stands for "code complexity number."  This is
         * sometimes referred to as McCabe's number.  This method
         * calculates the average cyclomatic code complexity of all
         * methods of all classes in a given directory.  
         * @param sourceFileName 
         *
         * @param file The source file for which you want to calculate
         *        the complexity
         * @return average complexity for the specified source file 
         * @throws IOException 
         */
        private Complexity getAccumlatedCCNForSingleFile(String sourceFileName) throws IOException {
                Source source = finder.getSource(sourceFileName);
                try
                {
                return getAccumlatedCCNForSource(sourceFileName, source);
                }
                finally
                {
                        if (source != null)
                        {
                                source.close();
                        }
                }
        }

        // @see com.sam.moca.applications.cobertura.ComplexityCalculator#getCCNForProject(net.sourceforge.cobertura.coveragedata.ProjectData)
        
        @Override
        @SuppressWarnings("rawtypes")
        public double getCCNForProject( ProjectData projectData) {
                // Sum complexity for all packages
                Complexity act = new Complexity();
                for( Iterator it = projectData.getPackages().iterator(); it.hasNext();) {
                        PackageData packageData = (PackageData)it.next();
                        act.add( getCCNForPackageInternal( packageData));
                }

                // Return average CCN for source files
                return act.averageCCN();
        }
        
        // @see com.sam.moca.applications.cobertura.ComplexityCalculator#getCCNForPackage(net.sourceforge.cobertura.coveragedata.PackageData)
        
        @Override
        public double getCCNForPackage(PackageData packageData) {
                return getCCNForPackageInternal(packageData).averageCCN();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private Complexity getCCNForPackageInternal(PackageData packageData) {
                // Return CCN if computed earlier
                Complexity cachedCCN = (Complexity) packageCNNCache.get( packageData.getName());
                if( cachedCCN!=null) {
                        return cachedCCN;
                }
                
                // Compute CCN for all source files inside package
                Complexity act = new Complexity();
                for( Iterator it = packageData.getSourceFiles().iterator(); it.hasNext();) {
                        SourceFileData sourceData = (SourceFileData)it.next();
                        act.add( getCCNForSourceFileNameInternal( sourceData.getName()));
                }
                
                // Cache result and return it
                packageCNNCache.put( packageData.getName(), act);
                return act;
        }

        
        // @see com.sam.moca.applications.cobertura.ComplexityCalculator#getCCNForSourceFile(net.sourceforge.cobertura.coveragedata.SourceFileData)
        
        @Override
        public double getCCNForSourceFile(SourceFileData sourceFile) {
                return getCCNForSourceFileNameInternal( sourceFile.getName()).averageCCN();
        }

        @SuppressWarnings("unchecked")
        private Complexity getCCNForSourceFileNameInternal(String sourceFileName) {
                // Return CCN if computed earlier
                Complexity cachedCCN = (Complexity) sourceFileCNNCache.get( sourceFileName);
                if( cachedCCN!=null) {
                        return cachedCCN;
                }

            // Compute CCN and cache it for further use
                Complexity result = ZERO_COMPLEXITY;
                try {
                        result = getAccumlatedCCNForSingleFile( sourceFileName );
                } catch( IOException ex) {
                        logger.info( "Cannot find source file during CCN computation, source=["+sourceFileName+"]");
                }
                sourceFileCNNCache.put( sourceFileName, result);
                return result;
        }

        // @see com.sam.moca.applications.cobertura.ComplexityCalculator#getCCNForClass(net.sourceforge.cobertura.coveragedata.ClassData)
        
        @Override
        public double getCCNForClass(ClassData classData) {
                return getCCNForSourceFileNameInternal( classData.getSourceFileName()).averageCCN();
        }


        /**
         * Represents complexity of source file, package or project. Stores the number of

import org.apache.logging.log4j.LogManager;         * methods inside entity and accumlated complexity for these methods.
         */
        private static class Complexity {
                private double accumlatedCCN;
                private int methodsNum;
                public Complexity(double accumlatedCCN, int methodsNum) {
                        this.accumlatedCCN = accumlatedCCN;
                        this.methodsNum = methodsNum;
                }
                public Complexity() {
                        this(0,0);
                }
                public double averageCCN() {
                        if( methodsNum==0) {
                                return 0;
                        }
                        return accumlatedCCN/methodsNum;
                }
                public void add( Complexity second) {
                        accumlatedCCN += second.accumlatedCCN;
                        methodsNum += second.methodsNum;
                }
        }
}

