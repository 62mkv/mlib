/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.db.translate.filter.mssql.functions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.util.AbstractMocaTestCase;



/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_ChrHandler extends AbstractMocaTestCase {
    @Test
    public void testChrHandlerTranslateException(){
        List<List<SQLElement>> listElements = new ArrayList<List<SQLElement>>();
        List<SQLElement> element1 = new ArrayList<SQLElement>();
        List<SQLElement> element2 = new ArrayList<SQLElement>();
        
        listElements.add(element1);
        listElements.add(element2);
        
        BindList bindArgs = new BindList();
        ChrHandler chrHand = new ChrHandler();
        try {
            chrHand.translate("Test", listElements, bindArgs);
            fail("Given 2 arguments should have failed in a TranslationException.");
        }
        catch (TranslationException e) {

        }
        

    }
    
    @Test
    public void testChrHandlerTranslate() throws TranslationException{
        List<List<SQLElement>> listElements = new ArrayList<List<SQLElement>>();
        SQLElement element1 = Mockito.mock(SQLElement.class);

        List<SQLElement> elements = new ArrayList<SQLElement>();
        elements.add(element1);
        
        listElements.add(elements);
        
        BindList bindArgs = new BindList();
        ChrHandler chrHand = new ChrHandler();
        List<SQLElement> checkList = chrHand.translate("Test", listElements, bindArgs);
        
        assertEquals(checkList.size(), 11);
        assertEquals(checkList.get(0).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(1).getType(), TokenType.WORD);
        assertEquals(checkList.get(2).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(3).getType(), TokenType.WORD);
        assertEquals(checkList.get(4).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(5), element1);
        assertEquals(checkList.get(6).getType(), TokenType.RIGHTPAREN);
        assertEquals(checkList.get(7).getType(), TokenType.WORD);
        assertEquals(checkList.get(8).getType(), TokenType.WORD);
        assertEquals(checkList.get(9).getType(), TokenType.RIGHTPAREN);
        assertEquals(checkList.get(10).getType(), TokenType.RIGHTPAREN);
    }
}
