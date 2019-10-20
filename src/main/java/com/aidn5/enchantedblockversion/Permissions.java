
package com.aidn5.enchantedblockversion;

import org.bukkit.entity.Player;

/**
 * Class contains all the used permissions as static variables. Can be used with
 * {@link Player#hasPermission(String)}.
 *
 * @author aidn5
 */
public class Permissions {
  private Permissions() {
    throw new AssertionError();
  }

  /**
   * allow player to connect using the blacklisted versions,
   * as long as the version is found in the whitelist.
   */
  public static final String BYPASS_BLACKLIST = "eblockversion.bypass.blacklist";
  /**
   * allow player to connect using any version.
   */
  public static final String BYPASS_ALL = "eblockversion.bypass.all";
  /**
   * do not send chat notification to the the player
   * if they are connected using the bypass.
   */
  public static final String DISABLE_NOTIFY = "eblockversion.bypass.disableNotify";
}
