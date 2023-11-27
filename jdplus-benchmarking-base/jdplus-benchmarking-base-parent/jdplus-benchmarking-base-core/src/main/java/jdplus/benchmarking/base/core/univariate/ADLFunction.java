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

import jdplus.benchmarking.base.core.ssf.SsfADL;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.functions.IFunctionPoint;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName ="Builder", toBuilder=true)
public class ADLFunction implements IFunction {

    private ADLDefinition definition;
    private DoubleSeq y;
    private FastMatrix X;
    private int ratio, startPosition;
    private double limit;

    @Override
    public Point evaluate(DoubleSeq ds) {
        return new Point(this, ds.get(0));
    }

    @Override
    public IParametersDomain getDomain() {
        return new Domain(limit);
    }

    public static class Point implements IFunctionPoint {

        private final ADLFunction fn;
        private final double phi;
        private final MarginalLikelihood ll;

        public Point(ADLFunction fn, double phi) {
            this.fn=fn;
            this.phi = phi;
            Ssf ssf = SsfADL.ssfRepresentation(fn.getDefinition().withPhi(phi), fn.getX(), fn.getRatio(), fn.getStartPosition());
            SsfData data = new SsfData(fn.getY());
            ll = DkToolkit.marginalLikelihood(ssf, data, true, true);
        }

        @Override
        public IFunction getFunction() {
            return fn;
        }

        @Override
        public double getValue() {
            return -ll.logLikelihood();
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(phi);
        }
        
        public MarginalLikelihood likelihood(){
            return ll;
        }
    }

    public static class Domain implements IParametersDomain {

        private static final double BOUNDARY = .9999999, EPS = 1e-8;
        
        private final double limit;
        
        Domain(double limit){
            this.limit=limit;
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
                p =  limit;
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
