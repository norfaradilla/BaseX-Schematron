let $source := doc('fitness2.xml')

return
    (
    		delete node $source//Member[last()]/FavoriteColor,
		    insert node <FavoriteColor>black</FavoriteColor> into $source//Member[4]
        
    )
