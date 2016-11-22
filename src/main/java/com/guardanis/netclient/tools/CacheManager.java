package com.guardanis.netclient.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.guardanis.netclient.WebResult;

import org.json.JSONObject;

import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CacheManager {

    private static CacheManager instance;
    public static CacheManager getInstance(Context context){
        if(instance == null)
            instance = new CacheManager(context);

        return instance;
    }

    private static final String PREFS = "nc__cachemanager_preferences";
    private static final String PREF__URL_DATA = "nc__url_%1$s";

    private static final String STORED_DATA = "data";
    private static final String STORED_RESPONSE_CODE = "response_code";
    private static final String STORED_EXTRAS = "extras";
    private static final String STORED_TIME = "updated_at";

    private Context context;
    private SharedPreferences prefs;

    protected CacheManager(Context context){
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS, 0);
    }

    public WebResult get(String cacheKey, String cypher, long validDuration){
        String encrypted = getEncryptedCacheData(cacheKey);

        if(encrypted == null)
            return null;

        try{
            String decrypted = decrypt(encrypted, cypher);

            JSONObject wrapped = new JSONObject(decrypted);

            if(System.currentTimeMillis() - wrapped.optLong(STORED_TIME, 0) < validDuration){
                WebResult result = new WebResult(wrapped.optInt(STORED_RESPONSE_CODE, 0),
                        wrapped.optString(STORED_DATA, null),
                        wrapped.optLong(STORED_TIME, 0));

                JSONObject extras = wrapped.optJSONObject(STORED_EXTRAS);

                if(extras != null){
                    Iterator<String> keys = extras.keys();

                    while(keys.hasNext()){
                        String key = keys.next();

                        result.putExtra(key, extras.get(key));
                    }
                }

                NetUtils.getInstance(context)
                        .log("Got cache: " + result.getUnparsedResponse());

                return result;
            }
        }
        catch(Throwable e){
            NetUtils.getInstance(context)
                    .log(e.getMessage());
        }

        return null;
    }

    public void cache(String cacheKey, String cypher, WebResult data){
        try{
            JSONObject wrapped = new JSONObject();
            wrapped.put(STORED_DATA, data.getUnparsedResponse());
            wrapped.put(STORED_RESPONSE_CODE, data.getResponseCode());
            wrapped.put(STORED_TIME, data.getCreatedAtMs());

            if(data.getExtras() != null){
                JSONObject extras = new JSONObject();

                for(String key : data.getExtras().keySet())
                    extras.put(key, data.getExtras().get(key));

                wrapped.put(STORED_EXTRAS, extras);
            }

            String encrypted = encrypt(wrapped.toString(),
                    cypher);

            prefs.edit()
                    .putString(getCacheKey(cacheKey), encrypted)
                    .commit();

            NetUtils.getInstance(context)
                    .log("Cached: " + encrypted);
        }
        catch(Throwable e){
            NetUtils.getInstance(context)
                    .log(e.getMessage());
        }
    }

    protected String getEncryptedCacheData(String cacheKey){
        return prefs.getString(getCacheKey(cacheKey),
                null);
    }

    protected String getCacheKey(String cacheKey){
        return String.format(PREF__URL_DATA,
                String.valueOf(cacheKey.hashCode()));
    }

    protected String encrypt(String data, String cypher){
        try {
            DESKeySpec keySpec = new DESKeySpec(cypher.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] formatted = data.getBytes("UTF8");

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return new String(Base64.encodeToString(cipher.doFinal(formatted),
                    Base64.DEFAULT));
        }
        catch(Throwable e){
            NetUtils.getInstance(context)
                .log(e.getMessage());

            throw new RuntimeException("Failed to encrypt data: " + e.getMessage());
        }
    }

    protected String decrypt(String data, String cypher){
        try {
            DESKeySpec keySpec = new DESKeySpec(cypher.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encryptedData = Base64.decode(data, Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(cipher.doFinal(encryptedData));
        }
        catch (Throwable e){
            NetUtils.getInstance(context)
                    .log(e.getMessage());

            throw new RuntimeException("Failed to decrypt data: " + e.getMessage());
        }
    }

    public void clear(){
        prefs.edit()
                .clear()
                .commit();
    }

    public void clear(String cacheKey){
        prefs.edit()
                .remove(getCacheKey(cacheKey))
                .commit();
    }

}
