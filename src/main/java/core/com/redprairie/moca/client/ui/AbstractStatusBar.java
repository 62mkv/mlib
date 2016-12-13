/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.redprairie.moca.client.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Simple status bar for display of text information. 
 * 
 * @author $Author$ on $Date: 2006-11-06 14:50:53 -0600 (Mon, 06 Nov
 *         2006) $
 * @version $Revision$
 */
public abstract class AbstractStatusBar extends JPanel {

    /**
     * Creates a new PanelStatus object.
     * 
     * @param iNoLabels DOCUMENT ME!
     */
    public AbstractStatusBar(int maxLabels) {
        _labels = new JLabel[maxLabels];

        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; // 

        c.gridx = 0;
        c.gridy = 0;

        for (int i = 0; i < maxLabels; i++) {
            c.gridx = i;
            c.weighty = 1;
            _labels[i] = new JLabel();
            _labels[i].setBorder(BorderFactory.createLoweredBevelBorder());
            if (i == 0) {
                c.weightx = 0.8;
                c.ipadx = 3;
                _labels[i].setMinimumSize(new Dimension(200, 22));
                _labels[i].setPreferredSize(new Dimension(200, 22));
            }
            else {
                c.weightx = 0.1;
                c.ipadx = 1;
                _labels[i].setMinimumSize(new Dimension(40, 22));
                _labels[i].setPreferredSize(new Dimension(40, 22));
                _labels[i].setMaximumSize(new Dimension(40, 22));
            }
            this.add(_labels[i], c);
        }
    }

    protected void updateStatusBar(String msg) {
        updateStatusBar(msg, 0);
    }

    protected void updateStatusBar(String msg, int index) {
        if (index >= 0 && index < _labels.length) {
            if (msg == null || msg.trim().length() == 0) {
                msg = "";
            }
            _labels[index].setText(msg);
            if (index == 0) _labels[index].setToolTipText(msg);
        }
    }
    
    protected JLabel getLabel(int index) {
        return _labels[index];
    }

    private static final long serialVersionUID = -7912728945294691681L;
    private JLabel[] _labels;
}
