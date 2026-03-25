# 📋 BUSINESS.md - Système de Votes et Sondages

> **Document de référence métier**

---

## 1. CONTEXTE MÉTIER

### Pourquoi Ce Projet ?
- Les équipes ont besoin de recueillir des avis rapidement
- Les résultats doivent être visibles en temps réel
- Il faut éviter la fraude (votes multiples)
- La plateforme doit être scalable

### Objectifs du POC
| Objectif | Comment le mesurer |
|----------|-------------------|
| Valider l'architecture modulaire | 4 modules isolés (`users/`, `polls/`, `votes/`, `notifications/`) |
| Valider la performance de lecture | Latence P95 < 100ms sur les résultats |
| Valider la cohérence des données | 100% des votes comptabilisés correctement |

### Ce Que Ce Projet N'EST PAS
- ❌ Un produit de production (c'est une preuve de concept)
- ❌ Une plateforme de sondages complète
- ❌ Un système de vote anonyme (authentification requise)

---

## 2. ACTEURS

| Acteur | Rôle | Permissions |
|--------|------|-------------|
| **Participant** | Utilisateur authentifié | Voter, voir les résultats, recevoir notifications (si email vérifié) |
| **Créateur** | Utilisateur qui crée un sondage | Créer, modifier, clôturer, exporter ses sondages |
| **Admin** | Gestionnaire de la plateforme | Modérer, supprimer, exporter tous les sondages |

### Matrice des Permissions
| Action | Participant | Créateur | Admin |
|--------|-------------|----------|-------|
| Voir les sondages actifs | ✅ | ✅ | ✅ |
| Voter à un sondage | ✅ | ✅ | ✅ |
| Créer un sondage | ❌ | ✅ | ✅ |
| Clôturer un sondage | ❌ | ✅ (le sien) | ✅ (tous) |
| Exporter les résultats | ❌ | ✅ (les siens) | ✅ (tous) |
| Recevoir notification de clôture | ✅ (si email vérifié) | ✅ (si email vérifié) | ✅ |

---

## 3. CONCEPTS MÉTIER

### Glossaire
| Terme | Définition |
|-------|-----------|
| **Sondage** | Questionnaire avec un titre, des options et une date de fin |
| **Option** | Choix possible dans un sondage (minimum 2) |
| **Vote** | Action d'un participant sur une option (irréversible) |
| **Notification** | Email réel envoyé quand un sondage est clos |
| **Email vérifié** | Statut requis pour recevoir des notifications |

### Cycle de Vie d'un Sondage
```
CRÉÉ → ACTIF → CLOS → ARCHIVÉ
         │
         └─→ Les votes sont acceptés seulement quand ACTIF
```

### Cycle de Vie d'une Notification
```
ÉVÉNEMENT (clôture) → GÉNÉRÉE → ENVOYÉE → LIVRÉE (ou ÉCHOUÉE)
                              │
                              └─→ Uniquement si destinataire.emailVerified == true
```

---

## 4. RÈGLES DE GESTION

### Règles Critiques
| ID | Règle | Comment Tester |
|----|-------|----------------|
| **BR-001** | 1 vote max par utilisateur par sondage | Tenter 2 votes → erreur 409 |
| **BR-002** | Un sondage n'accepte des votes que s'il est ACTIF | Voter sur sondage CLOS → erreur 400 |
| **BR-003** | Un sondage doit avoir au moins 2 options | Créer sondage avec 1 option → erreur 400 |
| **BR-004** | La date de fin doit être dans le futur | Créer sondage avec date passée → erreur 400 |
| **BR-005** | Les résultats doivent refléter 100% des votes | Comparer count votes vs résultats |

### Règles Importantes
| ID | Règle | Comment Tester |
|----|-------|----------------|
| **BR-010** | Le titre du sondage ne peut pas être vide | Créer sondage sans titre → erreur 400 |
| **BR-011** | Le texte d'une option ne peut pas être vide | Créer option sans texte → erreur 400 |
| **BR-012** | Un utilisateur doit être authentifié pour voter | Voter sans token → erreur 401 |
| **BR-013** | Les résultats doivent être mis à jour en < 5 secondes | Voter → vérifier résultats |
| **BR-014** | Seul le créateur peut modifier/clôturer son sondage | Modifier sondage d'un autre → erreur 403 |
| **BR-020** | Un sondage peut être clôturé manuellement avant sa date de fin | POST /polls/{id}/close → statut = CLOS |
| **BR-021** | Les résultats peuvent être exportés en CSV | GET /polls/{id}/export → CSV téléchargé |
| **BR-022** | Notification email réel quand un sondage est clos | Événement → email envoyé via SMTP |
| **BR-022-1** | L'envoi de notification ne bloque PAS la clôture | Vérifier async (clôture < 500ms) |
| **BR-022-2** | Seuls les participants ayant voté reçoivent la notification | Vérifier destinataires = voters uniquement |
| **BR-023** | Limite de 10 sondages actifs par créateur | 11ème sondage → erreur 400 |
| **BR-024** | Un utilisateur ne reçoit des notifications que si son email est vérifié | Voter sans email vérifié → pas de notification |

---

## 5. USER STORIES

### US-001 : Créer un Sondage
> **En tant que** Créateur, **je veux** créer un sondage avec plusieurs options, **afin de** recueillir des avis.

**Critères :** Titre (non vide), ≥2 options, date de fin (futur), statut initial ACTIF, max 10 sondages actifs par créateur.

---

### US-002 : Voter à un Sondage
> **En tant que** Participant, **je veux** voter pour une option, **afin d'** exprimer mon avis.

**Critères :** Sondage doit être ACTIF, 1 vote max par sondage, comptabilisation immédiate, temps < 200ms.

---

### US-003 : Voir les Résultats
> **En tant que** Participant, **je veux** voir les résultats, **afin de** connaître l'opinion générale.

**Critères :** Count + pourcentages par option, total des votes, mise à jour < 5s, chargement < 100ms (avec cache).

---

### US-004 : Clôturer un Sondage
> **En tant que** Créateur, **je veux** clôturer mon sondage avant sa date de fin, **afin d'** arrêter de recevoir des votes.

**Critères :** Seul le créateur peut clôturer, statut → CLOS, votes suivants rejetés, notification email envoyée aux participants (si email vérifié).

---

### US-005 : Voir Mes Votes
> **En tant que** Participant, **je veux** voir l'historique de mes votes, **afin de** me rappeler où j'ai voté.

**Critères :** Tous les votes passés visibles, sondage + option + date affichés, pas de modification possible.

---

### US-006 : Exporter les Résultats
> **En tant que** Créateur, **je veux** exporter les résultats en CSV, **afin d'** analyser les données hors plateforme.

**Critères :** Seul le créateur peut exporter, CSV contient option/count/pourcentage, téléchargement immédiat, < 2 secondes.

---

### US-007 : Recevoir Notification de Clôture
> **En tant que** Participant, **je veux** être notifié par email quand un sondage auquel j'ai voté est clos, **afin de** consulter les résultats finaux.

**Critères :** Email envoyé à la clôture, contient titre + lien vers résultats, uniquement si email vérifié, envoi async (ne bloque pas), échec loggué (pas de retry).

**Scénario :**
```
Étant donné 50 participants ayant voté (40 avec email vérifié, 10 sans)
Quand le créateur clôture le sondage
Alors 40 notifications sont envoyées (aux emails vérifiés uniquement)
Et la clôture n'est pas bloquée par l'envoi
```

---

### US-009 : Vérifier son Email
> **En tant que** Participant, **je veux** vérifier mon adresse email, **afin de** recevoir des notifications de clôture.

**Critères :** Email de vérification envoyé à l'inscription, lien avec token valide, vérification NON bloquante pour voter, banner suggère la vérification si emailVerified == false.

**Scénario :**
```
Étant donné un utilisateur nouvellement inscrit avec email non vérifié
Quand il clique sur le lien de vérification dans l'email
Alors emailVerified passe à true
Et il peut maintenant recevoir des notifications
```

---

## 6. MÉTRIQUES DE SUCCÈS

### Performance
| Métrique | Cible |
|----------|-------|
| Temps de création d'un sondage | < 30 secondes |
| Temps de vote | < 200ms (P95) |
| Temps d'affichage des résultats | < 100ms (P95, avec cache) |
| Fraîcheur des résultats | < 5 secondes entre vote et affichage |
| Délai d'envoi notification | < 10 secondes après clôture |
| Temps de génération export CSV | < 2 secondes |

### Qualité
| Métrique | Cible |
|----------|-------|
| Taux d'erreur sur les votes | < 1% |
| Cohérence des résultats | 100% (count votes = somme des options) |
| Couverture de tests | > 80% |
| Taux de délivrabilité email | > 95% |
| Taux de vérification email | > 60% des utilisateurs |

---

## 7. HORS PÉRIMÈTRE

| Fonctionnalité | Statut |
|----------------|--------|
| Push notifications | ❌ Exclu (faible valeur pour ce cas d'usage) |
| SMS notifications | ❌ Exclu (coût + complexité) |
| Authentification sociale | ❌ Exclu (Phase 2) |
| Sondages anonymes | ❌ Exclu (Phase 2) |
| Questions multiples par sondage | ❌ Exclu (Phase 3) |
| Modification de vote après soumission | ❌ Jamais (contredit BR-001) |
| Templates email personnalisés | ❌ Exclu (template fixe validé) |
| Modération avant publication | ❌ Exclu (publication immédiate validée) |
| Durée maximale des sondages | ❌ Exclu (date de fin suffit) |
| Vérification email bloquante à l'inscription | ❌ Exclu (Option B : suggestion post-inscription) |

---

## 8. GLOSSAIRE

| Terme | Définition |
|-------|-----------|
| **ACTIF** | Statut d'un sondage qui accepte des votes |
| **CLOS** | Statut d'un sondage qui n'accepte plus de votes |
| **Read Model** | Table dénormalisée optimisée pour la lecture |
| **Write Model** | Table normalisée avec contraintes métier |
| **Async** | Traitement qui ne bloque pas le flux principal |
| **Email vérifié** | Utilisateur a confirmé la propriété de son email via token |

---

**Document — Système de Votes et Sondages**