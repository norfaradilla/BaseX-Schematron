<?xml version="1.0" encoding="UTF-8"?>

<!-- Using style 1 of constraint -->

<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
    
	<sch:pattern name="xind_base" abstract="true" id="XIND">
            <!-- rule 1: LHS -->
            <sch:rule id="A" context="//VIP/*">
                <key name="Name" path="VIP/Member/Name"/>
                <key name="Remark" path="VIP/Member/Remark"/>
                
            </sch:rule>
        
            <!-- rule 2: RHS -->
            <sch:rule id="B" context="//All/*">
                <key name="NameR" path="All/Member/Name"/>
                <key name="RemarkR" path="All/Member/Remark"/>
                <key name="FavoriteColorR" path="All/Member/FavoriteColor"/>
             </sch:rule>
             
             <!-- rule 3: RHS and LHS checking -->
             <sch:rule context="*">
                 <extends rule="A"/>
                 <assert test="@Name='xx' and @Remark='xx'">LHS incomplete</assert>
                 <assert test="@Name='xx' and @Remark='xx'">LHS incomplete</assert>
                 <assert test="@Name='xx' and @Remark='xx'">LHS incomplete</assert>
             </sch:rule>                                                                                    .

	</sch:pattern>    
	
</sc12h:schema>