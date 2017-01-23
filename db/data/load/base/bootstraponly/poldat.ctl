if ('@grp_id@' = '')
{
    publish data
      where grp_id = '----'
}
else
{
    publish data
      where grp_id = '@grp_id@'
}
|
[ select count(*) row_count
    from poldat 
   where polcod = '@polcod@' 
     and polvar = '@polvar@' 
     and polval = '@polval@' 
     and srtseq = @srtseq@
     and grp_id = @grp_id ] 
 | 
 if (@row_count > 0) 
 {
      change record  
        where table_name ='poldat'
          and polcod = '@polcod@'
          and polvar = '@polvar@'
          and polval = '@polval@'
          and srtseq = @srtseq@
          and rtstr1 = '@rtstr1@'
          and rtstr2 = '@rtstr2@'
          and rtnum1 = int('@rtnum1@')
          and rtnum2 = int('@rtnum2@')
          and rtflt1 = float('@rtflt1@')
          and rtflt2 = float('@rtflt2@')
          and grp_id = @grp_id
  }
  else 
  {  
      create record  
      where table_name ='poldat'
      and polcod = '@polcod@'
      and polvar = '@polvar@'
      and polval = '@polval@'
      and srtseq = @srtseq@
      and rtstr1 = '@rtstr1@'
      and rtstr2 = '@rtstr2@'
      and rtnum1 = int('@rtnum1@')
      and rtnum2 = int('@rtnum2@')
      and rtflt1 = float('@rtflt1@')
      and rtflt2 = float('@rtflt2@')
      and grp_id = @grp_id
  }

