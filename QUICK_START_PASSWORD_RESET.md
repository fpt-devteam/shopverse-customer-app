# Quick Start: Password Reset with Deep Link

## ✅ Fixed: URL Fragment Issue

**Problem:** Supabase sends tokens in URL **fragment** (after `#`), not query parameters (after `?`)

**Your URL format:**
```
shopverse://auth/reset#access_token=xxx&refresh_token=xxx&type=recovery&expires_at=xxx
                      ↑
                    Fragment (not query param!)
```

**Solution:** Updated `ResetPasswordActivity` to parse URL fragments correctly ✅

## 🚀 Quick Setup (2 Steps)

### Step 1: Configure Supabase Redirect URL

1. Go to: https://supabase.com/dashboard
2. Navigate to: **Authentication** → **URL Configuration**
3. Add to **Redirect URLs:**
   ```
   shopverse://auth/reset
   ```
4. Click **Save**

### Step 2: Keep Default Email Template

The default Supabase email template already works! Just make sure it uses:
```html
<a href="{{ .ConfirmationURL }}">Reset Password</a>
```

That's it! Supabase automatically handles the redirect with tokens in the fragment.

## 🧪 Test Now

1. **Request password reset** in app
2. **Check email** and click the link
3. **App should open** → ResetPasswordActivity
4. **Enter new password**
5. **Success!** ✅

## 📊 Expected Logs

After clicking the email link, you should see:

```
D/ResetPasswordActivity: === ResetPasswordActivity Created ===
D/ResetPasswordActivity: Deep link received
D/ResetPasswordActivity: Full URI: shopverse://auth/reset#access_token=xxx...
D/ResetPasswordActivity: URL Fragment: access_token=xxx&refresh_token=xxx&type=recovery...
D/ResetPasswordActivity: Fragment param: access_token = eyJhbGciOiJIUzI1NiI...
D/ResetPasswordActivity: Fragment param: refresh_token = yu44oc7rsvqh
D/ResetPasswordActivity: Fragment param: type = recovery
D/ResetPasswordActivity: Access token present: true
D/ResetPasswordActivity: Refresh token present: true
D/ResetPasswordActivity: ✅ Recovery token extracted successfully from URL fragment
D/ResetPasswordActivity: Access token length: 375
```

## ❌ vs ✅ Comparison

### Before Fix (Wrong):
```
D/ResetPasswordActivity: Access token present: false  ← WRONG!
E/ResetPasswordActivity: ERROR: Access token is missing!
```

### After Fix (Correct):
```
D/ResetPasswordActivity: URL Fragment: access_token=xxx...
D/ResetPasswordActivity: Access token present: true  ← CORRECT!
D/ResetPasswordActivity: ✅ Recovery token extracted successfully
```

## 🔍 How It Works

1. **User clicks email link:**
   ```
   {{ .ConfirmationURL }}
   ```

2. **Supabase redirects to:**
   ```
   shopverse://auth/reset#access_token=xxx&refresh_token=xxx&type=recovery
   ```

3. **Android opens your app** → ResetPasswordActivity

4. **Activity extracts from fragment:**
   ```java
   String fragment = data.getFragment(); // Everything after #
   // Parse: access_token=xxx&refresh_token=xxx&type=recovery
   ```

5. **User resets password** using the access_token

## 🎯 Key Points

- ✅ Tokens are in **URL fragment** (after `#`)
- ✅ Code now uses `data.getFragment()` instead of `data.getQueryParameter()`
- ✅ Works with Supabase's default `{{ .ConfirmationURL }}`
- ✅ Automatically parses all fragment parameters
- ✅ Token expires after 1 hour (3600 seconds)

## 🐛 Troubleshooting

**Still seeing "Access token is missing"?**

Check the logs for:
```
D/ResetPasswordActivity: URL Fragment: null
```

If fragment is null, the URL format might be wrong. Share your email template configuration.

**Token expired?**

```
D/ResetPasswordActivity: Fragment param: expires_at = 1762767806
```

Tokens expire after 1 hour. Request a new reset email.

---

**Ready to test!** Click the reset link in your email and it should work perfectly now. 🎉
