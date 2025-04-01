package it.versy.stellarlevels.events;

import it.versy.stellarlevels.StellarLevels;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Set;

public class KillEvent implements Listener {

    private final Set<EntityType> excludedMobs = EnumSet.of(
            EntityType.BAT, EntityType.CAT, EntityType.CHICKEN, EntityType.COD,
            EntityType.COW, EntityType.DONKEY, EntityType.FOX,
            EntityType.HORSE, EntityType.MULE, EntityType.OCELOT,
            EntityType.PARROT, EntityType.PIG, EntityType.RABBIT, EntityType.SALMON,
            EntityType.SHEEP, EntityType.SKELETON_HORSE,
            EntityType.SQUID, EntityType.TROPICAL_FISH, EntityType.TURTLE, EntityType.VILLAGER,
            EntityType.WANDERING_TRADER, EntityType.WOLF, EntityType.SILVERFISH,
            EntityType.ENDERMITE, EntityType.VEX, EntityType.BEE
    );

    private final StellarLevels plugin;

    public KillEvent(StellarLevels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            EntityType mobType = event.getEntity().getType();

            // Determina il mondo e l'EXP da assegnare
            double expGained = getExpForWorld(player.getWorld());

            // Aggiungi EXP al giocatore se è un mob ostile
            if (!(excludedMobs.contains(mobType))) {
                plugin.getLevelManager().addExp(player, expGained);
            }
        }
    }

    // Metodo per determinare quanta EXP assegnare in base al mondo
    private double getExpForWorld(World world) {
        switch (world.getEnvironment()) {
            case NORMAL: // Mondo normale
                return 5.0;
            case NETHER: // Nether
                return 15.0;
            case THE_END: // End
                return 20.0;
            default:
                return 0.0;
        }
    }
}
