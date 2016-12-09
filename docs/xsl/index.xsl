<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns="http://www.w3.org/TR/xhtml11/strict">
<xsl:output method="html"/>
<xsl:template match="/documentation">

 <html>

  <head>
   <meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
   <link rel="stylesheet" type="text/css" href="./css/redprairie.css"/>
  </head>

  <body>

   <!--                                                     -->
   <!-- BEGIN : PAGE TITLE                                  -->
   <!--                                                     -->

   <table class="title-table">
    <tr valign="bottom">
     <td>
      <span class="title">
       E2e Documentation
      </span>
     </td>
     <td>
      <a href="./index.html">
       <img align="right" border="0"
        alt="E2e Documentation"
	src="./gif/redprairie.gif"/>
      </a>
     </td>
    </tr>
   </table>

   <hr size="1" color="#cc0000"/>

   <!--                                                     -->
   <!-- END   : PAGE TITLE                                  -->
   <!--                                                     -->

   <table width="100%">
   <tr valign="top">
   <td width="50%">

   <!--                                                     -->
   <!-- BEGIN : COMPONENT DOCUMENTATION                     -->
   <!--                                                     -->

   <xsl:if test="component-level">

    <img src="./gif/arrow.gif"/>
    <span class="section">
       Component Library Documentation
    </span>

    <div class="link-list">
     <xsl:for-each select="component-level">
      <div class="item">
       <a class="link" href="{uri}"> 
        <xsl:value-of select="description"/>
       </a>
      </div>
     </xsl:for-each>
    </div>

   </xsl:if>

   <!--                                                     -->
   <!-- END   : COMPONENT DOCUMENTATION                     -->
   <!--                                                     -->

   </td>
   <td width="50%">

   <!--                                                     -->
   <!-- BEGIN : WAREHOUSE DOCUMENTATION                     -->
   <!--                                                     -->

    <img src="./gif/arrow.gif"/>
    <span class="section">
     Warehouse Documentation
    </span>

    <div class="link-list">
     <div class="item">
      <a class="link" href="./wm/index.html"> 
       Online Help
      </a>
     </div>
    </div>

   <!--                                                     -->
   <!-- END   : WAREHOUSE DOCUMENTATION                     -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : LOGISTIX INTEGRATOR DOCUMENTATION           -->
   <!--                                                     -->

    <img src="./gif/arrow.gif"/>
    <span class="section">
       Integrator Documentation
    </span>

    <div class="link-list">
     <div class="item">
      <a class="link" href="./integration/index.html"> 
       Transactions
      </a>
     </div>
    </div>

   <!--                                                     -->
   <!-- END   : LOGISTIX INTEGRATOR DOCUMENTATION           -->
   <!--                                                     -->

   <!--                                                     -->
   <!-- BEGIN : DATABASE DOCUMENTATION                      -->
   <!--                                                     -->

    <img src="./gif/arrow.gif"/>
    <span class="section">
     Database Documentation
    </span>

    <div class="link-list">
     <div class="item">
      <a class="link" href="./database/index.html"> 
       Schema
      </a>
     </div>
    </div>

   <!--                                                     -->
   <!-- END   : DATABASE DOCUMENTATION                      -->
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
