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

import jdplus.benchmarking.base.core.ssf.SsfADL2;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.functions.IFunctionPoint;
import jdplus.toolkit.base.core.math.functions.ssq.ISsqFunction;
import jdplus.toolkit.base.core.math.functions.ssq.ISsqFunctionPoint;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihood;
import jdplus.toolkit.base.core.ssf.likelihood.ProfileLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class ADLFunction implements IFunction, ISsqFunction {

    private ADLDefinition definition;
    private DoubleSeq y;
    private FastMatrix X;
    private int ratio, startPosition;
    private double limit;
    private boolean marginal, log;

    @Override
    public Point evaluate(DoubleSeq ds) {
        return new Point(this, ds.get(0));
    }

    @Override
    public Point ssqEvaluate(DoubleSeq ds) {
        return new Point(this, ds.get(0));
    }

    @Override
    public IParametersDomain getDomain() {
        return new Domain(limit);
    }

    public static class Point implements IFunctionPoint, ISsqFunctionPoint {

        private final ADLFunction fn;
        private final double phi;
        private final MarginalLikelihood mll;
        private final ProfileLikelihood pll;
        private final DoubleSeq E;

        public Point(ADLFunction fn, double phi) {
            this.fn = fn;
            this.phi = phi;
            Ssf ssf = SsfADL2.ssfRepresentation(fn.getDefinition().withPhi(phi), fn.getX(), fn.getRatio(), fn.getStartPosition());
            SsfData data = new SsfData(fn.getY());
            DoubleSeq e;
            if (fn.isMarginal()) {
                mll = AkfToolkit.marginalLikelihoodComputer(true, true).compute(ssf, data);
                pll = null;
                e = mll.deviances();
            } else {
                mll = null;
                pll = AkfToolkit.profileLikelihoodComputer().compute(ssf, data);
                e = pll.deviances();
            }
            E = e;
        }

        @Override
        public IFunction getFunction() {
            return fn;
        }

        @Override
        public ISsqFunction getSsqFunction() {
            return fn;
        }

        @Override
        public double getValue() {
            if (fn.isLog()) {
                return mll != null ? -mll.logLikelihood() : -pll.logLikelihood();
            } else {
                return mll != null ? mll.ssq() * mll.factor() : pll.ssq() * pll.factor();
            }
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(phi);
        }

        public MarginalLikelihood marginalLikelihood() {
            return mll;
        }

        public ProfileLikelihood profileLikelihood() {
            return pll;
        }

        public double logLikelihood() {
            return mll != null ? mll.logLikelihood() : pll.logLikelihood();
        }

        @Override
        public DoubleSeq getE() {
            return E;
        }

        @Override
        public double getSsqE() {
            if (mll != null) {
                return mll.ssq() * mll.factor();
            } else if (pll != null) {
                return pll.ssq() * pll.factor();
            } else {
                return Double.NaN;
            }
        }
    }

    public static class Domain implements IParametersDomain {

        private static final double BOUNDARY = .999999, EPS = 1e-8;

        private final double limit;

        Domain(double limit) {
            this.limit = limit;
        }

        @Override
        public boolean checkBoundaries(DoubleSeq ds) {
            double e = ds.get(0);
            return e > limit && e < BOUNDARY;
        }

        @Override
        public double epsilon(DoubleSeq ds, int i) {
            double e = ds.get(0);
            return e > 0 ? -EPS : EPS;

        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int i) {
            return limit;
        }

        @Override
        public double ubound(int i) {
            return BOUNDARY;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            double p = ioparams.get(0);
            if (p < limit) {
                p = limit;
                ioparams.set(p);
                return ParamValidation.Changed;
            } else if (p > BOUNDARY) {
                p = 1 / p;
                if (p > BOUNDARY) {
                    p = 1 - 1e-6;
                }
                ioparams.set(p);
                return ParamValidation.Changed;
            } else {
                return ParamValidation.Valid;
            }
        }
    }
}
