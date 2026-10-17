package id.local.tecnovoiceprobe;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
  private VoiceControl voice;
  private TextView status, selected, feedback, overlayStatus;
  private final Button[] effects = new Button[5];
  private Button ml, wa;
  private final Handler handler = new Handler();
  private boolean waitingOverlay;
  private final Runnable poll =
      new Runnable() {
        public void run() {
          refresh();
          handler.postDelayed(this, 1500);
        }
      };

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    voice = new VoiceControl(this);
    waitingOverlay = state != null && state.getBoolean("waitingOverlay");
    getWindow().setStatusBarColor(Ui.BG);
    getWindow().setNavigationBarColor(Ui.BG);
    ScrollView scroll = new ScrollView(this);
    scroll.setBackgroundColor(Ui.BG);
    LinearLayout root = Ui.column(this, 22);
    scroll.addView(root);
    root.addView(Ui.text(this, "VOXPANEL   /   0.4", 12, Ui.ACCENT));
    root.addView(Ui.text(this, "Karakter suaramu,\nsatu sentuhan.", 29, Ui.TEXT));
    root.addView(
        Ui.text(
            this,
            "Empat karakter suara. Kontrol langsung dari layar permainan.\n"
                + "Menggunakan efek bawaan TECNO.",
            14,
            Ui.MUTED));
    LinearLayout card = Ui.column(this, 16);
    card.setBackground(Ui.background(Ui.CARD, 18, this));
    card.addView(Ui.text(this, "STATUS DARI SISTEM", 11, Ui.ACCENT));
    status = Ui.text(this, "Membaca…", 20, Ui.TEXT);
    card.addView(status);
    card.addView(
        Ui.text(
            this,
            "Status ini menunjukkan parameter efek, bukan analisis suara yang didengar teman.",
            12,
            Ui.MUTED));
    root.addView(card);
    root.addView(Ui.text(this, "Aplikasi target", 19, Ui.TEXT));
    selected = Ui.text(this, "", 14, Ui.ACCENT);
    root.addView(selected);
    ml = Ui.button(this, "Mobile Legends", false, () -> choose(VoiceControl.ML));
    root.addView(ml);
    wa = Ui.button(this, "WhatsApp", false, () -> choose(VoiceControl.WA));
    root.addView(wa);
    root.addView(Ui.text(this, "Pilih karakter", 19, Ui.TEXT));
    for (int i = 1; i <= 4; i++) {
      final int effect = i;
      effects[i] = Ui.button(this, VoiceControl.NAMES[i], false, () -> apply(effect));
      root.addView(effects[i]);
    }
    effects[0] = Ui.button(this, "Normal · suara asli", true, () -> normal());
    root.addView(effects[0]);
    feedback = Ui.text(this, "", 13, Ui.MUTED);
    root.addView(feedback);
    root.addView(Ui.text(this, "Kontrol saat bermain", 19, Ui.TEXT));
    overlayStatus = Ui.text(this, "", 13, Ui.MUTED);
    root.addView(overlayStatus);
    root.addView(Ui.button(this, "Tampilkan tombol mengambang", true, () -> requestOverlay()));
    root.addView(
        Ui.button(
            this,
            "Normal & tutup tombol mengambang",
            false,
            () -> {
              normal();
              stopService(new Intent(this, OverlayService.class));
              refresh();
            }));
    root.addView(
        Ui.button(
            this,
            "Pengaturan notifikasi",
            false,
            () ->
                startActivity(
                    new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()))));
    root.addView(
        Ui.text(
            this,
            "Geser VP untuk memindahkan kontrol. Ketuk untuk memilih target dan suara. Aktifkan"
                + " mikrofon game sebelum memilih efek.\n\n"
                + "Setelah selesai pilih Normal & tutup. Jika aplikasi dihentikan paksa, buka"
                + " VoxPanel dan tekan Normal.\n\n"
                + "Proyek independen. Diuji pada TECNO SPARK 20 / Android 13; perangkat lain belum"
                + " diverifikasi. Tidak merekam atau mengunggah suara.",
            13,
            Ui.MUTED));
    setContentView(scroll);
    refresh();
  }

  private void choose(String pkg) {
    try {
      voice.choose(pkg);
      feedback.setText(
          "Target: "
              + VoiceControl.appName(pkg)
              + ". Efek target sebelumnya direset bila terdeteksi aktif.");
    } catch (RuntimeException e) {
      error(e);
    }
    refresh();
  }

  private void apply(int effect) {
    try {
      feedback.setText(voice.effect(effect));
    } catch (RuntimeException e) {
      error(e);
    }
    refresh();
  }

  private void normal() {
    try {
      voice.normal();
      feedback.setText("Perintah Normal dikirim. Periksa status di atas.");
    } catch (RuntimeException e) {
      error(e);
    }
    refresh();
  }

  private void error(RuntimeException e) {
    feedback.setText("Gagal: " + e.getClass().getSimpleName() + " · " + e.getMessage());
  }

  private void refresh() {
    if (status == null) return;
    VoiceProtocol.State s = voice.state();
    status.setText(s.label());
    selected.setText("Dipilih: " + VoiceControl.appName(voice.target()));
    Ui.style(ml, VoiceControl.ML.equals(voice.target()));
    Ui.style(wa, VoiceControl.WA.equals(voice.target()));
    for (int i = 0; i < effects.length; i++)
      if (effects[i] != null) {
        boolean active = s.matches(voice.target(), i);
        effects[i].setText(
            (active ? "✓  " : "") + (i == 0 ? "Normal · suara asli" : VoiceControl.NAMES[i]));
        Ui.style(effects[i], active);
      }
    overlayStatus.setText(
        OverlayService.running
            ? "Tombol mengambang aktif."
            : Settings.canDrawOverlays(this)
                ? "Izin overlay tersedia. Tekan tombol untuk menampilkan."
                : "Perlu izin Tampil di atas aplikasi lain. Izin diminta saat tombol ditekan.");
  }

  private void requestOverlay() {
    if (!Settings.canDrawOverlays(this)) {
      waitingOverlay = true;
      try {
        startActivity(
            new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())));
      } catch (RuntimeException e) {
        waitingOverlay = false;
        error(e);
      }
      return;
    }
    startOverlay();
  }

  private void startOverlay() {
    if (Build.VERSION.SDK_INT >= 33
        && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        && !getPreferences(MODE_PRIVATE).getBoolean("notificationAsked", false)) {
      getPreferences(MODE_PRIVATE).edit().putBoolean("notificationAsked", true).apply();
      requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 41);
    }
    try {
      startForegroundService(new Intent(this, OverlayService.class));
      feedback.setText(
          "Menampilkan kontrol. Mengizinkan notifikasi menyediakan tombol Normal & Tutup di panel"
              + " notifikasi.");
    } catch (RuntimeException e) {
      error(e);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    handler.removeCallbacks(poll);
    handler.post(poll);
    if (waitingOverlay) {
      waitingOverlay = false;
      if (Settings.canDrawOverlays(this)) startOverlay();
    }
  }

  @Override
  protected void onPause() {
    handler.removeCallbacks(poll);
    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);
    state.putBoolean("waitingOverlay", waitingOverlay);
  }
}
