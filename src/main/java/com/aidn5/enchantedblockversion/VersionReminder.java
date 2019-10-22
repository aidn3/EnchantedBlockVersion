
package com.aidn5.enchantedblockversion;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;


/**
 * Class timer used to send time-fixed messages to players who connected to the
 * server using not-allowed protocols by using their bypass permission.
 *
 * <p>The timer will be disabled if {@link Config#getRepeatBypassMessage()} is
 * smaller than <code>0</code>
 *
 * @author aidn5
 *
 * @see Config#getRepeatBypassMessage()
 * @see Config#getBypassMessage()
 */
public class VersionReminder {
  @Nonnull
  private final EnchantedBlockVersion pluginInstance;
  @Nonnull
  private final Timer schuduler = new Timer(true);
  private final int repeat;

  VersionReminder(@Nonnull EnchantedBlockVersion pluginInstance) {
    this.pluginInstance = Objects
        .requireNonNull(pluginInstance, "pluginInstance must not be null");

    repeat = pluginInstance.getConfigInstance().getRepeatBypassMessage();

    if (repeat <= 0) {
      return;
    }

    schuduler.schedule(new TimerTask() {
      @Override
      public void run() {
        Bukkit.getScheduler().runTask(pluginInstance, () -> {
          sendMessageToAll();
        });
      }
    }, 200, repeat * 1000);
  }

  /**
   * shutdown the thread and cancel all the scheduled tasks.
   * The instance of this class will be useless. Create new instance if needed.
   */
  void shutdown() {
    schuduler.cancel();
  }

  /**
   * Remind the player about their bypass by sending a chat message to them.
   * Works only when the repeat status is <code>0</code> or bigger.
   *
   * @param player
   *          the player to notify.
   *
   * @see Config#getRepeatBypassMessage()
   * @see EnchantedBlockVersion#getConfigInstance()
   * @see Config#getBypassMessage()
   */
  public void remindPlayer(@Nonnull Player player) {
    if (repeat >= 0) {
      player.sendMessage(pluginInstance.getConfigInstance().getBypassMessage());
    }
  }

  /**
   * Recommend the player to use the specified minecraft version in the
   * settings for the utmost experience.
   *
   * @param player
   *          the player to notify/recommend
   *
   * @see Config#getRecommendedVersion()
   * @see Config#getRecommendMessage()
   * @see EnchantedBlockVersion#getConfigInstance()
   */
  public void recommendPlayer(@Nonnull Player player) {
    player.sendMessage(pluginInstance.getConfigInstance().getRecommendMessage());
  }

  private void sendMessageToAll() {
    final String bypassMessage = pluginInstance.getConfigInstance().getBypassMessage();
    final List<Connection> connections = ProtocolSupportAPI.getConnections();

    for (Connection connection : connections) {
      ProtocolVersion protocolVersion = connection.getVersion();

      if (!pluginInstance.isWhitelisted(protocolVersion)
          || pluginInstance.isBlacklisted(protocolVersion)) {

        final Player player = connection.getPlayer();

        if (player != null
            && !player.hasPermission(Permissions.DISABLE_NOTIFY)) {

          player.sendMessage(bypassMessage);
        }
      }
    }
  }
}
