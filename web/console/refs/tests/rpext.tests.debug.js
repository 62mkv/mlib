﻿/*
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */
//////////////////////
// ..\TU_String.js
//////////////////////
Ext.ns("RP.tests");

(function() {
  var assert = CSUnit.Assert;
  
  RP.tests.TU_String = new CSUnit.Test.Case({
    name: "TU_String",
    
    testContains: function() {
      var string = "The quick brown fox jumps over the lazy dog";
      
      assert.isTrue(string.contains("The"));
      assert.isTrue(string.contains("the"));
      assert.isFalse(string.contains("THe"));
      
      var html = '<div class="test">foo <span>bar</span></div>';
      
      assert.isTrue(html.contains("\"test\""));
      assert.isTrue(html.contains('"test"'));
      
      assert.isTrue(html.contains("class="));
      assert.isTrue(html.contains("<span>"));
    }
  });
})();

CSUnit.addTest(RP.tests.TU_String);
//////////////////////
// ..\collections\TU_Stack.js
//////////////////////
Ext.ns("RP.tests.rpext");


RP.tests.rpext.TestStack = new CSUnit.Test.Case({
  name: "Unit Test for RP.collections.Stack",
  
  testCount: function() {
    var s = new RP.collections.Stack();
    CSUnit.Assert.areEqual(0, s.count(), "Stack should initially be empty.");
    
    s.push(1, 2, 3);
    CSUnit.Assert.areEqual(3, s.count(), "Stack should be 3.");
    
    s.pop();
    CSUnit.Assert.areEqual(2, s.count(), "Stack should be 2.");
  },
  
  testPeek: function() {
    var s = new RP.collections.Stack();
    CSUnit.Assert.areEqual(null, s.peek(), "Peek from an empty stack should return null.");
    
    s.push("a");
    CSUnit.Assert.areEqual("a", s.peek(), "Peek should be 'a'.");
    
    s.push("b");
    CSUnit.Assert.areEqual("b", s.peek(), "Peek should be 'b'.");
  },
  
  testPeekn: function() {
    var s = this._stack;
    var peeks = s.peekn(4);
    
    CSUnit.Assert.isArray(peeks);
    CSUnit.Assert.areEqual(4, peeks.length, "Value of peeks.length should be 4.");
    CSUnit.Assert.areEqual("dcba", peeks.join(""), "Value of peeks.join() should be 'dcba'.");
  },
  
  testContains: function() {
    var s = this._stack;
    
    CSUnit.Assert.isTrue(s.contains("b"));
    CSUnit.Assert.isTrue(s.contains("c"));
    CSUnit.Assert.isTrue(s.contains("d"));
    CSUnit.Assert.isFalse(s.contains("e"));
  },
  
  testClear: function() {
    var s = this._stack;
    
    s.clear();
    CSUnit.Assert.areEqual(0, s.count());
  },
  
  testUntil: function() {
    var s = this._stack;
    
    CSUnit.Assert.areEqual(0, s.countUntil("d"));
    CSUnit.Assert.areEqual(1, s.countUntil("c"));
    CSUnit.Assert.areEqual(2, s.countUntil("b"));
    CSUnit.Assert.areEqual(3, s.countUntil("a"));
    CSUnit.Assert.areEqual(-1, s.countUntil("e"));
  },
  
  testCountUntil: function() {
    var s = this._stack;
    
    CSUnit.Assert.areEqual(2, s.countUntil("b"));
  },
  
  testCountAndPop: function() {
    var s = this._stack;
    
    CSUnit.Assert.areEqual(4, s.count());
    CSUnit.Assert.areEqual("d", s.pop());
    CSUnit.Assert.areEqual(3, s.count());
    CSUnit.Assert.areEqual("c", s.popn(1)[0]);
    CSUnit.Assert.areEqual(2, s.count());
    CSUnit.Assert.areEqual("ba", s.popn(2).join(""));
  },
  
  testPop: function() {
    var s = this._stack;
    
    CSUnit.Assert.areEqual("d", s.pop());
    CSUnit.Assert.areEqual("c", s.pop());
    CSUnit.Assert.areEqual("b", s.pop());
    CSUnit.Assert.areEqual("a", s.pop());
    CSUnit.Assert.isNull(s.pop());
  },
  
  testPopn: function() {
    var s = this._stack;
    
    var popped = s.popn(2);
    CSUnit.Assert.isArray(popped);
    CSUnit.Assert.areEqual(2, popped.length, "Value of popped.length should be 2.");
    CSUnit.Assert.areEqual("dc", popped.join(""), "Value of popped.join() should be 'dc'.");
    
    popped = s.popn(2);
    CSUnit.Assert.isArray(popped);
    CSUnit.Assert.areEqual(2, popped.length, "Value of popped.length should be 2.");
    CSUnit.Assert.areEqual("ba", popped.join(""), "Value of popped.join() should be 'ba'.");
  },
  
  testMixedStack: function() {
    var s = new RP.collections.Stack();
    
    s.push(1, "a", function() {}, [], {}, null, undefined, true, new Date());
    
    CSUnit.Assert.isInstanceOf(Date, s.pop());
    CSUnit.Assert.isBoolean(s.pop());
    CSUnit.Assert.isUndefined(s.pop());
    CSUnit.Assert.isNull(s.pop());
    CSUnit.Assert.isObject(s.pop());
    CSUnit.Assert.isArray(s.pop());
    CSUnit.Assert.isFunction(s.pop());
    CSUnit.Assert.isString(s.pop());
    CSUnit.Assert.isNumber(s.pop());
  },
  
  // private
  _stack: null,
  
  setUp: function() {
    this._stack = new RP.collections.Stack();
    this._stack.push("a");
    this._stack.push("b");
    this._stack.push("c", "d");
  },
  
  tearDown: function() {
    this._stack = null;
  }
});

CSUnit.addTest(RP.tests.rpext.TestStack);

//////////////////////
// ..\collections\TU_MixedCollection.js
//////////////////////
Ext.ns("RP.tests.rpext");


RP.tests.rpext.TestMixedCollection = new CSUnit.Test.Case({
  name: "Unit Test for RP.collections.MixedCollection",
  
  setUp: function() {
    this._mixedCollection = new RP.collections.MixedCollection({
      maxSize: 5
    });
    this._mixedCollection.add(1);
    this._mixedCollection.add(2);
    this._mixedCollection.add(3);
    this._mixedCollection.add(4);
    this._mixedCollection.add(5);
  },
  
  tearDown: function() {
    this._mixedCollection = null;
  },
  
  testmaxSize: function() {
    var mc = this._mixedCollection;
    var second = 2;
    var third = 3;
    var sixth = 6;
    var seventh = 7;
    
    mc.add(sixth);
    CSUnit.Assert.areEqual(5, mc.length, "Length should be 5.");
    CSUnit.Assert.areEqual(second, mc.first(), "First item should be }.");
    CSUnit.Assert.areEqual(sixth, mc.last(), "Last item should be 6.");
    
    mc.add(seventh);
    CSUnit.Assert.areEqual(5, mc.length, "Length should be 5.");
    CSUnit.Assert.areEqual(third, mc.first(), "First item should be 3.");
    CSUnit.Assert.areEqual(seventh, mc.last(), "Last item should be 7.");
  },
  
  testTrimAt: function() {
    var mc = this._mixedCollection;
    var fourth = 4;
    var second = 2;
    
    mc.trimAt(4);
    CSUnit.Assert.areEqual(4, mc.length, "Length should be 4.");
    CSUnit.Assert.areEqual(fourth, mc.last(), "Last item should be 4.");
    
    mc.trimAt(2);
    CSUnit.Assert.areEqual(2, mc.length, "Length should be 2.");
    CSUnit.Assert.areEqual(second, mc.last(), "last item should be 2.");
  }
});

CSUnit.addTest(RP.tests.rpext.TestMixedCollection);

//////////////////////
// ..\core\TU_FormatEngine.js
//////////////////////
Ext.ns("RP.tests.rpext");

RP.EUFormats = { Date: { Long:        { type: "date",
                                      order: "d,m,y",
                                      formatstring: "dd'/'MM'/'yyyy hh':'mm tt"},
                       Default:     { type: "date", 
                                          order:"d,m,y",
                                          formatstring:"dd'/'MM'/'yyyy"},
                       Medium:      { type: "date", 
                                          order:"d,m,y",
                                          formatstring:"dd'/'MM'/'yyyy"},
                       Short:       { type: "date", 
                                          order:"d,m,y",
                                          formatstring:"d'/'M'/'yy"},
                       MonthYear:       { type: "date", 
                                          order:"m,y",
                                          formatstring:"MMM yyyy"},
                       MonthDate:       { type: "date", 
                                          order:"d,m",
                                          formatstring:"dd'/'MM"},
                       Weekday:         { type: "date", 
                                          order:"d",
                                          formatstring:"dddd"},
                       TimeStamp:       { type: "date", 
                                          order:"d,m,y",
                                          formatstring:"dd'/'MM'/'yyyy hh':'mm':'ss tt"},
                       FullDateOnly:    { type: "date", 
                                          order:"d,m,y",
                                          formatstring:"dddd', 'dd MMMM', 'yyyy"},
                       LongYear:        { type: "date", 
                                          order:"y",
                                          formatstring:"yyyy"},
                       MonthDateDay:    { type: "date", 
                                          order:"d,m",
                                          formatstring:"dd'/'MM ddd"},
                       ShortWeekday:    { type: "date", 
                                          order:"d",
                                          formatstring:"ddd"},
                       FullDateTime:    { type: "date", 
                                          order:"d,m,y",
                                          formatstring:"dddd', 'dd MMMM', 'yyyy hh':'mm tt"},
                       POSDate:         { type: "date", 
                                          order:"y,m,d",
                                          formatstring:"yyyy'-'MM'-'dd'T'hh':'mm':'ss"}
                     },
               Time: { Default:     { type: "time",  
                                          formatstring: "hh':'mm tt"}, 
                       Military:    { type: "time", 
                                          formatstring:"HH':'mm"}
                     },
               Number: { Default:   { type:"number", 
                                            thousand:".", 
                                            dec:",", 
                                            scale:"2", 
                                            negsign:"()"},
                         HighPrecision:{ type:"number", 
                                            thousand:".", 
                                            dec:",", 
                                            scale:"4", 
                                            negsign:"()",
                                            trimzero:"n"},
                         MediumPrecision: { type:"number", 
                                            thousand:".", 
                                            dec:",", 
                                            scale:"2", 
                                            negsign:"()",
                                            trimzero:"n"},
                         LowPrecision: { type:"number", 
                                            thousand:".", 
                                            dec:",", 
                                            scale:"0", 
                                            negsign:"()",
                                            trimzero:"n"}
                     },
               Currency: { Default:         { type:"currency", 
                                              leadchar:"", 
                                              thousand:".", 
                                              dec:",", 
                                              scale:"2", 
                                              negsign:"()", 
                                              trailchar:"€",
                                              trimzero:"y"},
                           HighPrecision: { type:"currency", 
                                              leadchar:"", 
                                              thousand:".", 
                                              dec:",", 
                                              scale:"4", 
                                              negsign:"()", 
                                              trailchar:"€",
                                              trimzero:"n"},
                           MediumPrecision: { type:"currency", 
                                              leadchar:"", 
                                              thousand:".", 
                                              dec:",", 
                                              scale:"2", 
                                              negsign:"()", 
                                              trailchar:"€",
                                              trimzero:"n"},
                           LowPrecision: { type:"currency", 
                                              leadchar:"", 
                                              thousand:".", 
                                              dec:",", 
                                              scale:"0", 
                                              negsign:"()", 
                                              trailchar:"€",
                                              trimzero:"n"}
                     },
               Percent: { HighPrecision:  { type:"number", 
                                            leadchar:"%", 
                                            thousand:".", 
                                            dec:",", 
                                            scale:"4", 
                                            negsign:"()",
                                            trailchar:"",
                                            trimzero:"n"},
                         MediumPrecision: { type:"number", 
                                            leadchar:"%", 
                                            thousand:".", 
                                            dec:",", 
                                            scale:"2", 
                                            negsign:"()",
                                            trailchar:"",
                                            trimzero:"n"},
                         LowPrecision: { type:"number", 
                                            leadchar:"%", 
                                            thousand:".", 
                                            dec:",", 
                                            scale:"0", 
                                            negsign:"()",
                                            trailchar:"",
                                            trimzero:"n"}
                     }
};

RP.FRFormatConstants =  { Weekdays: ["dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam."],
                        FullWeekdays:["dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"],
                        Months:["janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc."],
                        FullMonths:["janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre"]
};


RP.FRFormat = RP.core.FormatEngine(RP.EUFormats, RP.FRFormatConstants);

