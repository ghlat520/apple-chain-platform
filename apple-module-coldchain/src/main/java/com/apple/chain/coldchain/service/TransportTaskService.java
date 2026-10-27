package com.apple.chain.coldchain.service;

import com.apple.chain.coldchain.entity.TransportTask;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

public interface TransportTaskService extends IService<TransportTask> {

    IPage<TransportTask> listTasks(int page, int size, String keyword, String status);

    TransportTask getTaskDetail(Long id);

    TransportTask createTask(TransportTask task);

    TransportTask updateTask(Long id, TransportTask task);

    void deleteTask(Long id);

    TransportTask depart(Long id);

    TransportTask deliver(Long id);

    TransportTask cancelTask(Long id);

    void exportTasks(String keyword, String status, HttpServletResponse response);
}
