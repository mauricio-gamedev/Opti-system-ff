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
        snapshot.availableRamRatio?.let { ratio ->
            when {
                ratio < lowRamHeadroomThreshold -> add(
                    DiagnosticFinding(
                        id = "DIAG-RAM-LOW",
                        severity = FindingSeverity.WARNING,
                        title = "Pouca memória disponível",
                        detail = "O Android informa pouca RAM disponível no sistema. Isso não representa o consumo exclusivo do Free Fire.",
                        recommendation = "Antes da partida, feche manualmente apenas os aplicativos que você não pretende usar.",
                        evidenceKind = EvidenceKind.OBSERVED,
                    ),
                )
                ratio < constrainedRamHeadroomThreshold -> add(
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
