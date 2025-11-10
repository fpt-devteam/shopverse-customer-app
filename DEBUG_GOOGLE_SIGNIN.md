# Debug Guide for Google Sign-In

## How to View Logs

### In Android Studio:
1. Open "Logcat" tab at the bottom
2. Select your device/emulator
3. Select your app package: `com.example.shopverse_customer_app`
4. Use these filters to see specific logs:

**Filter by tag:**
- `LoginActivity` - Login screen logs
- `AuthViewModel` - ViewModel logs
- `AuthRepository` - Repository/API logs
- `GoogleSignInManager` - Google Sign-In configuration logs

**Filter by log level:**
- `Debug` - All debug information
- `Error` - Only errors

### Expected Log Flow:

When you click "Sign in with Google" button, you should see:

```
1. LoginActivity logs:
   D/LoginActivity: === Starting Google Sign-In flow ===
   D/LoginActivity: GoogleSignInClient: initialized
   D/LoginActivity: Sign-In Intent created
   D/LoginActivity: Launching Google Sign-In activity...

2. GoogleSignInManager logs (on app startup):
   D/GoogleSignInManager: === Initializing Google Sign-In ===
   D/GoogleSignInManager: Client ID from BuildConfig: [your_client_id]
   D/GoogleSignInManager: GoogleSignInClient created: SUCCESS

3. After selecting Google account:
   D/LoginActivity: Google Sign-In result received
   D/LoginActivity: Result code: -1
   D/LoginActivity: RESULT_OK: -1
   D/LoginActivity: Result is OK
   D/LoginActivity: Intent data is not null
   D/LoginActivity: === Handling Google Sign-In Result ===
   D/LoginActivity: Task is successful: true
   D/LoginActivity: Account email: [your_email]
   D/LoginActivity: ID Token is PRESENT
   D/LoginActivity: Full ID Token: [long_token_string]

4. AuthViewModel logs:
   D/AuthViewModel: === loginWithGoogle called ===
   D/AuthViewModel: ID Token is present
   D/AuthViewModel: Calling authRepository.loginWithGoogle()

5. AuthRepository logs:
   D/AuthRepository: === loginWithGoogle in AuthRepository ===
   D/AuthRepository: Request body created: {"id_token":"...","provider":"google"}
   D/AuthRepository: Calling Supabase Auth API endpoint
   D/AuthRepository: === Supabase Auth Response Received ===
   D/AuthRepository: Response code: 200
   D/AuthRepository: === Google Sign-In SUCCESS ===
```

## Common Issues and What to Look For:

### Issue 1: "Nothing happens after selecting account"
**Look for these logs:**
```
D/LoginActivity: Google Sign-In result received
D/LoginActivity: Result code: [number]
```

**If you DON'T see these logs:**
- The ActivityResultLauncher might not be registered properly
- Try rebuilding the app

**If you see `Result code: 0` (RESULT_CANCELED):**
- User canceled the sign-in
- OR Google Sign-In configuration is wrong

### Issue 2: "ID Token is NULL"
**Look for:**
```
E/LoginActivity: ID token is null!
E/LoginActivity: Server Client ID might be incorrectly configured
```

**This means:**
- The `GOOGLE_CLIENT_ID` in `local.properties` is wrong
- OR you didn't create an Android OAuth client in Google Cloud Console
- OR the SHA-1 fingerprint doesn't match

**Solution:**
1. Verify `GOOGLE_CLIENT_ID` in `local.properties` is the Web Client ID
2. Get your SHA-1 fingerprint:
   ```bash
   cd C:\Users\NHATTHANG\FptUniversity\PRM392\shopverse-customer-app
   gradlew signingReport
   ```
3. Create Android OAuth client in Google Cloud Console with this SHA-1
4. Wait 5 minutes for changes to propagate
5. Uninstall and reinstall the app

### Issue 3: "Error code 10"
**Look for:**
```
E/LoginActivity: Status code: 10
```

**This means:**
- Developer console not set up correctly
- No Android OAuth client configured
- SHA-1 fingerprint doesn't match

