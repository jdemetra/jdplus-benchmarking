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

import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinResults;
import jdplus.benchmarking.base.core.univariate.ADLResults;
import jdplus.benchmarking.base.core.univariate.ModelBasedDentonResults;
import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationResults;
import jdplus.benchmarking.base.r.util.DictionaryGroups;
import jdplus.toolkit.base.r.util.Dictionary;
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
    public void testADL() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        ADLResults rslt = TemporalDisaggregation.processADL(y, true, false, new TsData[]{q}, "Sum", 0, false, 0, "FREE", "TRANSITION", false);
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
    public void testLitterman2() {
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

    @Test
    public void testmultivariateChowLin() {

        double[] Y1Arr = {30.0,30.6,31.2,31.6};
        TsData Y1 = TsData.ofInternal(TsPeriod.yearly(2021), Y1Arr);
        double[] Y2Arr = {80.0,81.2,82.5,82.6};
        TsData Y2 = TsData.ofInternal(TsPeriod.yearly(2021), Y2Arr);
        double[] Y3Arr = {8.0,8.1,8.2,8.2};
        TsData Y3 = TsData.ofInternal(TsPeriod.yearly(2021), Y3Arr);

        double[] z1Arr = {27.1,29.8,29.9,31.2,29.4,27.9,30.9,31.7,29.2,30.2,30.6,31.9,29.3,30.4,30.7,32.0};
        TsData z1 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), z1Arr);

        double[] x11Arr = {7,7.2,8.1,7.5,8.5,7.8,8.1,8.4,8.6,7.8,8.0,8.3,8.7,7.9,8.0,8.6};
        double[] x12Arr = {18,19.5,19.0,19.7,18.5,19.0,20.3,20.0,18.6,19.5,20.4,20.1,18.7,19.1,20.4,20.8};
        TsData x11 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), x11Arr);
        TsData x12 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), x12Arr);
        TsData x2 = null;
        double[] x31Arr = {1.5,1.8,2,2.5,2.0,1.5,1.7,2.1,2.1,1.6,1.6,2.2,2.3,1.7,1.9,2.3};
        TsData x3 = TsData.ofInternal(TsPeriod.quarterly(2021, 1), x31Arr);

        boolean[] constant = {false, false, true};
        boolean[] trend = {false, false, false};
        String[] ccdefinition = new String[]{"z1=y1+y2+y3"};
        double[] rhos = {0.85,1.0,0.9};
        FastMatrix errVariance = null;

        Dictionary series = new Dictionary();
        series.add("y1", Y1);
        series.add("y2", Y2);
        series.add("y3", Y3);

        DictionaryGroups indicators = new DictionaryGroups();
        indicators.add("y1", x11);
        indicators.add("y1", x12);
        indicators.add("y2", x2);
        indicators.add("y3", x3);

        Dictionary ccseries = new Dictionary();
        ccseries.add("z1", z1);

        MultivariateChowLinResults rslt = TemporalDisaggregation.multiChowLin(series, constant, trend,
                indicators, ccseries, ccdefinition, 4, rhos, "fromUnivariate", null);

        System.out.println(rslt.getDisaggregatedSeries().get("y1"));
    }
}
