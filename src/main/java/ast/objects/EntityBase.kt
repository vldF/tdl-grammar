package ast.objects


abstract class EntityBase {
    abstract val name: String

    open val children = hashMapOf<String, MutableList<EntityBase>>()
    var parent: EntityBase? = null
    var isUsed = false

    fun addChild(entity: EntityBase) {
        if (children[entity.name] == null)
            children[entity.name] = mutableListOf(entity)
        else {
            children[entity.name]!!.add(entity)
        }

        entity.parent = this
    }

    override fun hashCode(): Int {
        return name.hashCode() + parent.hashCode() + children.hashCode()  // todo check it
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