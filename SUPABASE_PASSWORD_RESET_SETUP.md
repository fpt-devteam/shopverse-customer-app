# Supabase Password Reset Setup Guide

## Overview

This guide will help you configure email-based password reset for your ShopVerse Customer App using Supabase.

## Step 1: Configure Email Settings in Supabase

### 1.1 Go to Supabase Dashboard

1. Visit: https://supabase.com/dashboard
2. Select your project: `uehonyhpopuxynbzshyo`
3. Navigate to: **Authentication** → **Email Templates**

### 1.2 Customize Reset Password Email Template

Click on **"Reset Password"** template and customize it:

**Subject:**
```
Reset Your ShopVerse Password
```

**Email Body (HTML):**
```html
<h2>Reset Your Password</h2>
<p>Hi there,</p>
<p>Someone requested a password reset for your ShopVerse account.</p>
<p>Click the button below to reset your password:</p>
<p><a href="{{ .ConfirmationURL }}" style="background-color: #D32F2F; color: white; padding: 12px 24px; text-decoration: none; border-radius: 8px; display: inline-block;">Reset Password</a></p>
<p>Or copy and paste this URL into your browser:</p>
<p>{{ .ConfirmationURL }}</p>
<p>If you didn't request this, you can safely ignore this email.</p>
<p>This link will expire in 60 minutes.</p>
<br>
<p>Thanks,<br>The ShopVerse Team</p>
```

**Important:** The `{{ .ConfirmationURL }}` placeholder will be replaced by Supabase with the actual password reset link.

### 1.3 Configure Redirect URLs

1. Go to: **Authentication** → **URL Configuration**
2. Set **Site URL** to your app's deep link or website:
   - For testing: `http://localhost:3000` or your dev URL
   - For production: Your actual website URL
3. Add **Redirect URLs**:
   - Add: `shopverse://reset-password` (for mobile deep linking)
   - Or your website's reset password page URL

### 1.4 Enable Email Confirmations

1. Go to: **Authentication** → **Settings**
2. Ensure **"Enable email confirmations"** is ON
3. Set **Password requirements** as desired:
   - Minimum length: 6 characters (or more)

## Step 2: Configure Email Provider

Supabase needs an email provider to send emails. By default, Supabase uses their own SMTP server (limited for development).

### Option 1: Use Supabase's Built-in Email (Development Only)

**Limitations:**
- Limited to 3-4 emails per hour
- Only for testing
- Emails might go to spam

**Setup:**
- Already enabled by default
- No additional configuration needed

### Option 2: Use Custom SMTP (Recommended for Production)

1. Go to: **Project Settings** → **Authentication**
2. Scroll to **SMTP Settings**
3. Enable **"Enable Custom SMTP"**
4. Configure your email provider:

**Example: Gmail SMTP**
```
SMTP Host: smtp.gmail.com
SMTP Port: 587
SMTP User: your-email@gmail.com
SMTP Password: [App Password, not your regular password]
Sender Email: your-email@gmail.com
Sender Name: ShopVerse
```

**Example: SendGrid**
```
SMTP Host: smtp.sendgrid.net
SMTP Port: 587
SMTP User: apikey
SMTP Password: [Your SendGrid API Key]
Sender Email: noreply@shopverse.com
Sender Name: ShopVerse
```

**Example: AWS SES, Mailgun, Postmark** - Follow their SMTP docs

## Step 3: Test the Flow in Your App

### 3.1 How It Works

1. User clicks "Forgot Password" on login screen
2. User enters their email
3. App calls Supabase: `POST /auth/v1/recover`
4. Supabase sends email with reset link
5. User clicks link in email
6. Link redirects to your configured URL
7. User enters new password
8. Password is updated

### 3.2 Test Password Reset

1. **Launch the app**
2. **Go to Login screen**
3. **Click "Forgot Password"**
4. **Enter a valid email** (must be registered in Supabase)
5. **Click "Continue"**
6. **Watch logs:**
   ```
   D/ForgotPasswordActivity: Submit button clicked
   D/ForgotPasswordActivity: Email entered: test@example.com
   D/ForgotPasswordActivity: Requesting password reset
   D/AuthRepository: === Requesting Password Reset ===
   D/AuthRepository: Email: test@example.com
   D/AuthRepository: Calling Supabase Auth API: POST /auth/v1/recover
   D/AuthRepository: Response code: 200
   D/AuthRepository: ✅ Password reset email sent successfully
   ```
7. **Check email inbox** - you should receive the reset email
8. **Click the link** in the email

## Step 4: Handle Password Reset Link (Optional - For Mobile Deep Link)

If you want the reset link to open your app directly:

### 4.1 Update AndroidManifest.xml

Add this intent filter to handle the deep link:

```xml
<activity
    android:name=".ui.auth.ResetPasswordActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="shopverse"
            android:host="reset-password" />
    </intent-filter>
</activity>
```

