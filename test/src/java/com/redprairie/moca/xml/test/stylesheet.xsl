<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml11/strict">
<xsl:output method="html"/>
<xsl:template match="/documentation">

 <html>

  <head>
   <link rel="stylesheet" type="text/css" href="../../css/redprairie.css"/>
  </head>

  <body>

   <!--                                                     -->
   <!-- BEGIN : PAGE TITLE                                  -->
   <!--                                                     -->

   <table class="title-table">
    <tr valign="bottom">
     <td>
      <span class="title">
       <xsl:value-of select="description"/>
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

   <table width="100%">
   <tr valign="top">
   <td width="50%">

   <!--                                                     -->
   <!-- BEGIN : COMMANDS                                    -->
   <!--                                                     -->

   <xsl:if test="command">

    <img src="../../gif/arrow.gif"/>
    <span class="section">
     Commands
    </span>

    <div class="link-list">
     <xsl:for-each select="command">
      <div class="item">
       <a class="link" href="{uri}">
        <xsl:value-of select="name"/>
       </a>
      </div>
     </xsl:for-each>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : COMMANDS                                    -->
   <!--                                                     -->

   </td>
   <td width="50%">

   <!--                                                     -->
   <!-- BEGIN : TRIGGERS                                    -->
   <!--                                                     -->

   <xsl:if test="trigger">

    <img src="../../gif/arrow.gif"/>
    <span class="section">
     Commands
    </span>

    <div class="link-list">
     <xsl:for-each select="trigger">
      <div class="item">
       <a class="link" href="{uri}">
        <xsl:value-of select="name"/>
       </a>
      </div>
     </xsl:for-each>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : TRIGGERS                                    -->
   <!--                                                     -->

   </td>
   </tr>
   </table>

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
