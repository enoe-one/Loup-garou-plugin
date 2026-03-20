# Configuration du Monde Loup-Garou UHC

## Option 1 — Utiliser le générateur custom (recommandé)

Dans `server.properties` :
```
level-name=lgworld
level-type=default
```

Dans `bukkit.yml`, ajouter sous `worlds:` :
```yaml
worlds:
  lgworld:
    generator: LoupGarou
```

Le plugin génère automatiquement :
- **Forêt sombre** (Dark Forest) dans un rayon de 500 blocs autour de (0,0)
- **Forêt** entre 500 et 750 blocs (transition)
- **Plaines** au-delà
- **Pas de deepslate** (remplacé par stone)
- **Grottes simples** style pré-1.18 (pas de lush caves, dripstone caves)
- **Pas de villages** (bloqués par WorldListener + EntitySpawnEvent)

## Option 2 — Monde vanilla pré-généré

Si vous voulez garder un monde vanilla mais bloquer deepslate et villages :

1. Générer un monde normal avec seed personnalisée
2. Le plugin remplace le deepslate dans les **nouveaux chunks** chargés
3. Configurer dans `spigot.yml` pour désactiver les structures :
```yaml
world-settings:
  default:
    structures:
      stronghold: false
      village: false
      mansion: false
```

## Désactiver les biomes souterrains spéciaux (lush caves, dripstone)

Dans `server.properties` ou via WorldCreator :
- Le BiomeProvider du plugin (`LGBiomeProvider`) ne déclare que `DARK_FOREST`, `FOREST`, `PLAINS`
- → Paper/Spigot ne génère pas d'autres biomes dans les zones contrôlées

## Règles de jeu appliquées automatiquement

Le plugin configure automatiquement :
- `naturalRegeneration = false` (UHC)
- `showDeathMessages = false`
- Météo désactivée
- PNJ Villageois bloqués (EntitySpawnEvent)
- WorldBorder 1000×1000 centrée sur (0,0)

## Structure de la map

```
         [Forêt Sombre 500 blocs]
          ████████████████
       ████████████████████████
      █████████ SPAWN ██████████
       ████████(0,0)███████████
          ████████████████

         [Forêt 250 blocs]
         [Plaines extérieur]
         [WorldBorder à 500 blocs]
```

## Commandes utiles post-génération

```
/worldborder center 0 0
/worldborder set 1000
/gamerule naturalRegeneration false
/gamerule doImmediateRespawn false
```
