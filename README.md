# Jenealogio 2

A user-friendly, inclusive, LGBTQAI+-friendly and non-spying family tree manager.

## Why this name?

The name Jenealogio is a build from the Esperanto word for genealogy, “_genealogio_”, with the _G_ replaced by a _J_ for
**J**ava, the language the app is written in.

## Why 2?

The first version of this software was developed quite a while back but I was not satisfied with the direction it took.
So in 2023 I archived its [repository](https://github.com/DamiaV/Jenealogio) and started again from scratch with this
new version.

## Why yet another genealogical software?

They are two main reasons: privacy and inclusivity.

Firstly, regarding privacy, I generally don’t trust online genealogical apps with ensuring the privacy and security of
the information I give them, and that they will not sell it to third-parties. Plus, there is the risk data leaks due to
hacks.

Secondly, being queer myself, while researching the genealogy of my own family, I grew tired of the lack of
LGBTQAI+ inclusivity in most genealogical softwares. As such, I decided to build my own that would fill this need. I was
also inspired by blog posts on _[sixgen.org](https://sixgen.org/)_ that talked about this issue. After reading their
posts on [LGBTQ Genealogy & Software](https://sixgen.org/lgbtq-genealogy-software-part-1/), I was further motivated to
pursue this endeavor.

# ⚠️ _Important notice, please read!_ ⚠️

**This application is currently under active developpement. As such, tree files’ structure may change drastically during
this time and until the first release, with no guarantee of backward compatibility.**

You can follow planned features and developpement progress over on the
project’s [Trello board](https://trello.com/b/PsvxkYIB).

# Design choices

## Inclusivity

In _[LGBTQ Genealogy & Software – Part 6](https://sixgen.org/lgbtq-genealogy-software-part-6/)_ at _sixgen.org_, they
list the following points they wish to see in genealogical softwares:

1. **Ability to Enter Spouses with Accurately Defined Relationships**: Provide options for accurately defining
   relationships such as marriage, domestic partnership, civil unions, partners, co-parents, unmarried unions, or
   however people commit to each other.
2. **Link Any Person in a Family Unit and Define Relationships Independently**: Allow the flexibility to link any
   individual in a family unit and define their relationships independent of hetero-normative models.
3. **Allow for More Than Two Parents**: Acknowledge the reality of families with more than two parents, accommodating
   diverse family structures.
4. **Evolve Standard Genealogy Documents to Report on Family History**: Move beyond DNA lines and evolve standard
   genealogy documents to comprehensively report on Family History.
5. **Gender-Neutral Parent and Spouse Labels with Gender-Neutral Iconography**: Implement gender-neutral labels for
   parents and spouses accompanied by inclusive iconography.
6. **Allow for Birth Sex, Trans Sex, and Gender Identification**: Incorporate options for recording birth sex, trans
   sex, and gender identification to reflect the diverse spectrum of gender identities.
7. **Adopt the Latest GEDCOM 5.5.5 Standard to Remove Religious Control of Genealogical Data**: Embrace the GEDCOM 5.5.5
   standard to ensure genealogical data is free from religious control, fostering inclusivity and diversity.

I tried to follow these suggestions in Jenealogio 2. Here’s how those points have been addressed:

1. The app offers various builtin relationship types such as marriage, non-married partners, and civil solidarity pact.
   This list may be expanded in the future. The app also offers the ability to create custom types if none of the
   builtin ones fit a situation.
2. The only explicit family links currently supported are parent-child relations and relations between partners/spouses.
   Siblings are deduced from the parents they have in common.
3. This point is not yet fully supported. As I made the decision to show parent-child relations as a binary tree to
   simplify its rendering, a person can have at most 2 direct parents. However, adoptive, foster and godparents can be
   specified without any upper cap on the number but they do not appear in the graphical tree view. All of this may
   change in the future but I won’t guarantee anything.
4. The software makes no assumptions regarding DNA relations between the members of a tree. It is left to the
   interpretation of the users.
5. The app uses neutral terminology such as _Parent 1_ and _Parent 2_ for parents, and _Partner_ instead of _Husband_ or
   _Wife_. A single non-representative placeholder icon is used for all people, regardless of their gender. Gender is
   represented by an icon instead of a color.
6. Assigned gender at birth and gender are two separate fields. There are several builtin gender identities (agender,
   female, gender fluid, male, non-binary) and users can define the ones they want if these are not sufficient. More
   builtin genders may be added in the future if needs be.
7. I’m _trying_ to add the ability to import/export GEDCOM 5.5.5 files but I _cannot_ guarantee that it will be
   supported one day. The specification is quite long (166 pages) and complex, and the format may not support every type
   of information that this app handles when exporting trees.

## Date and time representation

The app offers 11 different calendar systems for dates:

* Coptic
* Ethiopian
* French Republican
* French Republican with decimal time (10 hours in a day, 100 minutes in 1 hour, etc.)
* Gregorian
* Hebrew
* Indian
* Julian
* Minguo
* Solar Hijri
* Thai Solar (Buddhist Era only)

You can choose how dates and times are displayed in the app among several formats: D M Y, M D Y, 12h clock, 24h clock,
and many more.

**Notice:** All dates and times are expressed as local time, this means that there may be some sorting inconsistencies
if events occur in different time zones. Conversions from French Republican calendars to Gregorian use the modern
Europe/Paris timezone.

# Languages

The app is available in 3 languages:

* English
* Esperanto
* French

# Privacy

This app does not and will never send your data and files anywhere or to anyone.
You keep full control of your data and files.

# Requirements

The app is written in [Java](https://www.java.com). It works on Linux and should also work without any issues on
Windows (untested) and MacOS (untested).
It requires at least Java 17 and should also work with newer versions (untested).

# License

This software is available under the GPL-3.0 license. Please refer to the LICENSE file for the full license text.
