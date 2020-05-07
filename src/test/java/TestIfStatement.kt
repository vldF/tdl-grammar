import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestIfStatement {
    @Test
    fun ifStatement() {
        val verifier = Verifier("examples/ifStatement/")
        verifier.loadAndVerifyFile("ifStatement")  // point will be verified automatically

        val expectedErrors = setOf(
                Unresolved("h", "main", 7),
                Unresolved("t", "main", 9)
        )

        val errors = verifier.getErrors("ifStatement")
        val unused = verifier.getUnused("ifStatement")

        Assertions.assertEquals(expectedErrors, errors?.toSet())
        Assertions.assertEquals(0, unused?.size)
    }
}