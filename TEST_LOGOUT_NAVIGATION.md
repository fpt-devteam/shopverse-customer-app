# Test Guide: Logout Navigation Fix

## The Problem
After logout, clicking Account tab gets stuck on Dashboard - can't navigate to Account.

## Fixes Applied

### 1. Better NavOptions in AccountFragment.handleLogout()
```java
NavOptions navOptions = new NavOptions.Builder()
    .setPopUpTo(R.id.mobile_navigation, false)
    .setLaunchSingleTop(true)
    .build();
navController.navigate(R.id.navigation_dashboard, null, navOptions);
```

### 2. Enhanced Bottom Nav Listener in MainActivity
- Now explicitly handles all navigation clicks
- Logs every click for debugging
- Uses `NavigationUI.onNavDestinationSelected()` to ensure proper navigation

### 3. Better Sync Logic
- Only syncs when bottom nav and NavController are out of sync
- Prevents unnecessary updates that might interfere

## How to Test

### Test 1: Basic Logout Flow
1. **Log in** with Google
2. **Go to Account tab** - you should see your email and logout button
3. **Click Logout**
4. **Watch logs:**
   ```
   D/AccountFragment: handleLogout: All tokens cleared
   D/AccountFragment: UI updated to logged out state
   D/AccountFragment: Navigated to dashboard with NavOptions
   D/MainActivity: Destination changed to: Dashboard
   ```
5. **You should see Dashboard screen**
6. **Click Account tab**
7. **Watch logs:**
   ```
   D/MainActivity: === Bottom Nav Item Clicked ===
   D/MainActivity: Item ID clicked: [account_id]
   D/MainActivity: Current destination: [dashboard_id]
   D/MainActivity: Navigation handled: true
   D/MainActivity: New destination: [account_id]
   D/MainActivity: Destination changed to: Account
   D/AccountFragment: onResume: Fragment resumed
   D/AccountFragment: USER IS NOT LOGGED IN - Showing login/register UI
   ```
8. **You should see Login/Register buttons** ✅

### Test 2: Rapid Tab Switching After Logout
1. Log in
2. Logout
3. Quickly click: **Dashboard → Cart → Account → Maps → Account**
4. Each click should work and show the correct screen

### Test 3: Login After Logout
1. Logout
2. Click Account tab
3. Click "Sign in with Google"
4. **You should see account picker** (not auto-login)
5. Select account and log in
6. **You should stay on or return to Account tab**
7. **You should see logged-in UI** with your email

## What to Look For in Logs

### Good Pattern (Working):
```
// After clicking Account tab:
D/MainActivity: === Bottom Nav Item Clicked ===
D/MainActivity: Item ID clicked: 2131296384
D/MainActivity: Current destination: 2131296512  (Dashboard)
D/MainActivity: Navigation handled: true
D/MainActivity: New destination: 2131296384      (Account)
D/MainActivity: Destination changed to: Account
D/AccountFragment: onResume: Fragment resumed
D/AccountFragment: updateUIBasedOnLoginStatus: isLoggedIn = false
D/AccountFragment: USER IS NOT LOGGED IN - Showing login/register UI
```

### Bad Pattern (Bug):
```
// After clicking Account tab:
D/MainActivity: === Bottom Nav Item Clicked ===
D/MainActivity: Item ID clicked: 2131296384
D/MainActivity: Current destination: 2131296512
D/MainActivity: Navigation handled: false  ← PROBLEM!
(No destination change!)
(No AccountFragment onResume!)
```

If you see `Navigation handled: false`, it means the navigation was blocked.

## Common Issues

### Issue 1: "Still stuck on Dashboard"

**Check logs for:**
```
D/MainActivity: Navigation handled: false
```

**This means:** NavController refused the navigation

**Debug:**
1. Check if there's a navigation action from Dashboard to Account in `mobile_navigation.xml`
2. The bottom nav IDs must match the fragment IDs exactly

### Issue 2: "Account tab shows Dashboard content"

**Check logs for:**
```
D/MainActivity: New destination: 2131296512  (Should be Account ID, not Dashboard)
```

**This means:** Navigation went to wrong destination

**Debug:**
1. Check bottom navigation menu item IDs match fragment IDs
2. Verify `mobile_navigation.xml` fragment IDs

### Issue 3: "Can navigate to Account but shows wrong UI"

**Check logs for:**
```
D/AccountFragment: onResume: Fragment resumed
D/AccountFragment: updateUIBasedOnLoginStatus: isLoggedIn = true  ← WRONG!
```

**This means:** Tokens weren't cleared properly

**Debug:**
1. Check `TokenManager.clearTokens()` implementation
2. Verify `tokenManager.isLoggedIn()` returns false after logout

## Resource IDs to Check

Run this in Logcat to see your navigation IDs:
```
adb logcat -s MainActivity:D AccountFragment:D
```

Look for lines like:
```
D/MainActivity: Item ID clicked: 2131296384
D/MainActivity: Current destination: 2131296512
D/MainActivity: New destination: 2131296384
```

Take note of these IDs:
- **Dashboard ID:** Usually `2131296512` or similar
- **Account ID:** Usually `2131296384` or similar
- **Cart ID:** ...
- **Maps ID:** ...

They should be consistent across all logs.

## Quick Checklist

After logout, test all these navigation paths:

- [ ] Dashboard → Account (should work)
- [ ] Cart → Account (should work)
- [ ] Maps → Account (should work)
- [ ] Account → Dashboard → Account (should work)
- [ ] Logout → Click Account immediately (should work)
- [ ] Logout → Click Dashboard → Click Account (should work)

All should navigate correctly and show the appropriate UI based on login status.

## Still Stuck?

If navigation is still blocked after these fixes, share:

1. **Full logs** from logout to trying to click Account tab:
   ```bash
   adb logcat -s MainActivity:* AccountFragment:* -v time
   ```

2. **Your bottom navigation menu** file (`res/menu/bottom_nav_menu.xml`)

3. **Navigation graph** file (`res/navigation/mobile_navigation.xml`)

4. **Exact steps** to reproduce

---

**Expected behavior:** After all fixes, clicking any bottom nav tab should always navigate to that tab, regardless of where you are or what you just did (logout, login, etc.).
