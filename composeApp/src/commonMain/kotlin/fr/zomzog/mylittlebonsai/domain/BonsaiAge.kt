package fr.zomzog.mylittlebonsai.domain

/**
 * Exactly one of [Birthday] (`YYYY-MM-DD` or partial `YYYY-MM`) or [Years] (frozen,
 * does not grow), per the vault format's `age` field.
 */
sealed interface BonsaiAge {
    /** [value] is the raw ISO date, either full (`YYYY-MM-DD`) or partial (`YYYY-MM`). */
    data class Birthday(val value: String) : BonsaiAge
    data class Years(val years: Int) : BonsaiAge
}
