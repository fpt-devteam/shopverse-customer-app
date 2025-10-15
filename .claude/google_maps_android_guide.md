# 🗺️ Prompt for Claude — Google Maps Integration (Android Java)

You are a **senior Android engineer**. Implement Google Maps integration in an **existing Android app (Java)** with the following features and deliverables.

---

## 🎯 Required Features

1. **Display store locations** (markers) loaded from a **hardcoded JSON file** in the app (no backend).
2. **Find the nearest store** to the user's current location and highlight it.
3. **Provide directions** from the user's current location to a selected store.

   * **Baseline:** launch Google Maps app via intent.
   * **Optional bonus:** in-app polyline using Directions API (only if time allows and key is enabled).

Deliver **clean, production-quality Java code** (AndroidX), with clear comments and separation of concerns.

---

## ⚙️ Project Setup Constraints

* **Language:** Java (not Kotlin)
* **Google Maps:** `com.google.android.gms:play-services-maps`
* **Location:** `FusedLocationProviderClient`
* **Min API:** ~24+ (reasonable default)
* **UI:** `SupportMapFragment`
* **Permissions:** Runtime location permissions (FINE/COARSE)

---

## 📂 Target Project Structure

```
app/
 └─ src/main/
     ├─ AndroidManifest.xml
     ├─ java/com/example/app/
     │   ├─ data/
     │   │   ├─ Store.java
     │   │   └─ StoreRepository.java         // loads JSON from assets
     │   ├─ location/
     │   │   ├─ LocationHelper.java          // fused location + permission helpers
     │   │   └─ DistanceUtils.java           // Haversine distance
     │   ├─ ui/maps/
     │   │   ├─ MapsActivity.java            // main map screen (SupportMapFragment)
     │   │   └─ MapUiController.java         // markers, camera, info windows
     │   └─ utils/
     │       └─ JsonUtils.java               // read JSON from assets
     ├─ res/layout/
     │   ├─ activity_maps.xml
     │   └─ layout_store_action_panel.xml    // small panel with actions
     └─ assets/
         └─ stores.json
```

---

## 🧱 Gradle Dependencies (Module: `app`)

```gradle
dependencies {
    implementation 'com.google.android.gms:play-services-maps:latest.release'
    implementation 'com.google.android.gms:play-services-location:latest.release'
    implementation 'androidx.appcompat:appcompat:latest.release'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

> Use latest stable versions.

---

## 🗝️ AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<application ...>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="@string/google_maps_key" />
</application>
```

> Put your Maps key in `res/values/strings.xml` as `google_maps_key`.

---

## 📄 Sample JSON (`assets/stores.json`)

```json
{
  "stores": [
    {
      "id": "store-a",
      "name": "Store A - District 1",
      "latitude": 10.776889,
      "longitude": 106.700806,
      "address": "123 Le Loi, District 1",
      "hours": "08:00-22:00"
    },
    {
      "id": "store-b",
      "name": "Store B - District 3",
      "latitude": 10.78412,
      "longitude": 106.69482,
      "address": "88 Vo Van Tan, District 3",
      "hours": "09:00-21:00"
    },
    {
      "id": "store-c",
      "name": "Store C - Phu Nhuan",
      "latitude": 10.79707,
      "longitude": 106.67773,
      "address": "45 Phan Xich Long, Phu Nhuan",
      "hours": "08:30-22:00"
    }
  ]
}
```

---

## 🧩 Core Components & Responsibilities

### `Store.java`

* POJO fields: `id`, `name`, `latitude`, `longitude`, `address`, `hours`.
* Getters, optional `toString()`.

### `JsonUtils.java`

* Read `assets/stores.json` into `String`.

### `StoreRepository.java`

* Parse `stores.json` using Gson → `List<Store>`.
* Validate entries; skip invalid ones gracefully.

### `LocationHelper.java`

Encapsulate fused location & permission flow:

* `boolean hasLocationPermission(Context)`
* `void requestLocationPermission(Activity, int REQUEST_CODE)`
* `void getLastKnownLocation(Consumer<Location> onSuccess, Runnable onFailure)`
* If permission denied or location unavailable, fall back gracefully (features still usable except nearest/directions-from-me).

### `DistanceUtils.java`

* `double haversineKm(double lat1, double lon1, double lat2, double lon2)`

---

## 🗺️ UI & Map

### `activity_maps.xml`

* Root `FrameLayout` or `CoordinatorLayout`.
* `<fragment>` for `SupportMapFragment` with id `@+id/map`.
* Bottom overlay (include `layout_store_action_panel.xml`).

### `layout_store_action_panel.xml`

* Minimal `LinearLayout` with:

  * `TextView` store title
  * `TextView` store address
  * `Button` **Nearest Store**
  * `Button` **Get Directions**

---

## 🧭 Map Controller

### `MapUiController.java`

