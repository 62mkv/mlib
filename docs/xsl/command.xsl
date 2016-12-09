<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns="http://www.w3.org/TR/xhtml11/strict">
<xsl:output method="html"/>
<xsl:template match="/documentation">

 <html>

  <head>
   <meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
   <link rel="stylesheet" type="text/css" href="../../css/redprairie.css"/>
   <script type="text/javascript" src="../../script/fixdesc.js">
    <xsl:comment> Load the code to "fix" descriptions. </xsl:comment>
   </script>
  </head>

  <body onload="fixdesc( );">

   <!--                                                     -->
   <!-- BEGIN : PAGE TITLE                                  -->
   <!--                                                     -->

   <table class="title-table">
    <tr valign="bottom">
     <td>
      <span class="title">
       <xsl:value-of select="name"/>
      </span>
      <span class="backlink">
       <a class="link" href="{component-level/uri}">
        [<xsl:value-of select="component-level/name"/>]
       </a>
      </span>
     </td>
     <td>
      <a href="../../index.html">
       <img align="right" border="0"
            alt="E2e Documentation"
            src="../../gif/redprairie.gif"/>
      </a>
     </td>
    </tr>
   </table>

   <hr size="1"/>

   <!--                                                     -->
   <!-- END   : PAGE TITLE                                  -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : DESCRIPTION                                 -->
   <!--                                                     -->

   <!-- The description takes precedence over the summary. -->

   <div class="description">

    <xsl:choose>

     <xsl:when test="description">
      <xsl:value-of select="description"/>
      <br/>
     </xsl:when>

     <xsl:when test="summary">
      <xsl:value-of select="summary"/>
      <br/>
     </xsl:when>

    </xsl:choose>

   </div>

   <xsl:if test="private">

    <div class="private">
     This command is private to this component library and should not be 
     referenced by any other commands.
    </div>
    <div class="private">
     Its arguments, published data and behavior are free to change at any 
     time without notice.
     <br/>
    </div>

   </xsl:if>

   <xsl:if test="overridden-by">

    <span class="overridden">
     This command is overridden by
    </span>
    <span class="item">
     <a class="link" href="{overridden-by/uri}">
      <xsl:value-of select="overridden-by/component-level"/> 
      /
      <xsl:value-of select="overridden-by/command"/>.
     </a>
     <br/>
    </span>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : DESCRIPTION                                 -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : ARGUMENTS                                   -->
   <!--                                                     -->

   <div class="section">
    Arguments
   </div>

   <div class="indent1">

    <xsl:choose>

     <xsl:when test="argument">
      <table class="my-table" cellpadding="3">
       <tr class="my-th">
        <th nowrap="yes">Name       </th>
        <th nowrap="yes">Alias      </th>
        <th nowrap="yes">Type       </th>
        <th nowrap="yes">Required   </th>
        <th nowrap="yes">Default    </th>
        <th nowrap="yes">Description</th>
       </tr>
       <xsl:for-each select="argument">
        <tr class="my-td">
         <td nowrap="yes"><xsl:value-of select="@name" /></td>
         <td nowrap="yes"><xsl:value-of select="@alias"/></td>
         <td nowrap="yes"> 
          <xsl:choose>
           <xsl:when test="@type = 'ARGTYP_FLAG'">
	    flag
	   </xsl:when>
           <xsl:when test="@type = 'flag'">
	    flag
	   </xsl:when>
           <xsl:when test="@type = 'Flag'">
	    flag
	   </xsl:when>
           <xsl:when test="@type = 'ARGTYP_INT'">
	    integer
	   </xsl:when>
           <xsl:when test="@type = 'integer'">
	    integer
	   </xsl:when>
           <xsl:when test="@type = 'Integer'">
	    integer
	   </xsl:when>
           <xsl:when test="@type = 'ARGTYP_FLOAT'">
	    float
	   </xsl:when>
           <xsl:when test="@type = 'float'">
	    float
	   </xsl:when>
           <xsl:when test="@type = 'Float'">
	    float
	   </xsl:when>
           <xsl:when test="@type = 'ARGTYP_STR'">
	    string
	   </xsl:when>
           <xsl:when test="@type = 'string'">
	    string
	   </xsl:when>
           <xsl:when test="@type = 'String'">
	    string
	   </xsl:when>
           <xsl:when test="@type = 'ARGTYP_GENERIC'">
	    pointer
	   </xsl:when>
           <xsl:when test="@type = 'generic'">
	    pointer
	   </xsl:when>
           <xsl:when test="@type = 'Generic'">
	    pointer
	   </xsl:when>
           <xsl:when test="@type = 'ARGTYP_RESULTS'">
	    results
	   </xsl:when>
           <xsl:when test="@type = 'results'">
	    results
	   </xsl:when>
           <xsl:when test="@type = 'Results'">
	    results
	   </xsl:when>
           <xsl:otherwise>
            <xsl:value-of select="@type"/>
           </xsl:otherwise>
          </xsl:choose>
	 </td>
         <td nowrap="yes"> 
          <xsl:choose>
           <xsl:when test="@required = 'No'">
	    no
	   </xsl:when>
           <xsl:when test="@required = 'no'">
	    no
	   </xsl:when>
           <xsl:when test="@required = 'N'">
	    no
	   </xsl:when>
           <xsl:when test="@required = 'n'">
	    no
	   </xsl:when>
           <xsl:when test="@required = 'Yes'">
	    yes
	   </xsl:when>
           <xsl:when test="@required = 'yes'">
	    yes
	   </xsl:when>
           <xsl:when test="@required = 'Y'">
	    yes
	   </xsl:when>
           <xsl:when test="@required = 'y'">
	    yes
	   </xsl:when>
           <xsl:otherwise>
            <xsl:value-of select="@required"/>
           </xsl:otherwise>
          </xsl:choose>
         </td>
         <td nowrap="yes"><xsl:value-of select="@default"/></td>
         <td>
          <span class="my-description">
           <span>
            <xsl:value-of select="."/>
           </span>
          </span>
         </td>
        </tr>
       </xsl:for-each>
      </table>
     </xsl:when>
 
     <xsl:otherwise>
       There are no documented arguments for this command.
     </xsl:otherwise>

    </xsl:choose>

   </div>

   <!--                                                     -->
   <!-- END   : ARGUMENTS                                   -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : EXPECTED ROWS RETURNED                      -->
   <!--                                                     -->

   <xsl:if test="retrows">

    <div class="section">
     Expected Rows Returned
    </div>

    <div class="indent1">
     <xsl:value-of select="retrows"/>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : EXPECTED ROWS RETURNED                      -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : PUBLISHED DATA                              -->
   <!--                                                     -->

   <xsl:if test="retcol">

    <div class="section">
     Published Data
    </div>

    <div class="indent1">
     <table class="my-table" cellpadding="3">
      <tr class="my-th">
       <th nowrap="yes">Name       </th>
       <th nowrap="yes">Type       </th>
       <th nowrap="yes">Description</th>
      </tr>
      <xsl:for-each select="retcol">
       <tr class="my-td">
        <td><xsl:value-of select="@name"/></td>
        <td>
         <xsl:choose>
          <xsl:when test="@type = 'COMTYP_INT'">
	   integer
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_LONG'">
	   long
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_FLOAT'">
	   float
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_DATTIM'">
	   date/time
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_CHAR'">
	   string
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_STRING'">
	   string
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_TEXT'">
	   string
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_BINARY'">
	   binary
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_GENERIC'">
	   pointer
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_BOOLEAN'">
	   boolean
	  </xsl:when>
          <xsl:when test="@type = 'COMTYP_RESULTS'">
	   results
	  </xsl:when>
          <xsl:otherwise>
           <xsl:value-of select="@type"/>
          </xsl:otherwise>
         </xsl:choose>
        </td>
        <td>
         <span class="my-description">
          <span>
           <xsl:value-of select="."/>
          </span>
         </span>
        </td>
       </tr>
      </xsl:for-each>
     </table>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : PUBLISHED DATA                              -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : POLICIES                                    -->
   <!--                                                     -->

   <xsl:if test="policy">

    <span class="section">
     Policies
    </span>
 
    <div class="indent1">
     <table class="my-table" cellpadding="3">
      <tr class="my-th">
       <th nowrap="yes">Code       </th>
       <th nowrap="yes">Variable   </th>
       <th nowrap="yes">Value      </th>
       <th nowrap="yes">String 1   </th>
       <th nowrap="yes">String 2   </th>
       <th nowrap="yes">Number 1   </th>
       <th nowrap="yes">Number 2   </th>
       <th nowrap="yes">Float 1    </th>
       <th nowrap="yes">Float 2    </th>
       <th nowrap="yes">Description</th>
      </tr>
      <xsl:for-each select="policy">
       <tr class="my-td">
        <td nowrap="yes"><xsl:value-of select="@polcod"/></td>
        <td nowrap="yes"><xsl:value-of select="@polvar"/></td>
        <td nowrap="yes"><xsl:value-of select="@polval"/></td>
        <td nowrap="yes"><xsl:value-of select="@rtstr1"/></td>
        <td nowrap="yes"><xsl:value-of select="@rtstr2"/></td>
        <td nowrap="yes"><xsl:value-of select="@rtnum1"/></td>
        <td nowrap="yes"><xsl:value-of select="@rtnum2"/></td>
        <td nowrap="yes"><xsl:value-of select="@rtflt1"/></td>
        <td nowrap="yes"><xsl:value-of select="@rtflt2"/></td>
        <td>
         <span class="my-description">
          <span>
           <xsl:value-of select="."/>
          </span>
         </span>
        </td>
       </tr>
      </xsl:for-each>
     </table>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : POLICIES                                    -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : EXCEPTIONS                                  -->
   <!--                                                     -->

   <xsl:if test="exception">

    <div class="section">
     Exceptions
    </div>

    <div class="indent1">
     <table class="my-table" cellpadding="3">
      <tr class="my-th">
       <th nowrap="yes">Value      </th>
       <th nowrap="yes">Description</th>
      </tr>
      <xsl:for-each select="exception">
       <tr class="my-td">
        <td nowrap="yes"><xsl:value-of select="@value"/></td>
        <td>
         <span class="my-description">
          <span>
           <xsl:value-of select="."/>
          </span>
         </span>
        </td>
       </tr>
      </xsl:for-each>
     </table>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : EXCEPTIONS                                  -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : TRANSACTION                                 -->
   <!--                                                     -->

   <xsl:if test="transaction">

    <div class="section">
     Transaction Type
    </div>

     <div class="indent1">
     <xsl:value-of select="transaction"/>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : TRANSACTION                                 -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : REMARKS                                     -->
   <!--                                                     -->

   <xsl:if test="remarks">

    <div class="section">
     Remarks
    </div>

    <div class="indent1">
     <xsl:value-of disable-output-escaping="yes" select="remarks"/>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : REMARKS                                     -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : EXAMPLE                                     -->
   <!--                                                     -->

   <xsl:if test="example">

    <div class="section">
     Example(s)
    </div>

    <div class="indent1">
     <xsl:for-each select="example">
      <div class="code">
       <pre>
        <xsl:value-of select="."/>
       </pre>
      </div>
     </xsl:for-each>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : EXAMPLE                                     -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : LOCAL SYNTAX                                -->
   <!--                                                     -->

   <xsl:if test="local-syntax">

    <div class="section">
     Local Syntax
    </div>

    <div class="indent1">
     <div class="code">
      <pre> 
       <xsl:value-of select="local-syntax"/>
      </pre> 
     </div>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : LOCAL SYNTAX                                -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : TRIGGERS                                    -->
   <!--                                                     -->

   <xsl:if test="trigger">

    <div class="section">
     Triggers
    </div>

    <div class="indent1">
     <xsl:for-each select="trigger">
      <div class="item">
       <a class="link" href="{uri}">
	<xsl:value-of select="component-level"/> 
	/
	<xsl:value-of select="name"/>
       </a>
      </div>
     </xsl:for-each>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : TRIGGERS                                    -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : CALLED BY                                   -->
   <!--                                                     -->

   <xsl:if test="called-by">

    <div class="section">
     Called By
    </div>

    <div class="indent1">
     <xsl:for-each select="called-by">
      <div class="item">
       <a class="link" href="{uri}"> 
        <xsl:value-of select="component-level"/> 
	/
	<xsl:value-of select="command"/>
       </a>
      </div>
     </xsl:for-each>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : CALLED BY                                   -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : SEE ALSO                                    -->
   <!--                                                     -->

   <xsl:if test="seealso">

    <div class="section">
     See Also
    </div>

    <div class="indent1">
     <xsl:for-each select="seealso">
      <div class="item">
       <a class="link" href="{uri}"> 
	<xsl:value-of select="component-level"/> 
	/
	<xsl:value-of select="command"/>
       </a>
      </div>
     </xsl:for-each>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : SEE ALSO                                    -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : COPYRIGHT                                   -->
   <!--                                                     -->

   <br/>

   <div class="copyright">
    Copyright &#169; 2002-2008 RedPrairie Corporation.  All rights reserved.
   </div>

   <!--                                                     -->
   <!-- END   : COPYRIGHT                                   -->
   <!--                                                     -->

  </body>

 </html>

</xsl:template>
</xsl:stylesheet>
