package ast.objects

import ast.objects.expression.Holder

open class Expression(override val name: String) : EntityBase() {

    open fun getAllHolders(): List<Holder> {
        val res = mutableListOf<Holder>()
        for (childrenList in children.values) {
            for (c in childrenList.filterIsInstance<Expression>()) {
                res.addAll(c.getAllHolders())
            }

            res.addAll(childrenList.filterIsInstance<Holder>())
        }

        return res
    }
}