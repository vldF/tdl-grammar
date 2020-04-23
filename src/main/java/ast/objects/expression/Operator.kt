package ast.objects.expression

import ast.objects.EntityBase
import ast.objects.Expression

abstract class Operator : Expression("operator")

abstract class BinaryOperator() : Operator() {
    abstract val a: EntityBase
    abstract val b: EntityBase
    override fun getAllChildren(): List<EntityBase> {
        val res = mutableListOf<EntityBase>()
        if (a is Expression) {
            res.addAll((a as Expression).getAllChildren())
        } else {
            res.add(a)
        }

        if (b is Expression) {
            res.addAll((b as Expression).getAllChildren())
        } else {
            res.add(b)
        }

        return res
    }
}

data class Add(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()
data class Mul(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()
data class Sub(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()
data class Div(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()

data class UMinus(val a: EntityBase) : Operator() {
    override fun getAllChildren(): List<EntityBase> {
        return listOf(a)
    }
}
