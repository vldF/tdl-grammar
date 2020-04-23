package ast.objects.expression

import ast.objects.EntityBase
import ast.objects.Expression
import ast.objects.ParametredEntity

data class Call(val entity: ParametredEntity) : Expression(entity.name) {
    override fun getAllChildren(): List<EntityBase> {
        return entity.parameterNameList
    }
}
