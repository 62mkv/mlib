[ select count(*) row_count from job_env_definition where
    job_id = '@job_id@' and name = '@name@' ] | if (@row_count > 0) {
       [ update job_env_definition set
          job_id = '@job_id@'
,          name = '@name@'
,          value = '@value@'
             where  job_id = '@job_id@' 
	       and name = '@name@'] }
             else { [ insert into job_env_definition
                      (job_id, name, value)
                      VALUES
                      ('@job_id@', '@name@', '@value@') ] }
