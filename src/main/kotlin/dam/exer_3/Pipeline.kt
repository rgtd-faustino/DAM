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

    // apanhamos os steps todos da lista original, depois removemos e juntamos os steps com a junção dos stage nomes
    // originalmente tinha feito um for loop a percorrer o dicionário, mas lembrei-me que podemos apanhar os valores
    // diretamente através do nome da chave
    fun compose(stageName1: String, stageName2: String) {
        // temos de afirmar que não é nulo senão dá erro
        val steps1 = stepsList[stageName1]!!
        val steps2 = stepsList[stageName2]!!

        stepsList.remove(stageName1)
        stepsList.remove(stageName2)

        // criamos uma função para podermos juntar la dentro as listas de steps
        stepsList[stageName1 + "_" + stageName2] = { input ->
            // chamamos a função steps1 com o input e apanhamos o resultado
            val result1 = steps1(input)
            // passamos esse resultado ao steps2 (para ser de acordo com o execute do pipeline, ou seja,
            // seguido para formar uma cadeia seguida) e devolvemos esse resultado final
            steps2(result1)
        }
    }

    // a função toma o input que é a lista de passos e os dois pipelines, no fim retorna o par dos resultados
    fun fork(input: List<String>, pipeline1: Pipeline, pipeline2: Pipeline): Pair<List<String>, List<String>> {
        val result1 = pipeline1.execute(input)
        val result2 = pipeline2.execute(input)
        return Pair(result1, result2)
    }

}

// aqui tentei originalmente retornar tudo na mesma linha, mas não dá para fazer isso porque o lambda retorna um void
// então temos de criar uma variável e aplicar-lhe o lambda
fun buildPipeline(lambda: (Pipeline) -> Unit): Pipeline{
    val pipeline = Pipeline()
    lambda(pipeline)
    return pipeline

}