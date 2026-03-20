package fr.enoe.loupgarou.core;

public enum GameState {
    WAITING,        // En attente dans la cage
    STARTING,       // Démarrage (attribution des rôles)
    RUNNING,        // Partie en cours
    NIGHT,          // Phase nuit
    DAY,            // Phase jour / vote
    ENDED           // Partie terminée
}
