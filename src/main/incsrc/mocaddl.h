#use $LESDIR/include

#use $MOCADIR/include
#use $MOCADIR/db/ddl/Docs
#use $MOCADIR/db/ddl/Indexes

/*
 * UTF8_SIZE is used by c headers to multiply character sizes by four.
 * We don't want this enabled for database creation unless the specific
 * database calls for a varchar instead of nvarchar configuration.
 *
 * But the multiplication will be handled in the specific database type
 * files instead of as a global replace
 */
#define UTF8_SIZE

