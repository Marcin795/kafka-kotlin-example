package app

import app.model.WrappedResult
import org.apache.kafka.streams.kstream.ValueMapper

class Unwrapper : ValueMapper<WrappedResult, Int> {

    override fun apply(wrappedResult: WrappedResult): Int {
        return wrappedResult.value as Int
    }

}
