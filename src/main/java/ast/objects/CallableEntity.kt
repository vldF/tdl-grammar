package ast.objects

data class CallableEntity(override val name: String, val parameterNameList: List<Variable>) : EntityBase() {
    override fun toString(): String {
        return "$name(${parameterNameList.map { it.name }.joinToString(", ")})"
    }
}