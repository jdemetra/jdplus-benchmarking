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

import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.benchmarking.multivariate.MultivariateCholette;
import jdplus.benchmarking.base.api.benchmarking.multivariate.MultivariateCholetteSpec;
import jdplus.benchmarking.base.api.benchmarking.multivariate.TemporalConstraint;
import jdplus.benchmarking.base.api.benchmarking.univariate.CholetteSpec;
import jdplus.benchmarking.base.api.benchmarking.univariate.DentonSpec;
import jdplus.benchmarking.base.api.benchmarking.univariate.Cholette;
import jdplus.benchmarking.base.api.benchmarking.univariate.CubicSpline;
import jdplus.benchmarking.base.api.benchmarking.univariate.CubicSplineSpec;
import jdplus.benchmarking.base.api.benchmarking.univariate.Denton;
import jdplus.benchmarking.base.api.benchmarking.univariate.GrowthRatePreservation;
import jdplus.benchmarking.base.api.benchmarking.univariate.GrpSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.r.util.Dictionary;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Benchmarking {

    public TsData denton(TsData source, TsData bench, int differencing, boolean multiplicative, boolean modified, String conversion, int pos) {
        DentonSpec spec = DentonSpec
                .builder()
                .differencing(differencing)
                .multiplicative(multiplicative)
                .modified(modified)
                .aggregationType(AggregationType.valueOf(conversion))
                .observationPosition(pos-1)
                .build();
        return Denton.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData denton(int nfreq, TsData bench, int differencing, boolean multiplicative, boolean modified, String conversion, int pos) {
        DentonSpec spec = DentonSpec
                .builder()
                .differencing(differencing)
                .multiplicative(multiplicative)
                .modified(modified)
                .aggregationType(AggregationType.valueOf(conversion))
                .observationPosition(pos-1)
                .build();
        return Denton.benchmark(TsUnit.ofAnnualFrequency(nfreq), bench.cleanExtremities(), spec);
    }

    public TsData cholette(TsData source, TsData bench, double rho, double lambda, String bias, String conversion, int pos) {
        CholetteSpec spec = CholetteSpec.builder()
                .rho(rho)
                .lambda(lambda)
                .aggregationType(AggregationType.valueOf(conversion))
                .observationPosition(pos-1)
                .bias(CholetteSpec.BiasCorrection.valueOf(bias))
                .build();
        return Cholette.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData grp(TsData source, TsData bench, String conversion, int pos, double eps, int iter, boolean denton) {
        AggregationType type = AggregationType.valueOf(conversion);
        GrpSpec spec=GrpSpec.builder()
                .aggregationType(type)
                .observationPosition(pos-1)
                .maxIter(iter)
                .precision(eps)
                .dentonInitialization(denton)
                .build();
        return GrowthRatePreservation.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData cubicSpline(TsData source, TsData bench, String conversion, int pos) {
        AggregationType type = AggregationType.valueOf(conversion);
        CubicSplineSpec spec=CubicSplineSpec.builder()
                .aggregationType(type)
                .observationPosition(pos-1)
                .build();
        return CubicSpline.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData cubicSpline(int nfreq, TsData bench, String conversion, int pos) {
        AggregationType type = AggregationType.valueOf(conversion);
        CubicSplineSpec spec=CubicSplineSpec.builder()
                .aggregationType(type)
                .observationPosition(pos-1)
                .build();
        return CubicSpline.benchmark(TsUnit.ofAnnualFrequency(nfreq), bench.cleanExtremities(), spec);
    }

    public Dictionary multiCholette(Dictionary input, String[] temporalConstraints, String[] contemporaneousConstraints, double rho, double lambda) {
        MultivariateCholetteSpec.Builder builder = MultivariateCholetteSpec.builder()
                .rho(rho)
                .lambda(lambda);
        if (temporalConstraints != null) {
            for (int i = 0; i < temporalConstraints.length; ++i) {
                if (temporalConstraints[i].length() > 0) {
                    builder.temporalConstraint(TemporalConstraint.parse(temporalConstraints[i]));
                }
            }
        }
        if (contemporaneousConstraints != null) {
            for (int i = 0; i < contemporaneousConstraints.length; ++i) {
                if (contemporaneousConstraints[i].length() > 0) {
                    builder.contemporaneousConstraint(ContemporaneousConstraint.parse(contemporaneousConstraints[i]));
                }
            }
        }
        Map<String, TsData> rslt = MultivariateCholette.benchmark(input.data(), builder.build());
        return Dictionary.of(rslt);
    }
}
