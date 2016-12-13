/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.crud;

import java.sql.Types;
import java.util.Collections;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.service.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.service.jdbc.dialect.internal.DialectFactoryImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.components.crud.TU_CrudService;
import com.redprairie.moca.db.hibernate.HibernateTools;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.util.MocaUtils;

/**
 * This is an abstract class that is used to basically create a dummy table
 * for testing crud commands.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public abstract class TU_AbstractCrudTableCreate {
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerContext context = ServerUtils.setupDaemonContext(
                TU_CrudService.class.getName(), true);
        
        // We create a temporary table of test_codmst so we can use this for
        // our testing
        StringBuilder tableCreate = new StringBuilder();
        DBType type = context.getDbType();
        
        DialectFactoryImpl dialectFactory = new DialectFactoryImpl();
        dialectFactory.setClassLoaderService(new ClassLoaderServiceImpl());
        
        switch (type) {
        case MSSQL:
            _dialect = dialectFactory.buildDialect(Collections.singletonMap(
                AvailableSettings.DIALECT,
                "com.redprairie.moca.db.hibernate.UnicodeSQLServerDialect"),
                null);
            break;
        case ORACLE:
            _dialect = dialectFactory.buildDialect(Collections.singletonMap(
                AvailableSettings.DIALECT,
                "org.hibernate.dialect.Oracle10gDialect"), null);
            break;
        default:
            throw new RuntimeException("There was no database supplied!");
        }

        tableCreate.append(_dialect.getCreateTableString());
        tableCreate.append(' ');
        tableCreate.append(tableName);
        tableCreate.append(" ( ");
        
        if(type.equals(DBType.MSSQL)){
            //identity column
            tableCreate.append( "rowid");
            tableCreate.append(' ');
            tableCreate.append(_dialect.getTypeName(Types.INTEGER));
            tableCreate.append(' ');
            tableCreate.append(_dialect.getIdentityColumnString(Types.INTEGER));
            tableCreate.append(",  ");
        }
        
        // colnam
        tableCreate.append( "colnam");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.VARCHAR, 20, 0, 0 ));
        tableCreate.append(" not null ");
        tableCreate.append(",  ");
        // codval
        tableCreate.append("codval");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.VARCHAR, 40, 0, 0 ));
        tableCreate.append(" not null ");
        tableCreate.append(", ");
        // change
        tableCreate.append("change");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.VARCHAR, 40, 0, 0 ));
        tableCreate.append(", ");
        // change_dt
        tableCreate.append("change_dt");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.TIMESTAMP ));
        tableCreate.append(", ");
        // change_int
        tableCreate.append("change_int");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.INTEGER ));
        tableCreate.append(", ");
        // ins_dt
        tableCreate.append("ins_dt");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.TIMESTAMP ));
        tableCreate.append(", ");
        // ins_user_id
        tableCreate.append("ins_user_id");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.VARCHAR, 20, 0, 0 ));
        tableCreate.append(", ");
        // last_upd_dt
        tableCreate.append("last_upd_dt");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.TIMESTAMP ));
        tableCreate.append(", ");
        // last_upd_user_id
        tableCreate.append("last_upd_user_id");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.VARCHAR, 20, 0, 0 ));
        tableCreate.append(", ");
        // u_version
        tableCreate.append("u_version");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.INTEGER ));
        tableCreate.append(", ");
        // defaulted not null flag
        tableCreate.append("def_flg");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.INTEGER ));
        tableCreate.append(" default 1 not null ");
        tableCreate.append(", ");
        // int_non_null value
        tableCreate.append("int_non_null");
        tableCreate.append(' ');
        tableCreate.append(_dialect.getTypeName( Types.INTEGER ));
        tableCreate.append(" not null ");
        
        tableCreate.append(')');

        Session session = HibernateTools.getSession();
        
        SQLQuery query = session.createSQLQuery(tableCreate.toString());
        query.executeUpdate();
        
        // We have to create an index as well, since our list
        StringBuilder indexCreate = new StringBuilder();
        indexCreate.append("alter table ");
        indexCreate.append(tableName);
        indexCreate.append(' ');
        indexCreate.append(_dialect.getAddPrimaryKeyConstraintString("pk"));
        indexCreate.append(" ( colnam, codval )");
        
        SQLQuery indexQuery = session.createSQLQuery(indexCreate.toString());
        indexQuery.executeUpdate();
    }
    
    @AfterClass
    public static void afterClass() {
        // Lastly we have to drop the table since in oracle the ddl isn't
        // transactional it gets committed automatically
        Session session = HibernateTools.getSession();
        session.createSQLQuery("drop table " + tableName).executeUpdate();
        
        try {
            MocaUtils.currentContext().rollback();
        }
        catch (MocaException e) {
            e.printStackTrace();
        }
    }
    
    @Before
    public void beforeEachTest() {
        _moca = MocaUtils.currentContext();
    }
    
    protected MocaContext _moca;
    protected final static String tableName = "test_codmst";
    
    private static Dialect _dialect;
}
