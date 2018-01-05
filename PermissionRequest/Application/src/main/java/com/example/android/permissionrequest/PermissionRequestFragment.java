/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.permissionrequest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.example.android.common.logger.Log;

/**
 * This fragment shows a {@link WebView} and loads a web app from the {@link SimpleWebServer}.
 */
public class PermissionRequestFragment extends Fragment
        implements ConfirmationDialogFragment.Listener {

    private static final String TAG = PermissionRequestFragment.class.getSimpleName();

    private static final String FRAGMENT_DIALOG = "dialog";

    /**
     * We use this web server to serve HTML files in the assets folder. This is because we cannot
     * use the JavaScript method "getUserMedia" from "file:///android_assets/..." URLs.
     */
    private SimpleWebServer mWebServer;

    /**
     * A reference to the {@link WebView}.
     */
    private WebView mWebView;

    //允许的权限的集合, getResources就是需要的权限的集合，我们这里面用到的就是RESOURCE_VIDEO_CAPTURE = "android.webkit.resource.VIDEO_CAPTURE"
    /**
     * This field stores the {@link PermissionRequest} from the web application until it is allowed
     * or denied by user.
     */
    private PermissionRequest mPermissionRequest;

    /**
     * For testing.
     */
    private ConsoleMonitor mConsoleMonitor;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permission_request, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mWebView = (WebView) view.findViewById(R.id.web_view);
        // Here, we use #mWebChromeClient with implementation for handling PermissionRequests.
        mWebView.setWebChromeClient(mWebChromeClient); //绑定WebChromeClient
        configureWebSettings(mWebView.getSettings()); //设置支持js
    }

    @Override
    public void onResume() {
        super.onResume();
        final int port = 8080; //设置安卓http的端口号是8080
        mWebServer = new SimpleWebServer(port, getResources().getAssets()); //开启一个httpserver 接受GET请求内容
        mWebServer.start();
        //mWebView.loadUrl("https://www.baidu.com");
        mWebView.loadUrl("http://localhost:" + port + "/sample.html");
    }

    @Override
    public void onPause() {
        mWebServer.stop();
        super.onPause();
    }

    //如果访问的页面中有Javascript，则webview必须设置支持Javascript。
    @SuppressLint("SetJavaScriptEnabled")
    private static void configureWebSettings(WebSettings settings) {
        settings.setJavaScriptEnabled(true);
    }

    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    /**
     * This {@link WebChromeClient} has implementation for handling {@link PermissionRequest}.
     */
    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        // 当点击Start时，会触发onPermissionRequest，此时
        // This method is called when the web content is requesting permission to access some
        // resources.
        @Override
        public void onPermissionRequest(PermissionRequest request) {
            Log.i(TAG, "$$$onPermissionRequest"+   TextUtils.join("\n", request.getResources()));
            mPermissionRequest = request;
            ConfirmationDialogFragment.newInstance(request.getResources())
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            Log.i(TAG, "z&&&");
        }

        //当
        // This method is called when the permission request is canceled by the web content.
        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            Log.i(TAG, "onPermissionRequestCanceled###");
            // We dismiss the prompt UI here as the request is no longer valid.
            mPermissionRequest = null;
            DialogFragment fragment = (DialogFragment) getChildFragmentManager()
                    .findFragmentByTag(FRAGMENT_DIALOG);
            if (null != fragment) {
                fragment.dismiss();
            }
        }

        //针对不同的信息在控制台打印不同的语句
        @Override
        public boolean onConsoleMessage(@NonNull ConsoleMessage message) {
            switch (message.messageLevel()) {
                case TIP:
                    Log.v(TAG, message.message());
                    break;
                case LOG:
                    Log.i(TAG, message.message());
                    break;
                case WARNING:
                    Log.w(TAG, message.message());
                    break;
                case ERROR:
                    Log.e(TAG, message.message());
                    break;
                case DEBUG:
                    Log.d(TAG, message.message());
                    break;
            }
            if (null != mConsoleMonitor) {
                mConsoleMonitor.onConsoleMessage(message);
            }
            return true;
        }

    };

    //根据allowed为true或者为false来给mPermissionRequest申请
    //如果是true就可以grant该权限
    @Override
    public void onConfirmation(boolean allowed) {
        System.out.println("!!!!!!!!!!"+allowed+"!!!!!!"+ TextUtils.join("\n", mPermissionRequest.getResources()));
        if (allowed) {
            mPermissionRequest.grant(mPermissionRequest.getResources());
            Log.d(TAG, "Permission granted.");
        } else {
            mPermissionRequest.deny();
            Log.d(TAG, "Permission request denied~~~.");
        }
        mPermissionRequest = null;
    }

    public void setConsoleMonitor(ConsoleMonitor monitor) {
        mConsoleMonitor = monitor;
    }

    /**
     * For testing.
     */
    public interface ConsoleMonitor {
        public void onConsoleMessage(ConsoleMessage message);
    }

}
