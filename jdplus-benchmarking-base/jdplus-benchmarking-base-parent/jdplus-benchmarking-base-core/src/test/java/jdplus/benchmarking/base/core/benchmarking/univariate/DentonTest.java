/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.benchmarking.base.core.benchmarking.univariate;

import jdplus.benchmarking.base.api.benchmarking.univariate.Denton;
import jdplus.benchmarking.base.api.benchmarking.univariate.DentonSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class DentonTest {

    public DentonTest() {

    }

    @Test
    public void test1() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(90);
        x.set(i -> (1 + i) * (1 + i));

        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .multiplicative(false)
                .build();
        TsPeriod q = TsPeriod.quarterly(1978, 3);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(q, x);
        TsData b = Denton.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }

    @Test
    public void test2() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));

        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .differencing(3)
                .build();
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.ofInternal(a, y.toArray());
        TsData b = Denton.benchmark(TsUnit.of(4, ChronoUnit.MONTHS), t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(x -> Math.abs(x) < 1e-9));
    }
    
    @Test
    public void test3() {
        
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(230);
        x.set(i -> (1 + i) * (1 + i));
        
        DentonSpec spec = DentonSpec.builder()
                .modified(true)
                .multiplicative(true)
                .build();
        TsPeriod m = TsPeriod.monthly(1980, 1);
        TsPeriod a = TsPeriod.yearly(1980);
        TsData t = TsData.of(a, y);
        TsData s = TsData.of(m, x);
        TsData b = Denton.benchmark(s, t, spec);
        TsData bc = b.aggregate(TsUnit.YEAR, AggregationType.Sum, true);
        assertTrue(TsDataToolkit.subtract(t, bc).getValues().allMatch(w -> Math.abs(w) < 1e-9));
    }
}
