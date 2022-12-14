package com.anjiplus.template.gaea.business.modules.reportexcel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.anji.plus.gaea.constant.BaseOperationEnum;
import com.anji.plus.gaea.curd.mapper.GaeaBaseMapper;
import com.anji.plus.gaea.exception.BusinessException;
import com.anji.plus.gaea.exception.BusinessExceptionBuilder;
import com.anji.plus.gaea.utils.GaeaAssert;
import com.anji.plus.gaea.utils.GaeaBeanUtils;
import com.anjiplus.template.gaea.business.code.ResponseCode;
import com.anjiplus.template.gaea.business.enums.ExportTypeEnum;
import com.anjiplus.template.gaea.business.modules.dataset.controller.dto.DataSetDto;
import com.anjiplus.template.gaea.business.modules.dataset.controller.dto.OriginalDataDto;
import com.anjiplus.template.gaea.business.modules.dataset.service.DataSetService;
import com.anjiplus.template.gaea.business.modules.file.service.GaeaFileService;
import com.anjiplus.template.gaea.business.modules.report.dao.ReportMapper;
import com.anjiplus.template.gaea.business.modules.report.dao.entity.Report;
import com.anjiplus.template.gaea.business.modules.reportexcel.controller.dto.ReportExcelDto;
import com.anjiplus.template.gaea.business.modules.reportexcel.dao.ReportExcelMapper;
import com.anjiplus.template.gaea.business.modules.reportexcel.dao.entity.ReportExcel;
import com.anjiplus.template.gaea.business.modules.reportexcel.service.ReportExcelService;
import com.anjiplus.template.gaea.business.modules.reportexcel.util.CellType;
import com.anjiplus.template.gaea.business.modules.reportexcel.util.XlsUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author chenkening
 * @date 2021/4/13 15:14
 */
