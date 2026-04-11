package com.apple.chain.coldchain.service;

import com.apple.chain.coldchain.entity.PreCoolTask;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PreCoolTaskService extends IService<PreCoolTask> {

    IPage<PreCoolTask> listTasks(int page, int size, Long vehicleId, String status);

    PreCoolTask getTaskDetail(Long id);

    PreCoolTask createTask(PreCoolTask task);

    PreCoolTask updateTask(Long id, PreCoolTask task);

    void deleteTask(Long id);

    PreCoolTask start(Long id);

    PreCoolTask complete(Long id);
}
