<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml11/strict">
<xsl:output method="html"/>

<xsl:template match="/">

 <html>

  <head>
   <meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
   <link rel="stylesheet" type="text/css" href="../css/redprairie.css"/>
  </head>

  <body>

   <table class="title-table">
    <tr valign="bottom">
     <td>
      <span class="title">
       Database Schema Documentation
      </span>
     </td>
     <td>
      <a href="../index.html">
       <img align="right" border="0" 
            alt="E2e Documentation" 
            src="../gif/redprairie.gif"/>
      </a>
     </td>
    </tr>
   </table>

   <hr size="1"/>

   <xsl:apply-templates/>

   <br/>
  
   <div class="copyright">
    Copyright &#169; 2002-2008 RedPrairie Corporation.  All rights reserved.
   </div>

  </body>

 </html>

</xsl:template>

<!--                                                     -->
<!-- BEGIN : TABLE LIST                                  -->
<!--                                                     -->

<xsl:template match="/resultset">

 <table width="100%">
  <tr valign="top">
   <td width="50%">

    <img src="../gif/arrow.gif"/>
    <span class="section">
     Tables
    </span>

    <div class="link-list">
     <table>
     <xsl:for-each select="row">
       <tr>
         <td>
           <div class="item">
             <a class="link" href="{table_name_0}.html">
               <xsl:value-of select="table_name_0"/>
             </a>
           </div>
         </td>
         <td>
           <div class="item">
             <a class="link" href="{table_name_1}.html">
               <xsl:value-of select="table_name_1"/>
             </a>
           </div>
         </td>
         <td>
           <div class="item">
             <a class="link" href="{table_name_2}.html">
               <xsl:value-of select="table_name_2"/>
             </a>
           </div>
         </td>
       </tr>
    </xsl:for-each>
    </table>
   </div>
   </td>
  </tr>
 </table>

</xsl:template>

<!--                                                     -->
<!-- END   : TABLE LIST                                  -->
<!--                                                     -->

</xsl:stylesheet>