@Service
public class ReportExcelServiceImpl implements ReportExcelService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ReportExcelMapper reportExcelMapper;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private GaeaFileService gaeaFileService;


    @Autowired
    private ReportMapper reportMapper;

    @Value("${customer.file.tmp-path:.}")
    private String dictPath;

    private final static String ZIP_PATH = "/tmp_zip/";


    @Override
    public GaeaBaseMapper<ReportExcel> getMapper() {
        return reportExcelMapper;
    }

    @Override
    public ReportExcelDto detailByReportCode(String reportCode) {
        QueryWrapper<ReportExcel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_code", reportCode);
        ReportExcel reportExcel = reportExcelMapper.selectOne(queryWrapper);
        if (reportExcel != null) {
            ReportExcelDto dto = new ReportExcelDto();
            BeanUtils.copyProperties(reportExcel, dto);
            return dto;
        }
        return null;
    }

    /**
     * ???????????????
     *
     * @param entity        ?????????????????????
     * @param operationEnum ????????????
     * @throws BusinessException ???????????????????????????????????????
     */
    @Override
    public void processBeforeOperation(ReportExcel entity, BaseOperationEnum operationEnum) throws BusinessException {
        if (operationEnum.equals(BaseOperationEnum.INSERT)) {
            String reportCode = entity.getReportCode();
            ReportExcel report = this.selectOne("report_code", reportCode);
            if (null != report) {
                this.deleteById(report.getId());
            }
        }
    }

    /**
     * ????????????
     */
    @Override
    public ReportExcelDto preview(ReportExcelDto reportExcelDto) {
        // ??????id?????? ????????????
        ReportExcel reportExcel = selectOne("report_code", reportExcelDto.getReportCode());
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_code", reportExcelDto.getReportCode());
        Report report = reportMapper.selectOne(queryWrapper);
        GaeaAssert.notNull(reportExcel, ResponseCode.RULE_CONTENT_NOT_EXIST, "reportExcel");
        String setParam = reportExcelDto.getSetParam();

        GaeaBeanUtils.copyAndFormatter(reportExcel, reportExcelDto);
        if (StringUtils.isNotBlank(setParam)) {
            reportExcelDto.setSetParam(setParam);
        }
        reportExcelDto.setReportName(report.getReportName());
        // ???????????????
        String jsonStr = analysisReportData(reportExcelDto);
        reportExcelDto.setJsonStr(jsonStr);
//        reportExcelDto.setTotal(jsonObject.getJSONObject("rows").size());
        return reportExcelDto;
    }

    @Override
    public Boolean exportExcel(ReportExcelDto reportExcelDto) {
        String reportCode = reportExcelDto.getReportCode();
        String exportType = reportExcelDto.getExportType();
        logger.error("??????...");
        if (exportType.equals(ExportTypeEnum.GAEA_TEMPLATE_EXCEL.getCodeValue())) {
            ReportExcelDto report = detailByReportCode(reportCode);
            reportExcelDto.setJsonStr(report.getJsonStr());
            String jsonStr = analysisReportData(reportExcelDto);
            List<JSONObject> lists=(List<JSONObject> ) JSON.parse(jsonStr);
            OutputStream out = null;
            File file = null;
            try {
                String fileName = report.getReportCode();
                File dir = new File(dictPath + ZIP_PATH);
                if (!dir.exists()){
                    dir.mkdirs();
                }
                String filePath = dir.getAbsolutePath() + File.separator + fileName + ".xlsx";
                file = new File(filePath);
                out = Files.newOutputStream(Paths.get(filePath));
                XlsUtil.exportXlsFile(out, true, lists);
                gaeaFileService.upload(file);

            } catch (IOException e) {
                logger.error("????????????", e);
            }finally {
                try {
                    out.close();
                    file.delete();
                } catch (IOException e) {
                    throw BusinessExceptionBuilder.build(ResponseCode.FILE_OPERATION_FAILED, e.getMessage());
                }

            }
        }
        return true;
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    private String analysisReportData(ReportExcelDto reportExcelDto) {

        String jsonStr = reportExcelDto.getJsonStr();
        String setParam = reportExcelDto.getSetParam();
        List<JSONObject> dbObjectList = (List<JSONObject>) JSON.parse(jsonStr);

        if (dbObjectList != null && dbObjectList.size() > 0) {
            for (int x = 0; x < dbObjectList.size(); x++) {
                analysisSheetCellData(dbObjectList.get(x), setParam);
            }
        }
        //fastjson $ref ????????????
        return JSONObject.toJSONString(dbObjectList, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * ?????????sheet data
     *
     * @param dbObject
     */
    private void analysisSheet(JSONObject dbObject, String setParma) {
        //data?????????????????????
        if (dbObject.containsKey("data") && null != dbObject.get("data")) {
            List<JSONArray> data = (List<JSONArray>) dbObject.get("data");


            //???
            for (int r = 0; r < data.size(); r++) {
                JSONArray jsonArray = data.get(r);
                //???
                for (int c = 0; c < jsonArray.size(); c++) {
                    //?????????
                    JSONObject cell = jsonArray.getJSONObject(c);
                    if (null != cell && cell.containsKey("v") && StringUtils.isNotBlank(cell.getString("v"))) {
                        String v = cell.getString("v");
                        DataSetDto dataSet = getDataSet(v, setParma);
                        if (null != dataSet) {
                            OriginalDataDto originalDataDto = dataSetService.getData(dataSet);
                            if (null != originalDataDto.getData()) {
                                if (originalDataDto.getData().size() == 1) {
                                    //??????
                                    JSONObject jsonObject = originalDataDto.getData().get(0);
                                    String fieldLabel = jsonObject.getString(dataSet.getFieldLabel());

                                    String replace = v.replace("#{".concat(dataSet.getSetCode()).concat(".").concat(dataSet.getFieldLabel()).concat("}"), fieldLabel);
                                    dbObject.getJSONArray("data").getJSONArray(r).getJSONObject(c).put("v", replace);
                                    dbObject.getJSONArray("data").getJSONArray(r).getJSONObject(c).put("m", replace);

                                } else {
                                    //??????
                                    JSONObject jsonObject = originalDataDto.getData().get(0);
                                    String fieldLabel = jsonObject.getString(dataSet.getFieldLabel());

                                    String replace = v.replace("#{".concat(dataSet.getSetCode()).concat(".").concat(dataSet.getFieldLabel()).concat("}"), fieldLabel);
                                    dbObject.getJSONArray("data").getJSONArray(r).getJSONObject(c).put("v", replace);
                                    dbObject.getJSONArray("data").getJSONArray(r).getJSONObject(c).put("m", replace);
                                }
                            }

                        }
                    }



                }
            }


            System.out.println("aaaa");


        }


    }

    /**
     * ?????????sheet celldata
     *
     * @param dbObject
     */
    private void analysisSheetCellData(JSONObject dbObject, String setParam) {
        //??????data???
        dbObject.remove("data");
        //celldata?????????????????????
        if (dbObject.containsKey("celldata") && null != dbObject.get("celldata")) {
            List<JSONObject> celldata = new ArrayList<>();
            celldata.addAll((List<JSONObject>) dbObject.get("celldata"));

            //??????celldata??????????????????map?????????????????????????????????????????????????????????cell??????
            Map<String,JSONObject> cellDataMap = cellDataList2Map(celldata);
            //?????????????????????
            dbObject.getJSONArray("celldata").clear();
            //?????????????????????????????????
            JSONObject merge = dbObject.getJSONObject("config").getJSONObject("merge");
            if(merge != null) merge.clear();
            //??????????????????????????????????????????
            Map<Integer,Integer> colAddCntMap = new HashMap<>();
            // ????????????????????????????????????????????????????????????
            for (int i = 0; i < celldata.size(); i++) {
                //???????????????
                JSONObject cellObj = celldata.get(i);
                //fastjson???????????????
                String cellStr = cellObj.toJSONString();
                analysisCellData(cellObj,setParam,colAddCntMap,cellStr,merge, dbObject,cellDataMap);
            }
        }
    }

    /**
     * ????????????????????? cellData
     * @param cellObject
     */
    public void analysisCellData(JSONObject cellObject,String setParam,Map<Integer,Integer> colAddCntMap,String cellStr,
                                 JSONObject merge,JSONObject dbObject,Map<String,JSONObject> cellDataMap){
        //????????????
        Integer cellR = cellObject.getInteger("r");
        //????????????
        Integer cellC = cellObject.getInteger("c");
        //????????????????????????????????????????????????0???
        int cnt = colAddCntMap.get(cellC) == null ? 0 : colAddCntMap.get(cellC);
        //?????????????????????
        CellType cellType = getCellType(cellObject);
        switch (cellType){
            case BLACK:
                //???????????????????????????
                break;
            case DYNAMIC_MERGE:
            case DYNAMIC:
                //?????????????????????
                String v = cellObject.getJSONObject("v").getString("v");
                DataSetDto dataSet = getDataSet(v, setParam);
                handleDynamicCellObject(dataSet,v,cellStr,cnt,cellR,cellC,merge,dbObject,colAddCntMap);
                break;
            default:
                //?????????????????????
                handleStaticCellObject(cellStr,dbObject,cnt,cellR,cellC,cellDataMap,setParam,merge,colAddCntMap,cellType);
                break;
        }
    }

    /**
     * ???????????????????????????????????????
     * @param dataSet
     * @param v
     * @param cellStr
     * @param cnt
     * @param r
     * @param c
     * @param merge
     * @param dbObject
     * @param colAddCntMap
     */
    public void handleDynamicCellObject(DataSetDto dataSet,String v,String cellStr,int cnt,int r,int c,
                                        JSONObject merge,JSONObject dbObject,Map<Integer,Integer> colAddCntMap){
        //??????????????????
        OriginalDataDto originalDataDto = dataSetService.getData(dataSet);
        List<JSONObject> cellDynamicData = originalDataDto.getData();

        if(cellDynamicData != null){
            //??????????????????
            for (int j = 0; j < cellDynamicData.size(); j++) {
                //??????????????????
                JSONObject addCell = cellDynamicData.get(j);
                //??????
                String fieldLabel = addCell.getString(dataSet.getFieldLabel());
                if (StringUtils.isBlank(fieldLabel)) {
                    fieldLabel = StringUtils.EMPTY;
                }
                String replace = v.replace("#{".concat(dataSet.getSetCode()).concat(".").concat(dataSet.getFieldLabel()).concat("}"), fieldLabel);
                //????????????????????????????????????
                JSONObject addCellData = JSONObject.parseObject(cellStr);

                addCellData.put("r",  cnt + r + j); //????????????
                addCellData.put("c", c);
                addCellData.getJSONObject("v").put("v", replace);
                addCellData.getJSONObject("v").put("m", replace);
                JSONObject cellMc = addCellData.getJSONObject("v").getJSONObject("mc");
                //??????????????????????????????
                if(null != cellMc){
                    //?????????????????????
                    Integer rs = cellMc.getInteger("rs");
                    cellMc.put("r",  cnt + r + rs*j); //????????????
                    cellMc.put("c", c);
                    addCellData.put("r",  cnt + r + rs*j);
                    //???????????????????????????config.merge
                    merge.put(cellMc.getString("r")+"_"+cellMc.getString("c"),cellMc);
                    //???????????????????????????????????????????????????
                    colAddCntMap.put(c,cnt + rs * cellDynamicData.size() - 1);
                }else{
                    //???????????????????????????????????????????????????
                    colAddCntMap.put(c,cnt + cellDynamicData.size() - 1);
                }
                dbObject.getJSONArray("celldata").add(addCellData);
            }
        }
    }

    /**
     * ???????????????????????????
     * @param cellStr
     * @param dbObject
     * @param cnt
     * @param r
     * @param c
     * @param cellDataMap
     * @param setParam
     * @param merge
     * @param colAddCntMap
     * @param cellType
     */
    public void handleStaticCellObject(String cellStr,JSONObject dbObject,int cnt,int r,int c,
                                       Map<String,JSONObject> cellDataMap,String setParam,
                                       JSONObject merge,Map<Integer,Integer> colAddCntMap,CellType cellType){
        //????????????????????????????????????
        JSONObject addCellData = JSONObject.parseObject(cellStr);
        int rows = 0;
        switch(cellType){
            case STATIC:
            case STATIC_MERGE:
                //?????????????????????????????????????????????????????????
                initCellPosition(addCellData,cnt,merge);
                break;
            case STATIC_AUTO:
                //??????????????????????????????????????????????????????
                rows = getRightDynamicCellRows(addCellData,cellDataMap,setParam,rows,cellType);
                initCellPosition(addCellData,cnt,merge);
                if(rows > 1){
                    //???????????????????????????????????? ????????? ?????????????????????????????????????????????????????? mc ??????merge??????
                    JSONObject mc = new JSONObject();
                    mc.put("rs",rows);
                    mc.put("cs",1);
                    mc.put("r",addCellData.getIntValue("r"));
                    mc.put("c",addCellData.getIntValue("c"));
                    addCellData.getJSONObject("v").put("mc",mc);
                    //???????????????????????????config.merge
                    merge.put((mc.getInteger("r")) + "_" + mc.getString("c"),mc);
                    //???????????????????????????????????????????????????
                    colAddCntMap.put(c,cnt + rows - 1);
                }
                break;
            case STATIC_MERGE_AUTO:
                //??????????????????????????????????????????????????????
                rows = getRightDynamicCellRows(addCellData,cellDataMap,setParam,rows,cellType);
                initCellPosition(addCellData,cnt,merge);
                if(rows > 0){
                    //?????????????????????mc??????rs
                    JSONObject cellMc = addCellData.getJSONObject("v").getJSONObject("mc");
                    int addCnt = cellMc.getInteger("rs");
                    cellMc.put("rs",rows);
                    //???????????????????????????config.merge
                    merge.put((cellMc.getInteger("r")) + "_" + cellMc.getString("c"),cellMc);
                    //???????????????????????????????????????????????????
                    colAddCntMap.put(c,cnt + rows - addCnt);
                }
                break;
        }
        dbObject.getJSONArray("celldata").add(addCellData);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     * @param addCellData
     * @param cnt
     * @param merge
     */
    public void initCellPosition(JSONObject addCellData,int cnt,JSONObject merge){
        addCellData.put("r", cnt + addCellData.getInteger("r"));//????????????
        //????????????????????????
        JSONObject mc = addCellData.getJSONObject("v").getJSONObject("mc");
        if(mc != null){
            mc.put("r",addCellData.getInteger("r"));
            initCellMerge(merge,mc);
        }
    }

    /**
     * ???????????????????????????????????????
     * @param merge
     * @param mc
     */
    public void initCellMerge(JSONObject merge,JSONObject mc){
        merge.put((mc.getInteger("r"))+"_"+mc.getString("c"),mc);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     * @param addCellData
     * @param cellDataMap
     * @param setParam
     * @param sumRows
     * @param cellType
     * @return
     */
    public int getRightDynamicCellRows(JSONObject addCellData,Map<String,JSONObject> cellDataMap,String setParam,int sumRows,CellType cellType){
        //1???????????????????????????????????????????????????
        List<JSONObject> rightCellList = getRightDynamicCell(addCellData,cellDataMap,cellType);
        //2?????????????????????????????????????????????
        for (JSONObject rightCell : rightCellList) {
            //??????????????????????????????????????????????????????????????????
            CellType rightCellType = getCellType(rightCell);
            switch (rightCellType){
                case STATIC_AUTO:
                case STATIC_MERGE_AUTO:
                    //????????????
                    sumRows = getRightDynamicCellRows(rightCell,cellDataMap,setParam,sumRows,rightCellType);
                    break;
                case BLACK:
                case STATIC:
                    sumRows++;
                    break;
                case STATIC_MERGE:
                    sumRows += rightCell.getJSONObject("v").getJSONObject("mc").getInteger("rs");
                    break;
                default:
                    List<JSONObject> cellDynamicData = getDynamicDataList(rightCell.getJSONObject("v").getString("v"),setParam);
                    if(cellDynamicData != null && cellDynamicData.size() > 1){
                        int size = cellDynamicData.size();
                        sumRows += size;
                    }else{
                        sumRows++;
                    }
                    break;
            }
        }
        return sumRows;
    }

    /**
     * ?????????????????????????????????????????????????????????
     * @param addCellData
     * @param cellDataMap
     * @param cellType
     * @return
     */
    public List<JSONObject> getRightDynamicCell(JSONObject addCellData,Map<String,JSONObject> cellDataMap,CellType cellType){
        //?????????????????????????????????????????????????????????????????????????????????
        //1??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //????????????????????? 2???5????????????????????? ??????2?????????3?????????????????????????????????????????????
        //?????????2???8 ??? 3???8
        Integer cellR = addCellData.getInteger("r");
        Integer cellC = addCellData.getInteger("c");
        Integer cellRs = 0;
        Integer cellCs = 0;
        switch (cellType){
            case STATIC_AUTO:
                cellRs = 1;
                cellCs = 1;
                break;
            case STATIC_MERGE_AUTO:
                cellRs = addCellData.getJSONObject("v").getJSONObject("mc").getInteger("rs");
                cellCs = addCellData.getJSONObject("v").getJSONObject("mc").getInteger("cs");
                break;
        }
        List<JSONObject> rightCells = new ArrayList<>();
        for(int nums = 0;nums < cellRs;nums++){
            int r = cellR + nums;
            int c = cellC + cellCs;
            String key = r + "," + c;
            if(cellDataMap.containsKey(key)){
                JSONObject cellData = cellDataMap.get(r + "," + c);
                rightCells.add(cellData);
            }
        }
        return rightCells;
    }

    /**
     * ?????????????????????
     * @param cellObject
     * @return
     */
    public CellType getCellType(JSONObject cellObject){
        JSONObject cellV1 = cellObject.getJSONObject("v");
        if (null != cellV1 && cellV1.containsKey("v") && StringUtils.isNotBlank(cellV1.getString("v"))) {
            String cellV2 = cellObject.getJSONObject("v").getString("v");
            String auto = cellObject.getJSONObject("v").getString("auto");
            JSONObject mc = cellObject.getJSONObject("v").getJSONObject("mc");
            if(cellV2.contains("#{") && cellV2.contains("}") ){
                //???????????????
                if(mc != null){
                    return CellType.DYNAMIC_MERGE;
                }else{
                    return CellType.DYNAMIC;
                }
            }else{
                //???????????????
                if(mc != null && "1".equals(auto)){
                    return CellType.STATIC_MERGE_AUTO;
                }else {
                    if("1".equals(auto)){
                        return CellType.STATIC_AUTO;
                    }else if(mc != null){
                        return CellType.STATIC_MERGE;
                    }else{
                        return CellType.STATIC;
                    }
                }
            }
        }else{
            return CellType.BLACK;
        }
    }

    /**
     * list??????map??????????????????????????????????????????cell??????
     * @param cellDataList
     * @return
     */
    public Map<String,JSONObject> cellDataList2Map(List<JSONObject> cellDataList){
        Map<String,JSONObject> cellDataMap = new HashMap<>();
        for (JSONObject cellData : cellDataList) {
            String r = cellData.getString("r");
            String c = cellData.getString("c");
            cellDataMap.put(r + "," + c, cellData);
        }
        return cellDataMap;
    }

    /**
     * ?????? #{xxxx.xxxxx} ??????
     * @param v
     * @return
     */
    private DataSetDto getDataSet(String v, String setParam) {

        DataSetDto dto = new DataSetDto();
        if (v.contains("#{") && v.contains("}")) {
            int start = v.indexOf("#{") + 2;
            int end = v.indexOf("}");
            if (start < end) {
                String substring = v.substring(start, end);
                if (substring.contains(".")) {
                    String[] split = substring.split("\\.");
                    dto.setSetCode( split[0]);
                    dto.setFieldLabel(split[1]);
                    getContextData(setParam, dto);
                    return dto;
                }
            }
        }
        return null;
    }

    /**
     * ???????????????????????????????????????
     * @param v
     * @param setParam
     * @return
     */
    private List<JSONObject> getDynamicDataList(String v, String setParam){
        if(StringUtils.isNotBlank(v)){
            DataSetDto dataSet = getDataSet(v,setParam);
            if(dataSet != null){
                OriginalDataDto originalDataDto = dataSetService.getData(dataSet);
                List<JSONObject> cellDynamicData = originalDataDto.getData();
                return cellDynamicData;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    /**
     * ??????????????????
     * @param setParam
     * @param dto
     */
    private void getContextData(String setParam, DataSetDto dto) {
        if (StringUtils.isNotBlank(setParam)) {
            JSONObject setParamJson = JSONObject.parseObject(setParam);
            Map<String, Object> map = new HashMap<>();
            // ????????????
            if (setParamJson.containsKey(dto.getSetCode())) {
                JSONObject paramCondition = setParamJson.getJSONObject(dto.getSetCode());
                paramCondition.forEach(map::put);
            }
            dto.setContextData(map);
        }
    }

}
