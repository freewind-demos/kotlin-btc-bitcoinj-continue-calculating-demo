package example

import com.google.gson.Gson
import org.bitcoinj.core.Context
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.BlockFileLoader
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.sequences.asSequence

val mainNetParams = MainNetParams().apply {
    Context.getOrCreate(this)
}

val blockChainFiles = listOf(File("./btc-data/blocks/blk00000.dat"))
val calculatedDataDir = File("./calculated-data")

data class CalculatedData(
        val blockHeight: Int,
        val data: Map<String, Int>
)

val fileStore = run {
    if (!calculatedDataDir.exists()) {
        calculatedDataDir.mkdir()
    }
    FileStore(calculatedDataDir)
}

fun loadPreviousCalculatedData(): CalculatedData? {
    val data = fileStore.read()
    return data?.let { Gson().fromJson(it, CalculatedData::class.java) }
}

fun main(args: Array<String>) {
    val calculatedData = run {
        val previous = loadPreviousCalculatedData()
        previous ?: CalculatedData(-1, emptyMap())
    }

    println("Previous height: ${calculatedData.blockHeight}")

    val data = calculatedData.data.toMutableMap()
    val loader = BlockFileLoader(mainNetParams, blockChainFiles)
    var currentHeight: Int? = null
    loader.asSequence().mapIndexed { height, block ->
        height to block
    }.dropWhile { (height, _) ->
        height <= calculatedData.blockHeight
    }.forEach { (height, block) ->
        currentHeight = height

        val date = toDate(block.time)
        val transactionCount = block.transactions?.size ?: 0
        data.compute(date) { _, count ->
            (count ?: 0) + transactionCount
        }

        if (height % 10 == 0) {
            saveCalculatedData(height, data)
        }
    }

    currentHeight?.run {
        saveCalculatedData(this, data)
    }

}

fun saveCalculatedData(height: Int, data: MutableMap<String, Int>) {
    println("> saveCalculatedData: $height $data")
    val json = Gson().toJson(CalculatedData(height, data))
    fileStore.write(json)
}

fun toDate(time: Date): String {
    return SimpleDateFormat("yyyy-MM-dd").format(time)
}
