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

package com.redprairie.moca;

/**
 * Constants for the MOCA registry.
 *
 * <b><pre>
 * Copyright (c) 20167-2009 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Mike Lange
 * @version $Revision$
 */
public class MocaRegistry {

    // Section Names
    public static final String REGSEC_SERVER = "SERVER";
    public static final String REGSEC_CLUSTER = "CLUSTER";
    public static final String REGSEC_NATIVE = "NATIVE";
    public static final String REGSEC_DATABASE = "DATABASE";
    public static final String REGSEC_EMS = "EMS";
    public static final String REGSEC_ENVIRONMENT = "ENVIRONMENT";
    public static final String REGSEC_SECURITY = "SECURITY";
    public static final String REGSEC_LICENSE = "LICENSE";
    public static final String REGSEC_SERVER_MAPPING = "SERVER MAPPING";
    public static final String REGSEC_CLIENTS = "CLIENTS";
    public static final String REGSEC_ZABBIX = "ZABBIX";

    // Server Section Keys
    public static final String REGKEY_SERVER_URL = "server.url";
    public static final String REGKEY_SERVER_PORT = "server.port";
    public static final String REGKEY_SERVER_RMI_PORT = "server.rmi-port";
    public static final String REGKEY_SERVER_CLASSIC_PORT = "server.classic-port";
    public static final String REGKEY_SERVER_CLASSIC_POOL_SIZE = "server.classic-pool-size";
    public static final String REGKEY_SERVER_CLASSIC_ENCODING = "server.classic-encoding";
    public static final String REGKEY_SERVER_CLASSIC_IDLE_TIMEOUT = "server.classic-idle-timeout";
    public static final String REGKEY_TRACE_FILE = "server.trace-file";
    public static final String REGKEY_TRACE_LEVEL = "server.trace-level";
    public static final String REGKEY_SERVER_MEMORY_FILE = "server.memory-file";
    public static final String REGKEY_SERVER_MAILBOX_FILE = "server.mailbox-file";
    public static final String REGKEY_SERVER_INHIBIT_TASKS = "server.inhibit-tasks";
    public static final String REGKEY_SERVER_INHIBIT_JOBS = "server.inhibit-jobs";
    public static final String REGKEY_SERVER_COMMAND_PROFILE = "server.command-profile";
    public static final String REGKEY_SERVER_ARG_BLACKLIST = "server.arg-blacklist";
    public static final String REGKEY_SERVER_MIN_IDLE_POOL_SIZE = "server.min-idle-pool-size";
    public static final String REGKEY_SERVER_MAX_POOL_SIZE = "server.max-pool-size";
    public static final String REGKEY_SERVER_TEST_DISABLE_NATIVE = "server.test-disable-native";
    public static final String REGKEY_SERVER_PROCESS_TIMEOUT = "server.process-timeout";
    public static final String REGKEY_SERVER_MAX_SERVER_REQUESTS = "server.max-commands";
    public static final String REGKEY_SERVER_PROD_DIRS = "server.prod-dirs";
    public static final String REGKEY_SERVER_SESSION_IDLE_TIMEOUT = "server.session-idle-timeout";
    public static final String REGKEY_SERVER_SESSION_MAX = "server.session-max";
    public static final String REGKEY_SERVER_BIND_LOG = "server.bind-log";
    public static final String REGKEY_SERVER_QUERY_LIMIT= "server.query-limit";
    public static final String REGKEY_SERVER_MAX_ASYNC_THREAD = "server.max-async-thread";
    public static final String REGKEY_SERVER_COMPRESSION = "server.compression";
    public static final String REGKEY_SERVER_TRANSACTION_MANAGER = "server.transaction-manager";
    public static final String REGKEY_SERVER_MAD_PROBING_ENABLED = "server.mad-probing-enabled";
    public static final String REGKEY_SERVER_SUPPORT_ZIP_TIMEOUT = "server.support-zip-timeout";

    // Service Keys
    public static final String REGKEY_SERVICE_OUTPUT = "service.output";
    