RP.tests.rpext.TU_FormatEngine = new CSUnit.Test.Case({
  //name of the test case - if not provided, one is auto-generated
  name: "Unit Test for RP.core.FormatEngine",
  
  //---------------------------------------------------------------------
  // setUp and tearDown methods - optional
  //---------------------------------------------------------------------
  
  /*
   * Sets up data that is needed by each test.
   * @method
   */
  setUp: function() {
    
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
     
  },
  
  //---------------------------------------------------------------------
  // Test methods - names must begin with "test"
  //---------------------------------------------------------------------
  
  testBaseFormatDate: function()
  {
    // the format engine should be in this name 
    CSUnit.Assert.areNotEqual(null, RP.core.Format, "The Format Engine should be a global variable");
    CSUnit.Assert.areNotEqual(undefined, RP.core.Format, "The Format Engine should be a global variable");

    var someDate = new Date(2008, 0, 13, 0, 0, 0);

    var formattedDate = RP.core.Format.formatDate(someDate);
    CSUnit.Assert.areNotEqual("1/13/2008", formattedDate, "Expected format was not returned ");

    var parsedDate = RP.core.Format.parseDate(formattedDate);
    CSUnit.Assert.isTrue(someDate.equals(parsedDate), "A parse of a Format of Dates should be equal");

    someDate = new Date(2008, 0, 13, 7, 23, 15);

    formattedDate = RP.core.Format.formatDate(someDate, RP.core.Formats.Date.TimeStamp);
    CSUnit.Assert.areEqual("01/13/2008 07:23:15 am", formattedDate, "Expected format was not returned ");

    parsedDate = RP.core.Format.parseDateTime(formattedDate, RP.core.Formats.Date.TimeStamp);
    CSUnit.Assert.isTrue(someDate.equals(parsedDate), "A parse of a Format of Dates should be equal");

    var strDate = "1/13";
    var actual = new Date();
    actual.setMonth(0, 13);
    actual.setHours(0, 0, 0, 0);
    parsedDate = RP.core.Format.parseDate(strDate);
    CSUnit.Assert.datesAreEqual(actual, parsedDate);

    strDate = "2";
    var worked = false;
    try
    {
      parsedDate = RP.core.Format.parseDate(strDate);
    }
    catch (e)
    {
      worked = true;
    }
    CSUnit.Assert.isTrue(worked, "A parse of a Format of Dates should be equal");


  },

  testBaseFormatTime: function()
  {
    var strTime = "1:30 am";
    var actual = new Date();
    actual.setHours(1, 30, 0, 0);
    var parsedTime = RP.core.Format.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "6p";
    actual = new Date();
    actual.setHours(18, 0, 0, 0);
    parsedTime = RP.core.Format.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "6";
    actual = new Date();
    actual.setHours(6, 0, 0, 0);
    parsedTime = RP.core.Format.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "16";
    actual = new Date();
    actual.setHours(16, 0, 0, 0);
    parsedTime = RP.core.Format.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "6:70";
    var worked = false;
    try
    {
      parsedTime = RP.core.Format.parseTime(strTime);
    }
    catch (e)
    {
      worked = true;
    }
    CSUnit.Assert.isTrue(worked, "A parse of a Format of Dates should be equal");
  },

  testBaseFormatNumber: function()
  {
    var someNumber = -12.3456;

    var formattedNumber = RP.core.Format.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12.35)", formattedNumber, "Expected format was not returned ");

    var parsedNumber = RP.core.Format.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12.35 === parsedNumber, "A parse of a Format of numbers should be equal");

    someNumber = -12345.67;
    formattedNumber = RP.core.Format.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12,345.67)", formattedNumber, "Expected format was not returned ");
    parsedNumber = RP.core.Format.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12345.67 === parsedNumber, "A parse of a Format of numbers should be equal");
    
    someNumber = -12345;
    formattedNumber = RP.core.Format.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12,345.00)", formattedNumber, "Expected format was not returned ");
    parsedNumber = RP.core.Format.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12345.00 === parsedNumber, "A parse of a Format of numbers should be equal");

    someNumber = -12345678;
    formattedNumber = RP.core.Format.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12,345,678.00)", formattedNumber, "Expected format was not returned ");
    parsedNumber = RP.core.Format.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12345678.00 === parsedNumber, "A parse of a Format of numbers should be equal");
  },

  testBaseFormatCurrency: function()
  {
    var someNumber = -12.3456;

    var formattedNumber = RP.core.Format.formatCurrency(someNumber);
    CSUnit.Assert.areEqual("($12.35)", formattedNumber, "Expected format was not returned ");

    var parsedNumber = RP.core.Format.parseCurrency(formattedNumber);
    CSUnit.Assert.isTrue(-12.35 === parsedNumber, "A parse of a Format of numbers should be equal");

  },

  testFRBaseFormatDate: function testBaseFormatDate()
  {
    // the format engine should be in this name 
    CSUnit.Assert.areNotEqual(null, RP.FRFormat, "The Format Engine should be a global variable");
    CSUnit.Assert.areNotEqual(undefined, RP.FRFormat, "The Format Engine should be a global variable");

    var someDate = new Date(2008, 0, 13, 0, 0, 0);

    var formattedDate = RP.FRFormat.formatDate(someDate);
    CSUnit.Assert.areNotEqual("13/1/2008", formattedDate, "Expected format was not returned ");

    var parsedDate = RP.FRFormat.parseDate(formattedDate);
    CSUnit.Assert.isTrue(someDate.equals(parsedDate), "A parse of a Format of Dates should be equal");

    someDate = new Date(2008, 0, 13, 7, 23, 15);

    formattedDate = RP.FRFormat.formatDate(someDate, RP.EUFormats.Date.TimeStamp);
    CSUnit.Assert.areEqual("13/01/2008 07:23:15 am", formattedDate, "Expected format was not returned ");

    parsedDate = RP.FRFormat.parseDateTime(formattedDate, RP.EUFormats.Date.TimeStamp);
    CSUnit.Assert.isTrue(someDate.equals(parsedDate), "A parse of a Format of Dates should be equal");

    var strDate = "13/1";
    var actual = new Date();
    actual.setMonth(0, 13);
    actual.setHours(0, 0, 0, 0);
    parsedDate = RP.FRFormat.parseDate(strDate);
    CSUnit.Assert.datesAreEqual(actual, parsedDate);

    strDate = "2";
    var worked = false;
    try
    {
      parsedDate = RP.FRFormat.parseDate(strDate);
    }
    catch (e)
    {
      worked = true;
    }
    CSUnit.Assert.isTrue(worked, "A parse of a Format of Dates should be equal");


  },

  testFRBaseFormatTime: function()
  {
    var strTime = "1:30 am";
    var actual = new Date();
    actual.setHours(1, 30, 0, 0);
    var parsedTime = RP.FRFormat.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "6p";
    actual = new Date();
    actual.setHours(18, 0, 0, 0);
    parsedTime = RP.FRFormat.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "6";
    actual = new Date();
    actual.setHours(6, 0, 0, 0);
    parsedTime = RP.FRFormat.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "16";
    actual = new Date();
    actual.setHours(16, 0, 0, 0);
    parsedTime = RP.FRFormat.parseTime(strTime);
    CSUnit.Assert.isTrue(actual.equals(parsedTime), "A parse of a Format of Dates should be equal");

    strTime = "6:70";
    var worked = false;
    try
    {
      parsedTime = RP.FRFormat.parseTime(strTime);
    }
    catch (e)
    {
      worked = true;
    }
    CSUnit.Assert.isTrue(worked, "A parse of a Format of Dates should be equal");
  },

  testFRBaseFormatNumber: function()
  {
    var someNumber = -12.3456;

    var formattedNumber = RP.FRFormat.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12,35)", formattedNumber, "Expected format was not returned ");

    var parsedNumber = RP.FRFormat.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12.35 === parsedNumber, "A parse of a Format of numbers should be equal");
    
    someNumber = -12345.67;
    formattedNumber = RP.FRFormat.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12.345,67)", formattedNumber, "Expected format was not returned ");
    parsedNumber = RP.FRFormat.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12345.67 === parsedNumber, "A parse of a Format of numbers should be equal");
    
    someNumber = -12345;
    formattedNumber = RP.FRFormat.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12.345,00)", formattedNumber, "Expected format was not returned ");
    parsedNumber = RP.FRFormat.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12345.00 === parsedNumber, "A parse of a Format of numbers should be equal");

    someNumber = -12345678;
    formattedNumber = RP.FRFormat.formatNumber(someNumber);
    CSUnit.Assert.areEqual("(12.345.678,00)", formattedNumber, "Expected format was not returned ");
    parsedNumber = RP.FRFormat.parseNumber(formattedNumber);
    CSUnit.Assert.isTrue(-12345678.00 === parsedNumber, "A parse of a Format of numbers should be equal");
  },

  testFRBaseFormatCurrency: function()
  {
    var someNumber = -12.3456;

    var formattedNumber = RP.FRFormat.formatCurrency(someNumber);
    CSUnit.Assert.areEqual("(12,35€)", formattedNumber, "Expected format was not returned ");

    var parsedNumber = RP.FRFormat.parseCurrency(formattedNumber);
    CSUnit.Assert.isTrue(-12.35 === parsedNumber, "A parse of a Format of numbers should be equal");

  }
});

CSUnit.addTest(RP.tests.rpext.TU_FormatEngine);

//////////////////////
// ..\core\TU_Interface.js
//////////////////////
Ext.ns("RP.tests.rpext");

RP.tests.rpext.TU_Interface = new CSUnit.Test.Case({
  //name of the test case - if not provided, one is auto-generated
  name: "Unit Test for RP.core.Interface",
  
  //---------------------------------------------------------------------
  // setUp and tearDown methods - optional
  //---------------------------------------------------------------------
  
  /*
   * Sets up data that is needed by each test.
   * @method
   */
  setUp: function() {
    RP.tests.rpext.TU_Interface.ISample = {
      /**@private*/
      _name: "ISample",
      methA: Ext.emptyFn,
      methB: Ext.emptyFn
    };
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
    delete RP.tests.rpext.TU_Interface.ISample;
  },
  
  //---------------------------------------------------------------------
  // Test methods - names must begin with "test"
  //---------------------------------------------------------------------
  
  testGoodInterfaceImpl: function() {
    RP.tests.rpext.TU_Interface.MyClass = Ext.extend(Object, {
      methA: function() {
        return "methA";
      },
      
      methB: function() {
        return "methB";
      }
    });
    
    try {      
      RP.iimplement(RP.tests.rpext.TU_Interface.MyClass, RP.tests.rpext.TU_Interface.ISample);      
    }
    catch (e) {
      CSUnit.Assert.isTrue(false, "A good interface implementation failed but should have passed.");
    }
    
    var samp = new RP.tests.rpext.TU_Interface.MyClass();
    var iSample = RP.iget(samp, RP.tests.rpext.TU_Interface.ISample);
    CSUnit.Assert.areEqual("methB", iSample.methB(), "Interface method not invoked correctly");
  },
  
  testBadInterfaceImpl: function() {
    RP.tests.rpext.TU_Interface.MyClass = Ext.extend(Object, {
      methA: function() {
        return "methA";
      }
    });
    
    try {
      RP.iimplement(RP.tests.rpext.TU_Interface.MyClass, RP.tests.rpext.TU_Interface.ISample);
      CSUnit.Assert.isTrue(false, "A bad interface implementation passed but should have failed.");
    }
    catch (e) {    
    }
  },
  
  testInterfaceMapping: function() {
    RP.tests.rpext.TU_Interface.MyClass = Ext.extend(Object, {
      methA: function() {
        return "methA";
      },
      
      methB: function() {
        return "methB";
      },
      
      myMappedFn: function() {
        return "myMappedFn";
      }
    });
    
    try {
      RP.iimplement(RP.tests.rpext.TU_Interface.MyClass, RP.tests.rpext.TU_Interface.ISample, {
        "methB": "myMappedFn"
      });     
    }
    catch (e) {
      CSUnit.Assert.isTrue(false, "Mapped function in interface not found.");    
    }
    
    var samp = new RP.tests.rpext.TU_Interface.MyClass();
    var iSample = RP.iget(samp, RP.tests.rpext.TU_Interface.ISample);
    CSUnit.Assert.areEqual("myMappedFn", iSample.methB(), "Mapped function not called.");
  }
});

CSUnit.addTest(RP.tests.rpext.TU_Interface);

//////////////////////
// ..\core\TU_Intervals.js
//////////////////////
Ext.ns("RP.tests.rpext");


