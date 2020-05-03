import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.io.File

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
    val files = File(base).listFiles()!!
    var totalFiles = 0
    var successful = 0
    for (source in files) {
        totalFiles++
        println("Parsing: $source")
        System.out.flush()
        val parser = parseFile(source)
        parser.buildParseTree = true
        val ctx = parser.tdlFile()

        val globalScope = Scope()
        TdlTreeScopeBuilder(parser, globalScope).visit(ctx)

        for (unusedEntity in globalScope.getUnused()) {
            println(unusedEntity)
        }

        successful++
    }
    println("Total files: $totalFiles; successfully parsed: $successful")
}
