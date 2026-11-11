package io.github.astromg01.optisystemff

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.astromg01.optisystemff.core.diagnostics.DeviceDiagnostics
import io.github.astromg01.optisystemff.core.engine.DiagnosticFinding
import io.github.astromg01.optisystemff.core.engine.FindingSeverity
import io.github.astromg01.optisystemff.core.engine.OptimizationAdvisor
import io.github.astromg01.optisystemff.core.engine.SessionComparison
import io.github.astromg01.optisystemff.core.engine.SessionComparisonEngine
import io.github.astromg01.optisystemff.core.games.GameCatalog
import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.GameEdition
import io.github.astromg01.optisystemff.core.model.GameSession
import io.github.astromg01.optisystemff.core.model.ThermalState
import io.github.astromg01.optisystemff.core.session.GameSessionStore
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val diagnostics by lazy { DeviceDiagnostics(this) }
    private val gameCatalog by lazy { GameCatalog(this) }
    private val advisor by lazy { OptimizationAdvisor() }
    private val sessionStore by lazy { GameSessionStore(this) }
    private val resumeSequence = mutableIntStateOf(0)

    override fun onResume() {
        super.onResume()
        resumeSequence.intValue += 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OptiSystemTheme {
                var snapshot by remember { mutableStateOf(diagnostics.capture()) }
                var installedGames by remember { mutableStateOf(gameCatalog.installedGames()) }
                var activeSession by remember { mutableStateOf(sessionStore.activeSession()) }
                var recentSessions by remember { mutableStateOf(sessionStore.recentSessions()) }
                val findings = remember(snapshot) { advisor.evaluate(snapshot) }
                val resumeToken = resumeSequence.intValue

                LaunchedEffect(resumeToken) {
                    snapshot = diagnostics.capture()
                    installedGames = gameCatalog.installedGames()
                    activeSession = sessionStore.activeSession()
                    recentSessions = sessionStore.recentSessions()
                }

                DashboardScreen(
                    snapshot = snapshot,
                    findings = findings,
                    installedGames = installedGames,
                    activeSession = activeSession,
                    recentSessions = recentSessions,
                    onRefresh = {
                        snapshot = diagnostics.capture()
                        installedGames = gameCatalog.installedGames()
                        activeSession = sessionStore.activeSession()
                        recentSessions = sessionStore.recentSessions()
                    },
                    onStartSession = { game ->
                        val launchIntent = gameCatalog.launchIntent(game)
                        if (launchIntent == null) {
                            Toast.makeText(
                                this,
                                game.displayName + " não está instalado ou não pode ser aberto.",
                                Toast.LENGTH_LONG,
                            ).show()
                        } else {
                            val startSnapshot = diagnostics.capture()
                            val session = sessionStore.startSession(game, startSnapshot)
                            if (session == null) {
                                activeSession = sessionStore.activeSession()
                                Toast.makeText(
                                    this,
                                    "Já existe uma sessão em andamento. Finalize-a antes de iniciar outra.",
                                    Toast.LENGTH_LONG,
                                ).show()
                            } else {
                                activeSession = session
                                snapshot = startSnapshot
                                runCatching { startActivity(launchIntent) }
                                    .onFailure {
                                        sessionStore.discardActiveSession()
                                        activeSession = null
                                        Toast.makeText(
                                            this,
                                            "Não foi possível abrir o jogo. A sessão foi descartada.",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                            }
                        }
                    },
                    onFinishSession = {
                        val completed = sessionStore.finishSession(diagnostics.capture())
                        activeSession = sessionStore.activeSession()
                        recentSessions = sessionStore.recentSessions()
                        snapshot = diagnostics.capture()
                        if (completed != null) {
                            Toast.makeText(
                                this,
                                "Sessão registrada no histórico.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    snapshot: DeviceSnapshot,
    findings: List<DiagnosticFinding>,
    installedGames: Set<GameEdition>,
    activeSession: GameSession?,
    recentSessions: List<GameSession>,
    onRefresh: () -> Unit,
    onStartSession: (GameEdition) -> Unit,
    onFinishSession: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val comparisonEngine = remember { SessionComparisonEngine() }

    Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "OPTI SYSTEM FF",
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Desempenho com evidência.",
                    color = colors.onBackground,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Device Intelligence + Session Lab • 0.2.0",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }

            DeviceOverview(snapshot)

            if (activeSession != null) {
                ActiveSessionCard(session = activeSession, onFinish = onFinishSession)
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading(
                    title = "Seus jogos",
                    subtitle = if (activeSession == null) {
                        "Inicie uma sessão para comparar o estado do sistema antes e depois da partida."
                    } else {
                        "Finalize a sessão atual antes de registrar outra."
                    },
                )
                GameEdition.entries.forEach { game ->
                    GameCard(
                        game = game,
                        installed = game in installedGames,
                        canStart = activeSession == null,
                        onStart = { onStartSession(game) },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading(
                    title = "Preparação pré-partida",
                    subtitle = "Diagnóstico baseado em sinais públicos do Android.",
                )
                if (findings.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                "Nenhum alerta nesta leitura",
                                color = colors.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Isso não garante FPS estável. O diagnóstico não mede o processo do jogo.",
                                color = colors.onSurfaceVariant,
                                fontSize = 13.sp,
                            )
                        }
                    }
                } else {
                    findings.forEach { finding -> FindingCard(finding) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading(
                    title = "Histórico de sessões",
                    subtitle = "Comparação de snapshots do sistema. O histórico fica salvo neste aparelho.",
                )
                if (recentSessions.isEmpty()) {
                    Text(
                        "Nenhuma sessão registrada ainda. Inicie uma partida pelo painel para criar a primeira comparação.",
                        color = colors.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                } else {
                    recentSessions.forEach { session ->
                        SessionHistoryCard(
                            session = session,
                            comparison = comparisonEngine.compare(session),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading(
                    title = "Motores do projeto",
                    subtitle = "Recursos implementados e limites declarados.",
                )
                EngineStatusCard(
                    "Device Intelligence",
                    "Memória do sistema, SoC reportado, energia, bateria, tela e temperatura.",
                    "ATIVO",
                )
                EngineStatusCard(
                    "Game Session Lab",
                    "Snapshots antes/depois, comparação e histórico local limitado.",
                    "ATIVO",
                )
                EngineStatusCard(
                    "Performance Director",
                    "Orquestração adaptativa de ações ainda em desenvolvimento.",
                    "EM DESENVOLVIMENTO",
                )
                EngineStatusCard(
                    "Shizuku Bridge",
                    "Integração avançada opcional; ainda não conectada.",
                    "PLANEJADO",
                )
            }

            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color(0xFF07110D),
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Atualizar diagnóstico", fontWeight = FontWeight.Bold)
            }

            Text(
                text = "A coleta é sob demanda. O app registra snapshots e não altera configurações do sistema, não encerra processos e não modifica arquivos dos jogos. FPS/frametime reais ainda não são medidos.",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DeviceOverview(snapshot: DeviceSnapshot) {
    val colors = MaterialTheme.colorScheme
    val socLabel = listOfNotNull(snapshot.socManufacturer, snapshot.socModel)
        .joinToString(" ")
        .ifBlank { snapshot.hardware ?: "Não reportado pelo Android" }
    val refreshLabel = snapshot.currentRefreshRateHz?.let { formatHertz(it) } ?: "Indisponível"
    val supportedLabel = snapshot.supportedRefreshRatesHz
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" / ") { formatHertz(it) }
        ?: "Não informado"

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = (snapshot.manufacturer + " " + snapshot.model).trim(),
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text(
                    text = "Android " + snapshot.androidRelease + " • API " + snapshot.apiLevel,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                Text("SoC: $socLabel", color = colors.onSurfaceVariant, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Metric(
                    modifier = Modifier.weight(1f),
                    label = "RAM disponível",
                    value = formatBytes(snapshot.availableRamBytes),
                    supporting = "de " + formatBytes(snapshot.totalRamBytes),
                )
                Metric(
                    modifier = Modifier.weight(1f),
                    label = "Espaço livre",
                    value = formatBytes(snapshot.availableStorageBytes),
                    supporting = "de " + formatBytes(snapshot.totalStorageBytes),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Metric(
                    modifier = Modifier.weight(1f),
                    label = "Tela agora",
                    value = refreshLabel,
                    supporting = "Suportados: $supportedLabel",
                )
                Metric(
                    modifier = Modifier.weight(1f),
                    label = "Bateria",
                    value = snapshot.batteryPercent?.let { "$it%" } ?: "Indisponível",
                    supporting = if (snapshot.isPowerSaveMode) {
                        "Economia de energia ligada"
                    } else {
                        "Economia de energia desligada"
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Estado térmico", color = colors.onSurfaceVariant, fontSize = 13.sp)
                Text(
                    text = snapshot.thermalState.toDisplayLabel(),
                    color = thermalColor(snapshot.thermalState),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = buildString {
                    append(if (snapshot.isLowRamDevice) "Perfil de pouca RAM" else "Perfil de RAM padrão")
                    append(" • ")
                    append(if (snapshot.isSystemLowMemory) "Android sinaliza pouca memória" else "Sem sinal de pouca memória")
                },
                color = if (snapshot.isSystemLowMemory) Color(0xFFFF817E) else colors.onSurfaceVariant,
                fontSize = 12.sp,
            )
            Text(
                text = "A taxa da tela não é o FPS do Free Fire. A RAM exibida é do sistema inteiro, não do processo do jogo.",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun ActiveSessionCard(session: GameSession, onFinish: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                "Sessão em andamento",
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                session.gameEdition.displayName + " • iniciada " + formatTimestamp(session.startedAtEpochMillis),
                color = colors.onSurface,
                fontSize = 13.sp,
            )
            Text(
                "Quando voltar da partida, toque em finalizar para registrar a segunda leitura do aparelho.",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color(0xFF07110D),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Finalizar sessão e comparar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(
    session: GameSession,
    comparison: SessionComparison?,
) {
    val colors = MaterialTheme.colorScheme
    val endSnapshot = session.endSnapshot
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                session.gameEdition.displayName,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                formatTimestamp(session.startedAtEpochMillis) +
                    " • " + (comparison?.durationMillis?.let(::formatDuration) ?: "Sessão incompleta"),
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
            )
            if (comparison != null && endSnapshot != null) {
                Text(
                    "RAM disponível do sistema: " +
                        formatSignedBytes(comparison.availableRamDeltaBytes),
                    color = colors.onSurface,
                    fontSize = 12.sp,
                )
                Text(
                    "Temperatura do sistema: " +
                        session.startSnapshot.thermalState.toDisplayLabel() +
                        " → " + endSnapshot.thermalState.toDisplayLabel(),
                    color = if ((comparison.thermalSeverityDelta ?: 0) > 0) {
                        Color(0xFFFFC66D)
                    } else {
                        colors.onSurfaceVariant
                    },
                    fontSize = 12.sp,
                )
                if (comparison.systemLowMemoryBecameActive) {
                    Text(
                        "O Android passou a sinalizar pouca memória.",
                        color = Color(0xFFFF817E),
                        fontSize = 12.sp,
                    )
                }
                if (comparison.powerSaveModeChanged) {
                    Text(
                        "O modo de economia de energia mudou durante a sessão.",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
            }
            Text(
                "Variação entre leituras do sistema; não atribui a mudança apenas ao jogo e não representa FPS.",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun Metric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    supporting: String,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(
                color = colors.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = colors.onSurfaceVariant, fontSize = 11.sp)
        Text(value, color = colors.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(supporting, color = colors.onSurfaceVariant, fontSize = 10.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun GameCard(
    game: GameEdition,
    installed: Boolean,
    canStart: Boolean,
    onStart: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(game.displayName, color = colors.onSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (installed) "Instalado" else "Não encontrado",
                    color = if (installed) colors.primary else colors.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            OutlinedButton(
                onClick = onStart,
                enabled = installed && canStart,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Iniciar sessão")
            }
        }
    }
}

@Composable
private fun FindingCard(finding: DiagnosticFinding) {
    val colors = MaterialTheme.colorScheme
    val accent = when (finding.severity) {
        FindingSeverity.INFO -> colors.primary
        FindingSeverity.ATTENTION -> Color(0xFFFFC66D)
        FindingSeverity.WARNING -> Color(0xFFFF817E)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(finding.title, color = accent, fontWeight = FontWeight.Bold)
            Text(finding.detail, color = colors.onSurface, fontSize = 13.sp)
            Text(
                finding.recommendation,
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun EngineStatusCard(title: String, description: String, state: String) {
    val colors = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = colors.onSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    state,
                    color = if (state == "ATIVO") colors.primary else colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(description, color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun OptiSystemTheme(content: @Composable () -> Unit) {
    val scheme = androidx.compose.material3.darkColorScheme(
        primary = Color(0xFF70E7AD),
        onPrimary = Color(0xFF07110D),
        background = Color(0xFF080C11),
        surface = Color(0xFF121922),
        surfaceVariant = Color(0xFF202B36),
        onSurface = Color(0xFFEAF1F6),
        onBackground = Color(0xFFEAF1F6),
        onSurfaceVariant = Color(0xFF9BAAB8),
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

private fun ThermalState.toDisplayLabel(): String = when (this) {
    ThermalState.UNAVAILABLE -> "Indisponível"
    ThermalState.NONE -> "Normal"
    ThermalState.LIGHT -> "Leve"
    ThermalState.MODERATE -> "Moderado"
    ThermalState.SEVERE -> "Elevado"
    ThermalState.CRITICAL -> "Crítico"
    ThermalState.EMERGENCY -> "Emergência"
    ThermalState.SHUTDOWN -> "Desligamento"
}

private fun thermalColor(state: ThermalState): Color = when (state) {
    ThermalState.MODERATE -> Color(0xFFFFC66D)
    ThermalState.SEVERE,
    ThermalState.CRITICAL,
    ThermalState.EMERGENCY,
    ThermalState.SHUTDOWN -> Color(0xFFFF817E)
    else -> Color(0xFF70E7AD)
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "Indisponível"
    val megabytes = bytes / (1024.0 * 1024.0)
    val gigabytes = megabytes / 1024.0
    return when {
        gigabytes >= 1.0 -> String.format(Locale.getDefault(), "%.1f GB", gigabytes)
        megabytes >= 1.0 -> String.format(Locale.getDefault(), "%.0f MB", megabytes)
        else -> String.format(Locale.getDefault(), "%.0f KB", bytes / 1024.0)
    }
}

private fun formatSignedBytes(bytes: Long): String {
    if (bytes == 0L) return "0 B"
    val sign = if (bytes > 0L) "+" else "−"
    return sign + formatBytes(kotlin.math.abs(bytes))
}

private fun formatHertz(value: Float): String =
    String.format(Locale.getDefault(), "%.0f Hz", value)

private fun formatTimestamp(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT,
        Locale("pt", "BR"),
    ).format(Date(epochMillis))

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1_000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
    } else {
        String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds)
    }
}
