package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.Orchard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Orchard mapper.
 */
@Mapper
public interface OrchardMapper extends BaseMapper<Orchard> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(orchard_no, 12) AS SIGNED)), 0) + 1 " +
            "FROM farm_orchard WHERE orchard_no LIKE CONCAT('ORD', #{prefix}, '%')")
    int nextSeq(String prefix);

    /**
     * M4 GIS: viewport (bounding-box) query.
     * Returns orchards whose centroid falls inside the rectangle.
     * Index used: idx_farm_orchard_center.
     */
    @Select("SELECT * FROM farm_orchard " +
            "WHERE deleted = 0 " +
            "  AND center_lat IS NOT NULL " +
            "  AND center_lng IS NOT NULL " +
            "  AND center_lat BETWEEN #{lat1} AND #{lat2} " +
            "  AND center_lng BETWEEN #{lng1} AND #{lng2} " +
            "ORDER BY id DESC LIMIT 500")
    List<Orchard> findInBoundingBox(@Param("lng1") double lng1,
                                    @Param("lat1") double lat1,
                                    @Param("lng2") double lng2,
                                    @Param("lat2") double lat2);
}
