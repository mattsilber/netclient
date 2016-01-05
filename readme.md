# netclient

A stupid-simple wrapper around HTTP/S-UrlConnection.


# Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.guardanis:netclient:1.0.0')
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

In order to use the ApiRequest, you need to override some String resources. Assuming you're using a JSON-based API, you only need to override *R.string.nc__api_url* to hook up. When making API Requests, you don't need to include the API Url as it is prepended for you.

If you want to enable Basic Auth, just set *R.boolean.nc__api_basic_auth_enabled* to true, then override

    <string name="nc__api_basic_auth_username">some_user</string>
    <string name="nc__api_basic_auth_password">some_pass</string>

If you need an API-Version header, just set *R.bool.nc__api_version_header_enabled* to true, then override

    <string name="nc__api_version_header_name">API-Version</string>
    <string name="nc__api_version_header_value">420</string>

If you want to log requests and response data, set *R.bool.nc__log_enabled* to true.

There are a few other things you can configure, including several request properties, but if you need more control you can just extend ApiRequest.

# Limitations, Known Issues, and ToDo's
* Accept custom SSLSocketFactory for HttpsUrlConnections
* Accept custom ErrorParser instead of just DefaultErrorParser
