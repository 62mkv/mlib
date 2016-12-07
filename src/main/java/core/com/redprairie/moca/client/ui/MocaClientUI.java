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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * Simple Java-based GUI client. 
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaClientUI extends JPanel implements ActionListener {
    
    public static final String AUTO_COMMIT_ENABLED_MESSAGE = "Auto-commit enabled";
    public static final String AUTO_COMMIT_DISABLED_MESSAGE = "Auto-commit disabled";
    public static final Icon AUTO_COMMIT_ENABLED_ICON;
    public static final Icon AUTO_COMMIT_DISABLED_ICON;
    
    public static void main(String[] args) {
        Options opts = null;
        try {
            opts = Options.parse("d", args);
            args = opts.getRemainingArgs();
        }
        catch (OptionsException e) {
            // Ignore exceptions
        }
        
        final boolean debug;
        if (opts != null && opts.isSet('d')) {
            debug = true;
        }
        else {
            debug = false;
        }
        
        final String connection;
        
        if (args.length > 0) {
            connection = args[0];
        }
        else {
            connection = null;
        }
        
        _prefs = Preferences.userNodeForPackage(MocaClientUI.class);
        String prefsHost = _prefs.get("host", null);
        if (prefsHost != null) {
            String[] hosts = prefsHost.split(",");
            _knownHosts.addAll(Arrays.asList(hosts));
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startGUI(connection, debug); 
            }
        });
    }
    
    public void newConnection() {
        ConnectDialog loginDialog = new ConnectDialog(_parentFrame, _knownHosts); 
        MocaConnection moca = loginDialog.connect();
        if (moca != null) {
            addApplicationId(moca);
            ConnectionPanel newPanel = new ConnectionPanel(this, moca, _autoCommitButton, loginDialog.getClientKey());
            _connections.add(newPanel);
            _connectionTabs.addTab(moca.toString(), newPanel);
            _connectionTabs.setSelectedComponent(newPanel);
            newPanel.requestFocusInWindow();
        }
    }

    private void addApplicationId(MocaConnection conn) {
        Map<String, String> env = conn.getEnvironment();
        if (env != null) {
            env.put("MOCA_APPL_ID", "msql");
            conn.setEnvironment(env);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals(NEW_COMMAND)) {
            newConnection();
        }
        else if (cmd.equals(RUN_COMMAND)) {
            int tab = _connectionTabs.getSelectedIndex();
            if (tab >= 0) {
                ConnectionPanel connection = _connections.get(tab);
                connection.execute();
            }
        }
        else if (cmd.equals(HISTORY_COMMAND)) {
            int tab = _connectionTabs.getSelectedIndex();
            if (tab >= 0) {
                ConnectionPanel connection = _connections.get(tab);
                connection.showHistory(_parentFrame);
            }
        }
        else if (cmd.equals(CLOSE_COMMAND)) {
            if (_connections.size() > 0) {
                int tab = _connectionTabs.getSelectedIndex();
                if (tab >= 0) {
                    ConnectionPanel panel = _connections.remove(tab);
                    panel.close();
                    _connectionTabs.removeTabAt(tab);
                }
            }
        }
        else if (cmd.equals(AUTOCOMMIT_COMMAND)) {
            int tab = _connectionTabs.getSelectedIndex();
            if (tab >= 0) {
                ConnectionPanel connection = _connections.get(tab);
                connection.toggleAutoCommit();
            }
        }
        else if (cmd.equals(TRACE_COMMAND)) {
            int tab = _connectionTabs.getSelectedIndex();
            if (tab >= 0) {
                ConnectionPanel connection = _connections.get(tab);
                connection.showTrace(_parentFrame);
            }
        }
    }
    
    public void log(String message) {
        if (_debug) {
            _log.append(message + "\r\n");
        }
    }
    
    //
    // Implementation
    //

    private static final long serialVersionUID = 6403974388769174692L;
    private static final String NEW_COMMAND="new";
    private static final String RUN_COMMAND="run";
    private static final String AUTOCOMMIT_COMMAND="autocommit";
    private static final String CLOSE_COMMAND="close";
    private static final String HISTORY_COMMAND="history";
    private static final String TRACE_COMMAND="trace";
    
    private MocaClientUI(JFrame parent, String defaultConnection, boolean debug) {
        super(new BorderLayout());
        
        if (defaultConnection != null) {
            _knownHosts.add(defaultConnection);
        }
        
        _parentFrame = parent;
        _connectionTabs = new JTabbedPane();
        _connectionTabs.setPreferredSize(new Dimension(800, 600));
        _connectionTabs.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                int i = _connectionTabs.getSelectedIndex();
                if (i >= 0) {
                    ConnectionPanel connection = _connections.get(i);
                    _runButton.setEnabled(true);
                    _historyButton.setEnabled(true);
                    _tracingButton.setEnabled(true);
                    _autoCommitButton.setEnabled(true);
                    _closeButton.setEnabled(true);
                    connection.syncAutoCommit();
                }
                else {
                    _runButton.setEnabled(false);
                    _historyButton.setEnabled(false);
                    _tracingButton.setEnabled(false);
                    _autoCommitButton.setEnabled(false);
                    _closeButton.setEnabled(false);
                }
            }});
        add(_connectionTabs, BorderLayout.CENTER);
        JToolBar buttonBar = new JToolBar();
        buttonBar.setFloatable(false);
        
        JButton newButton = makeNewToolButton("NewPlug", NEW_COMMAND, "New Connection", "New");
        _closeButton = makeNewToolButton("UnPlug", CLOSE_COMMAND, "Close Connection", "Close");
        _runButton = makeNewToolButton("RunComp", RUN_COMMAND, "Run Command", "Run");
        _historyButton = makeNewToolButton("History", HISTORY_COMMAND, "Show History", "History");
        _tracingButton = makeNewToolButton("Tracing", TRACE_COMMAND, "Set Trace", "Trace");


