package com.sam.deal.api.stock;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

public enum eSTOCKTREND {
    UP,  //trend goes UP.
    EQUAL, //trend goes EQUAL.
    DOWN, //trend goes DOWN.
    CUP, //trend goes CURVL UP.
    CDOWN, //trend goes CURVL DOWN.
    NA  //unknown.
}
