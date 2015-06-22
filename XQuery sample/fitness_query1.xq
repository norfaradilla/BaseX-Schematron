let $source := doc('fitness1.xml')

return
    (
        replace node $source//Member[1]/Remark/msg with "Works at Hanscom AFB",
        replace node $source//Member[2]/Remark/msg with "Works in Bedford",
        replace node $source//Member[3]/Remark/msg with "Works in Hawaii"
    )
