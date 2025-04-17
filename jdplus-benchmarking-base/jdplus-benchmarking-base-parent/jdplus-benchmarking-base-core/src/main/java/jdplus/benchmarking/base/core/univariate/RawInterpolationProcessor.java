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

import jdplus.benchmarking.base.api.univariate.RawInterpolationSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
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
public class RawInterpolationProcessor {

    @lombok.Value
    @lombok.AllArgsConstructor
    private static class RawInterpolationEstimation {

        ObjectiveFunctionPoint ml;
        DiffuseConcentratedLikelihood dll;
        StateComponent noise;
        ISsfLoading loading;
    }

    public RawDisaggregationResults process(@NonNull DoubleSeq y, @NonNull FastMatrix regressors, @NonNegative int startOffset, @NonNull RawInterpolationSpec spec) {
        RawInterpolationModelBuilder builder = new RawInterpolationModelBuilder(y, regressors, startOffset, spec);
        RawInterpolationModel yx = builder.build();
        return compute(yx, spec);
    }

    public RawDisaggregationResults process(@NonNull DoubleSeq y, int nBackcasts, int nForecasts, @NonNull RawInterpolationSpec spec) {
        RawInterpolationModelBuilder builder = new RawInterpolationModelBuilder(y, spec, nBackcasts, nForecasts);
        RawInterpolationModel yx = builder.build();
        return compute(yx, spec);
    }

    private RawDisaggregationResults compute(RawInterpolationModel model, RawInterpolationSpec spec) {
        return spec.isFast() ? interpolate2(model, spec) : interpolate(model, spec);
    }

    private RawDisaggregationResults interpolate2(RawInterpolationModel model, RawInterpolationSpec spec) {
        RawInterpolationEstimation eim = estimateInterpolationModel(model, spec);
        double[] yh = new double[model.getHy().length()];
        double[] eyh = new double[yh.length];

        interpolateEstimation(model, eim, yh, eyh);

        double yfac = model.getYfactor();
        double[] xfac = model.getXfactors();

        DiffuseConcentratedLikelihood dll = eim.getDll();
        Ssf ssf = Ssf.of(eim.getNoise(), eim.getLoading());
        DoubleSeq res = residuals(model, dll.coefficients(), ssf);

        // correct the ll (and the coeff) with the scaling factors
        dll = dll.rescale(yfac, xfac);
        DoubleSeq regeffect = regeffect(model, dll.coefficients());
        int nparams = spec.isParameterEstimation() ? 1 : 0;
        return RawDisaggregationResults.builder()
                .series(model.getY())
                .regressors(model.getX())
                .maximum(eim.getMl())
                .likelihood(dll)
                .hyperParametersCount(nparams)
                .stats(dll.stats(0, nparams))
                .disaggregatedSeries(DoubleSeq.of(yh))
                .stdevDisaggregatedSeries(DoubleSeq.of(eyh))
                .regressionEffects(regeffect)
                .residualsDiagnostics(diagnostic(res, model.getRatio()))
                .build();
    }

