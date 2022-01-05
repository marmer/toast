import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

private val THREADS = (Runtime.getRuntime().availableProcessors() * 1.0).toInt()

private const val SCALE = 2500
private const val PRECISIONING_STEPS = 10_000
private val MATH_CONTEXT = MathContext.UNLIMITED

class ToastTest {
    private val BLOCK_COUNT = THREADS * 100
    private val BLOCK_SIZE = PRECISIONING_STEPS / BLOCK_COUNT


    @Test
//    @Timeout(30, unit = TimeUnit.SECONDS)
    fun `Create some CPU load`() {
        // Preparation
        val processedSteps = AtomicInteger(0)

        val startTime = System.currentTimeMillis()

        // Execution
        println("Calculating PI")
        runBlocking {
            val dispatcher = Executors.newFixedThreadPool(THREADS).asCoroutineDispatcher()

            println(
                "PI: " + (0 until BLOCK_COUNT)
                    .map { block ->
                        async(dispatcher) {
                            (block * BLOCK_SIZE until block * BLOCK_SIZE + BLOCK_SIZE)
                                .map {
                                    //Bailey–Borwein–Plouffe formula .. https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
                                    (1 / 16.pow(it)) * (
                                            (4.bd() / (8L * it + 1)) -
                                                    (2.bd() / (8L * it + 4)) -
                                                    (1.bd() / (8L * it + 5)) -
                                                    (1.bd() / (8L * it + 6)))
                                }.sum()
                                .also {
                                    printProgress(processedSteps)
                                }

                        }
                    }
                    .awaitAll()
                    .also { println("Bringing it all togehter") }
                    .sum()
            )
            println("Took ${(System.currentTimeMillis() - startTime).toDouble() / 1000} Seconds")
        }

        // Assertion
    }

    private fun CoroutineScope.printProgress(processedSteps: AtomicInteger) {
        async(Dispatchers.Default) {
            println(
                "Progress: %6.2f%%".format(
                    processedSteps.incrementAndGet() * 100 / BLOCK_COUNT.toDouble()
                )
            )
        }
    }
}


private operator fun Int.div(divisor: BigDecimal): BigDecimal =
    bd().divide(divisor, RoundingMode.UP)

private operator fun Int.times(factor: BigDecimal): BigDecimal = bd().multiply(factor, MATH_CONTEXT)

private fun Int.pow(p: Int): BigDecimal = bd().pow(p)

private operator fun BigDecimal.times(factor: Int): BigDecimal = multiply(factor.bd(), MATH_CONTEXT)
private operator fun BigDecimal.plus(addend: Int): BigDecimal = plus(addend.bd())
private operator fun BigDecimal.div(addend: Int): BigDecimal = divide(addend.bd(), RoundingMode.UP)
private operator fun BigDecimal.div(addend: Long): BigDecimal = divide(addend.bd(), RoundingMode.UP)


private fun Int.bd(): BigDecimal = toBigDecimal().setScale(SCALE)
private fun Long.bd(): BigDecimal = toBigDecimal().setScale(SCALE)

private fun List<BigDecimal>.sum() = reduce { a, b -> a.plus(b) }
