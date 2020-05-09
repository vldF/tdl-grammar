package ru.vldf.validator

import ru.vldf.validator.ast.objects.CallableEntity
import ru.vldf.validator.ast.objects.Variable
import java.lang.StringBuilder

/**
 * Stores unused entities. Any of listed params may be empty list
 */
data class UnusedStorage(
        val scopeName: String,
        val unusedVariables: List<Variable> = listOf(),
        val unusedFunctions: List<CallableEntity> = listOf(),
        val unusedTypes: List<CallableEntity> = listOf(),
        val unusedExemplars: List<Variable> = listOf(),
        val unusedInvokesOn: List<CallableEntity> = listOf(),
        val unusedFields: List<Variable> = listOf()
) {
    fun string(spaces: Int): String {
        val res = StringBuilder()
        if (unusedVariables.isNotEmpty()) {
            res.append("${" ".repeat(spaces)}variables: ").append(unusedVariables.joinToString(separator = ", ")).append("\n")
        }

        if (unusedFunctions.isNotEmpty()) {
            res.append("${" ".repeat(spaces)}functions: ").append(unusedFunctions.joinToString(separator = ", ")).append("\n")
        }

        if (unusedTypes.isNotEmpty()) {
            res.append("${" ".repeat(spaces)}types: ").append(unusedTypes.joinToString(separator = ", ")).append("\n")
        }

        if (unusedExemplars.isNotEmpty()) {
            res.append("${" ".repeat(spaces)}exemplars of types: ").append(unusedExemplars.joinToString(separator = ", ")).append("\n")
        }

        if (unusedInvokesOn.isNotEmpty()) {
            res.append("${" ".repeat(spaces)}invokes on: ").append(unusedInvokesOn.joinToString(separator = ", ")).append("\n")
        }

        return if (res.isNotEmpty()) res.insert(0, "${" ".repeat(spaces / 2)} unused for scope $scopeName:\n").toString() else ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnusedStorage

        if (scopeName != other.scopeName) return false
        if (unusedVariables != other.unusedVariables) return false
        if (unusedFunctions != other.unusedFunctions) return false
        if (unusedTypes != other.unusedTypes) return false
        if (unusedExemplars != other.unusedExemplars) return false
        if (unusedInvokesOn != other.unusedInvokesOn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scopeName.hashCode()
        result = 31 * result + unusedVariables.hashCode()
        result = 31 * result + unusedFunctions.hashCode()
        result = 31 * result + unusedTypes.hashCode()
        result = 31 * result + unusedExemplars.hashCode()
        result = 31 * result + unusedInvokesOn.hashCode()
        return result
    }


}