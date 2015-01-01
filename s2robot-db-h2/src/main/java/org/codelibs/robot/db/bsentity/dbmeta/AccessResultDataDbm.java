package org.codelibs.robot.db.bsentity.dbmeta;

import java.util.List;
import java.util.Map;

import org.codelibs.robot.db.allcommon.DBCurrent;
import org.codelibs.robot.db.allcommon.DBFluteConfig;
import org.codelibs.robot.db.exentity.AccessResult;
import org.codelibs.robot.db.exentity.AccessResultData;
import org.dbflute.Entity;
import org.dbflute.dbmeta.AbstractDBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.ForeignInfo;
import org.dbflute.dbmeta.info.UniqueInfo;
import org.dbflute.dbmeta.name.TableSqlName;
import org.dbflute.dbmeta.property.PropertyGateway;
import org.dbflute.dbway.DBDef;
import org.dbflute.optional.OptionalEntity;

/**
 * The DB meta of ACCESS_RESULT_DATA. (Singleton)
 * @author DBFlute(AutoGenerator)
 */
public class AccessResultDataDbm extends AbstractDBMeta {

    // ===================================================================================
    //                                                                           Singleton
    //                                                                           =========
    private static final AccessResultDataDbm _instance = new AccessResultDataDbm();

    private AccessResultDataDbm() {
    }

    public static AccessResultDataDbm getInstance() {
        return _instance;
    }

    // ===================================================================================
    //                                                                       Current DBDef
    //                                                                       =============
    @Override
    public String getProjectName() {
        return DBCurrent.getInstance().projectName();
    }

    @Override
    public String getProjectPrefix() {
        return DBCurrent.getInstance().projectPrefix();
    }

    @Override
    public String getGenerationGapBasePrefix() {
        return DBCurrent.getInstance().generationGapBasePrefix();
    }

    @Override
    public DBDef getCurrentDBDef() {
        return DBCurrent.getInstance().currentDBDef();
    }

    // ===================================================================================
    //                                                                    Property Gateway
    //                                                                    ================
    // -----------------------------------------------------
    //                                       Column Property
    //                                       ---------------
    protected final Map<String, PropertyGateway> _epgMap = newHashMap();
    {
        xsetupEpg();
    }

    protected void xsetupEpg() {
        setupEpg(_epgMap, et -> ((AccessResultData) et).getId(),
                (et, vl) -> ((AccessResultData) et).setId(ctl(vl)), "id");
        setupEpg(_epgMap, et -> ((AccessResultData) et).getTransformerName(),
                (et, vl) -> ((AccessResultData) et)
                        .setTransformerName((String) vl), "transformerName");
        setupEpg(_epgMap, et -> ((AccessResultData) et).getData(),
                (et, vl) -> ((AccessResultData) et).setData((byte[]) vl),
                "data");
        setupEpg(_epgMap, et -> ((AccessResultData) et).getEncoding(),
                (et, vl) -> ((AccessResultData) et).setEncoding((String) vl),
                "encoding");
    }

    @Override
    public PropertyGateway findPropertyGateway(final String prop) {
        return doFindEpg(_epgMap, prop);
    }

    // -----------------------------------------------------
    //                                      Foreign Property
    //                                      ----------------
    protected final Map<String, PropertyGateway> _efpgMap = newHashMap();
    {
        xsetupEfpg();
    }

    @SuppressWarnings("unchecked")
    protected void xsetupEfpg() {
        setupEfpg(_efpgMap, et -> ((AccessResultData) et).getAccessResult(),
                (et, vl) -> ((AccessResultData) et)
                        .setAccessResult((OptionalEntity<AccessResult>) vl),
                "accessResult");
    }

    @Override
    public PropertyGateway findForeignPropertyGateway(final String prop) {
        return doFindEfpg(_efpgMap, prop);
    }

    // ===================================================================================
    //                                                                          Table Info
    //                                                                          ==========
    protected final String _tableDbName = "ACCESS_RESULT_DATA";

    protected final String _tablePropertyName = "accessResultData";

    protected final TableSqlName _tableSqlName = new TableSqlName(
            "ACCESS_RESULT_DATA", _tableDbName);
    {
        _tableSqlName.xacceptFilter(DBFluteConfig.getInstance()
                .getTableSqlNameFilter());
    }

    @Override
    public String getTableDbName() {
        return _tableDbName;
    }

    @Override
    public String getTablePropertyName() {
        return _tablePropertyName;
    }

    @Override
    public TableSqlName getTableSqlName() {
        return _tableSqlName;
    }

    // ===================================================================================
    //                                                                         Column Info
    //                                                                         ===========
    protected final ColumnInfo _columnId = cci("ID", "ID", null, null,
            Long.class, "id", null, true, false, true, "BIGINT", 19, 0, null,
            false, null, null, "accessResult", null, null, false);