RP.tests.rpext.TU_Intervals = new CSUnit.Test.Case({
  //name of the test case - if not provided, one is auto-generated
  name: "Unit Test for RP.core.IntervalJN",
  
  //---------------------------------------------------------------------
  // setUp and tearDown methods - optional
  //---------------------------------------------------------------------
  
  /*
   * Sets up data that is needed by each test.
   * @method
   */
  setUp: function() {
  
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
  
  },
  
  //---------------------------------------------------------------------
  // Test methods - names must begin with "test"
  //---------------------------------------------------------------------
  
  testParseTimeInterval: function() {
    var today = new Date(2008, 2, 15);
    var startTime = new Date(2008, 2, 15, 23, 0, 0, 0);
    var endTime = new Date(2008, 2, 16, 13, 0, 0, 0);
    var interval = IntervalJN.parseTimeInterval("11p-1p", today);
    
    CSUnit.Assert.isTrue(interval.Start.equals(startTime), "Start Time not parsed correctly");
    CSUnit.Assert.isTrue(interval.End.equals(endTime), "End Time not parsed correctly");
    
    startTime = new Date(2008, 2, 15, 11, 0, 0, 0);
    endTime = new Date(2008, 2, 16, 1, 0, 0, 0);
    interval = IntervalJN.parseTimeInterval("11a-1a", today);
    
    CSUnit.Assert.isTrue(interval.Start.equals(startTime), "Start Time not parsed correctly");
    CSUnit.Assert.isTrue(interval.End.equals(endTime), "End Time not parsed correctly");
    
    startTime = new Date(2008, 2, 15, 11, 0, 0, 0);
    endTime = new Date(2008, 2, 15, 13, 0, 0, 0);
    interval = IntervalJN.parseTimeInterval("11a-1", today);
    
    CSUnit.Assert.isTrue(interval.Start.equals(startTime), "Start Time not parsed correctly");
    CSUnit.Assert.isTrue(interval.End.equals(endTime), "End Time not parsed correctly");
    
    startTime = new Date(2008, 2, 15, 11, 0, 0, 0);
    endTime = new Date(2008, 2, 16, 11, 0, 0, 0);
    interval = IntervalJN.parseTimeInterval("11a-11a", today);
    
    CSUnit.Assert.isTrue(interval.Start.equals(startTime), "Start Time not parsed correctly");
    CSUnit.Assert.isTrue(interval.End.equals(endTime), "End Time not parsed correctly");
  },
  
  testIntervalJN_isContinuous: function() {
  
    var today = new Date(2008, 2, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    var interval2 = IntervalJN.parseTimeInterval("1p-4", today);
    
    CSUnit.Assert.isTrue(IntervalJN.isContinuous(interval1, interval2), "fail to find continuous");
    CSUnit.Assert.isTrue(IntervalJN.isContinuous(interval2, interval1), "fail to find continuous");
    
    interval2 = IntervalJN.parseTimeInterval("1:01p-4", today);
    
    CSUnit.Assert.isFalse(IntervalJN.isContinuous(interval1, interval2), "fail to find non continuous");
    CSUnit.Assert.isFalse(IntervalJN.isContinuous(interval2, interval1), "fail to find non continuous");
    
  },
  
  
  testIntervalJN_isOverlap: function() {
  
    var today = new Date(2008, 5, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    var interval2 = IntervalJN.parseTimeInterval("1p-2:01", today);
    var interval3 = IntervalJN.parseTimeInterval("2p-4", today);
    var interval4 = IntervalJN.parseTimeInterval("10-5", today);
    
    CSUnit.Assert.isFalse(IntervalJN.isOverlapping(interval1, interval2), "[1] fail to find overlap");
    CSUnit.Assert.isTrue(IntervalJN.isOverlapping(interval2, interval3), "[2] fail to find non overlap");
    CSUnit.Assert.isTrue(IntervalJN.isOverlapping(interval1, interval4), "[3] fail to find overlap");
    CSUnit.Assert.isTrue(IntervalJN.isOverlapping(interval2, interval4), "[4] fail to find overlap");
    CSUnit.Assert.isTrue(IntervalJN.isOverlapping(interval3, interval4), "[5] fail to find overlap");
    
    
  },
  
  testIntervalJN_isCoincident: function() {
  
    var today = new Date(2008, 2, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    var interval2 = IntervalJN.parseTimeInterval("12p-1", today);
    var interval3 = IntervalJN.parseTimeInterval("12p-1:01", today);
    
    CSUnit.Assert.isTrue(IntervalJN.isCoincident(interval1, interval2), "fail to find Coincident");
    CSUnit.Assert.isFalse(IntervalJN.isCoincident(interval2, interval3), "fail to find non Coincident");    
  },
  
  testIntervalJN_getDuration: function() {
    var interval = {
      Start: new Date(2008, 6, 1, 2, 0, 0),
      End: new Date(2008, 6, 1, 3, 0, 0)
    };
    
    var duration = IntervalJN.getDuration(interval);
    
    CSUnit.Assert.isTrue((duration.totalMinutes() === 60), "IntervalJN getDuration did not return 60 minutes as expected");   
  },
  
  testIntervalJN_containsTime: function() {
    var interval = {
      Start: new Date(2008, 6, 1, 2, 0, 0),
      End: new Date(2008, 6, 1, 5, 0, 0)
    };
    var testDate = new Date(2008, 6, 1, 3, 30, 0);
    
    var contains = IntervalJN.containsTime(interval, testDate);
    
    CSUnit.Assert.isTrue((contains === true), "IntervalJN containsTime did not return 60 minutes as expected"); 
  }
});

CSUnit.addTest(RP.tests.rpext.TU_Intervals);

//////////////////////
// ..\core\TU_Sequence.js
//////////////////////
Ext.ns("RP.tests.rpext");

IntervalTestRule = {

  ruleType: "IntervalTestRule",
  
  split: function(intervalJN, splitAt) {
    var ret = {
      first: {
        Start: intervalJN.Start,
        End: splitAt
      },
      last: {
        Start: splitAt,
        End: intervalJN.End
      }
    };
    IntervalJN.setRule(ret.first, IntervalJN.getRule(intervalJN));
    IntervalJN.setRule(ret.last, IntervalJN.getRule(intervalJN));
    return ret;
  },
  
  
  merge: function(intervalJN1, intervalJN2) {
    return intervalJN1;
  },
  
  clone: function(interval) {
    return IntervalJN.cloneInterval(interval);
  }
};

SequenceTestRule = {

  ruleType: "SequenceTestRule",
  
  split: function(seqJN, splitAt) {
  
    var firstSeq = [];
    var lastSeq = [];
    
    Ext.each(seqJN, function(interval) {
    
      if (IntervalJN.containsInterval({
        Start: seqJN.Start,
        End: splitAt
      }, interval)) {
        SequenceJN.addInterval(firstSeq, interval, IntervalJN.getRule(interval));
      }
      else 
        if (IntervalJN.containsInterval({
          Start: splitAt,
          End: seqJN.End
        }, interval)) {
          SequenceJN.addInterval(lastSeq, interval, IntervalJN.getRule(interval));
        }
        else {
          var halves = IntervalJN.getRule(interval).split(interval, splitAt);
          SequenceJN.addInterval(firstSeq, halves.first, IntervalJN.getRule(interval));
          SequenceJN.addInterval(lastSeq, halves.last, IntervalJN.getRule(interval));
        }
      
    });
    
    
    
    return {
      first: firstSeq,
      last: lastSeq
    };
    
  },
  
  merge: function(intervalJN1, intervalJN2) {
    SequenceJN.addInterval(intervalJN1, intervalJN2, IntervalJN.getRule(intervalJN2));
    return intervalJN1;
  }
  
  
};


BreakRule = {

  split: function(intervalJN, splitAt) {
    return {
      first: {
        Start: intervalJN.Start,
        End: splitAt,
        PunchCode: intervalJN.PunchCode
      },
      last: {
        Start: splitAt,
        End: intervalJN.End,
        PunchCode: intervalJN.PunchCode
      }
    };
    
  },
  
  
  merge: function(intervalJN1, intervalJN2) {
    return intervalJN1;
  },
  
  canJoin: function(intervalJN1, intervalJN2) {
    return IntervalJN.compareDate(intervalJN1.End, intervalJN2.Start) === 0 &&
    intervalJN1.PunchCode === intervalJN2.PunchCode;
  },
  
  join: function(intervalJN1, intervalJN2) {
    intervalJN1.End = intervalJN2.End;
    return intervalJN1;
  }
  
};


RoleRule = {


  split: function(intervalJN, splitAt) {
    return {
      first: {
        Start: intervalJN.Start,
        End: splitAt,
        RoleID: intervalJN.RoleID,
        RoleName: intervalJN.RoleName,
        Status: intervalJN.Status !== "new" ? "modified" : "new"
      },
      last: {
        Start: splitAt,
        End: intervalJN.End,
        RoleID: intervalJN.RoleID,
        RoleName: intervalJN.RoleName,
        Status: "new"
      }
    };
    
  },
  
  merge: function(intervalJN1, intervalJN2) {
    return intervalJN1;
  }
  
  
};


RP.tests.rpext.TU_Sequence = new CSUnit.Test.Case({
  //name of the test case - if not provided, one is auto-generated
  name: "Unit Test for RP.core.SequenceJN",
  
  //---------------------------------------------------------------------
  // setUp and tearDown methods - optional
  //---------------------------------------------------------------------
  
  /*
   * Sets up data that is needed by each test.
   * @method
   */
  setUp: function() {
  
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
  
  },
  
  //---------------------------------------------------------------------
  // Test methods - names must begin with "test"
  //---------------------------------------------------------------------
  
  testSequenceJN: function() {
    var today = new Date(2008, 2, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    var interval2 = IntervalJN.parseTimeInterval("8-9:30", today);
    var interval3 = IntervalJN.parseTimeInterval("9-10:30", today);
    
    
    
    var sequence = [];
    SequenceJN.addInterval(sequence, interval1, IntervalTestRule);
    SequenceJN.addInterval(sequence, interval2, IntervalTestRule);
    SequenceJN.addInterval(sequence, interval3, IntervalTestRule);
    
    
    CSUnit.Assert.areEqual(4, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("8", today).getTime(), sequence[0].Start.getTime(), "wrong start time ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9", today).getTime(), sequence[0].End.getTime(), "wrong start time ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9", today).getTime(), sequence[1].Start.getTime(), "wrong start time ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), sequence[1].End.getTime(), "wrong start time ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), sequence[2].Start.getTime(), "wrong start time ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10:30", today).getTime(), sequence[2].End.getTime(), "wrong start time ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[3].Start.getTime(), "wrong start time ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[3].End.getTime(), "wrong start time ");
    
    
    var str = SequenceJN.formatSequence(sequence);
    
    
    // spliting Roles with mere Meals and Breaks
    var break1 = IntervalJN.parseTimeInterval("10-10:15", today);
    break1.PunchCode = "b";
    var break2 = IntervalJN.parseTimeInterval("12p-1", today);
    break2.PunchCode = "b";
    var break3 = IntervalJN.parseTimeInterval("3p- 3:15", today);
    break3.PunchCode = "b";
    
    
    var role1 = IntervalJN.parseTimeInterval("8-12:30", today);
    role1.RoleID = 123;
    role1.RoleName = "rocker";
    role1.Status = "original";
    
    var role2 = IntervalJN.parseTimeInterval("12:30p - 5", today);
    role2.RoleID = 456;
    role2.RoleName = "roller";
    role2.Status = "original";
    
    
    sequence = [];
    SequenceJN.addInterval(sequence, break1, BreakRule);
    SequenceJN.addInterval(sequence, break2, BreakRule);
    SequenceJN.addInterval(sequence, break3, BreakRule);
    SequenceJN.addInterval(sequence, role1, RoleRule);
    SequenceJN.addInterval(sequence, role2, RoleRule);
    
    var expect = [];
    expect.push("Start Date: 03/15/2008");
    expect.push("End Date: 03/15/2008");
    expect.push("[0] 08:00 am - 10:00 am");
    expect.push("[1] 10:00 am - 10:15 am");
    expect.push("[2] 10:15 am - 12:00 pm");
    expect.push("[3] 12:00 pm - 12:30 pm");
    expect.push("[4] 12:30 pm - 01:00 pm");
    expect.push("[5] 01:00 pm - 03:00 pm");
    expect.push("[6] 03:00 pm - 03:15 pm");
    expect.push("[7] 03:15 pm - 05:00 pm");
    
    CSUnit.Assert.areEqual(expect.join('\r\n'), SequenceJN.formatSequence(sequence), "incorrect Interval  ");
    
    expect = [];
    expect.push("Start Date: 03/15/2008");
    expect.push("End Date: 03/15/2008");
    expect.push("[0] 08:00 am - 10:00 am");
    expect.push("[1] 10:00 am - 10:15 am");
    expect.push("[2] 10:15 am - 12:00 pm");
    expect.push("[3] 12:00 pm - 01:00 pm");
    expect.push("[4] 01:00 pm - 03:00 pm");
    expect.push("[5] 03:00 pm - 03:15 pm");
    expect.push("[6] 03:15 pm - 05:00 pm");
    
    SequenceJN.coalesce(sequence);
    
    CSUnit.Assert.areEqual(expect.join('\r\n'), SequenceJN.formatSequence(sequence), "incorrect Interval  ");
    
    CSUnit.Assert.areEqual(123, sequence[0].RoleID, "incorrect Interval Data ");
    CSUnit.Assert.areEqual("modified", sequence[0].Status, "incorrect Interval Data ");
    CSUnit.Assert.areEqual(undefined, sequence[1].RoleID, "incorrect Interval Data ");
    CSUnit.Assert.areEqual(123, sequence[2].RoleID, "incorrect Interval Data ");
    CSUnit.Assert.areEqual("new", sequence[2].Status, "incorrect Interval Data ");
    CSUnit.Assert.areEqual(undefined, sequence[3].RoleID, "incorrect Interval Data ");
    CSUnit.Assert.areEqual(456, sequence[4].RoleID, "incorrect Interval Data ");
    CSUnit.Assert.areEqual("new", sequence[4].Status, "incorrect Interval Data ");
    CSUnit.Assert.areEqual(undefined, sequence[5].RoleID, "incorrect Interval Data ");
    CSUnit.Assert.areEqual(456, sequence[6].RoleID, "incorrect Interval Data ");
    CSUnit.Assert.areEqual("new", sequence[6].Status, "incorrect Interval Data ");
    
    
    
    
    
  },
  
  testNestedSequenceJN: function() {
  
    var today = new Date(2008, 2, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    
    var sequence = [];
    SequenceJN.addInterval(sequence, interval1, IntervalTestRule);
    
    var parentSequence = [];
    SequenceJN.addInterval(parentSequence, sequence, SequenceTestRule);
    
    var interval2 = IntervalJN.parseTimeInterval("10a-2", today);
    SequenceJN.addInterval(parentSequence, interval2, IntervalTestRule);
    
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(1, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10", today).getTime(), parentSequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), parentSequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), parentSequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), parentSequence[1].End.getTime(), "wrong end time interval2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), parentSequence[2].Start.getTime(), "wrong start time interval3");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("2p", today).getTime(), parentSequence[2].End.getTime(), "wrong end time interval3");
    
    
    var interval3 = IntervalJN.parseTimeInterval("10:00-12:30p", today);
    SequenceJN.addInterval(parentSequence, interval3, IntervalTestRule);
    
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(2, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12:30p", today).getTime(), sequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12:30p", today).getTime(), sequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[1].End.getTime(), "wrong end time interval2");
    
    var interval4 = IntervalJN.parseTimeInterval("10:00-12:30p", today);
    var sequence4 = [];
    SequenceJN.addInterval(sequence4, interval4, IntervalTestRule);
    SequenceJN.addInterval(parentSequence, sequence4, SequenceTestRule);
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(2, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12:30p", today).getTime(), sequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12:30p", today).getTime(), sequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[1].End.getTime(), "wrong end time interval2");
    
    
  },
  
  testBoundedSequenceJN: function() {
  
    var today = new Date(2008, 2, 15);
    
    var sequence = [];
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    SequenceJN.setBounds(sequence, interval1);
    
    var parentSequence = [];
    SequenceJN.addInterval(parentSequence, sequence, SequenceTestRule);
    
    var interval2 = IntervalJN.parseTimeInterval("10a-2", today);
    SequenceJN.addInterval(parentSequence, interval2, IntervalTestRule);
    
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(1, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10", today).getTime(), parentSequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), parentSequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), parentSequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), parentSequence[1].End.getTime(), "wrong end time interval2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), parentSequence[2].Start.getTime(), "wrong start time interval3");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("2p", today).getTime(), parentSequence[2].End.getTime(), "wrong end time interval3");
    
    var interval3 = IntervalJN.parseTimeInterval("12:15p-12:45p", today);
    SequenceJN.addInterval(parentSequence, interval3, IntervalTestRule);
    
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(3, sequence.length, "length was not correct ");
    
    //  now be abusive 
    var errThrown = false;
    try {
    
      //   sequence is a bounded sequence 12p - 1 so this should fail
      interval3 = IntervalJN.parseTimeInterval("11:15a-12:45p", today);
      SequenceJN.addInterval(sequence, interval3, IntervalTestRule);
      
      
    } 
    catch (e) {
      CSUnit.Assert.areEqual("interval is out of bounds for the bounded sequence", e.message, "unexpected error  thrown ");
      errThrown = true;
    }
    
    CSUnit.Assert.areEqual(true, errThrown, "expected error not thrown ");
    
  },
  
  testDisplacementSequenceJN: function() {
  
    var today = new Date(2008, 2, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    
    var sequence = [];
    SequenceJN.addInterval(sequence, interval1, IntervalTestRule);
    
    var parentSequence = [];
    SequenceJN.addInterval(parentSequence, sequence, SequenceTestRule);
    
    var interval2 = IntervalJN.parseTimeInterval("10a-2", today);
    SequenceJN.addInterval(parentSequence, interval2, IntervalTestRule);
    
    var interval3 = IntervalJN.parseTimeInterval("11a-12", today);
    SequenceJN.addInterval(sequence, interval3, IntervalTestRule);
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(2, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10", today).getTime(), parentSequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), parentSequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), parentSequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), parentSequence[1].End.getTime(), "wrong end time interval2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), parentSequence[2].Start.getTime(), "wrong start time interval3");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("2p", today).getTime(), parentSequence[2].End.getTime(), "wrong end time interval3");
    
    
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), sequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[1].End.getTime(), "wrong end time interval2");
    
    
    interval3 = IntervalJN.parseTimeInterval("12p-1:30", today);
    SequenceJN.addInterval(sequence, interval3, IntervalTestRule);
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(3, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10", today).getTime(), parentSequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), parentSequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), parentSequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1:30p", today).getTime(), parentSequence[1].End.getTime(), "wrong end time interval2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1:30p", today).getTime(), parentSequence[2].Start.getTime(), "wrong start time interval3");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("2p", today).getTime(), parentSequence[2].End.getTime(), "wrong end time interval3");
    
    
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), sequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[1].End.getTime(), "wrong end time interval2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[2].Start.getTime(), "wrong start time interval3 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1:30p", today).getTime(), sequence[2].End.getTime(), "wrong end time interval3");
  },
  
  testMoreDisplacementSequenceJN: function() {
  
    var today = new Date(2008, 2, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    
    var sequence = [];
    SequenceJN.addInterval(sequence, interval1, IntervalTestRule);
    
    var parentSequence = [];
    SequenceJN.addInterval(parentSequence, sequence, SequenceTestRule);
    
    var interval2 = IntervalJN.parseTimeInterval("10a-2", today);
    SequenceJN.addInterval(parentSequence, interval2, IntervalTestRule);
    
    var interval3 = IntervalJN.parseTimeInterval("11a-1:30", today);
    SequenceJN.addInterval(sequence, interval3, IntervalTestRule);
    
    CSUnit.Assert.areEqual(3, parentSequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(3, sequence.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10", today).getTime(), parentSequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), parentSequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), parentSequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1:30p", today).getTime(), parentSequence[1].End.getTime(), "wrong end time interval2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1:30p", today).getTime(), parentSequence[2].Start.getTime(), "wrong start time interval3");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("2p", today).getTime(), parentSequence[2].End.getTime(), "wrong end time interval3");
    
    
    CSUnit.Assert.areEqual(IntervalJN.parseTime("11a", today).getTime(), sequence[0].Start.getTime(), "wrong start time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[0].End.getTime(), "wrong end time interval1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), sequence[1].Start.getTime(), "wrong start time interval2 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[1].End.getTime(), "wrong end time interval2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), sequence[2].Start.getTime(), "wrong start time interval3 ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1:30p", today).getTime(), sequence[2].End.getTime(), "wrong end time interval3");
    
  },
  
  testGetSubSequenceJN: function() {
    var today = new Date(2008, 2, 15);
    var interval1 = IntervalJN.parseTimeInterval("12p-1", today);
    var interval2 = IntervalJN.parseTimeInterval("8-9:30", today);
    var interval3 = IntervalJN.parseTimeInterval("9-10:30", today);
    
    var sequence = [];
    SequenceJN.addInterval(sequence, interval1, IntervalTestRule);
    SequenceJN.addInterval(sequence, interval2, IntervalTestRule);
    SequenceJN.addInterval(sequence, interval3, IntervalTestRule);
    
    var seq = SequenceJN.getSubSequence(sequence, IntervalJN.parseTimeInterval("8-1", today));
    CSUnit.Assert.areEqual(4, seq.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("8", today).getTime(), seq[0].Start.getTime(), "wrong start time 0");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9", today).getTime(), seq[0].End.getTime(), "wrong end time 0");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9", today).getTime(), seq[1].Start.getTime(), "wrong start time 1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), seq[1].End.getTime(), "wrong end time 1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), seq[2].Start.getTime(), "wrong start time 2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10:30", today).getTime(), seq[2].End.getTime(), "wrong end time 2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), seq[3].Start.getTime(), "wrong start time 3");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("1p", today).getTime(), seq[3].End.getTime(), "wrong end time 3");
    
    seq = SequenceJN.getSubSequence(sequence, IntervalJN.parseTimeInterval("8:30-12:30", today));
    CSUnit.Assert.areEqual(4, seq.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("8:30", today).getTime(), seq[0].Start.getTime(), "wrong start time 0");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9", today).getTime(), seq[0].End.getTime(), "wrong end time 0");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9", today).getTime(), seq[1].Start.getTime(), "wrong start time 1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), seq[1].End.getTime(), "wrong end time 1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), seq[2].Start.getTime(), "wrong start time 2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10:30", today).getTime(), seq[2].End.getTime(), "wrong end time 2");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12p", today).getTime(), seq[3].Start.getTime(), "wrong start time 3");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("12:30p", today).getTime(), seq[3].End.getTime(), "wrong end time 3");
    
    seq = SequenceJN.getSubSequence(sequence, IntervalJN.parseTimeInterval("9:15-10:15", today));
    CSUnit.Assert.areEqual(2, seq.length, "length was not correct ");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:15", today).getTime(), seq[0].Start.getTime(), "wrong start time 0");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), seq[0].End.getTime(), "wrong end time 0");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("9:30", today).getTime(), seq[1].Start.getTime(), "wrong start time 1");
    CSUnit.Assert.areEqual(IntervalJN.parseTime("10:15", today).getTime(), seq[1].End.getTime(), "wrong end time 1");
    
  }
});

CSUnit.addTest(RP.tests.rpext.TU_Sequence);

//////////////////////
// ..\event\TU_AppEventProxy.js
//////////////////////
Ext.ns("RP.tests.rpext");

