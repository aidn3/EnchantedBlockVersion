
package com.aidn5.enchantedblockversion;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.plugin.java.JavaPlugin;

import protocolsupport.api.ProtocolVersion;

/**
 * Plugin's main instance contains all the instances and objects of the plugin.
 * It also provides static method {@link #getInstance()} to access the plugin
 * directly.
 *
 * <p>The plugin has also api ({@link #isWhitelisted(ProtocolVersion)},
 * {@link #isBlacklisted(ProtocolVersion)}). Use {@link #getProtocol(String)}
 * to convert from minecraft version to protocol version. The configurations are
 * parsed and processed by a stand-alone class {@link #getConfigInstance()}.
 *
 * @author aidn5
 *
 * @see #getInstance()
 * @see #isWhitelisted(ProtocolVersion)
 * @see #isBlacklisted(ProtocolVersion)
 * @see #getProtocol(String)
 * @see #getConfigInstance()
 */
public class EnchantedBlockVersion extends JavaPlugin {
  @Nullable
  private static volatile EnchantedBlockVersion instance = null;

  @Nullable
  private volatile Config config = null;
  @Nullable
  private VersionReminder versionReminder;
  @Nullable
  private volatile OnLoginListener onLoginListener;

  /**
   * Get the instance of the plugin.
   *
   * @return the instance of the plugin,
   *         or <code>null</code> if the plugin is
   *         not enabled for the first time yet.
   */
  @Nullable
  public static EnchantedBlockVersion getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    instance = this;

    saveDefaultConfig();
    reloadConfig();

    config = new Config(this);
    config.reload();

    onLoginListener = new OnLoginListener(this);
    versionReminder = new VersionReminder(this);

    getServer().getPluginManager().registerEvents(onLoginListener, this);
  }

  @Override
  public void onDisable() {
    config = null;
    onLoginListener = null;

    versionReminder.shutdown();
    versionReminder = null;
  }

  /**
   * Get the configuration instance of the plugin.
   *
   * @return the configuration instance of the plugin,
   *         or <code>null</code> if the plugin is disabled.
   *
   * @see Config
   */
  @Nullable
  public Config getConfigInstance() {
    return config;
  }

  /**
   * Get the reminder instance of the plugin.
   *
   * @return the reminder instance of the plugin,
   *         or <code>null</code> if the plugin is disabled.
   */
  @Nullable
  public VersionReminder getVersionReminder() {
    return versionReminder;
  }

  /**
   * Check if a protocol is whitelisted in the configurations.
   *
   * @param protocolVersion
   *          the protocol to check.
   * @return <code>true</code> if it is whitelisted.
   *
   * @throws NullPointerException
   *           if <code>protocolVersion</code> is <code>null</code>.
   *
   * @see #isBlacklisted(ProtocolVersion)
   * @see #getProtocol(String)
   */
  public boolean isWhitelisted(@Nonnull ProtocolVersion protocolVersion)
      throws NullPointerException {

    Objects.requireNonNull(protocolVersion, "protocolVersion must not be null");
    return getConfigInstance().getWhitelistedVersionsInstance().contains(protocolVersion);
  }

  /**
   * Check if a protocol is blacklisted in the configurations.
   *
   * @param protocolVersion
   *          the protocol to check.
   *
   * @return <code>true</code> if it is blacklisted.
   *
   * @throws NullPointerException
   *           if <code>protocolVersion</code> is <code>null</code>.
   *
   * @see #isWhitelisted(ProtocolVersion)
   * @see #getProtocol(String)
   */
  public boolean isBlacklisted(@Nonnull ProtocolVersion protocolVersion)
      throws NullPointerException {

    Objects.requireNonNull(protocolVersion, "protocolVersion must not be null");
    return getConfigInstance().getBlacklistVersionsInstance().contains(protocolVersion);
  }

  /**
   * Get the recommended version the player should use on the server for the
   * utmost experience.
   *
   * @return
   *         the recommend minecraft version the
   *         player should use on the server,
   *         or <code>null</code> if it is disabled.
   *
   * @see Config#getRecommendedVersion()
   * @see Config#getRecommendMessage()
   * @see #getConfigInstance()
   */
  @Nullable
  public ProtocolVersion getRecommendedVersion() {
    return getConfigInstance().getRecommendedVersion();
  }

  /**
   * Get the protocol for the given <code>version</code>.
   * This method support both version types, minecraft version like "1.12.2" and
   * ProtocolSupport version from {@link ProtocolVersion} like "MINECRAFT_1_12_2".
   * <code>null</code> is returned if no associated version is found.
   * {@link ProtocolVersion#UNKNOWN} can be used if <code>null</code> is returned.
   *
   * @param version
   *          the version of the protocol to get.
   * @return the protocol in {@link ProtocolVersion},
   *         or <code>null</code> if not found
   *         or <code>version</code> is <code>null</code>.
   */
  @Nullable
  public static ProtocolVersion getProtocol(@Nullable String version) {
    final String prefixEnum = "MINECRAFT_";

    if (version == null || version.isEmpty()) {
      return null;
    }
    try {
      return ProtocolVersion.valueOf(version);
    } catch (Exception ignored) {}

    try {
      String constructedVersion = prefixEnum
          + Pattern.compile(".", Pattern.LITERAL).matcher(version).replaceAll("_");
      return ProtocolVersion.valueOf(constructedVersion);
    } catch (Exception ignored) {}

    return null;
  }
}
