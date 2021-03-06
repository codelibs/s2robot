/*
 * Copyright 2012-2015 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.robot.client.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.codelibs.core.beans.BeanDesc;
import org.codelibs.core.beans.PropertyDesc;
import org.codelibs.core.beans.factory.BeanDescFactory;
import org.codelibs.core.io.CopyUtil;
import org.codelibs.core.lang.StringUtil;
import org.codelibs.core.timer.TimeoutManager;
import org.codelibs.core.timer.TimeoutTarget;
import org.codelibs.core.timer.TimeoutTask;
import org.codelibs.robot.Constants;
import org.codelibs.robot.S2RobotContext;
import org.codelibs.robot.client.AbstractS2RobotClient;
import org.codelibs.robot.entity.ResponseData;
import org.codelibs.robot.entity.RobotsTxt;
import org.codelibs.robot.exception.MaxLengthExceededException;
import org.codelibs.robot.exception.RobotCrawlAccessException;
import org.codelibs.robot.exception.RobotSystemException;
import org.codelibs.robot.helper.ContentLengthHelper;
import org.codelibs.robot.helper.RobotsTxtHelper;
import org.codelibs.robot.util.CrawlingParameterUtil;
import org.codelibs.robot.util.TemporaryFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shinsuke
 *
 */
public class HcHttpClient extends AbstractS2RobotClient {

    public static final String ACCESS_TIMEOUT_PROPERTY = "accessTimeout";

    public static final String CONNECTION_TIMEOUT_PROPERTY = "connectionTimeout";

    public static final String STALE_CHECKING_ENABLED_PROPERTY = "staleCheckingEnabled";

    public static final String SO_TIMEOUT_PROPERTY = "soTimeout";

    public static final String PROXY_HOST_PROPERTY = "proxyHost";

    public static final String PROXY_PORT_PROPERTY = "proxyPort";

    public static final String PROXY_AUTH_SCHEME_PROPERTY = "proxyAuthScheme";

    public static final String PROXY_CREDENTIALS_PROPERTY = "proxyCredentials";

    public static final String USER_AGENT_PROPERTY = "userAgent";

    public static final String ROBOTS_TXT_ENABLED_PROPERTY = "robotsTxtEnabled";

    public static final String BASIC_AUTHENTICATIONS_PROPERTY = "basicAuthentications";

    public static final String REQUERT_HEADERS_PROPERTY = "requestHeaders";

    public static final String COOKIES_PROPERTY = "cookies";

    public static final String AUTH_SCHEME_PROVIDERS_PROPERTY = "authSchemeProviders";

    private static final Logger logger = LoggerFactory // NOPMD
            .getLogger(HcHttpClient.class);

    @Resource
    protected RobotsTxtHelper robotsTxtHelper;

    @Resource
    protected ContentLengthHelper contentLengthHelper;

    protected volatile CloseableHttpClient httpClient;

    private final List<Header> requestHeaderList = new ArrayList<Header>();

    private final Map<String, Object> httpClientPropertyMap = new HashMap<String, Object>();

    private TimeoutTask connectionMonitorTask;

    protected Integer accessTimeout; // sec

    protected Integer connectionTimeout;

    protected Integer maxTotalConnections;

    protected Integer maxConnectionsPerRoute;

    protected Boolean staleCheckingEnabled;

    protected Integer soTimeout;

    protected String cookieSpec;

    protected String userAgent = "S2Robot";

    protected HttpClientContext httpClientContext = HttpClientContext.create();

    protected String proxyHost;

    protected Integer proxyPort;

    protected AuthScheme proxyAuthScheme = new BasicScheme();

    protected Credentials proxyCredentials;

    protected int responseBodyInMemoryThresholdSize = 1 * 1024 * 1024; // 1M

    protected String defaultMimeType = "application/octet-stream";

    protected CookieStore cookieStore = new BasicCookieStore();

    protected HttpClientConnectionManager clientConnectionManager;