//      _autoCommitButton = makeNewToolButton("RedFlag", AUTOCOMMIT_COMMAND, AUTO_COMMIT_ENABLED_MESSAGE, "AutoCommit");
        _autoCommitButton = new JButton();
        _autoCommitButton.setActionCommand(AUTOCOMMIT_COMMAND);
        _autoCommitButton.setToolTipText(AUTO_COMMIT_ENABLED_MESSAGE);
        _autoCommitButton.addActionListener(this);

        _autoCommitButton.setIcon(AUTO_COMMIT_ENABLED_ICON);


        _closeButton.setEnabled(false);
        _runButton.setEnabled(false);
        _historyButton.setEnabled(false);
        _autoCommitButton.setEnabled(false);
        _tracingButton.setEnabled(false);
        
        buttonBar.add(newButton);
        buttonBar.add(_closeButton);
        buttonBar.addSeparator();
        buttonBar.add(_autoCommitButton);
        buttonBar.addSeparator();
        buttonBar.add(_runButton);
        buttonBar.addSeparator();
        buttonBar.add(_historyButton);
        buttonBar.addSeparator();
        buttonBar.add(_tracingButton);
        
        add(buttonBar, BorderLayout.PAGE_START);
        
        _debug = debug;
        if (debug) {
            _log = new LogWindow("Console", 800, 200);
            _log.setVisible(true);
        }
        else {
            _log = null;
        }
    }
    
    private static void startGUI(String defaultConnection, boolean debug) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException e) {
            System.out.println("Error while applying look and feel");
            e.printStackTrace(System.out);
        }
        catch (InstantiationException e) {
            System.out.println("Error while applying look and feel");
            e.printStackTrace(System.out);
        }
        catch (IllegalAccessException e) {
            System.out.println("Error while applying look and feel");
            e.printStackTrace(System.out);
        }
        catch (UnsupportedLookAndFeelException e) {
            System.out.println("Error while applying look and feel");
            e.printStackTrace(System.out);
        }

        //Create and set up the window.
        JFrame frame = new JFrame("MSQL");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new ImageIcon(MocaClientUI.class.getResource("resources/msql_icon.png")).getImage());

        //Create and set up the content pane.
        MocaClientUI newContentPane = new MocaClientUI(frame, defaultConnection, debug);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        newContentPane.newConnection();
    }
    
    private JButton makeNewToolButton(String imageName, String cmd, String tooltipText, String altText) {
        // Look for the image.
        String imgLocation = "resources/" + imageName + ".gif";
        URL imageURL = MocaClientUI.class.getResource(imgLocation);

        //Create and initialize the button.
        JButton button = new JButton();
        button.setActionCommand(cmd);
        button.setToolTipText(tooltipText);
        button.addActionListener(this);

        if (imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, altText));
        }
        else {
            button.setText(altText);
        }

        return button;
    }
    
    public static void trackHost(String host) {
        if (!_knownHosts.contains(host)) {
            _knownHosts.add(host);
            StringBuilder buf = new StringBuilder();
            boolean tmp = false;
            for (String h : _knownHosts) {
                if (tmp) {
                    buf.append(',');
                }
                else {
                    tmp = true;
                }
                buf.append(h);
            }
            _prefs.put("host", buf.toString());
            try {
                _prefs.flush();
            }
            catch (BackingStoreException e) {
                // Ignore
            }
        }
    }
    
    private JTabbedPane _connectionTabs;
    private List<ConnectionPanel> _connections = new ArrayList<ConnectionPanel>();
    private final JFrame _parentFrame;
    private JButton _autoCommitButton;
    private JButton _runButton;
    private JButton _historyButton;
    private JButton _tracingButton;
    private JButton _closeButton;
    private static Set<String> _knownHosts = new LinkedHashSet<String>();
    private static Preferences _prefs;
    private final LogWindow _log;
    private boolean _debug;
    
    // Package-private
    final static String DEFAULT_CLIENT_KEY = "msql";
    
    static {
        String imgLocation = "resources/RedFlag.gif";
        URL imageURL = MocaClientUI.class.getResource(imgLocation);
        AUTO_COMMIT_ENABLED_ICON = new ImageIcon(imageURL, "AutoCommit");
        imgLocation = "resources/RedFlagDisabled.gif";
        imageURL = MocaClientUI.class.getResource(imgLocation);
        AUTO_COMMIT_DISABLED_ICON = new ImageIcon(imageURL, "AutoCommit");
    }
}
