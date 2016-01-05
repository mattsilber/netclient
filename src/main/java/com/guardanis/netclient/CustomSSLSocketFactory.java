package com.guardanis.netclient;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CustomSSLSocketFactory extends SSLSocketFactory {

    protected SSLContext currentContext = SSLContext.getInstance("TLS");

    public CustomSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(null);
        currentContext.init(null, new TrustManager[]{new X509_Trust_Manager()}, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return currentContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return currentContext.getSocketFactory().createSocket();
    }

    private class X509_Trust_Manager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}

