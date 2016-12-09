#
# dbupgrade.pl
#
#  This script will process all of the files in the current directory.  
#  Currently, the following file types supported are listed below, along with
#  the command it will use:
#
#:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
#
#  <filename>.sql:
#  system("perl $MOCADIR/scripts/InstallSQL.pl <filename>.sql");
#
#  Example:  contents of a.sql
#
#  #include <../../../include/mocaddl.h>
#  #include <mocacolwid.h>
#  #include <mocatbldef.h>
#  #include <sqlDataTypes.h>
#
#  ALTER_TABLE_TABLE_INFO(aremst)
#  ALTER_TABLE_ADD_COLUMN_START(pdflg)
#          FLAG_TY
#  ALTER_TABLE_ADD_COLUMN_END
#
#  RUN_DDL
#
#  mset command on
#  [select srtseq
#     from poldat
#    where polcod = 'RCVTRKOPR'
#      and polvar = 'MISCELLANEOUS'
#      and polval = 'CLOSE-TRUCK-REPORT']
#  |
#  change policy
#   where polcod = 'RCVTRKOPR'
#     and polvar = 'MISCELLANEOUS'
#     and polval = 'CLOSE-TRUCK-REPORT'
#     and srtseq = @srtseq
#     and rtnum1 = 0
#     and rtnum2 = 1
#     and cmnt = "5.1.3 upgrade."
#  /
#  mset command off
#
#  delete from les_mls_cat
#  where mls_id = 'chkSubDtlEna'
#    and cust_lvl = '0'
#
#  RUN_SQL
#
#
#  commit
#  RUN_SQL
#
#
#:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
#
#  <filename>.pl:
#  system("perl <filename>.pl");
#
#  Example:  contents of a.pl
#
#  Any perl script
#
#:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
#
#  <filename>.msql:
#  open (MSQL, "|$MOCADIR/bin/msql -S") || die "Unable to run SQL command: $!";
#  print MSQL "\@<filename>.msql\n";
#  print MSQL "/\n";
#  print MSQL;
#  close MSQL;
#
#  Example:  contents of a.msql
#
#  [select '$MOCADIR/db/upgrade/5.1.0/SL_PR9964.SLEXP' file_name, 'sl_' table_name, '' for_sys_id  from  dual]| sl_get unload_data_file|sl_load data where insert_on ly_flg = 'F'
#
#  /
#
#:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
#
#
#  <filename>.mload:
#  - parse <filename>.mload file for parameters
#    Note:  Variables are allowed as part of parameter values,
#           but they must be present in the environment.
#  system ("mload <optnam> <option_value> <optnam> <option_value> ...");
#
#  Example:  contents of a.mload
#
#  -H
#  -D=$MOCADIR/db/data/load/base/safetoload/les_mls_cat
#  -d=les_mls_cat_fieldnames.csv
#  -c=$MOCADIR/db/data/load/base/safetoload/les_mls_cat.ctl
#
#  or
#
#  -H
#  -d=$MOCADIR/db/data/load/base/safetoload/les_mls_cat/les_mls_cat_tab.csv
#  -c=$MOCADIR/db/data/load/base/safetoload/les_mls_cat.ctl
#
###############################################################################

#
# Include needed sub-components
#

require "getopts.pl";

#
# Local Variables
#

$MOCADIR=$ENV{"MOCADIR"};
$UPGRADEDIR="$MOCADIR/db/upgrade/1.8.0";
my ($dir) = @_;
my (@dir_list, $file_name);
my ($parsed_file_name, $file_ext);
my ($separator1, $separator2);
my ($mload_cmd, $mload_opt, $mload_opt_value);

$arg_showwarnings = "";
$arg_filename = "";
$arg_printhelp = "";

#
# Local subroutines
#

#----------------------------------------------------------------------------
sub PrintHelp
{
    print "Usage: db_upgrade.pl [-h] [-w] [-f <filename>]\n";
    print "       -w            Show warning messages\n";
    print "       -f <filename> Process <filename> only\n";
    print "       -h            Show this message\n";
}
#----------------------------------------------------------------------------
sub GetEnvVar
{
    my $varname = shift;
    my $value;
    
    # Get the environment variable from the environment itself

    $value = $ENV{$varname};

    return $value;

}
#----------------------------------------------------------------------------
sub ExpandEnvVars
{
    my $string = shift;

    # Expand the environment variable
    $string =~ s/\$([A-Za-z0-9_]*)/GetEnvVar($1)/ge;
    $string =~ s/%([A-Za-z0-9_]*)%/GetEnvVar($1)/ge;

    return $string
}
#----------------------------------------------------------------------------

