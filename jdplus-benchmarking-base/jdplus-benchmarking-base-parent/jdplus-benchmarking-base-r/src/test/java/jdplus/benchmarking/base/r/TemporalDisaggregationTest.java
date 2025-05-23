/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.r;

import jdplus.benchmarking.base.core.univariate.ModelBasedDentonResults;
import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationResults;
import tck.demetra.data.Data;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class TemporalDisaggregationTest {

    public TemporalDisaggregationTest() {
    }

    @Test
    public void testChowLin() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, true, false, new TsData[]{q}, "Ar1", 0, 0, "Sum", 0, 0, false, 0, false, "Diffuse", false);
//        System.out.println(rslt.getData("disagg", TsData.class));
    }

    @Test
    public void testLitterman() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, new TsData[]{q}, "RwAr1", 0, 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testFernandez() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, new TsData[]{q}, "Rw", 0, 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testFernandez2() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, null, "Rw", 4, 8, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testLiiterman2() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, null, "RwAr1", 4, 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testChowLinRaw() {
        double[] y = Data.PCRA;
        FastMatrix x = FastMatrix.make(Data.IND_PCR.length, 1);
        x.column(0).add(DoubleSeq.of(Data.IND_PCR));
        RawTemporalDisaggregationResults rslt = TemporalDisaggregation.processRaw(y, false, false, x, 0, "Ar1", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
        //System.out.println(rslt.getDisaggregatedSeries());   
    }

    @Test
    public void testChowLinRawWithoutIndicator() {
        double[] y = Data.PCRA;
        //     RawDisaggregationResults rslt = TemporalDisaggregation.processRaw(y, false, false, null, 0, "Ar1", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
        //System.out.println(rslt.getDisaggregatedSeries());

        double[] y2Arr = {500, 510, 525, 520};
//        RawDisaggregationResults rslt2 = TemporalDisaggregation.processRaw(y2Arr, false, false, null, 0, "Rw", 5, "Sum", 0, 0, false, 0, false, "SqrtDiffuse", false);
//        System.out.println(rslt.getRegressionEffects().toArray().length);
    }
    
    @Test
    public void testModelBasedDenton() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        ModelBasedDentonResults rslt = TemporalDisaggregation.processModelBasedDenton(y, q, 1, "Sum", 0, null, null, null, null);
//        System.out.println(rslt.getLikelihood().toString());
//        System.out.println(rslt.getLikelihood().getObservationsCount());
//        System.out.println(rslt.getLikelihood().getEffectiveObservationsCount());
//        System.out.println(rslt.getData("disagg", TsData.class));
    }
    
    @Test
    public void testRawInterpolation() {
        double[] y = Data.IND_PCR;
        RawTemporalDisaggregationResults rslt = TemporalDisaggregation.processRawInterpolation(y, true, false, "Ar1", 3, 1, 0, false, 0, false, "SqrtDiffuse", false,1,1);     
//        System.out.println(rslt.getDisaggregatedSeries().toString());
    }
    
    @Test
    public void testInterpolationRwWithoutIndicator() {
        double[] yArr = {500, 510, 525, 520};
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(yArr));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.processInterpolation(y, false, false, "Rw", 12, -1, 0, false, 0, false, "SqrtDiffuse", false,0,6);
        TemporalDisaggregationResults rslt2 = TemporalDisaggregation.processDisaggregation(y, false, false, "Rw", 12, false, 0, false, 0, false, "SqrtDiffuse", false,0,6);
//        System.out.println(rslt.getRegressionEffects().toString());
//        System.out.println(rslt2.getRegressionEffects().toString());
//        System.out.println(rslt.getDisaggregatedSeries().toString());
    }
}