    protected final ColumnInfo _columnTransformerName = cci("TRANSFORMER_NAME",
            "TRANSFORMER_NAME", null, null, String.class, "transformerName",
            null, false, false, true, "VARCHAR", 255, 0, null, false, null,
            null, null, null, null, false);

    protected final ColumnInfo _columnData = cci("DATA", "DATA", null, null,
            byte[].class, "data", null, false, false, false, "BLOB",
            2147483647, 0, null, false, null, null, null, null, null, false);

    protected final ColumnInfo _columnEncoding = cci("ENCODING", "ENCODING",
            null, null, String.class, "encoding", null, false, false, false,
            "VARCHAR", 20, 0, null, false, null, null, null, null, null, false);

    /**
     * ID: {PK, NotNull, BIGINT(19), FK to ACCESS_RESULT}
     * @return The information object of specified column. (NotNull)
     */
    public ColumnInfo columnId() {
        return _columnId;
    }

    /**
     * TRANSFORMER_NAME: {NotNull, VARCHAR(255)}
     * @return The information object of specified column. (NotNull)
     */
    public ColumnInfo columnTransformerName() {
        return _columnTransformerName;
    }

    /**
     * DATA: {BLOB(2147483647)}
     * @return The information object of specified column. (NotNull)
     */
    public ColumnInfo columnData() {
        return _columnData;
    }

    /**
     * ENCODING: {VARCHAR(20)}
     * @return The information object of specified column. (NotNull)
     */
    public ColumnInfo columnEncoding() {
        return _columnEncoding;
    }

    @Override
    protected List<ColumnInfo> ccil() {
        final List<ColumnInfo> ls = newArrayList();
        ls.add(columnId());
        ls.add(columnTransformerName());
        ls.add(columnData());
        ls.add(columnEncoding());
        return ls;
    }

    {
        initializeInformationResource();
    }

    // ===================================================================================
    //                                                                         Unique Info
    //                                                                         ===========
    // -----------------------------------------------------
    //                                       Primary Element
    //                                       ---------------
    @Override
    protected UniqueInfo cpui() {
        return hpcpui(columnId());
    }

    @Override
    public boolean hasPrimaryKey() {
        return true;
    }

    @Override
    public boolean hasCompoundPrimaryKey() {
        return false;
    }

    // ===================================================================================
    //                                                                       Relation Info
    //                                                                       =============
    // cannot cache because it uses related DB meta instance while booting
    // (instead, cached by super's collection)
    // -----------------------------------------------------
    //                                      Foreign Property
    //                                      ----------------
    /**
     * ACCESS_RESULT by my ID, named 'accessResult'.
     * @return The information object of foreign property. (NotNull)
     */
    public ForeignInfo foreignAccessResult() {
        final Map<ColumnInfo, ColumnInfo> mp = newLinkedHashMap(columnId(),
                AccessResultDbm.getInstance().columnId());
        return cfi("CONSTRAINT_13", "accessResult", this,
                AccessResultDbm.getInstance(), mp, 0,
                org.dbflute.optional.OptionalEntity.class, true, false, false,
                false, null, null, false, "accessResultDataAsOne", false);
    }

    // -----------------------------------------------------
    //                                     Referrer Property
    //                                     -----------------

    // ===================================================================================
    //                                                                        Various Info
    //                                                                        ============

    // ===================================================================================
    //                                                                           Type Name
    //                                                                           =========
    @Override
    public String getEntityTypeName() {
        return "org.codelibs.robot.db.exentity.AccessResultData";
    }

    @Override
    public String getConditionBeanTypeName() {
        return "org.codelibs.robot.db.cbean.AccessResultDataCB";
    }

    @Override
    public String getBehaviorTypeName() {
        return "org.codelibs.robot.db.exbhv.AccessResultDataBhv";
    }

    // ===================================================================================
    //                                                                         Object Type
    //                                                                         ===========
    @Override
    public Class<AccessResultData> getEntityType() {
        return AccessResultData.class;
    }

    // ===================================================================================
    //                                                                     Object Instance
    //                                                                     ===============
    @Override
    public AccessResultData newEntity() {
        return new AccessResultData();
    }

    // ===================================================================================
    //                                                                   Map Communication
    //                                                                   =================
    @Override
    public void acceptPrimaryKeyMap(final Entity et,
            final Map<String, ? extends Object> mp) {
        doAcceptPrimaryKeyMap((AccessResultData) et, mp);
    }

    @Override
    public void acceptAllColumnMap(final Entity et,
            final Map<String, ? extends Object> mp) {
        doAcceptAllColumnMap((AccessResultData) et, mp);
    }

    @Override
    public Map<String, Object> extractPrimaryKeyMap(final Entity et) {
        return doExtractPrimaryKeyMap(et);
    }

    @Override
    public Map<String, Object> extractAllColumnMap(final Entity et) {
        return doExtractAllColumnMap(et);
    }
}