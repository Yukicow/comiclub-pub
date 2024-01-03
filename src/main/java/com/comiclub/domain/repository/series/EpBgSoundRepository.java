package com.comiclub.domain.repository.series;

import com.comiclub.domain.entity.sereis.EpBackgroundSound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpBgSoundRepository extends JpaRepository<EpBackgroundSound, Long> {

}
