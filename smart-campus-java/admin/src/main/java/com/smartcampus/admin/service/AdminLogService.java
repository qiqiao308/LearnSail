package com.smartcampus.admin.service;

import com.smartcampus.common.dto.PageDTO;
import com.smartcampus.common.po.SystemLog;
import com.smartcampus.common.vo.PageVO;

public interface AdminLogService {

    PageVO<SystemLog> loadLogList(PageDTO pageDTO);
}