    // Cluster Keys
    public static final String REGKEY_CLUSTER_NAME = "cluster.name";
    public static final String REGKEY_CLUSTER_COOKIE_DOMAIN = "cluster.cookie-domain";
    public static final String REGKEY_CLUSTER_JGROUPS_XML = "cluster.jgroups-xml";
    public static final String REGKEY_CLUSTER_JGROUPS_BIND_ADDR = "cluster.jgroups-bind-addr";
    public static final String REGKEY_CLUSTER_JGROUPS_BIND_INTERFACE = "cluster.jgroups-bind-interface";
    public static final String REGKEY_CLUSTER_JGROUPS_BIND_PORT = "cluster.jgroups-bind-port";
    public static final String REGKEY_CLUSTER_JGROUPS_COMPRESSION = "cluster.jgroups-compress";
    public static final String REGKEY_CLUSTER_JGROUPS_TCP_HOSTS = "cluster.jgroups-tcp-hosts";
    public static final String REGKEY_CLUSTER_JGROUPS_TCP_PING_TIMEOUT = "cluster.jgroups-tcp-ping-timeout";
    public static final String REGKEY_CLUSTER_JGROUPS_TCP_PORT_RANGE = "cluster.jgroups-tcp-port-range";
    public static final String REGKEY_CLUSTER_JGROUPS_TCPPING_PORT_RANGE = "cluster.jgroups-tcpping-port-range";
    public static final String REGKEY_CLUSTER_JGROUPS_TIMER_MAX_THREADS = "cluster.jgroups-timer-max-threads";
    public static final String REGKEY_CLUSTER_JGROUPS_JOIN_TIMEOUT = "cluster.jgroups-join-timeout";
    public static final String REGKEY_CLUSTER_JGROUPS_PROTOCOL = "cluster.jgroups-protocol";
    public static final String REGKEY_CLUSTER_JGROUPS_MCAST_ADDR = "cluster.jgroups-mcast-addr";
    public static final String REGKEY_CLUSTER_JGROUPS_MCAST_PORT = "cluster.jgroups-mcast-port";
    public static final String REGKEY_CLUSTER_JGROUPS_GMS_MERGE_TIMEOUT = "cluster.jgroups-merge-timeout";
    public static final String REGKEY_CLUSTER_MANAGER = "cluster.role-manager";
    public static final String REGKEY_CLUSTER_ROLES = "cluster.roles";
    public static final String REGKEY_CLUSTER_EXCLUDE_ROLES = "cluster.exclude-roles";
    public static final String REGKEY_CLUSTER_CHECK_RATE = "cluster.role-check-rate";
    public static final String REGKEY_CLUSTER_ASYNC_RUNNERS = "cluster.async-runners";
    public static final String REGKEY_CLUSTER_ASYNC_SUBMIT_CAP = "cluster.async-submit-cap";
    public static final String REGKEY_CLUSTER_REMOTE_RETRY_LIMIT = "cluster.remote-retry-limit";
    
    // Environment Keys
    public static final String REGKEY_ENVIRONMENT_PATH = "environment.path";

    // Database Section Keys
    public static final String REGKEY_DB_USERNAME = "database.username";
    public static final String REGKEY_DB_PASSWORD = "database.password";
    public static final String REGKEY_DB_URL = "database.url";
    public static final String REGKEY_DB_DRIVER = "database.driver";
    public static final String REGKEY_DB_DBA_USERNAME = "database.dba-username";
    public static final String REGKEY_DB_DBA_PASSWORD = "database.dba-password";
    public static final String REGKEY_DB_MIN_IDLE_CONNECTIONS = "database.min-idle-conn";
    public static final String REGKEY_DB_MAX_CONNECTIONS = "database.max-conn";
    public static final String REGKEY_DB_CONNECTION_TIMEOUT = "database.conn-timeout";
    public static final String REGKEY_DB_DIALECT = "database.dialect";
    public static final String REGKEY_DB_ISOLATION = "database.isolation";
    public static final String REGKEY_DB_POOL_VALIDATE_ON_CHECKOUT = "database.pool-validate-on-checkout";
    
    // EMS Section Keys
    public static final String REGKEY_EMS_URL = "ems.url";
    public static final String REGKEY_EMS_SPOOL_DIR = "ems.spool-dir";
    public static final String REGKEY_EMS_PROCESSED_DIR = "ems.processed-dir";
    public static final String REGKEY_EMS_BAD_DIR = "ems.bad-dir";

    // Security Section Keys
    public static final String REGKEY_SECURITY_DOMAIN = "security.domain";
    public static final String REGKEY_SECURITY_TRUSTED_DOMAINS = "security.trusted-domains";
    public static final String REGKEY_SECURITY_ACCESS_PASSWORD = "security.access-password";
    public static final String REGKEY_SECURITY_LDAP_URL = "security.ldap-url";
    public static final String REGKEY_SECURITY_LDAP_BIND_DN = "security.ldap-bind-dn";
    public static final String REGKEY_SECURITY_LDAP_BIND_PASSWORD = "security.ldap-bind-password";
    public static final String REGKEY_SECURITY_LDAP_AUTH_TYPE = "security.ldap-auth-type";
    public static final String REGKEY_SECURITY_LDAP_REFERRALS = "security.ldap-referrals";
    public static final String REGKEY_SECURITY_LDAP_UID_ATTR = "security.ldap-uid-attr";
    public static final String REGKEY_SECURITY_LDAP_ROLE_ATTR = "security.ldap-role-attr";
    public static final String REGKEY_SECURITY_ENCRYPTION = "security.encryption";
    public static final String REGKEY_SECURITY_ADMIN_USER = "security.admin-user";
    public static final String REGKEY_SECURITY_ADMIN_PASSWORD = "security.admin-password";
    public static final String REGKEY_SECURITY_ALLOW_LEGACY_SESSIONS = "security.allow-legacy-sessions";
    public static final String REGKEY_SECURITY_SESSION_KEY_IDLE_TIMEOUT = "security.session-key-idle-timeout";
    public static final String REGKEY_SECURITY_SESSION_REMOTE_TIMEOUT = "security.remote-timeout";
    public static final String REGKEY_SECURITY_SERVER_KEY = "security.server-key";

