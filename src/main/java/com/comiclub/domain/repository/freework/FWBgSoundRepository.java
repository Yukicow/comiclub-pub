package com.comiclub.domain.repository.freework;

import com.comiclub.domain.entity.freework.FWBackgroundSound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FWBgSoundRepository extends JpaRepository<FWBackgroundSound, Long> {


}