**Solution:**
- Follow Issue 2 solution above

### Issue 4: "Supabase returns 400 Bad Request"
**Look for:**
```
E/AuthRepository: Response code: 400
E/AuthRepository: Error body: [error details]
```

**Common causes:**
- Google provider not enabled in Supabase
- Wrong Web Client ID configured in Supabase
- ID token format is incorrect

**Solution:**
1. Go to Supabase Dashboard → Authentication → Providers → Google
2. Enable it
3. Add Web Client ID and Client Secret from Google Cloud Console
4. Save

### Issue 5: "Network error"
**Look for:**
```
E/AuthRepository: === Supabase Auth Network Error ===
```

**This means:**
- No internet connection
- Supabase is down
- Wrong Supabase URL in configuration

## Step-by-Step Debugging Process:

### Step 1: Check GoogleSignInManager Initialization
Run the app and look for:
```
D/GoogleSignInManager: Client ID from BuildConfig: [should be your client ID]
```

**If it says `YOUR_GOOGLE_CLIENT_ID_HERE`:**
- Your `local.properties` wasn't loaded correctly
- Sync Gradle and rebuild

### Step 2: Click "Sign in with Google" Button
Look for:
```
D/LoginActivity: === Starting Google Sign-In flow ===
```

**If you DON'T see this:**
- Button click listener is not working
- Check if `btnGoogleLogin` is null

### Step 3: Select a Google Account
Look for:
```
D/LoginActivity: Google Sign-In result received
D/LoginActivity: Result code: -1
```

**Result codes:**
- `-1` (RESULT_OK) = Success
- `0` (RESULT_CANCELED) = User canceled or config error

### Step 4: Check ID Token
Look for:
```
D/LoginActivity: ID Token is PRESENT
D/LoginActivity: Full ID Token: [token]
```

**If ID Token is NULL:**
- See Issue 2 above
- This is the most common problem!

### Step 5: Check Supabase API Call
Look for:
```
D/AuthRepository: Request body created: {"id_token":"...","provider":"google"}
```

Then look for response:
```
D/AuthRepository: Response code: [number]
```

**Response codes:**
- `200` = Success! ✅
- `400` = Bad request (config error)
- `401` = Unauthorized (wrong API key)
- `422` = Invalid ID token

## Quick Checklist:

Run through this checklist and check the logs for each step:

- [ ] App builds successfully without errors
- [ ] `GOOGLE_CLIENT_ID` in `local.properties` is your Web Client ID
- [ ] GoogleSignInManager shows correct Client ID in logs
- [ ] Clicking button shows "Starting Google Sign-In flow"
- [ ] Google account picker appears
- [ ] After selecting account, see "Result code: -1"
- [ ] See "ID Token is PRESENT" (NOT NULL!)
- [ ] See "Calling authRepository.loginWithGoogle()"
- [ ] See "Request body created" with your ID token
- [ ] See "Response code: 200" from Supabase
- [ ] See "Google Sign-In SUCCESS"
- [ ] User is logged in and navigated to home screen

## Commands to Help Debug:

### Get current git status:
```bash
git status
```

### Clean and rebuild:
```bash
gradlew clean build
```

### Get SHA-1 fingerprint:
```bash
gradlew signingReport
```

### View logcat in terminal (if needed):
```bash
adb logcat -s LoginActivity:D AuthViewModel:D AuthRepository:D GoogleSignInManager:D
```

## Still Having Issues?

Please share these logs:
1. The full logcat output starting from "Starting Google Sign-In flow"
2. The SHA-1 fingerprint from `gradlew signingReport`
3. Screenshot of Google Cloud Console OAuth clients
4. Screenshot of Supabase Google provider configuration

---

**Tip:** The most common issue is ID Token being NULL. This is almost always because:
1. No Android OAuth client created in Google Cloud Console, OR
2. Wrong SHA-1 fingerprint, OR
3. Using wrong Client ID (Android Client ID instead of Web Client ID)
