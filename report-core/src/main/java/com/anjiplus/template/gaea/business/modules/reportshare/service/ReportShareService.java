
package com.anjiplus.template.gaea.business.modules.reportshare.service;

import com.anji.plus.gaea.curd.service.GaeaBaseService;
import com.anjiplus.template.gaea.business.modules.reportshare.controller.dto.ReportShareDto;
import com.anjiplus.template.gaea.business.modules.reportshare.controller.param.ReportShareParam;
import com.anjiplus.template.gaea.business.modules.reportshare.dao.entity.ReportShare;

/**
* @desc ReportShare 报表分享服务接口
* @author Raod
* @date 2021-08-18 13:37:26.663
**/
public interface ReportShareService extends GaeaBaseService<ReportShareParam, ReportShare> {

    /***
     * 查询详情
     *
     * @param id
     * @return
     */
    ReportShare getDetail(Long id);

    ReportShare insertShare(ReportShareDto dto);

    ReportShare detailByCode(String shareCode);
}