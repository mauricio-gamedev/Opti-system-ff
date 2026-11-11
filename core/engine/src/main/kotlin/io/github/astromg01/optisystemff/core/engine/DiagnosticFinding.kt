package io.github.astromg01.optisystemff.core.engine

enum class FindingSeverity {
    INFO,
    ATTENTION,
    WARNING,
}

enum class EvidenceKind {
    OBSERVED,
    DERIVED,
}

data class DiagnosticFinding(
    val id: String,
    val severity: FindingSeverity,
    val title: String,
    val detail: String,
    val recommendation: String,
    val evidenceKind: EvidenceKind,
)
