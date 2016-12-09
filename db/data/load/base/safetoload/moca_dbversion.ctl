[ select count(*) row_count 
    from moca_dbversion 
]

|
   if (@row_count > 0) 
   {
       [ update moca_dbversion 
            set 
              version = '@version@' 
       ]
   }
   else
   {
       [ insert into moca_dbversion 
             (version) 
           VALUES 
             ('@version@') 
       ] 
   }
