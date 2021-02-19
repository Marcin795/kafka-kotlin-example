package app

import app.model.Division
import app.model.WrappedResult
import org.apache.kafka.streams.kstream.ValueMapper

class DivisionMapper : ValueMapper<Division, WrappedResult> {

    override fun apply(value: Division): WrappedResult {
        return try {
            val result = value.dividend / value.divisor
            WrappedResult(value = result)
        } catch (e: Exception) {
            WrappedResult(e, value)
        }
    }

}