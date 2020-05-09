package ru.vldf.validator

import TdlLexer
import TdlParser
import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.FileNotFoundException

class Verifier(private val filesLocation: String = "") {
    private val visited = mutableMapOf<String, Scope>()
    private val visitErrors = hashMapOf<String, VisitErrors>()

    fun loadAndVerifyFile(name: String) = loadAndVerifyFile(File("$filesLocation$name.tdl"))


    fun loadAndVerifyFile(file: File) {
        val lexer = TdlLexer(ANTLRFileStream(file.absolutePath))
        val tokenStream = CommonTokenStream(lexer)
        val parser = TdlParser(tokenStream)
        parser.removeErrorListeners()
        parser.addErrorListener(ErrorPrinter())

        verify(parser, file.nameWithoutExtension)
    }

    fun getUnused(name: String): List<UnusedStorage>? = visited[name]?.getUnused()

    fun getErrors(name: String) = visitErrors[name]?.getAll()


    fun getLastErrors() =
            if (visitErrors.isNotEmpty()) visitErrors.values.last()
            else null

    private fun verify(parser: TdlParser, name: String) {
        if (visited[name] != null)
            return

        val ctx = parser.tdlFile()
        val globalScope = Scope()

        visited[name] = globalScope
        val badImports = doImports(ctx, globalScope)

        val tree = TdlTreeScopeBuilder(parser, globalScope)
        tree.visit(ctx)

        visitErrors[name] = tree.getVisitResult()

        for (import in badImports)
            visitErrors[name]?.add(
                    Unresolved(import, "IMPORTS", 0)
            )
    }

    private fun doImports(ctx: TdlParser.TdlFileContext, scope: Scope): MutableList<String> {
        val importList = ctx.importList().importHeader().map {
            it.simpleIdentifier().Identifier().text
        }

        val badImports = mutableListOf<String>()

        for (import in importList) {
            if (visited[import] != null) {
                scope.importGlobalFromScope(visited[import]!!)
            } else {
                try {
                    this.loadAndVerifyFile(import)
                } catch (_: FileNotFoundException) {}
                visited[import]?.let { scope.importGlobalFromScope(it) }
                        ?: badImports.add(import)
            }
        }
        return badImports
    }

}