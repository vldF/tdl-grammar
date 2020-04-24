package ast.objects.expression

import ast.objects.EntityBase
import ast.objects.Expression

abstract class Operator : Expression("operator")

abstract class BinaryOperator() : Operator() {
    abstract val a: EntityBase
    abstract val b: EntityBase
    override fun getAllHolders(): List<Holder> {
        val res = mutableListOf<Holder>()
        if (a is Expression) {
            res.addAll((a as Expression).getAllHolders())
        }

        if (b is Expression) {
            res.addAll((b as Expression).getAllHolders())
        }

        return res
    }
}

data class Add(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()
data class Mul(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()
data class Sub(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()
data class Div(override val a: EntityBase, override val b: EntityBase) : BinaryOperator()

data class UMinus(val a: EntityBase) : Operator() {
    override fun getAllHolders(): List<Holder> {
        if (a is Holder) return listOf(a)
        return listOf()
    }
}
