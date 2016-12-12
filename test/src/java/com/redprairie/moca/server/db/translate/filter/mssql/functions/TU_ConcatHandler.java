/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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
 * This class tests the translation of the oracle concat to MSSql.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_ConcatHandler extends AbstractMocaTestCase{
    
    /***
     * This test will check to make sure we get a TranslationException when too
     * few arguments are passed.
     */
    @Test
    public void testConcatHandlerTranslateTooFewArgsException(){
        List<List<SQLElement>> listElements = new ArrayList<List<SQLElement>>();
        List<SQLElement> element1 = new ArrayList<SQLElement>();
        
        listElements.add(element1);
        
        BindList bindArgs = new BindList();
        ConcatHandler concatHand = new ConcatHandler();
        try {
            concatHand.translate("Test", listElements, bindArgs);
            fail("Given 1 arguments should have failed in a TranslationException.");
        }
        catch (TranslationException e) {

        }
        

    }
    
    /***
     * This test will check to make sure we get a TranslationException when too
     * many arguments are passed.
     */
    @Test
    public void testConcatHandlerTranslateTooManyArgsException(){
        List<List<SQLElement>> listElements = new ArrayList<List<SQLElement>>();
        List<SQLElement> element1 = new ArrayList<SQLElement>();
        List<SQLElement> element2 = new ArrayList<SQLElement>();
        List<SQLElement> element3 = new ArrayList<SQLElement>();
        
        listElements.add(element1);
        listElements.add(element2);
        listElements.add(element3);
        
        BindList bindArgs = new BindList();
        ConcatHandler concatHand = new ConcatHandler();
        try {
            concatHand.translate("Test", listElements, bindArgs);
            fail("Given 3 arguments should have failed in a TranslationException.");
        }
        catch (TranslationException e) {

        }
        
    }
    
    /***
     * This test will check to make sure we get the proper translation for a 
     * concat call to MSSql.
     * 
     * @throws TranslationException
     */
    @Test
    public void testConcatHandlerTranslate() throws TranslationException{
        List<List<SQLElement>> listElements = new ArrayList<List<SQLElement>>();
        SQLElement element1 = Mockito.mock(SQLElement.class);
        SQLElement element2 = Mockito.mock(SQLElement.class);
        
        List<SQLElement> elementList1 = new ArrayList<SQLElement>();
        List<SQLElement> elementList2 = new ArrayList<SQLElement>();
        
        elementList1.add(element1);
        elementList2.add(element2);
        
        listElements.add(elementList1);
        listElements.add(elementList2);
        
        BindList bindArgs = new BindList();
        ConcatHandler concatHand = new ConcatHandler();
        List<SQLElement> checkList = concatHand.translate("Test", listElements, bindArgs);
        
        //Expected Result: (CONVERT(VARCHAR(8000), element1) + CONVERT(VARCHAR(8000), element2))
        assertEquals(checkList.size(), 21);
        assertEquals(checkList.get(0).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(1).getType(), TokenType.WORD);
        assertEquals(checkList.get(1).getValue(), "convert");
        assertEquals(checkList.get(2).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(3).getType(), TokenType.WORD);
        assertEquals(checkList.get(3).getValue(), "varchar");
        assertEquals(checkList.get(4).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(5).getType(), TokenType.INT_LITERAL);
        assertEquals(checkList.get(6).getType(), TokenType.RIGHTPAREN);
        assertEquals(checkList.get(7).getType(), TokenType.COMMA);
        assertEquals(checkList.get(8),element1);
        assertEquals(checkList.get(9).getType(), TokenType.RIGHTPAREN);
        assertEquals(checkList.get(10).getType(), TokenType.PLUS);
        assertEquals(checkList.get(11).getType(), TokenType.WORD);
        assertEquals(checkList.get(11).getValue(), "convert");
        assertEquals(checkList.get(12).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(13).getType(), TokenType.WORD);
        assertEquals(checkList.get(13).getValue(), "varchar");
        assertEquals(checkList.get(14).getType(), TokenType.LEFTPAREN);
        assertEquals(checkList.get(15).getType(), TokenType.INT_LITERAL);
        assertEquals(checkList.get(16).getType(), TokenType.RIGHTPAREN);
        assertEquals(checkList.get(17).getType(), TokenType.COMMA);
        assertEquals(checkList.get(18),element2);
        assertEquals(checkList.get(19).getType(), TokenType.RIGHTPAREN);
        assertEquals(checkList.get(20).getType(), TokenType.RIGHTPAREN);
        
        
        
        
    }
}
