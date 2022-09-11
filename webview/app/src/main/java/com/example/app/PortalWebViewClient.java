package com.example.app;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class PortalWebViewClient extends WebViewClient {

    private final wailsdroid.WebviewClientPortal portal;

    private static class PortalInputStream extends java.io.InputStream {
        private final wailsdroid.InputStream wrapped;

        public PortalInputStream(wailsdroid.InputStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int read() throws IOException {
            try {
                return wrapped.read();
            } catch (Throwable t) {
                throw new IOException("Error during PortalInputStream.read: " + t.getMessage(), t);
            }
        }
    }

    public PortalWebViewClient(wailsdroid.WebviewClientPortal portal) {
        this.portal = portal;
    }

    private static Map<String, String> castMapStreamFromGo(wailsdroid.MapStream mapStream) {
        Map<String, String> headers = new HashMap<>();

        while (mapStream.hasValue()) {
            headers.put(mapStream.key(), mapStream.value());
            mapStream.next();
        }

        return headers;
    }

    private static wailsdroid.Request castRequestToGo(WebResourceRequest request) {
        wailsdroid.Request r = new wailsdroid.Request();
        r.setMethod(request.getMethod());
        r.setURL(request.getUrl().toString());
        Log.i("Wailsdroid", "Request: " + request.getMethod() + " " + request.getUrl());
        return r;
    }

    private static WebResourceResponse castResponseFromGo(wailsdroid.Response response) {
        java.io.InputStream inputStream = null;
        wailsdroid.InputStream respInputStream = response.getData();
        if (respInputStream != null) {
            inputStream = new PortalInputStream(respInputStream);
        }


        Log.i("Wailsdroid", "Response: HTTP " + response.getStatusCode() + " " + response.getReasonPhrase() + ", " + response.getMimeType() + " " + response.getEncoding());

        return new WebResourceResponse(
            response.getHasMimeType() ? response.getMimeType() : null,
            response.getHasEncoding() ? response.getEncoding() : null,
            response.getStatusCode(),
            response.getReasonPhrase(),
            castMapStreamFromGo(response.getHeaders()),
            inputStream
        );
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return portal.shouldOverrideUrlLoading(
            castRequestToGo(request)
        );
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        wailsdroid.Response resp = portal.shouldInterceptRequest(
            castRequestToGo(request)
        );

        return resp == null
            ? null
            : castResponseFromGo(resp);
    }
}
