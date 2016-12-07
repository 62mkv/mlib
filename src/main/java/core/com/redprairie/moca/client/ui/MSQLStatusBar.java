/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.client.ui;

import java.awt.Color;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;

/**
 * This is the status panal displayed at the bottom of the window
 * 
 * @author $Author$ on $Date: 2006-11-06 14:50:53 -0600 (Mon, 06 Nov
 *         2006) $
 * @version $Revision$
 */
public class MSQLStatusBar extends AbstractStatusBar {

    /**
     * Creates a new PanelStatus object.
     * 
     * @param iNoLabels DOCUMENT ME!
     */
    public MSQLStatusBar() {
        super(5);
    }

    public void beginWait() {
        updateStatusBar("Executing...", 0);
    }

    public void endWait() {
        updateStatusBar("", 3);
    }
    
    public void setTracingOn() {
        updateStatusBar("TRACING", 4);
    }

    public void setTracingOff() {
        updateStatusBar("", 4);
    }

    public void setOKStatus(MocaResults res) {
        updateStatusBar("", 0);
        int rowCount = (res != null) ? res.getRowCount() : 0;
        updateStatusBar(rowCount + ((rowCount == 1) ? " row" : " rows"), 1);
        updateStatusBar("OK", 2);
        getLabel(2).setOpaque(false);
        getLabel(2).setBackground(null);
    }
    
    public void setErrorStatus(MocaException e) {
        updateStatusBar(e.getMessage(), 0);
        updateStatusBar("0 rows", 1);
        updateStatusBar(String.valueOf(e.getErrorCode()), 2);
        if (e instanceof NotFoundException) {
            getLabel(2).setOpaque(true);
            getLabel(2).setBackground(Color.YELLOW);
        }
        else {
            getLabel(2).setOpaque(true);
            getLabel(2).setBackground(Color.RED);
        }
    }
    
    public void setElapsedTime(long begin, long end){
        updateStatusBar(timeDisplay(begin, end), 3);
    }

    private static final long serialVersionUID = 8593173805835957447L;

    /**
     * Takes two timestamps (Assumed to be milliseconds) and displays the
     * elapsed time between them. 
     * @param begin the "start" timestamp.
     * @param end the "end" timestamp.
     * @return a string representation of the elapsed time.
     */
    private String timeDisplay(long begin, long end) {
        int seconds = ((int)((end - begin)))/1000;
        int ms = (int)((end - begin)%1000);
        
        return String.format("%d.%03d sec", seconds, ms);
    }
}
