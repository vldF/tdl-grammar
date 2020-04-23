package ast.objects

abstract class Expression(override val name: String) : EntityBase() {

    open fun getAllChildren(): List<EntityBase> {
        val res = mutableListOf<EntityBase>()
        for (childrenList in children.values) {
            for (c in childrenList.filterIsInstance<Expression>()) {
                res.addAll(c.getAllChildren())
            }
        }

        return res
    }
}