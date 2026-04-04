package com.apple.chain.finance.mapper;

import com.apple.chain.finance.entity.Loan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoanMapper extends BaseMapper<Loan> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(loan_code, 11) AS UNSIGNED)), 0) + 1 " +
            "FROM sf_loan WHERE loan_code LIKE CONCAT('LN', #{prefix}, '%')")
    int nextSeq(String prefix);
}
