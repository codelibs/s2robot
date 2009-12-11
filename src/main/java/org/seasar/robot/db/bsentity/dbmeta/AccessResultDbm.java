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
package org.seasar.robot.db.bsentity.dbmeta;

import java.util.List;
import java.util.Map;

import org.seasar.robot.dbflute.Entity;
import org.seasar.robot.dbflute.dbmeta.AbstractDBMeta;
import org.seasar.robot.dbflute.dbmeta.info.*;
import org.seasar.robot.dbflute.helper.StringKeyMap;
import org.seasar.robot.db.exentity.AccessResult;

/**
 * The DB meta of ACCESS_RESULT. (Singleton)
 * @author DBFlute(AutoGenerator)
 */
public class AccessResultDbm extends AbstractDBMeta {

    // ===================================================================================
    //                                                                           Singleton
    //                                                                           =========
    private static final AccessResultDbm _instance = new AccessResultDbm();
    private AccessResultDbm() {}
    public static AccessResultDbm getInstance() { return _instance; }

    // ===================================================================================
    //                                                                          Table Info
    //                                                                          ==========
    public String getTableDbName() { return "ACCESS_RESULT"; }
    public String getTablePropertyName() { return "accessResult"; }
    public String getTableSqlName() { return "ACCESS_RESULT"; }

    // ===================================================================================
    //                                                                         Column Info
    //                                                                         ===========
    protected ColumnInfo _columnId = cci("ID", null, "id", Long.class, true, true, 19, 0, false, null);
    protected ColumnInfo _columnSessionId = cci("SESSION_ID", null, "sessionId", String.class, false, false, 20, 0, false, null);
    protected ColumnInfo _columnRuleId = cci("RULE_ID", null, "ruleId", String.class, false, false, 20, 0, false, null);
    protected ColumnInfo _columnUrl = cci("URL", null, "url", String.class, false, false, 65535, 0, false, null);
    protected ColumnInfo _columnParentUrl = cci("PARENT_URL", null, "parentUrl", String.class, false, false, 65535, 0, false, null);
    protected ColumnInfo _columnStatus = cci("STATUS", null, "status", Integer.class, false, false, 10, 0, false, null);
    protected ColumnInfo _columnHttpStatusCode = cci("HTTP_STATUS_CODE", null, "httpStatusCode", Integer.class, false, false, 10, 0, false, null);
    protected ColumnInfo _columnMethod = cci("METHOD", null, "method", String.class, false, false, 10, 0, false, null);
    protected ColumnInfo _columnMimeType = cci("MIME_TYPE", null, "mimeType", String.class, false, false, 100, 0, false, null);
    protected ColumnInfo _columnContentLength = cci("CONTENT_LENGTH", null, "contentLength", Long.class, false, false, 19, 0, false, null);
    protected ColumnInfo _columnExecutionTime = cci("EXECUTION_TIME", null, "executionTime", Integer.class, false, false, 10, 0, false, null);
    protected ColumnInfo _columnLastModified = cci("LAST_MODIFIED", null, "lastModified", java.sql.Timestamp.class, false, false, 19, 0, false, null);
    protected ColumnInfo _columnCreateTime = cci("CREATE_TIME", null, "createTime", java.sql.Timestamp.class, false, false, 19, 0, false, null);

    public ColumnInfo columnId() { return _columnId; }
    public ColumnInfo columnSessionId() { return _columnSessionId; }
    public ColumnInfo columnRuleId() { return _columnRuleId; }
    public ColumnInfo columnUrl() { return _columnUrl; }
    public ColumnInfo columnParentUrl() { return _columnParentUrl; }
    public ColumnInfo columnStatus() { return _columnStatus; }
    public ColumnInfo columnHttpStatusCode() { return _columnHttpStatusCode; }
    public ColumnInfo columnMethod() { return _columnMethod; }
    public ColumnInfo columnMimeType() { return _columnMimeType; }
    public ColumnInfo columnContentLength() { return _columnContentLength; }
    public ColumnInfo columnExecutionTime() { return _columnExecutionTime; }
    public ColumnInfo columnLastModified() { return _columnLastModified; }
    public ColumnInfo columnCreateTime() { return _columnCreateTime; }

