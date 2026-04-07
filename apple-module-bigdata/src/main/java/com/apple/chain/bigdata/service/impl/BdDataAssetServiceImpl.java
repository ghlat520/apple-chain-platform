package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdDataAsset;
import com.apple.chain.bigdata.entity.BdDataAssetField;
import com.apple.chain.bigdata.mapper.BdDataAssetFieldMapper;
import com.apple.chain.bigdata.mapper.BdDataAssetMapper;
import com.apple.chain.bigdata.service.BdDataAssetService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BdDataAssetServiceImpl
        extends ServiceImpl<BdDataAssetMapper, BdDataAsset>
        implements BdDataAssetService {

    private final BdDataAssetFieldMapper fieldMapper;

    @Override
    public List<BdDataAssetField> listFields(Long assetId) {
        return fieldMapper.selectList(
                new LambdaQueryWrapper<BdDataAssetField>()
                        .eq(BdDataAssetField::getAssetId, assetId)
                        .orderByAsc(BdDataAssetField::getOrdinal));
    }

    @Override
    public BdDataAssetField addField(BdDataAssetField field) {
        fieldMapper.insert(field);
        return field;
    }

    @Override
    public boolean removeField(Long fieldId) {
        return fieldMapper.deleteById(fieldId) > 0;
    }
}
