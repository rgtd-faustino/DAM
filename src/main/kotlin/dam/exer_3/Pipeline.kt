package dam.exer_3

class Pipeline {

    // decidi passar esta variável de uma lista de lista de strings para um dicionario porque pessoalmente
    // faz me mais sentido fazer nomeState -> lista de steps
    val stepsList = mutableMapOf<String, (List<String>) -> List<String>>()

    // e portanto aqui é simples, metemos a chave/valor diretamente ou seja o nome do stage e todos os seus passos
    fun addStage(name: String, transform: (List<String>) -> List<String>) {
        stepsList.put(name, transform)
    }

    // para dar execute é só meter o input (a lista de steps) em cada valor de cada stage
    fun execute(input: List<String>): List<String> {
        var result = input

        for (step in stepsList) {
            result = step.value(result)
        }

        return result
    }

    // para meter o índice de acordo com o output podemos meter uma variável contador
    fun describe() {
        var index = 1

        for (step in stepsList) {
            println("$index. ${step.key}")
            index++
        }
    }


}

// aqui tentei originalmente retornar tudo na mesma linha, mas não dá para fazer isso porque o lambda retorna um void
// então temos de criar uma variável e aplicar-lhe o lambda
fun buildPipeline(lambda: (Pipeline) -> Unit): Pipeline{
    val pipeline = Pipeline()
    lambda(pipeline)
    return pipeline

}