package io.github.astromg01.optisystemff.core.engine

import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.ThermalState

/**
 * Converts observed device conditions into recommendations.
 *
 * This pure class never changes Android settings, kills processes, starts shell commands
 * or claims that a recommendation has improved game performance.
 */
class OptimizationAdvisor(
    private val lowRamHeadroomThreshold: Double = 0.12,
    private val constrainedRamHeadroomThreshold: Double = 0.20,
    private val lowStorageHeadroomThreshold: Double = 0.08,
) {
    fun evaluate(snapshot: DeviceSnapshot): List<DiagnosticFinding> = buildList {
        val ramRatio = snapshot.availableRamRatio
        when {
            snapshot.isSystemLowMemory -> add(
                DiagnosticFinding(
                    id = "DIAG-RAM-LOW",
                    severity = FindingSeverity.WARNING,
                    title = "Android reporta pouca memória",
                    detail = "O próprio Android sinaliza uma condição de pouca memória. A leitura é do sistema inteiro, não do consumo exclusivo do Free Fire.",
                    recommendation = "Feche manualmente aplicativos que você não vai usar e evite iniciar downloads ou gravações durante a partida.",
                    evidenceKind = EvidenceKind.OBSERVED,
                ),
            )

            ramRatio != null && ramRatio < lowRamHeadroomThreshold -> add(
                DiagnosticFinding(
                    id = "DIAG-RAM-LOW",
                    severity = FindingSeverity.WARNING,
                    title = "Pouca memória disponível",
                    detail = "A margem de RAM disponível está muito baixa nesta leitura do sistema.",
                    recommendation = "Feche manualmente apenas aplicativos que você não pretende usar. Não é recomendado encerrar processos do sistema à força.",
                    evidenceKind = EvidenceKind.OBSERVED,
                ),
            )

            ramRatio != null && ramRatio < constrainedRamHeadroomThreshold -> add(
                DiagnosticFinding(
                    id = "DIAG-RAM-CONSTRAINED",
                    severity = FindingSeverity.ATTENTION,
                    title = "Margem de memória reduzida",
                    detail = "A margem de RAM disponível está abaixo da faixa de referência deste diagnóstico.",
                    recommendation = "Evite iniciar downloads, gravações ou outros aplicativos pesados durante a sessão.",
                    evidenceKind = EvidenceKind.OBSERVED,
                ),
            )
        }

        snapshot.availableStorageRatio?.takeIf { it < lowStorageHeadroomThreshold }?.let {
            add(
                DiagnosticFinding(
                    id = "DIAG-STORAGE-LOW",
                    severity = FindingSeverity.ATTENTION,
                    title = "Armazenamento quase cheio",
                    detail = "O espaço livre da partição de dados está baixo. Isso não é uma medição direta de FPS.",
                    recommendation = "Revise arquivos grandes e mantenha uma margem livre antes de atualizar ou gravar conteúdo.",
                    evidenceKind = EvidenceKind.OBSERVED,
                ),
            )
        }

        if (snapshot.isPowerSaveMode) {
            add(
                DiagnosticFinding(
                    id = "DIAG-POWER-SAVER",
                    severity = FindingSeverity.ATTENTION,
                    title = "Economia de energia ativada",
                    detail = "O modo de economia está ativo. O impacto no jogo depende das políticas do Android e da Samsung.",
                    recommendation = "Para comparar desempenho, mantenha o mesmo estado de economia em todos os testes. Desative manualmente somente se priorizar desempenho e bateria/temperatura permitirem.",
                    evidenceKind = EvidenceKind.OBSERVED,
                ),
            )
        }

        when (snapshot.thermalState) {
            ThermalState.MODERATE -> add(
                thermalFinding(
                    severity = FindingSeverity.ATTENTION,
                    title = "Aparelho aquecendo",
                    detail = "O Android reporta estado térmico moderado. A tendência pode evoluir durante uma partida longa.",
                ),
            )

            ThermalState.SEVERE,
            ThermalState.CRITICAL,
            ThermalState.EMERGENCY,
            ThermalState.SHUTDOWN -> add(
                thermalFinding(
                    severity = FindingSeverity.WARNING,
                    title = "Condição térmica elevada",
                    detail = "O Android reporta uma condição térmica elevada. O sistema pode limitar desempenho para proteger o aparelho.",
                ),
            )

            ThermalState.UNAVAILABLE,
            ThermalState.NONE,
            ThermalState.LIGHT -> Unit
        }
    }

    private fun thermalFinding(
        severity: FindingSeverity,
        title: String,
        detail: String,
    ) = DiagnosticFinding(
        id = "DIAG-THERMAL",
        severity = severity,
        title = title,
        detail = detail,
        recommendation = "Faça uma pausa e evite carregar o aparelho durante uma sessão exigente.",
        evidenceKind = EvidenceKind.OBSERVED,
    )
}
