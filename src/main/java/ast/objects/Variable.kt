package ast.objects

data class Variable(override val name: String) : EntityBase() {
    var fields: List<String>? = null
    var reference: CallableEntity? = null
    override fun toString() = name
}
