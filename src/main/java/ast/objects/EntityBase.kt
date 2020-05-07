package ast.objects


abstract class EntityBase {
    abstract val name: String

    var isUsed = false

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityBase

        if (name != other.name) return false

        return true
    }

    override fun toString(): String {
        return name
    }
}