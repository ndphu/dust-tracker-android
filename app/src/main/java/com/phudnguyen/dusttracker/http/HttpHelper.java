package com.phudnguyen.dusttracker.http;

import com.phudnguyen.dusttracker.utils.GsonUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpHelper {
    private static final String TAG = HttpHelper.class.getSimpleName();
    public static AtomicReference<String> JWT = new AtomicReference<>();

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .addInterceptor(logging)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T post(String url, Object data, Class<T> responseClass) throws IOException {
        OkHttpClient client = getUnsafeOkHttpClient();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(GsonUtils.GSON.toJson(data), MediaType.get("application/json")));
        if (JWT.get() != null) {
            builder.addHeader("Authorization", JWT.get());
        }
        Response response = client.newCall(builder.build()).execute();
        String body = response.body().string();
        return GsonUtils.GSON.fromJson(body, responseClass);
    }

    public static <T> T get(String url, Class<T> responseClass) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().get().url(url);
        if (JWT.get() != null) {
            builder.addHeader("Authorization", JWT.get());
        }
        Response response = client.newCall(builder.build()).execute();
        if (response.body() == null) {
            return null;
        }
        String body = response.body().string();
        return GsonUtils.GSON.fromJson(body, responseClass);
    }


}
