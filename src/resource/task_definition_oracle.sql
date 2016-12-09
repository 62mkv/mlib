CREATE TABLE task_definition(
	task_id nvarchar2(256) NOT NULL,
	role_id nvarchar2(256) NULL,
	name nvarchar2(2000) NOT NULL,
	task_typ nvarchar2(1) NOT NULL,
	cmd_line nvarchar2(2000) NOT NULL,
	run_dir nvarchar2(2000) NULL,
	log_file nvarchar2(2000) NULL,
	trace_level nvarchar2(60) NULL,
	restart number NOT NULL,
	auto_start number NOT NULL,
	start_delay number NULL,
	grp_nam nvarchar2(40) NULL,
 CONSTRAINT task_definition_pk PRIMARY KEY(task_id)
);

CREATE TABLE task_env_definition(
	task_id nvarchar2(256) NOT NULL,
	name nvarchar2(256) NOT NULL,
	value nvarchar2(2000) NULL,
 CONSTRAINT task_env_definition_pk PRIMARY KEY (task_id,name)
);

create view role_definition as 
select distinct coalesce(role_id, task_id) as role_id 
   from task_definition 
union
select distinct role_id as role_id 
   from job_definition 
     where role_id is not null;

CREATE TABLE task_definition_exec(
        task_id nvarchar2(256) NOT NULL,
        node_url nvarchar2(256) NOT NULL,
        start_dte date NOT NULL,
        end_dte date NULL,
        start_cause nvarchar2(2000) NULL,
        status nvarchar2(2000) NULL,
     CONSTRAINT task_definition_exec_pk PRIMARY KEY (task_id, node_url, start_dte)
    );