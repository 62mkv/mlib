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

   <xsl:apply-templates/>

   <br/>

   <div class="copyright">
    Copyright &#169; 2002-2008 RedPrairie Corporation.  All rights reserved.
   </div>

  </body>

 </html>

</xsl:template>

<!--                                                     -->
<!-- BEGIN : PAGE TITLE AND DESCRIPTION                  -->
<!--                                                     -->

<xsl:template match="/resultset/row/table_comment_rs/resultset">

 <table class="title-table">
  <tr valign="bottom">
   <td>
    <span class="title">
     <xsl:value-of select="row/table_name"/>
    </span>
    <span class="backlink">
     <a class="link" href="./index.html">
      [Index]
     </a>
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

 <xsl:for-each select="row/table_comment">

  <xsl:choose>

   <xsl:when test="@isnull = 'true'">
    <div class="description">
     No description is available for this table.
     <br/>
    </div>
   </xsl:when>

   <xsl:otherwise>
    <div class="description">
     <xsl:value-of select="."/>
     <br/>
    </div>
   </xsl:otherwise>

  </xsl:choose>

 </xsl:for-each>

</xsl:template>

<!--                                                     -->
<!-- END   : PAGE TITLE AND DESCRIPTION                  -->
<!--                                                     -->

<!--                                                     -->
<!-- BEGIN : TABLE DETAILS                               -->
<!--                                                     -->

<xsl:template match="/resultset/row/table_rs/resultset">

 <xsl:choose>

  <xsl:when test="row">

   <div class="section">
    Table Details
   </div>

   <div class="indent1">

    <table class="my-table" cellpadding="3">
     <tr class="my-th">
      <th nowrap="yes">Column     </th>
      <th nowrap="yes">Datatype   </th>
      <th nowrap="yes">Length     </th>
      <th nowrap="yes">Nullable   </th>
      <th nowrap="yes">Primary Key</th>
      <th nowrap="yes">Description</th>
     </tr>

     <xsl:for-each select="row">
      <tr class="my-td">
       <td nowrap="yes"> 
        <xsl:value-of select="column_name"/> 
       </td>
       <td nowrap="yes"> 
        <xsl:choose>
         <xsl:when test="comtyp = 'I'">
          integer
         </xsl:when>
         <xsl:when test="comtyp = 'S'">
          string
         </xsl:when>
         <xsl:when test="comtyp = 'D'">
          datetime
         </xsl:when>
         <xsl:when test="comtyp = 'F'">
          float
         </xsl:when>
        </xsl:choose> 
       </td>
       <td nowrap="yes"> 
        <xsl:value-of select="length"/> 
       </td>
       <td nowrap="yes"> 
        <xsl:choose>
         <xsl:when test="null_flg = '0'">
          no
         </xsl:when>
         <xsl:when test="null_flg = '1'">
          yes
         </xsl:when>
        </xsl:choose> 
       </td>
       <td nowrap="yes"> 
        <xsl:choose>
         <xsl:when test="pk_flg = '0'">
          no
         </xsl:when>
         <xsl:when test="pk_flg = '1'">
          yes
         </xsl:when>
        </xsl:choose> 
       </td>
       <td>
        <xsl:value-of select="column_comment"/> 
       </td>
      </tr>
     </xsl:for-each>

    </table>

   </div>

  </xsl:when>

 </xsl:choose>

</xsl:template>

<!--                                                     -->
<!-- END   : TABLE DETAILS                               -->
<!--                                                     -->

<!--                                                     -->
<!-- BEGIN : INDEX DETAILS                               -->
<!--                                                     -->

<xsl:template match="resultset/row/index_rs/resultset">

 <xsl:choose>

  <xsl:when test="row">

   <div class="section">
    Index Details
   </div>

   <div class="indent1">

    <table class="my-table" cellpadding="3">
     <tr class="my-th">
      <th nowrap="yes">Name   </th>
      <th nowrap="yes">Type   </th>
      <th nowrap="yes">Columns</th>
     </tr>

     <xsl:for-each select="row">
      <tr class="my-td">
       <td nowrap="yes"> 
        <xsl:value-of select="index_name"/> 
       </td>
       <td nowrap="yes"> 
        <xsl:value-of select="index_description"/> 
       </td>
       <td nowrap="yes">  
        <xsl:value-of select="index_keys"/> 
       </td>
      </tr>
     </xsl:for-each>

    </table>

   </div>

  </xsl:when>

 </xsl:choose>

</xsl:template>

<!--                                                     -->
<!-- END   : INDEX DETAILS                               -->
<!--                                                     -->

</xsl:stylesheet>
