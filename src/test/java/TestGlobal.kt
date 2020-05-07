import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestGlobal {
    @Test
    fun global() {
        val verifier = Verifier("examples/global/")
        verifier.loadAndVerifyFile("global")  // point will be verified automatically

        val errorsExpected = setOf(
                UnmatchingArgument("A", "global", 6, similarHolder)
        )

        val errors = verifier.getErrors("global")
        val unused = verifier.getUnused("global")

        Assertions.assertEquals(errorsExpected, errors?.toSet())
        Assertions.assertEquals(setOf<UnusedStorage>(), unused?.toSet())
    }
}