### 4.2 Create ResetPasswordActivity

You'll need to create a new activity to handle the password reset and extract the token from the deep link.

**For now, you can use the default web-based reset flow.**

## Step 5: Verify Email Configuration

### Check Email Settings

1. **Go to Supabase Dashboard** → **Authentication** → **Settings**
2. Verify:
   - ✅ Enable email confirmations: ON
   - ✅ Confirm email: Required for sign-up (optional)
   - ✅ Secure email change: Enabled
   - ✅ Double confirm email changes: Enabled (optional)

### Test Email Delivery

1. Send a test password reset
2. Check these folders:
   - Inbox
   - Spam/Junk (Supabase emails often go here initially)
   - Promotions (if using Gmail)

3. **If email doesn't arrive:**
   - Check Supabase logs: **Authentication** → **Logs**
   - Verify email is registered in Supabase: **Authentication** → **Users**
   - Check SMTP configuration if using custom SMTP

## Common Issues

### Issue 1: "Email not received"

**Possible causes:**
- Email went to spam
- User not registered in Supabase
- SMTP not configured (using built-in, which is rate-limited)
- Wrong email address

**Debug:**
1. Check Supabase logs: **Authentication** → **Logs**
2. Look for error messages
3. Verify user exists: **Authentication** → **Users**

### Issue 2: "Response code: 400"

**Check logs for:**
```
D/AuthRepository: Response code: 400
D/AuthRepository: Error body: {"error":"..."}
```

**Common causes:**
- Invalid email format
- Email not found in database
- Rate limiting (too many requests)

**Solution:**
- Ensure email is valid
- Check user exists in Supabase
- Wait a few minutes if rate-limited

### Issue 3: "Response code: 429"

**Cause:** Rate limiting - too many password reset requests

**Solution:**
- Wait 60 minutes
- Use custom SMTP to increase limits

### Issue 4: "Link expired"

**Cause:** Password reset links expire after 60 minutes (default)

**Solution:**
- Request a new password reset email
- Complete reset process faster
- Configure longer expiration in Supabase (not recommended for security)

## Testing Checklist

- [ ] Supabase email template configured
- [ ] Redirect URL configured in Supabase
- [ ] SMTP configured (for production)
- [ ] App can reach Supabase Auth API
- [ ] "Forgot Password" button works
- [ ] Email validation works (invalid emails rejected)
- [ ] Valid email sends reset email
- [ ] Email arrives in inbox (or spam)
- [ ] Reset link in email works
- [ ] User can successfully reset password
- [ ] Login works with new password

## Expected Logs

### Successful Password Reset Request:

```
D/ForgotPasswordActivity: Submit button clicked
D/ForgotPasswordActivity: Email entered: user@example.com
D/ForgotPasswordActivity: Requesting password reset for email: user@example.com
D/AuthViewModel: requestPasswordReset called
D/AuthRepository: === Requesting Password Reset ===
D/AuthRepository: Email: user@example.com
D/AuthRepository: Request body: {"email":"user@example.com"}
D/AuthRepository: Calling Supabase Auth API: POST /auth/v1/recover
D/AuthRepository: === Password Reset Response ===
D/AuthRepository: Response code: 200
D/AuthRepository: Response message: OK
D/AuthRepository: ✅ Password reset email sent successfully to: user@example.com
D/ForgotPasswordActivity: Password reset email sent successfully
```

### Failed Request (User Not Found):

```
D/AuthRepository: Response code: 400
D/AuthRepository: Error body: {"error":"User not found"}
E/AuthRepository: ❌ Password reset failed
E/ForgotPasswordActivity: Error received: User not found
```

## Production Checklist

Before launching to production:

- [ ] Set up custom SMTP provider (SendGrid, AWS SES, etc.)
- [ ] Customize email template with your branding
- [ ] Test email delivery to multiple email providers (Gmail, Outlook, Yahoo)
- [ ] Configure proper redirect URLs (your website or mobile deep link)
- [ ] Set appropriate password requirements
- [ ] Enable rate limiting protection
- [ ] Test on real devices
- [ ] Add analytics to track password reset success rate

## Support

If password reset isn't working:

1. **Share these logs:**
   - Full logs from clicking "Forgot Password" to the API response
   - Any error messages

2. **Share configuration:**
   - Screenshot of Supabase Email Template settings
   - Screenshot of URL Configuration
   - Is custom SMTP configured? (yes/no)

3. **Share test details:**
   - What email address did you use?
   - Does this email exist in Supabase Users?
   - Did you check spam folder?

---

**TL;DR:**
1. Configure email template in Supabase Dashboard
2. Set up SMTP (optional but recommended)
3. Test with a registered user's email
4. Check spam folder if email doesn't arrive
5. The app code is already implemented and ready to use!
