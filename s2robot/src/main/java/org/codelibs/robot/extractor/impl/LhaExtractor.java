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
package org.codelibs.robot.extractor.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.codelibs.core.io.CopyUtil;
import org.codelibs.robot.container.RobotContainer;
import org.codelibs.robot.entity.ExtractData;
import org.codelibs.robot.exception.ExtractException;
import org.codelibs.robot.exception.RobotSystemException;
import org.codelibs.robot.extractor.Extractor;
import org.codelibs.robot.extractor.ExtractorFactory;
import org.codelibs.robot.helper.MimeTypeHelper;
import org.codelibs.robot.util.IgnoreCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.gr.java_conf.dangan.util.lha.LhaFile;
import jp.gr.java_conf.dangan.util.lha.LhaHeader;

/**
 * Extractor implementation for LHA.
 *
 * @author shinsuke
 *
 */
public class LhaExtractor implements Extractor {
    private static final Logger logger = LoggerFactory // NOPMD
            .getLogger(LhaExtractor.class);

    @Resource
    protected RobotContainer robotContainer;

    @Override
    public ExtractData getText(final InputStream in,
            final Map<String, String> params) {
        if (in == null) {
            throw new RobotSystemException("The inputstream is null.");
        }

        final MimeTypeHelper mimeTypeHelper = robotContainer
                .getComponent("mimeTypeHelper");
        if (mimeTypeHelper == null) {
            throw new RobotSystemException("MimeTypeHelper is unavailable.");
        }

        final ExtractorFactory extractorFactory = robotContainer
                .getComponent("extractorFactory");
        if (extractorFactory == null) {
            throw new RobotSystemException("ExtractorFactory is unavailable.");
        }

        final StringBuilder buf = new StringBuilder(1000);

        File tempFile = null;
        LhaFile lhaFile = null;
        try {
            tempFile = File.createTempFile("s2robot-", ".lzh");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                CopyUtil.copy(in, fos);
            }

            lhaFile = new LhaFile(tempFile);
            @SuppressWarnings("unchecked")
            final Enumeration<LhaHeader> entries = lhaFile.entries();
            while (entries.hasMoreElements()) {
                final LhaHeader head = entries.nextElement();
                final String filename = head.getPath();
                final String mimeType = mimeTypeHelper.getContentType(null,
                        filename);
                if (mimeType != null) {
                    final Extractor extractor = extractorFactory
                            .getExtractor(mimeType);
                    if (extractor != null) {
                        InputStream is = null;
                        try {
                            is = lhaFile.getInputStream(head);
                            final Map<String, String> map = new HashMap<String, String>();
                            map.put(TikaMetadataKeys.RESOURCE_NAME_KEY,
                                    filename);
                            buf.append(extractor.getText(
                                    new IgnoreCloseInputStream(is), map)
                                    .getContent());
                            buf.append('\n');
                        } catch (final Exception e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Exception in an internal extractor.",
                                        e);
                            }
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new ExtractException("Could not extract a content.", e);
        } finally {
            if (lhaFile != null) {
                try {
                    lhaFile.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
            if (tempFile != null && !tempFile.delete()) {
                logger.warn("Failed to delete " + tempFile.getAbsolutePath());
            }
        }

        return new ExtractData(buf.toString());
    }
}
