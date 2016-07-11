package com.guardanis.netclient;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.guardanis.netclient.NetInterface.FailListener;
import com.guardanis.netclient.NetInterface.Jsonable;
import com.guardanis.netclient.NetInterface.ResponseParser;
import com.guardanis.netclient.NetInterface.SuccessListener;
import com.guardanis.netclient.errors.ApiError;
import com.guardanis.netclient.errors.ErrorParser;
import com.guardanis.netclient.errors.GeneralError;
import com.guardanis.netclient.errors.RequestCanceledError;
import com.guardanis.netclient.errors.RequestError;
import com.guardanis.netclient.tools.InputStreamHelper;
import com.guardanis.netclient.tools.NetUtils;
import com.guardanis.netclient.tools.OutputStreamHelper;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

public class WebRequest<T> implements Runnable {

    public enum ConnectionType {
        GET, POST, PUT, DELETE;
    }

    private static final int CORE_POOL_SIZE = 15;
    private static final int MAXIMUM_POOL_SIZE = 64;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "NetTask #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> THREAD_POOL_QUEUE = new LinkedBlockingQueue<Runnable>(CORE_POOL_SIZE * 2);
    protected static final Executor EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, THREAD_POOL_QUEUE, sThreadFactory);

    protected Context context;
    protected ConnectionType connectionType;
    protected String targetUrl = "";

    protected Map<String, String> requestProperties = new HashMap<String, String>();

    protected String data;
    protected ResponseParser<T> responseParser;
    protected SuccessListener<T> successListener;
    protected FailListener failListener;

    protected Looper originatingLooper;

    protected boolean canceled = false;
    protected boolean failOnCancel = false;

    protected ErrorParser errorParser;

    protected int sslCertResource = R.raw.nc__cert;
    protected String sslCertPassword;
    protected boolean unsafeSslModeEnabled = false;

    public WebRequest(Context context, ConnectionType connectionType){
        this(context, connectionType, "");
    }

    public WebRequest(Context context, ConnectionType connectionType, String targetUrl){
        this.context = context;
        this.connectionType = connectionType;
        this.sslCertPassword = context.getString(R.string.nc__ssl_cert_keystore_password);

        setTargetUrl(targetUrl);
    }

    public WebRequest<T> setTargetUrl(String targetUrl){
        this.targetUrl = targetUrl.trim();
        return this;
    }

    /**
     * Set Jsonable. Throws RuntimeException if there's an issue with the Jsonable
     */
    public WebRequest<T> setData(Jsonable data){
        return setData(data.toJson());
    }

    public WebRequest<T> setData(JSONObject data){
        return setData(data.toString());
    }

    public WebRequest<T> setData(String data){
        this.data = data;
        return this;
    }

    public WebRequest<T> addRequestProperty(String key, String value){
        requestProperties.put(key, value);
        return this;
    }

    public WebRequest<T> setResponseParser(ResponseParser<T> responseParser){
        this.responseParser = responseParser;
        return this;
    }

    public WebRequest<T> onSuccess(SuccessListener<T> successListener){
        this.successListener = successListener;
        return this;
    }

    public WebRequest<T> onFail(FailListener failListener){
        this.failListener = failListener;
        return this;
    }

    public WebRequest<T> setFailOnCancel(boolean failOnCancel){
        this.failOnCancel = failOnCancel;
        return this;
    }

    public WebRequest<T> setErrorParser(ErrorParser errorParser){
        this.errorParser = errorParser;
        return this;
    }

    public WebRequest<T> setSslCertificateInfo(int sslCertResource, String sslCertPassword){
        this.sslCertResource = sslCertResource;
        this.sslCertPassword = sslCertPassword;
        return this;
    }

    /**
     * Force the SSLSocketFactory to accept all certificates. Do not enable this in production.
     */
    public WebRequest<T> setSslUnsafeModeEnabled(boolean unsafeSslModeEnabled){
        this.unsafeSslModeEnabled = unsafeSslModeEnabled;
        return this;
    }

    public WebRequest<T> execute(){
        this.originatingLooper = Looper.myLooper();

        EXECUTOR.execute(new Thread(this));

        return this;
    }

    @Override
    public void run(){
        try{
            HttpURLConnection connection = openConnection();

            if(connectionType == ConnectionType.POST)
                connection.setDoOutput(true);

            connection.setRequestMethod(connectionType.name());
            setRequestProperties(connection);

            WebResult result = makeRequest(connection, data);
            onResponseReceived(result);
        }
        catch(final Throwable e){
            postToOriginalThread(new Runnable() {
                public void run() {
                    if(failListener != null)
                        failListener.onFail(new GeneralError(context, e));
                }
            });

            e.printStackTrace();
        }
    }

    protected HttpURLConnection openConnection() throws Exception {
        URL url = buildUrl();

        if(targetUrl.startsWith("https://")){
            HttpURLConnection conn = (HttpsURLConnection) url.openConnection();

            CustomSSLSocketFactory factory = unsafeSslModeEnabled
                        ? CustomSSLSocketFactory.getUnsafeInstance()
                        : CustomSSLSocketFactory.getInstance(context, sslCertResource, sslCertPassword);

            ((HttpsURLConnection) conn).setSSLSocketFactory(factory.currentContext.getSocketFactory());

            return conn;
        }
        else return (HttpURLConnection) url.openConnection();
    }

    protected void setRequestProperties(HttpURLConnection conn){
        conn.setRequestProperty("Accept", NetUtils.getInstance(context).getAcceptProperty());
        conn.setRequestProperty("Content-type", NetUtils.getInstance(context).getContentTypeProperty());

        for(String key : requestProperties.keySet())
            conn.setRequestProperty(key, requestProperties.get(key));
    }

    protected URL buildUrl() throws MalformedURLException, UnsupportedEncodingException {
        return new URL(targetUrl);
    }

    protected WebResult makeRequest(HttpURLConnection conn, String params) throws Exception {
        OutputStreamHelper streamHelper = new OutputStreamHelper(conn);
        streamHelper.write(params);

        WebResult response = readResponse(conn);

        streamHelper.closeConnection();

        NetUtils.getInstance(context)
                .log("Server response: " + response.getUnparsedResponse());

        return response;
    }

    protected WebResult readResponse(HttpURLConnection conn) throws Exception {
        InputStreamHelper streamHelper = null;
        String response = "";

        try{
            streamHelper = new InputStreamHelper(conn);
            response = streamHelper.read();
        }
        catch(Exception e){ e.printStackTrace(); }
        finally{
            if(streamHelper != null)
                streamHelper.closeConnection();
        }

        return new WebResult(conn.getResponseCode(), response);
    }

    protected void onResponseReceived(WebResult result) throws Exception {
        final RequestError errors = getErrorsFromResult(result);

        if(errors != null && errors.hasErrors())
            failWith(errors);
        else finishWith(responseParser.parse(result));
    }

    protected RequestError getErrorsFromResult(WebResult result){
        return errorParser == null
                ? null
                : new ApiError(result, errorParser);
    }

    protected void failWith(final RequestError errors){
        postToOriginalThread(new Runnable() {
            public void run() {
                if(failListener != null)
                    failListener.onFail(errors);
            }
        });
    }

    protected void finishWith(final T parsedResult){
        postToOriginalThread(new Runnable() {
            public void run() {
                if(successListener != null)
                    successListener.onSuccess(parsedResult);
            }
        });
    }

    protected void postToOriginalThread(Runnable runnable){
        try{
            if(canceled)
                return;

            new Handler(originatingLooper)
                    .post(runnable);
        }
        catch(Throwable e){ e.printStackTrace(); }
    }

    public void cancel(){
        this.canceled = true;

        if(failOnCancel){
            new Handler(originatingLooper).post(new Runnable(){
                public void run(){
                    if(failListener != null)
                        failListener.onFail(new RequestCanceledError(context));
                }
            });
        }
    }

}
