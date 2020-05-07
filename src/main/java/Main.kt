import java.io.File


fun main(args: Array<String>) {
    val base = args.getOrNull(0) ?: "examples/"
    val files = File(base).listFiles()!!
    val verifier = Verifier(base)

    for (source in files) {
        println("validating file: ${source.nameWithoutExtension}")
        verifier.loadAndVerifyFile(source)

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


    println("done")
}

