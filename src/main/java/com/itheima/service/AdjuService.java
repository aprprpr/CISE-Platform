package com.itheima.service;

import com.itheima.domain.Adjudicator;
import com.itheima.domain.Judge;
import java.util.List;

public interface AdjuService {
    void addScore( Integer projectScore,
                  Integer studentId,
                   Integer projectId,
                   Integer adjuId
                   );
    List<Judge> queryAllNeedGradeAdju(Integer adjuId);
    Judge querySingleNeedGradeAdju(Integer studentId, Integer projectId, Integer adjuId);
    void addScore1(Integer projectScore, Integer studentId, Integer projectId, Integer adjuId);
    Integer addScore2(Integer studentId,Integer projectTotalScore);

    Adjudicator login(String adjudicatorName, String adjudicatorPassword);
    List<Adjudicator> getAll();
    void adjuDelById(Integer adjuId);
    int save(Adjudicator adjudicator);
}