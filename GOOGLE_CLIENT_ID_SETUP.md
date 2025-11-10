# Google Cloud Console - OAuth Client Setup for Supabase

## The Problem

When you get `Result code: 0` (RESULT_CANCELED) immediately after launching Google Sign-In, it means Google is rejecting your sign-in request because the OAuth client configuration doesn't match.

## Understanding the Two Client IDs

For Supabase + Android, you need **TWO different OAuth 2.0 clients** in Google Cloud Console:

### 1. Web Application Client (for Supabase Backend)
- **Type:** Web application
- **Purpose:** Used by Supabase to verify the Google ID token
- **Authorized redirect URIs:**
  ```
  https://uehonyhpopuxynbzshyo.supabase.co/auth/v1/callback
  ```
- **You configure this in:** Supabase Dashboard > Authentication > Providers > Google

### 2. Android Client (for Your App)
- **Type:** Android
- **Purpose:** Used by your Android app to get the ID token
- **Package name:** `com.example.shopverse_customer_app`
- **SHA-1 certificate fingerprint:** Your debug/release keystore SHA-1
- **You use this in:** Your app's `local.properties` file

## The Correct Setup

### Step 1: Go to Google Cloud Console
https://console.cloud.google.com/apis/credentials

### Step 2: Check Your Existing Clients

You should see something like:
```
OAuth 2.0 Client IDs
┌─────────────────────────────────────┬──────────────┬────────────────┐
│ Name                                │ Type         │ Client ID      │
├─────────────────────────────────────┼──────────────┼────────────────┤
│ Web client (auto from Google)      │ Web app      │ 343911...      │  <- For Supabase
│ Android client (auto from Google)  │ Android      │ 343911...      │  <- For your app
│ ShopVerse Android                   │ Android      │ XXXXXX...      │  <- You may need to create this
└─────────────────────────────────────┴──────────────┴────────────────┘
```

### Step 3: Create Android OAuth Client (if needed)

1. Click **"+ CREATE CREDENTIALS"** > **"OAuth 2.0 Client ID"**

2. **Application type:** Android

3. **Name:** ShopVerse Android Debug

4. **Package name:**
   ```
   com.example.shopverse_customer_app
   ```

5. **SHA-1 certificate fingerprint:**
   - Run this command in your project directory:
   ```bash
   cd C:\Users\NHATTHANG\FptUniversity\PRM392\shopverse-customer-app
   gradlew signingReport
   ```
   - Look for the debug variant SHA-1
   - Copy and paste it (looks like: `XX:XX:XX:XX:...`)

6. Click **"CREATE"**

### Step 4: Verify Which Client ID to Use

After creating the Android client, you'll have a new Client ID. **DON'T** use this one!

**Use the WEB Client ID** in your `local.properties`:
```properties
GOOGLE_CLIENT_ID=343911010427-4kk7tvk97e4l45uslpba09a59492ttff.apps.googleusercontent.com
```

This seems counterintuitive, but here's why:
- The Web Client ID is what Supabase uses to verify tokens
- The Android Client ID is automatically matched by package name + SHA-1
- Google Sign-In SDK uses the Web Client ID to request tokens
- The Android client must exist for the request to succeed

## Current Configuration Check

Your current `local.properties` has:
```properties
GOOGLE_CLIENT_ID=343911010427-4kk7tvk97e4l45uslpba09a59492ttff.apps.googleusercontent.com
```

### Verify This is Your Web Client ID:

1. Go to Google Cloud Console > Credentials
2. Click on **"Web client (auto created by Google Sign-In)"** or similar
3. Check if the Client ID matches `343911010427-4kk7tvk97e4l45uslpba09a59492ttff`
4. Check **Authorized redirect URIs** - should include your Supabase callback URL

### Verify Android Client Exists:

1. Look for an **Android** type OAuth client
2. Click on it
3. Verify:
   - **Package name** = `com.example.shopverse_customer_app`
   - **SHA-1** = Your actual debug keystore SHA-1

## Common Mistakes

### ❌ Wrong: Using Android Client ID in local.properties
```properties
# DON'T DO THIS
GOOGLE_CLIENT_ID=343911010427-XXXXXXXXXX.apps.googleusercontent.com  # Android Client ID
```

### ✅ Correct: Using Web Client ID in local.properties
```properties
# DO THIS
GOOGLE_CLIENT_ID=343911010427-4kk7tvk97e4l45uslpba09a59492ttff.apps.googleusercontent.com  # Web Client ID
```

### ❌ Wrong: No Android OAuth client created
Result: `Result code: 0` (RESULT_CANCELED)

### ✅ Correct: Both Web and Android clients exist
Result: Google account picker appears

## Debugging Steps

### Test 1: Check if Android Client Exists
Run the app and try Google Sign-In:
- If you get `Result code: 0` immediately → Android client missing or wrong SHA-1
- If account picker appears → Android client configured correctly

### Test 2: Check Client ID in Logs
Look for this log:
```
D/GoogleSignInManager: Client ID from BuildConfig: 343911010427-4kk7tvk97e4l45uslpba09a59492ttff.apps.googleusercontent.com
```

If it shows `YOUR_GOOGLE_CLIENT_ID_HERE`:
- Your `local.properties` wasn't loaded
- Clean and rebuild: `gradlew clean build`

### Test 3: Verify SHA-1 Matches
1. Get your actual SHA-1:
   ```bash
   gradlew signingReport
   ```

2. Compare with the SHA-1 in Google Cloud Console Android client

3. If they don't match:
   - Update the Android OAuth client with the correct SHA-1
   - OR you're using a different keystore

## After Making Changes

1. **Wait 5 minutes** for Google to propagate changes
2. **Uninstall the app** from your device/emulator
3. **Clean and rebuild:**
   ```bash
   gradlew clean build
   ```
4. **Reinstall and test**

## What Should Happen

When everything is configured correctly:

1. Click "Sign in with Google"
2. Google account picker appears (system UI)
3. Select account
4. App receives ID token
5. Supabase verifies token
6. User is logged in

## Still Not Working?

### Check in Google Cloud Console:

1. **OAuth consent screen** is configured
2. **Web client** exists with Supabase redirect URI
3. **Android client** exists with correct package name and SHA-1
4. **Both clients** are in the same project
5. **Google+ API** is enabled (optional but recommended)

### Share These Details:

1. Screenshot of your Google Cloud Console Credentials page
2. SHA-1 from `gradlew signingReport` (debug variant)
3. The logs showing:
   - `Client ID from BuildConfig`
   - `Result code` after selecting account
   - Whether ID token or server auth code is present

---

**TL;DR:**
- Create **Android OAuth client** with your package name + SHA-1
- Use **Web Client ID** in `local.properties` (the one configured in Supabase)
- Both must exist in the same Google Cloud project
- Wait 5 minutes after creating, then uninstall/reinstall app
