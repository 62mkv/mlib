[ select count(*) row_count from task_definition where
    task_id = '@task_id@' ] | if (@row_count > 0) {
       [ update task_definition set
          task_id = '@task_id@'
,          role_id = '@role_id@'
,          name = '@name@'
,          task_typ = '@task_typ@'
,          cmd_line = '@cmd_line@'
,          run_dir = '@run_dir@'
,          log_file = '@log_file@'
,          restart = @restart@
,          auto_start = @auto_start@
,          start_delay = to_number('@start_delay@')
,          grp_nam = '@grp_nam@'
,          trace_level =  '@trace_level@'
             where  task_id = '@task_id@' ] }
             else { [ insert into task_definition
                      (task_id, role_id, name, task_typ, cmd_line, run_dir, log_file, restart, auto_start, start_delay, grp_nam, trace_level)
                      VALUES
                      ('@task_id@', '@role_id@', '@name@', '@task_typ@', '@cmd_line@', '@run_dir@', '@log_file@', @restart@, @auto_start@, to_number('@start_delay@'), '@grp_nam@', '@trace_level@') ] }