    protected Map<String, AuthSchemeProvider> authSchemeProviderMap;

    protected int connectionCheckInterval = 5; // sec

    protected long idleConnectionTimeout = 60 * 1000; // 1min

    protected Pattern redirectHttpStatusPattern = Pattern
            .compile("[3][0-9][0-9]");

    protected boolean useRobotsTxtDisallows = true;

    protected boolean useRobotsTxtAllows = false;

    protected CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    protected AuthCache authCache = new BasicAuthCache();

    protected HttpRoutePlanner routePlanner;

    public synchronized void init() {
        if (httpClient != null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Initializing " + HcHttpClient.class.getName());
        }

        // access timeout
        final Integer accessTimeoutParam = getInitParameter(
                ACCESS_TIMEOUT_PROPERTY, accessTimeout);
        if (accessTimeoutParam != null) {
            accessTimeout = accessTimeoutParam;
        }

        // robots.txt parser
        final Boolean robotsTxtEnabled = getInitParameter(
                ROBOTS_TXT_ENABLED_PROPERTY, Boolean.TRUE);
        if (robotsTxtHelper != null) {
            robotsTxtHelper.setEnabled(robotsTxtEnabled.booleanValue());
        }

        // httpclient
        final org.apache.http.client.config.RequestConfig.Builder requestConfigBuilder = RequestConfig
                .custom();
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        final Integer connectionTimeoutParam = getInitParameter(
                CONNECTION_TIMEOUT_PROPERTY, connectionTimeout);
        if (connectionTimeoutParam != null) {
            requestConfigBuilder.setConnectTimeout(connectionTimeoutParam);

        }
        final Boolean staleCheckingEnabledParam = getInitParameter(
                STALE_CHECKING_ENABLED_PROPERTY, staleCheckingEnabled);
        if (staleCheckingEnabledParam != null) {
            requestConfigBuilder
                    .setStaleConnectionCheckEnabled(staleCheckingEnabledParam);
        }
        final Integer soTimeoutParam = getInitParameter(SO_TIMEOUT_PROPERTY,
                soTimeout);
        if (soTimeoutParam != null) {
            requestConfigBuilder.setSocketTimeout(soTimeoutParam);
        }

        // AuthSchemeFactory
        final RegistryBuilder<AuthSchemeProvider> authSchemeProviderBuilder = RegistryBuilder
                .create();
        final Map<String, AuthSchemeProvider> factoryMap = getInitParameter(
                AUTH_SCHEME_PROVIDERS_PROPERTY, authSchemeProviderMap);
        if (factoryMap != null) {
            for (final Map.Entry<String, AuthSchemeProvider> entry : factoryMap
                    .entrySet()) {
                authSchemeProviderBuilder.register(entry.getKey(),
                        entry.getValue());
            }
        }

        // user agent
        userAgent = getInitParameter(USER_AGENT_PROPERTY, userAgent);
        if (StringUtil.isNotBlank(userAgent)) {
            httpClientBuilder.setUserAgent(userAgent);
        }

        final HttpRoutePlanner planner = buildRoutePlanner();
        if (planner != null) {
            httpClientBuilder.setRoutePlanner(planner);
        }

        // Authentication
        final Authentication[] siteCredentialList = getInitParameter(
                BASIC_AUTHENTICATIONS_PROPERTY, new Authentication[0]);
        for (final Authentication authentication : siteCredentialList) {
            final AuthScope authScope = authentication.getAuthScope();
            credentialsProvider.setCredentials(authScope,
                    authentication.getCredentials());
            final AuthScheme authScheme = authentication.getAuthScheme();
            if (authScope.getHost() != null && authScheme != null) {
                final HttpHost targetHost = new HttpHost(authScope.getHost(),
                        authScope.getPort());
                authCache.put(targetHost, authScheme);
            }
        }

        httpClientContext.setAuthCache(authCache);
        httpClientContext.setCredentialsProvider(credentialsProvider);

        // Request Header
        final RequestHeader[] requestHeaders = getInitParameter(
                REQUERT_HEADERS_PROPERTY, new RequestHeader[0]);
        for (final RequestHeader requestHeader : requestHeaders) {
            if (requestHeader.isValid()) {
                requestHeaderList.add(new BasicHeader(requestHeader.getName(),
                        requestHeader.getValue()));
            }
        }

        // do not redirect
        requestConfigBuilder.setRedirectsEnabled(false);

        // cookie
        if (cookieSpec != null) {
            requestConfigBuilder.setCookieSpec(cookieSpec);
        }

        // cookie store
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        if (cookieStore != null) {
            final Cookie[] cookies = getInitParameter(COOKIES_PROPERTY,
                    new Cookie[0]);
            for (final Cookie cookie : cookies) {
                cookieStore.addCookie(cookie);
            }
        }

        connectionMonitorTask = TimeoutManager.getInstance().addTimeoutTarget(
                new HcConnectionMonitorTarget(clientConnectionManager,
                        idleConnectionTimeout), connectionCheckInterval, true);

        final CloseableHttpClient closeableHttpClient = httpClientBuilder
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(requestConfigBuilder.build()).build();
        if (!httpClientPropertyMap.isEmpty()) {
            final BeanDesc beanDesc = BeanDescFactory
                    .getBeanDesc(closeableHttpClient.getClass());
            for (final Map.Entry<String, Object> entry : httpClientPropertyMap
                    .entrySet()) {
                final String propertyName = entry.getKey();
                if (beanDesc.hasPropertyDesc(propertyName)) {
                    final PropertyDesc propertyDesc = beanDesc
                            .getPropertyDesc(propertyName);
                    propertyDesc
                            .setValue(closeableHttpClient, entry.getValue());
                } else {
                    logger.warn("DefaultHttpClient does not have "
                            + propertyName + ".");
                }
            }
        }

        httpClient = closeableHttpClient;
    }

