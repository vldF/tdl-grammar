import ast.objects.EntityBase

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

abstract class Result {
    abstract val entityName: String
    abstract val parentName: String
    abstract val line: Int
}

data class Unresolved(
        override val entityName: String,
        override val parentName: String,
        override val line: Int
) : Result()

data class UnmatchingArgument(
        override val entityName: String,
        override val parentName: String,
        override val line: Int,
        val similar: EntityBase
) : Result()

data class Ambiguity(
        override val entityName: String,
        override val parentName: String,
        override val line: Int
) : Result()

data class EmptyType(
        override val entityName: String,
        override val parentName: String,
        override val line: Int
) : Result()
