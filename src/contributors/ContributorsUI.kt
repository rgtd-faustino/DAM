package contributors

import kotlinx.coroutines.Job
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val INSETS = Insets(3, 10, 3, 10)
private val COLUMNS = arrayOf("Login", "Contributions")

@Suppress("CONFLICTING_INHERITED_JVM_DECLARATIONS")
class ContributorsUI : JFrame("GitHub Contributors"), Contributors {
    private val username = JTextField(20)
    private val password = JPasswordField(20)
    private val org = JTextField(20)
    private val variant = JComboBox(Variant.values())
    private val load = JButton("Load contributors")
    private val cancel = JButton("Cancel").apply { isEnabled = false }

    private val resultsModel = DefaultTableModel(COLUMNS, 0)
    private val results = JTable(resultsModel)
    private val resultsScroll = JScrollPane(results).apply {
        preferredSize = Dimension(200, 200)
    }

    private val loadingIcon = ImageIcon(javaClass.classLoader.getResource("ajax-loader.gif"))
    private val loadingStatus = JLabel("Start new loading", loadingIcon, SwingConstants.CENTER)

    override val job = Job()

    // o _loadingState é mutável e privado, apenas esta classe é que pode escrever nele
    // o loadingState é a versão pública e imutável que as outras classes podem observar (mas não alterar)
    private val _loadingState = MutableStateFlow(Contributors.LoadingStateData())
    override val loadingState: StateFlow<Contributors.LoadingStateData> = _loadingState.asStateFlow()

    // em vez de chamar setLoadingStatus diretamente agora usamos um coletor do StateFlow
    // que reage automaticamente a mudanças de estado
    override fun observeLoadingStatus() {
        launch {
            loadingState.collect { status ->
                val text = "Loading status: " + when (status.status) {
                    Contributors.LoadingStatus.COMPLETED -> "completed in ${status.elapsedTime}"
                    Contributors.LoadingStatus.IN_PROGRESS -> "in progress ${status.elapsedTime}"
                    Contributors.LoadingStatus.CANCELED -> "canceled"
                    Contributors.LoadingStatus.INIT -> "init"
                }
                loadingStatus.text = text
                loadingStatus.icon = if (status.status == Contributors.LoadingStatus.IN_PROGRESS) loadingIcon else null
            }
        }
    }

    init {
        // chamamos o coletor do flow antes de construir a UI para não perder nenhum estado
        observeLoadingStatus()

        // Create UI
        rootPane.contentPane = JPanel(GridBagLayout()).apply {
            addLabeled("GitHub Username", username)
            addLabeled("Password/Token", password)
            addWideSeparator()
            addLabeled("Organization", org)
            addLabeled("Variant", variant)
            addWideSeparator()
            addWide(JPanel().apply {
                add(load)
                add(cancel)
            })
            addWide(resultsScroll) {
                weightx = 1.0
                weighty = 1.0
                fill = GridBagConstraints.BOTH
            }
            addWide(loadingStatus)
        }
        // Initialize actions
        init()
    }

    // em vez de atualizar a UI diretamente aqui metemos o novo estado no StateFlow para que o coletor em
    // observeLoadingStatus() trate do resto, assim este método não precisa de saber nada sobre a UI,
    // só emite o estado novo
    override fun updateLoadingStatus(newStatus: Contributors.LoadingStateData) {
        _loadingState.value = newStatus
    }

    override fun getSelectedVariant(): Variant = variant.getItemAt(variant.selectedIndex)

    override fun updateContributors(users: List<User>) {
        if (users.isNotEmpty()) {
            log.info("Updating result with ${users.size} rows")
        }
        else {
            log.info("Clearing result")
        }
        resultsModel.setDataVector(users.map {
            arrayOf<Any>(it.login, it.contributions)
        }.toTypedArray(), COLUMNS)
    }

    // o setLoadingStatus foi removido porque a atualização da UI é agora feita pelo StateFlow
    // no observeLoadingStatus()
    /*override fun setLoadingStatus(text: String, iconRunning: Boolean) {
        loadingStatus.text = text
        loadingStatus.icon = if (iconRunning) loadingIcon else null
    }*/

    override fun addCancelListener(listener: ActionListener) {
        cancel.addActionListener(listener)
    }

    override fun removeCancelListener(listener: ActionListener) {
        cancel.removeActionListener(listener)
    }

    override fun addLoadListener(listener: () -> Unit) {
        load.addActionListener { listener() }
    }

    override fun addOnWindowClosingListener(listener: () -> Unit) {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                listener()
            }
        })
    }

    override fun setActionsStatus(newLoadingEnabled: Boolean, cancellationEnabled: Boolean) {
        load.isEnabled = newLoadingEnabled
        cancel.isEnabled = cancellationEnabled
    }

    override fun setParams(params: Params) {
        username.text = params.username
        password.text = params.password
        org.text = params.org
        variant.selectedIndex = params.variant.ordinal
    }

    override fun getParams(): Params {
        return Params(username.text, password.password.joinToString(""), org.text, getSelectedVariant())
    }
}

fun JPanel.addLabeled(label: String, component: JComponent) {
    add(JLabel(label), GridBagConstraints().apply {
        gridx = 0
        insets = INSETS
    })
    add(component, GridBagConstraints().apply {
        gridx = 1
        insets = INSETS
        anchor = GridBagConstraints.WEST
        fill = GridBagConstraints.HORIZONTAL
        weightx = 1.0
    })
}

fun JPanel.addWide(component: JComponent, constraints: GridBagConstraints.() -> Unit = {}) {
    add(component, GridBagConstraints().apply {
        gridx = 0
        gridwidth = 2
        insets = INSETS
        constraints()
    })
}

fun JPanel.addWideSeparator() {
    addWide(JSeparator()) {
        fill = GridBagConstraints.HORIZONTAL
    }
}

fun setDefaultFontSize(size: Float) {
    for (key in UIManager.getLookAndFeelDefaults().keys.toTypedArray()) {
        if (key.toString().lowercase().contains("font")) {
            val font = UIManager.getDefaults().getFont(key) ?: continue
            val newFont = font.deriveFont(size)
            UIManager.put(key, newFont)
        }
    }
}