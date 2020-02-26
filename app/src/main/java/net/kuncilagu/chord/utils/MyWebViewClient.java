package net.kuncilagu.chord.utils;

import android.app.ProgressDialog;
import android.net.Uri;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MyWebViewClient extends WebViewClient {

    private ProgressDialog progressBar;

    public MyWebViewClient(ProgressDialog progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (Uri.parse(url).getHost().endsWith("example.com")) {
            return false;
        }

        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);
        if (progressBar.isShowing()) {
            progressBar.dismiss();
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        // Do something
        Toast.makeText(view.getContext(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        Toast.makeText(view.getContext(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Toast.makeText(view.getContext(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
    }
}
