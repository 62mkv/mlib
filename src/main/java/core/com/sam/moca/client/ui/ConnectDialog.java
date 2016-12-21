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
import java.util.Collection;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.sam.moca.MocaException;
import com.sam.moca.client.ConnectionUtils;
import com.sam.moca.client.DirectConnection;
import com.sam.moca.client.HttpConnection;
import com.sam.moca.client.MocaConnection;

public class ConnectDialog extends JDialog implements ActionListener {
    ConnectDialog(Frame owner, Collection<String> defaultConnectionStrings) {
        super(owner, "Connect to Server", true);
        
        setLocationByPlatform(true);
        
        Container top = getContentPane();
        top.setLayout(new BorderLayout());
        
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        
        TextSelectListener selectOnFocus = new TextSelectListener();
        
        JLabel l = new JLabel("Connection: ", JLabel.TRAILING);
        _host = new JComboBox(new Vector<String>(defaultConnectionStrings));
        _host.addFocusListener(selectOnFocus);
        _host.setEditable(true);

        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        fieldPanel.add(l, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        l.setLabelFor(_host);
        fieldPanel.add(_host, c);
        
        l = new JLabel("User: ", JLabel.TRAILING);
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

        l = new JLabel("Charset: ", JLabel.TRAILING);
        _charset = new JComboBox(new String[] {"Use Default", "UTF-8", "ISO-8859-1"});
        
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        fieldPanel.add(l, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        l.setLabelFor(_charset);
        fieldPanel.add(_charset, c);
        
        l = new JLabel("Client Key: ", JLabel.TRAILING);
        _clientKey = new JTextField(40);
        _clientKey.addFocusListener(selectOnFocus);
        
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        fieldPanel.add(l, c);

        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        l.setLabelFor(_clientKey);
        fieldPanel.add(_clientKey, c);
        
        _clientKey.setText(MocaClientUI.DEFAULT_CLIENT_KEY);

        _okButton = new JButton("Connect");
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
    }
    
    public MocaConnection connect() {
        setVisible(true);
        return _conn;
    }
    
    // @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == _okButton) {
            try {
                String host = String.valueOf(_host.getSelectedItem()).trim();
                String username = _username.getText().trim();
                String password = _password.getText().trim();
                String clientKey = _clientKey.getText().trim();
                String charset = (String)_charset.getSelectedItem();
                if (charset.equals("Use Default")) {
                    charset = null;
                }
                
                MocaConnection tmp;
                
                String origHost = host;
                if (host.startsWith("http://") || host.startsWith("https://")) {
                    tmp = new HttpConnection(host, null);
                }
                else {
                    String[] fields = host.split(":");
                    int port = 4500;
                    if (fields.length == 2) {
                        host = fields[0];
                        port = Integer.parseInt(fields[1]);
                    }
                    tmp = new DirectConnection(host, port, null, charset);
                }

                MocaClientUI.trackHost(origHost);
                
                if (username.length() != 0) {
                    ConnectionUtils.login(tmp, username, password, clientKey);
                }
                _conn = tmp;
                dispose();
            }
            catch (NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid Port Number");
            }
            catch (MocaException ex) {
                JOptionPane.showMessageDialog(this, "Connection Failed:\n" + ex);
            }
        }
        else if (e.getSource() == _cancelButton) {
            dispose();
        }
    }
    
    /**
     * @return Returns the clientKey.
     */
    public String getClientKey() {
        return _clientKey.getText().trim();
    }
    
    private static final long serialVersionUID = -4577375921069077619L;
    private JComboBox _host;
    private JTextField _username;
    private JTextField _password;
    private JTextField _clientKey;
    private JComboBox _charset;
    
    private JButton _okButton;
    private JButton _cancelButton;
    private MocaConnection _conn = null;
}
