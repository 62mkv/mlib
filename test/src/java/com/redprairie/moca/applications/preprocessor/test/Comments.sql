#define COL_LENGTH 5 /* This is to test to make sure multi line comments
                        are working correctly
		      */
#define RUN_SQL / /* Single line */

mset command on
publish data where foo = COL_LENGTH
RUN_SQL
mset command off
