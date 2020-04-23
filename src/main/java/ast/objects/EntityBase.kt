package ast.objects

abstract class EntityBase {
    abstract val name: String

    open val children = hashMapOf<String, MutableList<EntityBase>>()
    var parent: EntityBase? = null
    var scope: HashMap<String, MutableList<EntityBase>> = HashMap()

    fun addChild(entity: EntityBase) {
        if (children[entity.name] == null)
            children[entity.name] = mutableListOf(entity)
        else {
            children[entity.name]!!.add(entity)
        }

        entity.parent = this
    }

    open fun getInnerScope() = HashMap(children)


    open fun getBasicScope(): MutableSet<EntityBase> {
        val scope = mutableSetOf<EntityBase>()
        scope.addAll(children.values.flatten())
        scope.add(this)
        if (parent != null)
            scope.addAll(parent!!.getBasicScope())

        return scope
    }

    open fun getEntityFromScopeByName(name: String): EntityBase? { // todo узнать, есть ли совпадение с договором
        if (parent?.name?.equals(name) == true)
            return parent

        if (parent == null)
            return null

        val resInBrothers = children[name]?.first() // todo: delete this fun or edit
        if (resInBrothers != null)
            return resInBrothers

        return parent!!.getEntityFromScopeByName(name)
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
}