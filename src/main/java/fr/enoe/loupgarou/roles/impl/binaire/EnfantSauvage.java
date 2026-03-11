package fr.enoe.loupgarou.roles.impl.binaire;
import fr.enoe.loupgarou.LoupGarouPlugin;
import fr.enoe.loupgarou.roles.Role;
import fr.enoe.loupgarou.roles.RoleFamily;
import fr.enoe.loupgarou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;
public class EnfantSauvage extends Role {
    private UUID mentorUUID=null;
    private boolean becameWolf=false;
    private RoleFamily family=RoleFamily.VILLAGE;
    public EnfantSauvage(LoupGarouPlugin p, UUID u) { super(p, u); }
    @Override public String getDisplayName() { return becameWolf?"§cEnfant Sauvage (Loup)":"§aEnfant Sauvage"; }
    @Override public String getDescription()  { return "§7/lg mentor <joueur> — si le mentor meurt, tu deviens loup."; }
    @Override public RoleFamily getFamily()   { return family; }
    @Override public String getId()           { return "enfant_sauvage"; }
    @Override public boolean isBinary()       { return true; }
    @Override public boolean onPowerCommand(Player player, String[] args) {
        if (!args[0].equalsIgnoreCase("mentor")) return false;
        if (mentorUUID!=null) { player.sendMessage(MessageUtils.error("Mentor déjà choisi !")); return true; }
        if (args.length<2)    { player.sendMessage(MessageUtils.error("Usage: /lg mentor <joueur>")); return true; }
        Player mentor = Bukkit.getPlayer(args[1]);
        if (mentor==null) { player.sendMessage(MessageUtils.error("Joueur introuvable.")); return true; }
        mentorUUID = mentor.getUniqueId();
        player.sendMessage(MessageUtils.success(mentor.getName() + " est ton mentor."));
        return true;
    }
    public UUID getMentorUUID() { return mentorUUID; }
    public void onMentorDied() {
        if (mentorUUID==null||becameWolf) return;
        becameWolf=true; family=RoleFamily.LOUP;
        Player p = Bukkit.getPlayer(playerUUID);
        if (p!=null) { p.sendMessage("§c[Enfant Sauvage] §7Ton mentor est mort ! Tu deviens LOUP !"); plugin.getRoleManager().addToWolfTeam(playerUUID); p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,Integer.MAX_VALUE,0,false,false,true)); }
    }
}
