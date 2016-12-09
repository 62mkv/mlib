
mset command on
[select count(*) polcnt
   from poldat 
  where polcod = 'SYSTEM-INFORMATION'
    and polvar = 'CLIENT-UPDATE'
    and polval = 'UPDATE-PATH'
    and srtseq = 91
    and rtstr1 = '$MOCADIR\downloads\components'] |
if (@polcnt = 0)
{
    create policy
   where polcod = 'SYSTEM-INFORMATION'
    and polvar = 'CLIENT-UPDATE'
    and polval = 'UPDATE-PATH'
    and srtseq = 91
    and rtstr1 = '$MOCADIR\downloads\components'
    and cmnt = "Missing upgrade for downloads"
}
/
mset command off
