# Google Sign-In Setup Guide

## Overview
This guide will help you complete the setup for Google Sign-In integration with Firebase Authentication in your ShopVerse Customer App.

## Prerequisites
- Firebase project created
- Firebase Console access
- Android Studio with your project open

## Step 1: Get Your Google Client ID from Firebase Console

1. **Go to Firebase Console**
   - Visit: https://console.firebase.google.com/
   - Select your project

2. **Navigate to Authentication**
   - Click on "Authentication" in the left sidebar
   - Click on "Sign-in method" tab

3. **Enable Google Sign-In**
   - Find "Google" in the list of providers
   - Click on it to expand
   - Toggle the "Enable" switch to ON
   - You'll see a "Web SDK configuration" section

4. **Copy the Web Client ID**
   - In the Web SDK configuration section, you'll see a field called "Web client ID"
   - It looks like: `123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com`
   - Copy this entire string

5. **Add SHA-1 and SHA-256 Fingerprints** (Important!)
   - Scroll down to see your Android app
   - You need to add your debug and release SHA-1/SHA-256 fingerprints

   **Get Debug SHA-1:**
   ```bash
   cd android
   ./gradlew signingReport
   ```
   Or on Windows:
   ```bash
   gradlew signingReport
   ```

   Look for the "SHA1" and "SHA256" under the "debug" variant

   **Alternative method using keytool:**
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
   On Windows:
   ```bash
   keytool -list -v -keystore "C:\Users\NHATTHANG\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```

6. **Add Fingerprints to Firebase**
   - In Firebase Console, go to Project Settings (gear icon)
   - Scroll to "Your apps" section
   - Click on your Android app
   - Click "Add fingerprint"
   - Paste your SHA-1 and SHA-256 fingerprints
   - Click "Save"

## Step 2: Update local.properties File

1. **Open `local.properties`** in your project root

2. **Replace the placeholder** with your actual Web Client ID:
   ```properties
   GOOGLE_CLIENT_ID=YOUR_ACTUAL_WEB_CLIENT_ID_HERE.apps.googleusercontent.com
   ```

3. **Example:**
   ```properties
   GOOGLE_CLIENT_ID=123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com
   ```

## Step 3: Download Updated google-services.json

1. **In Firebase Console**, go to Project Settings
2. Scroll to "Your apps" section
3. Click the "Download google-services.json" button
4. **Replace** the existing `app/google-services.json` file with the new one
   - Location: `app/google-services.json`

## Step 4: Sync and Build

1. **Sync Gradle**
   - In Android Studio, click "Sync Now" or "Sync Project with Gradle Files"

2. **Clean and Rebuild**
   ```bash
   ./gradlew clean build
   ```

3. **Run the app** on your device or emulator

## Step 5: Test Google Sign-In

1. Launch the app
2. Navigate to the Login screen
3. Click on "Sign in with Google" button
4. Select a Google account
5. The app should authenticate and log you in

## Implementation Details

### Files Modified/Created:

1. **Build Configuration:**
   - `app/build.gradle.kts` - Added dependencies and BuildConfig field
   - `local.properties` - Added GOOGLE_CLIENT_ID configuration

2. **Code Files:**
   - `GoogleSignInManager.java` - Manages Google Sign-In operations
   - `AuthRepository.java` - Added `loginWithGoogle()` method
   - `AuthViewModel.java` - Added `loginWithGoogle()` method
   - `LoginActivity.java` - Integrated Google Sign-In flow

3. **Resources:**
   - `ic_google_logo.xml` - Google logo drawable for the button

### Dependencies Added:
```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-auth")

// Google Sign-In
implementation("com.google.android.gms:play-services-auth:21.2.0")
```

### How It Works:

1. **User clicks "Sign in with Google"** button
2. **GoogleSignInClient** launches the Google account picker
3. **User selects an account** and grants permissions
4. **Google returns an ID token** to your app
5. **Firebase Auth** uses the ID token to authenticate the user
6. **Your backend** receives the Firebase ID token (or you can exchange it)
7. **User is logged in** and navigated to the main screen

## Backend Integration (Optional)

Currently, the implementation uses Firebase Authentication directly. If you want to integrate with your Supabase backend:

### Option 1: Add Firebase Token Verification to Backend
- Send the Firebase ID token to your Supabase backend
- Verify the token on the server side
- Create/link the user account in your database

### Option 2: Use Supabase Social Login
- Configure Google OAuth in Supabase
- Update the implementation to use Supabase's social login instead

## Troubleshooting

### Issue: "Sign-in failed with error code 10"
**Solution:** You haven't added the SHA-1 fingerprint to Firebase Console
- Follow Step 1, substep 5 again

### Issue: "ID token is null"
**Solution:** The Web Client ID is incorrect or not configured
- Make sure you're using the **Web Client ID**, not the Android Client ID
- Verify the `GOOGLE_CLIENT_ID` in `local.properties`

### Issue: "Google Sign-In button doesn't appear"
**Solution:** Check the layout file
- The button exists in `activity_login.xml`
- Make sure `btnGoogleLogin` is properly initialized

### Issue: "Failed to get Firebase token"
**Solution:**
- Check your Firebase project configuration
- Ensure `google-services.json` is up to date
- Verify internet connection

## Security Notes

- ✅ `local.properties` is in `.gitignore` and won't be committed
- ✅ Never commit your Web Client ID to version control
- ✅ Use different OAuth clients for debug and release builds (optional)
- ✅ Enable Google Play App Signing for production builds

## Next Steps

1. Test the implementation thoroughly
2. Add error handling for edge cases
3. Implement account linking (if user already has an account)
4. Add analytics to track sign-in success/failure rates
5. Consider implementing Sign in with Apple for iOS version

## Support

If you encounter issues:
1. Check the logcat output for error messages
2. Verify all configuration steps were completed
3. Ensure Firebase project is properly set up
4. Check that google-services.json is up to date

---

**Note:** Remember to replace `YOUR_ACTUAL_WEB_CLIENT_ID_HERE` with your actual Web Client ID from Firebase Console!
