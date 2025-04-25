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

import jdplus.toolkit.base.api.data.AggregationType;
import tck.demetra.data.Data;
import jdplus.benchmarking.base.api.univariate.ModelBasedDentonSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class ModelBasedDentonProcessorTest {
    
    public ModelBasedDentonProcessorTest() {
    }

    @Test
    public void testSomeMethod() {
        TsData y = TsData.ofInternal(TsPeriod.yearly(1978),  Data.PCRA);
        TsData q = TsData.ofInternal(TsPeriod.quarterly(1977, 1),  Data.IND_PCR);
        ModelBasedDentonSpec spec=ModelBasedDentonSpec.builder()
                .aggregationType(AggregationType.Average)
                .build();
        ModelBasedDentonResults rslts = ModelBasedDentonProcessor.process(y, q, spec);
        System.out.println(rslts.getBiRatios().getValues());
        System.out.println(rslts.getStdevBiRatios().getValues());
        System.out.println(rslts.getDisaggregatedSeries().getValues());
        
    }
    
}
