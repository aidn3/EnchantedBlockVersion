
package com.aidn5.enchantedblockversion;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import protocolsupport.api.ProtocolVersion;

/**
 * Class manages, parses and processes the configuration and provides methods to
 * access the settings. <b>{@link #reload()} must be done before accessing the
 * settings.</b>
 *
 * @author aidn5
 */
public class Config {
  @Nonnull
  private final EnchantedBlockVersion pluginInstace;

  private boolean whitelistEnableStartEnd = false;
  @Nullable
  private ProtocolVersion whitelistStart = null;
  @Nullable
  private ProtocolVersion whitelistEnd = null;

  @Nonnull
  private Set<ProtocolVersion> blacklistVersions = new HashSet<>();
  @Nonnull
  private Set<ProtocolVersion> whitelistedVersions = new HashSet<>();

  @Nonnull
  private String whitelistMessage = "";
  @Nonnull
  private String blacklistMessage = "";
  @Nonnull
  private String bypassMessage = "";

  private int repeatBypassMessage = 600;

  /**
   * Constructor. Use {@link #reload()} to initiate the settings on the first run.
   *
   * @param pluginInstace
   *          an instance of the plugin to use {@link JavaPlugin#getConfig()}.
   */
  Config(@Nonnull EnchantedBlockVersion pluginInstace) {
    this.pluginInstace = Objects.requireNonNull(pluginInstace);
  }

  /**
   * Check if the whitelist version selector is enabled. It is used to select
   * multiple versions between two versions like selecting all version between 1.8
   * and 1.14.
   *
   * @return <code>true</code> if it is enabled.
   */
  public boolean isWhitelistStartEndEnabled() {
    return whitelistEnableStartEnd;
  }

  /**
   * Get the start version of the whitelist, if
   * {@link #isWhitelistStartEndEnabled()}.
   *
   * @return the start (oldest allowed protocol) of the whitelisted protocols,
   *         or <code>null</code> if it is disabled.
   */
  @Nullable
  public ProtocolVersion getWhitelistStart() {
    return whitelistStart;
  }

  /**
   * get the end version of the whitelist, if
   * {@link #isWhitelistStartEndEnabled()}.
   *
   * @return the end (newest allowed protocol) of the whitelisted protocols,
   *         or <code>null</code> if it is disabled.
   */
  @Nullable
  public ProtocolVersion getWhitelistEnd() {
    return whitelistEnd;
  }

  /**
   * Create a new instance with all the blacklisted versions.
   *
   * @return a separated instance contains all the blacklisted versions.
   */
  @Nonnull
  public Set<ProtocolVersion> getBlacklistVersions() {
    Set<ProtocolVersion> list = new HashSet<>(blacklistVersions.size());
    list.addAll(blacklistVersions);
    return list;
  }

  /**
   * Create a new instance with all the whitelisted versions.
   *
   * @return a separated instance contains all the whitelisted versions.
   */
  @Nonnull
  public Set<ProtocolVersion> getWhitelistedVersions() {
    Set<ProtocolVersion> list = new HashSet<>(whitelistedVersions.size());
    list.addAll(whitelistedVersions);
    return list;
  }

  /**
   * Get the interval in seconds for the message to send to player's chat,
   * reminding them to change their version, even if they have bypass.
   *
   * <p>If the number is <code>0</code>, it means the message should not be
   * repeated. <code>-1</code> means the message should not be sent at all.
   *
   * @return
   *         the interval in seconds for the repeat message.
   */
  public int getRepeatBypassMessage() {
    return repeatBypassMessage;
  }

  /**
   * Get the message to send to the player on kick if they are using a blacklisted
   * version (even if it is set on the whitelist).
   *
   * @return the message to show on blacklist.
   */
  @Nonnull
  public String getBlacklistMessage() {
    return blacklistMessage;
  }

  /**
   * Get the message to send to the player's chat, if they are using a
   * non-whitelisted version, reminding them to change the version.
   *
   * @return the message to show to the player.
   */
  @Nonnull
  public String getBypassMessage() {
    return bypassMessage;
  }

  /**
   * Get the message to send to the player on kick if the version they are using
   * is not whitelisted.
   *
   * @return the message to show to the player on kick.
   *
   * @see #getBlacklistMessage()
   */
  @Nonnull
  public String getWhitelistMessage() {
    return whitelistMessage;
  }

  /**
   * Get the used instance with all the blacklisted versions.
   *
   * @return main instance contains all the blacklisted versions.
   */
  @Nonnull
  Set<ProtocolVersion> getBlacklistVersionsInstance() {
    return blacklistVersions;
  }

