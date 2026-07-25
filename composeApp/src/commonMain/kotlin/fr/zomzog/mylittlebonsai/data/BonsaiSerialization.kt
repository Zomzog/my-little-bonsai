package fr.zomzog.mylittlebonsai.data

import fr.zomzog.mylittlebonsai.domain.Bonsai
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Storage representation of [Bonsai].
 *
 * Dates are held as ISO-8601 strings rather than `LocalDate` so the stored payload
 * stays independent of kotlinx-datetime's serializer artifacts.
 */
@Serializable
internal data class BonsaiDto(
    val id: String,
    val name: String,
    val kind: String,
    val purchaseDate: String,
    val lastMaintenanceDate: String? = null,
)

internal object BonsaiSerialization {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(BonsaiDto.serializer())

    fun encode(bonsais: List<Bonsai>): String =
        json.encodeToString(serializer, bonsais.map { it.toDto() })

    /**
     * Decodes a previously encoded payload, returning an empty list when the stored
     * value is missing, truncated or otherwise unreadable — a corrupt entry must not
     * prevent the app from starting.
     */
    fun decode(raw: String): List<Bonsai> = runCatching {
        json.decodeFromString(serializer, raw).map { it.toDomain() }
    }.getOrElse { emptyList() }
}

private fun Bonsai.toDto() = BonsaiDto(
    id = id,
    name = name,
    kind = kind,
    purchaseDate = purchaseDate.toString(),
    lastMaintenanceDate = lastMaintenanceDate?.toString(),
)

private fun BonsaiDto.toDomain() = Bonsai(
    id = id,
    name = name,
    kind = kind,
    purchaseDate = LocalDate.parse(purchaseDate),
    lastMaintenanceDate = lastMaintenanceDate?.let { LocalDate.parse(it) },
)
