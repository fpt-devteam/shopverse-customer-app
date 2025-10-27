
# Short Supabase Auth API (Android Java, Retrofit)

## 1) Constants

```java
public final class Supabase {
  public static final String BASE_URL = "{{baseUrl}}/";          // e.g. https://xyz.supabase.co
  public static final String ANON_KEY = "{{anon-key}}";          // Supabase anon public key
}
```
key should be put it in local.properties (like gg map api key)

## 2) Auth Interceptor (adds apikey always, Bearer if available)

```java
import okhttp3.*;

public class AuthInterceptor implements Interceptor {
  private volatile String accessToken; // nullable

  public void setAccessToken(String token) { this.accessToken = token; }

  @Override public Response intercept(Chain chain) throws java.io.IOException {
    Request original = chain.request();
    Request.Builder b = original.newBuilder()
        .header("apikey", Supabase.ANON_KEY)
        .header("Content-Type", "application/json");

    if (accessToken != null && !accessToken.isEmpty()) {
      b.header("Authorization", "Bearer " + accessToken);
    }
    return chain.proceed(b.build());
  }
}
```

## 3) Retrofit init

```java
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

public class ApiClient {
  public final AuthInterceptor authInterceptor = new AuthInterceptor();
  public final Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(Supabase.BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .client(new OkHttpClient.Builder().addInterceptor(authInterceptor).build())
      .build();

  public AuthService auth() { return retrofit.create(AuthService.class); }
  public DbService db() { return retrofit.create(DbService.class); }
}
```

## 4) DTOs

```java
// Auth
class LoginReq { public String email; public String password; }
class SignupReq { public String email; public String password; }
class EmailReq { public String email; }
class UpdatePasswordReq { public String password; }

// Tokens & user
class TokenRes { public String access_token; public String token_type; public Long expires_in; public String refresh_token; }
class SupabaseUser { public String id; public String email; /* add fields you need */ }

// Your public table "users" (PostgREST)
class AppUser { public String user_id; public String gmail; public String phone; /* … */ }
```

## 5) Services

```java
import retrofit2.Call;
import retrofit2.http.*;

// ---- Auth endpoints ----
interface AuthService {
  // LOGIN: /auth/v1/token?grant_type=password  (apikey only; NO bearer)
  @POST("auth/v1/token?grant_type=password")
  Call<TokenRes> login(@Body LoginReq body);

  // SIGNUP: /auth/v1/signup  (requires apikey + bearer? No; only apikey is fine before login. Works with just apikey.)
  @POST("auth/v1/signup")
  Call<SupabaseUser> signup(@Body SignupReq body);

  // RECOVER: /auth/v1/recover  (apikey only)
  @POST("auth/v1/recover")
  Call<Void> recover(@Body EmailReq body);

  // GET USER: /auth/v1/user  (apikey + bearer)
  @GET("auth/v1/user")
  Call<SupabaseUser> getUser();

  // UPDATE PASSWORD: /auth/v1/user  (apikey + bearer)
  @PUT("auth/v1/user")
  Call<SupabaseUser> updatePassword(@Body UpdatePasswordReq body);
}

// ---- Database (PostgREST) ----
interface DbService {
  // /rest/v1/users?select=*  (apikey + bearer)
  @GET("rest/v1/users")
  Call<java.util.List<AppUser>> listUsers(@Query("select") String select /* pass "*" */);
}
```

## 6) Usage flow (Java)

```java
ApiClient api = new ApiClient();

// 1) Sign up (optional if user already exists)
SignupReq su = new SignupReq(); su.email = "nhatthang270404@gmail.com"; su.password = "Thang2704!";
api.auth().signup(su).execute(); // handle 200/201 or error

// 2) Login -> save access token
LoginReq lr = new LoginReq(); lr.email = "nhatthang270404@gmail.com"; lr.password = "NewPassword123!";
TokenRes tok = api.auth().login(lr).execute().body();
api.authInterceptor.setAccessToken(tok.access_token);

// 3) Get current user (needs bearer)
SupabaseUser me = api.auth().getUser().execute().body();

// 4) Update password (needs bearer)
UpdatePasswordReq up = new UpdatePasswordReq(); up.password = "NewPassword123!";
api.auth().updatePassword(up).execute();

// 5) Query your table /rest/v1/users?select=*
java.util.List<AppUser> users = api.db().listUsers("*").execute().body();
```

## 7) cURL cheats (for quick testing)

* **Login (no bearer):**

```bash
curl -X POST "{{baseUrl}}/auth/v1/token?grant_type=password" \
  -H "apikey: {{anon-key}}" -H "Content-Type: application/json" \
  -d '{"email":"nhatthang270404@gmail.com","password":"NewPassword123!"}'
```

* **Get user (needs bearer):**

```bash
curl "{{baseUrl}}/auth/v1/user" \
  -H "apikey: {{anon-key}}" \
  -H "Authorization: Bearer {{access_token}}"
```

* **/rest/v1/users?select=*** (needs bearer):

```bash
curl "{{baseUrl}}/rest/v1/users?select=*" \
  -H "apikey: {{anon-key}}" \
  -H "Authorization: Bearer {{access_token}}"
```

---