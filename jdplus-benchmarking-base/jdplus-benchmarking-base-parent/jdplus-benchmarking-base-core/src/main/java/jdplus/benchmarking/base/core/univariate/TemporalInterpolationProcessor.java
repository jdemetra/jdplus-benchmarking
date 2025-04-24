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

import java.util.ArrayList;
import jdplus.benchmarking.base.api.univariate.EstimationSpec;
import jdplus.benchmarking.base.api.univariate.IndexRange;
import jdplus.benchmarking.base.api.univariate.RawInterpolationSpec;
import jdplus.benchmarking.base.api.univariate.TemporalInterpolationSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.regression.Constant;
import jdplus.toolkit.base.api.timeseries.regression.LinearTrend;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TemporalInterpolationProcessor {

    public TemporalDisaggregationResults process(TsData y, TsData[] indicators, TemporalInterpolationSpec spec) {
        int lfreq = y.getAnnualFrequency();
        if (indicators == null || indicators.length == 0) {
            int hfreq = spec.getDefaultPeriod();
            if (lfreq >= hfreq) {
                return null;
            }
            return process(y, 0, 0, spec);
        }
        TsDomain hdom = indicators[0].getDomain();
        for (int i = 1; i < indicators.length; ++i) {
            hdom = hdom.intersection(indicators[i].getDomain());
        }
        int hfreq = hdom.getAnnualFrequency();
        if (lfreq >= hfreq || hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        FastMatrix X = FastMatrix.make(hdom.length(), indicators.length);
        for (int i = 0; i < indicators.length; ++i) {
            X.column(i).copy(TsData.fitToDomain(indicators[i], hdom).getValues());
        }
        int frequencyRatio = hfreq / lfreq;
        TsPeriod lystart = y.getStart();
        TsPeriod hystart = TsPeriod.of(hdom.getTsUnit(), lystart.start());
        int obspos = spec.getObservationPosition();
        hystart = hystart.plus(obspos == -1 ? frequencyRatio - 1 : obspos);
        // high-frequency variant of y
//        TsDomain hydom = TsDomain.of(hystart, 1 + frequencyRatio * (y.length() - 1));
//        TsPeriod hyend = hydom.getEndPeriod();
        TsPeriod hxstart = hdom.getStartPeriod(); //, hxend = hdom.getEndPeriod();
        int startOffset = hxstart.until(hystart);
//        int endOffset = hxend.until(hyend);
        int bydrop = 0;
//        int eydrop=0;
        if (startOffset < 0) {
            bydrop = 1 + (-startOffset - 1) / frequencyRatio;
            startOffset += bydrop * frequencyRatio;
        }
//        if (endOffset>0){ 
//            eydrop=1+(endOffset-1)/frequencyRatio;         
//            endOffset-=eydrop*frequencyRatio;
//        }

        TsData yc = y.drop(bydrop, 0); //eydrop);
        TsDomain ydom = yc.getDomain();
        TsDomain ydomc = y.getDomain().select(spec.getEstimationSpec().getEstimationSpan()).intersection(ydom);
        IndexRange range = IndexRange.of(ydom.indexOf(ydomc.getStartPeriod()), ydom.indexOf(ydomc.getEndPeriod()));

        EstimationSpec espec = EstimationSpec.builder()
                .estimationPrecision(spec.getEstimationSpec().getEstimationPrecision())
                .truncatedParameter(spec.getEstimationSpec().getTruncatedParameter())
                .estimationRange(range)
                .build();

        RawInterpolationSpec rawSpec = RawInterpolationSpec.builder(frequencyRatio)
                .firstObservationPosition(startOffset)
                .modelSpec(spec.getModelSpec())
                .algorithmSpec(spec.getAlgorithmSpec())
                .estimationSpec(espec)
                .build();
        RawTemporalDisaggregationResults rslts = RawInterpolationProcessor.process(yc.getValues(), X, startOffset, rawSpec);
        TsPeriod hStart = hdom.getStartPeriod();
        ArrayList<Variable> vars = new ArrayList<>();
        if (spec.getModelSpec().isConstant()) {
            vars.add(Variable.variable("const", Constant.C));
        }
        if (spec.getModelSpec().isTrend()) {
            vars.add(Variable.variable("trend", new LinearTrend(hStart.start())));
        }
        for (int i = 0; i < indicators.length; ++i) {
            String name = "var-" + (i + 1);
            vars.add(Variable.variable(name, new UserVariable(name, indicators[i], null)));
        }

        return TemporalDisaggregationResults.builder()
                .originalSeries(y)
                .disaggregationDomain(y.getDomain())
                .disaggregatedSeries(TsData.of(hStart, rslts.getDisaggregatedSeries()))
                .stdevDisaggregatedSeries(TsData.of(hStart, rslts.getStdevDisaggregatedSeries()))
                .regressionEffects(rslts.getRegressionEffects() == null ? null : TsData.of(hStart, rslts.getRegressionEffects()))
                .hyperParametersCount(rslts.getHyperParametersCount())
                .likelihood(rslts.getLikelihood())
                .maximum(rslts.getMaximum())
                .stats(rslts.getStats())
                .residualsDiagnostics(ResidualsDiagnostics.builder()
                        .fullResiduals(TsData.of(yc.getStart(), rslts.getResidualsDiagnostics().getFullResiduals()))
                        .niid(rslts.getResidualsDiagnostics().getNiid())
                        .build())
                .indicators(vars.toArray(Variable[]::new))
                .build();
    }

    public TemporalDisaggregationResults process(TsData y, int nBackcasts, int nForecasts, TemporalInterpolationSpec spec) {
        int hfreq = spec.getDefaultPeriod(), lfreq = y.getAnnualFrequency();
        if (lfreq >= hfreq || hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        int frequencyRatio = hfreq / lfreq;
        TsPeriod lystart = y.getStart();
        TsPeriod hStart = TsPeriod.of(TsUnit.ofAnnualFrequency(hfreq), lystart.start());
        int obspos = spec.getObservationPosition();
        if (obspos == -1) {
            obspos = frequencyRatio - 1;
        }
        hStart = hStart.plus(obspos - nBackcasts);
        TsDomain ydom = y.getDomain();
        TsDomain ldom = ydom.select(spec.getEstimationSpec().getEstimationSpan());
        IndexRange range = IndexRange.of(ydom.indexOf(ldom.getStartPeriod()), ydom.indexOf(ldom.getEndPeriod()));
        EstimationSpec espec = EstimationSpec.builder()
                .estimationPrecision(spec.getEstimationSpec().getEstimationPrecision())
                .truncatedParameter(spec.getEstimationSpec().getTruncatedParameter())
                .estimationRange(range)
                .build();
        RawInterpolationSpec rawSpec = RawInterpolationSpec.builder(hfreq / lfreq)
                .firstObservationPosition(obspos)
                .modelSpec(spec.getModelSpec())
                .algorithmSpec(spec.getAlgorithmSpec())
                .estimationSpec(espec)
                .build();

        RawTemporalDisaggregationResults rslts = RawInterpolationProcessor.process(y.getValues(), nBackcasts, nForecasts, rawSpec);

        ArrayList<Variable> vars = new ArrayList<>();
        if (spec.getModelSpec().isConstant()) {
            vars.add(Variable.variable("const", Constant.C));
        }
        if (spec.getModelSpec().isTrend()) {
            vars.add(Variable.variable("trend", new LinearTrend(hStart.start())));
        }

        return TemporalDisaggregationResults.builder()
                .originalSeries(y)
                .disaggregationDomain(y.getDomain())
                .disaggregatedSeries(TsData.of(hStart, rslts.getDisaggregatedSeries()))
                .stdevDisaggregatedSeries(TsData.of(hStart, rslts.getStdevDisaggregatedSeries()))
                .regressionEffects(rslts.getRegressionEffects() == null ? null : TsData.of(hStart, rslts.getRegressionEffects()))
                .hyperParametersCount(rslts.getHyperParametersCount())
                .likelihood(rslts.getLikelihood())
                .maximum(rslts.getMaximum())
                .stats(rslts.getStats())
                .residualsDiagnostics(ResidualsDiagnostics.builder()
                        .fullResiduals(TsData.of(y.getStart(), rslts.getResidualsDiagnostics().getFullResiduals()))
                        .niid(rslts.getResidualsDiagnostics().getNiid())
                        .build())
                .indicators(vars.toArray(Variable[]::new))
                .build();
    }

}
