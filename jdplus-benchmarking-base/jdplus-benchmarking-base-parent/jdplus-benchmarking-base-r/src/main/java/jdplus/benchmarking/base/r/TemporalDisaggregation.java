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

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.benchmarking.base.api.univariate.ModelBasedDentonSpec;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationIResults;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationISpec;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec.Model;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.benchmarking.base.api.univariate.ADLSpec;
import static jdplus.benchmarking.base.api.univariate.ADLSpec.DEF_ALGORITHM;
import static jdplus.benchmarking.base.api.univariate.ADLSpec.DEF_EPS;
import static jdplus.benchmarking.base.api.univariate.ADLSpec.DEF_RESCALE;
import static jdplus.benchmarking.base.api.univariate.ADLSpec.builder;
import jdplus.benchmarking.base.api.univariate.RawTemporalDisaggregationSpec;
import jdplus.benchmarking.base.core.univariate.ADLProcessor;
import jdplus.benchmarking.base.core.univariate.ADLResults;
import jdplus.benchmarking.base.core.univariate.ModelBasedDentonProcessor;
import jdplus.benchmarking.base.core.univariate.ModelBasedDentonResults;
import jdplus.benchmarking.base.core.univariate.ProcessorI;
import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationProcessor;
import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationResults;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationProcessor;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TemporalDisaggregation {

    public TemporalDisaggregationIResults processI(TsData y, TsData indicator, String model, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho) {
        TemporalDisaggregationISpec spec = TemporalDisaggregationISpec.builder()
                .constant(true)
                .residualsModel(Model.valueOf(model))
                .aggregationType(AggregationType.valueOf(aggregation))
                .observationPosition(obspos)
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .truncatedRho(truncatedRho)
                .build();
        return ProcessorI.process(y, indicator, spec);
    }

    public ModelBasedDentonResults processModelBasedDenton(TsData y, TsData indicator, int differencing, String aggregation, int obspos, String[] odates, double[] ovar, String[] fdates, double[] fval) {
        ModelBasedDentonSpec.Builder builder = ModelBasedDentonSpec.builder()
                .aggregationType(AggregationType.valueOf(aggregation));
        if (odates != null && ovar != null) {
            if (odates.length != ovar.length) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < odates.length; ++i) {
                builder.shockVariance(LocalDate.parse(odates[i], DateTimeFormatter.ISO_DATE), ovar[i]);
            }
        }
        if (fdates != null && fval != null) {
            if (fdates.length != fval.length) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < fdates.length; ++i) {
                builder.fixedBiRatio(LocalDate.parse(fdates[i], DateTimeFormatter.ISO_DATE), fval[i]);
            }
        }
        return ModelBasedDentonProcessor.process(y, indicator, builder.build());
    }

    public TemporalDisaggregationResults process(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String model, int freq, int nExt, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {
        TemporalDisaggregationSpec.Builder builder = TemporalDisaggregationSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(TemporalDisaggregationSpec.Model.valueOf(model))
                .aggregationType(AggregationType.valueOf(aggregation))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .algorithm(SsfInitialization.valueOf(algorithm))
                .zeroInitialization(zeroinit)
                .diffuseRegressors(diffuseregs)
                .rescale(true);
        if (aggregation.equals("UserDefined")) {
            builder.observationPosition(obspos);
        }
        if (indicators == null) {
            TsUnit unit = TsUnit.ofAnnualFrequency(freq);
            TsPeriod start = TsPeriod.of(unit, y.getStart().start());
            TsPeriod end = TsPeriod.of(unit, y.getDomain().end());
            TsDomain all = TsDomain.of(start, start.until(end) + nExt);
            return TemporalDisaggregationProcessor.process(y, all, builder.build());
        } else {
            for (int i = 0; i < indicators.length; ++i) {
                indicators[i] = indicators[i].cleanExtremities();
            }
            return TemporalDisaggregationProcessor.process(y, indicators, builder.build());
        }
    }
    
    public RawTemporalDisaggregationResults processRaw(double[] y, boolean constant, boolean trend, Matrix indicators,
            String model, int disaggregationRatio, int nExt, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {
        
        RawTemporalDisaggregationSpec.Builder builder = RawTemporalDisaggregationSpec.builder()
                .disaggregationRatio(disaggregationRatio)
                .constant(constant)
                .trend(trend)
                .residualsModel(RawTemporalDisaggregationSpec.Model.valueOf(model))
                .aggregationType(AggregationType.valueOf(aggregation))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .algorithm(SsfInitialization.valueOf(algorithm))
                .zeroInitialization(zeroinit)
                .diffuseRegressors(diffuseregs)
                .rescale(true);
        if (aggregation.equals("UserDefined")) {
            builder.observationPosition(obspos);
        }
        
        if (indicators == null) {
            return RawTemporalDisaggregationProcessor.process(DoubleSeq.of(y), builder.build(), nExt);
        } else {
            FastMatrix indicatorsClean = FastMatrix.make(indicators.getRowsCount(), indicators.getColumnsCount());
            for (int j = 0; j < indicators.getColumnsCount(); ++j) {
                indicatorsClean.column(j).add(indicators.column(j).cleanExtremities());
            } 
            
            return RawTemporalDisaggregationProcessor.process(DoubleSeq.of(y), indicatorsClean, builder.build());
        }
    }
    
    public ADLResults processADL(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String aggregation, double phi, boolean fixedphi, double truncatedPhi, String xar) {
        if (indicators == null) {
            return null;
        }
        ADLSpec spec = builder()
                .estimationSpan(TimeSelector.all())
                .aggregationType(AggregationType.valueOf(aggregation))
                .mean(constant)
                .trend(trend)
                .xar(ADLSpec.XAR.valueOf(xar))
                .phi(fixedphi ? Parameter.fixed(phi) : (Double.isFinite(phi) ? Parameter.initial(phi) : Parameter.undefined()))
                .truncation(truncatedPhi <= -1 ? null : truncatedPhi)
                .estimationPrecision(DEF_EPS)
                .rescale(DEF_RESCALE)
//                .algorithm(SsfInitialization.valueOf(algorithm))
                .build();
        for (int i = 0; i < indicators.length; ++i) {
            indicators[i] = indicators[i].cleanExtremities();
        }
        return ADLProcessor.process(y, indicators, spec);

    }
}
