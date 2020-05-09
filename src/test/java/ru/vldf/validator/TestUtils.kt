package ru.vldf.validator

import TdlLexer
import TdlParser
import org.antlr.v4.runtime.ANTLRFileStream
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class TestUtils {
    private fun loadParser(path: String): TdlParser {
        // creating tree
        val file = File(path)
        val lexer = TdlLexer(ANTLRFileStream(file.absolutePath))
        val tokenStream = CommonTokenStream(lexer)
        val parser = TdlParser(tokenStream)
        parser.removeErrorListeners()
        parser.addErrorListener(ErrorPrinter())

        return parser
    }

    @Test
    fun testGetFlattenLeafs() {
        val parser = loadParser("./examples/types/types.tdl")
        val leafs = getFlattenLeaf(parser.tdlFile()).map { it.text }

        val leafNames = listOf(
                "A", "B", "x", "C", "x", "y", "z", "print", "s", "main",
                "a", "A", "b", "B", "c", "C", "d", "B", "print", "c", "x"
        )
        Assertions.assertEquals(leafNames, leafs)

    }

    @Test
    fun testGetTokenType() {
        val parser = loadParser("./examples/tokenTypes/block.tdl")
        val leafs = getFlattenLeaf(parser.functionBody())
        val tokenTypes = leafs.map { getTokenType(it, parser) }

        // see ./examples/tokenTypes/block.tdl to info
        val expectedTokenTypes = listOf(
                TokenType.VARIABLE,             // TokenType(a), line 2
                TokenType.TYPE,                 // TokenType(A), line 2
                TokenType.VARIABLE_DECLARATION, // TokenType(b), line 3
                TokenType.VARIABLE,             // TokenType(a), line 3
                TokenType.VARIABLE,             // TT(a), line 4
                TokenType.MEMBER,               // TT(x), line 4
                TokenType.CALLABLE,             // TT(a), line 5
                TokenType.CALLABLE,             // TT(a), line 6
                TokenType.CALLABLE,             // TT(a), line 8
                TokenType.VARIABLE,             // TT(arg), line 9
                TokenType.VARIABLE,             // TT(arg), line 10
                TokenType.VARIABLE,             // TT(b), line 12
                TokenType.VARIABLE,             // TT(a), line 12
                TokenType.CALLABLE              // TT(a), line 12
        )

        Assertions.assertEquals(expectedTokenTypes, tokenTypes)
    }

    @Test
    fun testTypedVariableByMemberToken() {
        val parser = loadParser("./examples/memberToken/block.tdl")
        val block = parser.block()

        val names = getFlattenLeaf(block)
                .filter { getTokenType(it, parser) == TokenType.MEMBER }
                .map { getTypedVariableByMemberToken(it, parser) }

        val expectedNames = listOf("a", "superPuperMegaLongVariable", "a")

        Assertions.assertEquals(expectedNames, names)
    }

    @Test
    fun testGetParamsCount() {
        val parser = loadParser("./examples/functionCall/paramsCount.tdl")
        val tree = parser.block()

        val expectedParamCounts = listOf(0, 1, 3, 3)

        val paramCounts = getFlattenLeaf(tree)
                .filter { getTokenType(it, parser) == TokenType.CALLABLE }
                .map { getParamsCount(it, parser) }
        Assertions.assertEquals(expectedParamCounts, paramCounts)
    }
}