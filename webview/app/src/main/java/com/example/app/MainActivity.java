package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import wailsdroid.AppPortal;
import wailsdroid.WebviewClientPortal;


public class MainActivity extends Activity implements AppPortal {

    private WebView mWebView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView = findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // let wails invoke us
        wailsdroid.Wailsdroid.offerAppPortal(this);
        // start main()
        notmain.Notmain.boot();
        // wait for this.Run() to be called
    }

    @Override
    public void browserOpenURL(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public void execJS(String s) {
        // how? navigate to javascript://?
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:" + s);
            }
        });

    }

    @Override
    public void hide() {

    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    public void quit() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finishAffinity();
            }
        });
    }

    @Override
    public void run(String startURL) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(startURL);
            }
        });
    }

    private static class JSObject {
        private final WebviewClientPortal webviewClientPortal;

        public JSObject(WebviewClientPortal webviewClientPortal) {
            this.webviewClientPortal = webviewClientPortal;
        }

        @JavascriptInterface
        public void postMessage(String message) {
            Log.i("Wailsdroid", "Received message: " + message);
            this.webviewClientPortal.receiveMessage(message);
        }
    }

    @Override
    public void setWebViewClientPortal(WebviewClientPortal webviewClientPortal) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.setWebViewClient(new PortalWebViewClient(webviewClientPortal));
                mWebView.addJavascriptInterface(new JSObject(webviewClientPortal), "wailsdroid");
            }
        });
    }

    @Override
    public void show() {

    }

    @Override
    public void startDrag() {

    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
