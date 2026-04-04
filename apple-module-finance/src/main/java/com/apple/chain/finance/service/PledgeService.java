package com.apple.chain.finance.service;

import com.apple.chain.finance.entity.Pledge;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

public interface PledgeService extends IService<Pledge> {
    IPage<Pledge> listPledges(int page, int size, String keyword, String status);
    Pledge getPledgeDetail(Long id);
    Pledge createPledge(Pledge pledge);
    Pledge updatePledge(Long id, Pledge pledge);
    Pledge activate(Long id);
    Pledge release(Long id);
    void deletePledge(Long id);
    void exportPledges(String keyword, String status, HttpServletResponse response);
}
