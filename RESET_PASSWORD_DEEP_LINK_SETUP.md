## Complete Password Reset with Deep Link Setup Guide

## Overview

This guide shows you how to set up the complete password reset flow where:
1. User requests password reset
2. User receives email with magic link
3. **User clicks link → Opens app directly** (shopverse://auth/reset)
4. User enters new password in the app
5. Password is reset successfully

## Files Created/Modified

### 1. AndroidManifest.xml
- Added deep link intent filter for `shopverse://auth/reset`
- ResetPasswordActivity is exported and handles the deep link

### 2. ResetPasswordActivity.java
- Receives access_token from deep link
- Shows password reset form
- Calls Supabase API to reset password
- Navigates to login on success

### 3. activity_reset_password.xml
- Password reset form UI
- New password and confirm password fields

### 4. SupabaseAuthApi.java
- Added `resetPassword()` endpoint

## Supabase Configuration

### Step 1: Configure Redirect URL in Supabase

1. **Go to Supabase Dashboard:**
   - Visit: https://supabase.com/dashboard
   - Select your project: `uehonyhpopuxynbzshyo`

2. **Navigate to Authentication → URL Configuration**

3. **Add Redirect URL:**
   ```
   shopverse://auth/reset
   ```

   Add this to the **"Redirect URLs"** list.

   Click **Save**.

### Step 2: Update Email Template

1. **Go to:** **Authentication** → **Email Templates**

2. **Click on:** **"Reset Password"**

3. **Update the Confirmation URL:**

   By default, Supabase uses `{{ .ConfirmationURL }}` which includes tokens in the URL fragment (after `#`).

   **IMPORTANT:** Keep the default `{{ .ConfirmationURL }}` but modify the redirect URL!

   Supabase generates URLs like:
   ```
   https://yourproject.supabase.co/auth/v1/verify?redirect_to=shopverse://auth/reset&token=xxx&type=recovery
   ```

   Which then redirects to:
   ```
   shopverse://auth/reset#access_token=xxx&refresh_token=xxx&type=recovery
   ```

   **Complete Email Template Example:**
   ```html
   <h2>Reset Your ShopVerse Password</h2>
   <p>Hi {{ .Email }},</p>
   <p>Someone requested a password reset for your ShopVerse account.</p>
   <p>Click the button below to reset your password:</p>
   <p>
     <a href="{{ .ConfirmationURL }}"
        style="background-color: #D32F2F; color: white; padding: 12px 24px; text-decoration: none; border-radius: 8px; display: inline-block;">
       Reset Password in App
     </a>
   </p>
   <p><strong>Important:</strong> This link will expire in 60 minutes.</p>
   <p>If you didn't request this, you can safely ignore this email.</p>
   <br>
   <p>Thanks,<br>The ShopVerse Team</p>
   ```

   **Important:** Just use `{{ .ConfirmationURL }}` - Supabase will automatically append the tokens as URL fragments!

4. **Click Save**

## How It Works

### Complete Flow:

```
1. User clicks "Forgot Password" in app
   ↓
2. User enters email
   ↓
3. App calls: POST /auth/v1/recover
   ↓
4. Supabase sends email with magic link:
   shopverse://auth/reset?access_token=xxx&refresh_token=xxx&type=recovery
   ↓
5. User clicks link in email
   ↓
6. Android opens ShopVerse app → ResetPasswordActivity
   ↓
7. Activity extracts access_token from deep link
   ↓
8. User enters new password
   ↓
9. App calls: PUT /auth/v1/user (with access_token in header)
   Body: { "password": "new_password" }
   ↓
10. Supabase resets password
   ↓
11. User redirected to login screen
   ↓
12. User logs in with new password ✅
```

### Deep Link Format:

```
shopverse://auth/reset#access_token=xxx&refresh_token=xxx&type=recovery&expires_at=xxx
```

**Important:** Supabase sends tokens in the URL **fragment** (after `#`), not query parameters!

**Fragment Parameters:**
- `access_token` (Required) - Temporary token to authorize password reset
- `refresh_token` (Optional) - Can be used for session refresh
- `type=recovery` (Optional) - Indicates this is a recovery flow
- `expires_at` (Optional) - Unix timestamp when token expires
- `expires_in` (Optional) - Seconds until expiration (usually 3600 = 1 hour)

## Testing the Complete Flow

### Prerequisites:
1. Supabase redirect URL configured: `shopverse://auth/reset`
2. Email template updated with custom deep link
3. App installed on device/emulator
4. User registered in Supabase

### Test Steps:

1. **Request Password Reset:**
   ```
   - Open app
   - Click "Forgot Password"
   - Enter registered email: test@example.com
   - Click "Continue"
   - See success toast: "Email đặt lại mật khẩu đã được gửi!"
   ```

2. **Check Logcat:**
   ```
   D/ForgotPasswordActivity: Requesting password reset for email: test@example.com
   D/AuthRepository: === Requesting Password Reset ===
   D/AuthRepository: Response code: 200
   D/AuthRepository: ✅ Password reset email sent successfully
   ```

3. **Check Email:**
   ```
   - Open email inbox (or spam folder)
   - Find email: "Reset Your ShopVerse Password"
   - Verify link format:
     shopverse://auth/reset?access_token=xxx&refresh_token=xxx&type=recovery
   ```

4. **Click Link in Email:**
   ```
   - Click "Reset Password in App" button
   - OR click the deep link URL
   ```

5. **App Opens Automatically:**
   ```
   - Android should open ShopVerse app
   - ResetPasswordActivity should appear
   - Form with "New Password" and "Confirm Password" fields
   ```

6. **Check Logcat:**
   ```
   D/ResetPasswordActivity: === ResetPasswordActivity Created ===
   D/ResetPasswordActivity: Deep link received
   D/ResetPasswordActivity: Scheme: shopverse
   D/ResetPasswordActivity: Host: auth
   D/ResetPasswordActivity: Path: /reset
   D/ResetPasswordActivity: Access token present: true
   D/ResetPasswordActivity: Recovery token extracted successfully
   ```

7. **Enter New Password:**
   ```
   - New Password: test123456
   - Confirm Password: test123456
   - Click "Đặt lại mật khẩu"
   ```

8. **Check Logcat:**
   ```
   D/ResetPasswordActivity: === Validate Password Reset ===
   D/ResetPasswordActivity: Validation passed - submitting password reset
   D/ResetPasswordActivity: === Submitting Password Reset ===
   D/ResetPasswordActivity: Calling Supabase API: PUT /auth/v1/user
   D/ResetPasswordActivity: === Password Reset Response ===
   D/ResetPasswordActivity: Response code: 200
   D/ResetPasswordActivity: ✅ Password reset successful
   D/ResetPasswordActivity: Navigating to login screen
   ```

9. **Success!**
   ```
   - See toast: "✅ Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới."
   - App navigates to LoginActivity
   ```

10. **Login with New Password:**
    ```
    - Enter email: test@example.com
    - Enter password: test123456 (the new password)
    - Click "Login"
    - Should login successfully! ✅
    ```

## Troubleshooting

### Issue 1: "Link doesn't open the app"

**Possible causes:**
- App not installed
- Deep link not configured properly
- Android cache issue

**Solutions:**
1. **Verify AndroidManifest.xml:**
   ```xml
   <activity
       android:name=".ui.auth.ResetPasswordActivity"
       android:exported="true"
       android:launchMode="singleTask">
       <intent-filter>
           <action android:name="android.intent.action.VIEW" />
           <category android:name="android.intent.category.DEFAULT" />
           <category android:name="android.intent.category.BROWSABLE" />
           <data
               android:scheme="shopverse"
               android:host="auth"
               android:path="/reset" />
       </intent-filter>
   </activity>
   ```

2. **Test deep link manually:**
   ```bash
   adb shell am start -W -a android.intent.action.VIEW -d "shopverse://auth/reset?access_token=test&type=recovery" com.example.shopverse_customer_app
   ```

3. **Clear app data and reinstall:**
   ```bash
   adb uninstall com.example.shopverse_customer_app
   ./gradlew installDebug
   ```

### Issue 2: "Access token is missing"

**Check logs:**
```
E/ResetPasswordActivity: ERROR: Access token is missing!
```

**Possible causes:**
- Email template doesn't include `{{ .Token }}`
- Link format is wrong
- Supabase didn't generate token

**Solutions:**
1. Verify email template has: `?access_token={{ .Token }}`
2. Check received email - inspect the actual link
3. Request a new password reset email

### Issue 3: "Response code: 401 Unauthorized"

**Check logs:**
```
E/ResetPasswordActivity: Response code: 401
E/ResetPasswordActivity: Error body: {"error":"Invalid token"}
```

**Possible causes:**
- Access token expired (60 minute limit)
- Access token invalid or corrupted
- Wrong token used

**Solutions:**
1. Request a new password reset email (tokens expire after 60 minutes)
2. Click the link immediately after receiving email
3. Don't reuse old reset links

### Issue 4: "Response code: 422 Unprocessable Entity"

**Possible cause:** Password doesn't meet requirements

**Solution:**
- Ensure password is at least 6 characters
- Check Supabase password requirements settings

### Issue 5: "Email goes to spam"

**Solutions:**
1. Check spam/junk folder
2. Set up custom SMTP (SendGrid, AWS SES, etc.)
3. Add your domain's SPF/DKIM records
4. Whitelist sender email

## Verification Checklist

Before testing, verify:

- [ ] AndroidManifest.xml has ResetPasswordActivity with intent filter
- [ ] Supabase redirect URL includes: `shopverse://auth/reset`
- [ ] Email template uses custom deep link with `{{ .Token }}`
- [ ] App is installed on test device
- [ ] Test user exists in Supabase
- [ ] Test user email is accessible

After successful test:

- [ ] Email received with reset link
- [ ] Link opens app (not browser)
- [ ] ResetPasswordActivity appears
- [ ] Access token extracted from URL
- [ ] New password form appears
- [ ] Password validation works
- [ ] Password reset API call succeeds (200 OK)
- [ ] Success message shows
- [ ] Navigates to login screen
- [ ] Can login with new password

## Advanced: Fallback to Web

If you want a fallback for users who don't have the app installed:

**Email template with both options:**
```html
<h2>Reset Your ShopVerse Password</h2>
<p>Click the button to reset your password:</p>

<!-- Mobile app deep link -->
<p>
  <a href="shopverse://auth/reset?access_token={{ .Token }}&refresh_token={{ .RefreshToken }}&type=recovery">
    Reset in App
  </a>
</p>

<!-- Web fallback -->
<p>Or reset via web:</p>
<p>
  <a href="https://yourwebsite.com/reset-password?token={{ .Token }}">
    Reset on Website
  </a>
</p>
```

You'll need to create a web page to handle the web flow.

## Security Notes

- ✅ Access tokens expire after 60 minutes
- ✅ Tokens are single-use (can't reuse old links)
- ✅ Password must be at least 6 characters
- ✅ Token is passed securely via HTTPS email
- ✅ Token is verified server-side by Supabase
- ⚠️ Deep links can be intercepted - use HTTPS for email delivery
- ⚠️ Don't log full access tokens in production

## Production Checklist

Before going to production:

- [ ] Remove all `Log.d()` statements containing tokens
- [ ] Set up custom SMTP provider
- [ ] Configure proper email sender domain
- [ ] Test on multiple email providers (Gmail, Outlook, Yahoo)
- [ ] Test deep link on multiple Android versions
- [ ] Add analytics to track reset success rate
- [ ] Set up monitoring for failed resets
- [ ] Add rate limiting to prevent abuse
- [ ] Create web fallback for users without app

---

**Complete! 🎉**

Your password reset flow with deep link is now fully functional. Users can reset their password by clicking a link in their email, which opens your app directly!
