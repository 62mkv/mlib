[ select count(*) row_count from task_env_definition where
    task_id = '@task_id@' and name = '@name@' ] | if (@row_count > 0) {
       [ update task_env_definition set
          task_id = '@task_id@'
,          name = '@name@'
,          value = '@value@'
             where  task_id = '@task_id@' 
	       and name = '@name@'] }
             else { [ insert into task_env_definition
                      (task_id, name, value)
                      VALUES
                      ('@task_id@', '@name@', '@value@') ] }
