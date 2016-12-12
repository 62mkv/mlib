/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;

/**
 * This class is to test some input and output values of the MSSQL 
 * ToDateHandler
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_ToDateHandler {

    @Before
    public void beforeEachTest() {
        _toDateHandler = new ToDateHandler();
    }
    
    @Test
    public void testNStringLiteralArgument() throws TranslationException {
        BindList bindList = new BindList();
        List<List<SQLElement>> args = new ArrayList<List<SQLElement>>();
        List<SQLElement> element = new ArrayList<SQLElement>();
        final String dateFormat = "'12252009120000'";
        
        element.add(new TestNStringSQLElement("N" + dateFormat, TokenType.NSTRING_LITERAL, " "));
        args.add(element);
        List<SQLElement> returnElements = _toDateHandler.translate("test", args, 
                bindList);
        
        Assert.assertEquals(8, returnElements.size());
    }
    
    private static class TestNStringSQLElement implements SQLElement {

        /**
         * @param value
         * @param type
         * @param whitespace
         */
        public TestNStringSQLElement(String value, TokenType type,
                String leadingWhitespace) {
            super();
            _value = value;
            _type = type;
            _leadingWhitespace = leadingWhitespace;
        }

        // @see com.redprairie.moca.server.db.translate.SQLElement#getValue()
        public String getValue() {
            return _value;
        }

        // @see com.redprairie.moca.server.db.translate.SQLElement#getType()
        public TokenType getType() {
            return _type;
        }

        // @see com.redprairie.moca.server.db.translate.SQLElement#getLeadingWhitespace()
        public String getLeadingWhitespace() {
            return _leadingWhitespace;
        }

        private final String _value;
        private final TokenType _type;
        private final String _leadingWhitespace;
    }
    
    private ToDateHandler _toDateHandler;
}
