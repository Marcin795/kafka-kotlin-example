package app.util

import app.model.Division
import app.model.DivisionResult
import app.model.FailedResult
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DivisionMapperTest {

    @Test
    fun `return division result`() {
        val division = Division(121, 11)
        val result = DivisionMapper().apply(division)
        assert(result is DivisionResult)
        result as DivisionResult
        assertEquals(division, result.input)
        assertEquals(11, result.result)
    }

    @Test
    fun `division by 0 is failed result`() {
        val division = Division(121, 0)
        val result = DivisionMapper().apply(division)
        assert(result is FailedResult)
        result as FailedResult
        assertEquals(division, result.input)
        assertEquals("java.lang.ArithmeticException: / by zero", result.exception)
    }

}