RP.tests.rpext.TU_AppEventProxy = new CSUnit.Test.Case({
  //name of the test case - if not provided, one is auto-generated
  name: "Unit Test for RP.event.AppEventProxy",
  
  //---------------------------------------------------------------------
  // Private members
  //---------------------------------------------------------------------
  alarm1: "alarm 1",
  alarm2: "alarm 2",
  buffer: "",
  alarmsCaught: "",
  
  //---------------------------------------------------------------------
  // setUp and tearDown methods - optional
  //---------------------------------------------------------------------
  
  /*
   * Sets up data that is needed by each test.
   * @method
   */
  setUp: function() {
    this.buffer = "";
    this.alarmsCaught = "";
    
    RP.event.AppEventProxy.registerListener(this, true);    
    
    this.cb = this._onEvent.createDelegate(this);
    
    RP.event.AppEventProxy.subscribe(this.alarm1, this, this.cb, true);
    RP.event.AppEventProxy.subscribe(this.alarm2, this, this.cb, false);
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
    RP.event.AppEventProxy.unregisterListener(this); 
  },
  
  //---------------------------------------------------------------------
  // Test methods - names must begin with "test"
  //---------------------------------------------------------------------
  
  /**
   * Test basic functionality during awake state
   * @method
   */
  testAwakeState: function() {
    var constants = {
      expectedEventArgument1: "JUNK1",
      expectedAlarmCaught: "1"
    };
    
    RP.event.AppEventProxy.setState(this, true);
    
    CSUnit.Assert.areEqual("", this.buffer, "Buffer should initially be empty");
    CSUnit.Assert.areEqual("", this.alarmsCaught, "Alarms caught buffer should initially be empty");
    
    RP.event.AppEventManager.fire(this.alarm1, constants.expectedEventArgument1);
    CSUnit.Assert.areEqual(constants.expectedAlarmCaught, this.alarmsCaught, "Uncaught event.");
    CSUnit.Assert.areEqual(constants.expectedEventArgument1, this.buffer, "Unexpected or no argument received.");
  },
  
  /**
   * Test basic functionality during asleep state
   * @method
   */
  testAsleepStateAlarm1: function() {
    var constants = {
      uncaughtArgument1: "brown",
      uncaughtArgument2: "dog",
      expectedEventArgument1: "JUNK1",
      expectedAlarmCaught: "1"
    };
    
    RP.event.AppEventProxy.setState(this, false);
    
    CSUnit.Assert.areEqual("", this.buffer, "Buffer should initially be empty");
    CSUnit.Assert.areEqual("", this.alarmsCaught, "Alarms caught buffer should initially be empty");
    
    // listener is asleep and additional event's triggered should only indicate
    // the last event as fired.
    RP.event.AppEventManager.fire(this.alarm1, constants.uncaughtArgument1);
    RP.event.AppEventManager.fire(this.alarm1, constants.uncaughtArgument2);
    RP.event.AppEventManager.fire(this.alarm1, constants.expectedEventArgument1);
    
    // wake up and see what events are caught.
    RP.event.AppEventProxy.setState(this, true);
  
    CSUnit.Assert.areEqual(constants.expectedAlarmCaught, this.alarmsCaught, "Uncaught event.");
    CSUnit.Assert.areEqual(constants.expectedEventArgument1, this.buffer, "Unexpected or no argument received.");
  },
  
  /**
   * Test basic functionality during asleep state
   * @method
   */
  testAsleepStateAlarm2: function() {
    var constants = {
      expectedEventArgument1: "brown",
      expectedEventArgument2: "dog",
      expectedEventArgument3: "jumped",
      expectedEventResult: "browndogjumped",
      expectedAlarmCaught: "222"
    };
    
    RP.event.AppEventProxy.setState(this, false);
    
    // initial state should be empty.
    CSUnit.Assert.areEqual("", this.buffer, "Buffer should initially be empty");
    CSUnit.Assert.areEqual("", this.alarmsCaught, "Alarms caught buffer should initially be empty");
    
    // listener is asleep and additional event's triggered should queue up
    // all events while in sleeping state.
    RP.event.AppEventManager.fire(this.alarm2, constants.expectedEventArgument1);
    RP.event.AppEventManager.fire(this.alarm2, constants.expectedEventArgument2);
    RP.event.AppEventManager.fire(this.alarm2, constants.expectedEventArgument3);
    
    // wake up and see what events are caught.
    RP.event.AppEventProxy.setState(this, true);
  
    CSUnit.Assert.areEqual(constants.expectedAlarmCaught, this.alarmsCaught, "Uncaught event.");
    CSUnit.Assert.areEqual(constants.expectedEventResult, this.buffer, "Unexpected or no argument received.");
  },
  
  testUnsubscribedEvent: function() {
    // Initial buffers should be empty.
    CSUnit.Assert.areEqual("", this.buffer, "Buffer should initially be empty");
    CSUnit.Assert.areEqual("", this.alarmsCaught, "Alarms caught buffer should initially be empty");
    
    // Fire some other event we're not subscribed to, and ensure we don't
    // catch it.
    RP.event.AppEventManager.fire("idontcareaboutthisevent", "foo");
    
    CSUnit.Assert.areEqual("", this.alarmsCaught, "Should not have caught the event");
    CSUnit.Assert.areEqual("", this.buffer, "Unsubscribed event was caught");    
  },
  
  /**
   * Comprehensive unit test of RP.event.AppEventProxy.  Test to make sure
   * expected behavior is observed after changing asleep/awake state, as
   * well as resubscribing to events.
   * @method
   */
  testComprehensive: function() {
    
    // We will generate events (changing the state of the listener between events)
    // and capture the event arguments (string phrases).  The event listener buffers 
    // up the event arguments and at the very end, we should have a buffer containing
    // the sentence "the quick brown fox jumped over the lazy dog".
    
    // This class is a listener of two events - alarm1 and alarm2.  "alarm1" is set 
    // up so that duplicates of it are discarded if this listener is in a "sleeping"
    // state.  "alarm2" is set up so that duplicates are kept.  This test will 
    // generate alarm1 events while in the sleeping state to make sure they are 
    // properly discarded, and that alarm2 events are kept.  The test breaks if the
    // buffer contains additional junk or missing words from alarm2, such as:
    // "the quick XXX brown XXX XXX jumped the lazy XXX dog"
    // where "XXX" are junk that should've been discarded and "over" is missing between
    // "jumped" and "the".
    
    CSUnit.Assert.areEqual("", this.buffer, "Buffer should initially be empty");
    CSUnit.Assert.areEqual("", this.alarmsCaught, "Alarms caught buffer should initially be empty");
    
    //
    // Now keep firing events to generate "the quick brown fox jumped over the lazy dog"...
    //
    
    var nextWord = "the ";
    var expectedBuffer = "";
    var expectedAlarmNumBuffer = "";
    
    RP.event.AppEventManager.fire(this.alarm1, nextWord);
    
    expectedAlarmNumBuffer += "1";
    expectedBuffer += nextWord;
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "wrong event name");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "alarm1 not caught during awake state");
    
    nextWord = "quick ";
    expectedAlarmNumBuffer += "2";
    expectedBuffer += nextWord;
    RP.event.AppEventManager.fire(this.alarm2, nextWord);
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "wrong event name");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "alarm2 not caught during awake state");
    
    // Go to sleep:
    // Duplicates of alarm1 should be thrown out (only last one is kept)
    // Duplicates of alarm2 should all be caught
    RP.event.AppEventProxy.setState(this, false);
    
    // Generate duplicate alarm1 events - only the last one should be kept
    // because this listener is in the sleeping state.
    nextWord = "brown ";
    expectedAlarmNumBuffer += "1";
    expectedBuffer += nextWord;
    RP.event.AppEventManager.fire(this.alarm1, "JUNK1");
    RP.event.AppEventManager.fire(this.alarm1, nextWord);
    
    // Generate duplicate alarm2 events - both should be kept because
    // it was registered to not remove duplicates during sleeping state.
    nextWord = "fox ";
    RP.event.AppEventManager.fire(this.alarm2, nextWord);
    expectedAlarmNumBuffer += "2";
    expectedBuffer += nextWord;
    
    nextWord = "jumped ";
    RP.event.AppEventManager.fire(this.alarm2, nextWord);
    expectedAlarmNumBuffer += "2";
    expectedBuffer += nextWord;
    
    // Now wake up - we should have the caught the last alarm1 event and
    // both alarm2 events.
    RP.event.AppEventProxy.setState(this, true);
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "Events not caught properly during sleeping state");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "Event arguments messed up during sleeping state");
    
    // Now go back to sleep, generate alarm1 and alarm2 events, but then
    // unsubscribe to alarm2 events.  This should cause alarm2 events to be
    // discarded when this listener wakes back up.
    nextWord = "over ";
    expectedAlarmNumBuffer += "1";
    expectedBuffer += nextWord;
    RP.event.AppEventProxy.setState(this, false);
    RP.event.AppEventManager.fire(this.alarm1, "JUNK2");
    RP.event.AppEventManager.fire(this.alarm1, nextWord);
    RP.event.AppEventManager.fire(this.alarm2, "JUNK3");
    RP.event.AppEventManager.fire(this.alarm2, "JUNK4"); 
    RP.event.AppEventProxy.unsubscribe(this.alarm2, this);  // JUNK3 and JUNK4 should be gone
    
    // Now wake up and verify unsubscribed events are not caught...
    RP.event.AppEventProxy.setState(this, true);
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "Unsubscribed events not handled properly or remove dups not functioning");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "Event arguments messed up during sleeping state");
    
    // Resubscribe back to alarm2 and make sure (a) old events are not caught, and
    // (b) we can still catch new events.  Also change it so that this time, alarm2
    // also discards duplicates during sleeping state.
    RP.event.AppEventProxy.subscribe(this.alarm2, this, this.cb, true);
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "Resubscribe should not have gotten old discarded events");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "Resubscribe got old discarded events");
    
    nextWord = "the ";
    expectedAlarmNumBuffer += "2";
    expectedBuffer += nextWord;
    RP.event.AppEventManager.fire(this.alarm2, nextWord);
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "Resubscribe did not catch new event");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "Resubscribe got wrong event args");
    
    nextWord = "lazy ";
    expectedAlarmNumBuffer += "2";
    expectedBuffer += nextWord;
    RP.event.AppEventManager.fire(this.alarm2, nextWord);
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "Resubscribe did not catch new event");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "Resubscribe got wrong event args");
    
    // Go back to sleep, and fire 2 alarm2 events and make sure this time,
    // duplicates of alarm2 are discarded.
    nextWord = "dog";
    expectedAlarmNumBuffer += "2";
    expectedBuffer += nextWord;
    RP.event.AppEventProxy.setState(this, false);
    RP.event.AppEventManager.fire(this.alarm2, "JUNK5");
    RP.event.AppEventManager.fire(this.alarm2, nextWord);
    
    // Now wake up.  Only the last alarm2 event should be caught...
    RP.event.AppEventProxy.setState(this, true);
    CSUnit.Assert.areEqual(expectedAlarmNumBuffer, this.alarmsCaught, "Resubscribe of alarm2 did not discard correctly");
    CSUnit.Assert.areEqual(expectedBuffer, this.buffer, "Resubscribe with discard got wrong event args");  
  },
  
  //---------------------------------------------------------------------
  // Private methods
  //---------------------------------------------------------------------
  _onEvent: function(evtName /* , vargs */, phrase) {
    this.buffer += phrase;
    
    switch(evtName)
    {
      case this.alarm1:
        this.alarmsCaught += "1";
        break;
        
      case this.alarm2:
        this.alarmsCaught += "2";
        break;
        
      default:
        CSUnit.Assert.isTrue(false, "Event name unrecognized");
        break;
    }
  }
});

CSUnit.addTest(RP.tests.rpext.TU_AppEventProxy);

//////////////////////
// ..\locale\TU_Dispatch.js
//////////////////////
Ext.ns("RP.tests.rpext");

