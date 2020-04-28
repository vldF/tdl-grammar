import ast.objects.CallableEntity
import ast.objects.Parameter
import ast.objects.Variable
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class TdlTreeScopeValidatorVisitor(parser: TdlParser, private val globalScope: Scope) : TdlParserBaseVisitor<Unit>() {
    private val text: String = parser.inputStream.text

    override fun visitFunctionBody(ctx: TdlParser.FunctionBodyContext) {
        val name =
                if (ctx.parent is TdlParser.FunctionDeclarationContext)
                    (ctx.parent as TdlParser.FunctionDeclarationContext).simpleIdentifier().Identifier().text
                else {
                    super.visitFunctionBody(ctx)
                    return
                }
        val localScope = globalScope.getScope(name) ?: throw Exception("scope doesn't exist")

        for (leaf in getFlattenLeaf(ctx)) {
            when(getTokenType(leaf, text)) {
                TokenType.VARIABLE -> localScope.getVariable(leaf.text) ?: System.err.println("cant resolve variable ${leaf.text}")
                else -> println("${leaf.text} skipped")
            }
        }

        super.visitFunctionBody(ctx)
    }

}