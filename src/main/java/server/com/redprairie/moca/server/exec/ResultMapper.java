/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.server.exec;

import java.lang.reflect.Array;
import java.util.Collection;

import com.redprairie.moca.BeanResults;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;

/**
 * Maps component return values to WrappedResults.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ResultMapper {
    /**
     * Create a result set from another object.
     * @param result the object to translate into a MocaResults object.
     * @return a WrappedResults object that corresponds to the given
     * result.
     */
    public static MocaResults createResults(Object result)  {
        if (result instanceof MocaResults) {
            return (MocaResults)result;
        }
        else {
            MocaResults out;
            Class<?> resultClass = result.getClass();
            MocaType type = MocaType.lookupClass(resultClass);

            if (!type.equals(MocaType.UNKNOWN) && !type.equals(MocaType.OBJECT)) {
                EditableResults res = new SimpleResults();
                res.addColumn("result", type);
                res.addRow();
                res.setValue(0, result);
                out = res;
            }
            else {
                if (resultClass.isArray()) {
                    type = MocaType.lookupClass(resultClass.getComponentType());
                    if (!type.equals(MocaType.UNKNOWN) && !type.equals(MocaType.OBJECT)) {
                        EditableResults res = new SimpleResults();
                        res.addColumn("result", type);
                        int length = Array.getLength(result);
                        for (int i = 0; i < length; i++) {
                            res.addRow();
                            res.setValue(0, Array.get(result, i));
                        }
                        out = res;
                    }
                    else {
                        out = new BeanResults<Object>((Object[])result);
                    }
                }
                else if (result instanceof Collection<?>) {
                    if (((Collection<?>) result).isEmpty()) {
                        out = new SimpleResults();
                    }
                    else {
                        // We cannot type this (leave raw) else we will fool 
                        // the compiler into using the Object arg constructor 
                        // instead of the collection one
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        BeanResults<Object> temp = new BeanResults(
                                (Collection<?>)result);
                        out = temp;
                    }
                }
                else {
                    out = new BeanResults<Object>(result);
                }
            }

            return out;
        }
    }
}
