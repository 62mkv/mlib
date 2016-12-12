/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.crud;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * A Unit test class for the {@link TableFactory} class.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class TU_TableFactory extends AbstractMocaTestCase {

    /** Tests wither meta data can be retrieved from the system.
     * 
     */
    public void testGetTableMetaData() throws MocaException {
        
        TableDefinition def = TableFactory.getTableDefinition("comp_ver");
        
        assertNotNull(def);
        assertEquals(12, def.getColumns().size());
        assertEquals(5, def.getPKFields().size());
        assertEquals(10, def.getRequiredFields().size());
        
        ColumnDefinition column = def.getColumn("COMP_NEED_FW");
        assertNotNull(column);
        assertEquals("COMP_NEED_FW", column.getColumnName().toUpperCase());
        assertEquals(MocaType.INTEGER, column.getDataType());
        assertEquals(4, column.getLength());
        assertEquals(false, column.isPKField());
        assertEquals(false, column.isIdentity());
        assertEquals(true, column.isNullable());
        assertEquals(false, column.isRequired());
    }
 
    /** Tests that a NotFoundException is thrown on a non-existent table.
     * 
     */
    public void testGetTableMetaDataNotFound() {
        try {
            TableFactory.getTableDefinition("notfoundtable");
            
            fail("NotFoundException was not thrown while getting definition");
        }
        catch (NotFoundException e) {
        }
    }
    
}
