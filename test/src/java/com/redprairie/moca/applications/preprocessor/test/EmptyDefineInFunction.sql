#define FOO_LENGTH 5
#define FOO_EXTRA

#define FOO_FUNC(len, extra) foo ( len - extra )

create table foo
(
    FOO_FUNC(FOO_LENGTH, FOO_EXTRA)
)