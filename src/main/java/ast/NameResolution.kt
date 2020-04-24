package ast

import ast.objects.*
import ast.objects.expression.CallableHolder

class NameResolution {
    fun resolve(node: EntityBase): MutableList<Exceptions> {
        val errors = mutableListOf<Exceptions>()
        for (c in node.children.values) {
            for (expressionNode in c.filterIsInstance<Expression>()) {
                val holders = expressionNode.getAllHolders()
                for (h in holders) {
                    val variants = node.scope[h.name]

                    if (variants == null) {
                        errors.add(UnresolvedException(h))
                        continue
                    }

                    val res = if (h is CallableHolder) {
                        val callableVariants = variants.filterIsInstance<CallableEntity>()
                        if (callableVariants.isEmpty()) {
                            errors.add(UnresolvedException(h))
                            continue
                        }
                        callableVariants.find { it.parameterNameList.size == h.params.size }
                    }else
                        variants.filterIsInstance<Variable>().first()


                    if (res == null) {
                        errors.add(UnmatchingArgumentsException(h, variants))
                        continue
                    }

                    h.obj = res

                }
            }
        }

        return errors
    }
}