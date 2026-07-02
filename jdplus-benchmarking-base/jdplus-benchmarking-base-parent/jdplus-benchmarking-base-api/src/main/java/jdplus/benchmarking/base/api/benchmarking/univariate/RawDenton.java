/*
 * Copyright 2025 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.benchmarking.base.api.benchmarking.univariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.design.Algorithm;
import nbbrd.design.Development;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author LEMASSO
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class RawDenton {

    private final AtomicReference<RawDenton.Processor> PROCESSOR = new AtomicReference<>(RawDentonLoader.Processor.load());

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public double[] benchmark(DoubleSeq highFreqSeries, DoubleSeq aggregationConstraint, int startOffset, RawDentonSpec spec) {
        return PROCESSOR.get().benchmark(highFreqSeries, aggregationConstraint, startOffset, spec);
    }

    public double[] benchmark(DoubleSeq aggregationConstraint, RawDentonSpec spec) {
        return PROCESSOR.get().benchmark(aggregationConstraint, spec);
    }

    @Algorithm
    @SuppressWarnings("SingleFallbackNotExpected")
    @ServiceDefinition(quantifier = Quantifier.SINGLE)
    public interface Processor {

        double[] benchmark(DoubleSeq highFreqSeries, DoubleSeq aggregationConstraint, int startOffset, RawDentonSpec spec);

        double[] benchmark(DoubleSeq aggregationConstraint, RawDentonSpec spec);
    }

}
