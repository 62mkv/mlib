package com.redprairie.moca.web.console;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.ServerUtils.CurrentValues;
import com.redprairie.moca.server.TestServerUtils;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.session.InfinispanMocaSessionManager;
import com.redprairie.moca.server.session.MocaSessionManager;
import com.redprairie.moca.servlet.WebSessionManager;
import com.redprairie.moca.servlet.WebSessionManager.ClosedSessionCallback;
import com.redprairie.moca.util.AbstractMocaTestCase;
import com.redprairie.moca.web.WebResults;

/**
 * Created by j1014843 on 12/1/2014.
 */
public class TI_ConsoleModel extends AbstractMocaTestCase {
    
    private static final ConsoleModel CONSOLE_MODEL = new ConsoleModel(
        ServerUtils.globalAttribute(MocaClusterAdministration.class));
    
    //Since it's an infinispan session manager, I have to wait
    //until the daemon context is set up before i can do this. 
    //otherwise it complains about clustering setup.  That's why 
    //this is one of the only non static variables.
    private MocaSessionManager SESSION_MANAGER;
    
    @Override
    public void mocaSetUp() {
        SystemContext context = ServerUtils.globalContext();
        CurrentValues values = TestServerUtils.takeCurrentValues();
        AsynchronousExecutor executor = new MocaAsynchronousExecutor(
            values.getServerContextFactory(), 1);
        TestServerUtils.restoreValues(values);
        // Since this is a process based task (technically), MOCA doesn't allow
        // us to have a Asynchronous executor. So we're going to make one for
        // now.
        context.putAttribute(AsynchronousExecutor.class.getName(), executor);

        int timeout = Integer.parseInt(context.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_SESSION_IDLE_TIMEOUT,
            MocaRegistry.REGKEY_SERVER_SESSION_IDLE_TIMEOUT_DEFAULT));

        int sessionMax = Integer.parseInt(context.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_SESSION_MAX,
            MocaRegistry.REGKEY_SERVER_SESSION_MAX_DEFAULT));

        WebSessionManager mgr = new WebSessionManager(timeout,
            TimeUnit.SECONDS, sessionMax, new ClosedSessionCallback() {
                @Override
                public void onSessionClose(SessionContext sessionContext) {
                    // Stubbed
                }
            });

        context.putAttribute(WebSessionManager.class.getName(), mgr);

        SESSION_MANAGER = new InfinispanMocaSessionManager("",
            new String[] { "" }, "moca-sessions", 1000, false);
        ServerUtils.globalContext().putAttribute(MocaSessionManager.class.getName(), SESSION_MANAGER);
    }
    
    @Test
    public void testGetJobHistory() throws MocaException {
        try {
            _moca.executeCommand("[insert into job_definition_exec(job_id, node_url, status, message, start_dte, end_dte) " +
                                "values (@job_id, @node_url, @status, @message, @start_dte:date, @end_dte:date)]", 
                                new MocaArgument("job_id", "TI_ConsoleModel"),
                                new MocaArgument("node_url", "localhost"),
                                new MocaArgument("status", 0),
                                new MocaArgument("message", "TI_ConsoleModel"),
                                new MocaArgument("start_dte", new Date(System.currentTimeMillis())),
                                new MocaArgument("end_dte", new Date(System.currentTimeMillis())));
            
            _moca.commit();
            @SuppressWarnings("unchecked")
            WebResults<MocaResults> results = (WebResults<MocaResults>) CONSOLE_MODEL.getJobHistory(createParameterMap(new String[0]));
            checkForRowCountAndColumns(results.getData().get(0), null, 
                "job_id", "node_url", "status", "message", "start_dte", "end_dte");
        }
        finally {
            _moca.executeCommand("[delete from job_definition_exec where job_id = @job_id] catch (-1403)", 
                    new MocaArgument("job_id", "TI_ConsoleModel"));
            _moca.commit();
        }
    }
    
    @Test
    public void testGetTaskHistory() throws MocaException {
        try {
            _moca.executeCommand("[insert into task_definition_exec(task_id, node_url, status, start_cause, start_dte, end_dte) " +
                    "values (@task_id, @node_url, @status, @start_cause, @start_dte:date, @end_dte:date)]", 
                    new MocaArgument("task_id", "TI_ConsoleModel"),
                    new MocaArgument("node_url", "localhost"),
                    new MocaArgument("status", 0),
                    new MocaArgument("start_cause", "TI_ConsoleModel"),
                    new MocaArgument("start_dte", new Date(System.currentTimeMillis())),
                    new MocaArgument("end_dte", new Date(System.currentTimeMillis())));
            @SuppressWarnings("unchecked")
            WebResults<MocaResults> results = (WebResults<MocaResults>) CONSOLE_MODEL.getTaskHistory(createParameterMap(new String[0]));
            checkForRowCountAndColumns(results.getData().get(0), null, 
                "task_id", "node_url", "status", "start_cause", "start_dte", "end_dte");
        }
        finally {
            _moca.executeCommand("[delete from task_definition_exec where task_id = @task_id] catch (-1403)", 
                new MocaArgument("task_id", "TI_ConsoleModel"));
            _moca.commit();
        }
    }
    
    private void checkForRowCountAndColumns(MocaResults res, Integer rowCount, String ...cols) {
        
        if (cols != null) {
            for (String column : cols) {
                assertTrue("Checking for column: " + column + " failed", res.containsColumn(column));
            }
            assertEquals(cols.length, res.getColumnCount());
        }
        
        if(rowCount != null) {
            assertEquals(rowCount, Integer.valueOf(res.getRowCount()));
        }
    }
    
    private Map<String, String[]> createParameterMap(String ... args) {
        Map<String, String[]> map = new HashMap<>();
        
        if(args.length % 2 != 0) {
            throw new IllegalArgumentException("Uneven number of args");
        }
        
        for(int i = 0; i<args.length; i=i+2) {
            map.put(args[i], new String[]{args[i+1]});
        }
         
        return map;
    }
}