    @PreDestroy
    public void destroy() {
        if (connectionMonitorTask != null) {
            connectionMonitorTask.cancel();
        }
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (final IOException e) {
                logger.error("Failed to close httpClient.", e);
            }
        }
    }

    public void addHttpClientProperty(final String name, final Object value) {
        if (StringUtil.isNotBlank(name) && value != null) {
            httpClientPropertyMap.put(name, value);
        }
    }

    protected void processRobotsTxt(final String url) {
        if (StringUtil.isBlank(url)) {
            throw new RobotSystemException("url is null or empty.");
        }

        if (robotsTxtHelper == null || !robotsTxtHelper.isEnabled()) {
            // not support robots.txt
            return;
        }

        // robot context
        final S2RobotContext robotContext = CrawlingParameterUtil
                .getRobotContext();
        if (robotContext == null) {
            // wrong state
            return;
        }

        final int idx = url.indexOf('/', url.indexOf("://") + 3);
        String hostUrl;
        if (idx >= 0) {
            hostUrl = url.substring(0, idx);
        } else {
            hostUrl = url;
        }
        final String robotTxtUrl = hostUrl + "/robots.txt";

        // check url
        if (robotContext.getRobotTxtUrlSet().contains(robotTxtUrl)) {
            if (logger.isDebugEnabled()) {
                logger.debug(robotTxtUrl + " is already visited.");
            }
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Checking URL: " + robotTxtUrl);
        }
        // add url to a set
        robotContext.getRobotTxtUrlSet().add(robotTxtUrl);

        final HttpGet httpGet = new HttpGet(robotTxtUrl);

        // request header
        for (final Header header : requestHeaderList) {
            httpGet.addHeader(header);
        }

        HttpEntity httpEntity = null;
        try {
            // get a content
            final HttpResponse response = executeHttpClient(httpGet);
            httpEntity = response.getEntity();

            final int httpStatusCode = response.getStatusLine().getStatusCode();
            if (httpStatusCode == 200) {

                // check file size
                final Header contentLengthHeader = response
                        .getFirstHeader("Content-Length");
                if (contentLengthHeader != null) {
                    final String value = contentLengthHeader.getValue();
                    final long contentLength = Long.parseLong(value);
                    if (contentLengthHelper != null) {
                        final long maxLength = contentLengthHelper
                                .getMaxLength("text/plain");
                        if (contentLength > maxLength) {
                            throw new MaxLengthExceededException(
                                    "The content length (" + contentLength
                                            + " byte) is over " + maxLength
                                            + " byte. The url is "
                                            + robotTxtUrl);
                        }
                    }
                }

                if (httpEntity != null) {
                    final RobotsTxt robotsTxt = robotsTxtHelper
                            .parse(httpEntity.getContent());
                    if (robotsTxt != null) {
                        final String[] sitemaps = robotsTxt.getSitemaps();
                        if (sitemaps.length > 0) {
                            robotContext.addSitemaps(sitemaps);
                        }

                        final RobotsTxt.Directive directive = robotsTxt
                                .getMatchedDirective(userAgent);
                        if (directive != null) {
                            if (useRobotsTxtDisallows) {
                                for (String urlPattern : directive
                                        .getDisallows()) {
                                    if (StringUtil.isNotBlank(urlPattern)) {
                                        urlPattern = convertRobotsTxtPathPattern(urlPattern);
                                        robotContext.getUrlFilter().addExclude(
                                                hostUrl + urlPattern);
                                    }
                                }
                            }
                            if (useRobotsTxtAllows) {
                                for (String urlPattern : directive.getAllows()) {
                                    if (StringUtil.isNotBlank(urlPattern)) {
                                        urlPattern = convertRobotsTxtPathPattern(urlPattern);
                                        robotContext.getUrlFilter().addInclude(
                                                hostUrl + urlPattern);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (final RobotSystemException e) {
            httpGet.abort();
            throw e;
        } catch (final Exception e) {
            httpGet.abort();
            throw new RobotCrawlAccessException("Could not process "
                    + robotTxtUrl + ". ", e);
        } finally {
            EntityUtils.consumeQuietly(httpEntity);
        }
    }

    protected String convertRobotsTxtPathPattern(final String path) {
        String newPath = path.replaceAll("\\.", "\\\\.")
                .replaceAll("\\*", ".*");
        if (newPath.charAt(0) != '/') {
            newPath = ".*" + newPath;
        }
        if (!newPath.endsWith("$") && !newPath.endsWith(".*")) {
            newPath = newPath + ".*";
        }
        return newPath.replaceAll("\\.\\*\\.\\*", ".*");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codelibs.robot.http.HttpClient#doGet(java.lang.String)
     */
    @Override
    public ResponseData doGet(final String url) {
        HttpUriRequest httpGet;
        try {
            httpGet = new HttpGet(url);
        } catch (final IllegalArgumentException e) {
            throw new RobotCrawlAccessException("The url may not be valid: "
                    + url, e);
        }
        return doHttpMethod(url, httpGet);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.codelibs.robot.http.HttpClient#doHead(java.lang.String)
     */
    @Override
    public ResponseData doHead(final String url) {
        HttpUriRequest httpHead;
        try {
            httpHead = new HttpHead(url);
        } catch (final IllegalArgumentException e) {
            throw new RobotCrawlAccessException("The url may not be valid: "
                    + url, e);
        }
        return doHttpMethod(url, httpHead);
    }

    public ResponseData doHttpMethod(final String url,
            final HttpUriRequest httpRequest) {
        if (httpClient == null) {
            init();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Accessing " + url);
        }

        // start
        AccessTimeoutTarget accessTimeoutTarget = null;
        TimeoutTask accessTimeoutTask = null;
        if (accessTimeout != null) {
            accessTimeoutTarget = new AccessTimeoutTarget(
                    Thread.currentThread());
            accessTimeoutTask = TimeoutManager.getInstance().addTimeoutTarget(
                    accessTimeoutTarget, accessTimeout.intValue(), false);
        }

        try {
            return processHttpMethod(url, httpRequest);
        } finally {
            if (accessTimeout != null) {
                accessTimeoutTarget.stop();
                if (!accessTimeoutTask.isCanceled()) {
                    accessTimeoutTask.cancel();
                }
            }
        }
    }

    protected ResponseData processHttpMethod(final String url,
            final HttpUriRequest httpRequest) {
        try {
            processRobotsTxt(url);
        } catch (final RobotCrawlAccessException e) {
            if (logger.isInfoEnabled()) {
                final StringBuilder buf = new StringBuilder();
                buf.append(e.getMessage());
                if (e.getCause() != null) {
                    buf.append(e.getCause().getMessage());
                }
                logger.info(buf.toString());
            } else if (logger.isDebugEnabled()) {
                logger.debug("Crawling Access Exception at " + url, e);
            }
        }

        // request header
        for (final Header header : requestHeaderList) {
            httpRequest.addHeader(header);
        }

        ResponseData responseData = null;
        InputStream inputStream = null;
        HttpEntity httpEntity = null;
        try {
            // get a content
            final HttpResponse response = executeHttpClient(httpRequest);
            httpEntity = response.getEntity();

            final int httpStatusCode = response.getStatusLine().getStatusCode();
            // redirect
            if (isRedirectHttpStatus(httpStatusCode)) {
                final Header locationHeader = response
                        .getFirstHeader("location");
                if (locationHeader == null) {
                    logger.warn("Invalid redirect location at " + url);
                } else {
                    responseData = new ResponseData();
                    responseData.setRedirectLocation(locationHeader.getValue());
                    return responseData;
                }
            }

            long contentLength = 0;
            String contentEncoding = Constants.UTF_8;
            if (httpEntity == null) {
                inputStream = new ByteArrayInputStream(new byte[0]);
            } else {
                final InputStream responseBodyStream = httpEntity.getContent();
                final File outputFile = File.createTempFile(
                        "s2robot-HcHttpClient-", ".out");
                DeferredFileOutputStream dfos = null;
                try {
                    try {
                        dfos = new DeferredFileOutputStream(
                                responseBodyInMemoryThresholdSize, outputFile);
                        CopyUtil.copy(responseBodyStream, dfos);
                        dfos.flush();
                    } finally {
                        IOUtils.closeQuietly(dfos);
                    }
                } catch (final Exception e) {
                    if (!outputFile.delete()) {
                        logger.warn("Could not delete "
                                + outputFile.getAbsolutePath());
                    }
                    throw e;
                }

                if (dfos.isInMemory()) {
                    inputStream = new ByteArrayInputStream(dfos.getData());
                    contentLength = dfos.getData().length;
                    if (!outputFile.delete()) {
                        logger.warn("Could not delete "
                                + outputFile.getAbsolutePath());
                    }
                } else {
                    inputStream = new TemporaryFileInputStream(outputFile);
                    contentLength = outputFile.length();
                }

                final Header contentEncodingHeader = httpEntity
                        .getContentEncoding();
                if (contentEncodingHeader != null) {
                    contentEncoding = contentEncodingHeader.getValue();
                }
            }

            String contentType = null;
            final Header contentTypeHeader = response
                    .getFirstHeader("Content-Type");
            if (contentTypeHeader != null) {
                contentType = contentTypeHeader.getValue();
                final int idx = contentType.indexOf(';');
                if (idx > 0) {
                    contentType = contentType.substring(0, idx);
                }
            }

            // check file size
            if (contentLengthHelper != null) {
                final long maxLength = contentLengthHelper
                        .getMaxLength(contentType);
                if (contentLength > maxLength) {
                    throw new MaxLengthExceededException("The content length ("
                            + contentLength + " byte) is over " + maxLength
                            + " byte. The url is " + url);
                }
            }

            responseData = new ResponseData();
            responseData.setUrl(url);
            responseData.setCharSet(contentEncoding);
            if (httpRequest instanceof HttpHead) {
                responseData.setMethod(Constants.HEAD_METHOD);
            } else {
                responseData.setMethod(Constants.GET_METHOD);
            }
            responseData.setResponseBody(inputStream);
            responseData.setHttpStatusCode(httpStatusCode);
            for (final Header header : response.getAllHeaders()) {
                responseData.addMetaData(header.getName(), header.getValue());
            }
            if (contentType == null) {
                responseData.setMimeType(defaultMimeType);
            } else {
                responseData.setMimeType(contentType);
            }
            final Header contentLengthHeader = response
                    .getFirstHeader("Content-Length");
            if (contentLengthHeader == null) {
                responseData.setContentLength(contentLength);
            } else {
                final String value = contentLengthHeader.getValue();
                try {
                    responseData.setContentLength(Long.parseLong(value));
                } catch (final Exception e) {
                    responseData.setContentLength(contentLength);
                }
            }
            final Header lastModifiedHeader = response
                    .getFirstHeader("Last-Modified");
            if (lastModifiedHeader != null) {
                final String value = lastModifiedHeader.getValue();
                if (StringUtil.isNotBlank(value)) {
                    final Date d = parseLastModified(value);
                    if (d != null) {
                        responseData.setLastModified(d);
                    }
                }
            }

            return responseData;
        } catch (final UnknownHostException e) {
            httpRequest.abort();
            IOUtils.closeQuietly(inputStream);
            throw new RobotCrawlAccessException("Unknown host("
                    + e.getMessage() + "): " + url, e);
        } catch (final NoRouteToHostException e) {
            httpRequest.abort();
            IOUtils.closeQuietly(inputStream);
            throw new RobotCrawlAccessException("No route to host("
                    + e.getMessage() + "): " + url, e);
        } catch (final ConnectException e) {
            httpRequest.abort();
            IOUtils.closeQuietly(inputStream);
            throw new RobotCrawlAccessException("Connection time out("
                    + e.getMessage() + "): " + url, e);
        } catch (final SocketException e) {
            httpRequest.abort();
            IOUtils.closeQuietly(inputStream);
            throw new RobotCrawlAccessException("Socket exception("
                    + e.getMessage() + "): " + url, e);
        } catch (final IOException e) {
            httpRequest.abort();
            IOUtils.closeQuietly(inputStream);
            throw new RobotCrawlAccessException("I/O exception("
                    + e.getMessage() + "): " + url, e);
        } catch (final RobotSystemException e) {
            httpRequest.abort();
            IOUtils.closeQuietly(inputStream);
            throw e;
        } catch (final Exception e) {
            httpRequest.abort();
            IOUtils.closeQuietly(inputStream);
            throw new RobotSystemException("Failed to access " + url, e);
        } finally {
            EntityUtils.consumeQuietly(httpEntity);
        }
    }

    protected boolean isRedirectHttpStatus(final int httpStatusCode) {
        return redirectHttpStatusPattern.matcher(
                Integer.toString(httpStatusCode)).matches();
    }

    protected HttpResponse executeHttpClient(final HttpUriRequest httpRequest)
            throws IOException {
        return httpClient.execute(httpRequest, new BasicHttpContext(
                httpClientContext));
    }

    protected Date parseLastModified(final String value) {
        final SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        try {
            return sdf.parse(value);
        } catch (final ParseException e) {
            return null;
        }
    }

    protected HttpRoutePlanner buildRoutePlanner() {
        if (routePlanner != null) {
            return routePlanner;
        }

        // proxy
        final String proxyHost = getInitParameter(PROXY_HOST_PROPERTY, this.proxyHost);
        final Integer proxyPort = getInitParameter(PROXY_PORT_PROPERTY, this.proxyPort);
        if (proxyHost != null && proxyPort != null) {
            final HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            final DefaultProxyRoutePlanner defaultRoutePlanner = new DefaultProxyRoutePlanner(proxy);

            final Credentials credentials = getInitParameter(PROXY_CREDENTIALS_PROPERTY, proxyCredentials);
            if (credentials != null) {
                credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), credentials);
                final AuthScheme authScheme = getInitParameter(PROXY_AUTH_SCHEME_PROPERTY, proxyAuthScheme);
                if (authScheme != null) {
                    authCache.put(proxy, authScheme);
                }
            }
            return defaultRoutePlanner;
        }
        return null;
    }

    protected static class AccessTimeoutTarget implements TimeoutTarget {

        private static final int MAX_LOOP_COUNT = 10;

        protected Thread runninThread;

        protected AtomicBoolean running = new AtomicBoolean();

        protected AccessTimeoutTarget(final Thread thread) {
            runninThread = thread;
            running.set(true);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.seasar.extension.timer.TimeoutTarget#expired()
         */
        @Override
        public void expired() {
            int count = 0;
            while (running.get() && count < MAX_LOOP_COUNT) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Interrupt " + runninThread);
                }
                runninThread.interrupt();
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    // ignore
                }
                count++;
            }
        }

        public void stop() {
            running.set(false);
        }
    }

    public void setAccessTimeout(final Integer accessTimeout) {
        this.accessTimeout = accessTimeout;
    }

    public void setConnectionTimeout(final Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setMaxTotalConnections(final Integer maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public void setMaxConnectionsPerRoute(final Integer maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public void setStaleCheckingEnabled(final Boolean staleCheckingEnabled) {
        this.staleCheckingEnabled = staleCheckingEnabled;
    }

    public void setSoTimeout(final Integer soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setCookieSpec(final String cookieSpec) {
        this.cookieSpec = cookieSpec;
    }

    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(final Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyAuthScheme(final AuthScheme proxyAuthScheme) {
        this.proxyAuthScheme = proxyAuthScheme;
    }

    public void setProxyCredentials(final Credentials proxyCredentials) {
        this.proxyCredentials = proxyCredentials;
    }

    public void setResponseBodyInMemoryThresholdSize(
            final int responseBodyInMemoryThresholdSize) {
        this.responseBodyInMemoryThresholdSize = responseBodyInMemoryThresholdSize;
    }

    public void setDefaultMimeType(final String defaultMimeType) {
        this.defaultMimeType = defaultMimeType;
    }

    public void setCookieStore(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public void setHttpClientContext(final HttpClientContext httpClientContext) {
        this.httpClientContext = httpClientContext;
    }

    public void setClientConnectionManager(
            final HttpClientConnectionManager clientConnectionManager) {
        this.clientConnectionManager = clientConnectionManager;
    }

    public void setAuthSchemeProviderMap(
            final Map<String, AuthSchemeProvider> authSchemeProviderMap) {
        this.authSchemeProviderMap = authSchemeProviderMap;
    }

    public void setConnectionCheckInterval(final int connectionCheckInterval) {
        this.connectionCheckInterval = connectionCheckInterval;
    }

    public void setIdleConnectionTimeout(final long idleConnectionTimeout) {
        this.idleConnectionTimeout = idleConnectionTimeout;
    }

    public void setRedirectHttpStatusPattern(
            final Pattern redirectHttpStatusPattern) {
        this.redirectHttpStatusPattern = redirectHttpStatusPattern;
    }

    public void setUseRobotsTxtDisallows(final boolean useRobotsTxtDisallows) {
        this.useRobotsTxtDisallows = useRobotsTxtDisallows;
    }

    public void setUseRobotsTxtAllows(final boolean useRobotsTxtAllows) {
        this.useRobotsTxtAllows = useRobotsTxtAllows;
    }

    public void setCredentialsProvider(final CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public void setAuthCache(final AuthCache authCache) {
        this.authCache = authCache;
    }

    public void setRoutePlanner(final HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
    }
}
