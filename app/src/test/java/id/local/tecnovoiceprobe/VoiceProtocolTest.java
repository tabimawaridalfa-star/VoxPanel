package id.local.tecnovoiceprobe;

import static org.junit.Assert.*;

import org.junit.Test;

public class VoiceProtocolTest {
  @Test
  public void readsActualDeviceResponse() {
    assertTrue(VoiceProtocol.parse("com.whatsapp,2").matches(VoiceProtocol.WA, 2));
    assertTrue(VoiceProtocol.parse("com.mobile.legends,0").matches(VoiceProtocol.ML, 0));
  }

  @Test
  public void readsKeyedAndWhitespaceResponses() {
    assertTrue(
        VoiceProtocol.parse("  TranMagiqueMode=com.whatsapp, 4;  ").matches(VoiceProtocol.WA, 4));
  }

  @Test
  public void missingAndMalformedResponsesNeverBecomeNormal() {
    for (String response :
        new String[] {
          null,
          "",
          "com.whatsapp",
          "com.whatsapp,",
          "com.whatsapp,abc",
          "com.whatsapp,0,2",
          "com.whatsapp,-1",
          "com.whatsapp,5",
          "com.whatsapp,99999999999",
          "other.app,1"
        }) {
      assertEquals(
          "Unexpected valid state for " + response, -1, VoiceProtocol.parse(response).effect);
    }
  }

  @Test
  public void emitsOnlySupportedVendorCommands() {
    assertEquals(
        "TranMagiqueMode=com.mobile.legends,2", VoiceProtocol.command(VoiceProtocol.ML, 2));
    assertEquals("TranMagiqueMode=com.whatsapp,0", VoiceProtocol.command(VoiceProtocol.WA, 0));
  }

  @Test
  public void rejectsArbitraryCommandsAndOutOfRangePresets() {
    for (String pkg :
        new String[] {null, "", "other.app", "com.whatsapp;pitch=9", "com.whatsapp,2"}) {
      try {
        VoiceProtocol.command(pkg, 1);
        fail("Accepted invalid target");
      } catch (IllegalArgumentException expected) {
      }
    }
    for (int mode : new int[] {-1, 5, 100}) {
      try {
        VoiceProtocol.command(VoiceProtocol.ML, mode);
        fail("Accepted invalid preset");
      } catch (IllegalArgumentException expected) {
      }
    }
  }
}
