/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.server.db.translate;

/**
 * A SQLElement implementation that can be used independent of the original SQL
 * statement.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ReplacementElement implements SQLElement {

    /**
     * Creates a general-purpose element.  A SQLElement created from this
     * constructor has a type of <code>TokenType.OTHER</code>, and a single
     * space character as its whitespace.
     * @param text the text value of the SQL element. 
     */
    public ReplacementElement(String text) {
        this(TokenType.OTHER, text, " ");
    }
    
    /**
     * Creates any kind of element with any token, value and whitespace value.
     * @param type the <code>TokenType</code> value to use for this element.
     * @param text the value to use for this element.
     * @param whitespace the whitespace to use for this element. Whitespace is
     * always placed before the text of the element when SQL elements are
     * rendered as strings.
     */
    public ReplacementElement(TokenType type, String text, String whitespace) {
        _type = type;
        _text = text;
        _whitespace = whitespace;
    }

    // @see com.sam.moca.db.translate.SQLElement#getType()
    public TokenType getType() {
        return _type;
    }

    // @see com.sam.moca.db.translate.SQLElement#getValue()
    public String getValue() {
        return _text;
    }
    
    // @see com.sam.moca.db.translate.SQLElement#getLeadingWhitespace
    public String getLeadingWhitespace() {
        return _whitespace;
    }
    
    public String toString() {
        return _whitespace + _text;
    }
    
    //
    // Implementation
    //
    private TokenType _type;
    private String _text;
    private String _whitespace;
}