RP.tests.rpext.TU_Dispatch = new CSUnit.Test.Case({
  //name of the test case - if not provided, one is auto-generated
  name: "Unit Test for RP.locale.Dispatch",
  
  helloStr: "Hello",
  goodbyeStr: "Good bye",
  
  //---------------------------------------------------------------------
  // setUp and tearDown methods - optional
  //---------------------------------------------------------------------
  
  /*
   * Sets up data that is needed by each test.
   * @method
   */
  setUp: function() {
    RP.locale.Dispatch.setMessages("RP.tests.rpext.TU_Dispatch", {
      hello: this.helloStr,
      goodbye: this.goodbyeStr
    });
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {  
  },
  
  //---------------------------------------------------------------------
  // Test methods - names must begin with "test"
  //---------------------------------------------------------------------
  
  testMessages: function() {
    CSUnit.Assert.areEqual(this.helloStr, RP.getMessage("RP.tests.rpext.TU_Dispatch.hello"), "Message failed");
    CSUnit.Assert.areEqual(this.goodbyeStr, RP.getMessage("RP.tests.rpext.TU_Dispatch.goodbye"), "Message failed");      
  }
});

CSUnit.addTest(RP.tests.rpext.TU_Dispatch);
//////////////////////
// ..\login\TU_Login.js
//////////////////////
Ext.ns("RP.tests.login");

RP.tests.login.TU_Login = new CSUnit.Test.Case({
  constants: {
    urlRoot: "http://host:port",
    moduleInUrl: "/rp/TEST#/",
    urlWithModule: "/rp/login?redirectUrl=/rp/TEST#/",
    urlWithNoRedirect: "/rp/login",
    urlWithEmptyRedirect: "/rp/login?redirectUrl=",
    urlWithRPRedirect: "/rp/login?redirectUrl=/rp",
    urlWithRPRedirectSlash: "/rp/login?redirectUrl=/rp/"
  },
  
  testNoRedirectUrl: function() {
    var url = this.constants.urlRoot + this.constants.urlWithNoRedirect;
    
    var redirectUrl = RP.login.LoginForm.prototype.onAuthenticatedHandler(url);

    CSUnit.Assert.isUndefined(redirectUrl, "The redirectUrl was unexpectedly specified.");
  },
  testEmptyRedirectUrl: function() {
    var url = this.constants.urlRoot + this.constants.urlWithEmptyRedirect;
    
    var redirectUrl = RP.login.LoginForm.prototype.onAuthenticatedHandler(url);

    CSUnit.Assert.isUndefined(redirectUrl, "The redirectUrl was unexpectedly specified.");
  },
  
  testRedirectUrlEndsWithRP: function() {
    var url = this.constants.urlRoot + this.constants.urlWithRPRedirect;
    
    var redirectUrl = RP.login.LoginForm.prototype.onAuthenticatedHandler(url);

    CSUnit.Assert.isUndefined(redirectUrl, "The redirectUrl was unexpectedly specified.");
  },
  
  testExpectedRedirectUrl: function() {
    var url = this.constants.urlRoot + this.constants.urlWithModule;
    
    var redirectUrl = RP.login.LoginForm.prototype.onAuthenticatedHandler(url);

    CSUnit.Assert.isNotUndefined(redirectUrl, "The redirectUrl was not specified as expected.");
    CSUnit.Assert.areEqual(this.constants.moduleInUrl, redirectUrl, "The redirectUrl did not match the passed url.");
  },
  
  testRedirectUrlEndsWithRPSlash: function() {
    var url = this.constants.urlRoot + this.constants.urlWithRPRedirectSlash;
    
    var redirectUrl = RP.login.LoginForm.prototype.onAuthenticatedHandler(url);

    CSUnit.Assert.isUndefined(redirectUrl, "The redirectUrl was unexpectedly specified.");
  }
});

CSUnit.addTest(RP.tests.login.TU_Login);
//////////////////////
// ..\login\TU_LoginNameAndPassword.js
//////////////////////
Ext.ns("RP.tests.login");

RP.tests.login.TU_LoginNameAndPassword = new CSUnit.Test.Case({
  setUp: function() {
    this.testPassword = "TESTPASSWORD";
    this.testUser = "TESTUSER";
    
    this.loginForm = new RP.login.LoginForm({
      securityServiceURL: '/bad/loginservice',
      changePasswordURLFormat: '/bad/changepasswordservice',
      afterRender: function() {
        RP.login.LoginForm.superclass.afterRender.call(this);
      }
    });
    
    this.count = 0;
    this.countFn = function(conn, options){
      this.count++;
    };
    Ext.Ajax.on("beforerequest", this.countFn, this);
  },

  testSubmitNoLoginNameAndPassword: function() {
    var formValues = Ext.getCmp("loginForm").getForm().getValues();
    CSUnit.Assert.areEqual("", formValues.password);
    CSUnit.Assert.areEqual("", formValues.userName);
    
    this.loginForm.submit();
    
    CSUnit.Assert.areEqual(0, this.count);
  },
  
  testSubmitNoPassword: function(){
    Ext.getCmp("userName").setValue(this.testUser);
    
    var formValues = Ext.getCmp("loginForm").getForm().getValues();
    CSUnit.Assert.areEqual("", formValues.password);
    CSUnit.Assert.areEqual(this.testUser, formValues.userName);
    
    this.loginForm.submit();
    CSUnit.Assert.areEqual(0, this.count);
  },
  
  testSubmitNoLoginName: function(){
    Ext.getCmp("password").setValue(this.testPassword);
    
    var formValues = Ext.getCmp("loginForm").getForm().getValues();
    CSUnit.Assert.areEqual(this.testPassword, formValues.password);
    CSUnit.Assert.areEqual("", formValues.userName);

    this.loginForm.submit();
    CSUnit.Assert.areEqual(0, this.count);
  },
  
  /**
   * testSubmitWithLoginNameAndPassword should not be executed to avoid requiring
   * a proper server configuration.  More importantly, this goes beyond the test of
   * the LoginForm as a unit.  A mock server response maybe warranted to appropriately
   * test the complete handling of the form submission and message display.
   */
  // testSubmitWithLoginNameAndPassword: function(){
  //   Ext.getCmp("password").setValue(this.testPassword);
  //   Ext.getCmp("userName").setValue(this.testUser);
  //   
  //   var formValues = Ext.getCmp("loginForm").getForm().getValues();
  //   CSUnit.Assert.areEqual(this.testPassword, formValues.password);
  //   CSUnit.Assert.areEqual(this.testUser, formValues.userName);
  // 
  //   this.loginForm.submit();
  //   CSUnit.Assert.areEqual(1, this.count);
  // },
  
  tearDown: function() {
    Ext.Ajax.un("beforerequest", this.countFn, this);
    this.loginForm.destroy();
  }
});

CSUnit.addTest(RP.tests.login.TU_LoginNameAndPassword);
//////////////////////
// ..\taskflow\TU_TaskflowFrame.js
//////////////////////
Ext.ns("RP.tests.taskflow");

RP.tests.taskflow.TU_TaskflowFrame = new CSUnit.Test.Case({
    
    name: "Tests for RP.taskflow.TaskflowFrame",
    
    expectedHash: "",

    /**
     * Test to check if the activateDefaultWidget boolean parameter is 
     * handled and passed through correctly.
     */
    testActivateTaskflowActivateDefaultWidget : function() {
        
        // Store off to put it back later
        var _activateTaskflow =  RP.taskflow.TaskflowFrame.prototype._activateTaskflow;
        var _getTaskflowContainer =  RP.taskflow.TaskflowFrame.prototype._getTaskflowContainer;
        
        // Override _getTaskflowContainer since we don't need it for this test.
        RP.taskflow.TaskflowFrame.prototype._getTaskflowContainer = Ext.emptyFn;
        
        // Test with true activateDefaultWidget
        RP.taskflow.TaskflowFrame.prototype._activateTaskflow = function(tfc, activateDefaultWidget, activatedFn) {
            CSUnit.Assert.areSame(true, activateDefaultWidget);
        };
        RP.taskflow.TaskflowFrame.prototype.activateTaskflow(null, true, null);
        
        // Test with false activateDefaultWidget
        RP.taskflow.TaskflowFrame.prototype._activateTaskflow = function(tfc, activateDefaultWidget, activatedFn) {
            CSUnit.Assert.areSame(false, activateDefaultWidget);
        };
        RP.taskflow.TaskflowFrame.prototype.activateTaskflow(null, false, null);
        
        // Test with null activateDefaultWidget
        RP.taskflow.TaskflowFrame.prototype._activateTaskflow = function(tfc, activateDefaultWidget, activatedFn) {
            CSUnit.Assert.areSame(true, activateDefaultWidget);
        };
        RP.taskflow.TaskflowFrame.prototype.activateTaskflow(null, null, null);
        
        // Test with false activateDefaultWidget
        RP.taskflow.TaskflowFrame.prototype._activateTaskflow = function(tfc, activateDefaultWidget, activatedFn) {
            CSUnit.Assert.areSame(true, activateDefaultWidget);
        };
        RP.taskflow.TaskflowFrame.prototype.activateTaskflow(null, undefined, null);

        // Put these back in case any other test needs them
        RP.taskflow.TaskflowFrame.prototype._activateTaskflow = _activateTaskflow;
        RP.taskflow.TaskflowFrame.prototype._getTaskflowContainer = _getTaskflowContainer;
        
        //Check to make sure the functions got put back properly.
        CSUnit.Assert.areSame(_activateTaskflow, RP.taskflow.TaskflowFrame.prototype._activateTaskflow);
        CSUnit.Assert.areSame(_getTaskflowContainer, RP.taskflow.TaskflowFrame.prototype._getTaskflowContainer);
    },
    
    /**
     * Test to make sure that the '#' is only sliced off if it is present when navigating to URL.
     */
    testNavigateToURLHashSlice: function() {
        
        // Store off the original _rpTaskflowsContainer
        var _rpTaskflowsContainer = RP.taskflow.TaskflowFrame.prototype._rpTaskflowsContainer;
        
        // Rig up getCount to return a non-zero number
        RP.taskflow.TaskflowFrame.prototype._rpTaskflowsContainer = {
            items: {
                getCount: function(){
                    return 1;
                }
            }
        };
        
        var assertFunction = function(hash) {
             this.expectedHash = hash;
        };
        
        Ext.History.on("change", assertFunction, this);
        
        /*
         * This is a big scary async mess with the history change listener and the fact that
         * the navigateToURLHash method has two defers inside it. We used the YUI wait 
         * functionality to fix this. Unfortunately we need to check this for two different
         * occations, so we need to "chain" the waits together, hence the nested wait calls. 
         */
        
        // Test with a hash mark present
        RP.taskflow.TaskflowFrame.prototype.navigateToURLHash("#DM.DailyActionPlan:dmdailyprodreport");
        this.wait(function() {
            CSUnit.Assert.areSame("DM.DailyActionPlan:dmdailyprodreport", this.expectedHash);
            
            // Test without a hash mark present
            RP.taskflow.TaskflowFrame.prototype.navigateToURLHash("DM.DailyActionPlan:dmdailyprodreport");
            this.wait(function(){
                CSUnit.Assert.areSame("DM.DailyActionPlan:dmdailyprodreport", this.expectedHash);
                
                // Clean up the history change event listener
                Ext.History.un("change", assertFunction, this);
                
                // Put the original _rpTaskflowsContainer back in case any other test needs them
                RP.taskflow.TaskflowFrame.prototype._rpTaskflowsContainer = _rpTaskflowsContainer;
                
                //Check to make sure the original _rpTaskflowsContainer object got put back properly.
                CSUnit.Assert.areSame(_rpTaskflowsContainer, RP.taskflow.TaskflowFrame.prototype._rpTaskflowsContainer);
            }, 1000);
        }, 1000);
        
        
    }

});

CSUnit.addTest(RP.tests.taskflow.TU_TaskflowFrame);
//////////////////////
// ..\taskflow\widgets\Assert.js
//////////////////////
/**
 * Applies additional static assertion methods which are used
 * for testing the widgets within this package.
 */
(function() {
  var assert = CSUnit.Assert;
  var states = RP.taskflow.widgets.WidgetWithState.prototype.widgetStates;
  
  Ext.apply(assert, {
    /**
     * Asserts that a widget is in the failed state.
     * @param {RP.taskflow.widgets.WidgetWithState} widget
     */
    widgetFailed: function(widget) {
      assert.isTrue(widget.isFailed());
      assert.areEqual("rp-widget-failed-item", widget.backgroundCls);
      assert.areEqual("rp-widget-failed", widget.imgCls);
      assert.areEqual(states.FAILED, widget.widgetState);
      
      if (widget.rendered) {
        assert.isNotUndefined(widget.vantageStatus);
        assert.isTrue(widget.el.hasClass("rp-widget-failed-item"));
        assert.isTrue(widget.header.child("img").hasClass("rp-widget-failed"));
      }
    },
    
    /**
     * Asserts that a widget is in the normal state.
     * @param {RP.taskflow.widgets.WidgetWithState} widget
     */
    widgetNormal: function(widget) {
      assert.isTrue(widget.isNormal());
      assert.areEqual("", widget.backgroundCls);
      assert.areEqual("", widget.imgCls);
      assert.areEqual(states.NORMAL, widget.widgetState);
      
      if (widget.rendered) {
        assert.isUndefined(widget.vantageStatus);
      }
    },
    
    /**
     * Asserts that a widget is in the successful state.
     * @param {RP.taskflow.widgets.WidgetWithState} widget
     */
    widgetSuccessful: function(widget) {
      assert.isTrue(widget.isSuccessful());
      assert.areEqual("rp-widget-done-item", widget.backgroundCls);
      assert.areEqual("rp-widget-checked", widget.imgCls);
      assert.areEqual(states.SUCCESSFUL, widget.widgetState);
      
      if (widget.rendered) {
        assert.isNotUndefined(widget.vantageStatus);
        assert.isTrue(widget.el.hasClass("rp-widget-done-item"));
        assert.isTrue(widget.header.child("img").hasClass("rp-widget-checked"));
      }
    },
    
    /**
     * Asserts that a widget is in the warning state.
     * @param {RP.taskflow.widgets.WidgetWithState} widget
     */
    widgetWarning: function(widget) {
      assert.isTrue(widget.isWarning());
      assert.areEqual("", widget.backgroundCls);
      assert.areEqual("rp-widget-warning", widget.imgCls);
      assert.areEqual(states.WARNING, widget.widgetState);
      
      if (widget.rendered) {
        assert.isNotUndefined(widget.vantageStatus);
        assert.isTrue(widget.header.child("img").hasClass("rp-widget-warning"));
      }
    },
    
    /**
     * Asserts that a widget is in the working state.
     * @param {RP.taskflow.widgets.WidgetWithState} widget
     */
    widgetWorking: function(widget) {
      assert.isTrue(widget.isWorking());
      assert.areEqual("", widget.backgroundCls);
      assert.areEqual("rp-widget-loading", widget.imgCls);
      assert.areEqual(states.WORKING, widget.widgetState);
      
      if (widget.rendered) {
        assert.isNotUndefined(widget.vantageStatus);
        assert.isTrue(widget.header.child("img").hasClass("rp-widget-loading"));
      }
    },
    
    /**
     * Checks to make sure the widget actually has the expected count.
     * @param {RP.taskflow.widgets.WidgetWithState} widget
     * @param {Number/String} expected
     */
    widgetHasCount: function(widget, expected) {
      assert.areEqual(expected, widget._count);
      assert.areEqual(expected, widget.getCount());
      
      if (widget.rendered) {
        assert.areEqual(expected || "", widget.header.child("span").dom.innerHTML);
      }
    }
  });
})();

//////////////////////
// ..\taskflow\widgets\TU_WidgetWithState.js
//////////////////////
Ext.ns("RP.tests.taskflow.widgets");

(function() {
  var assert = CSUnit.Assert;
  var widget;
  
  /**
   * Unit tests for RP.taskflow.widgets.WidgetWithState.
   */
  RP.tests.taskflow.widgets.TU_WidgetWithState = new CSUnit.Test.Case({
    name: "TU_WidgetWithState",
    
    setUp: function() {
      widget = new RP.taskflow.widgets.WidgetWithState();
    },
    
    tearDown: function() {
      widget.destroy();
    },
    
    testDefaultNormalState: function() {
      assert.widgetNormal(widget);
    },
    
    testShowNormal: function() {
      widget.render(document.body);
      assert.widgetNormal(widget);
    },
    
    testShowFailed: function() {
      widget.showFailed();
      
      assert.widgetFailed(widget);
      widget.render(document.body);
      assert.widgetFailed(widget);
    },
    
    testShowSuccessful: function() {
      widget.showSuccessful();
      
      assert.widgetSuccessful(widget);
      widget.render(document.body);
      assert.widgetSuccessful(widget);
    },
    
    testShowWarning: function() {
      widget.showWarning();
      
      assert.widgetWarning(widget);
      widget.render(document.body);
      assert.widgetWarning(widget);
    },
    
    testShowWorking: function() {
      widget.showWorking();
      
      assert.widgetWorking(widget);
      widget.render(document.body);
      assert.widgetWorking(widget);
    },
    
    testChangeStates: function() {
      widget.render(document.body);
      
      // test default normal state
      assert.widgetNormal(widget);
      
      // change to failed
      widget.showFailed();
      assert.widgetFailed(widget);
      
      // change to successful
      widget.showSuccessful();
      assert.widgetSuccessful(widget);
      
      // change to warning
      widget.showWarning();
      assert.widgetWarning(widget);
      
      // change to working
      widget.showWorking();
      assert.widgetWorking(widget);
      
      // change back to normal
      widget.showNormal();
      assert.widgetNormal(widget);
    },
    
    testChangingStateBeforeRender: function() {
      widget.showNormal();
      widget.showFailed();
      widget.showSuccessful();
      widget.showWarning();
      widget.showWorking();
      
      widget.render(document.body);
      
      assert.widgetWorking(widget);
    }
  });
})();

CSUnit.addTest(RP.tests.taskflow.widgets.TU_WidgetWithState);
//////////////////////
// ..\taskflow\widgets\TU_WidgetWithCount.js
//////////////////////
Ext.ns("RP.tests.taskflow.widgets");

(function() {
  var assert = CSUnit.Assert;
  var widget;
  
  /**
   * Unit tests for RP.taskflow.widgets.WidgetWithCount.
   */
  RP.tests.taskflow.widgets.TU_WidgetWithCount = new CSUnit.Test.Case({
    name: "TU_WidgetWithCount",
    
    setUp: function() {
      widget = new RP.taskflow.widgets.WidgetWithCount();
    },
    
    tearDown: function() {
      widget.destroy();
    },
    
    testDefaultUndefinedCount: function() {
      assert.widgetHasCount(widget, undefined);
      
      widget.render(document.body);
      
      assert.widgetHasCount(widget, undefined);
    },
    
    testDoneItem: function() {
      widget.displayCount(0);
      
      assert.widgetHasCount(widget, 0);
      
      widget.render(document.body);
      
      // not using assert.widgetHasCount because that will also test
      // for a span's innerHTML in the header, which won't exist because
      // the widget will have the checkmark image instead.
      assert.areEqual(0, widget._count);
      assert.areEqual(0, widget.getCount());
      assert.widgetSuccessful(widget);
    },
    
    testNormalCount: function() {
      widget.displayCount(10);
      
      assert.widgetHasCount(widget, 10);
      
      widget.render(document.body);
      
      assert.widgetHasCount(widget, 10);
    },
    
    testActualZero: function() {
      widget.render(document.body);
      widget.displayCount(0);
      
      assert.widgetSuccessful(widget);
    },
    
    testStringZero: function() {
      widget.render(document.body);
      widget.displayCount("0");
      
      assert.widgetHasCount(widget, "0");
      
      // checks to make sure the widget does not contain any of the checkmark styles
      assert.isFalse(widget.isSuccessful());
      assert.isNotUndefined(widget.vantageStatus);
      assert.isFalse(widget.el.hasClass("rp-widget-done-item"));
      assert.isNull(widget.el.child("img"));
      assert.areEqual(0, widget.header.child("span", true).innerHTML, "widget should contain the string '0'");
    },
    
    testNumberGreaterThan999: function() {
      widget.render(document.body);
      widget.displayCount(1000);
      
      assert.widgetHasCount(widget, 1000);
      assert.isTrue(widget.header.child("span").hasClass("rp-vantage-sum-small"));
    },
    
    testChangingState: function() {
      widget.render(document.body);
      widget.displayCount(0);
      widget.displayCount(30);
      
      // manual style checks to make sure the successful state
      // has properly cleared back into the normal state
      assert.isFalse(widget.isSuccessful());
      assert.isNotUndefined(widget.vantageStatus);
      assert.isFalse(widget.el.hasClass("rp-widget-done-item"));
      assert.isNull(widget.el.child("img"));
      assert.widgetHasCount(widget, 30);
      
      widget.displayCount(0);
      
      assert.widgetSuccessful(widget);
    },
    
    testStateDoesNotChangeAfterRender: function() {
      widget.showWorking();
      
      assert.widgetWorking(widget);
      widget.render(document.body);
      assert.widgetWorking(widget);
    },
    
    testBackwardsCompatibility: function() {
      // backwards compatibility is maintained by assigning the references of the
      // old, deprecrated methods to the new ones.
      var oldProto = RP.taskflow.BaseTaskflowWidgetWithCount.prototype;
      var newProto = RP.taskflow.widgets.WidgetWithState.prototype;
      
      assert.areSame(RP.taskflow.widgets.WidgetWithCount,
                     RP.taskflow.BaseTaskflowWidgetWithCount,
                     "BaseTaskflowWidgetWithCount is not pointing to RP.taskflow.widgets.WidgetWithCount");
      
      assert.isFunction(oldProto.renderCount, "BaseTaskflowWidgetWithCount's prototype is missing renderCount()");
      assert.isFunction(oldProto.writeLine, "BaseTaskflowWidgetWithCount's prototype is missing writeLine()");
      
      assert.areSame(newProto.displayCount, newProto.renderCount, "renderCount() is not pointing to displayCount()");
      assert.areSame(newProto.displayCount, newProto.writeLine, "writeLine() is not pointing to displayCount()");
    }
  });
})();

CSUnit.addTest(RP.tests.taskflow.widgets.TU_WidgetWithCount);
//////////////////////
// ..\ui\Schedule\Activities\TU_SimpleActivity.js
//////////////////////
Ext.ns("RP.tests.ui.Schedule.Activities");

RP.tests.ui.Schedule.Activities.TU_SimpleActivity = new CSUnit.Test.Case({
  constants: {
    FIELD: "TEST",
    START_DATE: "2010-01-01T02:00:00",
    STOP_DATE: "2010-01-01T03:00:00",
    MISSING_CONFIG_EXCEPTION: "A display field or renderer method is required for labels."
  },
  
  name: "Unit Test for RP.ui.Schedule.Activities.SimpleActivity",
  record: null,
  
  /**
   * Set up for tests to make sure the state of the
   * class is prepared for the next test properly.
   */
  setUp: function() {
    var DataRecord = Ext.data.Record.create({
      phantom: true,
      fields: [{
        name: "field"
      }, {
        name: 'startDate',
        type: 'date'
      }, {
        name: 'stopDate',
        type: 'date'
      }]
    });
    
    this.record = new DataRecord({
      field: this.constants.FIELD,
      startDate: new Date(this.constants.START_DATE),
      stopDate: new Date(this.constants.STOP_DATE)
    });
  },

  /**
   * Test straight instantation without arguments.  An exception
   * is expected when no arguments are passed.
   */
  testEmptyConfiguration: function() {
    try {
      var activity = new RP.ui.Schedule.Activities.SimpleActivity();
      CSUnit.Assert.fail("Exception was not generated for missing configuration options.");
    }
    catch(e) {
      CSUnit.Assert.isTrue(e.message.indexOf(this.constants.MISSING_CONFIG_EXCEPTION) > -1, "Exception thrown was not expected.(" + e.message + ")");
    }
  },
  
  /**
   * Test creation of the object through ComponentMgr by xtype.  An exception
   * is expected when no arguments are passed.
   */
  testEmptyConfigurationByXtype: function() {
    var config = {
      xtype: "rpuischedulesimpleactivity"
    };
    try {
      var activity = Ext.ComponentMgr.create(config);
      CSUnit.Assert.fail("Exception was not generated for missing configuration options.");
    }
    catch(e) {
      CSUnit.Assert.isTrue(e.message.indexOf(this.constants.MISSING_CONFIG_EXCEPTION) > -1, "Exception thrown was not expected. (" + e.message + ")");
    }
  },
  
  /**
   * Test the getRecord method to ensure that a copy of the record is returned
   * rather than the original from the store.
   */
  testGetRecord: function() {
    var config = {
      displayField: "field",
      record: this.record
    };
    
    var activity = new RP.ui.Schedule.Activities.SimpleActivity(config);
    var copyOfRecord = activity.getRecord();
    CSUnit.Assert.areNotSame(this.record.id, copyOfRecord.id, "Copy of the activity did not generate a new id.");
  },
  
  /**
   * Test setting and getting the start date
   */
  testGetStartDate: function() {
    var config = {
      displayField: "field",
      record: this.record,
      startDateField: "startDate"
    };
    
    var activity = new RP.ui.Schedule.Activities.SimpleActivity(config);
    var startDate = activity.getStartDate();
    
    CSUnit.Assert.isInstanceOf(Date, startDate, "Start date was not a date.");
    CSUnit.Assert.areSame(new Date(this.constants.START_DATE).toString(), startDate.toString(), "The start date was not same as the one passed in.");
  },
  
  /**
   * Test setting and getting the stop date
   */
   testGetStopDate: function() {
    var config = {
      displayField: "field",
      record: this.record,
      stopDateField: "stopDate"
    };
    
    var activity = new RP.ui.Schedule.Activities.SimpleActivity(config);
    var stopDate = activity.getStopDate();
    
    CSUnit.Assert.isInstanceOf(Date, stopDate, "Stop date was not a date.");
    CSUnit.Assert.areSame(new Date(this.constants.STOP_DATE).toString(), stopDate.toString(), "The stop date was not same as the one passed in.");
  },
  
  /**
   * Test setting and getting the display field
   */
  testGetDisplayField: function() {
    var config = {
      displayField: "field",
      record: this.record
    };
    
    var activity = new RP.ui.Schedule.Activities.SimpleActivity(config);
    var displayField = activity.getDisplayField();
    
    CSUnit.Assert.isString(displayField, "Display field was not a string");
    CSUnit.Assert.areEqual(config.displayField, displayField, "config.displayField and displayField were not the same");
  },
  
  /**
   * Test to make sure the renderer method is defined and called when 
   * update of activity is triggered.
   */
  testActivityRenderer: function() {
    var calledRenderer = false;
    
    var config = {
      displayField: "field",
      renderer: function(){
        calledRenderer = true;
      },
      record: this.record
    };
    
    var activity = new RP.ui.Schedule.Activities.SimpleActivity(config);
    activity.updateActivity();
    
    CSUnit.Assert.isTrue(calledRenderer, "Failed to call the renderer function.");
  },
  
  
  /**
   * Test the activity renderer to make sure it uses the displayField even when
   * the renderer is defined but not a function.
   */
  testActivityRendererWithDisplayField: function() {
    var calledRenderer = false;
    
    var config = {
      xtype: "rpuischedulesimpleactivity",
      displayField: "field",
      renderer: "undefined",
      record: this.record,
      renderTo: Ext.getBody()
    };
    
    var activity = new RP.ui.Schedule.Activities.SimpleActivity(config);
    activity.doLayout();
    var currentHtml = activity.getLayoutTarget().dom.innerHTML;
    
    var newFieldValue = "UPDATED";
    this.record.set(config.displayField, newFieldValue);

    activity.updateActivity();
    var newHtml = activity.getLayoutTarget().dom.innerHTML;
    
    CSUnit.Assert.areNotSame(currentHtml, newHtml, "The content of the activity did not change from its original value.");
    CSUnit.Assert.areSame(newFieldValue, newHtml, "The content was not the same as the intended value. ");
  }
});

CSUnit.addTest(RP.tests.ui.Schedule.Activities.TU_SimpleActivity);
//////////////////////
// ..\ui\Schedule\Labels\TU_SimpleLabel.js
//////////////////////
Ext.ns("RP.tests.ui.Schedule.Labels");

RP.tests.ui.Schedule.Labels.TU_SimpleLabel = new CSUnit.Test.Case({
  constants: {
    FIELD: "TEST",
    MISSING_CONFIG_EXCEPTION: "A display field or renderer method is required for labels."
  },
  
  name: "Unit Test for RP.ui.Schedule.Labels.SimpleLabel",
  record: null,
  
  /**
   * Set up for tests to make sure the state of the
   * class is prepared for the next test properly.
   */
  setUp: function() {
    var DataRecord = Ext.data.Record.create({
      phantom: true,
      fields: [{
        name: "field"
      }]
    });
    
    this.record = new DataRecord({
      field: this.constants.FIELD
    });
  },

  /**
   * Test straight instantation without arguments.  An exception
   * is expected when no arguments are passed.
   */
  testEmptyConfiguration: function() {
    try {
      var label = new RP.ui.Schedule.Labels.SimpleLabel();
      CSUnit.Assert.fail("Exception was not generated for missing configuration options.");
    }
    catch(e) {
      CSUnit.Assert.isTrue(e.message.indexOf(this.constants.MISSING_CONFIG_EXCEPTION) > -1, "Exception thrown was not expected.(" + e.message + ")");
    }
  },
  
  /**
   * Test creation of the object through ComponentMgr by xtype.  An exception
   * is expected when no arguments are passed.
   */
  testEmptyConfigurationByXtype: function() {
    var config = {
      xtype: "rpuischedulesimplelabel"
    };
    try {
      var label = Ext.ComponentMgr.create(config);
      CSUnit.Assert.fail("Exception was not generated for missing configuration options.");
    }
    catch(e) {
      CSUnit.Assert.isTrue(e.message.indexOf(this.constants.MISSING_CONFIG_EXCEPTION) > -1, "Exception thrown was not expected. (" + e.message + ")");
    }
  },
  
  /**
   * Test the getRecord method to ensure that a copy of the record is returned
   * rather than the original from the store.
   */
  testGetRecord: function() {
    var config = {
      displayField: "field",
      record: this.record
    };
    
    var label = new RP.ui.Schedule.Labels.SimpleLabel(config);
    var copyOfRecord = label.getRecord();
    CSUnit.Assert.areNotSame(this.record.id, copyOfRecord.id, "Copy of the activity did not generate a new id.");
  },
  
  /**
   * Test setting and getting the display field
   */
  testGetDisplayField: function() {
    var config = {
      displayField: "field",
      record: this.record
    };
    
    var label = new RP.ui.Schedule.Labels.SimpleLabel(config);
    var displayField = label.getDisplayField();
    
    CSUnit.Assert.isString(displayField, "Display field was not a string");
    CSUnit.Assert.areEqual(config.displayField, displayField, "config.displayField and displayField were not the same");
  },
  
  /**
   * Test to make sure the renderer method is defined and called when 
   * update of activity is triggered.
   */
  testActivityRenderer: function() {
    var calledRenderer = false;
    
    var config = {
      displayField: "field",
      renderer: function(){
        calledRenderer = true;
      },
      record: this.record
    };
    
    var label = new RP.ui.Schedule.Labels.SimpleLabel(config);
    label.updateActivity();
    
    CSUnit.Assert.isTrue(calledRenderer, "Failed to call the renderer function.");
  },
  
  
  /**
   * Test the activity renderer to make sure it uses the displayField even when
   * the renderer is defined but not a function.
   */
  testActivityRendererWithDisplayField: function() {
    var calledRenderer = false;
    
    var config = {
      xtype: "rpuischedulesimplelabel",
      displayField: "field",
      renderer: "undefined",
      record: this.record,
      renderTo: Ext.getBody()
    };
    
    var label = new RP.ui.Schedule.Labels.SimpleLabel(config);
    label.doLayout();
    
    var currentValue = label.get(0).getValue();
    
    var newFieldValue = "UPDATED";
    this.record.set(config.displayField, newFieldValue);

    label.updateActivity();
    var newValue = label.get(0).getValue();
    
    CSUnit.Assert.areNotSame(currentValue, newValue, "The content of the activity did not change from its original value.");
    CSUnit.Assert.areSame(newFieldValue, newValue, "The content was not the same as the intended value. ");
  }
});

CSUnit.addTest(RP.tests.ui.Schedule.Labels.TU_SimpleLabel);
//////////////////////
// ..\upb\TU_ChangeSiteWindow.js
//////////////////////
Ext.ns("RP.tests.upb");

(function() {
    var assert = CSUnit.Assert;
    var changeSiteWindow;
    
    // extension of ChangeSiteWindow to be used for testing
    var TestChangeSiteWindow = Ext.extend(RP.upb.ChangeSiteWindow, {
        createSiteStore: function() {
            return new Ext.data.ArrayStore({
                fields: ["siteId", "siteName"],
                data: [["Bruce", "Wayne"], ["Foo", "Bar"], ["Chuck", "Norris"]]
            });
        },
        
        // focus natively does a defer which causes timing issues between the setup/tear down
        focus: Ext.emptyFn
    });
    
    /**
     * Unit tests for RP.upb.ChangeSiteWindow.
     */
    RP.tests.upb.TU_ChangeSiteWindow = new CSUnit.Test.Case({
        name: "TU_ChangeSiteWindow",
        
        setUp: function() {
            this.oldSiteID = RP.globals.SITEID;
            RP.globals.SITEID = "Bruce";
            
            changeSiteWindow = new TestChangeSiteWindow();
        },
        
        tearDown: function() {
            RP.globals.SITEID = this.oldSiteID;
            
            if (!changeSiteWindow.isDestroyed) {
                changeSiteWindow.close();
            }
        },
        
        testCloseAction: function() {
            var expectedAction = "close";
            
            changeSiteWindow.show();
            assert.areSame(expectedAction, changeSiteWindow.closeAction);
            assert.areSame(expectedAction, TestChangeSiteWindow.prototype.closeAction);
        },
        
        testDefaultValues: function() {
            changeSiteWindow.show();
            assert.areSame("Bruce", changeSiteWindow.siteComboBox.getValue());
            assert.areSame("Wayne", changeSiteWindow.siteComboBox.getRawValue());
        },
        
        testChangeSite: function() {
            changeSiteWindow.show();
            
            changeSiteWindow.changeSite = function(siteId, newWindow) {
                assert.areSame("Bruce", siteId);
                assert.isFalse(newWindow);
            };
            changeSiteWindow.onSelect();
            
            changeSiteWindow.siteComboBox.setValue("Foo");
            changeSiteWindow.getComponent("form").getComponent("chkNewWindow").setValue(true);
            
            changeSiteWindow.changeSite = function(siteId, newWindow) {
                assert.areSame("Foo", siteId);
                assert.isTrue(newWindow);
            };
            changeSiteWindow.onSelect();
            
            changeSiteWindow.siteComboBox.setValue("Chuck");
            changeSiteWindow.getComponent("form").getComponent("chkNewWindow").setValue(false);
            
            changeSiteWindow.changeSite = function(siteId, newWindow) {
                assert.areSame("Chuck", siteId);
                assert.isFalse(newWindow);
            };
            changeSiteWindow.onSelect();
        },
        
        testOnSpecialKeyEnter: function() {
            changeSiteWindow.show();
            
            var called = false;
            
            changeSiteWindow.onSelect = function() {
                called = true;
            };
            
            var fakeEvent = {
                getKey: function() {
                    return 8675309;
                },
                ENTER: 8675309,
                stopEvent: Ext.emptyFn
            };
            
            changeSiteWindow.onSpecialKey(null, fakeEvent);
            
            assert.isTrue(called);
        },
        
        testOnSpecialKeyNonEnter: function() {
            changeSiteWindow.show();
            
            changeSiteWindow.onSelect = function() {
                assert.fail("onSelect should not have been called from non ENTER key event.");
            };
            
            var fakeEvent = {
                getKey: function() {
                    return 9000;
                },
                ENTER: 8675309,
                stopEvent: Ext.emptyFn
            };
            
            changeSiteWindow.onSpecialKey(null, fakeEvent);
        },
        
        testCancel: function() {
            changeSiteWindow.show();
            
            assert.areSame(changeSiteWindow.close, changeSiteWindow.getComponent("form").getFooterBar().getComponent("btnCancel").handler);
            
            changeSiteWindow.close();
            
            assert.isTrue(changeSiteWindow.isDestroyed);
        }
    });
})();

CSUnit.addTest(RP.tests.upb.TU_ChangeSiteWindow);

//////////////////////
// ..\upb\TU_HeaderPanel.js
//////////////////////
Ext.ns("RP.tests.upb");

RP.tests.upb.TU_HeaderPanel = new CSUnit.Test.Case({
  noSiteObject: {"sites":[]},
  oneSiteObject: {"sites":[["1017911","ESS 01 - Day Cut"]]},
  multipleSiteObject: {"sites":[["RPWEB1","RPWEB1"],["RPWEB2","RPWEB2"],["RPWEB3","RPWEB3"],["RPWEB4","RPWEB4"],["RPWEB5","RPWEB5"],["RPWEB6","RPWEB6"]]},
  
  testNoSites: function() {
    RP.core.ApplicationSites.setApps(null, this.noSiteObject);
    CSUnit.Assert.isNull(RP.upb.HeaderPanel.prototype.getSiteComponent(),
                         "The site component should be null with no sites.");
  },
  
  testOneSite: function() {
    RP.core.ApplicationSites.setApps(null, this.oneSiteObject);
    CSUnit.Assert.isInstanceOf(Ext.form.Label, RP.upb.HeaderPanel.prototype.getSiteComponent(),
                         "The site component should be an Ext.form.Label with one site.");
  },
  
  testMultipleSites: function() {
    RP.core.ApplicationSites.setApps(null, this.multipleSiteObject);
    CSUnit.Assert.isInstanceOf(RP.ui.Hyperlink, RP.upb.HeaderPanel.prototype.getSiteComponent(),
                         "The site component should be an RP.ui.Hyperlink with multiple sites.");
  }
});

CSUnit.addTest(RP.tests.upb.TU_HeaderPanel);
//////////////////////
// ..\util\TU_DataStore.js
//////////////////////
Ext.ns("RP.tests.util");

RP.tests.util.TU_DataStore = new CSUnit.Test.Case({
  name: "Unit Test for RP.util.DataStore",
  
  setUp: function() {
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
  },
  
  
  /**
   * Test the 'join' function
   */
  testJoin: function() {
    var englishStore = new Ext.data.ArrayStore({
      fields: [{
        name: "number",
        type: "int"
      }, {
        name: "word"
      }],
      data: [[0, "zero"], [1, "one"]]
    });
    
    var spanishStore = new Ext.data.ArrayStore({
      fields: [{
        name: "numero",
        type: "int"
      }, {
        name: "wordo"
      }],
      data: [[1, "uno"], [2, "dos"]]
    });
    
    var joinedStore = new Ext.data.ArrayStore({
      fields: [{
        name: "number",
        type: "int"
      }, {
        name: "englishSpelling"
      }, {
        name: "spanishSpelling"
      }]
    });
    
    RP.util.DataStore.join([ // source store #1
    {
      store: englishStore,
      pk: "number",
      map: [{
        src: "word",
        dest: "englishSpelling"
      }]
    }, // source store #2
    {
      store: spanishStore,
      pk: "numero",
      map: [{
        src: "wordo",
        dest: "spanishSpelling"
      }]
    }], { // destination store
      store: joinedStore,
      pk: "number"
    });
    
    CSUnit.Assert.areEqual(3, joinedStore.getCount(), "Did not result in correct # of rows joined");
    CSUnit.Assert.areEqual("zero", joinedStore.getAt(0).get("englishSpelling"), "Joined wrong column or row out of order");
    CSUnit.Assert.areEqual(undefined, joinedStore.getAt(0).get("spanishSpelling"), "Joined wrong column or row out of order");
    CSUnit.Assert.areEqual("one", joinedStore.getAt(1).get("englishSpelling"), "Joined wrong column or row out of order");
    CSUnit.Assert.areEqual("uno", joinedStore.getAt(1).get("spanishSpelling"), "Joined wrong column or row out of order");
    CSUnit.Assert.areEqual(undefined, joinedStore.getAt(2).get("englishSpelling"), "Joined wrong column or row out of order");
    CSUnit.Assert.areEqual("dos", joinedStore.getAt(2).get("spanishSpelling"), "Joined wrong column or row out of order");
  },
  
  /**
   * Test the 'transpose' function
   */
  testTranspose: function() {
    var startDate = new Date("1/1/2010 0:00:00");
    
    var srcStore = new Ext.data.ArrayStore({
      fields: [{
        name: "Date",
        type: "date"
      }, {
        name: "Sales",
        type: "float"
      }, {
        name: "Cost",
        type: "float"
      }],
      data: [
        [startDate.clone(), 100000, 65000],
        [startDate.clone().addDays(1), 105000, 67000],
        [startDate.clone().addDays(2), 110000, 69000],
        [startDate.clone().addDays(3), 98000, 63000],
        [startDate.clone().addDays(4), 118000, 70000],
        [startDate.clone().addDays(5), 112000, 65000],
        [startDate.clone().addDays(6), 125000, 71000]
      ]
    });

    var destStore = new Ext.data.ArrayStore({
      fields: [{
        name: "Metric"
      }, {
        name: "Day1",
        type: "float"
      }, {
        name: "Day2",
        type: "float"
      }, {
        name: "Day3",
        type: "float"
      }, {
        name: "Day4",
        type: "float"
      }, {
        name: "Day5",
        type: "float"
      }, {
        name: "Day6",
        type: "float"
      }, {
        name: "Day7",
        type: "float"
      }, {
        name: "Total",
        type: "float"
      }]
    });
    
    RP.util.DataStore.transpose({
      source: {
        store: srcStore,
        pivotColumn: "Date",
        pivotMap: function(value) {
          var span = new TimeSpan(value - startDate);
          return ("Day" + (span.totalDays() + 1).toString());
        },
        transforms: [{ // divides by 1,000
          target: "Sales",
          fn: function(recData) {
            return recData.Sales / 1000;
          }
        }, { // divides by 1,000
          target: "Cost",
          fn: function(recData) {
            return recData.Cost / 1000;
          }
        }]
      },
      dest: {
        store: destStore,
        pivotColumn: "Metric",
        pivots: [{
          s: "Sales",
          d: "Sales (k)"
        }, // optional renderer and metadata can be specified
        {
          s: "Cost",
          d: "Cost (k)"
        }],
        transforms: [{ // sums the specified columns
          target: "Total",
          fn: "sum", // or function(recData, transform, metaData) {}
          columns: ["Day1", "Day2", "Day3", "Day4", "Day5", "Day6", "Day7"]
        }]
      }
    });
    
    CSUnit.Assert.areEqual(2, destStore.getCount(), "Transpose did not result in correct # of rows");
    
    var rec = destStore.getAt(0);
    CSUnit.Assert.areEqual("Sales (k)", rec.get("Metric"), "Metric column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(100, rec.get("Day1"), "Day1 column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(105, rec.get("Day2"), "Day2 column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(110, rec.get("Day3"), "Day3 column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(98, rec.get("Day4"), "Day4 column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(118, rec.get("Day5"), "Day5 column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(112, rec.get("Day6"), "Day6 column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(125, rec.get("Day7"), "Day7 column at row 0 transposed incorrectly");
    CSUnit.Assert.areEqual(768, rec.get("Total"), "Total column at row 0 transposed incorrectly");
    
    rec = destStore.getAt(1);
    CSUnit.Assert.areEqual("Cost (k)", rec.get("Metric"), "Metric column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(65, rec.get("Day1"), "Day1 column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(67, rec.get("Day2"), "Day2 column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(69, rec.get("Day3"), "Day3 column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(63, rec.get("Day4"), "Day4 column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(70, rec.get("Day5"), "Day5 column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(65, rec.get("Day6"), "Day6 column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(71, rec.get("Day7"), "Day7 column at row 1 transposed incorrectly");
    CSUnit.Assert.areEqual(470, rec.get("Total"), "Total column at row 1 transposed incorrectly");
  }
});

CSUnit.addTest(RP.tests.util.TU_DataStore);

//////////////////////
// ..\util\TU_Dates.js
//////////////////////
Ext.ns("RP.tests.util");

(function() {
   var assert = CSUnit.Assert;
   
   var EXPECTED_MILLI_DATE_ZERO = new Date("11/07/2010 1:15:00 CDT");
   var EXPECTED_MILLI_DATE_POS = new Date(new Date("11/07/2010 1:15:00 CDT").getTime() + 500);
   var EXPECTED_MILLI_DATE_NEG = new Date(new Date("11/07/2010 1:15:00 CDT").getTime() - 1500);
   
   var EXPECTED_SECONDS_DATE_ZERO =  new Date("11/07/2010 1:15:00 CDT");
   var EXPECTED_SECONDS_DATE_POS =  new Date("11/07/2010 1:16:30 CDT");
   var EXPECTED_SECONDS_DATE_NEG =  new Date("11/07/2010 1:14:30 CDT");
   
   var EXPECTED_MINUTES_DATE_ZERO  = new Date("11/07/2010 1:00:30 CDT");
   var EXPECTED_MINUTES_DATE_POS  = new Date("11/07/2010 1:10:30 CDT");
   var EXPECTED_MINUTES_DATE_NEG  = new Date("11/07/2010 00:50:30 CDT");
   
   var EXPECTED_MINUTES_DATE_ROLLOVER  = new Date("11/07/2010 1:05:30 CST");
   
  RP.tests.util.TU_Dates = new CSUnit.Test.Case({
    name: "Unit Test for RP.util.Dates",
    
    setUp: function() {
    },
    
    tearDown: function() {
    },
    
    
    /**
     * Test the setMilliseconds function
     */
    testSetMilliseconds: function() {
      
      // Test with setting zero milliseconds
      var date = new Date(new Date("11/07/2010 1:15:00 CDT").getTime() + 150);
      RP.util.Dates.setMilliseconds(date, 0);
      assert.datesAreEqual(EXPECTED_MILLI_DATE_ZERO, date);
      assert.areSame(EXPECTED_MILLI_DATE_ZERO.getTimezoneOffset(), date.getTimezoneOffset());
      // 
      date = new Date("11/07/2010 1:15:00 CDT");
      RP.util.Dates.setMilliseconds(date, 500);
      assert.datesAreEqual(EXPECTED_MILLI_DATE_POS, date);
      assert.areSame(EXPECTED_MILLI_DATE_POS.getTimezoneOffset(), date.getTimezoneOffset());
      
      // Test with setting a negative value.
      date = new Date("11/07/2010 1:15:00 CDT");
      RP.util.Dates.setMilliseconds(date, -1500);
      assert.datesAreEqual(EXPECTED_MILLI_DATE_NEG, date);
      assert.areSame(EXPECTED_MILLI_DATE_NEG.getTimezoneOffset(), date.getTimezoneOffset());
      
    },
    
        /**
     * Test the setSeconds function
     */
    testSetSeconds: function() {
      
      // Test with setting zero milliseconds
      var date = new Date("11/07/2010 1:15:30 CDT");
      RP.util.Dates.setSeconds(date, 0);
      assert.datesAreEqual(EXPECTED_SECONDS_DATE_ZERO, date);
      assert.areSame(EXPECTED_SECONDS_DATE_ZERO.getTimezoneOffset(), date.getTimezoneOffset());
      
      // Test with a positive value.
      date = new Date("11/07/2010 1:15:00 CDT");
      RP.util.Dates.setSeconds(date, 90);
      assert.datesAreEqual(EXPECTED_SECONDS_DATE_POS, date);
      assert.areSame(EXPECTED_SECONDS_DATE_POS.getTimezoneOffset(), date.getTimezoneOffset());
      
      // Test with setting a negative value.
      date = new Date("11/07/2010 1:15:00 CDT");
      RP.util.Dates.setSeconds(date, -30);
      assert.datesAreEqual(EXPECTED_SECONDS_DATE_NEG, date);
      assert.areSame(EXPECTED_SECONDS_DATE_NEG.getTimezoneOffset(), date.getTimezoneOffset());
      
    },
    
        /**
     * Test the setMinutes function
     */
    testSetMinutes: function() {
      
      // Test with setting zero milliseconds
      var date = new Date("11/07/2010 1:15:30 CDT");
      RP.util.Dates.setMinutes(date, 0);
      assert.datesAreEqual(EXPECTED_MINUTES_DATE_ZERO, date);
      assert.areSame(EXPECTED_MINUTES_DATE_ZERO.getTimezoneOffset(), date.getTimezoneOffset());
      
      // 
      date = new Date("11/07/2010 1:15:30 CDT");
      RP.util.Dates.setMinutes(date, 10);
      assert.datesAreEqual(EXPECTED_MINUTES_DATE_POS, date);
      assert.areSame(EXPECTED_MINUTES_DATE_POS.getTimezoneOffset(), date.getTimezoneOffset());
      
      // Test with setting a negative value.
      date = new Date("11/07/2010 1:15:30 CDT");
      RP.util.Dates.setMinutes(date, -10);
      assert.datesAreEqual(EXPECTED_MINUTES_DATE_NEG, date);
      assert.areSame(EXPECTED_MINUTES_DATE_NEG.getTimezoneOffset(), date.getTimezoneOffset());
      
    },
    
    testDaylightRollOver: function() {
      var date  = new Date("11/07/2010 1:15:30 CDT");
      RP.util.Dates.setMinutes(date, 65);
      assert.datesAreEqual(EXPECTED_MINUTES_DATE_ROLLOVER, date);
      assert.areSame(EXPECTED_MINUTES_DATE_ROLLOVER.getTimezoneOffset(), date.getTimezoneOffset());
      
    }
  });
  
})();

CSUnit.addTest(RP.tests.util.TU_Dates);

//////////////////////
// ..\util\TU_DateExt.js
//////////////////////
Ext.ns("RP.tests.util");

(function() {
   var assert = CSUnit.Assert;
   
   // All dates are testing round with daylight savings time specifically.
   var EXPECTED_ROUND = new Date("11/07/2010 1:30:00 CDT");
   var EXPECTED_ROUND_PARAM =  new Date("11/07/2010 1:10:00 CDT");
   var EXPECTED_ROUND_NEAREST_MINUTE  = new Date("11/07/2010 1:15:00 CDT");
   var EXPECTED_ROUND_NEAREST_SECONDS  = new Date("11/07/2010 1:15:30 CDT");
   var EXPECTED_ROUND_NEAREST_SECONDS_PARAM  = new Date("11/07/2010 1:15:40 CDT");
   var EXPECTED_ROUND_BACK_TO_HOUR  = new Date("11/07/2010 1:00:00 CDT");
   
  RP.tests.util.TU_DateExt = new CSUnit.Test.Case({
    name: "Unit Test for DateExt",
    
    setUp: function() {
    },
    
    tearDown: function() {
    },
    
    
    /**
     * Test the Date.prototype.round function
     */
    testRound: function() {
      date = new Date("11/07/2010 1:25:00 CDT");
      date.round();
      assert.datesAreEqual(EXPECTED_ROUND, date);
      assert.areSame(EXPECTED_ROUND.getTimezoneOffset(), date.getTimezoneOffset());
      
    },
    
    /**
     * Test the Date.prototype.round function with parameter passed in
     */
    testRoundWithParam: function() {
      
      var date = new Date("11/07/2010 1:08:00 CDT");
      date.round(10);
      assert.datesAreEqual(EXPECTED_ROUND_PARAM, date);
      assert.areSame(EXPECTED_ROUND_PARAM.getTimezoneOffset(), date.getTimezoneOffset());
    },
    
    /**
     * Test the Date.prototype.roundToNearestMinute function
     */
    testRoundToNearestMinute: function() {
      
      var date = new Date("11/07/2010 1:15:15 CDT");
      date.roundToNearestMinute();
      assert.datesAreEqual(EXPECTED_ROUND_NEAREST_MINUTE, date);
      assert.areSame(EXPECTED_ROUND_NEAREST_MINUTE.getTimezoneOffset(), date.getTimezoneOffset());
      
    },
    
    /**
     * Test the Date.prototype.roundToNearestSeconds function
     */
    testRoundToNearestSeconds: function() {
      var date  = new Date("11/07/2010 1:15:25 CDT");
      date.roundToNearestSeconds();
      assert.datesAreEqual(EXPECTED_ROUND_NEAREST_SECONDS, date);
      assert.areSame(EXPECTED_ROUND_NEAREST_SECONDS.getTimezoneOffset(), date.getTimezoneOffset());
      
    },
    
        /**
     * Test the Date.prototype.roundToNearestSeconds function with parameter passed in
     */
    testRoundToNearestSecondsWithParam: function() {
      var date  = new Date("11/07/2010 1:15:37 CDT");
      date.roundToNearestSeconds(10);
      assert.datesAreEqual(EXPECTED_ROUND_NEAREST_SECONDS_PARAM, date);
      assert.areSame(EXPECTED_ROUND_NEAREST_SECONDS_PARAM.getTimezoneOffset(), date.getTimezoneOffset());
      
    },
    
    /**
     * Test the Date.prototype.roundBackToHour function
     */
    testRoundBackToHour: function() {
      var date  = new Date("11/07/2010 1:15:30 CDT");
      date.roundBackToHour();
      assert.datesAreEqual(EXPECTED_ROUND_BACK_TO_HOUR, date);
      assert.areSame(EXPECTED_ROUND_BACK_TO_HOUR.getTimezoneOffset(), date.getTimezoneOffset());
      
    }
  });
  
})();

CSUnit.addTest(RP.tests.util.TU_DateExt);

//////////////////////
// ..\util\TU_DateOverride.js
//////////////////////
Ext.ns("RP.tests.util");

RP.tests.util.TU_DateOverride = new CSUnit.Test.Case({
  name: "Unit Test for RP.util.DateOverride",
  
  /**
   * Test the date object to ensure proper type. 
   */
  testInstanceofOperator: function() {
    var test = {
        input: new Date()
    };
    
    var expected = {
        instance: Date
    };
    
    CSUnit.Assert.isInstanceOf(expected.instance, test.input);
    CSUnit.Assert.isTrue(Ext.isDate(test.input), "ExtJS not considering the test.input object as a date.");
    CSUnit.Assert.isTrue(test.input instanceof expected.instance, "Expected a date instance, but did not return as a date.");
  },
  
  /**
   * 
   */
  testISO8601: function() {
    var test = {
        isoString: "2010-01-01T01:00:00"
    };
    
    var expected = {
        isoDate: new Date(2010, 0, 1, 1, 0, 0),
        typeCheck: typeof new Date()
    };
    
    var testDate = new Date(test.isoString);
    CSUnit.Assert.areEqual(expected.typeCheck, typeof testDate, "Type check failed, new date is not type Date.");
    CSUnit.Assert.areEqual(expected.isoDate.toString(), testDate.toString(), "String conversion from ISO 8601 failed.");
  },
  
  /**
   * 
   */
  testDateString: function() {
    var test = {
        dateString: "Wed May 26 2010 15:16:23"
    };
    
    var expected = {
        date: new Date(2010, 4, 26, 15, 16, 23),
        typeCheck: typeof new Date()
    };
    
    
    var testDate = new Date(test.dateString);
    CSUnit.Assert.areEqual(expected.typeCheck, typeof testDate, "Type check failed, new date is not type Date.");
    CSUnit.Assert.areEqual(expected.date.toString(), testDate.toString(), "String conversion from a Date String (" + test.dateString + ") failed.");
  },
  
  /**
   * Test with date parameters.
   */
  testDateParamters: function () {
    var test = {
        year: 2010,
        month: 4,
        day: 1
    };
    
    var expected = {
        dateString: "Sat May 01 2010 00:00:00",
        year: 2010,
        month: 4,
        day: 1
    };
    
    var dateObject = new Date(test.year, test.month, test.day);

    CSUnit.Assert.isInstanceOf(Date, dateObject);
    CSUnit.Assert.areEqual(expected.year, dateObject.getFullYear());
    CSUnit.Assert.areEqual(expected.month, dateObject.getMonth());
    CSUnit.Assert.areEqual(expected.day, dateObject.getDate());
  },
  
  /**
   * Test with date/time parameters.
   */
  testDateTimeParamters: function () {
    var test = {
        year: 2010,
        month: 4,
        day: 1,
        hour: 10,
        minutes: 0,
        seconds: 0
    };
    
    var expected = {
        dateString: "Sat May 01 2010 00:00:00",
        year: 2010,
        month: 4,
        day: 1,
        hour: 10,
        minutes: 0,
        seconds: 0
    };
    
    var dateObject = new Date(test.year, test.month, test.day, test.hour, test.minutes, test.seconds);

    CSUnit.Assert.isInstanceOf(Date, dateObject);
    CSUnit.Assert.areEqual(expected.year, dateObject.getFullYear());
    CSUnit.Assert.areEqual(expected.month, dateObject.getMonth());
    CSUnit.Assert.areEqual(expected.day, dateObject.getDate());
    CSUnit.Assert.areEqual(expected.hour, dateObject.getHours());
    CSUnit.Assert.areEqual(expected.minutes, dateObject.getMinutes());
    CSUnit.Assert.areEqual(expected.seconds, dateObject.getSeconds());
  },  
  
  testDateNow: function() {
    var test = {
        date: new Date()
    };
    
    if (test.date.getHours() === 0 && test.date.getMinutes() === 0 && test.date.getSeconds() === 0) {
      CSUnit.Assert.fail("Assertion that date object returned a time that is passed exact second of midnight failed.");
    }
    else {
      CSUnit.Assert.isTrue(true);
    }
  }
});

CSUnit.addTest(RP.tests.util.TU_DateOverride);
//////////////////////
// ..\util\TU_DefaultUrlTransform.js
//////////////////////
Ext.ns("RP.tests.util");

RP.tests.util.TU_DefaultUrlTransform = new CSUnit.Test.Case({
  name: "Unit Test for RP.util.DefaultUrlTransform",
  
  /**
   * Test the transformation of the URL when the url has the
   * fully qualified domain name path specified within the url.
   */
  testFQDNOfStaticPath: function() {
    var test = {
      url : "http://test/server/web/path/to/file.js",
      staticRoot : "http://test/server/"
    };

    var expected = {
      url : "http://test/server/web/path/to/file.js"
    };
    
    Ext.ns("RP.globals.paths");
    RP.globals.paths.STATIC = test.staticRoot;
    
    var transformedUrl = RP.util.DefaultUrlTransform(test.url);
    CSUnit.Assert.isNotNull(transformedUrl);
    CSUnit.Assert.areEqual(expected.url, transformedUrl);
  },
  
  /**
   * Tests the transformation of the path of the URL when the path
   * is defined as a relative path from the server host.
   */
  testRelativeUrlStaticPath: function() {
    var test = {
      url : "/web/path/to/file.js",
      staticRoot : "/web"
    };

    var expected = {
      url : "/web/path/to/file.js"
    };
    
    Ext.ns("RP.globals.paths");
    RP.globals.paths.STATIC = test.staticRoot;
    
    var transformedUrl = RP.util.DefaultUrlTransform(test.url);
    CSUnit.Assert.isNotNull(transformedUrl);
    CSUnit.Assert.areEqual(expected.url, transformedUrl);
  },
  
  /**
   * Tests prepending of the STATIC path to a URL where the
   * path does not include the FQDN at the start.
   */
  testAdditionOfFQDNToRelativePath: function() {
    var test = {
      url : "/web/path/to/file.js",
      staticRoot : "http://test/server"
    };

    var expected = {
      url : "http://test/server/web/path/to/file.js"
    };
    
    Ext.ns("RP.globals.paths");
    RP.globals.paths.STATIC = test.staticRoot;
    
    var transformedUrl = RP.util.DefaultUrlTransform(test.url);
    CSUnit.Assert.isNotNull(transformedUrl);
    CSUnit.Assert.areEqual(expected.url, transformedUrl);
  },
  
  /**
   * Validates handling of null urls.
   */
  testNullUrl: function() {
    var test = {
      url: null
    };
    
    var transformedUrl = RP.util.DefaultUrlTransform(test.url);
    CSUnit.Assert.isNull(transformedUrl);
  },
  
  /**
   * Validate the logic for replacing the {client-mode} token.
   */
  testReplacementOfClientMode: function () {
    var test = {
      url : "/web/path/to/file.{client-mode}.js",
      staticRoot : "http://test/server",
      clientMode: "test"
    };

    var expected = {
      url : "http://test/server/web/path/to/file.test.js"
    };
    
    Ext.ns("RP.globals.paths");
    RP.globals.CLIENTMODE = test.clientMode;
    RP.globals.paths.STATIC = test.staticRoot;
    
    var transformedUrl = RP.util.DefaultUrlTransform(test.url);
    CSUnit.Assert.isNotNull(transformedUrl);
    CSUnit.Assert.areEqual(expected.url, transformedUrl);
  },
  
  /**
   * Validate the logic for not replacing the {client-mode} token.
   */
  testNoReplacementOfClientMode: function () {
    var test = {
      url : "/web/path/to/file.js",
      staticRoot : "http://test/server",
      clientMode: "test"
    };

    var expected = {
      url : "http://test/server/web/path/to/file.js"
    };
    
    Ext.ns("RP.globals.paths");
    RP.globals.CLIENTMODE = test.clientMode;
    RP.globals.paths.STATIC = test.staticRoot;
    
    var transformedUrl = RP.util.DefaultUrlTransform(test.url);
    CSUnit.Assert.isNotNull(transformedUrl);
    CSUnit.Assert.areEqual(expected.url, transformedUrl);
  }
});

CSUnit.addTest(RP.tests.util.TU_DefaultUrlTransform);
//////////////////////
// ..\util\TU_FunctionQueue.js
//////////////////////
Ext.ns("RP.tests.util");

RP.tests.util.TU_FunctionQueue = new CSUnit.Test.Case({
  name: "Unit Test for RP.util.FunctionQueue",
  results: null,
  fq: null,
  
  setUp: function() { 
    this.fq = new RP.util.FunctionQueue({noDefer: true});
    this.results = [];   
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
    delete this.results;
  },
  
  
  /**
   * Test single function successful
   */
  testSingleSuccess: function() {    
    this.fq.add(this._successFn.createDelegate(this.results));
    
    this.fq.execute((function() {
      CSUnit.Assert.areEqual(1, this.results.length, "Did not execute correctly");
    }).createDelegate(this), function() {
      CSUnit.Assert.isTrue(false, "Should not have errored out");
    });
  },
  
  /**
   * Test multiple functions all successful 
   */
  testMultipleSuccess: function() {
    this.fq.add(this._successFn.createDelegate(this.results));
    this.fq.add(this._successFn.createDelegate(this.results));
    
    this.fq.execute((function() {
      CSUnit.Assert.areEqual(2, this.results.length, "Did not execute correctly");
    }).createDelegate(this), function() {
      CSUnit.Assert.isTrue(false, "Should not have errored out");
    });
  },
  
  /**
   * Test single function with failure 
   */
  testSingleFailure: function() {
    this.fq.add(this._failureFn.createDelegate(this));
    
    this.fq.execute(function() {
      CSUnit.Assert.isTrue(false, "Should not have succeeded");      
    }, (function() {
      CSUnit.Assert.areEqual(0, this.results.length, "Did not execute correctly");
    }).createDelegate(this));
  },
  
  /**
   * Test multiple functions with failure in the middle 
   */
  testMultipleFailure: function() {
    this.fq.add(this._successFn.createDelegate(this.results));
    this.fq.add(this._failureFn.createDelegate(this));
    this.fq.add(this._successFn.createDelegate(this.results));
    
    this.fq.execute(function() {
      CSUnit.Assert.isTrue(false, "Should not have succeeded");      
    }, (function() {
      CSUnit.Assert.areEqual(1, this.results.length, "Did not execute correctly");
    }).createDelegate(this));
  },
  
  _successFn: function(successFn, failFn) {
    this.push("ok");
    successFn();
  },
  
  _failureFn: function(successFn, failFn) {
    failFn();
  },
  
  _exceptionFn: function(successFn, failFn) {
    throw new Error("Forced throw of error!");
  }
});

CSUnit.addTest(RP.tests.util.TU_FunctionQueue);
//////////////////////
// ..\util\TU_Object.js
//////////////////////
Ext.ns("RP.tests.util");

RP.tests.util.TU_Object = new CSUnit.Test.Case({
  name: "Unit Test for RP.util.Object",
  objEnglish: {
    hello: "hello",
    goodbye: "goodbye",
    engUnique1: "unique 1",
    address: {
      number: 1234,
      street: "Main"
    }
  },
  objSpanish: {
    hello: "hola",
    goodbye: "adios",
    spUnique1: "unico 1"
  },
  
  setUp: function() {   
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
  },
  
  
  /**
   * Test RP.mergeProperties with single object
   */
  testMergePropertiesSingleObject: function() {
    var x = RP.mergeProperties(this.objEnglish);
    
    // Verify all properties are copied.
    CSUnit.Assert.areEqual("hello", x.hello, "Merge failed.");
    CSUnit.Assert.areEqual("goodbye", x.goodbye, "Merge failed.");
    CSUnit.Assert.areEqual("unique 1", x.engUnique1, "Merge failed.");
    CSUnit.Assert.areEqual(1234, x.address.number, "Merge did not recursively copy property.");
    CSUnit.Assert.areEqual("Main", x.address.street, "Merge did not recursively copy property.");
    
    // Verify we get our own copy by editing our local copy and checking
    // the source hasn't changed.
    x.hello = "HELLO";
    CSUnit.Assert.areEqual("hello", this.objEnglish.hello, "Merge failed.");
  },
  
  /**
   * Test RP.mergeProperties with multiple objects
   */
  testMergePropertiesMultipleObjects: function() {
    // Merge with objEnglish first, then objSpanish.  objSpanish properties should overwrite those
    // in objEnglish.
    var x = RP.mergeProperties(this.objEnglish, this.objSpanish);
    
    CSUnit.Assert.areEqual("hola", x.hello, "Merge failed; latter object properties should overwrite earlier ones.");
    CSUnit.Assert.areEqual("adios", x.goodbye, "Merge failed; latter object properties should overwrite earlier ones.");
    CSUnit.Assert.areEqual("unique 1", x.engUnique1, "Merge failed; latter object property not copied.");
    CSUnit.Assert.areEqual(1234, x.address.number, "Merge failed; latter object property not copied.");
    CSUnit.Assert.areEqual("Main", x.address.street, "Merge failed; latter object property not copied.");
    CSUnit.Assert.areEqual("unico 1", x.spUnique1, "Merge failed; second object property not copied.");
    
    x = RP.mergeProperties(this.objSpanish, this.objEnglish);
    
    CSUnit.Assert.areEqual("hello", x.hello, "Merge failed; latter object properties should overwrite earlier ones.");
    CSUnit.Assert.areEqual("goodbye", x.goodbye, "Merge failed; latter object properties should overwrite earlier ones.");
    CSUnit.Assert.areEqual("unique 1", x.engUnique1, "Merge failed; latter object property not copied.");
    CSUnit.Assert.areEqual(1234, x.address.number, "Merge failed; latter object property not copied.");
    CSUnit.Assert.areEqual("Main", x.address.street, "Merge failed; latter object property not copied.");
    CSUnit.Assert.areEqual("unico 1", x.spUnique1, "Merge failed; first object property not copied.");
  }
});

CSUnit.addTest(RP.tests.util.TU_Object);
//////////////////////
// ..\util\TU_Waiter.js
//////////////////////
Ext.ns("RP.tests.util");

RP.tests.util.TU_Waiter = new CSUnit.Test.Case({
  name: "Unit Test for RP.util.Waiter",
    
  setUp: function() {   
  },
  
  /*
   * Cleans up everything that was created by setUp().
   * @method
   */
  tearDown: function() {
  },
  
  
  /**
   * Test Add, Clear, and Get Count
   */
  testAddAndCount: function() {
    var waiter = new RP.util.Waiter();
    
    waiter.add("a");
    waiter.add("b");
    CSUnit.Assert.areEqual(2, waiter.getCount(), "Wrong count.");
    CSUnit.Assert.areEqual(false, waiter.isEmpty(), "isEmpty wrong result.");
    
    waiter.clear();
    CSUnit.Assert.areEqual(0, waiter.getCount(), "Wrong count after clearing.");
    CSUnit.Assert.areEqual(true, waiter.isEmpty(), "isEmpty wrong result.");
  },
  
  /**
   * Test Remove
   */
  testRemove: function() {
    var waiter = new RP.util.Waiter();
    
    waiter.add("a");
    waiter.add("b");
    waiter.remove("a");
    CSUnit.Assert.areEqual(1, waiter.getCount(), "Wrong count after remove.");
    waiter.remove("b");
    CSUnit.Assert.areEqual(0, waiter.getCount(), "Wrong count after remove.");
  },
  
  /**
   * Test to ensure handler is called when wait list is empty
   */
  testHandlerCalled: function() {
    var waiter = new RP.util.Waiter({defer: false});
    var processed = false;
    
    waiter.addHandler(function() {
      processed = true;
    }, this);
    waiter.add("a");
    waiter.add("b");
    
    waiter.remove("a");
    waiter.remove("b");
    CSUnit.Assert.areEqual(true, processed, "Handler not called");
  },
  
  /**
   * Test Suspend/Resume
   */
  testSuspendAndResume: function() {
    var waiter = new RP.util.Waiter({defer: false});
    var processed = false;
    
    waiter.addHandler(function() {
      processed = true;
    }, this);
    waiter.add("a");
    waiter.add("b");
    waiter.suspend();
    
    waiter.remove("a");
    waiter.remove("b");
    CSUnit.Assert.areEqual(false, processed, "Handler called while suspended");
    
    waiter.resume();
    CSUnit.Assert.areEqual(true, processed, "Handler not called after resume");
  },
  
  /**
   * Test insertion of handler to begin of list
   */
  testInsertHandler: function() {
    var waiter = new RP.util.Waiter({defer: false});
    var handler1 = false, handler2 = false;
    
    waiter.addHandler(function() {
      CSUnit.Assert.areEqual(true, handler1, "Handler 2 should be called after handler 1");
      handler2 = true;
    }, this);
    
    waiter.insertHandler(function() {
      CSUnit.Assert.areEqual(false, handler2, "Handler inserted must be called first");
      handler1 = true;      
    }, this);
    
    waiter.add("a");
    waiter.remove("a");
  }
});

CSUnit.addTest(RP.tests.util.TU_Waiter);
//////////////////////
// ..\util\DebugConsole\TU_Path.js
//////////////////////
Ext.ns("RP.tests.util.DebugConsole");

(function() {
  
  var assert = CSUnit.Assert;
  var Path = RP.util.DebugConsole.Path;
  
  var PATH_IDS_ONLY = "one/two/three";
  var PATH_IDS_ONLY_EXPECTED = "window.Ext.getCmp('one')" + "" +
                                                       ".getComponent('two')" + 
                                                       ".getComponent('three')";
  var PATH_WILD_CARD = "one/two/*/three";
  var PATH_WILD_CARD_EXPECTED = "window.Ext.getCmp('one')" + "" +
                                                        ".getComponent('two')" + 
                                                        ".find('itemId', 'three')[0]";
                                                        
  var PATH_ENDING_WILD_CARD ="one/two/three/*";
  
  var PATH_CSS = "one/two/css=.myClass/three";
  var PATH_CSS_EXPECTED = "window.Ext.getCmp(window.Ext.getCmp('one')" + "" +
                                                        ".getComponent('two')" + 
                                                        ".el.child('.myClass').id)" +
                                                        ".getComponent('three')";
  
  var PATH_XTYPE = "one/two/xtype=mytype/three";
  var PATH_XTYPE_EXPECTED = "window.Ext.getCmp('one')" + "" +
                                                        ".getComponent('two')" + 
                                                        ".findByType('mytype')[0]" +
                                                        ".getComponent('three')";
  
  var PATH_LITERAL = "one/two/.myFunction()/three";
  var PATH_LITERAL_EXPECTED = "window.Ext.getCmp('one')" + "" +
                                                        ".getComponent('two')" + 
                                                        ".myFunction()" +
                                                        ".getComponent('three')";
  
  var PATH_CSS_WITH_WILD_CARD = "one/two/*/css=.myClass/three";
  var PATH_XTYPE_WITH_WILD_CARD = "one/two/*/xtype=mytype/three";
  
  var PATH_COMBINED = "one/two/.myFunction()/css=.myClass/three/*/four/xtype=mytype/five";
  var PATH_COMBINED_EXPECTED = "window.Ext.getCmp(window.Ext.getCmp('one')" + "" +
                                                        ".getComponent('two')" + 
                                                        ".myFunction()" +
                                                        ".el.child('.myClass').id)" +
                                                        ".getComponent('three')" +
                                                        ".find('itemId', 'four')[0]" +
                                                        ".findByType('mytype')[0]" +
                                                        ".getComponent('five')";
  
  var PATH_CSS_XTYPE_CHAIN = "one/two/css=.myClass/xtype=mytype/css=.myClass2/five";
  var PATH_CSS_XTYPE_CHAIN_EXPECTED = "window.Ext.getCmp(window.Ext.getCmp(window.Ext.getCmp('one')" +
                                                              ".getComponent('two')" +
                                                              ".el.child('.myClass').id)" +
                                                              ".findByType('mytype')[0]" +
                                                              ".el.child('.myClass2').id)" +
                                                              ".getComponent('five')";
  
  RP.tests.util.DebugConsole.TU_Path = new CSUnit.Test.Case({
    
    name: "Unit Test for RP.tests.util.DebugConsole.Path",
    
    _should: {
      error: {
        testGetPathEndingWildCard: "Parse Error: Path cannot end with a '*' wildcard."
      }
    },
    
    setUp: function() {
    },
    
    /*
     * Cleans up everything that was created by setUp().
     * @method
     */
    tearDown: function() {
    },
    
    testGetPathIdsOnly: function() {
          var script = Path.parse(PATH_IDS_ONLY);
          assert.areEqual(PATH_IDS_ONLY_EXPECTED, script, "Script strings should match");
      },
      
      testGetPathWildCard: function() {
          var script = Path.parse(PATH_WILD_CARD);
          assert.areEqual(PATH_WILD_CARD_EXPECTED, script, "Script strings should match");
      },
      
      testGetPathEndingWildCard: function() {
            var script = Path.parse(PATH_ENDING_WILD_CARD);
      },
      
      testGetCssPath: function() {
          var script = Path.parse(PATH_CSS);
          assert.areEqual(PATH_CSS_EXPECTED, script, "Script strings should match");
      },
      
      testGetXtypePath: function() {
          var script = Path.parse(PATH_XTYPE);
          assert.areEqual(PATH_XTYPE_EXPECTED, script, "Script strings should match");
      },
      
      testGetLiteralPath: function() {
          var script = Path.parse(PATH_LITERAL);
          assert.areEqual(PATH_LITERAL_EXPECTED, script, "Script strings should match");
      },
        
      testCssWithWildCard: function() {
          var script = Path.parse(PATH_CSS_WITH_WILD_CARD);
          assert.areEqual(PATH_CSS_EXPECTED, script, "Script strings should match");
      },
      
      testXtypeWithWildCard: function() {
          var script = Path.parse(PATH_XTYPE_WITH_WILD_CARD);
          assert.areEqual(PATH_XTYPE_EXPECTED, script, "Script strings should match");
      },
      
      testCombined: function() {
          var script = Path.parse(PATH_COMBINED);
          assert.areEqual(PATH_COMBINED_EXPECTED, script, "Script strings should match");
      },
      
      testCssXtypeChain: function() {
          var script = Path.parse(PATH_CSS_XTYPE_CHAIN);
          assert.areEqual(PATH_CSS_XTYPE_CHAIN_EXPECTED, script, "Script strings should match");
      }
  });
})();

CSUnit.addTest(RP.tests.util.DebugConsole.TU_Path);

