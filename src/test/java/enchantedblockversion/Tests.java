
package enchantedblockversion;

import static org.junit.Assert.fail;

import com.aidn5.enchantedblockversion.EnchantedBlockVersion;

import org.junit.Test;

public class Tests {
  @Test
  public void testVersions() {
    final String[] versions = new String[] { "1.7.5", "1.8", "1.12.2", "1.14.4" };
    for (int i = 0; i < versions.length; i++) {
      if (EnchantedBlockVersion.getProtocol(versions[i]) == null) {
        fail("'" + versions[i] + "' is not parse-able.");
      }
    }
  }
}
