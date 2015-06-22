let $source := doc('fitness2.xml')

return
    (
    		delete node $source//Member[last()]/FavoriteColor
        
    )