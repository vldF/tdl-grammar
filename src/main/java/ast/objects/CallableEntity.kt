package ast.objects

import Scope

data class CallableEntity(override val name: String, val parameterNameList: List<Parameter>) : EntityBase()