import ast.objects.ParametredEntity
import ast.objects.Variable
import ast.objects.expression.Add
import ast.objects.expression.Constant
import ast.objects.expression.Mul
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExpressionTest {
    @Test
    fun basicTest() {
        val exp = Add(Mul(Constant(2), Variable("var")), ParametredEntity("fun", listOf()))
        val entities = exp.getAllChildren()
        Assertions.assertTrue(entities.size == 2)
        Assertions.assertTrue(entities.any { it.name == "var" })
        Assertions.assertTrue(entities.any { it.name == "fun" })
    }
}