# Debug Guide: Navigation Tab Not Updating After Login/Logout

## The Problem

After logging in or logging out, when you navigate to the Account tab, the bottom navigation shows you're on Account but the screen still shows Dashboard (or another tab).

## Root Causes

### 1. Fragment Not Refreshing
- When you click a bottom nav tab, the NavController navigates to that fragment
- BUT if the fragment is already in the backstack, it might not call `onResume()` or recreate the view
- The UI doesn't update to reflect login/logout state

### 2. Bottom Navigation Out of Sync
- NavController has its own navigation state
- Bottom Navigation has its own selected item state
- These can get out of sync, especially after programmatic navigation

### 3. Login/Logout Flow Issues
- After login: `LoginActivity` finishes → returns to `MainActivity`
- After logout: `AccountFragment` navigates to Dashboard
- In both cases, the bottom nav might not sync properly

## Fixes Applied

### Fix 1: Update UI Before Navigation on Logout
**File:** `AccountFragment.java` line 196-219

```java
private void handleLogout() {
    // 1. Clear tokens
    tokenManager.clearTokens();

    // 2. Update UI IMMEDIATELY (before navigation)
    updateUIBasedOnLoginStatus();

    // 3. Show toast
    Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

    // 4. Navigate to dashboard with a delay
    requireView().postDelayed(() -> {
        Navigation.findNavController(requireView()).navigate(R.id.navigation_dashboard);
    }, 100);
}
```

**Why this helps:**
- Updates UI to logged-out state before navigating away
- Ensures the fragment is ready when user comes back
- Small delay prevents navigation conflicts

### Fix 2: Sync Bottom Nav on Resume
**File:** `MainActivity.java` line 83-108

```java
@Override
protected void onResume() {
    super.onResume();
    // Ensure bottom navigation matches current destination
    syncBottomNavigationWithNavController();
}

private void syncBottomNavigationWithNavController() {
    int currentDestinationId = navController.getCurrentDestination().getId();
    binding.navView.setSelectedItemId(currentDestinationId);
}
```

**Why this helps:**
- Every time MainActivity resumes (e.g., after login), syncs the bottom nav
- Ensures bottom nav selection matches where you actually are
- Prevents "stuck on wrong tab" issue

### Fix 3: Enhanced Logging
**Files:** `MainActivity.java`, `AccountFragment.java`

Added comprehensive logging to track:
- When fragments are created/resumed
- When login status changes
- When navigation occurs
- When bottom nav selection changes

## How to Debug

### Step 1: Watch the Logs

Filter by these tags in Logcat:
- `MainActivity` - Activity lifecycle and navigation
- `AccountFragment` - Fragment lifecycle and login/logout

### Step 2: Reproduce the Issue

1. **Test Logout:**
   ```
   1. Log in
   2. Go to Account tab
   3. Click Logout
   4. Watch logs:
      - Should see: "handleLogout: All tokens cleared"
      - Should see: "UI updated to logged out state"
      - Should see: "Navigated to dashboard"
   5. Click Account tab again
   6. Watch logs:
      - Should see: "onResume: Fragment resumed"
      - Should see: "USER IS NOT LOGGED IN - Showing login/register UI"
   ```

2. **Test Login:**
   ```
   1. Start logged out
   2. Go to Account tab
   3. Click Login
   4. Log in successfully
   5. Watch logs:
      - Should see: "onResume: Activity resumed" (MainActivity)
      - Should see: "syncBottomNavigationWithNavController"
   6. Manually go to Account tab
   7. Watch logs:
      - Should see: "onResume: Fragment resumed" (AccountFragment)
      - Should see: "USER IS LOGGED IN - Showing logged in UI"
   ```

### Step 3: Check What You See in Logs

