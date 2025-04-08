/*
 * Copyright 2025 National Bank of Belgium
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

import jdplus.benchmarking.base.api.benchmarking.univariate.RawDenton;
import jdplus.benchmarking.base.api.benchmarking.univariate.RawDentonSpec;
import jdplus.benchmarking.base.api.benchmarking.univariate.DentonSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author LEMASSO
 */
@ServiceProvider(RawDenton.Processor.class)
public class RawDentonProcessor implements RawDenton.Processor {

    public static final RawDentonProcessor PROCESSOR=new RawDentonProcessor();

    @Override
    public double[] benchmark(DoubleSeq highFreqSeries, DoubleSeq aggregationConstraint, int offset, RawDentonSpec spec) {
        int ratio = spec.getFrequencyRatio();
        
        DoubleSeq naggregationConstraint;
        switch (spec.getAggregationType()){
            case Sum, Average -> naggregationConstraint=BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint, ratio, offset);
            case Last -> naggregationConstraint=BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, ratio, offset, ratio-1);
            case First -> naggregationConstraint=BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, ratio, offset, 0);
            case UserDefined -> naggregationConstraint=BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, ratio, offset, spec.getObservationPosition());
            default -> throw new IllegalArgumentException();
        }
        
        DentonSpec specDenton = DentonSpec.builder()
                .multiplicative(spec.isMultiplicative())
                .modified(spec.isModified())
                .differencing(spec.getDifferencing())
                .aggregationType(spec.getAggregationType())
                .observationPosition(spec.getObservationPosition())
                .build();
        
        MatrixDenton denton = new MatrixDenton(specDenton, ratio, offset);
        return denton.process(highFreqSeries, naggregationConstraint);
    }

    @Override
    public double[] benchmark(DoubleSeq aggregationConstraint, RawDentonSpec spec) {
        int ratio = spec.getFrequencyRatio();
        
        DentonSpec specDenton = DentonSpec.builder()
                .multiplicative(spec.isMultiplicative())
                .modified(spec.isModified())
                .differencing(spec.getDifferencing())
                .aggregationType(spec.getAggregationType())
                .observationPosition(spec.getObservationPosition())
                .build();
        
        MatrixDenton denton = new MatrixDenton(specDenton, ratio, 0);
        return denton.process(aggregationConstraint);
    }
}
