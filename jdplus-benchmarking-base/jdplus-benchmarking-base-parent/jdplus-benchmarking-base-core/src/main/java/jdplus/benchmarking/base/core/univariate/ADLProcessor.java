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
import jdplus.benchmarking.base.core.ssf.SsfADL;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.math.functions.ObjectiveFunctionPoint;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.functions.FunctionMinimizer;
import jdplus.toolkit.base.core.math.functions.bfgs.Bfgs;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
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

    public FastMatrix regressionMatrix(ADLDefinition definition, FastMatrix X) {
        int nx = X.getColumnsCount();
        if (definition.getXar() == ADLSpec.XAR.FREE) {
            nx += X.getColumnsCount();
        }
        if (definition.isMean()) {
            ++nx;
        }
        if (definition.isTrend()) {
            ++nx;
        }
        FastMatrix z = X;
        int n = z.getRowsCount();
//        if (definition.isXunitRoot()) {
//            z = X.deepClone();
//            for (int i = n - 1; i > 0; --i) {
//                z.row(i).sub(z.row(i - 1));
//            }
//            z.row(0).set(0);
//        }
        // z contains now either the original x or dx
        double phi = definition.getPhi();
        FastMatrix W = FastMatrix.make(n, nx);
        int c = 0;
        if (definition.isMean()) {
            W.column(c++).set(1);
        }
        if (definition.isTrend()) {
            W.column(c++).set(i -> i);
        }
        switch (definition.getXar()) {
            case NONE -> {
                DataBlockIterator cols = z.columnsIterator();
                while (cols.hasNext()) {
                    W.column(c++).copy(cols.next());
                }
            }
            case FREE -> {
                DataBlockIterator cols = z.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock cur = cols.next();
                    DataBlock column = W.column(c++);
                    column.drop(0, 1).copy(cur.drop(1, 0));
                    column.set(n - 1, cur.get(n - 1));
                    W.column(c).copy(cur);
                }
            }
            case SAME -> {
                DataBlockIterator cols = z.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock cur = cols.next();
                    DataBlock column = W.column(c++);
                    column.drop(0, 1).copy(cur.drop(1, 0));
                    column.set(n - 1, cur.get(n - 1));
                    column.addAY(-phi, cur);
                }
            }
        }
        return W;
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
        double limit = spec.getTruncation() == null ? -1 : spec.getTruncation() == null ? -1 : spec.getTruncation();
        ObjectiveFunctionPoint ml = null;
        ADLFunction fn = ADLFunction.builder()
                .definition(definition)
                .y(DoubleSeq.of(model.getHEY()))
                .X(model.getHEX())
                .ratio(model.getFrequencyRatio())
                .startPosition(model.getStart())
                .limit(Math.max(-1, limit))
                .build();
        ADLFunction.Point rslt = fn.evaluate(DoubleSeq.of(definition.getPhi()));
        if (spec.isParameterEstimation()) {

            FunctionMinimizer fmin = Bfgs.builder()
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
            ml = new ObjectiveFunctionPoint(rslt.likelihood().logLikelihood(),
                    new double[]{phi}, grad, hessian);
        }
        Ssf ssf = SsfADL.ssfRepresentation(definition, model.getHX(), model.getFrequencyRatio(), model.getStart());
        DefaultSmoothingResults ss = DkToolkit.sqrtSmooth(ssf, new SsfData(model.getHY()), true, true);
        DataBlock coeff = ss.a(0).drop(2, 0);
        FastMatrix cvar = ss.P(0).extract(2, coeff.length(), 2, coeff.length());
        int nparams = spec.isParameterEstimation() ? 1 : 0;

        return ADLResults.builder()
                .originalSeries(model.getOriginalSeries())
                .disaggregatedSeries(TsData.of(model.getHDom().getStartPeriod(), ss.getComponent(1)))
                .stdevDisaggregatedSeries(TsData.of(model.getHDom().getStartPeriod(), ss.getComponentVariance(1).fn(z -> z < 0 ? 0 : Math.sqrt(z))))
                .disaggregationDomain(model.getHDom())
                .likelihood(MarginalLikelihoodStatistics.stats(rslt.likelihood(), 0, 1 + nparams)) // + scaling factor
                .coefficients(DoubleSeq.of(coeff.toArray()))
                .coefficientsCovariance(cvar.deepClone())
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
