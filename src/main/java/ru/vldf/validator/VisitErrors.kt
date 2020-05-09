package ru.vldf.validator

import ru.vldf.validator.ast.objects.EntityBase

/**
 * Errors in program
 */
class VisitErrors {
    private val children = mutableListOf<VisitErrors>()
    private val results = mutableListOf<Result>()

    fun add(result: Result) {
        results.add(result)
    }

    fun getAll(): List<Result> {
        return results + children.flatMap { it.getAll() }
    }

    fun get(): List<Result> = results

    fun addChild(visitErrors: VisitErrors) = children.add(visitErrors)
}

/**
 * Base abstract class for all errors
 */
abstract class Result {
    abstract val entityName: String
    abstract val parentName: String
    abstract val line: Int
}

/**
 * Unresolved error. Creates if entity (variable, function, etc) doesn't exist
 */
data class Unresolved(
        override val entityName: String,
        override val parentName: String,
        override val line: Int
) : Result()

/**
 * Unmatching Argument. Creates if function/type with this name exist, but with different arguments
 */
data class UnmatchingArgument(
        override val entityName: String,
        override val parentName: String,
        override val line: Int,
        val similar: EntityBase
) : Result() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnmatchingArgument) return false

        if (entityName != other.entityName) return false
        if (parentName != other.parentName) return false
        if (line != other.line) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityName.hashCode()
        result = 31 * result + parentName.hashCode()
        result = 31 * result + line
        return result
    }
}

/**
 * Ambiguity error. Creates if variable or type with the name already exists.
 * Or if function with name and params set exist
 */
data class Ambiguity(
        override val entityName: String,
        override val parentName: String,
        override val line: Int
) : Result()

/**
 * Empty type error. Creates if type declared without params
 */
data class EmptyType(
        override val entityName: String,
        override val parentName: String,
        override val line: Int
) : Result()
