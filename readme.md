# netclient
[![Download](https://api.bintray.com/packages/mattsilber/maven/netclient/images/download.svg) ](https://bintray.com/mattsilber/maven/netclient/_latestVersion)

A stupid-simple wrapper around HTTP/S-UrlConnection.

# Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.guardanis:netclient:1.0.15')
    }
```

# Usage

This library is meant to easily hook up to an API, parse that returned response, and pass off the successfully parsed data (or a *RequestError*) to the calling Thread.

The basic classes to work with are the general *WebRequest* and the more extendable *ApiRequest*. 

When you execute the request, it will download the data in a separate thread, pass a WebResult wrapping the returned String data / response code to the supplied ResponseParser, and return the result on the calling thread. 

A RequestError will be returned if there was an issue connecting, parsing, or doing whatever. It's basically a wrapper around a list of error messages that can be nicely displayed to a user. A GeneralError will be returned in the event of an error is thrown and will contain whatever Throwable caused it. An ApiError is thrown when any WebRequest's response contains error data instead of the desired data (see current limitations for the ErrorParser). 

### Example

    new ApiRequest<SomeObject>(context, ConnectionType.GET)
        .setTargetUrl("some_endpoint/some_id")
        .setResponseParser((webResult) -> new SomeObject(webResult.getResponseJson))
        .onSuccess((result) -> doSomethingWithSomeObject(result))
        .onFail((errors) -> Log.d(TAG, errors.toString())
        .execute();

And with a POST request...

    new ApiRequest<SomeObject>(context, ConnectionType.POST)
        .setTargetUrl("some_endpoint")
        .setData(someObject.toJson())
        ...

### Configuration

In order to use the ApiRequest, you need to override some String resources. Assuming you're using a JSON-based API, you only need to override

    <string name="nc__api_url">http://some_url.com/api/v1/</string>

When making API Requests, you don't need to include the API Url as it is prepended for you.

**Note:** The trailing slash in the API Url is more important than it should be...

If you want to enable Basic Auth, just set *R.boolean.nc__api_basic_auth_enabled* to true, then override

    <string name="nc__api_basic_auth_username">some_user</string>
    <string name="nc__api_basic_auth_password">some_pass</string>

If you need an API-Version header, just set *R.bool.nc__api_version_header_enabled* to true, then override

    <string name="nc__api_version_header_name">API-Version</string>
    <string name="nc__api_version_header_value">420</string>

If you want to log requests and response data, set *R.bool.nc__log_enabled* to true.

There are a few other things you can configure, including several request properties, but if you need more control you can just extend ApiRequest.

##### Global API Request Properties
Each WebRequest can have it's own individual header properties applied via addRequestProperty(String key, String value), or they can be applied globally via the GlobalApiRequestProperties singleton. e.g.

    GlobalApiRequestProperties.getInstance(context)
        .register("Some-Property", "some_value")
        .register("Some-Other-Property", "some_other_value");

As of v1.0.14, you can also use the `RequestParams` class for helping build URLs for non-ApiRequests.

##### Global API URL Parameters
The GlobalApiUrlParams singleton can be used to append encoded properties to the end of each API URL automatically (i.e. ?key=value&other_key=other_value). Simply register the key/value pair and the ApiRequest will add them for you:

    GlobalApiUrlParams.getInstance(context)
        .register("some_key", "some_value")
        .register("some_other_key", "some_other_value");

As of v1.0.14, you can also use the `UrlParams` class for helping build URLs for non-ApiRequests.

##### Error Handling
If you want to use a separate *ErrorParser*, you can either manually attach one via *WebRequest.setErrorParser(ErrorParser)* for specific requests, or set the default ErrorParser for both general WebRequests and ApiRequests via the methods below. If globally overriding, I'd recommend it be done in your Application's onCreate().

    NetUtils.getInstance(context)
        .setGeneralErrorParser(ErrorParser)
        .setApiErrorParser(ErrorParser);

Note on the ErrorParser implementations: they must have a default constructor as they are saved and loaded via reflection. You should also ensure your ErrorParser class names are excluded in your ProGuard rules, else it may fail to load them if you don't actively ensure they're set correctly.

##### SSL Support
If you would like to use custom SSL verification for your WebRequests, as of version 1.0.8, you can provide a BKS Keystore file and a password (default cert is configurable through resources by overriding *R.string.nc__ssl_cert_password* and the actual keystore file, *R.raw.nc__cert.bks*) to the WebRequest.

By default, using custom certificate verification is disabled and will default to using the system's. To enable the use of custom certificate verification, set *R.bool.nc__custom_ssl_mode_enabled* to true and supply your certification/password to the library.

Note: For testing purposes only, you may disable the SSL security measures by calling *setSslUnsafeModeEnabled(boolean unsafeSslModeEnabled)* to accept all host name verifiers. Do NOT put that in production. Also, if custom SSL verification is disabled, this unsafe mode will have no effect.

If you need help generating the required files, check the wiki.

##### Multi-API Support
It's absolutely possible to work with multiple API's, but it's not currently configurable via overriding resources. If you want to implement the ApiRequest pattern for a secondary API, you can extends the ApiRequest class and override the configuration methods. The following template can help you do that pretty easily:

    public class SomeApiRequest<T> extends com.guardanis.netclient.ApiRequest<T> {

        public SomeApiRequest(Context context, ConnectionType connectionType) {
            this(context, connectionType, "");
        }

        public SomeApiRequest(Context context, ConnectionType connectionType, String targetUrl) {
            super(context, connectionType, targetUrl);

            setErrorParser(new DefaultErrorParser()); // Set your error parser     
            setSslCertificateInfo(R.raw.some_api_cert, context.getString(R.string.some_api_cert_password)); // Set the SSL cert info if you're using it   
        }

        @Override
        public SomeApiRequest<T> setTargetUrl(String targetUrl){
            this.targetUrl = "https://some_api.com/api/v1/" + targetUrl.trim();
            return this;
        }

        @Override
        protected void setRequestProperties(HttpURLConnection conn){
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Accept-Encoding", "gzip");

            for(String key : requestProperties.keySet())
                conn.setRequestProperty(key, requestProperties.get(key));
        }

    }

You could then use SomeApiRequest the same way you would the normal ApiRequest.

##### Caching

As of version 1.0.15, the result of a GET request can be cached at the WebRequest-level by calling `WebRequest.setCacheDurationMs(long)` with the maximum length of time a result's cache can be considered valid for in milliseconds.

If you would like to globally override the cypher used for storing cached data, just set the value for `R.string.nc__cache_encryption_cypher*, or call `WebRequest.setCacheCypher(String)` on a per-request basis. If the cyphers from a previous cache don't match, the default behavior is to continue as if no data was available.

The cache key is based on the final URL (with all query parameters appended), but does NOT take request properties into account.

Note: if you're using the Map<String, Object> extras inside the WebResult for passing data, please ensure all Objects are serializable, or else it will fail to rebuild the extras and the caching will become inherently invalid.