    // License Section Keys
    public static final String REGKEY_LICENSE_LICENSEE = "license.licensee";
    public static final String REGKEY_LICENSE_PRODUCT_LIST = "license.product-list";
    public static final String REGKEY_LICENSE_USERS = "license.users";
    public static final String REGKEY_LICENSE_EXPDATE = "license.expdate";
    public static final String REGKEY_LICENSE_KEY = "license.key";
    
    // Zabbix Section Keys
    public static final String REGKEY_ZABBIX_IP = "zabbix.server-ip";
    public static final String REGKEY_ZABBIX_LOCAL_AGENT_PORT = "zabbix.local-agent-port";
    public static final String REGKEY_ZABBIX_TRAPPER_PORT = "zabbix.trapper-port";
    public static final String REGKEY_ZABBIX_HOSTS_GROUP = "zabbix.host-groups";
    public static final String REGKEY_ZABBIX_APPLICATION_TEMPLATES = "zabbix.application-templates";
    public static final String REGKEY_ZABBIX_API_USER = "zabbix.api-user";
    public static final String REGKEY_ZABBIX_API_PASSWORD = "zabbix.api-password";
    public static final String REGKEY_ZABBIX_ENABLE_MACHINE_SYNC = "zabbix.machine-sync-enabled";
    public static final String REGKEY_ZABBIX_ENABLE_APPLICATION_SYNC = "zabbix.application-sync-enabled";
    public static final String REGKEY_ZABBIX_HOST_IP = "zabbix.host-ip";
    public static final String REGKEY_ZABBIX_USE_DNS = "zabbix.use-dns";
    
    // Server Section Defaults
    public static final String REGKEY_SERVER_PORT_DEFAULT = "4500";
    public static final String REGKEY_SERVER_RMI_PORT_DEFAULT = "4501";
    public static final String REGKEY_SERVER_MIN_IDLE_POOL_SIZE_DEFAULT = "1";
    public static final String REGKEY_SERVER_MAX_POOL_SIZE_DEFAULT = "20";
    public static final String REGKEY_SERVER_PROCESS_TIMEOUT_DEFAULT = "20";
    public static final String REGKEY_SERVER_MAX_SERVER_REQUESTS_DEFAULT = "10000";
    public static final String REGKEY_SERVER_SESSION_IDLE_TIMEOUT_DEFAULT = "3600";
    public static final String REGKEY_SERVER_SESSION_MAX_DEFAULT = "10000";
    public static final String REGKEY_SERVER_QUERY_LIMIT_DEFAULT = "100000";
    public static final String REGKEY_SERVER_MAX_ASYNC_THREAD_DEFAULT = "20";
    public static final String REGKEY_SERVER_TRANSACTION_MANAGER_DEFAULT = 
        "com.arjuna.ats.jta.TransactionManager";
    public static final String REGKEY_SERVER_MAD_PROBING_ENABLED_DEFAULT = "true";
    public static final String REGKEY_SERVER_SUPPORT_ZIP_TIMEOUT_DEFAULT = "2000";
    public static final String REGKEY_SERVER_TEST_DISABLE_NATIVE_DEFAULT = "false";
    
    // Database Section Defaults
    public static final String REGKEY_DB_MIN_IDLE_CONNECTIONS_DEFAULT = "1";
    public static final String REGKEY_DB_MAX_CONNECTIONS_DEFAULT = "100";
    public static final String REGKEY_DB_POOL_VALIDATE_ON_CHECKOUT_DEFAULT = "false";
    
