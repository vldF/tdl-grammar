import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

class Verifier(private val filesLocation: String = "") {
    private val visited = mutableMapOf<String, Scope>()
    private val visitErrors = hashMapOf<String, VisitErrors>()
    private val unusedMap = hashMapOf<String, List<UnusedStorage>>()

    fun loadAndVerifyFile(name: String) = loadAdnVerifyFile(File("$filesLocation$name.tdl"))


    fun loadAdnVerifyFile(file: File) {
        val lexer = TdlLexer(ANTLRFileStream(file.absolutePath))
        val tokenStream = CommonTokenStream(lexer)
        val parser = TdlParser(tokenStream)
        parser.removeErrorListeners()
        parser.addErrorListener(ErrorPrinter())

        verify(parser, file.nameWithoutExtension)
    }

    fun getUnused(name: String): List<UnusedStorage>? {
        return unusedMap[name] ?: run {
            if (visited[name] == null)
                null
            else {
                unusedMap[name] = visited[name]!!.getUnused()
                unusedMap[name]
            }
        }
    }

    fun getUnused() = unusedMap.toMap()

    fun getLastUnused() =
            if (unusedMap.isNotEmpty()) unusedMap.values.last()
            else null

    fun getErrors(name: String) = visitErrors[name]

    fun getLastErrors() =
            if (visitErrors.isNotEmpty()) visitErrors.values.last()
            else null

    private fun verify(parser: TdlParser, name: String) {
        //todo parser.buildParseTree = true
        if (visited[name] != null) {
            println("parsing of file $name skipped")
            return
        }

        val ctx = parser.tdlFile()
        val globalScope = Scope()

        visited[name] = globalScope
        doImports(ctx, globalScope)

        val tree = TdlTreeScopeBuilder(parser, globalScope)
        tree.visit(ctx)

        visitErrors[name] = tree.getVisitResult()
        unusedMap[name] = globalScope.getUnused()
    }

    private fun doImports(ctx: TdlParser.TdlFileContext, scope: Scope) {
        val importList = ctx.importList().importHeader().map {
            it.simpleIdentifier().Identifier().text
        }

        for (import in importList) {
            if (visited[import] != null) {
                scope.importGlobalFromScope(visited[import]!!)
            } else {
                this.loadAndVerifyFile(import)
                visited[import]?.let { scope.importGlobalFromScope(it) }
                        ?: throw IllegalStateException("can't import file $import")
            }
        }
    }

}