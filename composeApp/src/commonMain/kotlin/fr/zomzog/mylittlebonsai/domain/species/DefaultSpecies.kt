package fr.zomzog.mylittlebonsai.domain.species

/**
 * The default species catalogue shipped with the app (#82). Ids are the Latin
 * name in kebab-case, per the vault format's id convention for default entries.
 * No categories in v1: consumers show this list sorted by [sortedByLabel].
 */
object DefaultSpecies {

    val all: List<Species> = listOf(
        Species("acer-palmatum", "Acer palmatum", "Japanese maple", "Érable du Japon"),
        Species("acer-buergerianum", "Acer buergerianum", "Trident maple", "Érable trident"),
        Species("acer-campestre", "Acer campestre", "Field maple", "Érable champêtre"),
        Species("ulmus-parvifolia", "Ulmus parvifolia", "Chinese elm", "Orme de Chine"),
        Species("zelkova-serrata", "Zelkova serrata", "Japanese zelkova", "Zelkova du Japon"),
        Species("ficus-microcarpa", "Ficus microcarpa", "Chinese banyan", "Figuier de Chine"),
        Species("ficus-benjamina", "Ficus benjamina", "Weeping fig", "Figuier pleureur"),
        Species("carmona-microphylla", "Carmona microphylla", "Fukien tea", "Thé de Fukien"),
        Species("pinus-thunbergii", "Pinus thunbergii", "Japanese black pine", "Pin noir du Japon"),
        Species("pinus-parviflora", "Pinus parviflora", "Japanese white pine", "Pin blanc du Japon"),
        Species("pinus-sylvestris", "Pinus sylvestris", "Scots pine", "Pin sylvestre"),
        Species("pinus-mugo", "Pinus mugo", "Mountain pine", "Pin mugo"),
        Species("juniperus-chinensis", "Juniperus chinensis", "Chinese juniper", "Genévrier de Chine"),
        Species("juniperus-procumbens", "Juniperus procumbens", "Japanese garden juniper", "Genévrier rampant du Japon"),
        Species("juniperus-rigida", "Juniperus rigida", "Needle juniper", "Genévrier rigide"),
        Species("taxus-baccata", "Taxus baccata", "English yew", "If commun"),
        Species("chamaecyparis-obtusa", "Chamaecyparis obtusa", "Hinoki cypress", "Faux-cyprès du Japon"),
        Species("cryptomeria-japonica", "Cryptomeria japonica", "Japanese cedar", "Cryptomeria du Japon"),
        Species("larix-kaempferi", "Larix kaempferi", "Japanese larch", "Mélèze du Japon"),
        Species("larix-decidua", "Larix decidua", "European larch", "Mélèze d'Europe"),
        Species("podocarpus-macrophyllus", "Podocarpus macrophyllus", "Buddhist pine", "Podocarpe à grandes feuilles"),
        Species("fagus-sylvatica", "Fagus sylvatica", "European beech", "Hêtre commun"),
        Species("fagus-crenata", "Fagus crenata", "Japanese beech", "Hêtre du Japon"),
        Species("carpinus-betulus", "Carpinus betulus", "European hornbeam", "Charme commun"),
        Species("carpinus-turczaninowii", "Carpinus turczaninowii", "Korean hornbeam", "Charme de Corée"),
        Species("quercus-robur", "Quercus robur", "English oak", "Chêne pédonculé"),
        Species("quercus-ilex", "Quercus ilex", "Holm oak", "Chêne vert"),
        Species("prunus-mume", "Prunus mume", "Japanese apricot", "Abricotier du Japon"),
        Species("malus-sylvestris", "Malus sylvestris", "Crab apple", "Pommier sauvage"),
        Species("punica-granatum", "Punica granatum", "Pomegranate", "Grenadier"),
        Species("wisteria-floribunda", "Wisteria floribunda", "Japanese wisteria", "Glycine du Japon"),
        Species("rhododendron-indicum", "Rhododendron indicum", "Satsuki azalea", "Azalée satsuki"),
        Species("camellia-japonica", "Camellia japonica", "Japanese camellia", "Camélia du Japon"),
        Species("serissa-japonica", "Serissa japonica", "Snowrose", "Serissa"),
        Species("cotoneaster-horizontalis", "Cotoneaster horizontalis", "Rockspray cotoneaster", "Cotonéaster horizontal"),
        Species("crataegus-monogyna", "Crataegus monogyna", "Common hawthorn", "Aubépine monogyne"),
        Species("buxus-sempervirens", "Buxus sempervirens", "Common boxwood", "Buis commun"),
        Species("olea-europaea", "Olea europaea", "Olive tree", "Olivier"),
        Species("bougainvillea-glabra", "Bougainvillea glabra", "Bougainvillea", "Bougainvillier"),
        Species("portulacaria-afra", "Portulacaria afra", "Dwarf jade / elephant bush", "Portulacaire"),
    )
}
