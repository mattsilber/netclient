package com.guardanis.netclient;

import android.content.Context;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CustomSSLSocketFactory extends SSLSocketFactory {

    protected SSLContext currentContext = SSLContext.getInstance("TLS");

    protected CustomSSLSocketFactory(KeyStore keyStore) throws CertificateException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(null);

        currentContext.init(null, new TrustManager[]{ new SingleX509TrustManager(keyStore) }, null);
    }

    protected CustomSSLSocketFactory() throws CertificateException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(null);

        currentContext.init(null, new TrustManager[]{ new UnsafeX509TrustManager() }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return currentContext.getSocketFactory()
                .createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return currentContext.getSocketFactory()
                .createSocket();
    }

    private class SingleX509TrustManager implements X509TrustManager {

        protected List<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();

        protected SingleX509TrustManager(KeyStore... additionalkeyStores) throws CertificateException {
            final List<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();

            try{
                final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                original.init((KeyStore) null);
                factories.add(original);

                for(KeyStore keyStore : additionalkeyStores){
                    final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                    additionalCerts.init(keyStore);
                    factories.add(additionalCerts);
                }

            }
            catch(Exception e){
                throw new CertificateException(e);
            }

            for(TrustManagerFactory tmf : factories)
                for(TrustManager tm : tmf.getTrustManagers())
                    if(tm instanceof X509TrustManager)
                        x509TrustManagers.add((X509TrustManager) tm);


            if(x509TrustManagers.size() == 0)
                throw new CertificateException("Couldn't find any X509TrustManagers");
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
            defaultX509TrustManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for(X509TrustManager tm : x509TrustManagers) {
                try {
                    tm.checkServerTrusted(chain,authType);

                    return;
                }
                catch(CertificateException e ) {  }
            }

            throw new CertificateException();
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();

            for(X509TrustManager tm : x509TrustManagers )
                list.addAll(Arrays.asList(tm.getAcceptedIssuers()));

            return list.toArray(new X509Certificate[list.size()]);
        }
    }

    private class UnsafeX509TrustManager implements X509TrustManager {

        private boolean enableThrowCertError = false;

        protected UnsafeX509TrustManager(){
            this.enableThrowCertError = false; // Get around that check to see if it's unsafe
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if(enableThrowCertError)
                throw new CertificateException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if(enableThrowCertError)
                throw new CertificateException();
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static CustomSSLSocketFactory getInstance(Context context, int certFile, String certPass) throws CertificateException {
        try {
            KeyStore ks = KeyStore.getInstance("BKS");

            InputStream in = context.getResources().openRawResource(certFile);

            try {
                ks.load(in, certPass.toCharArray());
            }
            finally {
                in.close();
            }

            return new CustomSSLSocketFactory(ks);
        }
        catch(Exception e ) {
            throw new CertificateException(e);
        }
    }

    public static CustomSSLSocketFactory getUnsafeInstance() throws CertificateException {
        try {
            return new CustomSSLSocketFactory();
        }
        catch(Exception e ) {
            throw new CertificateException(e);
        }
    }

}

