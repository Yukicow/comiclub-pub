package com.comiclub.database;


import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;


@SpringBootTest
@Transactional
public class JpaTes {

    @Autowired
    private EntityManager em;


    @Test
    @Rollback(value = false)
    public void test(){


    }

    @Test
    public int[] solution(int[] ints) {
        LinkedHashMap
    }
}
