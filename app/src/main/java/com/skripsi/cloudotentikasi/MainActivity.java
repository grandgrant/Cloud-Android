package com.skripsi.cloudotentikasi;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Output;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


import okhttp3.OkHttpClient;

import static com.skripsi.cloudotentikasi.R.id.button;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    String email;
    String password;
    String imei;
    String dataemail;
    String datapassword;
    String dataimei;
    String kodeperangkatilegal;
    String refreshtoken;
    String clientId = "995506483560-bvi5ili4pcnfukuaf2h5o2qsua5tlhf0.apps.googleusercontent.com";
    String newaccesstoken;
    boolean hasilotentikasipengguna;
    boolean hasilotentikasiperangkat = false;
    String[] projectNumber;
    String[] projectId;
    String[] lifecycleState;
    String[] projectName;
    String[] createTime;

    EditText textEmail;
    EditText textPassword;
    TextView hasil;
    String hasilgetpengguna;
    String hasilgetperangkat;
    String hasilgetperangkatilegal;
    String scopeCloud = "https://www.googleapis.com/auth/cloud-platform";
    String scopeGPlus = "email";
    Button login;
    private int PHONE_STATE_PERMISSION_CODE = 23;
    private final String LOG_TAG  = "Output";
    private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
    private static final String AUTH_STATE = "AUTH_STATE";
    private static final String USED_INTENT = "USED_INTENT";
    AuthState state;
    View v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button) findViewById(button);
        textEmail = (EditText) findViewById(R.id.editEmail);
        textPassword = (EditText) findViewById(R.id.editPassword);
        password = textPassword.getText().toString();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = view;
                Oauth2Google(scopeGPlus,"email");
            }
        });

    }
    /**
     * Exchanges the code, for the {@link TokenResponse}.
     *
     * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }
    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.skripsi.cloudotentikasi.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent, intent.getStringExtra("SCOPE"));
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;
                default:
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        checkIntent(getIntent());
    }
    public void Oauth2Google(String scope, String tipe){
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
                Uri.parse("https://www.googleapis.com/oauth2/v4/token")
        );

        AuthorizationService authorizationService = new AuthorizationService(this);
        Uri redirectUri = Uri.parse("com.skripsi.cloudotentikasi:/oauth2callback");
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                clientId,
                AuthorizationRequest.RESPONSE_TYPE_CODE,
                redirectUri
        );
        builder.setScopes(scope);

        AuthorizationRequest request = builder.build();
        String action = "com.skripsi.cloudotentikasi.HANDLE_AUTHORIZATION_RESPONSE";
        Intent postAuthorizationIntent = new Intent(action);
        postAuthorizationIntent.putExtra("SCOPE",tipe);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, request.hashCode(), postAuthorizationIntent, 0);
        authorizationService.performAuthorizationRequest(request, pendingIntent);
    }
    private void handleAuthorizationResponse(@NonNull Intent intent, final String scope) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);
        if (response != null) {
            Log.i(LOG_TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
            AuthorizationService service = new AuthorizationService(this);
            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                if (exception != null) {
                    Log.w(LOG_TAG, "Token Exchange failed", exception);
                } else {
                    if (tokenResponse != null) {
                        authState.update(tokenResponse, exception);
                        persistAuthState(authState);
                        Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
                        if (scope.equals("email")) {
                            callAPIemail(tokenResponse.accessToken);
                        }
                        if(scope.equals("cloud")){
                            writefile(tokenResponse.accessToken,"access");
                            writefile(tokenResponse.refreshToken,"refresh");
                            Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, REFRESH Token: %s ]", tokenResponse.accessToken, tokenResponse.refreshToken));
                            callAPIproject(tokenResponse.accessToken,tokenResponse.refreshToken);
                        }
                    }
                }
                }
            });
        }
    }
    private class ExchangeRefreshToken extends AsyncTask<String,Void,String>{
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        byte[] postbyte = null;
        String datapost = null;
        URL url;
        String hasil;
        String urlstring = "https://www.googleapis.com/oauth2/v4/token";

        @Override
        protected String doInBackground(String... params) {
            try {
                datapost = URLEncoder.encode("client_id","UTF-8") + "=" + URLEncoder.encode("995506483560-bvi5ili4pcnfukuaf2h5o2qsua5tlhf0.apps.googleusercontent.com","UTF-8")
                + "&" + URLEncoder.encode("refresh_token","UTF-8") + "=" + URLEncoder.encode(refreshtoken,"UTF-8")
                + "&" + URLEncoder.encode("grant_type","UTF-8") + "=" + URLEncoder.encode("refresh_token","UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try
            {
                url = new URL(urlstring);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(datapost);
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    response.append(line + "\n");
                }
                hasil = response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return hasil;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }
    private void callAPIproject(String access_token, String refreshtoken) {
        Map map = new HashMap();
        map.put("refresh_token",refreshtoken);
        map.put("client_id", clientId);
        map.put("grant_type", "refresh_token");
        AuthorizationService authorizationService = new AuthorizationService(this);
        state = restoreAuthState();
        state.performActionWithFreshTokens(authorizationService, map ,new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String access_token, @Nullable String idToken, @Nullable AuthorizationException ex) {
                new AsyncTask<String,Void,JSONObject>(){
                    @Override
                    protected JSONObject doInBackground(String... tokens) {
                        OkHttpClient client = new OkHttpClient();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url("https://cloudresourcemanager.googleapis.com/v1/projects")
                                .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                .build();
                        try {
                            okhttp3.Response response = client.newCall(request).execute();
                            String jsonBody = response.body().string();
                            Log.i(LOG_TAG, String.format("Project Info Response %s", jsonBody));
                            return new JSONObject(jsonBody);
                        } catch (Exception exception) {
                            Toast.makeText(MainActivity.this,exception.toString(),Toast.LENGTH_LONG).show();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(JSONObject projectInfo){
                        if (projectInfo != null){
                            // extrak hasil json
                            try {
                                JSONArray jsonArray = new JSONArray(projectInfo.getString("projects"));
                                projectNumber =  new String[jsonArray.length()];
                                projectId = new String [jsonArray.length()];
                                lifecycleState = new String[jsonArray.length()];
                                projectName = new String[jsonArray.length()];
                                createTime = new String[jsonArray.length()];

                                for (int j=0;j<jsonArray.length();j++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                                    projectNumber[j] = jsonObject.getString("projectNumber");
                                    projectId[j] = jsonObject.getString("projectId");
                                    lifecycleState[j] = jsonObject.getString("lifecycleState");
                                    projectName[j] = jsonObject.getString("name");
                                    createTime[j] = jsonObject.getString("createTime");
                                }
                                Intent intent = new Intent(MainActivity.this,SistemCloud.class);
                                intent.putExtra("Number",projectNumber);
                                intent.putExtra("Id",projectId);
                                intent.putExtra("lifecycle",lifecycleState);
                                intent.putExtra("name", projectName);
                                intent.putExtra("time", createTime);
                                intent.putExtra("size", createTime.length);
                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }.execute(access_token);
            }
        });



    }
    private void callAPIemail(String access_token) {
        AuthorizationService authorizationService = new AuthorizationService(this);
        state = restoreAuthState();
        state.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String access_token, @Nullable String idToken, @Nullable AuthorizationException ex) {
                new AsyncTask<String,Void,JSONObject>(){
                    @Override
                    protected JSONObject doInBackground(String... tokens) {
                        OkHttpClient client = new OkHttpClient();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url("https://www.googleapis.com/oauth2/v1/userinfo")
                                .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                .build();
                        try {
                            okhttp3.Response response = client.newCall(request).execute();
                            String jsonBody = response.body().string();
                            Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
                            return new JSONObject(jsonBody);
                        } catch (Exception exception) {
                            Toast.makeText(MainActivity.this,exception.toString(),Toast.LENGTH_LONG).show();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(JSONObject userInfo){
                        if (userInfo != null){
                            try {
                                email = userInfo.getString("email");
                                Otentikasi(email);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }.execute(access_token);
            }
        });

    }
    private void persistAuthState(@NonNull AuthState authState) {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(AUTH_STATE, authState.toJsonString())
                .commit();
    }
    @Nullable
    private AuthState restoreAuthState() {
        String jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(AUTH_STATE, null);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return AuthState.fromJson(jsonString);
            } catch (JSONException jsonException) {
                // should never happen
            }
        }
        return null;
    }

    public String readfile(String type)
    {
        String respon = null;
        Context context = this;
        if (type.equals("access")) {
            try {
                FileInputStream fis = context.openFileInput("access.token");
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                respon = sb.toString();
            } catch (FileNotFoundException e) {
                return "";
            } catch (UnsupportedEncodingException e) {
                return "";
            } catch (IOException e) {
                return "";
            }
        }
        if (type.equals("refresh")) {
            try {
                FileInputStream fis = context.openFileInput("refresh.token");
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                respon = sb.toString();
            } catch (FileNotFoundException e) {
                return "";
            } catch (UnsupportedEncodingException e) {
                return "";
            } catch (IOException e) {
                return "";
            }
        }
        return respon;
    }
    public void writefile(String token,String type)
    {
        FileOutputStream outputStream = null;
        if (type.equals("access")){
            File file = new File("access.token");
            try{
                outputStream = openFileOutput("access.token",MODE_PRIVATE);
                outputStream.write(token.getBytes());
                outputStream.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (type.equals("refresh")){
            File file = new File("refresh.token");
            try{
                outputStream = openFileOutput("refresh.token",MODE_PRIVATE);
                outputStream.write(token.getBytes());
                outputStream.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean bacapermissionPhoneState(){
        int hasilpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (hasilpermission == PackageManager.PERMISSION_GRANTED){
            return true;
        }else return false;
    }
    private void requestpermissionPhoneState(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_PHONE_STATE)){

        }
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},PHONE_STATE_PERMISSION_CODE);
    }
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == PHONE_STATE_PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission granted",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void Otentikasi(String emailhasil) {
        Log.i(LOG_TAG,String.format("email %s",emailhasil));
        if (emailhasil != null){
            if (bacapermissionPhoneState()) {
                imei = getImei();
            } else {
                requestpermissionPhoneState();
            }
            getDataPerangkat dataPerangkat = new getDataPerangkat();
            try {
                hasilgetperangkat = dataPerangkat.execute().get();
                hasilgetperangkat = hasilgetperangkat.substring(1,hasilgetperangkat.length()-5);
                if(hasilgetperangkat.equals("ok")){
                    hasilotentikasiperangkat = true;
                }else hasilotentikasiperangkat = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (hasilotentikasiperangkat)
            {
                if (readfile("access") != null && readfile("access") != "" && readfile("refresh") != null && readfile("refresh") != "") {
                    Log.i(LOG_TAG, String.format("REFRESH %s", readfile("refresh")));
                    Log.i(LOG_TAG, String.format("ACCESS %s", readfile("access")));
                    callAPIproject(readfile("access"),readfile("refresh"));
                } else {
                    Oauth2Google(scopeCloud,"cloud");
                }
            }else {
                KirimPerangkatIlegal kirimPerangkatIlegal = new KirimPerangkatIlegal();
                try {
                    hasilgetperangkatilegal = kirimPerangkatIlegal.execute().get();
                    JSONArray arrayilegal = new JSONArray(hasilgetperangkatilegal);
                    for (int l = 0; l < arrayilegal.length(); l++) {
                        JSONObject jsonObjectilegal = arrayilegal.getJSONObject(l);
                        kodeperangkatilegal = jsonObjectilegal.getString("kode_perangkat_baru");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SendEmail();
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                    }
                });
                thread.start();

                Toast.makeText(MainActivity.this, "Anda mengakses sistem dari perangkat baru. Silahkan cek email Anda.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(MainActivity.this,"Email atau password yang diisikan salah",Toast.LENGTH_LONG).show();
        }
    }
    public boolean getDataPengguna(View view){
        //Oauth2Google(view,scopeGPlus);
        Toast.makeText(this,email,Toast.LENGTH_LONG).show();
        if(email != null) return true;
        else return false;
        //permasalahan data email di toast muncul sebelum fungsi oauth2google selesai
    }

    public class getDataPerangkat extends AsyncTask<String,Void,String>{
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        byte[] postbyte = null;
        String datapost = null;
        URL url;
        String hasil;
        String urlstring = "http://aprihadiperdana.web.id/TA/getperangkat.php";

        @Override
        protected String doInBackground(String... params) {
            try {
                datapost = URLEncoder.encode("email","UTF-8") + "=" + URLEncoder.encode(email,"UTF-8")
                + "&" + URLEncoder.encode("jenis","UTF-8") + "=" + URLEncoder.encode("Smartphone","UTF-8")
                + "&" + URLEncoder.encode("imei","UTF-8") + "=" + URLEncoder.encode(imei,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try
            {
                url = new URL(urlstring);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(datapost);
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    response.append(line + "\n");
                }
                hasil = response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return hasil;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }

    public class KirimPerangkatIlegal extends AsyncTask<String,Void,String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        byte[] postbyte = null;
        String datapost = null;
        URL url;
        String hasil;
        String urlstring = "http://aprihadiperdana.web.id/TA/tambahperangkatilegal.php";

        @Override
        protected String doInBackground(String... params) {
            try {
                datapost = URLEncoder.encode("email","UTF-8") + "=" + URLEncoder.encode(email,"UTF-8")
                + "&" + URLEncoder.encode("imei","UTF-8") + "=" + URLEncoder.encode(imei,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try
            {
                url = new URL(urlstring);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(datapost);
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    response.append(line + "\n");
                }
                hasil = response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return hasil;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }

    public String getImei(){
        String hasilimei;
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        hasilimei = telephonyManager.getDeviceId();
        return hasilimei;
    }

    @Nullable
    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }

    public void SendEmail(){

        String linkperangkat = "http://aprihadiperdana.web.id/TA/tambahperangkat.php?png=" + md5(email) + "&" + "kde=" + md5(kodeperangkatilegal);
        String linkgantipassword = "http://aprihadiperdana.web.id/TA/ubahpassword.php?png=" + md5(email);
        final String username = "eb14854@gmail.com";
        final String password = "14854papa";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("eb14854@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("eril.ilkom@gmail.com"));
            message.setSubject("Deteksi login ilegal");
            message.setText("Akun ada diakses oleh perangkat yang tidak dikenal sistem. Jika Anda mengenali perangkat ini silahkan klik link "+ linkperangkat + ". Jika Anda tidak mengenali perangkat ini silahkan untuk mengganti password Anda menggunakan link " + linkgantipassword);
            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
