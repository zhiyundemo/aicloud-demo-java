package com.youdao.aicloud.translate.utils;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class HttpUtil {

    private static OkHttpClient httpClient = new OkHttpClient.Builder().build();

    public static byte[] doGet(String url, Map<String, String[]> header, Map<String, String[]> params, String expectContentType) {
        Request.Builder builder = new Request.Builder();
        addHeader(builder, header);
        addUrlParam(builder, url, params);
        return requestExec(builder.build(), expectContentType);
    }

    public static byte[] doPost(String url, Map<String, String[]> header, Map<String, String[]> body, String expectContentType) {
        Request.Builder builder = new Request.Builder().url(url);
        addHeader(builder, header);
        addBodyParam(builder, body, "POST");
        return requestExec(builder.build(), expectContentType);
    }

    public static byte[] doPost(String url, Map<String, String[]> header, String params, String expectContentType) {
        Request.Builder builder = new Request.Builder().url(url);
        addHeader(builder, header);
        addBodyParam(builder, params, "POST");
        return requestExec(builder.build(), expectContentType);
    }

    private static void addHeader(Request.Builder builder, Map<String, String[]> header) {
        if (header == null) {
            return;
        }
        for (String key : header.keySet()) {
            String[] values = header.get(key);
            if (values != null) {
                for (String value : values) {
                    builder.addHeader(key, value);
                }
            }
        }
    }

    private static void addUrlParam(Request.Builder builder, String url, Map<String, String[]> params) {
        if (params == null) {
            return;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            if (values != null) {
                for (String value : values) {
                    urlBuilder.addQueryParameter(key, value);
                }
            }
        }
        builder.url(urlBuilder.build());
    }

    private static void addBodyParam(Request.Builder builder, Map<String, String[]> body, String method) {
        if (body == null) {
            return;
        }
        FormBody.Builder formBodyBuilder = new FormBody.Builder(StandardCharsets.UTF_8);
        for (String key : body.keySet()) {
            String[] values = body.get(key);
            if (values != null) {
                for (String value : values) {
                    formBodyBuilder.add(key, value);
                }
            }
        }
        builder.method(method, formBodyBuilder.build());
    }

    private static void addBodyParam(Request.Builder builder, String params, String method) {
        if (method == null) {
            return;
        }
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, params);
        builder.method(method, requestBody);
    }

    private static byte[] requestExec(Request request, String expectContentType) {
        Objects.requireNonNull(request, "okHttp request is null");

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 200) {
                ResponseBody body = response.body();
                if (body != null) {
                    String contentType = response.header("Content-Type");
                    if (contentType != null && !contentType.contains(expectContentType)) {
                        String res = new String(body.bytes(), StandardCharsets.UTF_8);
                        System.out.println(res);
                        return null;
                    }
                    return body.bytes();
                }
                System.out.println("response body is null");
            } else {
                System.out.println("request failed, http code: " + response.code());
            }
        } catch (IOException ioException) {
            System.out.println("request exec error: " + ioException.getMessage());
        }
        return null;
    }

    public static byte[] doPost(String url, Map<String, String[]> header, Map<String, String[]> body, String parameterName, File file, String expectContentType) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        addFile(builder, parameterName, file);
        addBodyParam(builder, body);
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        return requestExec(request, expectContentType);
    }

    private static void addFile(MultipartBody.Builder builder, String parameterName, File file) {
        builder.addFormDataPart(parameterName, file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
    }

    private static void addBodyParam(MultipartBody.Builder builder, Map<String, String[]> body) {
        if (body == null) {
            return;
        }
        for (String key : body.keySet()) {
            String[] values = body.get(key);
            if (values != null) {
                for (String value : values) {
                    builder.addFormDataPart(key, value);
                }
            }
        }
    }
}
