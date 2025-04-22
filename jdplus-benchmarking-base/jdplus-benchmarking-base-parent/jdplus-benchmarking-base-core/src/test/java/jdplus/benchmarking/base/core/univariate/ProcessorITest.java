/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.toolkit.base.api.data.AggregationType;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationISpec;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class ProcessorITest {

    public ProcessorITest() {
    }

    @Test
    public void testQ() {
        TemporalDisaggregationISpec speci = TemporalDisaggregationISpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .truncatedRho(-1)
                .constant(true)
                .build();
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);

        TemporalDisaggregationIResults rslti = ProcessorI.process(y, q, speci);
//        System.out.println(rslti.getDisaggregatedSeries());
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.CHOWLIN;

        TemporalDisaggregationResults rslt = TemporalDisaggregationProcessor.process(y, new TsData[]{q}, spec);
//        System.out.println(rslt.getDisaggregatedSeries());
    }

    @Test
    public void testQ2() {
        TemporalDisaggregationISpec speci = TemporalDisaggregationISpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .constant(true)
                .truncatedRho(.5)
                .parameter(Parameter.initial(.6))
                .build();
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);

        TemporalDisaggregationIResults rslti = ProcessorI.process(y, q, speci);
//        System.out.println(rslti.getDisaggregatedSeries());
    }

    @Test
    public void testQ3() {
        TemporalDisaggregationISpec speci = TemporalDisaggregationISpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                .constant(true)
                .parameter(Parameter.fixed(.6))
                .build();
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);

        TemporalDisaggregationIResults rslti = ProcessorI.process(y, q, speci);
//        System.out.println(rslti.getDisaggregatedSeries());
    }
}
