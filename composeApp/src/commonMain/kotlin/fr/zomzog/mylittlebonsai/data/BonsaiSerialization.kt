package fr.zomzog.mylittlebonsai.data

import fr.zomzog.mylittlebonsai.domain.ArchivedInfo
import fr.zomzog.mylittlebonsai.domain.ArchivedReason
import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiAge
import fr.zomzog.mylittlebonsai.domain.BonsaiStatus
import fr.zomzog.mylittlebonsai.domain.Substrate
import fr.zomzog.mylittlebonsai.domain.SubstrateComponent
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Storage representation of [Bonsai].
 *
 * Dates are held as ISO-8601 strings rather than `LocalDate` so the stored payload
 * stays independent of kotlinx-datetime's serializer artifacts. The sealed [BonsaiAge]
 * and [Substrate] types are flattened into optional fields rather than serialized
 * polymorphically, matching the field-based shape they have in the vault format.
 */
@Serializable
internal data class SubstrateComponentDto(val soil: String, val percent: Int)

@Serializable
internal data class BonsaiDto(
    val id: String,
    val name: String,
    val addedOn: String,
    val species: String? = null,
    val style: String? = null,
    val status: String = BonsaiStatus.ACTIVE.name,
    val archivedReason: String? = null,
    val archivedDate: String? = null,
    val archivedNote: String? = null,
    val ageBirthday: String? = null,
    val ageYears: Int? = null,
    val substrateMixId: String? = null,
    val substrateComponents: List<SubstrateComponentDto>? = null,
    val pot: String? = null,
    val cover: String? = null,
    val description: String = "",
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

private fun Bonsai.toDto(): BonsaiDto {
    val substrate = substrate
    return BonsaiDto(
        id = id,
        name = name,
        addedOn = addedOn.toString(),
        species = species,
        style = style,
        status = status.name,
        archivedReason = archived?.reason?.name,
        archivedDate = archived?.date?.toString(),
        archivedNote = archived?.note,
        ageBirthday = (age as? BonsaiAge.Birthday)?.value,
        ageYears = (age as? BonsaiAge.Years)?.years,
        substrateMixId = (substrate as? Substrate.NamedMix)?.mixId,
        substrateComponents = (substrate as? Substrate.InlineMix)?.components?.map {
            SubstrateComponentDto(it.soil, it.percent)
        },
        pot = pot,
        cover = cover,
        description = description,
    )
}

private fun BonsaiDto.toDomain(): Bonsai = Bonsai(
    id = id,
    name = name,
    addedOn = LocalDate.parse(addedOn),
    species = species,
    style = style,
    status = BonsaiStatus.valueOf(status),
    archived = archivedReason?.let {
        ArchivedInfo(
            reason = ArchivedReason.valueOf(it),
            date = LocalDate.parse(requireNotNull(archivedDate)),
            note = archivedNote,
        )
    },
    age = ageBirthday?.let { BonsaiAge.Birthday(it) } ?: ageYears?.let { BonsaiAge.Years(it) },
    substrate = substrateMixId?.let { Substrate.NamedMix(it) }
        ?: substrateComponents?.let { components ->
            Substrate.InlineMix(components.map { SubstrateComponent(it.soil, it.percent) })
        },
    pot = pot,
    cover = cover,
    description = description,
)
