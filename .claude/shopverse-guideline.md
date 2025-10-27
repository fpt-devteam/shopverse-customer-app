

# 🧩 MASTER PROMPT — Shopverse Android (Java + Supabase backend)

## 0) You are

You are a **senior Android engineer** building a **production-grade Android Java application** called **Shopverse**, an **online shopping store**.
There is **no custom backend service** — the app communicates **directly with Supabase REST APIs** for database, authentication, and file storage.
Follow Android best practices (MVVM, LiveData, Repository pattern, Retrofit, Room caching) and use modular clean architecture.

---

## 1) Environment & Access Keys

Use these credentials throughout implementation:

```
SUPABASE_URL = "https://shopverse.supabase.co"
SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.SAMPLE-ANON-KEY-DEV-ONLY._not_a_real_signature_"
```

### Supabase Features Used:

* **Auth:** Email + Password
* **Database:** PostgreSQL (schema defined below)
* **Storage:** Buckets for images (product-images, user-avatars)
* **Edge Functions (optional):** For payment flow (mocked for now)
* **RLS:** Enabled on all tables

---

## 2) Database Schema (aligned with Supabase)

Use this schema inside Supabase SQL Editor or migrations:

```sql
-- ===== Enums =====
CREATE TYPE product_status  AS ENUM ('active','inactive','archived');
CREATE TYPE user_role       AS ENUM ('customer','staff','admin');
CREATE TYPE order_status    AS ENUM ('pending','paid','cancelled','shipped','completed','refunded');
CREATE TYPE payment_type    AS ENUM ('cod','card','ewallet','bank_transfer');
CREATE TYPE discount_status AS ENUM ('draft','active','expired','disabled');

-- ===== Core reference tables =====
CREATE TABLE categories (
  category_id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  category_name text NOT NULL UNIQUE
);

CREATE TABLE brands (
  brand_id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  brand_name text NOT NULL UNIQUE
);

CREATE TABLE stores (
  store_id  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  address   text NOT NULL,
  lat       double precision,
  lng       double precision,
  created_at timestamptz DEFAULT now()
);

-- ===== Users =====
CREATE TABLE profiles (
  user_id      uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email        text NOT NULL UNIQUE,
  display_name text,
  phone        text UNIQUE,
  address      text,
  role         user_role DEFAULT 'customer',
  avatar_url   text,
  created_at   timestamptz DEFAULT now()
);

-- ===== Products =====
CREATE TABLE products (
  product_id    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  category_id   uuid REFERENCES categories(category_id),
  brand_id      uuid REFERENCES brands(brand_id),
  product_name  text NOT NULL,
  description   text,
  product_media text[] DEFAULT '{}',
  stock         int NOT NULL DEFAULT 0 CHECK (stock >= 0),
  unit_price    numeric(18,2) NOT NULL CHECK (unit_price >= 0),
  status        product_status DEFAULT 'active',
  created_at    timestamptz DEFAULT now()
);

-- ===== Discounts =====
CREATE TABLE discounts (
  discount_id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  discount_name text NOT NULL,
  date_start    timestamptz NOT NULL,
  date_end      timestamptz NOT NULL,
  status        discount_status DEFAULT 'draft'
);

-- ===== Cart =====
CREATE TABLE cart_items (
  user_id    uuid NOT NULL REFERENCES profiles(user_id) ON DELETE CASCADE,
  product_id uuid NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
  quantity   int NOT NULL CHECK (quantity > 0),
  PRIMARY KEY (user_id, product_id)
);

-- ===== Orders =====
CREATE TABLE orders (
  order_id       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        uuid NOT NULL REFERENCES profiles(user_id),
  subtotal       numeric(18,2) DEFAULT 0,
  total_discount numeric(18,2) DEFAULT 0,
  shipping_fee   numeric(18,2) DEFAULT 0,
  total          numeric(18,2) DEFAULT 0,
  payment_type   payment_type NOT NULL,
  payment_status text DEFAULT 'unpaid',
  status         order_status DEFAULT 'pending',
  shipping_address jsonb,
  created_at     timestamptz DEFAULT now()
);

CREATE TABLE order_items (
  order_item_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id      uuid NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
  product_id    uuid NOT NULL REFERENCES products(product_id),
  discount_id   uuid REFERENCES discounts(discount_id),
  quantity      int NOT NULL,
  unit_price    numeric(18,2) NOT NULL,
  line_total    numeric(18,2) NOT NULL
);
```

---

## 3) Android App Architecture (MVVM + Clean)

```
com.shopverse
 ┣ data/
 ┃ ┣ model/
 ┃ ┣ remote/           # Retrofit, Supabase REST
 ┃ ┣ local/            # Room DB (offline cache)
 ┃ ┗ repository/
 ┣ ui/
 ┃ ┣ auth/             # Login/Register/Reset Password
 ┃ ┣ home/             # Product listing, search, filter
 ┃ ┣ category/         # Category browsing
 ┃ ┣ cart/             # Cart, checkout
 ┃ ┣ payment/          # Payment confirmation (mock)
 ┃ ┣ profile/          # User profile
 ┃ ┗ map/              # Google Map (store locator)
 ┣ util/
 ┣ viewmodel/
 ┣ di/                 # Hilt modules (if using Hilt)
 ┗ App.java
```

* **Network:** Retrofit2 + OkHttp3
* **JSON:** Gson
* **Async:** Kotlin coroutines are not used (pure Java). Use `ExecutorService` or `LiveData` for async tasks.
* **Image Loading:** Glide or Coil
* **Map:** Google Maps SDK for Android
* **Navigation:** Jetpack Navigation Component

---

## 4) Supabase API Usage (Retrofit Setup)

### Base URL

```
https://shopverse.supabase.co/rest/v1/
```

### Headers

