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

package com.redprairie.moca;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * Unit test for <code>BeanResults</code> using an annotation-based bean.
 *
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class TU_BeanResultsAnnotation extends TU_AbstractModifiableResults {

    public void testCaseInsensitiveColumns() {
        // override this method, since case-insensitive columns are unimportant
        // for this implementation
    }
    

    public static class _AnnotatedResultBean {
        
        /**
         * @param columnTwo The columnTwo to set.
         */
        @MocaColumn(name="columnTwo")
        public void setFoo(Date columnTwo) {
            _columnTwo = columnTwo;
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

        public _AnnotatedResultBean() {
            
        }
        
        _AnnotatedResultBean(String columnOne, Date columnTwo, Integer columnThree,
                    Double columnFour, Boolean columnFive, byte[] columnSix) {
            _columnOne = columnOne;
            _columnTwo = columnTwo;
            _columnThree = columnThree;
            _columnFour = columnFour;
            _columnFive = columnFive;
            _columnSix = columnSix;
        }
        
        @MocaColumn(order=5)
        public Boolean getColumnFive() {
            return _columnFive;
        }
        
        @MocaColumn(order=4)
        public Double getColumnFour() {
            return _columnFour;
        }
        
        @MocaColumn(order=1)
        public String getColumnOne() {
            return _columnOne;
        }

        @MocaColumn(order=6)
        public byte[] getColumnSix() {
            return _columnSix;
        }
        
        @MocaColumn(order=3)
        public Integer getColumnThree() {
            return _columnThree;
        }
        
        @MocaColumn(order=2,name="columnTwo")
        public Date getFoo() {
            return _columnTwo;
        }
        
        private String _columnOne;
        private Date _columnTwo;
        private Integer _columnThree;
        private Double _columnFour;
        private Boolean _columnFive;
        private byte[] _columnSix;

    }
    
    public static class _AnnotatedNestedResultBean  extends _AnnotatedResultBean {
        
        /**
         * @return Returns the results.
         */
        @MocaColumn(name="results")
        public _AnnotatedResultBean[] getBar() {
            return _bar;
        }
        
        /**
         * @param results The results to set.
         */
        @MocaColumn(name="results")
        public void setBar(_AnnotatedResultBean[] bar) {
            _bar = bar;
        }
        
        private _AnnotatedResultBean[] _bar;

    }

    protected MocaResults _createSampleResults() {
        Date now = new Date();
        _AnnotatedResultBean[] data = new _AnnotatedResultBean[3];
        data[0] = new _AnnotatedResultBean("Hello", now, Integer.valueOf(1), Double.valueOf(3.142), Boolean.TRUE,
                new byte[] {7,6,5,4,3,2,1,0,127}); 
        data[1] = new _AnnotatedResultBean("Blah ", new Date (now.getTime()+ 3600*1000),
                Integer.valueOf(900), Double.valueOf(Math.E), Boolean.FALSE, "foo".getBytes(Charset.forName("UTF-8")));
        data[2] = new _AnnotatedResultBean(null, null, null, null, null, null);
        return new BeanResults<_AnnotatedResultBean>(data);
    }
    
    @Override
    protected ModifiableResults _createEmptyNestedResults() {
        return new BeanResults<_AnnotatedNestedResultBean>(_AnnotatedNestedResultBean.class);
    }

    @Override
    protected ModifiableResults _createEmptyResults() {
        return new BeanResults<_AnnotatedResultBean>(_AnnotatedResultBean.class);
    }

    @Override
    protected boolean _checkColumnPosition() {
        return true;
    }

}


