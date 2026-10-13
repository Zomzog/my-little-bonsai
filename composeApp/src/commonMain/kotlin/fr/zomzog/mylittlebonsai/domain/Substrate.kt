package fr.zomzog.mylittlebonsai.domain

/** A substrate value is either a reference to a named mix or an inline mix of components. */
sealed interface Substrate {
    data class NamedMix(val mixId: String) : Substrate
    data class InlineMix(val components: List<SubstrateComponent>) : Substrate
}

data class SubstrateComponent(
    val soil: String,
    val percent: Int,
)
