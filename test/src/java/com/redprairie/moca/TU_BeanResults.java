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

package com.redprairie.moca;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

/**
 * Unit test for <code>BeanResults</code>
 *
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class TU_BeanResults extends TU_AbstractModifiableResults {

    public void testCaseInsensitiveColumns() {
        // override this method, since case-insensitive columns are unimportant
        // for this implementation
    }
    

    public static class _ResultBean {

        public _ResultBean() {
        }

        _ResultBean(String columnOne, Date columnTwo, Integer columnThree,
                    Double columnFour, Boolean columnFive, byte[] columnSix) {
            _columnOne = columnOne;
            _columnTwo = columnTwo;
            _columnThree = columnThree;
            _columnFour = columnFour;
            _columnFive = columnFive;
            _columnSix = columnSix;
        }
        
        public Boolean getColumnFive() {
            return _columnFive;
        }
        
        public Double getColumnFour() {
            return _columnFour;
        }
        
        public String getColumnOne() {
            return _columnOne;
        }

        public byte[] getColumnSix() {
            return _columnSix;
        }
        
        public Integer getColumnThree() {
            return _columnThree;
        }
        
        /**
         * @return Returns the columnTwo.
         */
        public Date getColumnTwo() {
            return _columnTwo;
        }

        
        /**
         * @param columnFive The columnFive to set.
         */
        public void setColumnFive(Boolean columnFive) {
            _columnFive = columnFive;
        }

        /**
         * @param columnFour The columnFour to set.
         */
        public void setColumnFour(Double columnFour) {
            _columnFour = columnFour;
        }

        /**
         * @param columnOne The columnOne to set.
         */
        public void setColumnOne(String columnOne) {
            _columnOne = columnOne;
        }

        /**
         * @param columnSix The columnSix to set.
         */
        public void setColumnSix(byte[] columnSix) {
            _columnSix = columnSix;
        }

        /**
         * @param columnThree The columnThree to set.
         */
        public void setColumnThree(Integer columnThree) {
            _columnThree = columnThree;
        }

        /**
         * @param columnTwo The columnTwo to set.
         */
        public void setColumnTwo(Date columnTwo) {
            _columnTwo = columnTwo;
        }

        private String _columnOne;
        private Date _columnTwo;
        private Integer _columnThree;
        private Double _columnFour;
        private Boolean _columnFive;
        private byte[] _columnSix;

    }
    
    public static class _NestedResultBean  extends _ResultBean {
        
        /**
         * @return Returns the results.
         */
        public _ResultBean[] getResults() {
            return _results;
        }
        
        /**
         * @param results The results to set.
         */
        public void setResults(_ResultBean[] results) {
            _results = results;
        }
        
        private _ResultBean[] _results;
    }
    
    public void testGetResultData() throws Exception {
        BeanResults<_ResultBean> res = new BeanResults<_ResultBean>(_ResultBean.class);
        res.addRow();
        res.setStringValue("columnOne", "Blah ");
        res.setDateValue("columnTwo", null);
        res.setIntValue("columnThree", 900);
        res.setDoubleValue("columnFour", Math.E);
        res.setBooleanValue("columnFive", false);
        res.setBinaryValue("columnSix", "foo".getBytes(Charset.forName("UTF-8")));
        
        _ResultBean[] data = res.getData();
        assertEquals(1, data.length);
        assertEquals("Blah ", data[0].getColumnOne());
        assertNull(data[0].getColumnTwo());
        assertEquals(900, data[0].getColumnThree().intValue());
        assertEquals(Math.E, data[0].getColumnFour().doubleValue(), 0.0);
        assertEquals(false, data[0].getColumnFive().booleanValue());
        assertTrue(Arrays.equals("foo".getBytes(Charset.forName("UTF-8")), data[0].getColumnSix()));
    }

    public void testGetResultDataFromInitialArray() throws Exception {
        _ResultBean[] origData = new _ResultBean[1];
        origData[0] = new _ResultBean(null, null, null, null, null, null);
        BeanResults<_ResultBean> res = new BeanResults<_ResultBean>(origData);
        res.addRow();
        res.setStringValue("columnOne", "Blah ");
        res.setDateValue("columnTwo", null);
        res.setIntValue("columnThree", 900);
        res.setDoubleValue("columnFour", Math.E);
        res.setBooleanValue("columnFive", false);
        res.setBinaryValue("columnSix", "foo".getBytes(Charset.forName("UTF-8")));
        
        _ResultBean[] data = res.getData();
        assertEquals(2, data.length);
        assertEquals("Blah ", data[1].getColumnOne());
        assertNull(data[1].getColumnTwo());
        assertEquals(900, data[1].getColumnThree().intValue());
        assertEquals(Math.E, data[1].getColumnFour().doubleValue(), 0.0);
        assertEquals(false, data[1].getColumnFive().booleanValue());
        assertTrue(Arrays.equals("foo".getBytes(Charset.forName("UTF-8")), data[1].getColumnSix()));
    }

    protected MocaResults _createSampleResults() {
        Date now = new Date();
        _ResultBean[] data = new _ResultBean[3];
        data[0] = new _ResultBean("Hello", now, Integer.valueOf(1), Double.valueOf(3.142), Boolean.TRUE,
                new byte[] {7,6,5,4,3,2,1,0,127}); 
        data[1] = new _ResultBean("Blah ", new Date (now.getTime()+ 3600*1000),
                Integer.valueOf(900), Double.valueOf(Math.E), Boolean.FALSE, "foo".getBytes(Charset.forName("UTF-8")));
        data[2] = new _ResultBean(null, null, null, null, null, null);
        return new BeanResults<_ResultBean>(data);
    }
    
    protected ModifiableResults _createEmptyNestedResults() {
        return new BeanResults<_NestedResultBean>(_NestedResultBean.class);
    }

    
    protected boolean _checkColumnPosition() {
        return false;
    }

    // @see com.redprairie.moca.TU_AbstractEditableResults#_createEmptyResults()
    @Override
    protected ModifiableResults _createEmptyResults() {
        return new BeanResults<_ResultBean>(_ResultBean.class);
    }
}


