import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.io.File

private fun tdlFiles(base: File): Sequence<File> {
    if (!base.isDirectory && base.name.endsWith(".tdl")) {
        return sequenceOf(base)
    }
    val files = base.listFiles()?.asSequence() ?: emptySequence()
    return files.flatMap { tdlFiles(it) }
}

private fun parseFile(file: File): TdlParser {
    val lexer = TdlLexer(ANTLRFileStream(file.absolutePath))
    val tokenStream = CommonTokenStream(lexer)
    val parser = TdlParser(tokenStream)
    parser.removeErrorListeners()
    parser.addErrorListener(ErrorPrinter())
    return parser
}

fun main(args: Array<String>) {
    val base = args.getOrNull(0) ?: "examples"
    val files = tdlFiles(File(base))
    var totalFiles = 0
    var successful = 0
    for (source in files) {
        try {
            totalFiles++
            println("Parsing: $source")
            System.out.flush()
            val parser = parseFile(source)
            val ctx = parser.tdlFile()
            if (files.count() == 1) {
                val ruleList = parser.ruleNames.asSequence()
                    .filter { it.matches("[a-zA-Z]*".toRegex()) }
                    .toList()
                ctx.inspect(ruleList)
            }
            successful++
        } catch (ex: Exception) {
            println("Exception: " + ex.message)
        }
    }
    println("Total files: $totalFiles; successfully parsed: $successful")
}

internal class ErrorPrinter : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        throw ParseCancellationException("line $line:$charPositionInLine $msg")
    }
}
