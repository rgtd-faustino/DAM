package dam.exer_3

class Pipeline {

    val stepsList = mutableMapOf<String, (List<String>) -> List<String>>()

    fun addStage(name: String, transform: (List<String>) -> List<String>) {

    }

    fun execute(input: List<String>): List<String> {

    }

    fun describe() {

    }
}


fun buildPipeline(lambda: (Pipeline) -> Unit): Pipeline{

}