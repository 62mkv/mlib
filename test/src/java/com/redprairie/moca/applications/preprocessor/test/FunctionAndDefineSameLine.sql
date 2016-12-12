#define FOO_PRE test1
#define FOO_FUNC(bar) foo
#define FOO_POST test2

create table foo
(
    FOO_PRE FOO_FUNC(bar) FOO_POST
)
