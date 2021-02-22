package app.util

import app.model.Division
import app.model.DivisionResult
import app.model.FailedResult
import app.model.Result
import org.apache.kafka.streams.kstream.ValueMapper

class DivisionMapper : ValueMapper<Division, Result> {

    override fun apply(input: Division): Result {
        return try {
            DivisionResult(input.dividend / input.divisor, input)
        } catch (e: Exception) {
            FailedResult(e.toString(), input)
        }
    }

}