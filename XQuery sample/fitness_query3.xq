let $source := doc('fitness2.xml')

return
    (
    		insert node <Member Level="gold"><Name>Dilla</Name>
    <FavoriteColor>purple</FavoriteColor></Member> as first into $source//FitnessCenter
        
    )
