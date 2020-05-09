import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestFunctionCall {
    @Test
    fun functionCall() {
        val verifier = Verifier("examples/functionCall/")
        verifier.loadAndVerifyFile("functionCall")  // point will be verified automatically

        val errors = verifier.getErrors("functionCall")
        val unused = verifier.getUnused("functionCall")

        Assertions.assertEquals(0, errors?.size)
        Assertions.assertEquals(0, unused?.size)
    }
}