package ast.objects

// todo: replace params to smthing
data class CallableEntity(override val name: String, val parameterNameList: List<Parameter>) : EntityBase() {
    override fun getBasicScope(): MutableSet<EntityBase> {
        val scope = mutableSetOf<EntityBase>()
        scope.addAll(parameterNameList)
        scope.addAll(children.values.flatten())
        if (parent != null)
            scope.addAll(parent!!.getBasicScope())

        return scope
    }

    override fun getInnerScope(): HashMap<String, MutableList<EntityBase>> {
        val scope = HashMap(children)
        for (p in parameterNameList) {
            if (scope[p.name] == null) {
                scope[p.name] = mutableListOf<EntityBase>(p)
            } else {
                scope[p.name]!!.add(p)
            }
        }
        return scope
    }

    override fun getEntityFromScopeByName(name: String): Parameter =
        parameterNameList.find { it.name == name } ?: this.getEntityFromScopeByName(name) // todo use hashMap?

}