package com.example.udacity.surfconnect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int APP_REQUEST_CODE = 1;

    Button phone_login_button, email_login_button;
    HashMap<String, String> params;
    ConstraintLayout constraintLayout;
    RequestQueue queue;
    AppEventsLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FontHelper.setCustomTypeface(findViewById(R.id.view_root));

        constraintLayout = (ConstraintLayout) findViewById(R.id.view_root);

        phone_login_button = (Button) findViewById(R.id.phone_login_button);
        email_login_button = (Button) findViewById(R.id.email_login_button);

        // For add custom app events
        logger = AppEventsLogger.newLogger(this);

        //Check for existing access token
        AccessToken accessToken = AccountKit.getCurrentAccessToken();

        if (accessToken != null) {
            //if previously logged in then proceed to account page
            launchAccountActivity();
        }

//      You will notice that we don’t use an else statement here, this is because if the user has not logged in previously we can simply wait for them to press one of the login buttons to initiate the flow.
// For "Client Access Token" : disable appsecret option in fb dashboard and enable client access token and use AccountKitActivity.ResponseType.TOKEN
// For "Authorization Code" : enable it and use AccountKitActivity.ResponseType.CODE in configuration
//      Note that if you have not chosen to Enable Client Access Token Flow your app will receive an authorization code that you will send to your server and you will be responsible for ensuring that your server communicates the correct login status to your application.
    }

    public void onPhoneLogin(View view) {
        logger.logEvent("onSMSLogin");
        onLogin(LoginType.PHONE);
    }

    public void onEmailLogin(View view) {
        logger.logEvent("onEmailLogin");
        onLogin(LoginType.EMAIL);
    }

    public void onLogin(final LoginType loginType) {

        //create intent for the Accountkit Activity
        Intent intent = new Intent(this, AccountKitActivity.class);

        //configure login type and response type
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType, AccountKitActivity.ResponseType.TOKEN);
        // For auth code flow
       /* AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType, AccountKitActivity.ResponseType.CODE);*/

        AccountKitConfiguration accountKitConfiguration = configurationBuilder.build();

        // launch the Account Kit Activity
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, accountKitConfiguration);
        startActivityForResult(intent, APP_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            /*below is the response for accesstoken(loginResult) :-
            accessToken = {AccessToken@830035195792} "{AccessToken token:ACCESS_TOKEN_REMOVED accountId:246780055793977}"
            accountId = "246780055793977"
            applicationId = "1289698624418928"
            lastRefresh = {Date@830035113880} "Fri Apr 21 17:37:58 IST 2017"
            token = "EMAWfuCfTDZB7zB4eIC83ijQOCHrNtboQ2xq55PYD1c3jWxzA6CZBYB53DYHoajprYTagAGf3d0GcjArOAf7bgPFf2Rs6U0PoPWKl3DU6QZDZD"
            tokenRefreshIntervalInSeconds = 2592000*/

            String toastMsg;
            if (loginResult.getError() != null) {
                // display login error
                toastMsg = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
            } else if (loginResult.getAccessToken() != null) {
                // on successful login, proceed to accout page
                launchAccountActivity();
            }/* else if (loginResult.wasCancelled()) {
                toastMsg = "Login Cancelled";
            } else {
                String authCode = loginResult.getAuthorizationCode();

                // pass authCode to server using AsyncTask or Volley
                *//*new AuthCodeAsync(authCode).execute();
                params = new HashMap<>();
                params.put("code", authCode);
                String url = "https://graph.accountkit.com/v1.1/access_token?grant_type=authorization_code&code=";

                if (isInternetAvailable(LoginActivity.this)) {
                    makeVolleyCall(url, params);
                } else {
                    showSnackBar(constraintLayout, "No Internet Connection", LoginActivity.this);
                }*//*
            }*/
        }
    }

    private void launchAccountActivity() {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isInternetAvailable(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected() && netInfo.isAvailable())
            return true;

        return false;
    }

    public void showSnackBar(View v, String message, Activity context) {
        Snackbar snackbar = Snackbar.make(v, message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextSize(22);

        snackbar.show();
    }

    public void makeVolleyCall(String url, final HashMap<String, String> params) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj = new JSONObject(response);
                            String accessToken = jObj.getString("access_token");
                            Log.v("response", jObj.toString());
                            // call GET https://graph.accountkint.com/v1.1/me/?access_token=<access_token>
                            // and then getresponse
                            // call launchAccountActivity(); with data intent passing which user gets in response
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // right now getting server error in both async and volley as it came from fb server side (ResponseCode = 500)
                        Log.e("error", "error in login= " + error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        this.addToRequestQueue(stringRequest);
    }

    public RequestQueue getRequestQueue() {
        if (queue == null) {
            queue = Volley.newRequestQueue(getApplicationContext());
        }
        return queue;
    }

    public <T> void addToRequestQueue(com.android.volley.Request<T> req) {
        req.setTag(LoginActivity.class.getSimpleName());
        getRequestQueue().add(req);
    }

    public class AuthCodeAsync extends AsyncTask<String, Void, String> {

        String AuthCode;

        public AuthCodeAsync(String authcode) {
            AuthCode = authcode;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String URL = "https://graph.accountkit.com/v1.1/access_token?grant_type=authorization_code";

                LinkedHashMap<String, String> paramss = new LinkedHashMap<>();
                paramss.put("code", AuthCode);

                return postWebServiceCall(URL, paramss);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONObject jObj = new JSONObject(response);
                String accessToken = jObj.getString("access_token");
                Log.v("response", jObj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String postWebServiceCall(String URL, LinkedHashMap<String, String> params) {

        String response = "";

        try {

            java.net.URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(60000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(params));
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line = br.readLine()) != null) {
                    response += line;
                }

            } else {
                response = null;
            }

        } catch (Exception e) {
            response = null;
        }

        return response;
    }

    public static String getPostDataString(LinkedHashMap<String, String> params) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();

        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
