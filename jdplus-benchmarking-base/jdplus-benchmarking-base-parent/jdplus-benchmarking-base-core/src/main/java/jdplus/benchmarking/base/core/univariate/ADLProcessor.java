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

import java.util.ArrayList;
import java.util.List;
import jdplus.benchmarking.base.api.univariate.ADLSpec;
import jdplus.benchmarking.base.core.benchmarking.extractors.MarginalLikelihoodStatistics;
import jdplus.benchmarking.base.core.benchmarking.extractors.ProfileLikelihoodStatistics;
import jdplus.benchmarking.base.core.ssf.SsfADL;
import jdplus.benchmarking.base.core.ssf.SsfADL1;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.math.functions.ObjectiveFunctionPoint;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.akf.SmoothingOutput;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihood;
import jdplus.toolkit.base.core.ssf.likelihood.ProfileLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ADLProcessor {

    public DisaggregationModel createModel(TsData aggregatedSeries, TsData[] indicators, ADLDefinition spec) {
        TsDomain hdomain = indicators[0].getDomain();
        for (int i = 1; i < indicators.length; ++i) {
            hdomain = hdomain.intersection(indicators[i].getDomain());
        }

        List<Variable> vars = new ArrayList<>();
        for (int i = 0; i < indicators.length; ++i) {
            vars.add(Variable.variable("var" + (i + 1), new UserVariable(null, indicators[i])));
        }
        return new DisaggregationModelBuilder(aggregatedSeries)
                .disaggregationDomain(hdomain)
                .aggregationType(AggregationType.Sum)
                .addX(vars)
                .rescale(false)
                .build();
    }

    public ADLResults process(TsData aggregatedSeries, TsData[] indicators, ADLSpec spec) {
        aggregatedSeries = aggregatedSeries.select(spec.getEstimationSpan());
        DisaggregationModel model = createModel(aggregatedSeries, indicators, definitionOf(spec));
        return compute(model, spec);
    }

    public static ADLDefinition definitionOf(ADLSpec spec) {
        Parameter phi = spec.getPhi();
        double p = phi.isDefined() ? phi.getValue() : 0.9;
        return ADLDefinition.builder()
                .mean(spec.isMean())
                .trend(spec.isTrend())
                .phi(p)
                .xar(spec.getXar())
                .build();
    }

    private ADLResults compute(DisaggregationModel model, ADLSpec spec) {
        return switch (spec.getAggregationType()) {
            case Sum, Average ->
                disaggregate(model, spec);
            case First, Last, UserDefined ->
                interpolate(model, spec);
            default ->
                null;
        };
    }

    private ADLResults disaggregate(DisaggregationModel model, ADLSpec spec) {
        ADLDefinition definition = definitionOf(spec);
        double limit = spec.getTruncation() == null ? -1 : spec.getTruncation();
        ObjectiveFunctionPoint ml = null;
        ADLFunction fn = ADLFunction.builder()
                .definition(definition)
                .y(DoubleSeq.of(model.getHEY()))
                .X(model.getHEX())
                .ratio(model.getFrequencyRatio())
                .startPosition(model.getStart())
                .limit(Math.max(-.999999, limit))
                .marginal(spec.isDiffuseRegressors())
                .log(false)
                .type(spec.getSsfType())
                .build();
        ADLFunction.Point rslt = fn.evaluate(DoubleSeq.of(definition.getPhi()));
        if (spec.isParameterEstimation()) {

            SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer.builder()
                    .functionPrecision(spec.getEstimationPrecision())
                    .build();
            fmin.minimize(rslt);
            rslt = (ADLFunction.Point) fmin.getResult();
            double phi = rslt.getParameters().get(0);
            definition = definition.withPhi(phi);
            double[] grad = fmin.gradientAtMinimum().toArray();
            for (int i = 0; i < grad.length; ++i) {
                grad[i] = -grad[i];
            }
            FastMatrix hessian = rslt.derivatives().hessian();
            ml = new ObjectiveFunctionPoint(rslt.logLikelihood(),
                    new double[]{phi}, grad, hessian);
        }
        SsfData ssfData = new SsfData(model.getHY());
        Ssf ssf;
        ISsfLoading rloading;
        if (spec.getSsfType() == ADLSpec.SsfType.CUMUL) {
            FastMatrix W = SsfADL.regressionMatrix(definition, model.getHX());
            ssf = SsfADL.ssfRepresentation(W, definition.getPhi(), model.getFrequencyRatio(), model.getStart());
            rloading = RegSsf.defaultLoading(1, Loading.fromPosition(0), W);
        } else {
            ssf = SsfADL1.ssfRepresentation(definition, model.getHX(), model.getFrequencyRatio(), model.getStart());
            rloading = Loading.fromPosition(0);
        }
        DefaultSmoothingResults ss = AkfToolkit.smooth(ssf, ssfData, true, true, false);
//        DefaultSmoothingResults ss = DkToolkit.sqrtSmooth(ssf, ssfData, true, true);

//        DataBlock coeff = ss.getSmoothing().a(0).drop(2, 0);
//        FastMatrix cvar = ss.getSmoothing().P(0).extract(2, coeff.length(), 2, coeff.length());
        DataBlock coeff = ss.a(0).drop(2, 0);
        FastMatrix cvar = ss.P(0).extract(2, coeff.length(), 2, coeff.length());
        int nparams = spec.isParameterEstimation() ? 1 : 0;
        int nz = ssf.getStateDim() - 1;

        double yfactor = model.getYfactor();
        if (spec.getAggregationType() == AggregationType.Average) {
            yfactor /= model.getFrequencyRatio();
        }

        double[] s = new double[ssfData.length()];
        double[] es = new double[ssfData.length()];
        for (int i = 0; i < s.length; ++i) {
//            s[i] = rloading.ZX(i, ss.getFiltering().a(i).drop(1, 0)) / yfactor;
//            double v = rloading.ZVZ(i, ss.getSmoothing().P(i).extract(1, nz, 1, nz));
            s[i] = rloading.ZX(i, ss.a(i).drop(1, 0)) / yfactor;
            double v = rloading.ZVZ(i, ss.P(i).extract(1, nz, 1, nz));
            if (v > 0) {
                es[i] = Math.sqrt(v) / yfactor;
            }
        }
        MarginalLikelihood mll = rslt.marginalLikelihood();
        if (mll != null) {
            mll = mll.rescale(yfactor);
        }

        ProfileLikelihood pll = rslt.profileLikelihood();
        if (pll != null) {
            pll.rescale(yfactor);
        }

        double[] pcoeff = coeff.toArray();
        for (int i = 0; i < pcoeff.length; ++i) {
            pcoeff[i] /= yfactor;
        }
        FastMatrix pcvar = cvar.deepClone();
        pcvar.div(yfactor * yfactor);
        return ADLResults.builder()
                .originalSeries(model.getOriginalSeries())
                .disaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), s))
                .stdevDisaggregatedSeries(TsData.ofInternal(model.getHDom().getStartPeriod(), es))
                .disaggregationDomain(model.getHDom())
                .marginalLikelihood(MarginalLikelihoodStatistics.stats(mll, 0, 1 + nparams)) // + scaling factor
                .profileLikelihood(ProfileLikelihoodStatistics.stats(pll, 0, 1 + nparams))
                .coefficients(DoubleSeq.of(pcoeff))
                .coefficientsCovariance(pcvar)
                .maximum(ml)
                .build();
    }

    private ADLResults interpolate(DisaggregationModel model, ADLSpec spec) {
        throw new UnsupportedOperationException("Not supported yet.");
//    private SsfFunction<Parameter, Ssf> ssfFunction(DisaggregationModel model, TemporalDisaggregationSpec spec) {
//        SsfData data = new SsfData(model.getHEY());
//        Double lbound = spec.getTruncatedParameter();
//        TemporalDisaggregationProcessor.Mapping mapping = new TemporalDisaggregationProcessor.Mapping(lbound == null ? -1 : lbound);
//        boolean cl = spec.getResidualsModel() == TemporalDisaggregationSpec.Model.Ar1;
//        boolean disagg = spec.getAggregationType() == AggregationType.Average || spec.getAggregationType() == AggregationType.Sum;
//        return SsfFunction.builder(data, mapping,
//                p -> ssf(p.getValue(), disagg, cl, spec.isZeroInitialization(), model.getFrequencyRatio()))
//                .regression(model.getHEX(), diffuseRegressors(model.nx(), spec))
//                .useMaximumLikelihood(true)
//                .build();
    }
}