    private RawDisaggregationResults interpolate(RawInterpolationModel model, RawInterpolationSpec spec) {
        RawInterpolationEstimation edm = estimateInterpolationModel(model, spec);
        Ssf nmodel = Ssf.of(edm.getNoise(), edm.getLoading());
        DiffuseConcentratedLikelihood dll = edm.getDll();

        ISsf rssf = RegSsf.ssf(nmodel, model.getX());
        SsfData ssfdata = new SsfData(model.getHy());
        DefaultSmoothingResults srslts;
        srslts = switch (spec.getAlgorithm()) {
            case Augmented ->
                AkfToolkit.smooth(rssf, ssfdata, true, false, false);
            case SqrtDiffuse ->
                DkToolkit.sqrtSmooth(rssf, ssfdata, true, false);
            case Augmented_NoCollapsing ->
                AkfToolkit.smooth(rssf, ssfdata, true, false, true);
            case Augmented_Robust ->
                AkfToolkit.robustSmooth(rssf, ssfdata, true, false).getSmoothing();
            default ->
                DkToolkit.smooth(rssf, ssfdata, true, false);
        };
        double[] Y = model.getHy().toArray();
        double[] O = model.getHo().toArray();
        double[] yh = new double[Y.length];
        double[] vyh = new double[Y.length];
        ISsfLoading loading = rssf.loading();
        double f = 1 / model.getYfactor();
        double sigma = f * Math.sqrt(dll.ssq() / dll.dim());
        for (int i = 0, j = 0; i < O.length; ++i, ++j) {
            if (Double.isFinite(O[j])) {
                yh[i] = O[j];
                vyh[i] = 0;
            } else {
                yh[i] = f * loading.ZX(j, srslts.a(j));
                double v = loading.ZVZ(j, srslts.P(j));
                vyh[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
            }
        }
        DoubleSeq res = residuals(model, dll.coefficients(), nmodel);
        // correct the ll (and the coeff) with the scaling factors
        dll = dll.rescale(model.getYfactor(), model.getXfactors());
        DoubleSeq regeffect = regeffect(model, dll.coefficients());
        int nparams = spec.isParameterEstimation() ? 1 : 0;
        return RawDisaggregationResults.builder()
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

    // Estimate the interpolation  model
    private RawInterpolationEstimation estimateInterpolationModel(RawInterpolationModel model, RawInterpolationSpec spec) {
        StateComponent ncmp = noiseComponent(spec);
        ISsfLoading nloading = noiseLoading(spec);
        int diffuse = diffuseRegressors(model.nx(), spec);
        if (!spec.isParameterEstimation()) {
            Ssf ssf = Ssf.of(ncmp, nloading);
            SsfData ssfdata = new SsfData(model.estimationY());
            SsfRegressionModel ssfmodel = new SsfRegressionModel(ssf, ssfdata, model.estimationX().isEmpty() ? null : model.estimationX(), diffuse);
            return new RawInterpolationEstimation(
                    null,
                    DkToolkit.concentratedLikelihoodComputer(true, false, true).compute(ssfmodel),
                    ncmp,
                    nloading
            );
        } else {
            SsfFunction<Parameter, Ssf> fn = ssfFunction(model, spec);
            SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
                    .builder()
                    .build();
            double start = spec.getParameter().getType() == ParameterType.Undefined
                    ? .9 : spec.getParameter().getValue();
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
            ncmp = rslt.getSsf().asComponent();
            return new RawInterpolationEstimation(
                    ml, dll, ncmp, nloading
            );
        }
    }

    private void interpolateEstimation(RawInterpolationModel model, RawInterpolationEstimation estimation, final double[] z, final double[] e) {
        double[] hy = model.getHy().toArray();
        FastMatrix hX = model.getX();
        double[] O = model.getHo().toArray();
        double f = 1 / model.getYfactor();

        StateComponent ncmp = estimation.getNoise();
        ISsfLoading nloading = estimation.getLoading();
        Ssf ssf = Ssf.of(ncmp, nloading);
        DiffuseSmoother smoother = DiffuseSmoother.builder(ssf)
                .calcVariance(true)
                .rescaleVariance(false)
                .build();
        DefaultSmoothingResults srslts = DefaultSmoothingResults.full();
        srslts.prepare(ssf.getStateDim(), 0, hy.length);
        DiffuseConcentratedLikelihood dll = estimation.getDll();
        double sigma = f * Math.sqrt(dll.sigma2());
        if (hX == null || hX.isEmpty()) {
            SsfData data = new SsfData(hy);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(ssf, data, true);
            smoother.process(hy.length, frslts, srslts);
            for (int i = 0; i < z.length; ++i) {
                if (Double.isFinite(hy[i])) {
                    z[i] = O[i];
                    e[i] = 0;
                } else {
                    z[i] = f * nloading.ZX(i, srslts.a(i));
                    double v = nloading.ZVZ(i, srslts.P(i));
                    e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
                }
            }
        } else {
            // Xb
            DoubleSeq b = dll.coefficients();
            DataBlock Xb = DataBlock.make(hX.getRowsCount());
            DoubleSeqCursor bcur = b.cursor();
            DataBlockIterator xcols = hX.columnsIterator();
            double c = bcur.getAndNext();
            Xb.setAY(c, xcols.next());
            while (xcols.hasNext()) {
                Xb.addAY(bcur.getAndNext(), xcols.next());
            }
            // u=y-Xb
            DataBlock u = DataBlock.copyOf(hy);
            u.sub(Xb);

            //L(y-Xb)
            SsfData data = new SsfData(u);
            DefaultDiffuseFilteringResults frslts = DkToolkit.filter(ssf, data, true);
            smoother.process(hy.length, frslts, srslts);

            // Z = L(y-Xb) + Xb  
            // V = V(L(y-Xb)) + (LX-X) V(B) (LX-X)'
            FastMatrix Vb = dll.unscaledCovariance();
            FastMatrix LhX = FastMatrix.make(hX.getRowsCount(), hX.getColumnsCount());
            DataBlockIterator lxcols = LhX.columnsIterator();
            xcols.reset();
            FastDkSmoother fsmoother = new FastDkSmoother(ssf, frslts);
            while (xcols.hasNext()) {
                fsmoother.smooth(xcols.next());
                DataBlockResults ss = fsmoother.smoothedStates();
                lxcols.next().set(i -> nloading.ZX(i, ss.datablock(i)));
            }

            LhX.sub(hX);
            for (int i = 0; i < z.length; ++i) {
                if (Double.isFinite(hy[i])) {
                    z[i] = O[i];
                    e[i] = 0;
                } else {
                    z[i] = f * (nloading.ZX(i, srslts.a(i)) + Xb.get(i));
                    double v = nloading.ZVZ(i, srslts.P(i));
                    v += QuadraticForm.apply(Vb, LhX.row(i));
                    e[i] = v <= 0 ? 0 : sigma * Math.sqrt(v);
                }
            }
        }
    }

    private StateComponent noiseComponent(RawInterpolationSpec spec) {
        switch (spec.getResidualsModel()) {
            case Wn -> {
                return Noise.of(1);
            }
            case Ar1 -> {
                return AR1.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization());
            }
            case RwAr1 -> {
                return Arima_1_1_0.of(spec.getParameter().getValue(), 1, spec.isZeroInitialization());
            }
            case Rw -> {
                return Rw.of(1, spec.isZeroInitialization());
            }
            default ->
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private ISsfLoading noiseLoading(RawInterpolationSpec spec) {
        switch (spec.getResidualsModel()) {
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

    private SsfFunction<Parameter, Ssf> ssfFunction(RawInterpolationModel model, RawInterpolationSpec spec) {
        SsfData data = new SsfData(model.estimationY());
        Double lbound = spec.getTruncatedParameter();
        Mapping mapping = new Mapping(lbound == null ? -1 : lbound);
        boolean cl = spec.getResidualsModel() == ResidualsModel.Ar1;
        return SsfFunction.builder(data, mapping,
                p -> ssf(p.getValue(), cl, spec.isZeroInitialization(), model.getRatio()))
                .regression(model.estimationX().isEmpty() ? null : model.estimationX(), diffuseRegressors(model.nx(), spec))
                .useMaximumLikelihood(true)
                .build();
    }

    private static Ssf ssf(double rho, boolean cl, boolean zeroinit, int ratio) {
        StateComponent cmp = cl ? AR1.of(rho, 1, zeroinit)
                : Arima_1_1_0.of(rho, 1, zeroinit);
        ISsfLoading loading = cl ? AR1.defaultLoading() : Arima_1_1_0.defaultLoading();
        return Ssf.of(cmp, loading);
    }

    private int diffuseRegressors(int nx, RawInterpolationSpec spec) {
        if (spec.isDiffuseRegressors()) {
            return nx;
        } else if (!spec.getResidualsModel().isStationary() && spec.isConstant()) // to be compatible with other specifications. Could be changed
        {
            return 1;
        } else {
            return 0;
        }
    }

    private DoubleSeq regeffect(RawInterpolationModel model, DoubleSeq coeff) {
        FastMatrix X = model.getX();
        if (X.isEmpty()) {
            return DoubleSeq.empty();
        }
        DataBlock regs = DataBlock.make(model.getX().getRowsCount());
        regs.product(X.rowsIterator(), DataBlock.of(coeff));
        return regs;
    }

    private DoubleSeq residuals(RawInterpolationModel model, DoubleSeq coeff, ISsf ssf) {
        DoubleSeq hy = model.estimationY();
        double[] y = hy.toArray();
        FastMatrix hx = model.estimationX();
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
        DataBlock err = DataBlock.of(errors.extract(0, n, model.getRatio()).toArray());
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
