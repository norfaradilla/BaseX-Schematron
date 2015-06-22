<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
    <sch:pattern name="Object2" id="object_low">
        <sch:rule context="FavoriteColor" role="optional"></sch:rule>
        <sch:rule context="Name" role="optional"></sch:rule>
        <sch:rule context="age" role="optional"></sch:rule>
    </sch:pattern>
    <sch:pattern name="Dynamic Constraint Test">
        <sch:rule context="target">
                <sch:assert test="@name = 'age' and newvalue > oldvalue">New age must be greater than previous age data</sch:assert>
        </sch:rule>
    </sch:pattern>
</sch:schema>