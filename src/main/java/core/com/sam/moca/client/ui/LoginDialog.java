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

package com.sam.moca.client.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.sam.moca.MocaException;
import com.sam.moca.client.ConnectionUtils;
import com.sam.moca.client.MocaConnection;

public class LoginDialog extends JDialog implements ActionListener {
    LoginDialog(Frame owner, MocaConnection conn, String clientKey) {
        super(owner, "Enter Authorization Credentials", true);
        
        setLocationByPlatform(true);
        
        Container top = getContentPane();
        top.setLayout(new BorderLayout());
        
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        
        TextSelectListener selectOnFocus = new TextSelectListener();
        
        JLabel l = new JLabel("User: ", JLabel.TRAILING);
        _username = new JTextField(40);
        _username.addFocusListener(selectOnFocus);
        
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        fieldPanel.add(l, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        l.setLabelFor(_username);
        fieldPanel.add(_username, c);

        l = new JLabel("Password: ", JLabel.TRAILING);
        _password = new JPasswordField(40);
        _password.addFocusListener(selectOnFocus);
        
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        fieldPanel.add(l, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        l.setLabelFor(_password);
        fieldPanel.add(_password, c);

        _okButton = new JButton("Log in");
        _okButton.addActionListener(this);

        _cancelButton = new JButton("Cancel");
        _cancelButton.addActionListener(this);
                
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(_okButton);
        buttonPanel.add(_cancelButton);
        
        top.add(fieldPanel, BorderLayout.CENTER);
        top.add(buttonPanel, BorderLayout.PAGE_END);
        
        getRootPane().setDefaultButton(_okButton);
        pack();
        _conn = conn;
        _clientKey = clientKey;
    }
    
    public boolean connect() {
        setVisible(true);
        return loggedIn;
    }
    
    // @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == _okButton) {
            try {
                String username = _username.getText().trim();
                String password = _password.getText().trim();
                String clientKey = _clientKey;

                if (username.length() != 0) {
                    ConnectionUtils.login(_conn, username, password, clientKey);
                    loggedIn = true;
                    dispose();
                }
                else {
                    JOptionPane.showMessageDialog(this, "You must enter a user ID");
                }
            }
            catch (MocaException ex) {
                JOptionPane.showMessageDialog(this, "Login Failed:\n" + ex);
            }
        }
        else if (e.getSource() == _cancelButton) {
            loggedIn = false;
            dispose();
        }
    }

    private static final long serialVersionUID = -4577375921069077619L;
    private JTextField _username;
    private JTextField _password;
    private String _clientKey;
    
    private JButton _okButton;
    private JButton _cancelButton;
    private MocaConnection _conn = null;
    private boolean loggedIn = false;
}
