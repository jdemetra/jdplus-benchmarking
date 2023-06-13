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
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.ADLSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author palatej
 */
public class ADLProcessorTest {

    public ADLProcessorTest() {
    }

    @Test
    public void testChowLin() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, ADLSpec.CHOWLIN);
        
//        System.out.print(rslts.getDisaggregatedSeries());
//        System.out.print(rslts.getStdevDisaggregatedSeries());
//        System.out.println(rslts.getLikelihood());
    }

    @Test
    public void testFernandez() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, ADLSpec.FERNANDEZ);
//        System.out.print(rslts.getDisaggregatedSeries());
//        System.out.print(rslts.getStdevDisaggregatedSeries());
    }

    @Test
    public void testADL11() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978), Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1), Data.IND_PCR);
        ADLResults rslts = ADLProcessor.process(y, new TsData[]{q}, ADLSpec.ADL_11);
//        System.out.print(rslts.getDisaggregatedSeries());
//        System.out.print(rslts.getStdevDisaggregatedSeries());
    }
}
