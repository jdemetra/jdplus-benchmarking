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

import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationResults;
import tck.demetra.data.Data;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import jdplus.toolkit.base.api.data.Doubles;

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
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, true, false, new TsData[]{q}, "Ar1", 0, "Sum", 0, 0, false, 0, false, "Diffuse", false);
        System.out.println(rslt.getData("disagg", TsData.class));
    }

    @Test
    public void testLitterman() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, new TsData[]{q}, "RwAr1", 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testFernandez() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, new TsData[]{q}, "Rw", 0, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testFernandez2() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, null, "Rw", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testLiiterman2() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TemporalDisaggregationResults rslt = TemporalDisaggregation.process(y, false, false, null, "RwAr1", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }
    
    @Test
    public void testChowLinRaw() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), Doubles.of(Data.IND_PCR));
        RawTemporalDisaggregationResults rslt = TemporalDisaggregation.processRaw(y, false, false, new TsData[]{q}, "Ar1", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }

    @Test
    public void testChowLinRawWithoutIndicator() {
        TsData y = TsData.of(TsPeriod.yearly(1977), Doubles.of(Data.PCRA));
        RawTemporalDisaggregationResults rslt = TemporalDisaggregation.processRaw(y, false, false, null, "Ar1", 4, "Sum", 0, 0, false, 0, false, "Augmented", false);
    }
}
