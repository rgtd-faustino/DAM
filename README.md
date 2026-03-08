# Assignment 5 — Building a System Info App
Course: Desenvolvimento de Aplicações Móveis
Student(s): A51394 Rafael Faustino
Date: Março 2026
Repository URL: [Inserir URL do repositório]

## 1. Introduction
O objetivo deste exercício era desenvolver uma aplicação Android simples capaz de extrair e apresentar as informações de hardware e sistema operativo do dispositivo atual (system build information). Este exercício serviu como uma introdução à exploração das APIs nativas do Android, permitindo-me compreender como uma aplicação acede aos componentes de contexto do sistema onde está a ser executada. A informação extraída devia ser unificada e apresentada no ecrã através de um componente de texto multilinha (**MultiLine TextView**).

## 2. System Overview
A "System Info App" é uma aplicação simples de ecrã único. Ao abrir, a aplicação comunica com os serviços do sistema Android subjacente, recolhe uma listagem variada de informações associadas ao ambiente de execução e à máquina física (ou virtual), e formata tudo com clareza. O produto final é semelhante à imagem de referência fornecida pelo enunciado do exercício, exibindo os dados crus diretamente na interface principal da aplicação.

## 3. Architecture and Design
A arquitetura base assenta na estrutura standard de componentes nativos Android, com uma única atividade: a `MainActivity`. A interface de utilizador (UI) no layout XML foca-se puramente na funcionalidade, incluindo apenas um `TextView` central configurado no ecrã para expansão de múltiplas linhas.

A nível de design de software estrutural, a aplicação segue uma abordagem procedural simples inserida no ciclo de vida da atividade. Assim que o método `onCreate` é disparado, todos os dados são instanciados sequencialmente a partir de classes utilitárias globais estáticas do Android e depois mapeados para a UI.

## 4. Implementation
Para a implementação do exercício, não foi necessário adicionar nenhuma dependência ou biblioteca externa (manteve-se o ambiente limpo). Utilizei apenas as bibliotecas padrão do sistema Android, nomeadamente a classe `android.os.Build` para processamento logístico e `android.widget.TextView` para a renderização visual.

Consoante o pedia o enunciado da atividade, foram declaradas variáveis focadas em aceder de forma direta aos atributos do sistema Android disponíveis na API nativa. O acesso aos dados ocorreu com o mapeamento das propriedades de sistema através das chamadas estáticas em `Build`. 

A obtenção da informação foi efetuada mapeando:
- **Manufacturer:** `Build.MANUFACTURER`
- **Brand:** `Build.BRAND`
- **Device:** `Build.DEVICE`
- **Model:** `Build.MODEL`
- **Android Version:** `Build.VERSION.RELEASE`
- **SDK Version:** `Build.VERSION.SDK_INT`
- Entre outros (Displays, Incremental Builds, etc.).

**Exemplo Prático de Código:**
Foi usada a funcionalidade de *Raw Strings* trimáveis do Kotlin (`"""..."""`) de forma a encadear as respostas numa String formatada legível contendo variadas linhas. 

```kotlin
val info = """
    Brand: ${Build.BRAND}
    Model: ${Build.MODEL}
    Manufacturer: ${Build.MANUFACTURER}
    Device: ${Build.DEVICE}
    Android Version: ${Build.VERSION.RELEASE}
    API Level: ${Build.VERSION.SDK_INT}
""".trimIndent()

val systemInfo = findViewById<TextView>(R.id.textSystemInfo)
systemInfo.setText(info)
```

## 5. Testing and Validation
Como não possuía um dispositivo físico disponível para teste com a API target, a aplicação foi totalmente simulada e testada num **emulador Android** dentro do ambiente do Android Studio. 
Durante a observação, comprovei que o resultado no emulador era autêntico (no qual o fabricante devolveu tipicamente "Google", o modelo e a board exibiam os metadados característicos da máquina virtual, etc.). Com base na verificação contínua, o texto renderizou eficazmente, suportando as diferentes linhas, de acordo com o planeado na imagem de entrega de referência.

## 6. Usage Instructions
1. Efetue o clone do repositório correspondente para a sua máquina de trabalho.
2. Na janela inicial do **Android Studio**, abra a pasta raiz do projeto.
3. Aguarde o fim do *Gradle Sync*.
4. Compile e execute o projeto utilizando o botão **Run** ou o atalho usual, focando a saída para um Emulador já inicializado.
5. O layout aparecerá automaticamente no ecrã exibindo as strings em multilinha com os dados locais do dispositivo.

# Development Process

## 12. Version Control and Commit History
O processo de desenvolvimento foi salvaguardado pelo sistema de controlo de versões Git. O percurso consistiu na:
1. Geração do *boilerplate* do app em Android Studio.
2. Ajuste do XML para conter um TextView que permitisse *scrolling* ou acomodação de longo texto.
3. Implementação da injeção de texto dinâmico recolhido da documentação de sistema no ficheiro de código `MainActivity.kt`.
4. Correções menores de legibilidade relativas às métricas extraídas para a vista final.

## 13. Difficulties and Lessons Learned
No início do exercício, não sabia propriamente como poderia invocar ou aceder com segurança às informações do dispositivo a nível de kernel ou camada aplicacional. Isto inicialmente causou alguma confusão sobre o ponto de partida do problema.

Contudo, após parar para consultar a **documentação oficial Android para desenvolvedores**, compreendi claramente a utilidade da classe de base utilitária `android.os.Build`, a qual retira este tipo de dados a partir das propriedades nativas do sistema. Com a leitura atenta da API pública da Google, pude discernir entre qualismos como `Build.VERSION.SDK_INT` versus `Build.VERSION.RELEASE`.
  
A partir do momento em que clarifiquei o conceito pela documentação, o processo em si tornou-se relativamente simplificado. Consistiu apenas em selecionar a informação de interesse pretendida a partir da classe lida, inseri-la num encapsulador de texto multilinha e invocar a mudança para a view de destino na atividade principal.  
A maior lição que retiro desta atividade corrobora que a pesquisa analítica a partir da documentação nativa é frequentemente o passo mais elementar para dissolver complexidades aparentemente intransponíveis a nível de construção arquitetural.

## 14. Future Improvements
* Refabricar a Interface Gráfica substituindo a *String* monolita num simples TextView por um componente modular como uma `RecyclerView` formatada com aspeto Chave-Valor, que confere um aspeto e comportamento mais polido de interface de sistema genuína.
* Implementar mecanismos práticos comuns destas ferramentas (como uma ação de *partilha* ou opção num canto para "*Copy properties to clipboard*").

## 15. AI Usage Disclosure
Neste trabalho, ferramentas de Inteligência Artificial foram utilizadas como suporte para garantir a coesão semântica e gramatical na elaboração do documento `README.md` explicativo, assegurando que o texto ficasse profissional e com excelente legibilidade. Todavia, refiro que toda a descoberta literária inicial e interpretação das utilidades através da documentação oficial do `android.os.Build`, bem como as reestruturações de fluxo no código, derivaram do meu esforço lógico, empírico e pensamento dedutivo original.
