package com.example.safesteps;

import android.app.*;
import android.app.admin.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {

    public static final String PREFS = "safe_steps";
    public static final String PASSWORD = "admin_password";
    public static final String TYPE = "password_type";
    public static final String ENABLED = "protection_enabled";
    public static final String PIN = "pin";
    public static final String PATTERN = "pattern";

    private boolean authed = false;
    private boolean settingsLaunch = false;

    private LinearLayout body;
    private TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        init();
        authenticate();
    }

    private void init() {
        SharedPreferences p = getSharedPreferences(PREFS, 0);

        if (!p.contains(PASSWORD)) {
            p.edit()
                    .putString(PASSWORD, "1234")
                    .putString(TYPE, PIN)
                    .putBoolean(ENABLED, false)
                    .apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (settingsLaunch) {
            settingsLaunch = false;
            return;
        }

        if (authed) {
            showManager();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void authenticate() {
        authed = false;

        String type = getSharedPreferences(PREFS, 0)
                .getString(TYPE, PIN);

        if (PATTERN.equals(type)) {
            patternAuth();
        } else {
            pinAuth();
        }
    }

    private LinearLayout root() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setGravity(Gravity.CENTER);
        r.setPadding(dp(28), dp(25), dp(28), dp(30));
        r.setBackground(
                grad(
                        new int[]{
                                Color.rgb(7, 29, 64),
                                Color.rgb(34, 112, 188),
                                Color.rgb(61, 46, 130)
                        },
                        0
                )
        );

        TextView icon = txt("🛡", 70, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        r.addView(
                icon,
                new LinearLayout.LayoutParams(-1, dp(100))
        );

        TextView title = txt("אימות מנהל", 30, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        r.addView(
                title,
                new LinearLayout.LayoutParams(-1, dp(55))
        );

        TextView sub = txt(
                "רק מנהל מורשה יכול להיכנס",
                16,
                Color.WHITE,
                false
        );
        sub.setGravity(Gravity.CENTER);
        r.addView(
                sub,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        return r;
    }

    private void pinAuth() {
        LinearLayout r = root();

        EditText input = new EditText(this);
        input.setHint("סיסמת מנהל");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setTextSize(21);
        input.setGravity(Gravity.CENTER);
        input.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        r.addView(
                input,
                new LinearLayout.LayoutParams(-1, dp(65))
        );

        Button button = btn("אימות");

        r.addView(
                button,
                new LinearLayout.LayoutParams(-1, dp(58))
        );

        button.setOnClickListener(v -> {
            String savedPassword =
                    getSharedPreferences(PREFS, 0)
                            .getString(PASSWORD, "1234");

            if (savedPassword.equals(input.getText().toString())) {
                open();
            } else {
                input.setError("סיסמה שגויה");
            }
        });

        setContentView(r);

        input.requestFocus();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        );
    }

    private void patternAuth() {
        LinearLayout r = root();

        PatternLockView patternView = new PatternLockView(this);

        r.addView(
                patternView,
                new LinearLayout.LayoutParams(
                        dp(300),
                        dp(300)
                )
        );

        Button button = btn("אימות");

        r.addView(
                button,
                new LinearLayout.LayoutParams(-1, dp(58))
        );

        button.setOnClickListener(v -> {
            String savedPattern =
                    getSharedPreferences(PREFS, 0)
                            .getString(PASSWORD, "");

            if (savedPattern.equals(patternView.getPattern())) {
                open();
            } else {
                Toast.makeText(
                        this,
                        "תבנית שגויה",
                        Toast.LENGTH_SHORT
                ).show();

                patternView.clearPattern();
            }
        });

        setContentView(r);
    }

    private void open() {
        authed = true;
        showManager();
    }

    private void showManager() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(
                Color.rgb(245, 248, 252)
        );

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(28)
        );

        scroll.addView(body);

        TextView title = txt(
                "Safe Steps",
                32,
                Color.rgb(20, 43, 76),
                true
        );
        title.setGravity(Gravity.CENTER);

        body.addView(
                title,
                new LinearLayout.LayoutParams(-1, dp(58))
        );

        TextView sub = txt(
                "צעדים בטוחים בשבילך",
                17,
                Color.rgb(91, 105, 126),
                false
        );
        sub.setGravity(Gravity.CENTER);

        body.addView(
                sub,
                new LinearLayout.LayoutParams(-1, dp(38))
        );

        LinearLayout protectionCard = card();

        status = txt(
                "",
                21,
                Color.rgb(20, 43, 76),
                true
        );

        protectionCard.addView(
                status,
                new LinearLayout.LayoutParams(-1, dp(48))
        );

        String currentType =
                getSharedPreferences(PREFS, 0)
                        .getString(TYPE, PIN);

        TextView typeText = txt(
                PATTERN.equals(currentType)
                        ? "סוג סיסמה: קווים"
                        : "סוג סיסמה: מספרית",
                16,
                Color.rgb(91, 105, 126),
                false
        );

        protectionCard.addView(
                typeText,
                new LinearLayout.LayoutParams(-1, dp(38))
        );

        Button toggle = btn("שינוי מצב ההגנה");

        protectionCard.addView(
                toggle,
                new LinearLayout.LayoutParams(-1, dp(54))
        );

        toggle.setOnClickListener(v ->
                credential(
                        "שינוי מצב ההגנה",
                        () -> {
                            boolean enabled =
                                    getSharedPreferences(PREFS, 0)
                                            .getBoolean(ENABLED, false);

                            getSharedPreferences(PREFS, 0)
                                    .edit()
                                    .putBoolean(ENABLED, !enabled)
                                    .apply();

                            refresh();
                        }
                )
        );

        body.addView(protectionCard, lp());

        LinearLayout actions = card();

        Button adminButton = btn("מנהל מכשיר");

        actions.addView(
                adminButton,
                new LinearLayout.LayoutParams(-1, dp(54))
        );

        adminButton.setOnClickListener(v ->
                credential(
                        "מנהל מכשיר",
                        this::admin
                )
        );

        Button accessibilityButton = btn("שירות ההגנה");

        actions.addView(
                accessibilityButton,
                new LinearLayout.LayoutParams(-1, dp(54))
        );

        accessibilityButton.setOnClickListener(v -> {
            settingsLaunch = true;

            try {
                startActivity(
                        new Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                );
            } catch (Exception ignored) {
            }
        });

        body.addView(actions, lp());

        TextView options = txt(
                "⋮  אפשרויות נוספות",
                17,
                Color.rgb(70, 92, 119),
                true
        );

        options.setPadding(
                0,
                dp(10),
                0,
                dp(4)
        );

        options.setOnClickListener(v -> menu(options));

        body.addView(
                options,
                new LinearLayout.LayoutParams(-1, dp(48))
        );

        refresh();

        setContentView(scroll);
    }

    private void menu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.getMenu().add("שינוי סיסמת מנהל");
        menu.getMenu().add("אודות");

        menu.setOnMenuItemClickListener(item -> {

            if (item.getTitle().toString().startsWith("שינוי")) {
                credential(
                        "אימות מנהל",
                        this::chooseType
                );
            } else {
                about();
            }

            return true;
        });

        menu.show();
    }

    private void refresh() {
        if (status == null) {
            return;
        }

        boolean enabled =
                getSharedPreferences(PREFS, 0)
                        .getBoolean(ENABLED, false);

        if (enabled) {
            status.setText("● ההגנה פעילה");
            status.setTextColor(
                    Color.rgb(13, 151, 91)
            );
        } else {
            status.setText("● ההגנה כבויה");
            status.setTextColor(
                    Color.rgb(211, 75, 65)
            );
        }
    }

    private void credential(
            String title,
            Runnable successAction
    ) {

        String type =
                getSharedPreferences(PREFS, 0)
                        .getString(TYPE, PIN);

        if (PATTERN.equals(type)) {

            PatternLockView patternView =
                    new PatternLockView(this);

            AlertDialog dialog =
                    new AlertDialog.Builder(this)
                            .setTitle(title)
                            .setMessage("ציירו את התבנית")
                            .setView(patternView)
                            .setNegativeButton(
                                    "ביטול",
                                    null
                            )
                            .setPositiveButton(
                                    "אישור",
                                    null
                            )
                            .create();

            dialog.setOnShowListener(
                    dialogListener ->
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            ).setOnClickListener(
                                    clickView -> {

                                        String savedPattern =
                                                getSharedPreferences(
                                                        PREFS,
                                                        0
                                                ).getString(
                                                        PASSWORD,
                                                        ""
                                                );

                                        if (savedPattern.equals(
                                                patternView.getPattern()
                                        )) {
                                            dialog.dismiss();
                                            successAction.run();
                                        } else {
                                            Toast.makeText(
                                                    this,
                                                    "אימות נכשל",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    }
                            )
            );

            dialog.show();

        } else {

            EditText input = new EditText(this);

            input.setInputType(
                    InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD
            );

            AlertDialog dialog =
                    new AlertDialog.Builder(this)
                            .setTitle(title)
                            .setMessage(
                                    "הזינו את סיסמת המנהל"
                            )
                            .setView(input)
                            .setNegativeButton(
                                    "ביטול",
                                    null
                            )
                            .setPositiveButton(
                                    "אישור",
                                    null
                            )
                            .create();

            dialog.setOnShowListener(
                    dialogListener ->
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            ).setOnClickListener(
                                    clickView -> {

                                        String savedPassword =
                                                getSharedPreferences(
                                                        PREFS,
                                                        0
                                                ).getString(
                                                        PASSWORD,
                                                        "1234"
                                                );

                                        if (savedPassword.equals(
                                                input.getText().toString()
                                        )) {
                                            dialog.dismiss();
                                            successAction.run();
                                        } else {
                                            input.setError(
                                                    "אימות נכשל"
                                            );
                                        }
                                    }
                            )
            );

            dialog.show();
        }
    }

    private void chooseType() {
        RadioGroup group = new RadioGroup(this);

        RadioButton pinRadio =
                new RadioButton(this);

        pinRadio.setText("סיסמה מספרית");

        RadioButton patternRadio =
                new RadioButton(this);

        patternRadio.setText("סיסמת קווים");

        pinRadio.setTextSize(18);
        patternRadio.setTextSize(18);

        group.addView(pinRadio);
        group.addView(patternRadio);

        pinRadio.setChecked(true);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("בחירת סוג סיסמה")
                        .setMessage(
                                "בחרו את סוג האימות החדש"
                        )
                        .setView(group)
                        .setNegativeButton(
                                "ביטול",
                                null
                        )
                        .setPositiveButton(
                                "המשך",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogListener ->
                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setOnClickListener(
                                clickView -> {

                                    dialog.dismiss();

                                    if (patternRadio.isChecked()) {
                                        newPattern();
                                    } else {
                                        newPin();
                                    }
                                }
                        )
        );

        dialog.show();
    }

    private void newPin() {

        EditText first = new EditText(this);
        EditText second = new EditText(this);

        first.setHint("סיסמה חדשה");
        second.setHint("הקלדה חוזרת");

        first.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        second.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.addView(first);
        layout.addView(second);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("סיסמה מספרית חדשה")
                        .setView(layout)
                        .setNegativeButton(
                                "ביטול",
                                null
                        )
                        .setPositiveButton(
                                "שמירה",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogListener ->
                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setOnClickListener(
                                clickView -> {

                                    String newPassword =
                                            first.getText()
                                                    .toString();

                                    String confirmation =
                                            second.getText()
                                                    .toString();

                                    if (newPassword.length() < 4) {
                                        first.setError(
                                                "לפחות 4 ספרות"
                                        );
                                        return;
                                    }

                                    if (!newPassword.matches(
                                            "[0-9]+"
                                    )) {
                                        first.setError(
                                                "יש להזין ספרות בלבד"
                                        );
                                        return;
                                    }

                                    if (!newPassword.equals(
                                            confirmation
                                    )) {
                                        second.setError(
                                                "הסיסמאות אינן זהות"
                                        );
                                        return;
                                    }

                                    getSharedPreferences(
                                            PREFS,
                                            0
                                    ).edit()
                                            .putString(
                                                    PASSWORD,
                                                    newPassword
                                            )
                                            .putString(
                                                    TYPE,
                                                    PIN
                                            )
                                            .apply();

                                    dialog.dismiss();

                                    Toast.makeText(
                                            MainActivity.this,
                                            "הסיסמה עודכנה",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                        )
        );

        dialog.show();
    }

    private void newPattern() {

        patternStep(
                "ציירו סיסמה חדשה",
                firstPattern ->
                        patternStep(
                                "ציירו שוב לאימות",
                                secondPattern -> {

                                    if (!firstPattern.equals(
                                            secondPattern
                                    )) {
                                        Toast.makeText(
                                                this,
                                                "התבניות אינן זהות",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        newPattern();
                                        return;
                                    }

                                    getSharedPreferences(
                                            PREFS,
                                            0
                                    ).edit()
                                            .putString(
                                                    PASSWORD,
                                                    firstPattern
                                            )
                                            .putString(
                                                    TYPE,
                                                    PATTERN
                                            )
                                            .apply();

                                    Toast.makeText(
                                            this,
                                            "הסיסמה עודכנה",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                        )
        );
    }

    interface Done {
        void go(String s);
    }

    private void patternStep(
            String title,
            Done done
    ) {

        PatternLockView patternView =
                new PatternLockView(this);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(
                                "יש לבחור לפחות 4 נקודות"
                        )
                        .setView(patternView)
                        .setNegativeButton(
                                "ביטול",
                                null
                        )
                        .setPositiveButton(
                                "המשך",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogListener ->
                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setOnClickListener(
                                clickView -> {

                                    String pattern =
                                            patternView.getPattern();

                                    if (pattern.length() < 4) {
                                        Toast.makeText(
                                                this,
                                                "לפחות 4 נקודות",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        return;
                                    }

                                    dialog.dismiss();
                                    done.go(pattern);
                                }
                        )
        );

        dialog.show();
    }

    private void admin() {

        DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager)
                        getSystemService(
                                DEVICE_POLICY_SERVICE
                        );

        ComponentName component =
                new ComponentName(
                        this,
                        SafeStepsDeviceAdminReceiver.class
                );

        if (devicePolicyManager.isAdminActive(component)) {

            new AlertDialog.Builder(this)
                    .setTitle("מנהל מכשיר פעיל")
                    .setMessage(
                            "Safe Steps כבר מוגדרת כמנהלת מכשיר. " +
                            "כדי לבטל אותה, יש לאמת את המנהל " +
                            "ולאחר מכן לבטל את ההרשאה."
                    )
                    .setNegativeButton(
                            "סגירה",
                            null
                    )
                    .setPositiveButton(
                            "ביטול הרשאה",
                            (dialogInterface, which) ->
                                    credential(
                                            "ביטול מנהל מכשיר",
                                            () -> {
                                                devicePolicyManager
                                                        .removeActiveAdmin(
                                                                component
                                                        );

                                                Toast.makeText(
                                                        this,
                                                        "הרשאת מנהל המכשיר בוטלה",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                    )
                    )
                    .show();

            return;
        }

        Intent intent =
                new Intent(
                        DevicePolicyManager
                                .ACTION_ADD_DEVICE_ADMIN
                );

        intent.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                component
        );

        intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "מנהל המכשיר מסייע למנוע הסרה רגילה " +
                "של Safe Steps עד לביטול ההרשאה."
        );

        startActivity(intent);
    }

    private void about() {

        new AlertDialog.Builder(this)
                .setTitle("אודות Safe Steps")
                .setMessage(
                        "© כל הזכויות שמורות לישראל מויאל.\n\n" +
                        "ליצירת קשר פנו במייל:\n" +
                        "inm758595@gmail.com"
                )
                .setPositiveButton(
                        "סגירה",
                        null
                )
                .show();
    }

    private LinearLayout card() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(18),
                dp(16),
                dp(18),
                dp(16)
        );

        card.setBackground(
                grad(
                        new int[]{
                                Color.WHITE,
                                Color.rgb(247, 250, 255)
                        },
                        dp(20)
                )
        );

        return card;
    }

    private Button btn(String text) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);

        return button;
    }

    private TextView txt(
            String text,
            float size,
            int color,
            boolean bold
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private LinearLayout.LayoutParams lp() {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.bottomMargin = dp(15);

        return params;
    }

    private GradientDrawable grad(
            int[] colors,
            float radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        colors
                );

        drawable.setCornerRadius(radius);

        return drawable;
    }

    private int dp(float value) {

        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
                + 0.5f
        );
    }
}
