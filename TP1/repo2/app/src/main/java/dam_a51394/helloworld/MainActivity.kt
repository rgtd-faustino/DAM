package dam_a51394.helloworld

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // tive de meter o elemento raiz linear alyout com o id main porque isto estava a procura
        // e não encontrava nenhum main
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        println(this@MainActivity.localClassName + getString(R.string.activity_oncreate_msg))


        // estes dois inputs embora sejam introduzidos numeros eles são strings à mesma por causa
        // do edit text
        val editCapital = findViewById<EditText>(R.id.editTextNumber2)
        val editAnos = findViewById<EditText>(R.id.editTextNumber4)

        val radioObrigacoes = findViewById<RadioButton>(R.id.radioButton2)
        val radioAcoes = findViewById<RadioButton>(R.id.radioButton)
        val textTaxa = findViewById<TextView>(R.id.textView5)
        val textResultado = findViewById<TextView>(R.id.textView31)
        val textLucro = findViewById<TextView>(R.id.textView32)
        val btnCalcular = findViewById<Button>(R.id.button)
        val btnPortfolio = findViewById<Button>(R.id.button5)

        // como não existe nada no modo portrair estava a dar erro então assim só corre o código
        // quando encontrar os botões
        if(editCapital == null || editAnos == null || radioObrigacoes == null ||
            radioAcoes == null || textTaxa == null || textResultado == null || textLucro == null ||
            btnCalcular == null || btnPortfolio == null)
            return

        // se o botão foi premido então mudamos o valor do texto para o que lhe corresponde
        radioObrigacoes.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked)
                textTaxa.text = "3,5% / ano"
        }

        // depois se o outro for escolhido voltamos a mudar o texto de acordo com o botao premido
        radioAcoes.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked)
                textTaxa.text = "8,0% / ano"
        }

        // se o botão for calculado apanhamos os valores que o utilizar deu input do capital e anos
        // e fazemos as contas e damos output
        btnCalcular.setOnClickListener {
            // depois aqui é que passamos de volta para numeros double e int
            val capital = editCapital.text.toString().toDoubleOrNull()
            val anos = editAnos.text.toString().toIntOrNull()

            //nenhum dos valores pode ser nulo senão não dá para fazer as contas então mostramos isso
            if (capital == null || anos == null) {
                textResultado.text = "Valores inválidos"
                // aqui dar return faz sair da função onCreate e não queremos isso então tem de ser
                // sair especificamente do click listener
                return@setOnClickListener
            }

            val taxa:Double
            if (radioObrigacoes.isChecked)
                taxa = 0.035
            else
                taxa = 0.08

            // auto complete sugere usar .pow em vez de math.pow no inicio
            val total = capital * (1 + taxa).pow(anos.toDouble())
            val lucro = total - capital
            val percentagem = (lucro / capital) * 100

            // % = valor, .2f = duas casas decimais no numero float/double, e no fim simbolo do euro
            textResultado.text = "+%.2f€".format(lucro) // lucro = ao valor a substituir o %
            // o lucro a primeira parte é igual mas depois mostramos a percentagem só com uma casa
            // decimal e como é percentagem queremos mostrar o simbolo então é dois %% para ser
            // mostrado literalmente
            // total e percentagem sao os numeros a substituir os %
            textLucro.text = "Total: %.2f€ (+%.1f%%)".format(total, percentagem)

            // metemos no log cat output para os comandos ADB depois filtrarem
            Log.i("Calcular", "Capital: ${capital} | Anos: ${anos} | Taxa: ${taxa} | " +
                    "Lucro: ${lucro}")
        }

        // output do nosso portfolio para o ADB depois filtar o logcat
        btnPortfolio.setOnClickListener {
            Log.i("Portfolio", "VWCE → 615,13€ ▲ +0,6%")
            Log.i("Portfolio", "TTWO → -20,16€ ▼ -1,3%")
            Log.i("Portfolio", "NVDA → 214,94€ ▲ +4,3%")
            Log.i("Portfolio", "BTC → -385,53€ ▼ -3,8%")
            Log.i("Portfolio", "Gold → 825,62€ ▲ +3,1%")
        }

        // quando o utilizador clica no botão aparece uma notificaçãozinha por cima da app a dizer o
        // seu portfolio total
        btnPortfolio.setOnClickListener {
            Toast.makeText(this, "Portfolio total: 1,250€ (+3,9%)",
                Toast.LENGTH_SHORT).show()
        }
    }


}