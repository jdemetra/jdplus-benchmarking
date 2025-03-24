/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.benchmarking.base.core.benchmarking.extractors;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.benchmarking.base.core.univariate.RawResidualsDiagnostics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author LEMASSO
 */
@ServiceProvider(InformationExtractor.class)
public class RawResidualsDiagnosticsExtractor extends InformationMapping<RawResidualsDiagnostics> {
    
    public final String FRES = "fullresiduals", MEAN = "mean", SKEWNESS = "skewness",
            KURTOSIS = "kurtosis", DH = "doornikhansen", LJUNGBOX = "ljungbox",
            DW = "durbinwatson", UDRUNS_NUMBER = "nudruns", UDRUNS_LENGTH = "ludruns",
            RUNS_NUMBER = "nruns", RUNS_LENGTH = "lruns";

    public RawResidualsDiagnosticsExtractor() {
        set(FRES, double[].class, source -> source.getFullResiduals().toArray());
        set(MEAN, StatisticalTest.class, source -> source.getNiid().meanTest());
        set(SKEWNESS, StatisticalTest.class, source -> source.getNiid().skewness());
        set(KURTOSIS, StatisticalTest.class, source -> source.getNiid().kurtosis());
        set(DH, StatisticalTest.class, source -> source.getNiid().normalityTest());
        set(LJUNGBOX, StatisticalTest.class, source -> source.getNiid().ljungBox());
        set(RUNS_NUMBER, StatisticalTest.class, source -> source.getNiid().runsNumber());
        set(RUNS_LENGTH, StatisticalTest.class, source -> source.getNiid().runsLength());
        set(UDRUNS_NUMBER, StatisticalTest.class, source -> source.getNiid().upAndDownRunsNumbber());
        set(UDRUNS_LENGTH, StatisticalTest.class, source -> source.getNiid().upAndDownRunsLength());
    }

    @Override
    public Class getSourceClass() {
        return RawResidualsDiagnostics.class;
    }
}
