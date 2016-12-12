#define STRING_TY(len)  nvarchar(__MOCA(publish data where something = 'GOOD' | publish data where value = @something) )
#define FOO_LEN 5
#define BAR_LEN 6

create table foo
(
    foobar STRING_TY(FOO_LEN)
)
