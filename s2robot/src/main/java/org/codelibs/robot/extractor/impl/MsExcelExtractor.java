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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codelibs.robot.entity.ExtractData;
import org.codelibs.robot.exception.ExtractException;
import org.codelibs.robot.exception.RobotSystemException;
import org.codelibs.robot.extractor.Extractor;

/**
 * Gets a text from .xls file.
 *
 * @author shinsuke
 *
 */
public class MsExcelExtractor implements Extractor {
    /*
     * (non-Javadoc)
     *
     * @see
     * org.codelibs.robot.extractor.impl.Extractor#getText(java.io.InputStream,
     * java.util.Map)
     */
    @Override
    public ExtractData getText(final InputStream in,
            final Map<String, String> params) {
        if (in == null) {
            throw new RobotSystemException("The inputstream is null.");
        }
        try {
            return new ExtractData(
                    new org.apache.poi.hssf.extractor.ExcelExtractor(
                            new HSSFWorkbook(in)).getText());
        } catch (final IOException e) {
            throw new ExtractException(e);
        }
    }
}
