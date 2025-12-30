package com.octal.fsm.repositories;

import com.octal.fsm.entities.DrawingToolAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DrawingToolAssetRepository extends JpaRepository<DrawingToolAsset, Long>, JpaSpecificationExecutor<DrawingToolAsset> {


}
