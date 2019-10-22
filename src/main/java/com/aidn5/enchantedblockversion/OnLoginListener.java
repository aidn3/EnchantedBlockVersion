
package com.aidn5.enchantedblockversion;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.api.events.PlayerLoginStartEvent;

/**
 * Class listens join events used to check the protocol version and decide
 * whether to allow or deny joining the server.
 *
 * @author aidn5
 *
 * @see VersionReminder
 */
class OnLoginListener implements Listener {
  /**
   * Delay in ticks for the first message (direct after logging in)
   * to not let it be drown by the other plugins' messages.
   */
  private static final int MESSAGE_DELAY = 20; // One second delay

  @Nonnull
  private final EnchantedBlockVersion parentInstance;
  private Object permissionProvider;

  /*
   * Config is not included in the constructor, since it is not final.
   * Parent is used instead to avoid further complexity.
   */
  OnLoginListener(@Nonnull EnchantedBlockVersion parentInstance)
      throws NullPointerException {

    this.parentInstance = Objects
        .requireNonNull(parentInstance, "parentInstance must not be null");

    try {
      RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager()
          .getRegistration(Permission.class);
      this.permissionProvider = Objects.requireNonNull(rsp.getProvider());

    } catch (Exception e) {
      parentInstance.getLogger().warning("Vault is not installed. Some features will be disabled");
      this.permissionProvider = null;
    }
  }

  /*
   * Block the connection if the version is not allowed.
   * If this method is disabled (due to lack of permission),
   * onPlayerJoin will be used to kick the player. This method exists
   * to avoid risks by allowing players to join the server and run code,
   * even if it is for a split of a second.
   * <p>Since Player instance is not access-able till the onPlayerLoginEvent,
   * Vault is used to take advantage of some permission managers, which support
   * offline players. We can only use offline players here, since there is a
   * Profile (offline-mode and only contains the username).
   */
  @SuppressWarnings("deprecation")
  @EventHandler
  public void onPlayerLogin(final PlayerLoginStartEvent e) {
    final ProtocolVersion usedVersion = ProtocolSupportAPI.getProtocolVersion(e.getAddress());
    final boolean bypassAll;
    final boolean bypassBlacklist;

    // Connection#getPlayer() is always null
    // Connection#getProfile() is offlineMode and contains only the username.
    Player player = e.getConnection().getPlayer();

    if (player != null) {
      bypassAll = player.hasPermission(Permissions.BYPASS_ALL);
      bypassBlacklist = player.hasPermission(Permissions.BYPASS_BLACKLIST);

    } else if (this.permissionProvider != null) {
      final OfflinePlayer offlinePlayer = Bukkit
          .getOfflinePlayer(e.getConnection().getProfile().getName());

      final Permission permP = (Permission) this.permissionProvider;
      bypassAll = permP.playerHas(null, offlinePlayer, Permissions.BYPASS_ALL);
      bypassBlacklist = permP.playerHas(null, offlinePlayer, Permissions.BYPASS_BLACKLIST);

    } else {
      // disable this feature if permissions not available at the moment
      // and rely on onPlayerJoin even to do the job.
      return;
    }


    // check blacklist first to override whitelist
    if (parentInstance.isBlacklisted(usedVersion)) {
      if (!bypassAll && !bypassBlacklist) {
        e.denyLogin(parentInstance.getConfigInstance().getBlacklistMessage());
      }

    } else if (!parentInstance.isWhitelisted(usedVersion)) {
      if (!bypassAll) {
        e.denyLogin(parentInstance.getConfigInstance().getWhitelistMessage());
      }
    }
  }

  /*
   * lowest priority is used, since there is no way to cancel the even.
   * Removing the chat join message is the least what we can do.
   */
  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(final PlayerJoinEvent e) {
    final Player player = e.getPlayer();
    final ProtocolVersion recommendedVersion = parentInstance.getRecommendedVersion();
    final ProtocolVersion usedVersion = ProtocolSupportAPI.getProtocolVersion(player);
    final boolean bypassAll = player.hasPermission(Permissions.BYPASS_ALL);
    final boolean bypassBlacklist = player.hasPermission(Permissions.BYPASS_BLACKLIST);

    // check blacklist first to override whitelist
    if (parentInstance.isBlacklisted(usedVersion)) {
      if (!bypassAll && !bypassBlacklist) {
        e.setJoinMessage(null);
        e.getPlayer().kickPlayer(parentInstance.getConfigInstance().getBlacklistMessage());

      } else {
        handleRemindPlayer(player);
      }

    } else if (!parentInstance.isWhitelisted(usedVersion)) {
      if (!bypassAll) {
        e.setJoinMessage(null);
        e.getPlayer().kickPlayer(parentInstance.getConfigInstance().getWhitelistMessage());

      } else {
        handleRemindPlayer(player);
      }
    }

    // null if it is disabled
    if (recommendedVersion != null) {
      if (usedVersion != recommendedVersion) {
        Bukkit.getScheduler().runTaskLater(parentInstance, () -> {
          parentInstance.getVersionReminder().remindPlayer(player);
        }, MESSAGE_DELAY);
      }
    }
  }

  private void handleRemindPlayer(@Nonnull final Player player) {
    Bukkit.getScheduler().runTaskLater(parentInstance, () -> {
      parentInstance.getVersionReminder().recommendPlayer(player);
    }, MESSAGE_DELAY);
  }
}
