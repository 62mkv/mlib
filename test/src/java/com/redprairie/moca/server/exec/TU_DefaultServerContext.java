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

package com.redprairie.moca.server.exec;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.exceptions.RemoteSessionClosedException;
import com.redprairie.moca.exceptions.SessionClosedException;
import com.redprairie.moca.exceptions.UniqueConstraintException;
import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.MocaTransaction;
import com.redprairie.moca.server.db.exceptions.MissingWhereClauseException;
import com.redprairie.moca.server.dispatch.MessageResolver;
import com.redprairie.moca.server.legacy.NativeAdapterFactory;
import com.redprairie.moca.server.profile.CommandUsage;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.JavaCommand;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;
import com.redprairie.moca.server.repository.MocaCommandRepository;
import com.redprairie.moca.server.session.SessionToken;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ProxyStub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class tests some things of default server context.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_DefaultServerContext {
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_DefaultServerContext.class.getName(), 
                true);
    }
    
    @Before
    public void beforeTest() {
        _moca = MocaUtils.currentContext();
        
        _scriptAdapter = Mockito.mock(ScriptAdapter.class);
        _dbAdapter = Mockito.mock(DBAdapter.class);
        _dbTransaction = Mockito.mock(MocaTransaction.class);
        Mockito.when(_dbAdapter.newTransaction()).thenReturn(_dbTransaction);
        _session = Mockito.mock(SessionContext.class);
        // Make sure sessions are valid by default
        SessionToken sessionToken = Mockito.mock(SessionToken.class);
        Mockito.when(sessionToken.getSecurityLevel()).thenReturn(SecurityLevel.ALL);
        Mockito.when(_session.getSessionToken()).thenReturn(sessionToken);
        _system = Mockito.mock(SystemContext.class);
        _request = Mockito.mock(RequestContext.class);
        _commands = Mockito.mock(CommandRepository.class);
        _native = Mockito.mock(NativeAdapterFactory.class);
        _usage = Mockito.mock(CommandUsage.class);
        _blacklist = Collections.emptyList();
        _message = Mockito.mock(MessageResolver.class);
        _remoteFactory = Mockito.mock(RemoteConnectionFactory.class);
    }
    
    @After
    public void afterTest() throws MocaException {
        _moca.rollback();
    }

    @Test
    public void testNullSystemAndRequestContextGetSystemVariable() {
        DefaultServerContext context = new DefaultServerContext(null, null, 
                null, ServerUtils.globalContext(), null, null, null, null, 
                null, null, null);
        
        String value = context.getSystemVariable("MOCADIR");
        
        assertNotNull("The MOCADIR value should be present", value);
    }
    
    @Test
    public void testLastStatementInitiated() throws MocaException {
        ComponentLevel compLevel = new ComponentLevel("MOCAcommand");
        
        LocalSyntaxCommand getCommand = new LocalSyntaxCommand("get command", 
                compLevel);
        
        getCommand.setSecurityLevel(SecurityLevel.OPEN);
        
        getCommand.setSyntax("publish data where command = command()");
        
        // We need to setup the publish data command
        JavaCommand publishData = new JavaCommand("publish data", 
                compLevel);
        
        publishData.setSecurityLevel(SecurityLevel.OPEN);
        publishData.setClassName("com.redprairie.moca.components.base.CoreService");
        publishData.setMethod("publishData");
        
        // Now we create our repository and throw the command and component
        // level into it
        MocaCommandRepository repos = new MocaCommandRepository();
        repos.addLevel(compLevel);
        repos.addCommand(getCommand);
        repos.addCommand(publishData);
        
        Map<String, String> sessionContext = new HashMap<String, String>();
        
        DefaultServerContext context = new DefaultServerContext(null, null, 
                new LocalSessionContext("MOCAcommand", SessionType.TASK, 
                    sessionContext), 
                ProxyStub.newProxy(SystemContext.class, new Object()), 
                null, repos, null, null, null, null, null);

        String commandToInitiate = "noop where worked = 'true' ; get command";
        Map<String, Object> emptyMap = Collections.emptyMap();
        
        MocaResults res = context.executeCommand(commandToInitiate, emptyMap, 
                false);
        
        RowIterator rowIter = res.getRows();
        
        Assert.assertTrue(rowIter.next());
        
        Assert.assertEquals(commandToInitiate, rowIter.getString("command"));
        
        Assert.assertFalse(rowIter.next());
    }
    
    @Test
    public void testDeleteWithoutWhereClause() throws MocaException {
        try {
            _moca.executeCommand("[delete from comp_ver]");
            fail("We should have thrown a MissingWhereClauseException");
        }
        catch (MissingWhereClauseException e) {
            // We should go in here
        }
    }
    
    @Test
    public void testDeleteWithoutWhereClauseMixedCase() throws MocaException {
        try {
            _moca.executeCommand("[DelEte from comp_ver]");
            fail("We should have thrown a MissingWhereClauseException");
        }
        catch (MissingWhereClauseException e) {
            // We should go in here
        }
    }
    
    @Test
    public void testDeleteWithWhereClauseMixedCase() throws MocaException {
        try {
            _moca.executeCommand("[DelEte from comp_ver WhERe 1 = 2]");
            fail("We should have gotten a NotFoundException since our where " +
                        "clause is always not true.");
        }
        catch (NotFoundException e) {
            // This was that there was no rows, which we just ignore as well.
        }
    }
    
    @Ignore
    // This is ignore since for now we just look for where only.
    @Test
    public void testUpdateWithWhereInValue() throws MocaException {
        try {
            _moca.executeCommand("[update comp_ver set comp_file_nam = 'where']");
            fail("We should have gotten a MissingWhereClauseException.");
        }
        catch (MissingWhereClauseException e) {
            // We didn't have a where clause only a name of a field being where
        }
    }
    
    @Ignore
    // This is ignore since for now we just look for where only.
    @Test
    public void testUpdateWithWhereInName() throws MocaException {
        try {
            _moca.executeCommand("[update comp_ver set where_val = 'error']");
            fail("We should have gotten a MissingWhereClauseException.");
        }
        catch (MissingWhereClauseException e) {
            // We didn't have a where clause only a name of a field being where
        }
    }
    
    @Test
    public void testUpdateWithNoWhiteSpaceBeforeWhere() throws MocaException {
        try {
            _moca.executeCommand("[update comp_ver set base_prog_id = 'error'WHerE 1=2]");
            fail("We should have gotten a NotFoundException.");
        }
        catch (NotFoundException e) {
            // This was that there was no rows, which we just ignore as well.
        }
    }
    
    @Test
    public void testUniqueConstraintExceptionThrown() throws MocaException {
        try {
            _moca.executeCommand("[ insert into task_definition (task_id, name, task_typ, cmd_line, restart, auto_start) values ('DUP', 'DUP', 'P', 'DUP', 0, 0)]");
            _moca.executeCommand("[ insert into task_definition (task_id, name, task_typ, cmd_line, restart, auto_start) values ('DUP', 'DUP', 'P', 'DUP', 0, 0)]");
            fail("We should have thrown a UniqueConstraintException");
        }
        catch (UniqueConstraintException e) {
            // We should throw this.
        }
    }
    
    @Test
    public void testLocalRemoteRedirect() throws MocaException {
        String url = ServerUtils.globalContext().getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_URL);
        String value = "1";

        StringBuilder sb = new StringBuilder();
        sb.append("publish data where test = '");
        sb.append(value);
        sb.append("'| remote('");
        sb.append(url);
        sb.append("') publish data where test = @test");
        
        MocaResults res = _moca.executeCommand(sb.toString());
        
        Assert.assertTrue(res.next());
        Assert.assertEquals(value, res.getString("test"));
        Assert.assertFalse(res.next());
    }
    
    @Test
    public void testLocalRemoteRedirectWithSavepoint() throws MocaException {
        String url = ServerUtils.globalContext().getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_URL);

        // We start a transaction in our thread then do a remote that
        // starts a tx and sets a savepoint
        StringBuilder sb = new StringBuilder();
        sb.append("[select count(1) from dual] | ");
        sb.append("remote('");
        sb.append(url);
        sb.append("') { [select count(1) from dual] | set savepoint where savepoint = 'test' }");
        
        String command = sb.toString();
        
        System.out.println(command);
        
        _moca.executeCommand(command);
        
        _moca.rollback();
    }
    
    @Test
    public void testLocalRemoteRedirectWithAtStar() throws MocaException {
        String url = ServerUtils.globalContext().getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_URL);
        String value = "1";
        String value2 = "foo";
        String foo = "bar";

        MocaResults res = _moca.executeCommand(
                "test remote at star " +
                "  where foo = '" + foo + "'" +
                "    and url = '" + url + "'" +
                "    and test = '" + value + "'" +
                "    and test2 = '" + value2 + "'");
        
        Assert.assertTrue(res.next());
        Assert.assertEquals(foo, res.getString("foo"));
        Assert.assertEquals(value, res.getString("test"));
        Assert.assertEquals(value2, res.getString("test2"));
        Assert.assertFalse(res.next());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoteSessionTimeoutError() throws MocaException, SQLException {
        DefaultServerContext context = new DefaultServerContext(_scriptAdapter, 
            _dbAdapter, _session, _system, _request, _commands,
            _native, _usage, _blacklist, _message, _remoteFactory);
        
        // Set the transaction to be active to force rollback call later
        Mockito.when(_dbTransaction.isOpen()).thenReturn(true);
        
        MocaConnection errorAfterSQL = Mockito.mock(MocaConnection.class);
        
        MocaResults mockresults = Mockito.mock(MocaResults.class);
        
        Mockito.when(errorAfterSQL.executeCommandWithContext(
            Mockito.contains("select"), (MocaArgument[])Mockito.any(), 
            (MocaArgument[])Mockito.any()))
            .thenReturn(mockresults).thenThrow(new SessionClosedException());
        
        Mockito.when(_remoteFactory.getConnection(Mockito.anyString(), 
            Mockito.anyMap())).thenReturn(errorAfterSQL);
        
        MocaResults answer = context.executeCommand(
            "remote('foo')[select 1 from dual]", null, false);
        
        assertEquals(mockresults, answer);
        
        try {
            context.executeCommand("remote('foo')[select 1 from dual]", null, false);
            fail("Excepted a RemoteSessionClosedException!");
        }
        catch (RemoteSessionClosedException e) {
            // We should have received a remote session closed exception
        }
        
        // Our local db should not have been rolled back
        Mockito.verify(_dbTransaction, Mockito.never()).rollback();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoteSessionSuccess() throws MocaException, SQLException {
        DefaultServerContext context = new DefaultServerContext(_scriptAdapter, 
            _dbAdapter, _session, _system, _request, _commands,
            _native, _usage, _blacklist, _message, _remoteFactory);
        
        // Set the transaction to be active to force commit call later
        Mockito.when(_dbTransaction.isOpen()).thenReturn(true);
        
        MocaConnection mocaConn = Mockito.mock(MocaConnection.class);
        
        Mockito.when(_remoteFactory.getConnection(Mockito.anyString(), 
            Mockito.anyMap())).thenReturn(mocaConn);
        
        context.executeCommand("remote('foo')[select 1 from dual]", null, false);
        
        context.executeCommand("remote('foo')[select 1 from dual]", null, false);
        
        context.commit();
        
        // Our local db should have been committed
        Mockito.verify(_dbTransaction).commit();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testParallelReceiveMocaException() throws MocaException {
        DefaultServerContext context = new DefaultServerContext(_scriptAdapter, 
            _dbAdapter, _session, _system, _request, _commands,
            _native, _usage, _blacklist, _message, _remoteFactory);
        
        EditableResults edRes = new SimpleResults();
        edRes.addColumn("foo", MocaType.STRING);
        edRes.addRow();
        edRes.setStringValue(0, "bar");
        
        String host1 = "foo1";
        String host2 = "foo2";
        
        MocaConnection mocaConn1 = Mockito.mock(MocaConnection.class);
        Mockito.when(mocaConn1.executeCommandWithContext(Mockito.anyString(),
                    (MocaArgument[]) Mockito.any(),
                    (MocaArgument[]) Mockito.any()))
                .thenReturn(edRes);
        
        Mockito.when(_remoteFactory.getConnection(Mockito.eq(host1), 
            Mockito.anyMap())).thenReturn(mocaConn1);
        
        MocaConnection mocaConn2 = Mockito.mock(MocaConnection.class);
        Mockito.when(mocaConn2.executeCommandWithContext(Mockito.anyString(),
                    (MocaArgument[]) Mockito.any(),
                    (MocaArgument[]) Mockito.any()))
                .thenThrow(new NullPointerException());
        
        Mockito.when(_remoteFactory.getConnection(Mockito.eq(host2), 
            Mockito.anyMap())).thenReturn(mocaConn2);
        
        MocaResults results = context.executeCommand(
            "parallel('" + host1 + "," + host2 + "')publish data where foo = 'bar'", null, false);
        
        assertTrue(results.next());
        assertEquals(host1, results.getString("system"));
        assertEquals(0, results.getInt("status"));
        
        MocaResults childRes = results.getResults("resultset");
        assertTrue(childRes.next());
        assertEquals("bar", childRes.getString("foo"));
        
        assertTrue(results.next());
        assertEquals(host2, results.getString("system"));
        assertEquals(502, results.getInt("status"));
        
        childRes = results.getResults("resultset");
        assertFalse(childRes.next());
        assertEquals(0, childRes.getColumnCount());
    }
    
    public void testRequestVariableOverride() {
        DefaultServerContext context = new DefaultServerContext(_scriptAdapter, 
            _dbAdapter, _session, _system, _request, _commands,
            _native, _usage, _blacklist, _message, _remoteFactory);
        
        String name = "nameofarg";
        String value = "testvalue";
        context.putSystemVariable(name, value);
        
        Mockito.verify(_request).putVariable(name, value);
        Mockito.verify(_session).putVariable(name, value);
    }
    
    @Test
    public void testCatchAllSimple() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaArgument bar = new MocaArgument("bar", "bar");
        MocaResults rs = ctx.executeCommand(
            "try { set return status where code=502 "
                    + "} catch (@?) { publish data where foo=@bar}", bar);
        rs.next();
        assertEquals(bar.getValue(), rs.getString("foo"));
    }

    @Test
    public void testDontCatchNegativeStatus() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaArgument bar = new MocaArgument("bar", "bar");
        MocaResults rs = ctx.executeCommand(
            "try { set return status where code=-1 "
                    + "} catch (@?) { publish data where foo=@bar}", bar);
        rs.next();
        assertEquals(bar.getValue(), rs.getString("foo"));
    }

    @Test
    public void testDontCatch() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaArgument bar = new MocaArgument("bar", "bar");
        MocaResults rs = ctx.executeCommand(
            "try { set return status where code=0 "
                    + "} catch (@?) { publish data where foo=@bar}", bar);
        rs.next();
        assertFalse(rs.containsColumn("foo"));
    }

    private ScriptAdapter _scriptAdapter;
    private DBAdapter _dbAdapter;
    private MocaTransaction _dbTransaction;
    private SessionContext _session;
    private SystemContext _system;
    private RequestContext _request;
    private CommandRepository _commands;
    private NativeAdapterFactory _native;
    private CommandUsage _usage;
    private Collection<String> _blacklist;
    private MessageResolver _message;
    private RemoteConnectionFactory _remoteFactory;
    
    private MocaContext _moca;
}
