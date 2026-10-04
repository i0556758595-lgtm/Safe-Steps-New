package com.example.safesteps;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class GuardAccessibilityService extends AccessibilityService {

    private final Handler handler = new Handler();
    private WindowManager windowManager;
    private LinearLayout overlay;
    private boolean blocking = false;
    private long lastBlockTime = 0;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        if (!getSharedPreferences(MainActivity.PREFS, 0)
                .getBoolean(MainActivity.ENABLED, false)) {
            return;
        }

        String packageName = String.valueOf(event.getPackageName());

        if (getPackageName().equals(packageName)) return;

        if (isForbidden(event, packageName)) {
            blockCurrentScreen();
        }
    }

    private boolean isForbidden(AccessibilityEvent event, String packageName) {
        String className = lower(event.getClassName());
        String text = lower(event.getText());
        String description = lower(event.getContentDescription());
        String allText = text + " " + description;

        // מסכי התקנה ואישור התקנה
        if (packageName.equals("com.android.packageinstaller")
                || packageName.equals("com.google.android.packageinstaller")
                || packageName.equals("com.android.permissioncontroller")
                || packageName.equals("com.google.android.permissioncontroller")) {
            return true;
        }

        if (!packageName.equals("com.android.settings")) {
            return false;
        }

        // מסכי נגישות
        if (className.contains("accessibility")
                || className.contains("installedaccessibility")
                || (className.contains("subsettings")
                && (allText.contains("נגישות")
                || allText.contains("accessibility")))) {
            return true;
        }

        // מסכי מנהלי מכשיר
        if (className.contains("deviceadmin")
                || className.contains("device_admin")) {
            return true;
        }

        /*
         * חסימת מסך רשימת האפליקציות שמנהלות את המכשיר.
         * הניסוחים משתנים בין גרסאות Android ויצרנים שונים,
         * לכן נבדקים כמה ניסוחים אפשריים.
         */
        if (allText.contains("מנהלי המכשיר")
                || allText.contains("מנהלי מכשירים")
                || allText.contains("אפליקציות שמנהלות את המכשיר")
                || allText.contains("יישומים שמנהלים את המכשיר")
                || allText.contains("אפליקציות מנהלות את המכשיר")
                || allText.contains("יישומים מנהלים את המכשיר")
                || allText.contains("אפליקציות שמנהלות מכשיר זה")
                || allText.contains("יישומים שמנהלים מכשיר זה")
                || allText.contains("device administrators")
                || allText.contains("device administrator")
                || allText.contains("device admin apps")
                || allText.contains("device admin")) {
            return true;
        }

        // פרטי האפליקציה Safe Steps בהגדרות
        boolean appDetailsScreen =
                className.contains("appdetails")
                || className.contains("installedappdetails")
                || className.contains("manageapplications")
                || className.contains("applicationdetails")
                || className.contains("applicationinfo")
                || className.contains("appinfo");

        return appDetailsScreen
                && (allText.contains("safe steps")
                || allText.contains("safesteps"));
    }

    private String lower(CharSequence value) {
        return String.valueOf(value == null ? "" : value)
                .toLowerCase(Locale.ROOT);
    }

    // AccessibilityEvent.getText() מחזיר רשימה ולא מחרוזת יחידה.
    private String lower(List<CharSequence> values) {
        if (values == null || values.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        for (CharSequence value : values) {
            if (value != null) {
                if (result.length() > 0) result.append(' ');
                result.append(value);
            }
        }

        return result.toString().toLowerCase(Locale.ROOT);
    }

    private void blockCurrentScreen() {
        long now = System.currentTimeMillis();

        if (blocking || now - lastBlockTime < 100) return;

        lastBlockTime = now;
        blocking = true;

        showProtectionScreen();

        handler.postDelayed(
                () -> performGlobalAction(GLOBAL_ACTION_BACK), 10);

        handler.postDelayed(
                () -> performGlobalAction(GLOBAL_ACTION_HOME), 40);
    }

    private void showProtectionScreen() {
        if (windowManager == null || overlay != null) {
            blocking = false;
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(7, 29, 64),
                        Color.rgb(34, 112, 188),
                        Color.rgb(61, 46, 130)
                }));

        TextView shield = label("🛡", 68, true);
        root.addView(shield, new LinearLayout.LayoutParams(
                -1, dp(105)));

        TextView title = label("הפעולה חסומה", 29, true);
        root.addView(title, new LinearLayout.LayoutParams(
                -1, dp(60)));

        TextView message = label(
                "מכשיר זה מוגן על ידי Safe Steps", 17, false);
        root.addView(message, new LinearLayout.LayoutParams(
                -1, dp(55)));

        TextView waitMessage = label(
                "המסך נסגר. יש להמתין שתי שניות…", 15, false);
        root.addView(waitMessage, new LinearLayout.LayoutParams(
                -1, dp(55)));

        Button backButton = new Button(this);
        backButton.setText("חזור");
        backButton.setTextSize(17);
        backButton.setAllCaps(false);
        backButton.setEnabled(false);

        backButton.setOnClickListener(view -> {
            performGlobalAction(GLOBAL_ACTION_HOME);
            removeProtectionScreen();
        });

        root.addView(backButton, new LinearLayout.LayoutParams(
                -1, dp(58)));

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        -1,
                        -1,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        PixelFormat.TRANSLUCENT);

        try {
            windowManager.addView(root, params);
            overlay = root;
        } catch (Exception exception) {
            blocking = false;
            return;
        }

        handler.postDelayed(() -> {
            if (overlay != root) return;

            backButton.setEnabled(true);
            waitMessage.setText(
                    "המסך נסגר. לחצו על חזור כדי לצאת.");
        }, 2000);
    }

    private TextView label(String text, float size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);

        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return view;
    }

    private int dp(float value) {
        return (int) (value
                * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void removeProtectionScreen() {
        if (overlay != null && windowManager != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {
            }
        }

        overlay = null;
        blocking = false;
    }

    @Override
    public void onInterrupt() {
        removeProtectionScreen();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        removeProtectionScreen();
        return super.onUnbind(intent);
    }
}
