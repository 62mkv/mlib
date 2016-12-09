CREATE TABLE job_definition(
	job_id nvarchar2(256) NOT NULL,
	role_id nvarchar2(256) NULL,
	name varchar2(2000) NOT NULL,
	enabled number NOT NULL,
	type nvarchar2(60) NOT NULL,
	command nvarchar2(2000) NOT NULL,
	log_file varchar2(2000) NULL,
	trace_level nvarchar2(60) NULL,
	overlap number NOT NULL,
	schedule varchar2(2000) NULL,
	start_delay number NULL,
	timer number NULL,
	grp_nam nvarchar2(40) NULL,
 CONSTRAINT job_definition_pk PRIMARY KEY (job_id)
 );

CREATE TABLE job_env_definition(
	job_id nvarchar2(256) NOT NULL,
	name nvarchar2(256) NOT NULL,
	value nvarchar2(2000) NULL,
 CONSTRAINT job_env_definition_pk PRIMARY KEY(job_id,name)
 )

CREATE TABLE job_definition_exec(
        job_id nvarchar2(256) NOT NULL,
        node_url nvarchar2(256) NOT NULL,
        start_dte date NOT NULL,
        status number NULL,
        message nvarchar2(2000) NULL,
        end_dte date NULL,
     CONSTRAINT job_definition_exec_pk PRIMARY KEY(job_id,node_url,start_dte)
)