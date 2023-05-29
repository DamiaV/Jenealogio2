package net.darmo_creations.jenealogio2.model;

import net.darmo_creations.jenealogio2.model.LifeEventType.RegistryArgs;
import net.darmo_creations.jenealogio2.model.Registry.BuiltinEntry;

import java.util.List;

public final class Registries {
  public static final Registry<Gender, String> GENDERS =
      new Registry<>("genders", List.of(
          new BuiltinEntry<>("agender", "#000000"),
          new BuiltinEntry<>("female", "#ee8434"),
          new BuiltinEntry<>("gender_fluid", "#bf17d5"),
          new BuiltinEntry<>("male", "#00b69e"),
          new BuiltinEntry<>("non_binary", "#fff430")
      ), Gender::new);

  public static final Registry<LifeEventType, RegistryArgs> LIFE_EVENT_TYPES =
      new Registry<>("life_event_types", List.<BuiltinEntry<RegistryArgs>>of(
          new BuiltinEntry<>("annulment", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Annulation
          new BuiltinEntry<>("civil_solidarity_pact", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // PACS
          new BuiltinEntry<>("divorce", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Divorce
          new BuiltinEntry<>("engagement", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Fiançailles
          new BuiltinEntry<>("marriage", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Mariage
          new BuiltinEntry<>("marriage_bann", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Bans de mariage
          new BuiltinEntry<>("marriage_contract", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Contrat de mariage
          new BuiltinEntry<>("marriage_licence", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Licence de mariage
          new BuiltinEntry<>("partners", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Couple non marié
          new BuiltinEntry<>("separation", new RegistryArgs(LifeEventType.Group.RELATIONSHIP, false, 2)), // Séparation

          new BuiltinEntry<>("acquisition", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Acquisition
          new BuiltinEntry<>("census", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Recensement
          new BuiltinEntry<>("change_of_gender", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Changement de genre
          new BuiltinEntry<>("change_of_name", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Changement de nom
          new BuiltinEntry<>("election", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Élection
          new BuiltinEntry<>("endowment", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Dotation
          new BuiltinEntry<>("emigration", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Émigration
          new BuiltinEntry<>("immigration", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Immigration
          new BuiltinEntry<>("naturalization", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Naturalisation
          new BuiltinEntry<>("occupation", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Profession
          new BuiltinEntry<>("property", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Propriété
          new BuiltinEntry<>("retirement", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Retraite
          new BuiltinEntry<>("sale_of_property", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Vente d’un bien
          new BuiltinEntry<>("will", new RegistryArgs(LifeEventType.Group.ADMIN, false)), // Testament

          new BuiltinEntry<>("diploma", new RegistryArgs(LifeEventType.Group.EDUCATION, false)), // Certificat
          new BuiltinEntry<>("education", new RegistryArgs(LifeEventType.Group.EDUCATION, false)), // Éducation
          new BuiltinEntry<>("graduation", new RegistryArgs(LifeEventType.Group.EDUCATION, false)), // Diplome

          new BuiltinEntry<>("birth", new RegistryArgs(LifeEventType.Group.LIFESPAN, false)), // Naissance
          new BuiltinEntry<>("burial", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Inhumation
          new BuiltinEntry<>("cremation", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Crémation
          new BuiltinEntry<>("death", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Décès
          new BuiltinEntry<>("funeral", new RegistryArgs(LifeEventType.Group.LIFESPAN, true)), // Funérailles

          new BuiltinEntry<>("circumcision", new RegistryArgs(LifeEventType.Group.MEDICAL, false)), // Circoncision
          new BuiltinEntry<>("disease", new RegistryArgs(LifeEventType.Group.MEDICAL, false)), // Maladie
          new BuiltinEntry<>("hospitalization", new RegistryArgs(LifeEventType.Group.MEDICAL, false)), // Hospitalisation

          new BuiltinEntry<>("conscription", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Service militaire
          new BuiltinEntry<>("demobilization", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Démobilisation militaire
          new BuiltinEntry<>("military_decoration", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Distinction militaire
          new BuiltinEntry<>("military_promotion", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Promotion militaire
          new BuiltinEntry<>("mobilization", new RegistryArgs(LifeEventType.Group.MILITARY, false)), // Mobilisation militaire

          new BuiltinEntry<>("baptism", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Baptème
          new BuiltinEntry<>("bar_mitzvah", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Bar mitzvah
          new BuiltinEntry<>("bat_mitzvah", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Bat mitzvah
          new BuiltinEntry<>("blessing", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Bénédiction
          new BuiltinEntry<>("confirmation", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Confirmation
          new BuiltinEntry<>("excommunication", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Excommunication
          new BuiltinEntry<>("first_communion", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Première communion
          new BuiltinEntry<>("ordinance", new RegistryArgs(LifeEventType.Group.RELIGION, false)), // Ordination

          new BuiltinEntry<>("accomplishment", new RegistryArgs(LifeEventType.Group.DISTINCTION, false)), // Accomplissement
          new BuiltinEntry<>("medal", new RegistryArgs(LifeEventType.Group.DISTINCTION, false)), // Décoration
          new BuiltinEntry<>("rank", new RegistryArgs(LifeEventType.Group.DISTINCTION, false)), // Distinction

          new BuiltinEntry<>("membership", new RegistryArgs(LifeEventType.Group.OTHER, false)), // Adhésion
          new BuiltinEntry<>("passenger_list", new RegistryArgs(LifeEventType.Group.OTHER, false)), // Liste de passagers
          new BuiltinEntry<>("residence", new RegistryArgs(LifeEventType.Group.OTHER, false)) // Résidence
      ), (key, args) -> new LifeEventType(key, args.group(), args.indicatesDeath(), args.maxActors()));

  private Registries() {
  }
}