**Good Pattern (Working):**
```
D/MainActivity: onResume: Activity resumed
D/MainActivity: syncBottomNavigationWithNavController: Current destination ID = 2131296514
D/MainActivity: syncBottomNavigationWithNavController: Bottom nav updated
D/AccountFragment: onResume: Fragment resumed
D/AccountFragment: updateUIBasedOnLoginStatus: ===== CHECKING LOGIN STATUS =====
D/AccountFragment: updateUIBasedOnLoginStatus: isLoggedIn = true
D/AccountFragment: updateUIBasedOnLoginStatus: USER IS LOGGED IN - Showing logged in UI
```

**Bad Pattern (Bug):**
```
D/MainActivity: Destination changed to: Account
(No AccountFragment onResume!)
(UI doesn't update!)
```

## Common Scenarios

### Scenario 1: "I logged in but Account tab still shows Login button"

**Diagnosis:**
```
Look for this in logs:
D/AccountFragment: onResume: Fragment resumed
D/AccountFragment: updateUIBasedOnLoginStatus: isLoggedIn = false
```

**This means:** TokenManager doesn't have the login token

**Solution:** Check if `LoginActivity` is properly saving tokens before finishing

### Scenario 2: "I logged out but clicking Account shows me still logged in"

**Diagnosis:**
```
Look for:
D/AccountFragment: handleLogout: All tokens cleared
D/AccountFragment: updateUIBasedOnLoginStatus: isLoggedIn = true  ← PROBLEM!
```

**This means:** Tokens weren't actually cleared

**Solution:** Check TokenManager.clearTokens() implementation

### Scenario 3: "Bottom nav shows Account but screen shows Dashboard"

**Diagnosis:**
```
Look for:
D/MainActivity: Destination changed to: Dashboard
(but bottom nav is on Account)
```

**This means:** Bottom nav and NavController are out of sync

**Solution:** Should be fixed by `syncBottomNavigationWithNavController()` in onResume

## Testing Checklist

Test all these flows and verify the UI updates correctly:

### Login Flows:
- [ ] Not logged in → Click Login → Log in → Should stay on Account or go to Dashboard
- [ ] Not logged in → Click Login → Log in → Click Account tab → Should show logged-in UI
- [ ] Not logged in → Click Login → Cancel → Click Account tab → Should still show login buttons

### Logout Flows:
- [ ] Logged in → Go to Account → Click Logout → Should navigate to Dashboard
- [ ] Logged in → Go to Account → Click Logout → Click Account again → Should show login buttons
- [ ] Logged in → On Dashboard → Go to Account → Click Logout → Should stay on Dashboard → Click Account → Should show login buttons

### Navigation Flows:
- [ ] Dashboard → Account → Should show correct UI based on login status
- [ ] Cart → Account → Should show correct UI based on login status
- [ ] Maps → Account → Should show correct UI based on login status
- [ ] After login from any tab → Go to Account → Should show logged-in UI
- [ ] After logout from Account → Go to any tab → Go back to Account → Should show login buttons

## Advanced Debugging

If the issue persists, add this to MainActivity:

```java
binding.navView.setOnItemSelectedListener(item -> {
    Log.d(TAG, "Bottom nav item selected: " + item.getItemId());
    Log.d(TAG, "Current destination before: " + navController.getCurrentDestination().getId());

    boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

    Log.d(TAG, "Navigation handled: " + handled);
    Log.d(TAG, "Current destination after: " + navController.getCurrentDestination().getId());

    return handled;
});
```

This will show exactly what happens when you click a bottom nav item.

## Still Not Working?

Share these logs:
1. Full log from app launch to reproducing the issue
2. Filter by: `MainActivity AccountFragment LoginActivity`
3. Include the exact steps you took
4. Note what you expected vs what actually happened

---

**TL;DR of fixes:**
1. ✅ `AccountFragment.handleLogout()` now updates UI before navigating
2. ✅ `MainActivity.onResume()` now syncs bottom nav with nav controller
3. ✅ `AccountFragment.onResume()` always checks login status and updates UI
4. ✅ Added comprehensive logging to debug navigation flow
