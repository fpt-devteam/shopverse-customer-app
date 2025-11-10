# Supabase Google Sign-In Setup Guide

## Overview
This guide will help you complete the setup for Google Sign-In integration with Supabase Authentication in your ShopVerse Customer App.

## Prerequisites
- Supabase project created
- Google Cloud Console access
- Android Studio with your project open

## Step 1: Configure Google OAuth in Google Cloud Console

### 1.1 Create OAuth 2.0 Client ID

1. **Go to Google Cloud Console**
   - Visit: https://console.cloud.google.com/
   - Select your project (or create a new one)

2. **Enable Google+ API** (if not already enabled)
   - Go to "APIs & Services" > "Library"
   - Search for "Google+ API"
   - Click "Enable"

3. **Configure OAuth Consent Screen**
   - Go to "APIs & Services" > "OAuth consent screen"
   - Choose "External" (or "Internal" if using Google Workspace)
   - Fill in the required fields:
     - App name: ShopVerse
     - User support email: your email
     - Developer contact email: your email
   - Click "Save and Continue"
   - Add scopes (optional for testing): email, profile, openid
   - Click "Save and Continue"
   - Add test users (if in testing mode)

4. **Create OAuth 2.0 Client IDs**
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth 2.0 Client ID"

   **Create TWO client IDs:**

   **a) Web Application Client (for Supabase):**
   - Application type: **Web application**
   - Name: `ShopVerse Web Client`
   - Authorized redirect URIs:
     ```
     https://uehonyhpopuxynbzshyo.supabase.co/auth/v1/callback
     ```
   - Click "Create"
   - **Save the Client ID and Client Secret** (you'll need these for Supabase)

   **b) Android Client (for your app):**
   - Application type: **Android**
   - Name: `ShopVerse Android`
   - Package name: `com.example.shopverse_customer_app`
   - SHA-1 certificate fingerprint: (see below)

### 1.2 Get SHA-1 Fingerprint

**For Debug Build:**
```bash
cd C:\Users\NHATTHANG\FptUniversity\PRM392\shopverse-customer-app
gradlew signingReport
```

Or using keytool:
```bash
keytool -list -v -keystore "C:\Users\NHATTHANG\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Copy the SHA-1 fingerprint and paste it when creating the Android OAuth client.

**For Release Build** (when you're ready to publish):
- Get the SHA-1 from your release keystore
- Create another Android OAuth client with the release SHA-1

## Step 2: Configure Google OAuth in Supabase

1. **Go to Supabase Dashboard**
   - Visit: https://supabase.com/dashboard
   - Select your project: `uehonyhpopuxynbzshyo`

2. **Navigate to Authentication > Providers**
   - Click on "Authentication" in the left sidebar
   - Click on "Providers" tab
   - Find "Google" in the list

3. **Enable Google Provider**
   - Toggle "Enable Sign in with Google" to ON
   - Enter the **Web Application Client ID** from Step 1.4a
   - Enter the **Client Secret** from Step 1.4a
   - The Redirect URL should already be filled:
     ```
     https://uehonyhpopuxynbzshyo.supabase.co/auth/v1/callback
     ```
   - Click "Save"

## Step 3: Update local.properties

You've already done this! Your `local.properties` should have:

```properties
GOOGLE_CLIENT_ID=343911010427-4kk7tvk97e4l45uslpba09a59492ttff.apps.googleusercontent.com
```

**Important:** This should be the **Web Application Client ID** from Step 1.4a (the same one you configured in Supabase), NOT the Android Client ID.

## Step 4: Verify Configuration

### 4.1 Check that you have THREE OAuth clients in Google Cloud Console:
1. **Web Application** - Used by Supabase (Client ID in Supabase settings)
2. **Android (Debug)** - For development testing (SHA-1 from debug keystore)
3. **Android (Release)** - For production (SHA-1 from release keystore) - optional for now

### 4.2 Verify Supabase Configuration
- Go to Supabase Dashboard > Authentication > Providers > Google
- Ensure it's enabled and has the Web Client credentials

## Step 5: Build and Test

1. **Sync Gradle**
   - In Android Studio, click "Sync Project with Gradle Files"

2. **Clean and Rebuild**
   ```bash
   gradlew clean build
   ```

3. **Run the app**
   - Launch on a physical device or emulator with Google Play Services

4. **Test Google Sign-In**
   - Open the app
   - Navigate to Login screen
   - Click "Sign in with Google"
   - Select a Google account
   - The app should authenticate through Supabase

## How It Works

```
┌─────────────┐         ┌──────────────┐         ┌──────────┐
│   Your App  │────1───▶│    Google    │────2───▶│  User    │
│             │         │  Sign-In     │         │ Selects  │
│             │         │              │         │ Account  │
└─────────────┘         └──────────────┘         └──────────┘
       │                       │
       │                   3. ID Token
       │◀──────────────────────┘
       │
       │  4. Send ID Token
       ▼
