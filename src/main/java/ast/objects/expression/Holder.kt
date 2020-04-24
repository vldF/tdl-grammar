package ast.objects.expression

import ast.objects.EntityBase
import ast.objects.Expression
import ast.objects.Parameter

abstract class Holder : Expression("holder") {
    lateinit var obj: EntityBase
}

class CallableHolder(override val name: String, val params: List<Parameter>) : Holder()

class VariableHolder(override val name: String) : Holder()