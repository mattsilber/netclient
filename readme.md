# netclient

A stupid-simple wrapper around HTTP/S-UrlConnection.


# Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.guardanis:netclient:1.0.10')
    }
```


# Usage

This library is meant to easily hook up to an API, parse that returned response, and pass off the successfully parsed data (or a ResponseError) to the calling Thread.

The basic classes to work with are WebRequest and ApiRequest (an extension of the former). 

When you execute the request, it will download the data in a separate thread, pass a WebResult wrapping the returned String data to the supplied ResponseParser, and return the result on the calling thread. 

A RequestError will be returned if there was an issue connecting, parsing, or doing whatever. It's basically a wrapper around a list of error messages that can be nicely displayed to a user. A GeneralError will be returned in the event of an error is thrown and will contain whatever Throwable caused it. An ApiError is thrown when the ApiRequest's response contains error data instead of the desired data (see current limitations for the ErrorParser). 

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

If you want to use a separate *ErrorParser*, you can either manually attach one via *setErrorParser(ErrorParser)* for specific requests, or set the default ErrorParser for ApiRequests via *NetUtils.setDefaultApiErrorParser(ErrorParser)*. If you're doing that, I'd recommend it be done in your Application's onCreate().

There are a few other things you can configure, including several request properties, but if you need more control you can just extend ApiRequest.

##### Global API Request Properties
Each WebRequest can have it's own individual header properties applied via addRequestProperty(String key, String value), or they can be applied globally via the GlobalApiRequestProperties singleton. e.g.

    GlobalApiRequestProperties.getInstance(context)
        .register("Some-Property", "some_value")
        .register("Some-Other-Property", "some_other_value");

##### Global API URL Parameters
The GlobalApiUrlParams singleton can be used to append encoded properties to the end of each API URL automatically (i.e. ?key=value&other_key=other_value). Simply register the key/value pair and the ApiRequest will add them for you:

    GlobalApiUrlParams.getInstance(context)
        .register("some_key", "some_value")
        .register("some_other_key", "some_other_value");

### SSL Support
If you would like to use custom SSL verification for your WebRequests, as of version 1.0.8, you can provide a BKS Keystore file and a password (default cert is configurable through resources by overriding *R.string.nc__ssl_cert_password* and the actual keystore file, *R.raw.nc__cert.bks*) to the WebRequest.

By default, using custom certificate verification is disabled and will default to using the system's. To enable the use of custom certificate verification, set *R.bool.nc__custom_ssl_mode_enabled* to true and supply your certification/password to the library.

Note: For testing purposes only, you may disable the SSL security measures by calling *setSslUnsafeModeEnabled(boolean unsafeSslModeEnabled)* to accept all host name verifiers. Do NOT put that in production. Also, if custom SSL verification is disabled, this unsafe mode will have no effect.

If you need help generating the required files, check the wiki.

### Multi-API Support
It's absolutely possible to work with multiple API's, but it's not currently configurable via overriding resources. If you want to implement the ApiRequest pattern for a secondary API, you can extends the ApiRequest class and override the configuration methods. The following template can help you do that pretty easily:

    public class SomeApiRequest<T> extends com.guardanis.netclient.ApiRequest<T> {

        public SomeApiRequest(Context context, ConnectionType connectionType) {
            this(context, connectionType, "");
        }

        public SomeApiRequest(Context context, ConnectionType connectionType, String targetUrl) {
            super(context, connectionType, targetUrl);

            setErrorParser(new DefaultErrorParser(context)); // Set your error parser     
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

        @Override
        protected URL buildUrl() throws MalformedURLException {
            return new URL(targetUrl);
        }
    }

You could then use SomeApiRequest the same way you would the normal ApiRequest.

