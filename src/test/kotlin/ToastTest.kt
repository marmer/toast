import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.concurrent.Executors

class ToastTest {
    private val THREADS = 16
    private val blockSize = 1000
    private val blockCount = 1000


    @Test
//    @Timeout(30, unit = TimeUnit.SECONDS)
    fun `Create some CPU load`() {
        // Preparation

        // Execution
        println("Calculating PI")
        runBlocking {
            val dispatcher = Executors.newFixedThreadPool(THREADS).asCoroutineDispatcher()

            println(
                "PI: " + (0 until blockCount)
                    .map { block ->
                        async(dispatcher) {
                            (block * blockSize until block * blockSize + blockSize)
                                .map {
                                    //Bailey–Borwein–Plouffe formula .. https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
                                    1 / 16.pow(it) * (
                                            (4/(8.0*it+1))-
                                            (2/(8.0*it+4))-
                                            (1/(8.0*it+5))-
                                            (1/(8.0*it+6))
                                            )
                                }.sum()
                        }
                    }
                    .awaitAll()
                    .sum()
            )

        }

        // Assertion
    }

}


private val MATH_CONTEXT = MathContext.DECIMAL128

private operator fun Int.div(divisor: BigDecimal): BigDecimal =
    bd().divide(divisor, RoundingMode.UP)

private operator fun Int.times(factor: BigDecimal): BigDecimal = bd().multiply(factor, MATH_CONTEXT)
//private fun Int.pow(p: Int): BigDecimal = bd().pow(p)
private fun Int.pow(p: Int): Double = Math.pow(toDouble(), p.toDouble())

private operator fun BigDecimal.times(factor: Int): BigDecimal = multiply(factor.bd(), MATH_CONTEXT)
private operator fun BigDecimal.plus(addend: Int): BigDecimal = plus(addend.bd())

private fun Int.bd() = toBigDecimal(MATH_CONTEXT)

private fun List<BigDecimal>.sum() = reduce { a, b -> a.plus(b) }
