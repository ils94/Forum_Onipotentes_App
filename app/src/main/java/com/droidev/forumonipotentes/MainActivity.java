package com.droidev.forumonipotentes;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

        setTitle("");

        sharedPreferences = getSharedPreferences("Favorites", MODE_PRIVATE);

        navigationView = findViewById(R.id.navigation_view);

        loadFavorites();

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
                currentPageTitle = view.getTitle();
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
            request.setDescription("Baixando arquivo...");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Baixando arquivo...", Toast.LENGTH_LONG).show();

        });

        webView.loadUrl(HOME_URL);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(HOME_URL);
        }
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

        if (id == R.id.clear_favorites) {
            showClearFavoritesDialog();
            return true;
        }

        if (id == R.id.action_home) {
            webView.loadUrl(HOME_URL);
            return true;
        }

        if (id == R.id.action_refresh) {
            webView.reload();
            return true;
        }

        if (id == R.id.action_back) {
            if (webView.canGoBack()) {
                webView.goBack();
            }
            return true;
        }

        if (id == R.id.action_forward) {
            if (webView.canGoForward()) {
                webView.goForward();
            }
            return true;
        }

        if (id == R.id.action_favorite) {
            String currentTitle = currentPageTitle;
            String currentUrl = webView.getUrl();
            showSaveFavoritesDialog(currentTitle, currentUrl);
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
            Toast.makeText(this, "Pressione voltar novamente para sair.", Toast.LENGTH_SHORT).show();

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
        Toast.makeText(this, "Página salva como favorito!", Toast.LENGTH_SHORT).show();
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

    private void clearFavorites() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        loadFavorites();
    }

    private void showClearFavoritesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deletar Favoritos");
        builder.setCancelable(false);
        builder.setMessage("Tem certeza que deseja deletar todos os favoritos?");

        builder.setPositiveButton("Sim", (dialog, which) -> {
            clearFavorites();
            Toast.makeText(MainActivity.this, "Todos os favoritos foram deletados!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSaveFavoritesDialog(String currentTitle, String currentUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salvar Favorito");
        builder.setMessage("Salvar a página abaixo como favorito?\n\n" + currentTitle);
        builder.setPositiveButton("Sim", (dialog, which) -> saveFavorite(currentTitle, currentUrl));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

}
