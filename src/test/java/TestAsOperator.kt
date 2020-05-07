import ast.objects.CallableEntity
import ast.objects.Variable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestAsOperator {
    @Test
    fun asOperator() {
        val verifier = Verifier("examples/asOperator/")
        verifier.loadAndVerifyFile("asOperator")

        val errorsExpected = setOf(
                Unresolved("t", "foo", 9)
        )
        val unusedExpected = setOf(
                UnusedStorage(
                        "global",
                        unusedFunctions = listOf(
                                CallableEntity("foo", listOf(
                                        Variable("a"),
                                        Variable("b"),
                                        Variable("c"),
                                        Variable("d")
                                ))
                        )
                )
        )

        val errors = verifier.getLastErrors()!!.getAll()
        val unused = verifier.getUnused("asOperator")
        Assertions.assertEquals(errorsExpected, errors.toSet())
        Assertions.assertEquals(unusedExpected, unused?.toSet())
    }
}