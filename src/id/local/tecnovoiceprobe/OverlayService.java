package id.local.tecnovoiceprobe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class OverlayService extends Service {
  static boolean running;
  private static final String CHANNEL = "voice_overlay", STOP = "id.local.tecnovoiceprobe.STOP";
  private VoiceControl voice;
  private WindowManager manager;
  private WindowManager.LayoutParams params;
  private LinearLayout root;
  private View panel;
  private TextView status;
  private final Button[] effects = new Button[5];
  private Button ml, wa;
  private boolean attached, expanded;
  private final Handler handler = new Handler();
  private final Runnable poll =
      new Runnable() {
        public void run() {
          if (!Settings.canDrawOverlays(OverlayService.this)) {
            stopSelf();
            return;
          }
          refreshPanel();
          handler.postDelayed(this, 1500);
        }
      };

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    voice = new VoiceControl(this);
    Context windowContext = this;
    if (Build.VERSION.SDK_INT >= 30) {
      Display display =
          ((DisplayManager) getSystemService(DISPLAY_SERVICE)).getDisplay(Display.DEFAULT_DISPLAY);
      windowContext =
          createDisplayContext(display)
              .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null);
    }
    manager = (WindowManager) windowContext.getSystemService(WINDOW_SERVICE);
    NotificationManager notifications =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notifications.createNotificationChannel(
        new NotificationChannel(
            CHANNEL, "Kontrol suara mengambang", NotificationManager.IMPORTANCE_LOW));
    PendingIntent open =
        PendingIntent.getActivity(
            this,
            0,
            new Intent(this, MainActivity.class),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    PendingIntent stop =
        PendingIntent.getService(
            this,
            1,
            new Intent(this, OverlayService.class).setAction(STOP),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    Notification n =
        new Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("VoxPanel aktif")
            .setContentText("Ketuk VP untuk mengganti efek. Geser untuk memindahkan.")
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(new Notification.Action.Builder(null, "Normal & tutup", stop).build())
            .build();
    startForeground(7, n);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int id) {
    if (intent != null && STOP.equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
    }
    if (!Settings.canDrawOverlays(this)) {
      Toast.makeText(
              this, "Izinkan Tampil di atas aplikasi lain terlebih dahulu.", Toast.LENGTH_LONG)
          .show();
      stopSelf();
      return START_NOT_STICKY;
    }
    if (!attached) {
      try {
        show();
        running = true;
        handler.post(poll);
      } catch (RuntimeException e) {
        Toast.makeText(this, "Overlay gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
        stopSelf();
      }
    }
    return START_NOT_STICKY;
  }

  private void show() {
    root = Ui.column(this, 6);
    root.setBackground(Ui.background(Ui.BG, 18, this));
    Button handle = Ui.button(this, "VP", true, () -> toggle(!expanded));
    root.addView(handle);
    handle.setContentDescription("Kontrol suara. Ketuk untuk membuka, geser untuk memindahkan.");
    ScrollView scroll = new ScrollView(this);
    LinearLayout contents = Ui.column(this, 5);
    scroll.addView(contents);
    panel = scroll;
    status = Ui.text(this, "", 12, Ui.ACCENT);
    contents.addView(status);
    ml = Ui.button(this, "Target: ML", false, () -> choose(VoiceControl.ML));
    contents.addView(ml);
    wa = Ui.button(this, "Target: WhatsApp", false, () -> choose(VoiceControl.WA));
    contents.addView(wa);
    for (int i = 0; i < 5; i++) {
      final int effect = i;
      effects[i] =
          Ui.button(
              this,
              VoiceControl.NAMES[i],
              i == 0,
              () -> {
                try {
                  if (effect == 0) voice.normal();
                  else voice.effect(effect);
                  refreshPanel();
                } catch (RuntimeException e) {
                  Toast.makeText(this, "Efek gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
              });
      contents.addView(effects[i]);
    }
    contents.addView(Ui.button(this, "Kecilkan", false, () -> toggle(false)));
    contents.addView(Ui.button(this, "Normal & tutup", false, () -> stopSelf()));
    root.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
    scroll.setVisibility(View.GONE);
    params =
        new WindowManager.LayoutParams(
            Ui.dp(this, 64),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
    params.gravity = Gravity.TOP | Gravity.LEFT;
    params.x = Ui.dp(this, 12);
    params.y = Ui.dp(this, 90);
    handle.setOnTouchListener(
        new View.OnTouchListener() {
          float downX, downY;
          int startX, startY;
          boolean moved;

          public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
              case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                startX = params.x;
                startY = params.y;
                moved = false;
                return true;
              case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - downX, dy = event.getRawY() - downY;
                if (Math.abs(dx) + Math.abs(dy) > Ui.dp(OverlayService.this, 8)) moved = true;
                if (moved) {
                  params.x = startX + (int) dx;
                  params.y = startY + (int) dy;
                  clamp();
                  update();
                }
                return true;
              case MotionEvent.ACTION_UP:
                if (!moved) view.performClick();
                else savePosition();
                return true;
              case MotionEvent.ACTION_CANCEL:
                return true;
            }
            return false;
          }
        });
    restorePosition();
    manager.addView(root, params);
    attached = true;
    refreshPanel();
  }

  private void refreshPanel() {
    if (status == null) return;
    VoiceProtocol.State s = voice.state();
    status.setText(s.label() + "\nTarget: " + VoiceControl.appName(voice.target()));
    Ui.style(ml, VoiceControl.ML.equals(voice.target()));
    Ui.style(wa, VoiceControl.WA.equals(voice.target()));
    for (int i = 0; i < effects.length; i++)
      if (effects[i] != null) {
        boolean active = s.matches(voice.target(), i);
        effects[i].setText((active ? "✓  " : "") + VoiceControl.NAMES[i]);
        Ui.style(effects[i], active);
      }
  }

  private void choose(String pkg) {
    try {
      voice.choose(pkg);
      refreshPanel();
    } catch (RuntimeException e) {
      Toast.makeText(this, "Target gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private String orientationKey() {
    return screenWidth() > screenHeight() ? "landscape" : "portrait";
  }

  private void savePosition() {
    getSharedPreferences("overlay_position", MODE_PRIVATE)
        .edit()
        .putFloat(
            orientationKey() + "_x", (float) params.x / Math.max(1, screenWidth() - params.width))
        .putFloat(
            orientationKey() + "_y",
            (float) params.y / Math.max(1, screenHeight() - root.getHeight() - Ui.dp(this, 30)))
        .apply();
  }

  private void restorePosition() {
    android.content.SharedPreferences p = getSharedPreferences("overlay_position", MODE_PRIVATE);
    String key = orientationKey();
    if (p.contains(key + "_x")) {
      params.x = (int) (p.getFloat(key + "_x", 0) * Math.max(0, screenWidth() - params.width));
      params.y =
          (int) (p.getFloat(key + "_y", 0.15f) * Math.max(0, screenHeight() - Ui.dp(this, 106)));
    }
    clamp();
  }

  private Point screenSize() {
    Point size = new Point();
    if (Build.VERSION.SDK_INT >= 30) {
      android.graphics.Rect bounds = manager.getMaximumWindowMetrics().getBounds();
      size.set(bounds.width(), bounds.height());
    } else manager.getDefaultDisplay().getSize(size);
    return size;
  }

  private int screenWidth() {
    return screenSize().x;
  }

  private int screenHeight() {
    return screenSize().y;
  }

  private void toggle(boolean open) {
    expanded = open;
    panel.setVisibility(open ? View.VISIBLE : View.GONE);
    params.width = Ui.dp(this, open ? 222 : 64);
    params.height =
        open
            ? Math.min(
                Ui.dp(this, 520), Math.max(Ui.dp(this, 180), screenHeight() - Ui.dp(this, 60)))
            : WindowManager.LayoutParams.WRAP_CONTENT;
    clamp();
    update();
  }

  private void clamp() {
    int height =
        params.height > 0
            ? params.height
            : (root.getHeight() > 0 ? root.getHeight() : Ui.dp(this, 76));
    params.x = Math.max(0, Math.min(params.x, Math.max(0, screenWidth() - params.width)));
    params.y =
        Math.max(0, Math.min(params.y, Math.max(0, screenHeight() - height - Ui.dp(this, 30))));
  }

  private void update() {
    if (attached) {
      try {
        manager.updateViewLayout(root, params);
      } catch (RuntimeException e) {
        stopSelf();
      }
    }
  }

  @Override
  public void onConfigurationChanged(Configuration c) {
    super.onConfigurationChanged(c);
    if (attached) {
      toggle(false);
      restorePosition();
      update();
    }
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacks(poll);
    running = false;
    if (attached) {
      try {
        voice.normal();
      } catch (RuntimeException e) {
        Toast.makeText(
                this,
                "Reset belum terkonfirmasi. Buka VoxPanel dan tekan Normal.",
                Toast.LENGTH_LONG)
            .show();
      }
      try {
        manager.removeView(root);
      } catch (RuntimeException ignored) {
      }
      attached = false;
    }
    stopForeground(STOP_FOREGROUND_REMOVE);
    super.onDestroy();
  }
}
