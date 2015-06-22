<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
    <sch:pattern name="Object2">
        <sch:rule context="city" role="type_Low">
           <sch:assert col="mandatory" test="city">City is part of Object2</sch:assert>
           <sch:assert col="mandatory" test="population">Population is part of Object2</sch:assert>
           <sch:assert col="mandatory" test="name">Population name is part of Object2</sch:assert>
        </sch:rule>
    </sch:pattern>
       
</sch:schema>