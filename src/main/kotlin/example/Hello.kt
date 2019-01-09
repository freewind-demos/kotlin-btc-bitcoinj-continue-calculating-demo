package example

import org.bitcoinj.core.Context
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.BlockFileLoader
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val mainNetParams = MainNetParams()
val blockChainFiles = listOf(File("./btc-data/blocks/blk00000.dat"))

fun main(args: Array<String>) {
    Context.getOrCreate(mainNetParams)

    val loader = BlockFileLoader(mainNetParams, blockChainFiles)

    val result = loader.map { block -> toDate(block.time) to (block.transactions?.size ?: 0) }
            .groupBy { it.first }
            .mapValues { entry -> entry.value.map { it.second }.sum() }
    println(result)
}

fun toDate(time: Date): String {
    return SimpleDateFormat("yyyy-MM-dd").format(time)
}
