package com.redprairie.moca.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

/**
 * Dialog box to present command history and allow the user to select a history element.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class HistoryDialog extends JDialog {
    /**
     * Dialog constructor.
     * @param owner
     * @param commands
     */
    public HistoryDialog(JFrame owner, List<String> commands) {
        super(owner, "Command History", true);
        setLocationByPlatform(true);
        
        _contentPane = new JPanel(new BorderLayout());
        setContentPane(_contentPane);
        
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        
        _commandList = new JList(commands.toArray());
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        JScrollPane commandPane = new JScrollPane(_commandList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        commandPane.setPreferredSize(new Dimension(400, 200));
        fieldPanel.add(commandPane, c);

        _commandList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onOK();
                }
            }
        });
        
        _okButton = new JButton("OK");

        _cancelButton = new JButton("Cancel");
                
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(_okButton);
        buttonPanel.add(_cancelButton);
        
        _contentPane.add(fieldPanel, BorderLayout.CENTER);
        _contentPane.add(buttonPanel, BorderLayout.PAGE_END);
        
        getRootPane().setDefaultButton(_okButton);

        _okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        _cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        _contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
    }

    /**
     * The typical usage is to create the dialog, then call this method.  This method will block until
     * the user dismisses the dialog box.
     * @return the command th user selected from the command history.  If the dialog box was dismissed
     * with a cancel event (button, accelerator), <code>null</code> is returned instead. 
     */
    public String getCommandFromHistory() {
        setVisible(true);
        return _command;
    }
    
    //
    // Implementation
    //
    private static final long serialVersionUID = -790405548874467090L;

    private void onOK() {
        _command = (String)_commandList.getSelectedValue();
        dispose();
    }

    private void onCancel() {
        _command = null;
        dispose();
    }
    
    private JPanel _contentPane;
    private JButton _okButton;
    private JButton _cancelButton;
    private JList _commandList;
    private String _command;

}