    protected List<ColumnInfo> ccil() {
        List<ColumnInfo> ls = newArrayList();
        ls.add(columnId());
        ls.add(columnSessionId());
        ls.add(columnRuleId());
        ls.add(columnUrl());
        ls.add(columnParentUrl());
        ls.add(columnStatus());
        ls.add(columnHttpStatusCode());
        ls.add(columnMethod());
        ls.add(columnMimeType());
        ls.add(columnContentLength());
        ls.add(columnExecutionTime());
        ls.add(columnLastModified());
        ls.add(columnCreateTime());
        return ls;
    }

    { initializeInformationResource(); }

    // ===================================================================================
    //                                                                         Unique Info
    //                                                                         ===========
    // -----------------------------------------------------
    //                                       Primary Element
    //                                       ---------------
    public UniqueInfo getPrimaryUniqueInfo() { return cpui(columnId()); }
    public boolean hasPrimaryKey() { return true; }
    public boolean hasTwoOrMorePrimaryKeys() { return false; }

    // ===================================================================================
    //                                                                       Relation Info
    //                                                                       =============
    // -----------------------------------------------------
    //                                      Foreign Property
    //                                      ----------------
    public ForeignInfo foreignAccessResultDataAsOne() {
        Map<ColumnInfo, ColumnInfo> map = newLinkedHashMap(columnId(), AccessResultDataDbm.getInstance().columnId());
        return cfi("accessResultDataAsOne", this, AccessResultDataDbm.getInstance(), map, 0, true);
    }

    // -----------------------------------------------------
    //                                     Referrer Property
    //                                     -----------------

    // ===================================================================================
    //                                                                        Various Info
    //                                                                        ============
    public boolean hasIdentity() { return true; }

    // ===================================================================================
    //                                                                           Type Name
    //                                                                           =========
    public String getEntityTypeName() { return "org.seasar.robot.db.exentity.AccessResult"; }
    public String getConditionBeanTypeName() { return "org.seasar.robot.db.cbean.bs.AccessResultCB"; }
    public String getDaoTypeName() { return "org.seasar.robot.db.exdao.AccessResultDao"; }
    public String getBehaviorTypeName() { return "org.seasar.robot.db.exbhv.AccessResultBhv"; }

    // ===================================================================================
    //                                                                         Object Type
    //                                                                         ===========
    public Class<AccessResult> getEntityType() { return AccessResult.class; }

    // ===================================================================================
    //                                                                     Object Instance
    //                                                                     ===============
    public Entity newEntity() { return newMyEntity(); }
    public AccessResult newMyEntity() { return new AccessResult(); }

    // ===================================================================================
    //                                                                     Entity Handling
    //                                                                     ===============  
    // -----------------------------------------------------
    //                                                Accept
    //                                                ------
    public void acceptPrimaryKeyMap(Entity entity, Map<String, ? extends Object> primaryKeyMap)
    { doAcceptPrimaryKeyMap((AccessResult)entity, primaryKeyMap, _epsMap); }
    public void acceptPrimaryKeyMapString(Entity entity, String primaryKeyMapString)
    { MapStringUtil.acceptPrimaryKeyMapString(primaryKeyMapString, entity); }
    public void acceptColumnValueMap(Entity entity, Map<String, ? extends Object> columnValueMap)
    { doAcceptColumnValueMap((AccessResult)entity, columnValueMap, _epsMap); }
    public void acceptColumnValueMapString(Entity entity, String columnValueMapString)
    { MapStringUtil.acceptColumnValueMapString(columnValueMapString, entity); }

