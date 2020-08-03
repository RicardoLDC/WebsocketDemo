package com.m4399.websocketdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.WebView;

import org.apache.http.HttpHost;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Project Name: WebsocketDemo
 * File Name:    SettingProxy.java
 * ClassName:    SettingProxy
 *
 * Description: 代理工具类.
 *
 * @author LDC
 * @date 2020年08月03日 13:52
 *
 * Copyright (c) 2020年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class SettingProxy
{
    private static final String LOG_TAG = "ldc";

    /**
     * 设置代理
     *
     * @param webview
     * @param host
     * @param port
     * @param applicationClassName
     * @return
     */
    public static boolean setProxy(WebView webview, String host, int port,
                                   String applicationClassName)
    {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            return setProxyUpToHC(webview, host, port);
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        {
            return setProxyICS(webview, host, port);
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            return setProxyJB(webview, host, port);
        }
        else
        {
            return setProxyKKPlus(webview, host, port, applicationClassName);
        }
    }

    public static boolean revertBackProxy(WebView webview, String applicationClassName)
    {
        if (Build.VERSION.SDK_INT <= 13)
        {
            return true;
        }
        else if (Build.VERSION.SDK_INT <= 15)
        {
            return revertProxyICS(webview);
        }
        else if (Build.VERSION.SDK_INT <= 18)
        {
            return revertProxyJB(webview);
        }
        else
        {
            return revertProxyKKPlus(webview, applicationClassName);
        }
    }

    private static boolean setProxyUpToHC(WebView webview, String host, int port)
    {
        Log.d(LOG_TAG, "Setting proxy with <= 3.2 API.");

        HttpHost proxyServer = new HttpHost(host, port);

        // Getting network
        Class networkClass = null;

        Object network = null;

        try
        {
            networkClass = Class.forName("android.webkit.Network");

            if (networkClass == null)
            {
                Log.e(LOG_TAG, "failed to get class for android.webkit.Network");

                return false;
            }

            Method getInstanceMethod = networkClass.getMethod("getInstance", Context.class);

            if (getInstanceMethod == null)
            {
                Log.e(LOG_TAG, "failed to get getInstance method");
            }

            network = getInstanceMethod.invoke(networkClass, new Object[]{webview.getContext()});
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "error getting network: " + ex);

            return false;
        }

        if (network == null)
        {
            Log.e(LOG_TAG, "error getting network: network is null");

            return false;
        }

        Object requestQueue = null;

        try
        {
            Field requestQueueField = networkClass.getDeclaredField("mRequestQueue");

            requestQueue = getFieldValueSafely(requestQueueField, network);
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "error getting field value");

            return false;
        }

        if (requestQueue == null)
        {
            Log.e(LOG_TAG, "Request queue is null");

            return false;
        }
        Field proxyHostField = null;

        try
        {
            Class requestQueueClass = Class.forName("android.net.http.RequestQueue");

            proxyHostField = requestQueueClass.getDeclaredField("mProxyHost");
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "error getting proxy host field");

            return false;
        }

        boolean temp = proxyHostField.isAccessible();

        try
        {
            proxyHostField.setAccessible(true);

            proxyHostField.set(requestQueue, proxyServer);
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "error setting proxy host");
        }
        finally
        {
            proxyHostField.setAccessible(temp);
        }

        Log.d(LOG_TAG, "Setting proxy with <= 3.2 API successful!");

        return true;
    }

    private static boolean setProxyICS(WebView webview, String host, int port)
    {
        try
        {
            Log.d(LOG_TAG, "Setting proxy with 4.0 API.");

            Class jWebCoreJavaBridgeClass = Class.forName("android.webkit.JWebCoreJavaBridge");
            Class params[] = new Class[1];
            params[0] = Class.forName("android.net.ProxyProperties");
            Method updateProxyInstance = jWebCoreJavaBridgeClass.getDeclaredMethod("updateProxy", params);

            Class wv = Class.forName("android.webkit.WebView");
            Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
            Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField, webview);

            Class wvc = Class.forName("android.webkit.WebViewCore");
            Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
            Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldInstance);

            Class bf = Class.forName("android.webkit.BrowserFrame");
            Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
            Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class proxyPropertiesParams[] = new Class[3];
            proxyPropertiesParams[0] = String.class;
            proxyPropertiesParams[1] = int.class;
            proxyPropertiesParams[2] = String.class;
            Constructor proxyPropertiesClassConstructor = proxyPropertiesClass.getConstructor(proxyPropertiesParams);

            updateProxyInstance.invoke(sJavaBridge, proxyPropertiesClassConstructor.newInstance(host, port, null));

            Log.d(LOG_TAG, "Setting proxy with 4.0 API successful!");
            return true;
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "failed to set HTTP proxy: " + ex);
            return false;
        }
    }

    private static boolean revertProxyICS(WebView webview)
    {
        try
        {
            Log.d(LOG_TAG, "Setting proxy with 4.0 API.");

            Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
            Class params[] = new Class[1];
            params[0] = Class.forName("android.net.ProxyProperties");
            Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

            Class wv = Class.forName("android.webkit.WebView");
            Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
            Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField, webview);

            Class wvc = Class.forName("android.webkit.WebViewCore");
            Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
            Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldInstance);

            Class bf = Class.forName("android.webkit.BrowserFrame");
            Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
            Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

            Class ppclass = Class.forName("android.net.ProxyProperties");
            Class pparams[] = new Class[3];
            pparams[0] = String.class;
            pparams[1] = int.class;
            pparams[2] = String.class;
            Constructor ppcont = ppclass.getConstructor(pparams);

            Object o = null;
            updateProxyInstance.invoke(sJavaBridge, o);

            Log.d(LOG_TAG, "Setting proxy with 4.0 API successful!");
            return true;
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "failed to set HTTP proxy: " + ex);
            return false;
        }
    }

    private static boolean setProxyJB(WebView webview, String host, int port)
    {
        Log.d(LOG_TAG, "Setting proxy with 4.1 - 4.3 API.");

        try
        {
            Class wvcClass = Class.forName("android.webkit.WebViewClassic");
            Class wvParams[] = new Class[1];
            wvParams[0] = Class.forName("android.webkit.WebView");
            Method fromWebView = wvcClass.getDeclaredMethod("fromWebView", wvParams);
            Object webViewClassic = fromWebView.invoke(null, webview);

            Class wv = Class.forName("android.webkit.WebViewClassic");
            Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
            Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField,
                                                                   webViewClassic);

            Class wvc = Class.forName("android.webkit.WebViewCore");
            Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
            Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField,
                                                       mWebViewCoreFieldInstance);

            Class bf = Class.forName("android.webkit.BrowserFrame");
            Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
            Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

            Class ppclass = Class.forName("android.net.ProxyProperties");
            Class pparams[] = new Class[3];
            pparams[0] = String.class;
            pparams[1] = int.class;
            pparams[2] = String.class;
            Constructor ppcont = ppclass.getConstructor(pparams);

            Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
            Class params[] = new Class[1];
            params[0] = Class.forName("android.net.ProxyProperties");
            Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

            updateProxyInstance.invoke(sJavaBridge, ppcont.newInstance(host, port, null));
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "Setting proxy with >= 4.1 API failed with error: " + ex.getMessage());
            return false;
        }

        Log.d(LOG_TAG, "Setting proxy with 4.1 - 4.3 API successful!");
        return true;
    }

    private static boolean revertProxyJB(WebView webview)
    {
        Log.d(LOG_TAG, "revert proxy with 4.1 - 4.3 API.");

        try
        {
            Class wvcClass = Class.forName("android.webkit.WebViewClassic");
            Class wvParams[] = new Class[1];
            wvParams[0] = Class.forName("android.webkit.WebView");
            Method fromWebView = wvcClass.getDeclaredMethod("fromWebView", wvParams);
            Object webViewClassic = fromWebView.invoke(null, webview);

            Class wv = Class.forName("android.webkit.WebViewClassic");
            Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
            Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField,
                                                                   webViewClassic);

            Class wvc = Class.forName("android.webkit.WebViewCore");
            Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
            Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField,
                                                       mWebViewCoreFieldInstance);

            Class bf = Class.forName("android.webkit.BrowserFrame");
            Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
            Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

            Class ppclass = Class.forName("android.net.ProxyProperties");
            Class pparams[] = new Class[3];
            pparams[0] = String.class;
            pparams[1] = int.class;
            pparams[2] = String.class;
            Constructor ppcont = ppclass.getConstructor(pparams);

            Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
            Class params[] = new Class[1];
            params[0] = Class.forName("android.net.ProxyProperties");
            Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

            Object o = null;
            updateProxyInstance.invoke(sJavaBridge, o);
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "Setting proxy with >= 4.1 API failed with error: " + ex.getMessage());
            return false;
        }

        Log.d(LOG_TAG, "revert proxy with 4.1 - 4.3 API successful!");
        return true;
    }

    @SuppressLint("NewApi")
    private static boolean setProxyKKPlus(WebView webview, String host, int port, String applicationClassName)
    {
        Log.d(LOG_TAG, "Setting proxy with >= 4.4 API.");

        Context appContext = webview.getContext().getApplicationContext();
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");
        try
        {
            Class applictionCls = Class.forName(applicationClassName);
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values())
            {
                for (Object rec : ((ArrayMap) receiverMap).keySet())
                {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener"))
                    {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        onReceiveMethod.invoke(rec, appContext, intent);
                    }
                }
            }

            Log.d(LOG_TAG, "Setting proxy with >= 4.4 API successful!");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (NoSuchFieldException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (IllegalAccessException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (IllegalArgumentException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (NoSuchMethodException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (InvocationTargetException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        return false;
    }

    @SuppressLint("NewApi")
    private static boolean revertProxyKKPlus(WebView webview, String applicationClassName)
    {
        Context appContext = webview.getContext().getApplicationContext();
        Properties properties = System.getProperties();

        properties.remove("http.proxyHost");
        properties.remove("http.proxyPort");
        properties.remove("https.proxyHost");
        properties.remove("https.proxyPort");
        try
        {
            Class applictionCls = Class.forName(applicationClassName);
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values())
            {
                for (Object rec : ((ArrayMap) receiverMap).keySet())
                {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener"))
                    {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        //                        intent.putExtra("proxy", null);
                        onReceiveMethod.invoke(rec, appContext, intent);
                    }
                }
            }
            Log.d(LOG_TAG, "Revert proxy with >= 4.4 API successful!");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (NoSuchFieldException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (IllegalAccessException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (IllegalArgumentException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (NoSuchMethodException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        catch (InvocationTargetException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        return false;
    }


    private static Object getFieldValueSafely(Field field, Object classInstance) throws IllegalArgumentException, IllegalAccessException
    {
        boolean oldAccessibleValue = field.isAccessible();

        field.setAccessible(true);

        Object result = field.get(classInstance);

        field.setAccessible(oldAccessibleValue);

        return result;
    }
}
