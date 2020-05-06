import ast.objects.Variable

class BlockVisitor(private val localScope: Scope,
                   private val name: String,
                   paramNames: List<String>,
                   private val text: String,
                   private val visitErrors: VisitErrors,
                   private val parser: TdlParser
) : TdlParserBaseVisitor<Unit>() {

    init {
        paramNames.forEach { localScope.addVariable(Variable(it)) }
    }

    override fun visitStatement(ctx: TdlParser.StatementContext) {
        if (ctx.assignment() != null || ctx.expression()?.asExpression() != null) {
            // this is assignment or as operator; for this case there is another way
            super.visitStatement(ctx)
            return
        }

        val results = exploreStatement(ctx, localScope, text, name, parser)
        visitErrors.addChild(results)
        super.visitStatement(ctx)

    }

    override fun visitAssignment(ctx: TdlParser.AssignmentContext) {
        val results = exploreAssignment(ctx, localScope, text, name, parser)
        visitErrors.addChild(results)
        super.visitAssignment(ctx)
    }

    override fun visitAsExpression(ctx: TdlParser.AsExpressionContext) {
        val results = exploreAsOperator(ctx, localScope, name)
        visitErrors.addChild(results)
        super.visitAsExpression(ctx)
    }
}