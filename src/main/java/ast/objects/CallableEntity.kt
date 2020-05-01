package ast.objects

import Scope

data class CallableEntity(override val name: String, val parameterNameList: List<Parameter>) : EntityBase() {
    override fun toString(): String {
        return "$name(${parameterNameList.map { it.name }.joinToString(", ")})"
    }
}