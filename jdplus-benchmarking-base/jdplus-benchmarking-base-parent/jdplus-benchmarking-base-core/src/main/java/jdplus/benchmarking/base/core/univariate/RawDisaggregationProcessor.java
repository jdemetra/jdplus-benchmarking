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

import jdplus.benchmarking.base.api.univariate.RawDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
//import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import jdplus.toolkit.base.api.math.functions.ObjectiveFunctionPoint;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.ssf.DataBlockResults;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.arima.AR1;
import jdplus.toolkit.base.core.ssf.arima.Arima_1_1_0;
import jdplus.toolkit.base.core.ssf.arima.Rw;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.benchmarking.SsfCumulator;
import jdplus.toolkit.base.core.ssf.dk.DefaultDiffuseFilteringResults;
import jdplus.toolkit.base.core.ssf.dk.DiffuseSmoother;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.FastDkSmoother;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.sts.Noise;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ssf.univariate.SsfRegressionModel;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.tests.NiidTests;
import lombok.NonNull;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RawDisaggregationProcessor {

    @lombok.Value
    @lombok.AllArgsConstructor
    private static class RawDisaggregationEstimation {

        ObjectiveFunctionPoint ml;
        DiffuseConcentratedLikelihood dll;
        StateComponent noise;
        ISsfLoading loading;
    }

    public RawTemporalDisaggregationResults process(@NonNull DoubleSeq y, @NonNull FastMatrix regressors, @NonNegative int startOffset, @NonNull RawDisaggregationSpec spec) {
        RawDisaggregationModelBuilder builder = new RawDisaggregationModelBuilder(y, regressors, startOffset, spec);
        RawDisaggregationModel yx = builder.build();
        return compute(yx, spec);
    }

    public RawTemporalDisaggregationResults process(@NonNull DoubleSeq y, int nBackcasts, int nForecasts, @NonNull RawDisaggregationSpec spec) {
        RawDisaggregationModelBuilder builder = new RawDisaggregationModelBuilder(y, spec, nBackcasts, nForecasts);
        RawDisaggregationModel yx = builder.build();
        return compute(yx, spec);
    }

    private RawTemporalDisaggregationResults compute(RawDisaggregationModel model, RawDisaggregationSpec spec) {
        return spec.getAlgorithmSpec().isFast()
                ? disaggregate2(model, spec) : disaggregate(model, spec);
    }

    // Estimate the disaggregation  model
    private RawDisaggregationEstimation estimateDisaggregationModel(RawDisaggregationModel model, RawDisaggregationSpec spec) {
        StateComponent ncmp = noiseComponent(spec);
        ISsfLoading nloading = noiseLoading(spec);
        int diffuse = diffuseRegressors(model.nx(), spec);
        if (!spec.getModelSpec().isParameterEstimation()) {
            Ssf cssf = Ssf.of(SsfCumulator.of(ncmp, nloading, model.getRatio(), 0),
                    SsfCumulator.defaultLoading(nloading, model.getRatio(), 0));
            SsfData ssfdata = new SsfData(model.estimationY());
            SsfRegressionModel ssfmodel = new SsfRegressionModel(cssf, ssfdata, model.estimationXc().isEmpty() ? null : model.estimationXc(), diffuse);
            return new RawDisaggregationEstimation(
                    null,
                    DkToolkit.concentratedLikelihoodComputer(true, false, true).compute(ssfmodel),
                    ncmp,
                    nloading
            );
        } else {
            SsfFunction<Parameter, Ssf> fn = ssfFunction(model, spec);
            SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
                    .builder()
                    .functionPrecision(spec.getEstimationSpec().getEstimationPrecision())
                    .build();
            double start = spec.getModelSpec().getParameter().getType() == ParameterType.Undefined
                    ? .9 : spec.getModelSpec().getParameter().getValue();
            fmin.minimize(fn.ssqEvaluate(Doubles.of(start)));
            SsfFunctionPoint<Parameter, Ssf> rslt = (SsfFunctionPoint<Parameter, Ssf>) fmin.getResult();
            DoubleSeq p = rslt.getParameters();
            DiffuseConcentratedLikelihood dll = rslt.getLikelihood();
            double c = -.5 * (dll.degreesOfFreedom() - 1) / rslt.getValue();
            double[] grad = fmin.gradientAtMinimum().toArray();
            for (int i = 0; i < grad.length; ++i) {
                grad[i] *= c;
            }
            FastMatrix hessian = fmin.curvatureAtMinimum().times(c);
            ObjectiveFunctionPoint ml = new ObjectiveFunctionPoint(rslt.getLikelihood().logLikelihood(),
                    p.toArray(), grad, hessian);
            if (spec.getModelSpec().getResidualsModel() == ResidualsModel.Ar1) {
                ncmp = AR1.of(p.get(0), 1, spec.getModelSpec().isZeroInitialization());
            } else {
                ncmp = Arima_1_1_0.of(p.get(0), 1, spec.getModelSpec().isZeroInitialization());
            }
            return new RawDisaggregationEstimation(
                    ml, dll, ncmp, nloading
            );
        }
    }

    private void disaggregateEstimation(RawDisaggregationModel model, RawDisaggregationEstimation estimation, final double[] z, final double[] e) {
        int cstart = model.getStartOffset() == 0 ? 0 : model.getRatio() - model.getStartOffset();
        double[] hy = model.getHy().toArray();
        FastMatrix hX = model.getX();
        FastMatrix hXC = model.getXc();

        StateComponent ncmp = estimation.getNoise();
        ISsfLoading nloading = estimation.getLoading();
        Ssf cssf = Ssf.of(SsfCumulator.of(ncmp, nloading, model.getRatio(), cstart),
                SsfCumulator.defaultLoading(nloading, model.getRatio(), cstart));
        DiffuseSmoother smoother = DiffuseSmoother.builder(cssf)
                .calcVariance(true)
                .rescaleVariance(false)
                .build();
        DefaultSmoothingResults srslts = DefaultSmoothingResults.full();
        srslts.prepare(cssf.getStateDim(), 0, hy.length);
        DiffuseConcentratedLikelihood dll = estimation.getDll();
        double sigma = Math.sqrt(dll.sigma2());
        int dim = ncmp.dim();
        if (hX == null || hX.isEmpty()) {
            SsfData data = new SsfData(hy);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(cssf, data, true);
            smoother.process(hy.length, frslts, srslts);
            for (int i = 0; i < z.length; ++i) {
                z[i] = nloading.ZX(i, srslts.a(i).drop(1, 0));
                double v = nloading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
                e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
            }
        } else {
            // Xb
            DoubleSeq b = dll.coefficients();
            DataBlock Xb = DataBlock.make(hXC.getRowsCount());
            DataBlock Xbc = DataBlock.make(hXC.getRowsCount());
            DoubleSeqCursor bcur = b.cursor();
            DataBlockIterator xcols = hX.columnsIterator();
            DataBlockIterator xccols = hXC.columnsIterator();
            double c = bcur.getAndNext();
            Xb.setAY(c, xcols.next());
            Xbc.setAY(c, xccols.next());
            while (xcols.hasNext()) {
                c = bcur.getAndNext();
                Xb.addAY(c, xcols.next());
                Xbc.addAY(c, xccols.next());
            }
            // u=y-Xb
            DataBlock u = DataBlock.copyOf(hy);
            u.sub(Xbc);

            //L(y-Xb)
            SsfData data = new SsfData(u);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(cssf, data, true);
            smoother.process(hy.length, frslts, srslts);

            // Z = L(y-Xb) + Xb  
            // V = V(L(y-Xb)) + (LX-X) V(B) (LX-X)'
            FastMatrix Vb = dll.unscaledCovariance();
            FastMatrix LhX = FastMatrix.make(hX.getRowsCount(), hX.getColumnsCount());
            DataBlockIterator lxcols = LhX.columnsIterator();
            xccols.reset();
            while (xccols.hasNext()) {
                FastDkSmoother fsmoother = new FastDkSmoother(cssf, frslts);
                fsmoother.smooth(xccols.next());
                DataBlockResults ss = fsmoother.smoothedStates();
                lxcols.next().set(i -> nloading.ZX(i, ss.datablock(i).drop(1, 0)));
            }

            LhX.sub(hX);
            for (int i = 0; i < z.length; ++i) {
                z[i] = nloading.ZX(i, srslts.a(i).drop(1, 0)) + Xb.get(i);
                double v = nloading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
                v += QuadraticForm.apply(Vb, LhX.row(i));
                e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
            }
        }
    }

    private RawTemporalDisaggregationResults disaggregate2(RawDisaggregationModel model, RawDisaggregationSpec spec) {
        int cstart = model.getStartOffset() == 0 ? 0 : model.getRatio() - model.getStartOffset();
        RawDisaggregationEstimation edm = estimateDisaggregationModel(model, spec);

        double[] yh = new double[model.getHy().length()];
        double[] eyh = new double[yh.length];

        disaggregateEstimation(model, edm, yh, eyh);
        double yfac = model.getYfactor();
        if (model.isAverage()) {
            yfac /= model.getRatio();
        }
        if (yfac != 1) {
            for (int i = 0; i < yh.length; ++i) {
                yh[i] /= yfac;
                eyh[i] /= yfac;
            }
        }

        double[] xfac = model.getXfactors();

        DiffuseConcentratedLikelihood dll = edm.getDll();
        // full residuals are obtained by applying the filter on the series without the
        // regression effects
        Ssf ssf = Ssf.of(SsfCumulator.of(edm.getNoise(), edm.getLoading(), model.getRatio(), cstart),
                SsfCumulator.defaultLoading(edm.getLoading(), model.getRatio(), cstart));
        DoubleSeq res = residuals(model, dll.coefficients(), ssf);

        // correct the ll (and the coeff) with the scaling factors
        dll = dll.rescale(yfac, xfac);
        DoubleSeq regeffect = regeffect(model, dll.coefficients());

        int nparams = spec.getModelSpec().isParameterEstimation() ? 1 : 0;
        return RawTemporalDisaggregationResults.builder()
                .series(model.getY())
                .regressors(model.getX())
                .maximum(edm.getMl())
                .likelihood(dll)
                .hyperParametersCount(nparams)
                .stats(dll.stats(0, nparams))
                .disaggregatedSeries(DoubleSeq.of(yh))
                .stdevDisaggregatedSeries(DoubleSeq.of(eyh))
                .regressionEffects(regeffect)
                .residualsDiagnostics(diagnostic(res, model.getRatio()))
                .build();
    }

    private RawTemporalDisaggregationResults disaggregate(RawDisaggregationModel model, RawDisaggregationSpec spec) {
        int cstart = model.getStartOffset() == 0 ? 0 : model.getRatio() - model.getStartOffset();
        RawDisaggregationEstimation edm = estimateDisaggregationModel(model, spec);
        StateComponent ncmp = edm.getNoise();
        ISsfLoading nloading = edm.getLoading();
        DiffuseConcentratedLikelihood dll = edm.getDll();
        DoubleSeq y = model.getHy();
        FastMatrix Xc = model.getX();
        StateComponent rcmp = model.nx() == 0 ? ncmp : RegSsf.of(ncmp, Xc);
        ISsfLoading rloading = model.nx() == 0 ? nloading : RegSsf.defaultLoading(ncmp.dim(), nloading, Xc);
        SsfData ssfdata = new SsfData(y);
        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, model.getRatio(), cstart),
                SsfCumulator.defaultLoading(rloading, model.getRatio(), cstart));
        DefaultSmoothingResults srslts;
        srslts = switch (spec.getAlgorithmSpec().getAlgorithm()) {
            case Augmented ->
                AkfToolkit.smooth(ssf, ssfdata, true, false, false);
            case SqrtDiffuse ->
                DkToolkit.sqrtSmooth(ssf, ssfdata, true, false);
            case Augmented_NoCollapsing ->
                AkfToolkit.smooth(ssf, ssfdata, true, false, true);
            case Augmented_Robust ->
                AkfToolkit.robustSmooth(ssf, ssfdata, true, false).getSmoothing();
            default ->
                DkToolkit.smooth(ssf, ssfdata, true, false);
        };

        double[] yh = new double[y.length()];
        double[] vyh = new double[y.length()];
        int dim = ssf.getStateDim();
        double yfac = model.getYfactor();
        if (model.isAverage()) {
            yfac /= model.getRatio();
        }
        double[] xfac = model.getXfactors();
        double sigma = Math.sqrt(dll.sigma2()) / yfac;
        for (int i = 0; i < yh.length; ++i) {
            yh[i] = rloading.ZX(i, srslts.a(i).drop(1, 0)) / yfac;
            double v = rloading.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
            vyh[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
        }
        Ssf cssf = Ssf.of(SsfCumulator.of(ncmp, nloading, model.getRatio(), cstart),
                SsfCumulator.defaultLoading(nloading, model.getRatio(), cstart));
        DoubleSeq res = residuals(model, dll.coefficients(), cssf);
        // correct first the ll (and the coeff) with the scaling factors
        dll = dll.rescale(yfac, xfac);
        DoubleSeq regeffect = regeffect(model, dll.coefficients());
        // full residuals are obtained by applying the filter on the series without the
        // regression effects
        int nparams = spec.getModelSpec().isParameterEstimation() ? 1 : 0;
        return RawTemporalDisaggregationResults.builder()
                .series(model.getY())
                .regressors(model.getX())
                .maximum(edm.getMl())
                .likelihood(dll)
                .hyperParametersCount(nparams)
                .stats(dll.stats(0, nparams))
                .disaggregatedSeries(DoubleSeq.of(yh))
                .stdevDisaggregatedSeries(DoubleSeq.of(vyh))
                .regressionEffects(regeffect)
                .residualsDiagnostics(diagnostic(res, model.getRatio()))
                .build();
    }

    private StateComponent noiseComponent(RawDisaggregationSpec spec) {
        switch (spec.getModelSpec().getResidualsModel()) {
            case Wn -> {
                return Noise.of(1);
            }
            case Ar1 -> {
                return AR1.of(spec.getModelSpec().getParameter().getValue(), 1, spec.getModelSpec().isZeroInitialization());
            }
            case RwAr1 -> {
                return Arima_1_1_0.of(spec.getModelSpec().getParameter().getValue(), 1, spec.getModelSpec().isZeroInitialization());
            }
            case Rw -> {
                return Rw.of(1, spec.getModelSpec().isZeroInitialization());
            }
            default ->
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private ISsfLoading noiseLoading(RawDisaggregationSpec spec) {
        switch (spec.getModelSpec().getResidualsModel()) {
            case Wn -> {
                return Noise.defaultLoading();
            }
            case Ar1 -> {
                return AR1.defaultLoading();
            }
            case RwAr1 -> {
                return Arima_1_1_0.defaultLoading();
            }
            case Rw -> {
                return Rw.defaultLoading();
            }
            default ->
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private SsfFunction<Parameter, Ssf> ssfFunction(RawDisaggregationModel model, RawDisaggregationSpec spec) {
        SsfData data = new SsfData(model.estimationY());
        Double lbound = spec.getEstimationSpec().getTruncatedParameter();
        Mapping mapping = new Mapping(lbound == null ? -1 : lbound);
        boolean cl = spec.getModelSpec().getResidualsModel() == ResidualsModel.Ar1;
        return SsfFunction.builder(data, mapping,
                p -> ssf(p.getValue(), cl, spec.getModelSpec().isZeroInitialization(), model.getRatio()))
                .regression(model.estimationXc().isEmpty() ? null : model.estimationXc(), diffuseRegressors(model.nx(), spec))
                .useMaximumLikelihood(true)
                .build();
    }

    private static Ssf ssf(double rho, boolean cl, boolean zeroinit, int ratio) {
        StateComponent cmp = cl ? AR1.of(rho, 1, zeroinit)
                : Arima_1_1_0.of(rho, 1, zeroinit);
        ISsfLoading loading = cl ? AR1.defaultLoading() : Arima_1_1_0.defaultLoading();
        return Ssf.of(SsfCumulator.of(cmp, loading, ratio, 0),
                SsfCumulator.defaultLoading(loading, ratio, 0));
    }

    private int diffuseRegressors(int nx, RawDisaggregationSpec spec) {
        if (spec.getModelSpec().isDiffuseRegressors()) {
            return nx;
        } else if (!spec.getModelSpec().getResidualsModel().isStationary() && spec.getModelSpec().isConstant()) // to be compatible with other specifications. Could be changed
        {
            return 1;
        } else {
            return 0;
        }
    }

    private DoubleSeq regeffect(RawDisaggregationModel model, DoubleSeq coeff) {
        FastMatrix X = model.getX();
        if (X.isEmpty()) {
            return DoubleSeq.empty();
        }
        DataBlock regs = DataBlock.make(model.getX().getRowsCount());
        regs.set(Double.NaN);
        regs.product(X.rowsIterator(), DataBlock.of(coeff));
        return regs;
    }

    private DoubleSeq residuals(RawDisaggregationModel model, DoubleSeq coeff, ISsf ssf) {
        DoubleSeq hy = model.estimationY();
        double[] y = hy.toArray();
        FastMatrix hx = model.estimationXc();
        if (!hx.isEmpty()) {
            for (int i = 0; i < y.length; ++i) {
                if (Double.isFinite(y[i])) {
                    y[i] = hy.get(i) - hx.row(i).dot(coeff);
                }
            }
        }
        DefaultDiffuseFilteringResults fr = DkToolkit.filter(ssf, new SsfData(y), false);
        DoubleSeq errors = fr.errors(true, false);
        int n = (errors.length() + model.getRatio() - 1) / model.getRatio();
        DataBlock err = DataBlock.of(errors.extract(model.getRatio() - 1, n, model.getRatio()).toArray());
        err.mul(1 / model.getYfactor());
        return err;
    }

    private RawResidualsDiagnostics diagnostic(DoubleSeq res, int ratio) {
        NiidTests tests = NiidTests.builder()
                .data(res)
                .period(ratio)
                .seasonal(false)
                .build();
        return RawResidualsDiagnostics.builder()
                .fullResiduals(res)
                .niid(tests)
                .build();

    }

    private static class Mapping implements IParametricMapping<Parameter> {

        private final double lbound;

        private Mapping(double lbound) {
            this.lbound = lbound;
        }

        @Override
        public Parameter map(DoubleSeq p) {
            return Parameter.estimated(p.get(0));
        }

        @Override
        public DoubleSeq getDefaultParameters() {
            return Doubles.of(.9);
        }

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            double p = inparams.get(0);
            if (lbound == -1) {
                return p > -1 && p < 1;
            } else {
                return p >= lbound && p < 1;
            }
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            return 1e-8;
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return lbound;
        }

        @Override
        public double ubound(int idx) {
            return 1;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            double p = ioparams.get(0);
            if (lbound == -1) {
                if (p > -1 && p < 1) {
                    return ParamValidation.Valid;
                } else {
                    if (p == 1) {
                        p = 1 - 1e-6;
                    } else if (p == -1) {
                        p = -1 + 1e-6;
                    } else {
                        p = 1 / p;
                    }
                    ioparams.set(p);
                    return ParamValidation.Changed;
                }
            } else if (p >= lbound && p < 1) {
                return ParamValidation.Valid;
            } else {
                if (p < lbound) {
                    p = lbound;
                } else if (p == -1) {
                    p = -1 + 1e-6;
                } else {
                    p = 1 / Math.abs(p);
                }
                ioparams.set(p);
                return ParamValidation.Changed;
            }
        }

    }
}
