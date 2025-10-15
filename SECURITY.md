# Security Guidelines

## API Key Management

### ⚠️ IMPORTANT: Never commit API keys to version control!

This project uses a secure approach to manage sensitive API keys:

### How it works:

1. **API keys are stored in `local.properties`**
   - This file is already in `.gitignore` and will NOT be committed
   - Each developer has their own local copy with their own keys

2. **Keys are injected at build time**
   - `build.gradle.kts` reads the key from `local.properties`
   - The key is injected into `AndroidManifest.xml` as a manifest placeholder
   - Also available as `BuildConfig.GOOGLE_MAPS_API_KEY` in code (if needed)

### Setup for new developers:

1. Copy `local.properties.example` to `local.properties`:
   ```bash
   cp local.properties.example local.properties
   ```

2. Get your Google Maps API key:
   - Go to [Google Cloud Console](https://console.cloud.google.com/google/maps-apis/credentials)
   - Create or select a project
   - Enable "Maps SDK for Android"
   - Create credentials → API key
   - Restrict the key (recommended):
     - Application restrictions: Android apps
     - Add your package name: `com.example.shopverse_customer_app`
     - Add your SHA-1 fingerprint

3. Add your API key to `local.properties`:
   ```properties
   GOOGLE_MAPS_API_KEY=your_actual_api_key_here
   ```

4. Sync Gradle and build the project

### What NOT to do:

❌ Never commit `local.properties` to git
❌ Never hardcode API keys in source code
❌ Never store keys in `strings.xml` or other tracked files
❌ Never share your API key in public channels

### What to do if you accidentally commit a key:

1. **Immediately revoke the exposed key** in Google Cloud Console
2. Generate a new API key
3. Update your `local.properties` with the new key
4. Remove the key from git history:
   ```bash
   # Use git-filter-repo or BFG Repo-Cleaner
   # Then force push (coordinate with team!)
   ```

### Additional Security Best Practices:

✅ Use API key restrictions (package name + SHA-1)
✅ Enable only the APIs you need
✅ Set up billing alerts in Google Cloud
✅ Rotate keys periodically
✅ Use different keys for debug and release builds (optional)

## Questions?

If you need help setting up your API key, contact the project maintainers.