┌─────────────┐
│  Supabase   │
│   Auth      │─────5. Verify with Google
│             │
│             │─────6. Return Access/Refresh Token
└─────────────┘
       │
       ▼
┌─────────────┐
│   Your App  │ (User logged in)
└─────────────┘
```

1. User clicks "Sign in with Google"
2. Google Sign-In dialog appears
3. User selects account, Google returns ID token to your app
4. Your app sends the ID token to Supabase Auth API
5. Supabase verifies the token with Google using Web Client credentials
6. Supabase returns access token and refresh token
7. Your app saves the tokens and user is logged in

## Implementation Details

### Files Modified:

1. **API Interface:**
   - `SupabaseAuthApi.java` - Added `signInWithGoogle()` endpoint

2. **Repository:**
   - `AuthRepository.java` - Updated to call Supabase Auth API directly
   - Removed Firebase Auth dependency

3. **Dependencies:**
   - Kept `play-services-auth` for Google Sign-In UI
   - Removed `firebase-auth` dependency

### API Endpoint Used:

```http
POST https://uehonyhpopuxynbzshyo.supabase.co/auth/v1/token?grant_type=id_token
Headers:
  apikey: YOUR_SUPABASE_ANON_KEY
  Content-Type: application/json

Body:
{
  "id_token": "google_id_token_from_client",
  "provider": "google"
}

Response:
{
  "access_token": "supabase_access_token",
  "refresh_token": "supabase_refresh_token",
  "expires_in": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    ...
  }
}
```

## Troubleshooting

### Issue: "Error 10: Developer console is not set up correctly"
**Solution:**
- Make sure you created the Android OAuth client with the correct package name
- Verify the SHA-1 fingerprint matches your debug keystore
- Wait a few minutes after creating the OAuth client (propagation delay)

### Issue: "Sign-in failed with status code 400"
**Solution:**
- Verify the Web Client ID in `local.properties` matches the one in Supabase
- Ensure the Web Client ID in Supabase matches the one from Google Cloud Console
- Check that Google provider is enabled in Supabase Dashboard

### Issue: "Invalid grant: id_token verification failed"
**Solution:**
- The ID token might have expired (they expire after 1 hour)
- Ensure the Web Client ID configured in Supabase matches the client ID in your app
- Verify that the OAuth consent screen is properly configured

### Issue: "Redirect URI mismatch"
**Solution:**
- This shouldn't happen in this flow since we're using ID token exchange
- If you see this, verify the redirect URI in Google Cloud Console matches Supabase exactly

### Issue: "Google Sign-In UI doesn't appear"
**Solution:**
- Make sure you're testing on a device/emulator with Google Play Services
- Check that the Android OAuth client exists in Google Cloud Console
- Verify the SHA-1 fingerprint is correct

## Testing Checklist

- [ ] Web Client created in Google Cloud Console
- [ ] Android Client created with correct SHA-1
- [ ] Google provider enabled in Supabase
- [ ] Web Client credentials configured in Supabase
- [ ] `GOOGLE_CLIENT_ID` in `local.properties` matches Web Client ID
- [ ] App builds successfully
- [ ] Google Sign-In button appears on login screen
- [ ] Google account picker appears when clicking the button
- [ ] User can successfully sign in
- [ ] User data is saved in Supabase auth.users table
- [ ] Access token is saved and works for API calls

## Security Notes

- ✅ `local.properties` is in `.gitignore` - never commit this file
- ✅ Use the Web Client ID (not Android Client ID) in your app config
- ✅ Supabase verifies the ID token server-side for security
- ✅ OAuth secrets are stored in Supabase (server-side) not in your app
- ✅ For production, use Google Play App Signing and add the SHA-1 from Play Console

## Additional Resources

- [Supabase Auth with Google](https://supabase.com/docs/guides/auth/social-login/auth-google)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start)
- [OAuth 2.0 for Mobile & Desktop Apps](https://developers.google.com/identity/protocols/oauth2/native-app)

## Next Steps

1. Test thoroughly with different Google accounts
2. Add error handling for edge cases
3. Implement account linking if needed
4. Set up proper analytics tracking
5. Configure production OAuth clients before release

---

**Important:** Your current setup uses the Web Client ID which is correct for Supabase integration. The Android OAuth client is needed for the Google Sign-In UI to work properly on Android devices.
