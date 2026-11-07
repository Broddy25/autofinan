package com.escaleras.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progress;
    private String appUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUrl = getString(R.string.app_url).trim();

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);

        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(progress, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 12));
        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setSupportZoom(false);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                // Boton "Reintentar" de la pantalla de error
                if ("app".equals(u.getScheme()) && "retry".equals(u.getHost())) {
                    view.loadUrl(appUrl);
                    return true;
                }
                String host = u.getHost();
                // Las paginas de Google (Apps Script) se quedan dentro de la app
                if (host != null && (host.endsWith("google.com") || host.endsWith("googleusercontent.com"))) {
                    return false;
                }
                // Todo lo demas (WhatsApp, telefono, correo, otras webs) se abre fuera de la app
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception ignorado) { }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    progress.setVisibility(View.GONE);
                    mostrarError(view);
                }
            }
        });

        if (appUrl.contains("PEGA_AQUI")) {
            mostrarMensaje(webView, "Falta configurar la direccion",
                    "Esta aplicacion se compilo sin la URL de Apps Script. Edita el archivo URL_APP.txt en GitHub y vuelve a compilar.", false);
        } else if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(appUrl);
        }
    }

    private void mostrarError(WebView view) {
        mostrarMensaje(view, "Sin conexion",
                "No se pudo abrir el Sistema de Autofinanciamiento. Revise su internet e intente de nuevo.", true);
    }

    private void mostrarMensaje(WebView view, String titulo, String texto, boolean reintentar) {
        String html = "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head>"
                + "<body style='font-family:sans-serif;background:#FDCC03;color:#00265f;text-align:center;padding:48px 24px'>"
                + "<h2>" + titulo + "</h2><p>" + texto + "</p>"
                + (reintentar
                    ? "<p><a href='app://retry' style='display:inline-block;padding:14px 32px;background:#00338a;"
                      + "color:#fff;border-radius:12px;text-decoration:none;font-weight:bold'>Reintentar</a></p>"
                    : "")
                + "</body></html>";
        view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
