package fr.enoe.loupgarou.roles.impl.binaire;

import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.UUID;

public class Ivrogne extends Role {

    private Role fakeRole = null;

    public Ivrogne(LoupGarouPlugin p, UUID u) { super(p, u); }

    @Override public String getDisplayName() { return "§6Ivrogne"; }
    @Override public String getDescription() {
        return "§7Reçoit un faux rôle avec effets visibles (non fonctionnels). Croit être en couple. Gagne avec le village.";
    }
    @Override public RoleFamily getFamily()  { return RoleFamily.VILLAGE; }
    @Override public String getId()          { return "ivrogne"; }
    @Override public boolean isBinary()      { return true; }

    @Override
    public void onGameStart(Player player) {
        fakeRole = plugin.getRoleManager().getRandomRoleForDisplay();
        String fakeName = fakeRole != null ? fakeRole.getDisplayName() : "?";

        // NE PAS révéler les rôles au démarrage — sera révélé à 20 min comme les autres
        // (le message de faux rôle sera envoyé au moment de announceRoles dans GameManager)
        // On stocke juste le faux rôle pour l'utiliser plus tard

        // Créer le faux couple (sera notifié à 25 min avec les vrais couples)
        plugin.getCoupleManager().createFakeCouple(
            playerUUID,
            new ArrayList<>(plugin.getGameManager().getAlivePlayers())
        );

        // Effets VISIBLES mais amplitude 0 (quasi-neutre) — ambient=false, showIcon=true
        // L'ivrogne voit les icônes d'effets dans son inventaire
        applyFakeEffects(player);
    }

    /** Appelé par GameManager.announceRoles() à 20 min pour afficher le faux rôle */
    public void revealFakeRole(Player player) {
        String fakeName = fakeRole != null ? fakeRole.getDisplayName() : "?";
        player.sendMessage("§6§l╔══════════════════════════╗");
        player.sendMessage("§6§l║  §eTon rôle est révélé !  §6§l║");
        player.sendMessage("§6§l╚══════════════════════════╝");
        player.sendMessage("§eTon rôle : §b" + fakeName);
        player.sendMessage("§7§o(Vrai objectif : Gagner avec le Village !)");
    }

    private void applyFakeEffects(Player player) {
        if (fakeRole == null) return;
        String fid = fakeRole.getId();
        // Effets visibles avec ambient=false, particles=false, icon=true
        // → affiche l'icône mais aucune particule (discret)
        if (fid.contains("loup") || fid.equals("chevalier") || fid.equals("chasseur")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,   Integer.MAX_VALUE, 0, false, false, true));
        } else if (fid.equals("maire") || fid.equals("citoyen")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,      Integer.MAX_VALUE, 0, false, false, true));
        } else if (fid.equals("salvateur") || fid.equals("astronome")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false, true));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
        }
    }

    public Role getFakeRole() { return fakeRole; }
}
