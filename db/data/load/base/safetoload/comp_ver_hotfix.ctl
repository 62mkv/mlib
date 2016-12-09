publish data 
    where comp_maj_ver = decode('@comp_maj_ver@', 
                                'dev', 0, 
				int('@comp_maj_ver@'))
      and comp_min_ver = decode('@comp_min_ver@', 
                                'dev', 0, 
				int('@comp_min_ver@'))
      and comp_bld_ver = decode('@comp_bld_ver@', 
                                'dev', 0, 
				int('@comp_bld_ver@'))
      and comp_rev_ver = decode('@comp_rev_ver@', 
                                'dev', 0, 
				int('@comp_rev_ver@'))
|
[
 select count(*) row_count
   from comp_ver
  where base_prog_id = '@base_prog_id@'
    and comp_maj_ver = @comp_maj_ver
    and comp_min_ver = @comp_min_ver 
    and comp_bld_ver = @comp_bld_ver
    and comp_rev_ver = @comp_rev_ver
]
|
if (@row_count > 0)
{
    [
     update comp_ver set comp_file_nam = '@comp_file_nam@',
                         comp_prog_id = '@comp_prog_id@',
                         comp_typ = '@comp_typ@',
                         comp_file_ext = '@comp_file_ext@',
                         comp_need_fw = '@comp_need_fw@',
                         lic_key = '@lic_key@',
                         grp_nam = '@grp_nam@'
      where base_prog_id = '@base_prog_id@'
        and comp_maj_ver = @comp_maj_ver
        and comp_min_ver = @comp_min_ver 
        and comp_bld_ver = @comp_bld_ver
        and comp_rev_ver = @comp_rev_ver
    ]
}
else
{
    [
     insert into comp_ver (base_prog_id,
                           comp_maj_ver,
                           comp_min_ver,
                           comp_bld_ver,
                           comp_rev_ver,
                           comp_file_nam,
                           comp_prog_id,
                           comp_typ,
                           comp_file_ext,
                           comp_need_fw,
                           lic_key,
                           grp_nam)
                   values ('@base_prog_id@',
                           @comp_maj_ver,
                           @comp_min_ver,
                           @comp_bld_ver,
                           @comp_rev_ver,
                           '@comp_file_nam@',
                           '@comp_prog_id@',
                           '@comp_typ@',
                           '@comp_file_ext@',
                           '@comp_need_fw@',
                           '@lic_key@',
                           '@grp_nam@')
    ]
}