#----------------------------------------------------------------------------
#
# Main
#
#----------------------------------------------------------------------------

#
# Get Command Line Arguments
#

$status = &Getopts ('wf:h');

if (!$status)
{
    &PrintHelp;
    exit (1)
}

$arg_showwarnings = "1" if ($opt_w);
$arg_filename = $opt_f if ($opt_f);
$arg_printhelp = $opt_h if ($opt_h);

if ($arg_printhelp)
{
    &PrintHelp;
    exit(0);
}

if ($arg_filename)
{
    if ( ! -e $arg_filename)
    {
        print "File not found: $arg_filename\n";
        &PrintHelp;
        exit(1);
    }
}

$separator1 = "==============================================================";
$separator2 = "--------------------------------------------------------------";

print "$separator1\n";

print "Running migration for $UPGRADEDIR\n";
print "$separator2\n";
print "      (Gathering upgrade scripts for migration)\n";

if ($arg_filename)
{
    @dir_list = $arg_filename;
}
else
{
    chdir($UPGRADEDIR);
    opendir(DIR, $UPGRADEDIR);
    @dir_list = readdir(DIR);
    close(DIR);
}

print "      (Processing upgrade scripts)\n";

print "\n$separator1\n";

foreach $file_name (@dir_list)
{

    if ( ! -d $file_name)
    {

        #
        # Make sure not to process the 'dbupgrade.pl' script again
        #
    
        if ($file_name ne "dbupgrade.pl")
        {

            #
            # Parse the filename by using the '.' as a delimiter 
            #
           
            @parsed_file_name = split /\./, $file_name;
    
            #
            # To get the file extension, simply reverse the parsed file name.
            # After reversing, the file extension should be in the first 
            # position of the array

            $file_ext = (reverse @parsed_file_name)[0];
       
            # Process the file according to its file extension
            
            # 
            # If no file extension was found, then don't run it.
            #
            if ( $#parsed_file_name == 0 )
            {
                if ($arg_showwarnings)
                {
                    print "Processing $file_name\n";
                    print "$separator2\n";
                    print "    WARNING! No file extension found: $file_name\n";
                    print "             Skipping File.\n";
                    print "\n$separator1\n";
                }
                next;
            }
            elsif ( $file_ext eq "sql" ) 
            {
                print "Processing $file_name\n";
                print "$separator2\n";
                system ("perl $MOCADIR/scripts/InstallSql.pl $file_name");
            }
            elsif ( $file_ext eq "pl" )
            {
                print "Processing $file_name\n";
                print "$separator2\n";
                system ("perl $file_name");
            }
            elsif ( $file_ext eq "msql")
            {
                print "Processing $file_name\n";
                print "$separator2\n";
                open (MSQL, "|$MOCADIR/bin/msql -S") || die "Unable to run SQL command: $!";
                print MSQL "\@$file_name\n";
                close MSQL;
            }
            elsif ( $file_ext eq "mload")
            {

                print "Processing $file_name\n";
                print "$separator2\n";

                #
                # read the mload parameters from the .mload file
                #
                $mload_cmd = "";
                open (PARMS, "<$file_name");
                while (<PARMS>)
                {
                   chomp;
                   ($mload_opt, $mload_opt_value) = split /=/, $_;
                   $mload_opt_value = ExpandEnvVars ($mload_opt_value);
                   $mload_cmd .= $mload_opt." ".$mload_opt_value." "
                }

                print "Processing as:   mload $mload_cmd\n";

                system ("mload $mload_cmd");
                close PARMS;

            }
            else 
            {
                if ($arg_showwarnings)
                {
                    print "Processing $file_name\n";
                    print "$separator2\n";
                    print "    WARNING! Unsupported file extension: (.$file_ext) $file_name\n";
                    print "             Skipping File.\n";
                    print "\n$separator1\n";
                }
                next;
            }

            if ($? != 0)
            {
                print "Error!  Errors encountered while running\n";
                print "          $file_name\n";
                print "Please check for severity!\n";
            }

            print "\n$separator1\n";

        }
    }
}

print "Done running migration for $UPGRADEDIR\n";

# Once next update is released, uncomment the following, and add the right path
system("perl $MOCADIR/db/upgrade/1.8.1/dbupgrade.pl");

exit 0;



