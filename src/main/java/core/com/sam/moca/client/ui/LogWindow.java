package com.sam.moca.client.ui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Dialog box to present logging and debug information.
 *  
 * @author dinksett
 * @version $Revision$
 */
public class LogWindow extends JFrame {
    public LogWindow(String title, int width, int height) {
        super(title);
        setSize(width, height);
        _text = new JTextArea();
        JScrollPane scroll = new JScrollPane(_text);
        getContentPane().add(scroll);
        setVisible(true);
    }

    /**
     * Appends the data to the log window.
     * @param data
     */
    public void append(String data) {
        _text.append(data);
        this.getContentPane().validate();
    }

    private JTextArea _text = null;
    private static final long serialVersionUID = 1L;
}