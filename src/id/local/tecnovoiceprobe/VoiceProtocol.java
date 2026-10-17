package id.local.tecnovoiceprobe;

/** Vendor protocol boundary: never send unchecked package names or mode IDs. */
final class VoiceProtocol {
  static final String ML = "com.mobile.legends", WA = "com.whatsapp", KEY = "TranMagiqueMode";
  static final String[] NAMES = {"Normal", "Agak berat", "Berat", "Agak tinggi", "Tinggi"};

  static boolean supported(String pkg) {
    return ML.equals(pkg) || WA.equals(pkg);
  }

  static String appName(String pkg) {
    return ML.equals(pkg) ? "Mobile Legends" : WA.equals(pkg) ? "WhatsApp" : pkg;
  }

  static String command(String pkg, int effect) {
    if (!supported(pkg) || effect < 0 || effect >= NAMES.length)
      throw new IllegalArgumentException("Target atau efek tidak didukung");
    return KEY + "=" + pkg + "," + effect;
  }

  static final class State {
    final String pkg, raw;
    final int effect;

    State(String pkg, int effect, String raw) {
      this.pkg = pkg;
      this.effect = effect;
      this.raw = raw;
    }

    boolean matches(String target, int mode) {
      return target.equals(pkg) && mode == effect;
    }

    String label() {
      return effect >= 0 ? appName(pkg) + " · " + NAMES[effect] : "Status belum tersedia";
    }
  }

  static State parse(String raw) {
    try {
      if (raw != null) {
        String value = raw.trim();
        if (value.startsWith(KEY + "=")) value = value.substring(KEY.length() + 1);
        if (value.endsWith(";")) value = value.substring(0, value.length() - 1).trim();
        String[] parts = value.split(",", -1);
        if (parts.length == 2) {
          String pkg = parts[0].trim();
          int effect = Integer.parseInt(parts[1].trim());
          if (supported(pkg) && effect >= 0 && effect < NAMES.length)
            return new State(pkg, effect, raw);
        }
      }
    } catch (NumberFormatException ignored) {
    }
    return new State("", -1, raw);
  }
}
