package net.darmo_creations.jenealogio2.model;

/**
 * Registry to manage {@link LifeEventType}s.
 */
public final class LifeEventTypeRegistry extends Registry<LifeEventType, LifeEventType.RegistryArgs> {
  /**
   * Create a new {@link LifeEventType} registry.
   */
  LifeEventTypeRegistry() {
    super(
        "life_event_types",
        (key, label, args) -> new LifeEventType(key, label, args.group(), args.indicatesDeath(), args.indicatesUnion(), args.minActors(), args.minActors(), args.isUnique()),

        new BuiltinEntry<>("annulment", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Annulation
        new BuiltinEntry<>("civil_solidarity_pact", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, true, 2, 2, false)), // PACS
        new BuiltinEntry<>("divorce", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Divorce
        new BuiltinEntry<>("engagement", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Fiançailles
        new BuiltinEntry<>("marriage", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, true, 2, 2, false)), // Mariage
        new BuiltinEntry<>("marriage_bann", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Bans de mariage
        new BuiltinEntry<>("marriage_contract", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Contrat de mariage
        new BuiltinEntry<>("marriage_license", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Licence de mariage
        new BuiltinEntry<>("partners", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, true, 2, 2, false)), // Couple non marié
        new BuiltinEntry<>("separation", new LifeEventType.RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Séparation

        new BuiltinEntry<>("acquisition", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Acquisition
        new BuiltinEntry<>("census", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Recensement
        new BuiltinEntry<>("change_of_gender", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Changement de genre
        new BuiltinEntry<>("change_of_name", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Changement de nom
        new BuiltinEntry<>("election", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Élection
        new BuiltinEntry<>("endowment", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Dotation
        new BuiltinEntry<>("emigration", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Émigration
        new BuiltinEntry<>("immigration", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Immigration
        new BuiltinEntry<>("naturalization", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Naturalisation
        new BuiltinEntry<>("occupation", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Profession
        new BuiltinEntry<>("property", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Propriété
        new BuiltinEntry<>("retirement", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Retraite
        new BuiltinEntry<>("sale_of_property", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Vente d’un bien
        new BuiltinEntry<>("will", new LifeEventType.RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Testament

        new BuiltinEntry<>("diploma", new LifeEventType.RegistryArgs(LifeEventType.Group.EDUCATION, false, false)), // Certificat
        new BuiltinEntry<>("education", new LifeEventType.RegistryArgs(LifeEventType.Group.EDUCATION, false, false)), // Éducation
        new BuiltinEntry<>("graduation", new LifeEventType.RegistryArgs(LifeEventType.Group.EDUCATION, false, false)), // Diplome

        new BuiltinEntry<>("birth", new LifeEventType.RegistryArgs(LifeEventType.Group.LIFESPAN, false, false, true)), // Naissance
        new BuiltinEntry<>("burial", new LifeEventType.RegistryArgs(LifeEventType.Group.LIFESPAN, true, false)), // Inhumation
        new BuiltinEntry<>("cremation", new LifeEventType.RegistryArgs(LifeEventType.Group.LIFESPAN, true, false, true)), // Crémation
        new BuiltinEntry<>("death", new LifeEventType.RegistryArgs(LifeEventType.Group.LIFESPAN, true, false, true)), // Décès
        new BuiltinEntry<>("funeral", new LifeEventType.RegistryArgs(LifeEventType.Group.LIFESPAN, true, false)), // Funérailles

        new BuiltinEntry<>("circumcision", new LifeEventType.RegistryArgs(LifeEventType.Group.MEDICAL, false, false)), // Circoncision
        new BuiltinEntry<>("disease", new LifeEventType.RegistryArgs(LifeEventType.Group.MEDICAL, false, false)), // Maladie
        new BuiltinEntry<>("hospitalization", new LifeEventType.RegistryArgs(LifeEventType.Group.MEDICAL, false, false)), // Hospitalisation

        new BuiltinEntry<>("conscription", new LifeEventType.RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Service militaire
        new BuiltinEntry<>("demobilization", new LifeEventType.RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Démobilisation militaire
        new BuiltinEntry<>("military_decoration", new LifeEventType.RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Distinction militaire
        new BuiltinEntry<>("military_promotion", new LifeEventType.RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Promotion militaire
        new BuiltinEntry<>("mobilization", new LifeEventType.RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Mobilisation militaire

        new BuiltinEntry<>("baptism", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Baptème
        new BuiltinEntry<>("bar_mitzvah", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Bar mitzvah
        new BuiltinEntry<>("bat_mitzvah", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Bat mitzvah
        new BuiltinEntry<>("blessing", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Bénédiction
        new BuiltinEntry<>("confirmation", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Confirmation
        new BuiltinEntry<>("excommunication", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Excommunication
        new BuiltinEntry<>("first_communion", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Première communion
        new BuiltinEntry<>("ordinance", new LifeEventType.RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Ordination

        new BuiltinEntry<>("accomplishment", new LifeEventType.RegistryArgs(LifeEventType.Group.DISTINCTION, false, false)), // Accomplissement
        new BuiltinEntry<>("medal", new LifeEventType.RegistryArgs(LifeEventType.Group.DISTINCTION, false, false)), // Décoration
        new BuiltinEntry<>("rank", new LifeEventType.RegistryArgs(LifeEventType.Group.DISTINCTION, false, false)), // Distinction

        new BuiltinEntry<>("membership", new LifeEventType.RegistryArgs(LifeEventType.Group.OTHER, false, false)), // Adhésion
        new BuiltinEntry<>("passenger_list", new LifeEventType.RegistryArgs(LifeEventType.Group.OTHER, false, false)), // Liste de passagers
        new BuiltinEntry<>("residence", new LifeEventType.RegistryArgs(LifeEventType.Group.OTHER, false, false)) // Résidence
    );
  }
}
