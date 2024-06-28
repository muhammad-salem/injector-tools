package org.injector.tools.proxy;

/**
 * HTTP defines a set of request methods to indicate the desired action to be
 * performed for a given resource. Although they can also be nouns, these
 * request methods are sometimes referred to as HTTP verbs. Each of them
 * implements a different semantic, but some common features are shared by a
 * group of them: e.g. a request method can be safe, idempotent, or cacheable.
 * <p>GET The GET method requests a representation of the specified resource.
 * Requests using GET should only retrieve data.
 * <p>HEAD The HEAD method asks for a
 * response identical to that of a GET request, but without the response body.
 * <p>POST The POST method is used to submit an entity to the specified resource,
 * often causing a change in state or side effects on the server
 * <p>PUT The PUT
 * method replaces all current representations of the target resource with the
 * request payload.
 * <p>DELETE The DELETE method deletes the specified resource.
 * <p>CONNECT The CONNECT method establishes a tunnel to the server identified by the target resource.
 *
 * <p>OPTIONS The OPTIONS method is used to describe the communication options for
 * the target resource.
 * <p>TRACE The TRACE method performs a message loop-back test
 * along the path to the target resource.
 *
 * <p>PATCH The PATCH method is used to apply partial modifications to a resource.
 *
 * @author salem
 */
public enum RequestMethod {
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    CONNECT("CONNECT"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    PATCH("PATCH");

    final String method;

    RequestMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return method;
    }
}