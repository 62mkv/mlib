/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.sam.moca.components.base;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.exceptions.InvalidArgumentException;

public class BitArithmetic {
    
    /**
     * Sets a bit in a bitset.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param bit the bit position to set
     * @param bitset the bitset to change
     * @throws IllegalArgumentException if invalid bit
     */
    public MocaResults dumpBitset(MocaContext moca, int bitset) {

        EditableResults res = moca.newResults();
        res.addColumn("representation", MocaType.STRING);

        StringBuilder representation = new StringBuilder();
        int ii, bitmask;

        // Set the high order bit of the bitmask
        bitmask = 1 << 31;

        // Cycle through every bit in the bitset.
        for (ii = 1; ii <= 32; ii++) {

            // Get the value of this bit.
            // Add this bit's value to our result
            if ((bitset & bitmask) == 0) {
                representation.append('0');
            }
            else {
                representation.append('1');
            }

            // Put a spacer in after every eighth bit
            if ((ii % 8 == 0) && (ii != 32)) {
                representation.append(' ');
            }

            // Shift the bitset one left
            bitset <<= 1;
        }

        res.addRow();
        res.setValue("representation", representation.toString());

        return res;
    }

    /**
     * Sets a bit in a bitset.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param bit the bit position to set
     * @param bitset the bitset to change
     * @throws IllegalArgumentException if invalid bit
     */
    public MocaResults setBit(MocaContext moca, int bit, int bitset)
    	throws InvalidArgumentException {
    	
    	if (bit < 1 || bit > 32) {
    		throw new InvalidArgumentException("bit", "Bit number out of range");
    	}
    	
    	EditableResults res = moca.newResults();
    	res.addColumn("bit", MocaType.INTEGER);
    	res.addColumn("bitset", MocaType.INTEGER);
    	
    	// Set the bit in the bitmask by shifting the 1 bit 
    	// bit-1 positions to the left
    	int bitmask = 1 << (bit - 1);
    	
    	// Set the bit in the bitmask
    	bitset |= bitmask;
    	
    	res.addRow();
    	res.setValue("bit", bit);
    	res.setValue("bitset", bitset);
    	
    	return res;
    }

    /**
     * Clears a bit in a bitset.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param bit the bit position to set
     * @param bitset the bitset to change
     * @throws IllegalArgumentException if invalid bit
     */
    public MocaResults clearBit(MocaContext moca, int bit, int bitset)
    	throws InvalidArgumentException {
    	
    	if (bit < 1 || bit > 32) {
    		throw new InvalidArgumentException("bit", "Bit number out of range");
    	}
    	
    	EditableResults res = moca.newResults();
    	res.addColumn("bit", MocaType.INTEGER);
    	res.addColumn("bitset", MocaType.INTEGER);
    	
    	// Set the bit in the bitmask by shifting the 1 bit 
    	// bit-1 positions to the left
    	int bitmask = 1 << (bit - 1);
    	
    	// Set the bit in the bitmask
    	bitset ^= bitmask;
    	
    	res.addRow();
    	res.setValue("bit", bit);
    	res.setValue("bitset", bitset);
    	
    	return res;
    }

    /**
     * Sets a bit in a bitset.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param bit the bit position to set
     * @param bitset the bitset to change
     * @throws IllegalArgumentException if invalid bit
     */
    public MocaResults setBitsFromBitmask(MocaContext moca, int bitmask, int bitset) {
    	
    	EditableResults res = moca.newResults();
    	res.addColumn("bitmask", MocaType.INTEGER);
    	res.addColumn("bitset", MocaType.INTEGER);
    	
    	// Set the bit in the bitmask
    	bitset |= bitmask;
    	
    	res.addRow();
    	res.setValue("bitmask", bitmask);
    	res.setValue("bitset", bitset);
    	
    	return res;
    }

    /**
     * Creates a clear bitset.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     */
    public MocaResults clearAllBits(MocaContext moca) {
    	
    	EditableResults res = moca.newResults();
    	res.addColumn("bitset", MocaType.INTEGER);
    	
    	int bitset = 0;
    	
    	res.addRow();
    	res.setValue("bitset", bitset);
    	
    	return res;
    }

    /**
     * Creates a bitset with all bits set.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     */
    public MocaResults setAllBits(MocaContext moca) {
    	
    	EditableResults res = moca.newResults();
    	res.addColumn("bitset", MocaType.INTEGER);
    	
    	int bitset = ~0;
    	
    	res.addRow();
    	res.setValue("bitset", bitset);
    	
    	return res;
    }

    /**
     * Checks if a bit is set in a number.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param bit the bit position to set
     * @param bitset the bitset to change
     * @throws IllegalArgumentException if invalid bit
     */
    public MocaResults bitIsSet(MocaContext moca, int bit, int bitset)
    	throws InvalidArgumentException {
    	
    	if (bit < 1 || bit > 32) {
    		throw new InvalidArgumentException("bit", "Bit number out of range");
    	}
    	
    	EditableResults res = moca.newResults();
    	res.addColumn("bit", MocaType.INTEGER);
    	res.addColumn("bitset", MocaType.INTEGER);
    	res.addColumn("result", MocaType.INTEGER);
    	
    	// Set the bit in the bitmask by shifting the 1 bit 
    	// bit-1 positions to the left
    	int bitmask = 1 << (bit - 1);
    	
    	// Determine if the bit is set
    	int result = ((bitmask & bitset) == 0) ? 0 : 1;
    	
    	res.addRow();
    	res.setValue("bit", bit);
    	res.setValue("bitset", bitset);
    	res.setValue("result", result);
    	
    	return res;
    }

    /**
     * Checks if a bit is clear in a number.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param bit the bit position to set
     * @param bitset the bitset to change
     * @throws IllegalArgumentException if invalid bit
     */
    public MocaResults bitIsClear(MocaContext moca, int bit, int bitset)
    	throws InvalidArgumentException {
    	
    	if (bit < 1 || bit > 32) {
    		throw new InvalidArgumentException("bit", "Bit number out of range");
    	}
    	
    	EditableResults res = moca.newResults();
    	res.addColumn("bit", MocaType.INTEGER);
    	res.addColumn("bitset", MocaType.INTEGER);
    	res.addColumn("result", MocaType.INTEGER);
    	
    	// Set the bit in the bitmask by shifting the 1 bit 
    	// bit-1 positions to the left
    	int bitmask = 1 << (bit - 1);
    	
    	// Determine if the bit is set
    	int result = ((bitmask ^ bitset) == 0) ? 0 : 1;
    	
    	res.addRow();
    	res.setValue("bit", bit);
    	res.setValue("bitset", bitset);
    	res.setValue("result", result);
    	
    	return res;
    }
}
