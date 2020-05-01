package ast

import ast.objects.CallableEntity
import ast.objects.Variable
import java.lang.StringBuilder

data class UnusedStorage(
        val scopeName: String,
        val unusedVariables: List<Variable>,
        val unusedFunctions: List<CallableEntity>,
        val unusedTypes: List<CallableEntity>,
        val unusedExemplars: List<Variable>,
        val unusedInvokesOn: List<CallableEntity>
) {
    override fun toString(): String {
        val res = StringBuilder("unused for scope $scopeName:\n")
        if (unusedVariables.isNotEmpty()) {
            res.append("variables: ").append(unusedVariables.joinToString(separator = ", ")).append("\n")
        }

        if (unusedFunctions.isNotEmpty()) {
            res.append("functions: ").append(unusedFunctions.joinToString(separator = ", ")).append("\n")
        }

        if (unusedTypes.isNotEmpty()) {
            res.append("types: ").append(unusedTypes.joinToString(separator = ", ")).append("\n")
        }

        if (unusedExemplars.isNotEmpty()) {
            res.append("exemplars of types: ").append(unusedExemplars.joinToString(separator = ", ")).append("\n")
        }

        if (unusedInvokesOn.isNotEmpty()) {
            res.append("invokes on: ").append(unusedInvokesOn.joinToString(separator = ", ")).append("\n")
        }

        return res.toString()
    }
}