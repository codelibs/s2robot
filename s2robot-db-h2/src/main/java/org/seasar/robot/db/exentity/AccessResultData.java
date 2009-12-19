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
package org.seasar.robot.db.exentity;

import java.io.UnsupportedEncodingException;

import org.seasar.framework.util.StringUtil;
import org.seasar.robot.Constants;
import org.seasar.robot.RobotSystemException;

/**
 * The entity of ACCESS_RESULT_DATA.
 * <p>
 * You can implement your original methods here.
 * This class remains when re-generating.
 * </p>
 * @author DBFlute(AutoGenerator)
 */
public class AccessResultData extends
        org.seasar.robot.db.bsentity.BsAccessResultData implements
        org.seasar.robot.entity.AccessResultData {

    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see org.seasar.robot.entity.AccessResultData#getDataAsString()
     */
    public String getDataAsString() {
        byte[] data = getData();
        if (data == null) {
            return null;
        }
        String encoding = getEncoding();
        try {
            return new String(data, StringUtil.isNotBlank(encoding) ? encoding
                    : Constants.UTF_8);
        } catch (UnsupportedEncodingException e) {
            try {
                return new String(data, Constants.UTF_8);
            } catch (UnsupportedEncodingException e1) {
                throw new RobotSystemException("Unexpected exception.", e1);
            }
        }
    }

}