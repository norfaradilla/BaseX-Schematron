<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:saxon="http://saxon.sf.net/"
    exclude-result-prefixes="#all">

  <xsl:output method="text"/>

  <xsl:template name="findComm">
 

	<xsl:param name="param1"/>
  	<xsl:param name="param2"/>
	
	<xsl:variable name="v1" select="saxon:evaluate($param1)"/>
	<xsl:variable name="v2" select="saxon:evaluate($param2)"/>

        <!-- code untuk cari common node(dia display node saje) bukan full xpath -->
              <xsl:variable name="vCommonAncestor" select=
    "$v1/ancestor::*
       [count(. | $v2/ancestor::*) 
       = 
        count($v2/ancestor::*)
       ]
        [1]"
   />
      <result>

<!--kat sini die cuba access ancestor of the most common, untuk dapatkan complete xpath instead of cuma show node yang most common tu je.. -->
<xsl:variable name="theNode" select="$vCommonAncestor"></xsl:variable>
<xsl:for-each select="$theNode |$theNode/ancestor-or-self::*">
       <xsl:value-of select="name()" />
          <!-- CUBA TUKAR POSITION() DENGAN COUNT!!SEBAB NAMPAKNYE PREDICATENYA X BTOL!!!--> 
   <xsl:text>[</xsl:text>  
   
        <xsl:value-of select="count(preceding-sibling::*)+1" /> 
        <xsl:text>]</xsl:text>  
<xsl:text>/</xsl:text>
</xsl:for-each>
<!--
        <xsl:variable name="pos" select="$vCommonAncestor"></xsl:variable>
        <xsl:text>[</xsl:text>        
        <xsl:value-of select="position()" />
        <xsl:text>]</xsl:text>

-->
      </result>
   
            </xsl:template>


            
 

</xsl:stylesheet>

