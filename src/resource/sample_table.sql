mset command on

[
DROP_TABLE (uc_bc_bl_interface)
] catch(@?)
RUN_DDL
aaa
[
CREATE_TABLE(uc_bc_bl_interface)
(
    wh_id            STRING_TY(10) not null,    /* PK */
    prt_client_id    STRING_TY(20) not null,  /* PK */
    prtnum           STRING_TY(50) not null, /* PK */
    uc_bc_bl         STRING_TY(10) not null,
    uc_status        STRING_TY(1) not null,
    uc_bc_bl_date           DATE_TY not null,
    uc_bc_bl_user      STRING_TY(20) not null
)
]catch (@?)
RUN_DDL

create db documentation
   where table = "uc_bc_bl_interface"
     and table_comment = "The table keep the value of uc_bc_bl: BL,BC for an item."
     and wh_id  = "Warehouse ID - the warehouse ID." 
     and prt_client_id = "Item client ID that the item belongs to."
     and prtnum = "Item number."
     and uc_bc_bl = "The value can be 'BL' or 'BC' only."
     and uc_status = "Action code, value can be: A-ADD, C-CHANGE or D-DELETE."
     and uc_bc_bl_date = "Record insert date."
     and uc_bc_bl_user = "Record insert user ID."
RUN_DDL
mset command off