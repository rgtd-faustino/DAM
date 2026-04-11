package dam.exer_3

fun main() {
    val logs = listOf(
        " INFO : server started ",
        " ERROR : disk full ",
        " DEBUG : checking config ",
        " ERROR : out of memory ",
        " INFO : request received ",
        " ERROR : connection timeout "
    )

    val pipeline = buildPipeline { pipeline ->
        pipeline.addStage("Trim") { list ->
            list.map {
                it.trim()
            }
        }

        pipeline.addStage("Filter errors") { list ->
            list.filter {
                it.contains("ERROR")
            }
        }

        pipeline.addStage("Uppercase") { list ->
            list.map {
                it.uppercase()
            }
        }

        pipeline.addStage("Add index") { list ->
            list.mapIndexed {
                i, line -> "${i + 1}. $line"
            }
        }
    }

    println("Pipeline stages:")
    pipeline.describe()

    println()

    println("Result:")
    val result = pipeline.execute(logs)
    for (line in result) {
        println(line)
    }
}