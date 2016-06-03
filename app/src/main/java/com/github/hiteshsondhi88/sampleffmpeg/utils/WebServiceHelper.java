package com.github.hiteshsondhi88.sampleffmpeg.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class WebServiceHelper {

    private static ProgressDialog progressDialog;
    private static final String COOKIES_HEADER = "Set-Cookie";
    public static final String COOKIE = "Cookie";

    public static HttpURLConnection openConnectionForUrl(String targetUrl, String method)
            throws IOException {
        CookieManager cookieManager = new CookieManager();
        URL url = new URL(targetUrl);
        System.out.println(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestMethod(method);
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
        if (cookiesHeader != null) {
            Log.i("Login / Signup", "cooker header not null");
            for (String cookie : cookiesHeader) {
                Log.i("Login / SignUp", "adding cookie" + cookie);
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
            if (cookieManager.getCookieStore().getCookies().size() > 0) {
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
//                connection.setRequestProperty(WebServiceHelper.COOKIE ,
//                        TextUtils.join(";", cookieManager.getCookieStore().getCookies()));
                Helpers.saveDataToSharedPreferences(AppGlobals.FULL_TOKEN,
                        TextUtils.join(";", cookieManager.getCookieStore().getCookies()));
                Log.i("Header"," test " + TextUtils.join(";", cookieManager.getCookieStore().getCookies()));
            }
        }
        return connection;
    }

    // Check if network is available
    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                AppGlobals.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // ping the google server to check if internet is really working or not
    public static boolean isInternetWorking() {
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static String getRegistrationString(
            String firstName, String lastName, String email, String password) {

        return AppGlobals.SIGNUP_URL +
                String.format("firstname=\"%s\"&", firstName) +
                String.format("lastname=\"%s\"&", lastName) +
                String.format("email=%s&", email) +
                String.format("password=%s", password);
    }

    public static String getLoginString(
            String email, String password) {

        return AppGlobals.LOGIN_URL +
                String.format("login_email=%s&", email) +
                String.format("login_password=%s", password);
    }

    public static String getFbLoginString(
            String token) {

        return AppGlobals.FB_LOGIN_URL +
                String.format("fb_access_token=%s", token);
    }

    public static void writeDataToStream(HttpURLConnection connection, String data) {

        try {
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(data);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readResponseData(HttpURLConnection connection) {
        int ch;
        StringBuilder sb = new StringBuilder();

        try {
            InputStream in = (InputStream) connection.getContent();
            while ((ch = in.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void showProgressDialog(Activity context, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static String convertInputStreamToString(InputStream is) throws IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader r1 = new BufferedReader(new InputStreamReader(
                        is, "UTF-8"));
                while ((line = r1.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}
