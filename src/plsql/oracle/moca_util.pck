create or replace package body moca_util as

    function date_diff_days(i_date_earlier in date, 
                            i_date_later   in date) return number 
    is
    begin
        return i_date_later - i_date_earlier;
    end date_diff_days;

    --
    -- We are saying that a trimmed string of NULL is the same as
    -- a NULL string -- important that SQLserver does the same!! 
    --
    function isnumeric(i_number in varchar2)        return number 
    is
         value number;
    begin

        if rtrim(i_number) is NULL or i_number is NULL then
	    return 0;
        end if;
  
        value := to_number(i_number);
  
        return 1;

        exception when others then
            return 0;

    end isnumeric;

end;
/
