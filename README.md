# Safe Steps – Final Clean Build

פרויקט נקי ומאוחד, ללא קבצים מהגרסאות הקודמות.

- Java 17 / Gradle 8.7 / AGP 8.6.1
- compileSdk 35 / minSdk 23 / targetSdk 35
- כניסה תמיד דרך אימות מנהל; ברירת מחדל 1234
- PIN או תבנית 3x3, כולל אימות כפול בעת שינוי
- 3 נקודות: שינוי סיסמת מנהל + אודות
- AccessibilityService עם notificationTimeout=0 וחסימה מיידית ככל שהמערכת מאפשרת
- מסך חסימה נשאר אחרי 2 שניות עד לחיצה על חזור
- Device Administrator
- אייקון מגן בלבד

הגנת Accessibility אינה הגנה מפני Root, Recovery, צריבת מערכת או איפוס/שינוי מערכת מחוץ ל-UI הרגיל של Android.
