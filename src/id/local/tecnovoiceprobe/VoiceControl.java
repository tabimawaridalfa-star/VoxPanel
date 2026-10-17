package id.local.tecnovoiceprobe;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

final class VoiceControl {
  static final String ML = VoiceProtocol.ML, WA = VoiceProtocol.WA;
  static final String[] NAMES = VoiceProtocol.NAMES;
  private final AudioManager audio;
  private final SharedPreferences prefs;

  VoiceControl(Context c) {
    audio = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
    prefs = c.getSharedPreferences("voice_control", Context.MODE_PRIVATE);
  }

  String target() {
    String target = prefs.getString("target", ML);
    return VoiceProtocol.supported(target) ? target : ML;
  }

  static String appName(String pkg) {
    return VoiceProtocol.appName(pkg);
  }

  VoiceProtocol.State state() {
    try {
      return VoiceProtocol.parse(audio.getParameters(VoiceProtocol.KEY));
    } catch (RuntimeException e) {
      return VoiceProtocol.parse(null);
    }
  }

  void choose(String pkg) {
    if (!ML.equals(pkg) && !WA.equals(pkg))
      throw new IllegalArgumentException("Target tidak didukung");
    VoiceProtocol.State s = state();
    if (s.effect > 0 && !pkg.equals(s.pkg)) resetChecked(s.pkg);
    else if (s.effect < 0) {
      String last = prefs.getString("lastTarget", null);
      if (VoiceProtocol.supported(last) && !last.equals(pkg)) resetChecked(last);
    }
    prefs.edit().putString("target", pkg).apply();
  }

  String effect(int effect) {
    if (effect < 0 || effect >= NAMES.length)
      throw new IllegalArgumentException("Efek tidak valid");
    VoiceProtocol.State current = state();
    String pkg = target();
    if (current.effect > 0 && !current.pkg.equals(pkg)) resetChecked(current.pkg);
    send(pkg, effect);
    VoiceProtocol.State after = state();
    return after.effect == effect && pkg.equals(after.pkg)
        ? "Parameter terbaca: " + NAMES[effect] + "."
        : "Perintah dikirim; status belum cocok. Coba refresh dan uji suara.";
  }

  void normal() {
    VoiceProtocol.State s = state();
    String pkg = s.effect >= 0 ? s.pkg : prefs.getString("lastTarget", target());
    resetChecked(VoiceProtocol.supported(pkg) ? pkg : target());
  }

  private void resetChecked(String pkg) {
    send(pkg, 0);
    VoiceProtocol.State after = state();
    if (!after.matches(pkg, 0))
      throw new IllegalStateException(
          "Normal belum terkonfirmasi. Coba kembali sebelum mengganti target.");
  }

  private void send(String pkg, int effect) {
    audio.setParameters(VoiceProtocol.command(pkg, effect));
    prefs.edit().putString("lastTarget", pkg).putInt("lastEffect", effect).apply();
  }
}
