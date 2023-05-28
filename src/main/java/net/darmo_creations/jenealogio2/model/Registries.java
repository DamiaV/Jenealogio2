package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.LifeEventType.RegistryArgs;
import net.darmo_creations.jenealogio2.model.Registry.BuiltinEntry;

import java.util.List;

public final class Registries {
  public static final Registry<Gender, Void> GENDERS =
      new Registry<>("genders", List.of(
          new BuiltinEntry<>(0, "agender"),
          new BuiltinEntry<>(1, "female"),
          new BuiltinEntry<>(2, "gender_fluid"),
          new BuiltinEntry<>(3, "male"),
          new BuiltinEntry<>(4, "non_binary")
      ), 100, (id, name, builtin, unused) -> new Gender(id, name, builtin));

  public static final Registry<LifeEventType, RegistryArgs> LIFE_EVENT_TYPES =
      new Registry<>("life_event_types", List.<BuiltinEntry<RegistryArgs>>of(
          new BuiltinEntry<>(0, "annulment", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Annulation
          new BuiltinEntry<>(1, "civil_solidarity_pact", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // PACS
          new BuiltinEntry<>(2, "divorce", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Divorce
          new BuiltinEntry<>(3, "engagement", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Fiançailles
          new BuiltinEntry<>(4, "marriage", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Mariage
          new BuiltinEntry<>(5, "marriage_bann", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Bans de mariage
          new BuiltinEntry<>(6, "marriage_contract", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Contrat de mariage
          new BuiltinEntry<>(7, "marriage_licence", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Licence de mariage
          new BuiltinEntry<>(8, "partners", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Couple non marié
          new BuiltinEntry<>(9, "residence", new RegistryArgs(LifeEventType.Group.FAMILY, false)), // Résidence
          new BuiltinEntry<>(10, "separation", new RegistryArgs(LifeEventType.Group.FAMILY, false, 2)), // Séparation

          new BuiltinEntry<>(11, "acquisition", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Acquisition
          new BuiltinEntry<>(12, "census", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Recensement
          new BuiltinEntry<>(13, "change_of_gender", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Changement de genre
          new BuiltinEntry<>(14, "change_of_name", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Changement de nom
          new BuiltinEntry<>(15, "election", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Élection
          new BuiltinEntry<>(16, "endowment", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Dotation
          new BuiltinEntry<>(17, "emigration", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Émigration
          new BuiltinEntry<>(18, "immigration", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Immigration
          new BuiltinEntry<>(19, "naturalization", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Naturalisation
          new BuiltinEntry<>(20, "occupation", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Profession
          new BuiltinEntry<>(21, "property", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Propriété
          new BuiltinEntry<>(22, "retirement", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Retraite
          new BuiltinEntry<>(23, "sale_of_property", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Vente d’un bien
          new BuiltinEntry<>(24, "will", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Testament

          new BuiltinEntry<>(25, "diploma", new RegistryArgs(LifeEventType.Group.EDUCATION, false)), // Certificat
          new BuiltinEntry<>(26, "education", new RegistryArgs(LifeEventType.Group.EDUCATION, false)), // Éducation
          new BuiltinEntry<>(27, "graduation", new RegistryArgs(LifeEventType.Group.EDUCATION, false)), // Diplome

          new BuiltinEntry<>(28, "birth", new RegistryArgs(LifeEventType.Group.LIFESPAN, false)), // Naissance
          new BuiltinEntry<>(29, "burial", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Inhumation
          new BuiltinEntry<>(30, "cremation", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Crémation
          new BuiltinEntry<>(31, "death", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Décès
          new BuiltinEntry<>(32, "funeral", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Funérailles

          new BuiltinEntry<>(33, "circumcision", new RegistryArgs(LifeEventType.Group.MEDICAL, false)), // Circoncision
          new BuiltinEntry<>(34, "disease", new RegistryArgs(LifeEventType.Group.MEDICAL, false)), // Maladie
          new BuiltinEntry<>(35, "hospitalization", new RegistryArgs(LifeEventType.Group.MEDICAL, false)), // Hospitalisation

          new BuiltinEntry<>(36, "conscription", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Service militaire
          new BuiltinEntry<>(37, "demobilization", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Démobilisation militaire
          new BuiltinEntry<>(38, "military_decoration", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Distinction militaire
          new BuiltinEntry<>(39, "military_promotion", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Promotion militaire
          new BuiltinEntry<>(40, "mobilization", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Mobilisation militaire

          new BuiltinEntry<>(41, "baptism", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Baptème
          new BuiltinEntry<>(42, "bar_mitzvah", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Bar mitzvah
          new BuiltinEntry<>(43, "bat_mitzvah", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Bat mitzvah
          new BuiltinEntry<>(44, "blessing", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Bénédiction
          new BuiltinEntry<>(45, "confirmation", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Confirmation
          new BuiltinEntry<>(46, "excommunication", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Excommunication
          new BuiltinEntry<>(47, "first_communion", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Première communion
          new BuiltinEntry<>(48, "ordinance", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Ordination

          new BuiltinEntry<>(49, "accomplishment", new RegistryArgs(LifeEventType.Group.DISTINCTION, false)), // Accomplissement
          new BuiltinEntry<>(50, "medal", new RegistryArgs(LifeEventType.Group.DISTINCTION, false)), // Décoration
          new BuiltinEntry<>(51, "rank", new RegistryArgs(LifeEventType.Group.DISTINCTION, false)), // Distinction

          new BuiltinEntry<>(52, "membership", new RegistryArgs(LifeEventType.Group.OTHER, false)), // Adhésion
          new BuiltinEntry<>(53, "passenger_list", new RegistryArgs(LifeEventType.Group.OTHER, false)) // Liste de passagers
      ), 1000, (id, name, builtin, args) -> new LifeEventType(id, name, builtin, args.group(), args.indicatesDeath(), args.maxActors()));

  private Registries() {
  }
}