    // Cluster Section Defaults
    public static final String REGKEY_CLUSTER_MANAGER_DEFAULT = "preferred";
    public static final String REGKEY_CLUSTER_CHECK_RATE_DEFAULT = "10";
    public static final String REGKEY_CLUSTER_ASYNC_RUNNERS_DEFAULT = "10";
    public static final String REGKEY_CLUSTER_ASYNC_SUBMIT_CAP_DEFAULT = "10000";
    public static final String REGKEY_CLUSTER_JGROUPS_BIND_PORT_DEFAULT = "7800";
    public static final String REGKEY_CLUSTER_JGROUPS_PROTOCOL_DEFAULT = "udp";
    public static final String REGKEY_CLUSTER_JGROUPS_MCAST_PORT_DEFAULT = "46655";
    public static final String REGKEY_CLUSTER_JGROUPS_MCAST_ADDR_DEFAULT = "228.6.7.8";
    public static final String REGKEY_CLUSTER_JGROUPS_TCP_PING_TIMEOUT_DEFAULT = "5000";    
    public static final String REGKEY_CLUSTER_JGROUPS_COMPRESSION_DEFAULT = "false";
    public static final String REGKEY_CLUSTER_JGROUPS_GMS_MERGE_TIMEOUT_DEFAULT = "15000";
    public static final String REGKEY_CLUSTER_JGROUPS_JOIN_TIMEOUT_DEFAULT = "5000";
    public static final String REGKEY_CLUSTER_JGROUPS_TCP_PORT_RANGE_DEFAULT = "30";
    public static final String REGKEY_CLUSTER_JGROUPS_TCPPING_PORT_RANGE_DEFAULT = "5";
    public static final String REGKEY_CLUSTER_JGROUPS_TIMER_MAX_THREADS_DEFAULT = "10";
    public static final String REGKEY_CLUSTER_REMOTE_RETRY_LIMIT_DEFAULT = "5";
    
    // Security Section Defaults
    public static final String REGKEY_SECURITY_SESSION_REMOTE_TIMEOUT_DEFAULT = "86400";
    public static final String REGKEY_SECURITY_SESSION_KEY_IDLE_TIMEOUT_DEFAULT = "1800";
    
    // Zabbix Section Defaults
    public static final String REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT = "10050";
    public static final String REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT = "10051";
    public static final String REGKEY_ZABBIX_API_USER_DEFAULT = "admin";
    public static final String REGKEY_ZABBIX_API_PASSWORD_DEFAULT = "zabbix";
    public static final String REGKEY_ZABBIX_ENABLE_MACHINE_SYNC_DEFAULT = "true";
    public static final String REGKEY_ZABBIX_ENABLE_APPLICATION_SYNC_DEFAULT = "true";
    
    // RPWriter Configuration
    // Application Server Config
    public static final String REGKEY_MOCARPT_APP_SERVER = "mocarpt.app-server";
    public static final String REGKEY_MOCARPT_REPORT_SERVER = "mocarpt.report-server";
    public static final String REGKEY_MOCARPT_ARCHIVE_IS_LOCAL="mocarpt.archive-is-local";
    
    // Report Server Config
    public static final String REGKEY_MOCARPT_ARCHIVE_FOLDER = "mocarpt.archive-folder";    
    public static final String REGKEY_MOCARPT_CACHE_FOLDER = "mocarpt.cache-folder";
    public static final String REGKEY_MOCARPT_DB_DT_FORMAT = "mocarpt.db-date-time-format";
    public static final String REGKEY_MOCARPT_DB_INIT_CMD = "mocarpt.db-init-command";
    public static final String REGKEY_MOCARPT_DEF_LOCALE_ID = "mocarpt.default-locale-id";
    public static final String REGKEY_MOCARPT_EMS_ATTACH_FORMAT = "mocarpt.ems-attachment-format";
    public static final String REGKEY_MOCARPT_EMS_FOLDER = "mocarpt.ems-folder";
    public static final String REGKEY_MOCARPT_LIB_FOLDER = "mocarpt.lib-folder";
    public static final String REGKEY_MOCARPT_FAILURE_LOG_PATH = "mocarpt.failure-log-path";
    public static final String REGKEY_MOCARPT_FAILURE_LOG_KEEP_DAYS = "mocarpt.failure-log-keep-days";
    public static final String REGKEY_MOCARPT_DB_CONN_STR = "mocarpt.local-db-connection";
    

    // Cache configuration
    public static final String REGKEY_CACHE = "cache";
    public static final String REGKEY_CACHE_SIZE_SUFFIX = "size";
    public static final String REGKEY_CACHE_CONTROLLER_SUFFIX = "controller";
    public static final String REGKEY_CACHE_FACTORY_SUFFIX = "factory";
    public static final String REGKEY_CACHE_TIMEOUT_SUFFIX = "timeout";
    
    // Java configuration
    public static final String REGKEY_JAVA_VM = "java.vm";
    public static final String REGKEY_JAVA_VM32 = "java.vm32";
    public static final String REGKEY_JAVA_VMARGS = "java.vmargs";
    public static final String REGKEY_JAVA_VMARGS32 = "java.vmargs32";

}
