package com.droidev.forumonipotentes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private final String HOME_URL = "https://forum.onipotentes.club/forums/2/";
    private Menu menu;
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler exitHandler = new Handler();
    String userAgent = System.getProperty("http.agent");

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("");

        Drawable scaledDrawable = redimensionarIcone(R.drawable.pepe);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(scaledDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUserAgentString(userAgent);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.startsWith("https://forum.onipotentes.club/")) {
                    return false;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://forum.onipotentes.club/")) {
                    return false;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(ProgressBar.GONE);

                updateNavigationButtons();
            }
        });

        progressBar.setVisibility(ProgressBar.VISIBLE);

        webView.loadUrl(HOME_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        updateNavigationButtons();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            webView.loadUrl(HOME_URL);
            return true;
        }

        if (id == R.id.action_refresh) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            webView.reload();
            return true;
        }

        if (id == R.id.action_back) {
            if (webView.canGoBack()) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                webView.goBack();
            }
            return true;
        }

        if (id == R.id.action_forward) {
            if (webView.canGoForward()) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                webView.goForward();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateNavigationButtons() {
        if (menu == null) return;

        MenuItem backButton = menu.findItem(R.id.action_back);
        MenuItem forwardButton = menu.findItem(R.id.action_forward);

        backButton.setEnabled(webView.canGoBack());
        forwardButton.setEnabled(webView.canGoForward());
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            exitHandler.postDelayed(() -> doubleBackToExitPressedOnce = false, 3000);
        }
    }

    private Drawable redimensionarIcone(int drawableRes) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, true);
        return new BitmapDrawable(getResources(), scaledBitmap);
    }
}
