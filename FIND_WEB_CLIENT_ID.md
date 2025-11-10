# How to Find Your Web Client ID

## Step 1: Go to Google Cloud Console Credentials

Visit: https://console.cloud.google.com/apis/credentials

## Step 2: Look at Your OAuth 2.0 Client IDs

You should see a list like this:

```
OAuth 2.0 Client IDs
┌──────────────────────────────────────┬──────────────┬─────────────────────────────────────┐
│ Name                                 │ Type         │ Client ID                           │
├──────────────────────────────────────┼──────────────┼─────────────────────────────────────┤
│ Web client (auto from Google)       │ Web app      │ 343911010427-abc123xyz.apps.g...    │  ← USE THIS ONE!
│ Android client (auto from Google)   │ Android      │ 343911010427-def456uvw.apps.g...    │  ← NOT this one
│ ShopVerse Android                    │ Android      │ 343911010427-4kk7tvk97e4...         │  ← This is what you're using now (WRONG)
└──────────────────────────────────────┴──────────────┴─────────────────────────────────────┘
```

## Step 3: Click on the Web Application Client

1. Click on the **Web application** type client (NOT Android)
2. You'll see:
   - **Client ID:** `343911010427-XXXXXXXXXXXXXXXX.apps.googleusercontent.com`
   - **Client secret:** `GOCSPX-XXXXXXXXXXXXX`
   - **Authorized redirect URIs:**
     ```
     https://uehonyhpopuxynbzshyo.supabase.co/auth/v1/callback
     ```

## Step 4: Verify This Matches Supabase

1. Go to Supabase Dashboard: https://supabase.com/dashboard
2. Navigate to: **Authentication** > **Providers** > **Google**
3. Check if the **Client ID** there matches the Web Client ID from Google Cloud Console
4. They should be **identical**

## Step 5: Copy the Web Client ID

**NOT** the Android Client ID!

The Web Client ID should be:
- From the "Web application" type OAuth client
- The same one configured in Supabase Dashboard

## Current Problem

Your `local.properties` currently has:
```properties
GOOGLE_CLIENT_ID=343911010427-4kk7tvk97e4l45uslpba09a59492ttff.apps.googleusercontent.com
```

This looks like it might be the **Android Client ID** (based on the error you're getting).

You need to replace it with your **Web Client ID**.

## How to Tell Which is Which

### Web Client ID:
- Type: Web application
- Has authorized redirect URIs (Supabase callback URL)
- Same as what's in Supabase Dashboard > Authentication > Providers > Google

### Android Client ID:
- Type: Android
- Has package name: `com.example.shopverse_customer_app`
- Has SHA-1 certificate fingerprint
- You do NOT put this in your code

## After Getting the Correct Web Client ID

Update `local.properties`:
```properties
GOOGLE_CLIENT_ID=YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com
```

Then:
1. Clean and rebuild: `gradlew clean build`
2. Uninstall app from device
3. Reinstall and test

---

**Quick Test:**

Compare the Client ID in your `local.properties` with the Client ID shown in Supabase Dashboard under Google provider settings. They should be **identical**.

If they're different, use the one from Supabase!