```
apikey: SUPABASE_ANON_KEY
Authorization: Bearer <access_token from Supabase Auth>
Content-Type: application/json
```

### Example Retrofit Interface

```java
public interface SupabaseApi {
    @GET("products")
    Call<List<Product>> getProducts(
        @Header("Authorization") String bearerToken,
        @Query("select") String select,
        @Query("status") String status
    );

    @POST("cart_items")
    Call<ResponseBody> addToCart(
        @Header("Authorization") String bearerToken,
        @Body CartItem item
    );

    @POST("orders")
    Call<ResponseBody> createOrder(
        @Header("Authorization") String bearerToken,
        @Body Order order
    );
}
```

---

## 5) Authentication Flow

* Use **Supabase Auth REST endpoints** (`/auth/v1/signup`, `/auth/v1/token?grant_type=password`)
* Save JWT token in **EncryptedSharedPreferences**
* Use JWT as `Authorization: Bearer <token>` for API calls
* Implement **auto-refresh** using `/auth/v1/token?grant_type=refresh_token`

---

## 6) Modules (matching `.claude` folders)

| Folder                        | Purpose                                  | Main Screens                                           |
| ----------------------------- | ---------------------------------------- | ------------------------------------------------------ |
| `.claude/auth-ui`             | Supabase email/password login & register | LoginActivity, RegisterActivity, ResetPasswordActivity |
| `.claude/home-ui`             | Product list + search + filters          | HomeActivity, ProductDetailActivity                    |
| `.claude/category-ui`         | Browse products by category              | CategoryActivity                                       |
| `.claude/cart-and-payment-ui` | Shopping cart, checkout, payment         | CartActivity, CheckoutActivity, PaymentSuccessActivity |
| `.claude/user-profile-ui`     | Manage profile & orders                  | ProfileActivity, EditProfileActivity, OrdersActivity   |
| `.claude/google-map-ui`       | Store locator using Google Maps          | MapActivity, StoreMarkerAdapter                        |

---

## 7) User Flows

### 🏠 Home & Browse

* Fetch all products with pagination via `/products?select=*`
* Search query → `name=ilike.%term%`
* Filter → category_id, brand_id, price range (via query params)

### 🛒 Cart

* Local persistence in Room DB + sync with `cart_items`
* Add/update/remove items via REST
* “Checkout” → creates an order (`orders` + `order_items`)

### 💳 Checkout

* Address entry (manual + Google Places Autocomplete)
* Payment mock (mark `payment_status='paid'`)
* After success → clear cart

### 📦 Orders

* Display list of user’s past orders via `/orders?user_id=eq.<uid>`
* Order details joined via `/order_items?order_id=eq.<id>`

### 👤 Profile

* Update profile via `/profiles?id=eq.<uid>`
* Avatar upload via Supabase Storage (`user-avatars`)

### 🗺️ Store Locator

* Fetch stores from `/stores`
* Show markers on Google Map with address and name

---

## 8) Security Rules (RLS Policies)

* `profiles`: user can access only their own data
* `products`, `categories`, `brands`, `stores`, `discounts`: public read
* `cart_items`: CRUD where `user_id = auth.uid()`
* `orders`: CRUD where `user_id = auth.uid()`
* `order_items`: read only if `auth.uid()` owns parent order

---

## 9) Offline & Caching

* Room tables: `products`, `categories`, `cart_items`
* Repository pattern merges cached + remote
* Network unavailable → read from Room; resync when online

---

## 10) Optional Supabase RPC (Edge-safe checkout)

Define RPC function:

```sql
CREATE OR REPLACE FUNCTION rpc_checkout(
  _user_id uuid,
  _address jsonb
) RETURNS uuid LANGUAGE plpgsql AS $$
DECLARE
  new_order uuid;
BEGIN
  INSERT INTO orders (user_id, shipping_address, payment_type, payment_status, status)
  VALUES (_user_id, _address, 'cod', 'paid', 'paid')
  RETURNING order_id INTO new_order;

  INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total)
  SELECT new_order, ci.product_id, ci.quantity, p.unit_price, p.unit_price * ci.quantity
  FROM cart_items ci JOIN products p ON ci.product_id = p.product_id
  WHERE ci.user_id = _user_id;

  DELETE FROM cart_items WHERE user_id = _user_id;
  RETURN new_order;
END $$ SECURITY DEFINER;
```

Call from Retrofit endpoint `/rpc/rpc_checkout`.

---

## 11) Testing Plan

* **Unit tests:** Repository layer (mock Retrofit)
* **Integration tests:** Auth + Cart + Checkout end-to-end
* **UI tests:** Espresso for navigation and data binding
* **Manual QA checklist:**

    1. Register/login
    2. Browse → add to cart → checkout → see order
    3. Edit profile → update avatar → logout/login again
    4. Map shows stores with correct pins

---

## 12) Deliverables

* Complete Android Studio project (Java 17+)
* README.md with:

    * Environment setup (API keys, Google Maps key)
    * Supabase schema SQL
    * How to run app and mock payment
* `.env.local` (for keys)
* Room database schema + migration
* Example unit test (e.g., `CartRepositoryTest`)

---

## 13) Implementation Order

1. Setup Retrofit, Gson, Room, and Supabase Auth client
2. Build Auth module (`.claude/auth-ui`)
3. Implement Product browsing (`.claude/home-ui`, `.claude/category-ui`)
4. Add Cart module with Room caching
5. Implement Checkout flow (`.claude/cart-and-payment-ui`)
6. Add Orders, Profile, and Map modules
7. Final polish, error handling, and testing

---

✅ **Final Rule:**
Use clean modular Java MVVM code, never expose Supabase service role key, and ensure all CRUD operations pass RLS security.
Deliver a complete, functional Android app implementing all features above.

---