* Accept a `GoogleMap` instance.
* Methods:

  * `Map<String, Marker> addStoreMarkers(List<Store>)` → keep mapping `storeId -> Marker`.
  * `void moveCameraToBounds(List<LatLng> points, int paddingPx)`
  * `void highlightNearest(Marker marker)` (e.g., set azure icon, animate camera).
  * `void clearHighlight()` (restore default icon if needed).
  * `void showStoreInfo(Store store)` (update panel text fields).

> Marker visuals: default **red**; nearest **azure** (`BitmapDescriptorFactory.HUE_AZURE`).
> Camera: fit all stores at start; zoom to nearest on highlight.

---

## 🚀 `MapsActivity.java` Flow (High-Level)

1. **onCreate**

   * Inflate layout, bind panel views & buttons.
   * Get `SupportMapFragment` → `getMapAsync(this)`.

2. **onMapReady(GoogleMap map)**

   * Keep `mMap = map`.
   * Load `stores` from `StoreRepository`.
   * Add markers, keep `Marker ↔ Store` map.
   * `moveCameraToBounds(allStoreLatLngs, padding)`.
   * Set `OnMarkerClickListener`:

     * Set `selectedStore`, update panel (title/address), optionally show default info window.
   * Location:

     * If permission granted:

       * `mMap.setMyLocationEnabled(true)`.
       * Fetch last known location asynchronously.
     * Else: request permission; on grant, enable & fetch.

3. **Nearest Store (button)**

   * If location available:

     * Loop stores → compute Haversine distance → get min.
     * Update highlight (azure icon) & animate camera to nearest.
     * Update panel with nearest store info.
   * Else: show toast explaining location permission needed.

4. **Get Directions (button)**

   * If a store is selected (or nearest resolved), open:

     ```java
     Uri nav = Uri.parse("google.navigation:q=" + lat + "," + lng);
     Intent i = new Intent(Intent.ACTION_VIEW, nav);
     i.setPackage("com.google.android.apps.maps");
     startActivity(i);
     ```
   * **Fallback** (no Maps app):

     ```java
     Uri web = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
     startActivity(new Intent(Intent.ACTION_VIEW, web));
     ```

5. **Optional:** In-app route polyline (if Directions API enabled).

   * If not, skip (baseline intent is enough).

---

## 🔐 Runtime Permissions

* Request `ACCESS_FINE_LOCATION` (and/or `COARSE` if needed).
* If denied:

  * Keep app functional (markers still visible).
  * Disable **Nearest** and **Directions from my location** or show rationale.

---

## 🎨 Visual & UX Details

* **Marker colors:** normal stores = red; nearest store = azure.
* **Camera behavior:**

  * On first load → fit all stores with padding.
  * On highlight → `animateCamera(CameraUpdateFactory.newLatLngZoom(nearest, 15f))`.
* **Panel:**

  * Shows selected/nearest store name & address.
  * Buttons: "Nearest Store", "Get Directions".

---

## ✅ Acceptance Criteria

* [ ] Stores load from `assets/stores.json`.
* [ ] All stores appear as markers.
* [ ] Tapping a marker updates the info panel with store name/address.
* [ ] If location permission granted, MyLocation dot shows.
* [ ] **Nearest Store** computes by Haversine, highlights, and recenters camera.
* [ ] **Get Directions** opens Google Maps app; if unavailable, opens browser URL.
* [ ] Graceful behavior when permission denied or location unavailable.
* [ ] Clean, modular Java code with comments; no crashes on edge cases.

---

## 🧪 Testing Notes

* Test with GPS **on/off** and permissions **granted/denied**.
* Move emulator/device mock location to verify nearest changes.
* Uninstall Google Maps to verify browser fallback.
* Corrupt one JSON entry (e.g., missing lat) → app should skip without crashing.

---

## ✨ Optional Enhancements (If Time Permits)

* Custom `InfoWindowAdapter` for richer marker popups (name + address + hours).
* Persist last selected store across rotation (`onSaveInstanceState`).
* Simple clustering (if store count grows), but **not required now**.

---

## 🔎 Implementation Hints (Snippets)

**Haversine (km):**

```java
public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371.0; // km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLon/2) * Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}
```

**Enable MyLocation (after permission):**

```java
if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
    mMap.setMyLocationEnabled(true);
}
```

**Directions intent (+ fallback):**

```java
Uri nav = Uri.parse("google.navigation:q=" + lat + "," + lng);
Intent i = new Intent(Intent.ACTION_VIEW, nav);
i.setPackage("com.google.android.apps.maps");
try {
    startActivity(i);
} catch (ActivityNotFoundException e) {
    Uri web = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
    startActivity(new Intent(Intent.ACTION_VIEW, web));
}
```

---

**Please implement everything above in Java with full working code, XML layouts, and the provided JSON. Add concise comments explaining permissions, location retrieval, nearest computation, and the directions intent.**