package ast

import ast.objects.EntityBase
import ast.objects.expression.Holder

open class Exceptions ()

data class UnresolvedException(val holder: Holder) : Exceptions()
data class UnmatchingArgumentsException(val problemHolder: Holder, val variants: List<EntityBase>) : Exceptions()