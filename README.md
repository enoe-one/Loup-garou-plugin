<div align="center">

# 🐺 Loup-Garou UHC

**Plugin Minecraft · Paper 1.21 · Java 21**

[![Version](https://img.shields.io/badge/version-2.0-orange?style=flat-square)](https://github.com/)
[![Paper](https://img.shields.io/badge/Paper-1.21-blue?style=flat-square)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-red?style=flat-square)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)

*Un plugin de déduction sociale Loup-Garou complet, en mode UHC, pour Paper 1.21.*  
*A full Werewolf social deduction plugin, UHC mode, for Paper 1.21.*

---

[🇫🇷 Français](#-français) · [🇬🇧 English](#-english)

</div>

---

## 🇫🇷 Français

### 📖 Présentation

**Loup-Garou UHC** est un plugin Minecraft pour serveurs **Paper 1.21** qui transforme votre serveur en une partie de Loup-Garou complète, jouable directement en jeu.

- **UHC** : la vie ne se régénère pas naturellement. Chaque cœur compte.
- **37 rôles** répartis en 4 familles : Village, Loups-Garous, Solitaires, Binaires.
- **Interface graphique** `/lgsetting` pour configurer la partie en quelques clics.
- **Système de messages** immersif : countdown, annonces de mort, votes, titres à l'écran.
- **PVP style 1.8** : plus de cooldown d'attaque.
- **Grottes boostées ×1.8** : les minerais droppent davantage.

---

### 🎮 Comment jouer

#### Prérequis
- Serveur **Paper 1.21** (pas Spigot, pas Bukkit)
- **Java 21** minimum
- 4 joueurs minimum recommandés (2 minimum pour tester)

#### Installation
1. Téléchargez le fichier `LoupGarou-X.X.jar` depuis les [Releases](releases/).
2. Placez-le dans le dossier `plugins/` de votre serveur.
3. Redémarrez le serveur.
4. Le fichier `config.yml` est généré automatiquement dans `plugins/LoupGarou/`.

#### Lancer une partie
```
1. /lg create "Nom de la partie"   → crée la room (vous devenez Owner)
2. Attendez que les joueurs rejoignent — ils sont téléportés dans la cage
3. /lgsetting                       → configurez les rôles et paramètres
4. /lg start                        → lancez la partie
```

---

### ⏱️ Déroulement d'une partie

| Temps | Événement |
|-------|-----------|
| `0 min` | Tous les joueurs dans la cage (bedrock, 0·220·0). PVP désactivé. |
| `0 → 20 min` | Exploration libre. Diamants limités à 25 max. Nether/End bloqués. |
| `20 min` | Rôles révélés en privé. **PVP activé.** |
| `25 min` | Couple annoncé publiquement. |
| `< 30 min` | Résurrections possibles. Mort → TP surface, stuff conservé. |
| `> 30 min` | Plus de résurrection. Mort → TP cage, spectateur. |
| `1h` | Le Loup Endormi reçoit la liste de ses alliés. |
| `Chaque épisode` | Vote du village possible. Événements spéciaux. |

---

### 🎭 Les Rôles

<details>
<summary><b>🟢 Village (16 rôles)</b></summary>

| Rôle | Pouvoir | Commande |
|------|---------|----------|
| **Maire** | Vote double, décisif en cas d'égalité. Speed +0.1/épisode | — |
| **Citoyen** | Vote double (sauf égalité) | — |
| **Voyant** | Révèle le rôle d'un joueur 1×/épisode | `/lg voir <joueur>` |
| **Renard** | Détecte un Loup parmi 3 voisins | `/lg renard <joueur>` |
| **Conteuse** | Reçoit les pseudos des joueurs actifs la nuit | automatique |
| **Salvateur** | Protège un joueur par nuit | `/lg proteger <joueur>` |
| **Sorcière** | Potion de vie + potion de mort (1× chacune) | `/lg vie` · `/lg mort <joueur>` |
| **Chasseur** | Tire sur un joueur en mourant | `/lg tirer <joueur>` |
| **Astronome** | Faisceau coloré vers un joueur toutes les 10 min | automatique |
| **Chevalier** | Force 2 vs Loups jusqu'au 1er kill | `/lg arme` |
| **Ancien** | Ressuscite si tué par les Loups (N fois config) | automatique |
| **Simple Villagois** | Aucun pouvoir | — |
| **Idiot du Village** | Ressuscite 1× si éliminé par vote | automatique |
| **Sœurs** | Se connaissent. Résistance si proches (<50 blocs) | automatique |
| **Monteur d'Ours** | Grogne le nombre de Loups proches | `/lg loup` (recharge 40 min) |
| **Petite Fille** | Voit le chat des Loups. Invisible sans armure la nuit | automatique |

</details>

<details>
<summary><b>🔴 Loups-Garous (8 rôles)</b></summary>

| Rôle | Pouvoir | Commande |
|------|---------|----------|
| **Loup Simple** | Loup de base. Force 1 la nuit | — |
| **Loup Perfide** | Invisible la nuit sans armure | automatique |
| **Loup Endormi** | Reçoit ses alliés après 1h de jeu | automatique |
| **Loup Vengeur** | Speed quand un Loup meurt | automatique |
| **Grand Méchant Loup** | Vote double + RAGE (Speed+Résistance 10 min) | `/lg rage` |
| **Infecté Père des Loups** | Transforme un mort en Loup (1×, 10 secondes) | `/lg infecter` |
| **Loup Timide** | Fort seul, faible proche d'un autre Loup | automatique |
| **Loup Empoisonneur** | Fausse les informations de 2 joueurs (2×) | `/lg empoisonner <joueur>` |

</details>

<details>
<summary><b>🟠 Solitaires (7 rôles)</b></summary>

| Rôle | Condition de victoire | Commande |
|------|----------------------|----------|
| **Loup Blanc** | Gagner seul — doit aussi éliminer les Loups | automatique |
| **Joueur de Flûte** | 100% des joueurs restants charmés | automatique |
| **Ange** | Gardien (protège) ou Déchu (tue une cible) | `/lg gardien` · `/lg dechu` |
| **Feu Follet** | Condition propre. Incendie + téléportation | `/lg incendie` · `/lg plume` |
| **Imitateur** | Copie un rôle chaque nuit | `/lg imiter <joueur>` |
| **Assassin** | Force+Résistance perma. Cache des morts | `/lg cacher <joueur>` |
| **Inconnu** | Vu Village par détection, Loup par les Loups | automatique |

</details>

<details>
<summary><b>🟣 Binaires (6 rôles)</b></summary>

| Rôle | Mécanique | Commande |
|------|-----------|----------|
| **Cupidon** | Lie 2 joueurs qui gagnent ensemble | `/lg lier <j1> <j2>` |
| **Enfant Sauvage** | Devient Loup si son mentor meurt | `/lg mentor <joueur>` |
| **Trublion** | Échange 2 rôles entre 20-25 min | `/lg echanger <j1> <j2>` |
| **Voleur** | Prend le rôle du premier mort | automatique |
| **Chien-Loup** | Choisit Village ou Loup au 1er épisode | `/lg village` · `/lg loup` |
| **Ivrogne** | Reçoit un faux rôle au départ | automatique |

</details>

---

### 🛠️ Commandes

#### Joueurs
```
/lg create "Nom"        Créer une room (vous devenez Owner)
/lg start               Lancer la partie
/lg voter <joueur>      Voter contre un joueur
/lg voir <joueur>       (Voyant) Révéler un rôle
/lg proteger <joueur>   (Salvateur) Protéger un joueur
/lg vie                 (Sorcière) Utiliser la potion de vie
/lg mort <joueur>       (Sorcière) Utiliser la potion de mort
/lg tirer <joueur>      (Chasseur) Tirer en mourant
/lg lier <j1> <j2>      (Cupidon) Lier deux joueurs
/lg mentor <joueur>     (Enfant Sauvage) Choisir son mentor
/lg rage                (Grand Méchant Loup) Activer la rage
/lg infecter            (Infecté) Transformer un mort
/lg imiter <joueur>     (Imitateur) Copier un rôle
/lg cacher <joueur>     (Assassin) Cacher une mort
/lg renard <joueur>     (Renard) Sonder un joueur
/lg gardien / /lg dechu (Ange) Choisir son rôle
/lg village / /lg loup  (Chien-Loup) Choisir son camp
/lg echanger <j1> <j2>  (Trublion) Échanger deux rôles
/lg arme                (Chevalier) Activer la Force
/lg plume / /lg incendie (Feu Follet) Pouvoirs spéciaux
/lg loup                (Monteur d'Ours) Détecter les loups
```

#### Owner
```
/lg create "Nom"           Créer la room
/lg start                  Lancer la partie
/lgsetting                 Ouvrir l'interface de configuration
/lgowner admin <pseudo>    Donner le statut Admin
/lgowner deadmin <pseudo>  Retirer le statut Admin
```

#### Admin
```
/lgadmin setting                          Aide des paramètres
/lgadmin setting wolves <n>               Définir le nombre de Loups
/lgadmin setting role <id> true/false     Activer/désactiver un rôle
/lgadmin setting couple true/false        Activer le couple aléatoire
/lgadmin setting trouple true/false       Activer le trouple
/lgadmin setting composition true/false   Rendre la composition visible
/lgadmin reviv <pseudo>                   Ressusciter un joueur (< 30 min)
/lgadmin info <message>                   Broadcast un message
```

---

### ⚙️ Configuration (`config.yml`)

```yaml
game:
  wolves: 2                  # Nombre de Loups-Garous
  max-diamonds: 25           # Diamants max minés par joueur
  pvp-delay: 1200            # Délai PVP en secondes (défaut 20 min)
  revive-limit: 1800         # Limite de résurrection en secondes (défaut 30 min)
  couple: true               # Couple aléatoire à 25 min
  trouple: false             # Trouple (nécessite ≥9 joueurs)
  composition-visible: false # Composition visible par tous
  ore-boost: true            # Grottes boostées ×1.8

events:
  bloody_night: false        # Nuit Sanglante
  trublionage: false         # Trublionage automatique
  election: false            # Élection du Maire
  expose: false              # Exposé (rôle caché parmi 3-4 joueurs)
  mystery: false             # Événement mystère aléatoire

enchants:
  sharpness: 3               # Tranchant max (Solitaires peuvent avoir 4)
  protection: 3              # Protection max
  power: 3                   # Puissance max
  knockback: 0               # Recul (0 = interdit)
  punch: 0                   # Frappe (0 = interdit)
  mending: false             # Raccommodage autorisé ?
```

---

### 🙏 Remerciements

Un grand merci à toutes les personnes qui ont contribué à ce projet :

| Rôle | Personne |
|------|----------|
| **Développement & Idée** | [Enoe_one](https://github.com/Enoe-one) |
| **Tank man / Support** | Tankman |

---

### 🤝 Contributions

Les contributions sont les bienvenues ! Voici comment participer :

1. **Fork** le projet
2. Crée une branche : `git checkout -b feature/ma-fonctionnalite`
3. Commit tes changements : `git commit -m 'Ajout de ma fonctionnalité'`
4. Push : `git push origin feature/ma-fonctionnalite`
5. Ouvre une **Pull Request**

**Tu as une idée ?** Ouvre une [Issue](issues/) avec le tag `idée` et décris ta suggestion !  
**Tu as trouvé un bug ?** Ouvre une [Issue](issues/) avec le tag `bug` et les étapes pour le reproduire.

---

## 🇬🇧 English

### 📖 Overview

**Loup-Garou UHC** is a Minecraft plugin for **Paper 1.21** servers that brings a full Werewolf social deduction game directly in-game.

- **UHC mode**: health does not regenerate naturally. Every heart matters.
- **37 roles** across 4 families: Village, Werewolves, Solitaries, Binary.
- **GUI interface** `/lgsetting` to configure the game in a few clicks.
- **Immersive message system**: countdown, death announcements, votes, screen titles.
- **1.8-style PVP**: no attack cooldown.
- **×1.8 ore boost**: mines drop more resources.

---

### 🎮 How to Play

#### Requirements
- **Paper 1.21** server (not Spigot, not Bukkit)
- **Java 21** minimum
- 4+ players recommended (2 minimum for testing)

#### Installation
1. Download `LoupGarou-X.X.jar` from [Releases](releases/).
2. Place it in your server's `plugins/` folder.
3. Restart the server.
4. `config.yml` is automatically generated in `plugins/LoupGarou/`.

#### Starting a Game
```
1. /lg create "Game Name"   → creates the room (you become Owner)
2. Wait for players to join — they are teleported to the cage
3. /lgsetting               → configure roles and settings
4. /lg start                → start the game
```

---

### ⏱️ Game Timeline

| Time | Event |
|------|-------|
| `0 min` | All players in the bedrock cage (0·220·0). PVP off. |
| `0 → 20 min` | Free exploration. Max 25 diamonds. Nether/End locked. |
| `20 min` | Roles revealed privately. **PVP enabled.** |
| `25 min` | Couple announced publicly. |
| `< 30 min` | Resurrections allowed. Death → TP to surface, inventory kept. |
| `> 30 min` | No more resurrections. Death → TP to cage, spectator mode. |
| `1 hour` | Sleeping Wolf receives their ally list. |
| `Each episode` | Village vote available. Special events trigger. |

---

### 🎭 Roles

<details>
<summary><b>🟢 Village (16 roles)</b></summary>

| Role | Ability | Command |
|------|---------|---------|
| **Mayor** | Double vote, decisive on tie. Speed +0.1/episode | — |
| **Citizen** | Double vote (except ties) | — |
| **Seer** | Reveals a player's role 1×/episode | `/lg voir <player>` |
| **Fox** | Detects a Wolf among 3 neighbors | `/lg renard <player>` |
| **Storyteller** | Receives names of active players each night | automatic |
| **Guardian** | Protects one player per night | `/lg proteger <player>` |
| **Witch** | Life potion + death potion (1× each) | `/lg vie` · `/lg mort <player>` |
| **Hunter** | Shoots a player upon death | `/lg tirer <player>` |
| **Astronomer** | Colored particle beam every 10 min | automatic |
| **Knight** | Strength 2 vs Wolves until first kill | `/lg arme` |
| **Elder** | Revives if killed by Wolves (N times, config) | automatic |
| **Simple Villager** | No ability | — |
| **Village Idiot** | Revives 1× if eliminated by village vote | automatic |
| **Sisters** | Know each other. Resistance if close (<50 blocks) | automatic |
| **Bear Tamer** | Bear growls count of nearby Wolves | `/lg loup` (40 min cooldown) |
| **Little Girl** | Sees Wolves' chat. Invisible without armor at night | automatic |

</details>

<details>
<summary><b>🔴 Werewolves (8 roles)</b></summary>

| Role | Ability | Command |
|------|---------|---------|
| **Simple Wolf** | Base wolf. Strength 1 at night | — |
| **Treacherous Wolf** | Invisible at night without armor | automatic |
| **Sleeping Wolf** | Receives ally list after 1 hour | automatic |
| **Vengeful Wolf** | Speed when a Wolf dies | automatic |
| **Big Bad Wolf** | Double vote + RAGE (Speed+Resistance 10 min) | `/lg rage` |
| **Infected Father** | Transforms a dead player into Wolf (1×, 10 sec) | `/lg infecter` |
| **Shy Wolf** | Strong alone, weak near another Wolf | automatic |
| **Poisoner Wolf** | Falsifies information for 2 players (2×) | `/lg empoisonner <player>` |

</details>

<details>
<summary><b>🟠 Solitaries (7 roles)</b></summary>

| Role | Win Condition | Command |
|------|--------------|---------|
| **White Wolf** | Win alone — must also eliminate Wolves | automatic |
| **Flute Player** | 100% of remaining players charmed | automatic |
| **Angel** | Guardian (protect) or Fallen (kill a target) | `/lg gardien` · `/lg dechu` |
| **Will o' Wisp** | Own condition. Fire + teleport | `/lg incendie` · `/lg plume` |
| **Mimic** | Copies a role each night | `/lg imiter <player>` |
| **Assassin** | Permanent Strength+Resistance. Hides deaths | `/lg cacher <player>` |
| **Unknown** | Seen as Village by detection, Wolf by Wolves | automatic |

</details>

<details>
<summary><b>🟣 Binary (6 roles)</b></summary>

| Role | Mechanic | Command |
|------|----------|---------|
| **Cupid** | Links 2 players who win together | `/lg lier <p1> <p2>` |
| **Wild Child** | Becomes Wolf if mentor dies | `/lg mentor <player>` |
| **Troublemaker** | Swaps 2 roles between 20-25 min | `/lg echanger <p1> <p2>` |
| **Thief** | Takes the first dead player's role | automatic |
| **Dog-Wolf** | Chooses Village or Wolf at episode 1 | `/lg village` · `/lg loup` |
| **Drunk** | Receives a fake role at start | automatic |

</details>

---

### 🛠️ Commands

#### Players
```
/lg create "Name"       Create a room (you become Owner)
/lg start               Start the game
/lg voter <player>      Vote against a player
/lg voir <player>       (Seer) Reveal a role
/lg proteger <player>   (Guardian) Protect a player
/lg vie                 (Witch) Use the life potion
/lg mort <player>       (Witch) Use the death potion
/lg tirer <player>      (Hunter) Shoot on death
/lg lier <p1> <p2>      (Cupid) Link two players
/lg mentor <player>     (Wild Child) Choose mentor
/lg rage                (Big Bad Wolf) Activate rage
/lg infecter            (Infected) Transform a dead player
/lg imiter <player>     (Mimic) Copy a role
/lg cacher <player>     (Assassin) Hide a death
/lg renard <player>     (Fox) Probe a player
/lg gardien / /lg dechu (Angel) Choose your role
/lg village / /lg loup  (Dog-Wolf) Choose your side
/lg echanger <p1> <p2>  (Troublemaker) Swap two roles
/lg arme                (Knight) Activate Strength
/lg plume / /lg incendie (Will o' Wisp) Special powers
/lg loup                (Bear Tamer) Detect wolves
```

#### Owner
```
/lg create "Name"          Create room
/lg start                  Start game
/lgsetting                 Open configuration GUI
/lgowner admin <name>      Grant Admin status
/lgowner deadmin <name>    Revoke Admin status
```

#### Admin
```
/lgadmin setting                          Settings help
/lgadmin setting wolves <n>               Set Wolf count
/lgadmin setting role <id> true/false     Enable/disable a role
/lgadmin setting couple true/false        Enable random couple
/lgadmin setting trouple true/false       Enable trouple
/lgadmin setting composition true/false   Show composition
/lgadmin reviv <name>                     Resurrect a player (< 30 min)
/lgadmin info <message>                   Broadcast a message
```

---

### ⚙️ Configuration (`config.yml`)

```yaml
game:
  wolves: 2                  # Number of Werewolves
  max-diamonds: 25           # Max diamonds mined per player
  pvp-delay: 1200            # PVP delay in seconds (default 20 min)
  revive-limit: 1800         # Resurrection limit in seconds (default 30 min)
  couple: true               # Random couple at 25 min
  trouple: false             # Trouple (requires ≥9 players)
  composition-visible: false # Composition visible to all
  ore-boost: true            # ×1.8 ore boost

events:
  bloody_night: false        # Bloody Night
  trublionage: false         # Automatic role swap
  election: false            # Mayor Election
  expose: false              # Expose event
  mystery: false             # Random mystery event

enchants:
  sharpness: 3               # Max Sharpness (Solitaries can have 4)
  protection: 3              # Max Protection
  power: 3                   # Max Power
  knockback: 0               # Knockback (0 = forbidden)
  punch: 0                   # Punch (0 = forbidden)
  mending: false             # Mending allowed?
```

---

### 🙏 Credits & Thanks

A big thank you to everyone who contributed to this project:

| Role | Person |
|------|--------|
| **Development & Idea** | [Enoe_one](https://github.com/Enoe-one) |
| **Tank man / Support** | Tankman |

---

### 🤝 Contributing

Contributions are welcome! Here's how to get involved:

1. **Fork** the project
2. Create your branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a **Pull Request**

**Got an idea?** Open an [Issue](issues/) with the `idea` tag and describe your suggestion!  
**Found a bug?** Open an [Issue](issues/) with the `bug` tag and include steps to reproduce.

---

<div align="center">

**Made with ❤️ by [Enoe_one](https://github.com/Enoe-one)**

*Loup-Garou UHC — Paper 1.21*

</div>
