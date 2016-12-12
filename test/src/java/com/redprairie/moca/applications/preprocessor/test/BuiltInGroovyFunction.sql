#define STRING_TY(len)  nvarchar(__MOCA([[ value = (int)Math.pow(len * BAR_LEN, 1)]]) )
#define FOO_LEN 5
#define BAR_LEN 6

create table foo
(
    foobar STRING_TY(FOO_LEN)
)
