let $source := doc('fitness2.xml')

return
    (
        replace value of node $source//Member[1]/Remark/age with "45",
        replace value of node $source//Member[2]/Remark/age with "12",
        replace node $source//Member[3]/Remark/age with <age>2</age>,
        delete node $source//Member[last()]/FavoriteColor,
        insert node <FavoriteColor>black</FavoriteColor> into $source//Member[last()]
    )