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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.exceptions.AuthenticationException;

/**
 * The base Swing panel that displays a text edit area, allowing the user to
 * enter a command, execute that command, and display the results in a table.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ConnectionPanel extends JPanel {
    
    /**
     * Create a new connection panel, using the given connection object.
     * @param conn a <code>MocaConnection</code> that is already connected
     * and configured.
     */
    public ConnectionPanel(MocaClientUI main, MocaConnection conn, JButton autoCommitButton, String clientKey) {
        super(new BorderLayout());
        _main = main;
        _conn = conn;
        _clientKey = clientKey;
        _autoCommitButton = autoCommitButton;

        _commandText = new JTextArea();
        _commandText.setEditable(true);
        JScrollPane commandScroll = new JScrollPane(_commandText,
                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        _resultsModel = new MocaResultsModel();
        _resultsTable = new JTable(_resultsModel);
        _resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _resultsTable.setRowSelectionAllowed(true);
        _resultsTable.setColumnSelectionAllowed(false);
        _resultsTable.setDefaultRenderer(String.class, new StringValueRenderer());
        _resultsTable.setDefaultRenderer(Date.class, new DateValueRenderer());

        JScrollPane resultsPane = new JScrollPane(_resultsTable,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, commandScroll, resultsPane);
        pane.setDividerLocation(200);
        add(pane, BorderLayout.CENTER);
        
        _statusBar = new MSQLStatusBar();
        _statusBar.updateStatusBar("READY");

        add(_statusBar, BorderLayout.PAGE_END);

        _commandText.getInputMap().put(KeyStroke.getKeyStroke(
            KeyEvent.VK_F5, 0),
            "runcommand");
        _commandText.getActionMap().put("runcommand", new AbstractAction() {
            private static final long serialVersionUID = 1223701570924794345L;

            public void actionPerformed(ActionEvent e) {
                execute();
            }
        });
    }
    
    /**
     * Executes the contents of the command text area and displays the results
     * in the results table.  This method is typically invoked by an execute
     * (run) button or key action.
     */
    public void execute() {
        // If we're already executing a command, don't try again.
        if (_inExecute) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        final long begin = System.currentTimeMillis();
        final String command = _commandText.getText();

        _statusBar.beginWait();
        
        new Thread() {
            // @see java.lang.Thread#run()
            @Override
            public void run() {
                if (_results != null) _results.close();
                try {
                    _results = (SimpleResults) _conn.executeCommand(command);
                    _exception = null;
                }
                catch (MocaException e) {
                    _results = (SimpleResults) e.getResults();
                    _exception = e;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (_exception == null) {
                            _statusBar.setOKStatus(_results);
                            _main.log("Session Key = " + _conn.getEnvironment().get("SESSION_KEY"));
                        }
                        else if (_exception.getErrorCode() == AuthenticationException.CODE) {
                            boolean loggedIn = new LoginDialog(null, _conn, _clientKey).connect();
                            syncAutoCommit();
                            if (!loggedIn) {
                                _statusBar.setErrorStatus(_exception);
                            }
                            else {
                                execute();
                            }
                        }
                        else {
                            _statusBar.setErrorStatus(_exception);
                        }
                        _resultsModel.newData();
                        _statusBar.setElapsedTime(begin, System.currentTimeMillis());
                    }
                });
            }
         }.start();

        if (_history.isEmpty() || !_history.getFirst().equals(command)) {
            _history.addFirst(command);
        }
        
        if (_history.size() > MAX_HISTORY) {
            _history.removeLast();
        }
    }
    
    public void showHistory(JFrame parentFrame) {
        String newCommand = new HistoryDialog(parentFrame, _history).getCommandFromHistory();
        if (newCommand != null) {
            _commandText.setText(newCommand);
        }
    }
    
    public void showTrace(JFrame parentFrame) {
        if (_traceFile == null) {
            String traceFile = (String)JOptionPane.showInputDialog(parentFrame, "Trace to file", "Enable Tracing", JOptionPane.PLAIN_MESSAGE);
            if (traceFile != null) {
                try {
                    _conn.executeCommand("set trace where filename = '"+ traceFile + "' and activate = 1 and level='*'");
                    _traceFile = traceFile;
                    _statusBar.setTracingOn();
                }
                catch (MocaException e) {
                    _traceFile = null;
                    _statusBar.setTracingOff();
                }
            }
        }
        else {
            try {
                _conn.executeCommand("set trace where activate = 0");
            }
            catch (MocaException e) {
                // Ignore
            }
            _traceFile = null;
            _statusBar.setTracingOff();
        }
    }
    
    public void toggleAutoCommit() {
        _conn.setAutoCommit(!_conn.isAutoCommit());
        syncAutoCommit();
    }
    
    public void syncAutoCommit() {
        boolean state = _conn.isAutoCommit();
        if (state) {
            _autoCommitButton.setToolTipText(MocaClientUI.AUTO_COMMIT_ENABLED_MESSAGE);
            _autoCommitButton.setIcon(MocaClientUI.AUTO_COMMIT_ENABLED_ICON);
        }
        else {
            _autoCommitButton.setToolTipText(MocaClientUI.AUTO_COMMIT_DISABLED_MESSAGE);
            _autoCommitButton.setIcon(MocaClientUI.AUTO_COMMIT_DISABLED_ICON);
        }
    }
    
    /**
     * Close this panel, as well as the associated MOCA connection.
     */
    public void close() {
        _conn.close();
        _conn = null;
        if (_results != null) {
            _results.close();
            _results = null;
        }
    }

    /**
     * An inner class used to represent the MocaResults object in a JTable.
     * This class allows us to have a table representation of the MOCA results
     * and not have to copy the data into a more "native" result set for display
     * purposes. 
     */
    private class MocaResultsModel extends AbstractTableModel {
        private static final long serialVersionUID = 7632758350662343488L;


        public Object getValueAt(int rowIndex, int columnIndex) {
            if (_results != null) {
                _results.setRow(rowIndex);
                return _results.getValue(columnIndex);
            }
            else {
                return null;
            }
        }
        
        public int getColumnCount() {
            return _results == null ? 0 : _results.getColumnCount();
        }
        
        public int getRowCount() {
            return _results == null ? 0 : _results.getRowCount();
        }
        
        @Override
        public String getColumnName(int column) {
            return _results == null ? null : _results.getColumnName(column);
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return _results == null ? null : _results.getColumnType(columnIndex).getValueClass();
        }
        
        
        /**
         * Used to indicate to the table that the data has been changed.
         */
        public void newData() {
            fireTableStructureChanged();
            initColumnWidths();
        }
    }
    
    private static class StringValueRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 2715008312503222377L;

        // @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);
            if (value != null && ((String)value).contains("\n")) {
                setToolTipText("<html><pre>" + value);
            }
            else {
                setToolTipText((String)value);
            }
            return result;
        }
    }
    
    static class DateValueRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -740217897900894939L;
        public DateValueRenderer() { super(); }

        public void setValue(Object value) {
            if (_formatter==null) {
                _formatter = DateFormat.getDateTimeInstance();
            }
            setText((value == null) ? "" : _formatter.format(value));
        }
        private DateFormat _formatter;
    }


    
    /**
     * Initialize the column widths according to the stored data lengths
     * in the MocaResults object.
     */
    private void initColumnWidths() {
        TableColumnModel columnModel = _resultsTable.getColumnModel();
        int columns = _results == null ? 0 : _results.getColumnCount();
        for (int i = 0; i < columns; i++) {
            TableColumn col = columnModel.getColumn(i);

            int maxLength = _results.getMaxLength(i);
            if (maxLength == 0) {
                
                if (_results.getColumnType(i) == MocaType.STRING) {
                    maxLength = 50;
                }
                else if (_results.getColumnType(i) == MocaType.INTEGER) {
                    maxLength = 10;
                }
                else {
                    maxLength = 15;
                }
            }
            else if (maxLength < 10 ) {
                maxLength = 10;
            }
            else if (maxLength > 50) {
                maxLength = 50;
            }
            
            col.setPreferredWidth(maxLength * 5);
        }
    }
    
    // @see javax.swing.JComponent#requestFocusInWindow()
    @Override
    public boolean requestFocusInWindow() {
        return _commandText.requestFocusInWindow();
    }

    //
    // Implementation
    //
    
    private static final int MAX_HISTORY = 100;
    private static final long serialVersionUID = 8663247041849799594L;
    
    private JTextArea _commandText;
    private MocaClientUI _main;
    private SimpleResults _results;
    private MocaException _exception;
    private JTable _resultsTable;
    private JButton _autoCommitButton;
    private MocaResultsModel _resultsModel;
    private MSQLStatusBar _statusBar;
    private MocaConnection _conn;
    private String _clientKey;
    private LinkedList<String> _history = new LinkedList<String>();
    private boolean _inExecute = false;
    private String _traceFile = null;
}
