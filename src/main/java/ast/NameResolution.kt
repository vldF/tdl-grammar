package ast

import ast.objects.EntityBase
import ast.objects.Expression

class NameResolution {
    fun resolve(node: EntityBase) {
        for (c in node.children.values) {
            for (expressionNode in c.filterIsInstance<Expression>()) {

            }
        }
    }

    private fun EntityBase.allChildren(): List<EntityBase> {
        return listOf()
    }
}