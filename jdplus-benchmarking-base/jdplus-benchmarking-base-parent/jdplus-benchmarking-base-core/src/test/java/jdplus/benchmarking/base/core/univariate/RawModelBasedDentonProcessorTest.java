/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.RawModelBasedDentonSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class RawModelBasedDentonProcessorTest {

    public RawModelBasedDentonProcessorTest() {
    }

    @Test
    public void test1() {
        DoubleSeq y = DoubleSeq.of(Data.PCRA);
        DoubleSeq q = DoubleSeq.of(Data.IND_PCR);
        RawModelBasedDentonSpec spec = RawModelBasedDentonSpec.builder()
                .aggregationType(AggregationType.Average)
                .frequencyRatio(4)
                .build();
        RawModelBasedDentonResults rslts = RawModelBasedDentonProcessor.process(y, q, 4, spec);
        System.out.println(rslts.getBiRatios());
        System.out.println(rslts.getStdevBiRatios());
        System.out.println(rslts.getDisaggregatedSeries());

    }

    @Test
    public void test2() {
        double[] yArr = {500, 510, 525, 520};
        double[] xArr = {97, 98, 98.5, 99.5, 104,
            99, 100, 100.5, 101, 105.5,
            103, 104.5, 103.5, 104.5, 109,
            104, 107, 103, 108, 113,
            110, 112, 116};
        DoubleSeq y = DoubleSeq.of(yArr);
        DoubleSeq x = DoubleSeq.of(xArr);
        RawModelBasedDentonSpec spec = RawModelBasedDentonSpec.builder()
                .aggregationType(AggregationType.Sum)
                .frequencyRatio(5)
                .build();

        RawModelBasedDentonResults rslts = RawModelBasedDentonProcessor.process(y, x, 0, spec);
        System.out.println(rslts.getBiRatios());
        System.out.println(rslts.getStdevBiRatios());
        System.out.println(rslts.getDisaggregatedSeries());
    }
}
