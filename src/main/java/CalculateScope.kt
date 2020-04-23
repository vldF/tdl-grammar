import ast.objects.EntityBase
import ast.objects.Root

class CalculateScope {
    private lateinit var root: Root

    fun calculateScopeForNode(node: EntityBase, scope: HashMap<String, MutableList<EntityBase>>) {
        //todo don't forget clone() scope
        for (c in node.getInnerScope()) {
            if (scope[c.key] == null)
                scope[c.key] = c.value
            else
                scope[c.key]!!.addAll(c.value)
        }
        node.scope = scope // todo: change list type to immutable

        for ((_, value) in node.children) {
            value.forEach { calculateScopeForNode(it, HashMap(scope)) } // todo: may be just Map?
        }
    }
}