package wikipedia.http;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Const;

public class WikiAPIClient {

    private static final int HTTP_TIMEOUT = 15000; // milisec
    private static final Logger LOG = LoggerFactory.getLogger(WikiAPIClient.class.getName());

    private final DefaultHttpClient httpclient;

    /**
     * Allow Users to disable gzip for small requests
     */
    public WikiAPIClient(final DefaultHttpClient httpclient, final boolean enableGzip) {
        this.httpclient = httpclient;
        setHTTPClientTimeouts(httpclient);
        if (enableGzip) {
            addGzipRequestInterceptor(httpclient);
            addGzipResponseInterceptor(httpclient);
        }
    }

    public WikiAPIClient(final DefaultHttpClient httpclient) {
        this(httpclient, true);
    }

    public String executeHTTPRequest(final String url) {
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("User-Agent", Const.USER_AGENT);
        LOG.info("executing request " + httpget.getURI());
        BasicHttpContext context = new BasicHttpContext();
        try {
            HttpResponse response = this.httpclient.execute(httpget, context);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                final String contentString = IOUtils.toString(entity.getContent());
                EntityUtils.consume(entity);
                return contentString;
            }
            // ensure the connection gets released to the manager
            EntityUtils.consume(entity);
        } catch (Exception ex) {
            httpget.abort();
            LOG.error("HTTP Abort!", ex);
        }
        LOG.error("Problem while executing request, URL was: " + url);
        return "";
    }

    private void addGzipRequestInterceptor(final DefaultHttpClient httpclient) {
        httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(final HttpRequest request,
                                final HttpContext context) throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });
    }

    private void addGzipResponseInterceptor(final DefaultHttpClient httpclient) {
        httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response,
                                final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    for (HeaderElement codec : ceheader.getElements()) {
                        if (codec.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });
    }

    private void setHTTPClientTimeouts(final DefaultHttpClient httpclient) {
        HttpParams httpParams = httpclient.getParams();
        int connectionTimeoutMillis = HTTP_TIMEOUT;
        int socketTimeoutMillis = HTTP_TIMEOUT;
        HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMillis);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeoutMillis);
    }

}