    // -----------------------------------------------------
    //                                               Extract
    //                                               -------
    public String extractPrimaryKeyMapString(Entity entity) { return MapStringUtil.extractPrimaryKeyMapString(entity); }
    public String extractPrimaryKeyMapString(Entity entity, String startBrace, String endBrace, String delimiter, String equal)
    { return doExtractPrimaryKeyMapString(entity, startBrace, endBrace, delimiter, equal); }
    public String extractColumnValueMapString(Entity entity) { return MapStringUtil.extractColumnValueMapString(entity); }
    public String extractColumnValueMapString(Entity entity, String startBrace, String endBrace, String delimiter, String equal)
    { return doExtractColumnValueMapString(entity, startBrace, endBrace, delimiter, equal); }

    // -----------------------------------------------------
    //                                               Convert
    //                                               -------
    public List<Object> convertToColumnValueList(Entity entity) { return newArrayList(convertToColumnValueMap(entity).values()); }
    public Map<String, Object> convertToColumnValueMap(Entity entity) { return doConvertToColumnValueMap(entity); }
    public List<String> convertToColumnStringValueList(Entity entity) { return newArrayList(convertToColumnStringValueMap(entity).values()); }
    public Map<String, String> convertToColumnStringValueMap(Entity entity) { return doConvertToColumnStringValueMap(entity); }

    // ===================================================================================
    //                                                               Entity Property Setup
    //                                                               =====================
    // It's very INTERNAL!
    protected final Map<String, Eps<AccessResult>> _epsMap = StringKeyMap.createAsFlexibleConcurrent();
    {
        setupEps(_epsMap, new EpsId(), columnId());
        setupEps(_epsMap, new EpsSessionId(), columnSessionId());
        setupEps(_epsMap, new EpsRuleId(), columnRuleId());
        setupEps(_epsMap, new EpsUrl(), columnUrl());
        setupEps(_epsMap, new EpsParentUrl(), columnParentUrl());
        setupEps(_epsMap, new EpsStatus(), columnStatus());
        setupEps(_epsMap, new EpsHttpStatusCode(), columnHttpStatusCode());
        setupEps(_epsMap, new EpsMethod(), columnMethod());
        setupEps(_epsMap, new EpsMimeType(), columnMimeType());
        setupEps(_epsMap, new EpsContentLength(), columnContentLength());
        setupEps(_epsMap, new EpsExecutionTime(), columnExecutionTime());
        setupEps(_epsMap, new EpsLastModified(), columnLastModified());
        setupEps(_epsMap, new EpsCreateTime(), columnCreateTime());
    }

    public boolean hasEntityPropertySetupper(String propertyName) { return _epsMap.containsKey(propertyName); }
    public void setupEntityProperty(String propertyName, Object entity, Object value)
    { findEps(_epsMap, propertyName).setup((AccessResult)entity, value); }

    public static class EpsId implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setId((Long)v); } }
    public static class EpsSessionId implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setSessionId((String)v); } }
    public static class EpsRuleId implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setRuleId((String)v); } }
    public static class EpsUrl implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setUrl((String)v); } }
    public static class EpsParentUrl implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setParentUrl((String)v); } }
    public static class EpsStatus implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setStatus((Integer)v); } }
    public static class EpsHttpStatusCode implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setHttpStatusCode((Integer)v); } }
    public static class EpsMethod implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setMethod((String)v); } }
    public static class EpsMimeType implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setMimeType((String)v); } }
    public static class EpsContentLength implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setContentLength((Long)v); } }
    public static class EpsExecutionTime implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setExecutionTime((Integer)v); } }
    public static class EpsLastModified implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setLastModified((java.sql.Timestamp)v); } }
    public static class EpsCreateTime implements Eps<AccessResult>
    { public void setup(AccessResult e, Object v) { e.setCreateTime((java.sql.Timestamp)v); } }
}
