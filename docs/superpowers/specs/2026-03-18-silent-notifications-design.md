# Silent Notification Updates Design

> **For agentic workers:** REQUIRED: Use superpowers:writing-plans to create an implementation plan for this design.

**Goal:** Prevent the NetZone VPN service from triggering sound or vibration alerts every time the list of blocked apps is updated.

**Architecture:** 
- Modify the notification channel configuration to use `IMPORTANCE_LOW` (no sound/vibration).
- Update the `NotificationCompat.Builder` to use `setOnlyAlertOnce(true)`, ensuring that only the initial service start can trigger an alert if the system allows.
- Maintain the informative content (e.g., "5 apps blocked") in the notification tray for user visibility without the intrusiveness.

**Tech Stack:** Kotlin, Android Notification API (NotificationChannel, NotificationCompat)

---

## 1. Context & Problem
The `NetZoneVpnService` currently creates a notification channel with `IMPORTANCE_DEFAULT`. On most Android versions, this means every call to `NotificationManager.notify()` with a changed content text or title will trigger a system alert (sound/vibration/peek). Since `updateVpn` is called every time a rule is toggled, this creates a poor user experience.

## 2. Proposed Changes

### A. Notification Channel Refactor
In `createNotificationChannel()`, the importance will be changed:
- **Old:** `NotificationManager.IMPORTANCE_DEFAULT`
- **New:** `NotificationManager.IMPORTANCE_LOW`

### B. Builder Configuration
In `createNotification()`, the builder will be updated:
- Add `.setOnlyAlertOnce(true)` to prevent re-alerting on updates.
- Ensure `.setPriority(NotificationCompat.PRIORITY_LOW)` is consistently used.

## 3. Benefits
- **User Experience:** Toggling apps in the UI will feel "instant" and silent, as intended for a background utility.
- **Battery:** Fewer system-wide alert cycles can slightly improve battery life on devices with heavy notification activity.

## 4. Verification Plan
1. Start the VPN service. Verify the initial notification appears.
2. Toggle an app's blocking status in the UI.
3. **Verify:** The notification text updates in the tray (e.g., from "0 apps" to "1 app"), but NO sound or vibration occurs.
