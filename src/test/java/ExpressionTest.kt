import ast.objects.Expression
import ast.objects.Parameter
import ast.objects.CallableEntity
import ast.objects.expression.CallableHolder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExpressionTest {
    @Test
    fun basicTest() {
        val root = Expression("root")
        val funcHolder = CallableHolder("fun", listOf(Parameter("a")))
        root.addChild(funcHolder)

        val func = CallableEntity("fun", listOf(Parameter("a")))
        funcHolder.obj = func

        val children = root.getAllHolders()
        Assertions.assertTrue(children.size == 1)
        Assertions.assertTrue(children[0].obj == func)
    }
}