  /**
   * Get the used instance with all the whitelisted versions.
   *
   * @return main instance contains all the whitelisted versions.
   */
  @Nonnull
  Set<ProtocolVersion> getWhitelistedVersionsInstance() {
    return whitelistedVersions;
  }

  /**
   * Reload the configuration and start parse and create the data and settings,
   * then apply them if no exception is thrown. <b>Reloading the configurations
   * does not guarantee it to be applied. Reload the plugin instead</b>
   *
   * @throws RuntimeException
   *           if any error occurs.
   */
  /*
   * - contains magic values of the configurations.
   * used there, since it is not used anywhere else.
   * - class variables are not touched during the reloading,
   * to avoid messing up on exception. They will be set at
   * the end of the method if the reload is success.
   * - whitelistStartEndSelector will be fused with
   * the individual whitelist versions to create one list.
   */
  void reload() throws RuntimeException {
    this.pluginInstace.reloadConfig();

    final FileConfiguration config = this.pluginInstace.getConfig();
    final Set<ProtocolVersion> tempWhitelistedVersions = new HashSet<>();
    final Set<ProtocolVersion> tempBlacklistedVersions = new HashSet<>();
    final boolean whitelistEnableStartEnd;
    final ProtocolVersion whitelistStart;
    final ProtocolVersion whitelistEnd;
    final String whitelistMessage;
    final String blacklistMessage;
    final String bypassMessage;


    // add whitelist protocols from "start" and "end" if enabled
    if (whitelistEnableStartEnd = config.getBoolean("whitelist.enableStartEnd")) {
      String whitelistStartString = config.getString("whitelist.start");
      whitelistStart = EnchantedBlockVersion.getProtocol(whitelistStartString);

      if (whitelistStart == null) {
        throw new RuntimeException("is the whitelist.start version valid? '"
            + String.valueOf(whitelistStartString) + "' is given.");
      }


      String whitelistEndString = config.getString("whitelist.end");
      whitelistEnd = EnchantedBlockVersion.getProtocol(whitelistEndString);

      if (whitelistEnd == null) {
        throw new RuntimeException("is the whitelist.end version valid? '"
            + String.valueOf(whitelistEndString) + "' is given.");
      }


      ProtocolVersion[] betweenWhitelist = ProtocolVersion
          .getAllBetween(whitelistStart, whitelistEnd);

      if (betweenWhitelist == null || betweenWhitelist.length == 0) {
        throw new RuntimeException("could not load whitelisted versions between: "
            + whitelistStart + " and " + whitelistEnd);
      }

      for (int i = 0; i < betweenWhitelist.length; i++) {
        tempWhitelistedVersions.add(betweenWhitelist[i]);
      }

    } else {
      whitelistStart = null;
      whitelistEnd = null;
    }

    // add whitelist protocols
    List<String> allowVersions = config.getStringList("whitelist.allowVersions");
    for (String version : allowVersions) {
      ProtocolVersion protocolVersion = EnchantedBlockVersion.getProtocol(version);

      if (protocolVersion == null) {
        throw new RuntimeException("is whitelist.allowVersions valid?"
            + " could not understand '" + version + "'");
      }

      tempWhitelistedVersions.add(protocolVersion);
    }

    // add blacklist protocols
    List<String> deniedVersions = config.getStringList("blacklist");
    for (String version : deniedVersions) {
      ProtocolVersion protocolVersion = EnchantedBlockVersion.getProtocol(version);

      if (protocolVersion == null) {
        throw new RuntimeException("is blacklist valid?"
            + " could not understand '" + version + "'");
      }

      tempBlacklistedVersions.add(protocolVersion);
    }


    // load the messages
    whitelistMessage = Objects.requireNonNull(
        config.getString("whitelistMessage"),
        "whitelistMessage in config.yml must not be null.");

    blacklistMessage = Objects.requireNonNull(
        config.getString("blacklistMessage"),
        "whitelistMessage in config.yml must not be null.");

    bypassMessage = Objects.requireNonNull(
        config.getString("bypassMessage"),
        "whitelistMessage in config.yml must not be null.");


    // after finishing reloading without any exception,
    // setting the variables and return
    this.whitelistEnableStartEnd = whitelistEnableStartEnd;
    this.whitelistStart = whitelistStart;
    this.whitelistEnd = whitelistEnd;

    this.whitelistedVersions = tempWhitelistedVersions;
    this.blacklistVersions = tempBlacklistedVersions;

    this.whitelistMessage = ChatColor.translateAlternateColorCodes('&', whitelistMessage);
    this.blacklistMessage = ChatColor.translateAlternateColorCodes('&', blacklistMessage);
    this.bypassMessage = ChatColor.translateAlternateColorCodes('&', bypassMessage);
    this.repeatBypassMessage = config.getInt("repeatBypassMessage");
  }
}
