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
import androidx.compose.runtime.getValue
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
import io.github.astromg01.optisystemff.core.games.GameCatalog
import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.GameEdition
import io.github.astromg01.optisystemff.core.model.ThermalState
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val diagnostics by lazy { DeviceDiagnostics(this) }
    private val gameCatalog by lazy { GameCatalog(this) }
    private val advisor by lazy { OptimizationAdvisor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OptiSystemTheme {
                var snapshot by remember { mutableStateOf(diagnostics.capture()) }
                var installedGames by remember { mutableStateOf(gameCatalog.installedGames()) }
                val findings = remember(snapshot) { advisor.evaluate(snapshot) }

                DashboardScreen(
                    snapshot = snapshot,
                    findings = findings,
                    installedGames = installedGames,
                    onRefresh = {
                        snapshot = diagnostics.capture()
                        installedGames = gameCatalog.installedGames()
                    },
                    onLaunchGame = { game ->
                        val intent = gameCatalog.launchIntent(game)
                        if (intent != null) {
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this,
                                game.displayName + " não está instalado ou não pode ser aberto.",
                                Toast.LENGTH_LONG,
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
    onRefresh: () -> Unit,
    onLaunchGame: (GameEdition) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

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
                    text = "Painel de diagnóstico • Fundação 0.1.0",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }

            DeviceOverview(snapshot)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading("Seus jogos", "Perfis independentes para cada edição.")
                GameEdition.entries.forEach { game ->
                    GameCard(
                        game = game,
                        installed = game in installedGames,
                        onLaunch = { onLaunchGame(game) },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeading(
                    "Análise do aparelho",
                    "Leituras públicas do Android; sem alterar configurações.",
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
                                "Isso não é uma certificação de FPS ou desempenho dentro do jogo.",
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
                    "Motores do projeto",
                    "Os módulos evoluem separadamente e passam por validação.",
                )
                EngineStatusCard(
                    "Device Diagnostics",
                    "RAM disponível, armazenamento e estado térmico.",
                    "ATIVO",
                )
                EngineStatusCard(
                    "Game Profiles",
                    "Detecção e abertura dos dois jogos oficiais.",
                    "ATIVO",
                )
                EngineStatusCard(
                    "Performance Director",
                    "Orquestração adaptativa e aplicação de políticas.",
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
                text = "Esta versão é somente leitura. Nenhum tweak é aplicado automaticamente, nenhum processo é encerrado e nenhum arquivo dos jogos é modificado.",
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
        Text(supporting, color = colors.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun GameCard(
    game: GameEdition,
    installed: Boolean,
    onLaunch: () -> Unit,
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
                onClick = onLaunch,
                enabled = installed,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Abrir")
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
    val gib = bytes / (1024.0 * 1024.0 * 1024.0)
    return String.format(Locale.getDefault(), "%.1f GB", gib)
}
