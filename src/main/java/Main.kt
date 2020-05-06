import ast.objects.CallableEntity
import ast.objects.Variable
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.io.File
import java.lang.StringBuilder


fun main(args: Array<String>) {
    val base = args.getOrNull(0) ?: "examples/"
    val files = File(base).listFiles()!!
    val verifier = Verifier(base)

    for (source in files) {
        println("validating file: ${source.nameWithoutExtension}")
        verifier.loadAdnVerifyFile(source)

        for (err in verifier.getLastErrors()!!.getAll()) {
            when (err) {
                is Unresolved -> System.err.println("unresolved: ${err.entityName} on line ${err.line} in ${err.parentName}")
                is UnmatchingArgument -> System.err.println("Unmatching arguments: " +
                        "${err.entityName} on line ${err.line} in ${err.parentName}. Similar: ${err.similar}")
                is Ambiguity -> System.err.println("Ambiguity: ${err.entityName} on line ${err.line} in ${err.parentName}")
                is EmptyType -> System.err.println("Empty type: ${err.entityName} on line ${err.line} in ${err.parentName}")
                else -> System.err.println("unknown error")
            }
            System.err.flush()
        }

    }

    for ((name, value) in verifier.getUnused()) {
        println("  unused for $name")
        value.forEach { println("    $it") }
    }

    println("done")
}

data class UnusedStorage(
        val scopeName: String,
        val unusedVariables: List<Variable>,
        val unusedFunctions: List<CallableEntity>,
        val unusedTypes: List<CallableEntity>,
        val unusedExemplars: List<Variable>,
        val unusedInvokesOn: List<CallableEntity>
) {
    override fun toString(): String {
        val res = StringBuilder()
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

        return if (res.isNotEmpty()) res.insert(0, "unused for scope $scopeName:\n").toString() else ""
    }
}