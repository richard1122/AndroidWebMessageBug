package com.example.mywebviewport;

import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mywebviewport.databinding.FragmentFirstBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private WebMessagePort port;
    private WebView wv;

    private WebMessagePort portToReference;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        wv = binding.webviewport;

        wv.getSettings().setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);


        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                // These ports will be closed automatically when the webview gets disposed.
                WebMessagePort[] nativeToJSPorts = wv.createWebMessageChannel();

                BlazorWebMessageCallback nativeToJs = new BlazorWebMessageCallback(wv);

                WebMessagePort[] destPort = new WebMessagePort[]{nativeToJSPorts[1]};

                // Keep a reference to MessagePort, workaround for the webview bug.
                portToReference = nativeToJSPorts[0];
                nativeToJSPorts[0].setWebMessageCallback(nativeToJs);

                wv.postWebMessage(new WebMessage("capturePort", destPort), Uri.parse("https://0.0.0.0/"));

            }
        });

        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        try {
            is = getActivity().getAssets().open("blah.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8 ));
        String str = null;
        while (true) {
            try {
                if (!((str = br.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.append(str);
            sb.append("\r\n");
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String hhh = sb.toString();
        binding.webviewport.loadDataWithBaseURL("https://0.0.0.0/", hhh, "text/html", "utf8", "");

        //binding.webviewport.loadUrl("file:///android_asset/blah.html");
        return binding.getRoot();
    }


    private class BlazorWebMessageCallback extends WebMessagePort.WebMessageCallback {
        WebView _wv;

        public BlazorWebMessageCallback(WebView wv) {
            _wv = wv;
        }

        int count;

        @Override
        public void onMessage(WebMessagePort port, WebMessage message) {
            Log.println(Log.WARN, "eilon", message.getData());

            _wv.postWebMessage(new WebMessage("hey from Java: " + count++), Uri.parse("https://0.0.0.0/"));
        }
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
