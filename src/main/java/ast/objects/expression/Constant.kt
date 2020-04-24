package ast.objects.expression

import ast.objects.EntityBase
import ast.objects.Expression

data class Constant(val value: Any) : Expression("Constant") { // todo Any??
    override fun getAllHolders(): List<Holder> {
        return listOf()
    }
}