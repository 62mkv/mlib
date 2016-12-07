/**
 * 
 */
package com.redprairie.moca.server.db.exceptions;

import com.redprairie.moca.exceptions.MocaDBException;

/**
 * @author wburns
 *
 */
public class MissingWhereClauseException extends MocaDBException {
    private static final long serialVersionUID = -2346680791985800960L;
    
    /**
     * This is the standard message that says a where clause must exist for
     * an update or delete statement.
     * @param code
     * @param message
     */
    public MissingWhereClauseException() {
        super(_CODE, _DEFAULTMSG);
    }

    private final static String _DEFAULTMSG = 
        "Update or Delete statement must include a where clause.";
    public final static int _CODE = 514;
}
