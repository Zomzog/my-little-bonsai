package fr.zomzog.mylittlebonsai.domain

enum class DoseUnit { G, ML, G_PER_L, ML_PER_L }

data class FertilizerDose(
    val amount: Double,
    val unit: DoseUnit,
)

/** One entry of a session's `actions` list. Every action carries an optional free-text [note]. */
sealed interface Action {
    val note: String?

    data class Measuring(
        val height: Double? = null,
        val width: Double? = null,
        val nebari: Double? = null,
        val weight: Double? = null,
        override val note: String? = null,
    ) : Action

    data class Repotting(
        val substrate: Substrate? = null,
        val pot: String? = null,
        override val note: String? = null,
    ) : Action

    data class Fertilizing(
        val fertilizer: String,
        val dose: FertilizerDose? = null,
        override val note: String? = null,
    ) : Action

    data class Treatment(
        val treatment: String,
        val target: String? = null,
        override val note: String? = null,
    ) : Action

    /** Any action id that is not one of the four typed actions above; it carries only [note]. */
    data class Other(
        val actionId: String,
        override val note: String? = null,
    ) : Action
}
