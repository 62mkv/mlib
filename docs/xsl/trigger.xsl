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
       <img align="right" border="0"
            alt="E2e Documentation"
            src="../gif/redprairie.gif"/>
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

   <!-- The description takes precedence over the summary.  -->

   <div class="description">

    <xsl:choose>

     <xsl:when test="description">
      <xsl:value-of select="description"/>
     </xsl:when>

     <xsl:when test="summary">
      <xsl:value-of select="summary"/>
     </xsl:when>

    </xsl:choose>

   </div>

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
           <xsl:when test="@type = 'Flag'">
            flag
           </xsl:when>
           <xsl:when test="@type = 'ARGTYP_INT'">
            integer
           </xsl:when>
           <xsl:when test="@type = 'Integer'">
            integer
           </xsl:when>
           <xsl:when test="@type = 'ARGTYP_FLOAT'">
            float
           </xsl:when>
           <xsl:when test="@type = 'Float'">
            float
           </xsl:when>
           <xsl:when test="@type = 'ARGTYP_STR'">
            string
           </xsl:when>
           <xsl:when test="@type = 'String'">
            string
           </xsl:when>
           <xsl:when test="@type = 'ARGTYP_GENERIC'">
            pointer
           </xsl:when>
           <xsl:when test="@type = 'Generic'">
            pointer
           </xsl:when>
           <xsl:when test="@type = 'ARGTYP_RESULTS'">
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
   <!-- BEGIN : ATTRIBUTES                                  -->
   <!--                                                     -->

   <div class="section">
    Attributes
   </div>

   <div class="indent1">
    <table class="my-table" cellpadding="3">
     <tr class="my-th">
       <th nowrap="yes">Fires On</th>
       <th nowrap="yes">Sequence</th>
       <th nowrap="yes">Enabled </th>
     </tr>
     <tr class="my-td">
      <td nowrap="yes">
       <a class="link" href="{command/uri}">
        <xsl:value-of select="command/component-level"/> /
        <xsl:value-of select="command/name"/>
       </a>
      </td>
      <td nowrap="yes"><xsl:value-of select="sequence"/></td>
      <td nowrap="yes">
       <xsl:choose>
        <xsl:when test="enabled = 'No'">
         no
        </xsl:when>
        <xsl:when test="enabled = 'no'">
         no
        </xsl:when>
        <xsl:when test="enabled = 'N'">
         no
        </xsl:when>
        <xsl:when test="enabled = 'n'">
         no
        </xsl:when>
        <xsl:when test="enabled = 'Yes'">
         yes
        </xsl:when>
        <xsl:when test="enabled = 'yes'">
         yes
        </xsl:when>
        <xsl:when test="enabled = 'Y'">
         yes
        </xsl:when>
        <xsl:otherwise>
         <xsl:value-of select="enabled"/>
        </xsl:otherwise>
       </xsl:choose>
      </td>
     </tr>
    </table>
   </div>

   <!--                                                     -->
   <!-- END   : ATTRIBUTES                                  -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : ACTION                                      -->
   <!--                                                     -->

   <div class="section">
    Action
   </div>

   <div class="indent1">
    <div class="code">
     <pre>
      <xsl:value-of select="action"/>
     </pre>
    </div>
   </div>

   <!--                                                     -->
   <!-- END   : ACTION                                      -->
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
