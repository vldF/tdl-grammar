package ast

import TdlParserBaseVisitor
import org.antlr.v4.runtime.tree.ParseTree

class TdlVisitor : TdlParserBaseVisitor<Any>() {
    override fun visit(tree: ParseTree?): Any {
        return super.visit(tree)
    }

}