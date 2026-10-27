package com.apple.chain.coldchain.dto;

import com.apple.chain.coldchain.entity.Delivery;
import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.entity.Vehicle;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogisticsFullVO {

    private TransportTask task;
    private Vehicle vehicle;
    private List<TemperatureRecord> tempRecords;
    private Delivery delivery;
    private long alarmCount;
}
