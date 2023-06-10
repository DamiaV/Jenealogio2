package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.LifeEventType.RegistryArgs;
import net.darmo_creations.jenealogio2.model.Registry.BuiltinEntry;

/**
 * This class declares all registries used throughout the app.
 */
public final class Registries {
  /**
   * Registry declaring all available person genders.
   */
  public static final Registry<Gender, String> GENDERS =
      new Registry<>(
          "genders",
          Gender::new,

          new BuiltinEntry<>("agender", "#000000"),
          new BuiltinEntry<>("female", "#ee8434"),
          new BuiltinEntry<>("gender_fluid", "#bf17d5"),
          new BuiltinEntry<>("male", "#00b69e"),
          new BuiltinEntry<>("non_binary", "#fff430")
      );

  /**
   * Registry declaring all available life event types.
   */
  public static final Registry<LifeEventType, RegistryArgs> LIFE_EVENT_TYPES =
      new Registry<>(
          "life_event_types",
          (key, args) -> new LifeEventType(key, args.group(), args.indicatesDeath(), args.indicatesUnion(), args.minActors(), args.minActors(), args.isUnique()),

          new BuiltinEntry<>("annulment", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Annulation
          new BuiltinEntry<>("civil_solidarity_pact", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, true, 2, 2, false)), // PACS
          new BuiltinEntry<>("divorce", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Divorce
          new BuiltinEntry<>("engagement", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Fiançailles
          new BuiltinEntry<>("marriage", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, true, 2, 2, false)), // Mariage
          new BuiltinEntry<>("marriage_bann", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Bans de mariage
          new BuiltinEntry<>("marriage_contract", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Contrat de mariage
          new BuiltinEntry<>("marriage_license", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Licence de mariage
          new BuiltinEntry<>("partners", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, true, 2, 2, false)), // Couple non marié
          new BuiltinEntry<>("separation", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, false, 2, 2, false)), // Séparation

          new BuiltinEntry<>("acquisition", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Acquisition
          new BuiltinEntry<>("census", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Recensement
          new BuiltinEntry<>("change_of_gender", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Changement de genre
          new BuiltinEntry<>("change_of_name", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Changement de nom
          new BuiltinEntry<>("election", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Élection
          new BuiltinEntry<>("endowment", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Dotation
          new BuiltinEntry<>("emigration", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Émigration
          new BuiltinEntry<>("immigration", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Immigration
          new BuiltinEntry<>("naturalization", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Naturalisation
          new BuiltinEntry<>("occupation", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Profession
          new BuiltinEntry<>("property", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Propriété
          new BuiltinEntry<>("retirement", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Retraite
          new BuiltinEntry<>("sale_of_property", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Vente d’un bien
          new BuiltinEntry<>("will", new RegistryArgs(LifeEventType.Group.ADMIN, false, false)), // Testament

          new BuiltinEntry<>("diploma", new RegistryArgs(LifeEventType.Group.EDUCATION, false, false)), // Certificat
          new BuiltinEntry<>("education", new RegistryArgs(LifeEventType.Group.EDUCATION, false, false)), // Éducation
          new BuiltinEntry<>("graduation", new RegistryArgs(LifeEventType.Group.EDUCATION, false, false)), // Diplome

          new BuiltinEntry<>("birth", new RegistryArgs(LifeEventType.Group.LIFESPAN, false, false, true)), // Naissance
          new BuiltinEntry<>("burial", new RegistryArgs(LifeEventType.Group.LIFESPAN, true, false)), // Inhumation
          new BuiltinEntry<>("cremation", new RegistryArgs(LifeEventType.Group.LIFESPAN, true, false, true)), // Crémation
          new BuiltinEntry<>("death", new RegistryArgs(LifeEventType.Group.LIFESPAN, true, false, true)), // Décès
          new BuiltinEntry<>("funeral", new RegistryArgs(LifeEventType.Group.LIFESPAN, true, false)), // Funérailles

          new BuiltinEntry<>("circumcision", new RegistryArgs(LifeEventType.Group.MEDICAL, false, false)), // Circoncision
          new BuiltinEntry<>("disease", new RegistryArgs(LifeEventType.Group.MEDICAL, false, false)), // Maladie
          new BuiltinEntry<>("hospitalization", new RegistryArgs(LifeEventType.Group.MEDICAL, false, false)), // Hospitalisation

          new BuiltinEntry<>("conscription", new RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Service militaire
          new BuiltinEntry<>("demobilization", new RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Démobilisation militaire
          new BuiltinEntry<>("military_decoration", new RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Distinction militaire
          new BuiltinEntry<>("military_promotion", new RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Promotion militaire
          new BuiltinEntry<>("mobilization", new RegistryArgs(LifeEventType.Group.MILITARY, false, false)), // Mobilisation militaire

          new BuiltinEntry<>("baptism", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Baptème
          new BuiltinEntry<>("bar_mitzvah", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Bar mitzvah
          new BuiltinEntry<>("bat_mitzvah", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Bat mitzvah
          new BuiltinEntry<>("blessing", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Bénédiction
          new BuiltinEntry<>("confirmation", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Confirmation
          new BuiltinEntry<>("excommunication", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Excommunication
          new BuiltinEntry<>("first_communion", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Première communion
          new BuiltinEntry<>("ordinance", new RegistryArgs(LifeEventType.Group.RELIGION, false, false)), // Ordination

          new BuiltinEntry<>("accomplishment", new RegistryArgs(LifeEventType.Group.DISTINCTION, false, false)), // Accomplissement
          new BuiltinEntry<>("medal", new RegistryArgs(LifeEventType.Group.DISTINCTION, false, false)), // Décoration
          new BuiltinEntry<>("rank", new RegistryArgs(LifeEventType.Group.DISTINCTION, false, false)), // Distinction

          new BuiltinEntry<>("membership", new RegistryArgs(LifeEventType.Group.OTHER, false, false)), // Adhésion
          new BuiltinEntry<>("passenger_list", new RegistryArgs(LifeEventType.Group.OTHER, false, false)), // Liste de passagers
          new BuiltinEntry<>("residence", new RegistryArgs(LifeEventType.Group.OTHER, false, false)) // Résidence
      );

  private Registries() {
  }
}
