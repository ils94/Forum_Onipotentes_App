package com.droidev.forumonipotentes;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationView;

import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private final String HOME_URL = "https://forum.onipotentes.club/forums/2/";
    private Menu menu;
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler exitHandler = new Handler();
    private final String userAgent = System.getProperty("http.agent");

    private ValueCallback<Uri[]> filePathCallback;
    private final static int FILE_CHOOSER_REQUEST_CODE = 1;

    private SharedPreferences sharedPreferences;
    private NavigationView navigationView;
    private String currentPageTitle = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);

        navigationView = findViewById(R.id.navigation_view);

        loadFavorites();

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
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(ProgressBar.GONE);
                currentPageTitle = view.getTitle();  // Get the page title
                updateNavigationButtons();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILE_CHOOSER_REQUEST_CODE);

                return true;
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimeType);
            String cookies = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookies);
            request.setDescription("Downloading file...");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Downloading File...", Toast.LENGTH_LONG).show();

        });

        progressBar.setVisibility(ProgressBar.VISIBLE);
        webView.loadUrl(HOME_URL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri[] results = null;

            if (data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            }

            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        } else {
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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

        if (id == R.id.action_favorite) {
            String currentUrl = webView.getUrl();
            String currentTitle = currentPageTitle;
            saveFavorite(currentTitle, currentUrl);
            Toast.makeText(this, "Page saved as favorite!", Toast.LENGTH_SHORT).show();
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

    private void saveFavorite(String title, String url) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(title, url);
        editor.apply();
        loadFavorites();
    }

    private void loadFavorites() {
        Menu menu = navigationView.getMenu();
        menu.clear();
        Map<String, ?> allFavorites = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allFavorites.entrySet()) {
            String title = entry.getKey();
            String url = entry.getValue().toString();

            menu.add(title).setOnMenuItemClickListener(menuItem -> {
                webView.loadUrl(url);
                return true;
            });
        }
    }
}
