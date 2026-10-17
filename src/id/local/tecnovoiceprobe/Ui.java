package id.local.tecnovoiceprobe;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
  static final int BG = Color.rgb(12, 18, 29),
      CARD = Color.rgb(23, 33, 49),
      TEXT = Color.rgb(235, 241, 250),
      MUTED = Color.rgb(160, 176, 197),
      ACCENT = Color.rgb(74, 222, 178);

  static int dp(Context c, int v) {
    return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
  }

  static GradientDrawable background(int color, int radius, Context c) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color);
    d.setCornerRadius(dp(c, radius));
    return d;
  }

  static LinearLayout column(Context c, int padding) {
    LinearLayout l = new LinearLayout(c);
    l.setOrientation(LinearLayout.VERTICAL);
    int p = dp(c, padding);
    l.setPadding(p, p, p, p);
    return l;
  }

  static TextView text(Context c, String label, int size, int color) {
    TextView t = new TextView(c);
    t.setText(label);
    t.setTextSize(size);
    t.setTextColor(color);
    t.setPadding(0, dp(c, 5), 0, dp(c, 7));
    return t;
  }

  static Button button(Context c, String label, boolean accent, Runnable action) {
    Button b = new Button(c);
    b.setText(label);
    b.setAllCaps(false);
    b.setTextSize(15);
    b.setTypeface(null, Typeface.BOLD);
    style(b, accent);
    b.setMinHeight(dp(c, 48));
    b.setMinimumHeight(dp(c, 48));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
    p.topMargin = dp(c, 8);
    b.setLayoutParams(p);
    b.setOnClickListener(v -> action.run());
    return b;
  }

  static void style(Button button, boolean active) {
    button.setTextColor(active ? BG : TEXT);
    button.setBackground(
        new RippleDrawable(
            ColorStateList.valueOf(0x447DDFC5),
            background(active ? ACCENT : CARD, 14, button.getContext()),
            null));
  }
}
