package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdDataAsset;
import com.apple.chain.bigdata.entity.BdDataAssetField;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/** Data asset catalog (table + field metadata). */
public interface BdDataAssetService extends IService<BdDataAsset> {

    List<BdDataAssetField> listFields(Long assetId);

    BdDataAssetField addField(BdDataAssetField field);

    boolean removeField(Long fieldId);
}
