/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.robot.client.fs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.util.FileUtil;
import org.seasar.robot.Constants;
import org.seasar.robot.client.S2RobotClient;
import org.seasar.robot.entity.ResponseData;
import org.seasar.robot.helper.MimeTypeHelper;
import org.seasar.robot.util.TemporaryFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shinsuke
 *
 */
public class FileSystemClient implements S2RobotClient {
    private final Logger logger = LoggerFactory
            .getLogger(FileSystemClient.class);

    protected String charset;

    /* (non-Javadoc)
     * @see org.seasar.robot.client.S2RobotClient#doGet(java.lang.String)
     */
    public ResponseData doGet(String url) {
        ResponseData responseData = new ResponseData();
        responseData.setMethod(Constants.GET_METHOD);
        responseData.setUrl(url);
        if (url.startsWith("file:")) {
            try {
                URI uri = new URI(url);
                url = uri.getPath();
            } catch (URISyntaxException e) {
                logger.warn("Could not parse url: " + url, e);
            }
        }

        File file = new File(url);
        if (file.isFile()) {
            responseData.setHttpStatusCode(200);
            responseData.setCharSet(geCharSet(file));
            responseData.setContentLength(file.length());
            responseData.setLastModified(new Date(file.lastModified()));
            MimeTypeHelper mimeTypeHelper = SingletonS2Container
                    .getComponent("mimeTypeHelper");
            responseData.setMimeType(mimeTypeHelper.getContentType(file
                    .getName()));
            if (file.canRead()) {
                try {
                    File outputFile = File.createTempFile(
                            "s2robot-FileSystemClient-", ".out");
                    outputFile.deleteOnExit();
                    FileUtil.copy(file, outputFile);
                    responseData.setResponseBody(new TemporaryFileInputStream(
                            outputFile));
                } catch (Exception e) {
                    logger.warn("I/O Exception.", e);
                    responseData.setHttpStatusCode(500);
                }
            } else {
                // Forbidden
                responseData.setHttpStatusCode(403);
            }
        } else if (file.isDirectory()) {
            Set<String> childUrlSet = new HashSet<String>();
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    String path = f.getAbsolutePath();
                    StringBuilder buf = new StringBuilder(255);
                    buf.append("file://");
                    if (!path.startsWith("/")) {
                        buf.append('/');
                    }
                    buf.append(path);
                    childUrlSet.add(buf.toString());
                }
            }
            throw new ChildUrlsException(childUrlSet);
        } else {
            responseData.setHttpStatusCode(404);
            responseData.setCharSet(charset);
            responseData.setContentLength(0);
        }

        return responseData;
    }

    protected String geCharSet(File file) {
        return charset;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

}