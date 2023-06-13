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
package jdplus.benchmarking.base.core.ssf;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixStorage;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;

/**
 * SSF extended by regression variables in the transition equation with fixed
 * [or time varying] coefficients. [Time varying coefficients follow a
 * multi-variate random walk]. TODO
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TransitionRegSsf {

    public StateComponent of(StateComponent model, FastMatrix W, DoubleSeq W0) {
        return new StateComponent(new Xinitializer(model.initialization(), W0),
                new Xdynamics1(model.dim(), model.dynamics(), W));
    }

    public ISsfLoading defaultLoading(ISsf model) {
        return new Xloading(model.getStateDim(), model.loading());
    }

    public ISsfLoading defaultLoading(int mdim, ISsfLoading loading) {
        return new Xloading(mdim, loading);
    }

//    public StateComponent of(ISsf model, MatrixStorage X) {
//        int mdim = model.getStateDim(), nx = X.getMatrixColumnsCount();
//        return new StateComponent(new Xinitializer(model.initialization(), nx),
//                new Xdynamics(mdim, model.dynamics(), X));
//    }
    static class Xdynamics implements ISsfDynamics {

        private final int m, n;
        private final ISsfDynamics dyn;
        private final MatrixStorage s;

        /**
         *
         * @param m size of the state without the regression variables
         * @param dyn dynamics without the regression variables
         * @param x regression variables
         */
        Xdynamics(int m, ISsfDynamics dyn, MatrixStorage x) {
            this.dyn = dyn;
            this.m = m;
            this.s = x;
            this.n = x.getMatrixColumnsCount() + m;
        }

        @Override
        public int getInnovationsDim() {
            return dyn.getInnovationsDim();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            dyn.V(pos, qm.extract(0, m, 0, m));
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            dyn.S(pos, cm.extract(0, m, 0, cm.getColumnsCount()));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return dyn.hasInnovations(pos);
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return dyn.areInnovationsTimeInvariant();
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            dyn.T(pos, tr.extract(0, m, 0, m));
            FastMatrix xcur = s.matrix(pos);
            tr.extract(0, xcur.getRowsCount(), m, xcur.getColumnsCount())
                    .copy(xcur);
            tr.diagonal().drop(m, 0).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            DataBlock a0 = x.range(0, m);
            DataBlock a1 = x.range(m, n);
            // a0=T*a0
            dyn.TX(pos, a0);
            // a0+=X*a1
            FastMatrix xcur = s.matrix(pos);
            DoubleSeqCursor.OnMutable cursor = a0.cursor();
            DataBlockIterator rows = xcur.rowsIterator();
            while (rows.hasNext()) {
                cursor.setAndNext(rows.next().dot(a1));
            }
        }

//        @Override
//        public void TM(int pos, FastMatrix m) {
//            dyn.TM(pos, m.extract(0, this.m, 0, m.getColumnsCount()));
//        }
//
//        @Override
//        public void TVT(int pos, FastMatrix m) {
//            FastMatrix dz = m.extract(0, this.m, 0, this.m);
//            dyn.TVT(pos, dz);
//            FastMatrix hz = m.extract(0, this.m, this.m, nx);
//            dyn.TM(pos, hz);
//            FastMatrix cz = m.extract(this.m, nx, 0, this.m);
//            cz.copyTranspose(hz);
//        }
        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            DataBlock a0 = x.range(0, m);
            dyn.XS(pos, a0, xs);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            DataBlock a0 = x.range(0, m);
            dyn.addSU(pos, a0, u);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            DataBlock a0 = x.range(0, m);
            DataBlock a1 = x.range(m, n);
            // a1=a0*X+a1  
            FastMatrix xcur = s.matrix(pos);
            DoubleSeqCursor.OnMutable cursor = a1.cursor();
            DataBlockIterator cols = xcur.columnsIterator();
            while (cols.hasNext()) {
                cursor.setAndNext(cols.next().dot(a0));
            }
            // a0=a0*T
            dyn.XT(pos, a0);
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            dyn.addV(pos, p.extract(0, m, 0, m));
        }

        @Override
        public boolean isTimeInvariant() {
            return dyn.isTimeInvariant();
        }

    }

    static class Xdynamics1 implements ISsfDynamics {

        private final int m, n;
        private final ISsfDynamics dyn;
        private final FastMatrix s;

        /**
         *
         * @param m size of the state without the regression variables
         * @param dyn dynamics without the regression variables
         * @param x regression variables
         * @param q item of the state to modify with the regression variables
         */
        Xdynamics1(int m, ISsfDynamics dyn, FastMatrix x) {
            this.dyn = dyn;
            this.m = m;
            this.s = x;
            this.n = x.getColumnsCount() + m;
        }

        @Override
        public int getInnovationsDim() {
            return dyn.getInnovationsDim();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            dyn.V(pos, qm.extract(0, m, 0, m));
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            dyn.S(pos, cm.extract(0, m, 0, cm.getColumnsCount()));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return dyn.hasInnovations(pos);
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return dyn.areInnovationsTimeInvariant();
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            dyn.T(pos, tr.extract(0, m, 0, m));
            tr.row(0).drop(m, 0).copy(s.row(pos));
            tr.diagonal().drop(m, 0).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            DataBlock a0 = x.range(0, m);
            DataBlock a1 = x.range(m, n);
            // a0=T*a0
            dyn.TX(pos, a0);
            // a0+=X*a1
            a0.add(0, s.row(pos).dot(a1));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            DataBlock a0 = x.range(0, m);
            dyn.XS(pos, a0, xs);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            DataBlock a0 = x.range(0, m);
            dyn.addSU(pos, a0, u);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            DataBlock a0 = x.range(0, m);
            DataBlock a1 = x.range(m, n);
            // a1=a0*X+a1  
            a1.addAY(a0.get(0), s.row(pos));
            // a0=a0*T
            dyn.XT(pos, a0);
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            dyn.addV(pos, p.extract(0, m, 0, m));
        }

        @Override
        public boolean isTimeInvariant() {
            return dyn.isTimeInvariant();
        }

    }

//    static class Xdynamics1 implements ISsfDynamics {
//
//        private final int n;
//        private final ISsfDynamics dyn;
//        private final FastMatrix s;
//
//        /**
//         *
//         * @param m size of the state without the regression variables
//         * @param dyn dynamics without the regression variables
//         * @param x regression variables
//         */
//        Xdynamics1(ISsfDynamics dyn, FastMatrix x) {
//            this.dyn = dyn;
//            this.s = x;
//            this.n = x.getColumnsCount() + 1;
//        }
//
//        @Override
//        public int getInnovationsDim() {
//            return dyn.getInnovationsDim();
//        }
//
//        @Override
//        public void V(int pos, FastMatrix qm) {
//            dyn.V(pos, qm.extract(0, 1, 0, 1));
//        }
//
//        @Override
//        public void S(int pos, FastMatrix cm) {
//            dyn.S(pos, cm.extract(0, 1, 0, cm.getColumnsCount()));
//        }
//
//        @Override
//        public boolean hasInnovations(int pos) {
//            return dyn.hasInnovations(pos);
//        }
//
//        @Override
//        public boolean areInnovationsTimeInvariant() {
//            return dyn.areInnovationsTimeInvariant();
//        }
//
//        @Override
//        public void T(int pos, FastMatrix tr) {
//            dyn.T(pos, tr.extract(0, 1, 0, 1));
//            tr.row(0).drop(1, 0).copy(s.row(pos));
//            tr.diagonal().drop(1, 0).set(1);
//        }
//
//        @Override
//        public void TX(int pos, DataBlock x) {
//            DataBlock a0 = x.range(0, 1);
//            DataBlock a1 = x.range(1, n);
//            // a0=T*a0
//            dyn.TX(pos, a0);
//            // a0+=X*a1
//            x.add(0, a1.dot(s.row(pos)));
//        }
//
////        @Override
////        public void TM(int pos, FastMatrix m) {
////            dyn.TM(pos, m.extract(0, this.m, 0, m.getColumnsCount()));
////        }
////
////        @Override
////        public void TVT(int pos, FastMatrix m) {
////            FastMatrix dz = m.extract(0, this.m, 0, this.m);
////            dyn.TVT(pos, dz);
////            FastMatrix hz = m.extract(0, this.m, this.m, nx);
////            dyn.TM(pos, hz);
////            FastMatrix cz = m.extract(this.m, nx, 0, this.m);
////            cz.copyTranspose(hz);
////        }
//        @Override
//        public void XS(int pos, DataBlock x, DataBlock xs) {
//            DataBlock a0 = x.range(0, 1);
//            dyn.XS(pos, a0, xs);
//        }
//
//        @Override
//        public void addSU(int pos, DataBlock x, DataBlock u) {
//            DataBlock a0 = x.range(0, 1);
//            dyn.addSU(pos, a0, u);
//        }
//
//        @Override
//        public void XT(int pos, DataBlock x) {
//            DataBlock a0 = x.range(0, 1);
//            DataBlock a1 = x.range(1, n);
//            // a1=a0*X+a1  
//            a1.addAY(a0.get(0), s.row(pos));
//            // a0=a0*T
//            dyn.XT(pos, a0);
//        }
//
//        @Override
//        public void addV(int pos, FastMatrix p) {
//            dyn.addV(pos, p.extract(0, 1, 0, 1));
//        }
//
//        @Override
//        public boolean isTimeInvariant() {
//            return dyn.isTimeInvariant();
//        }
//
//    }
//    static class Xvardynamics implements ISsfDynamics {
//
//        private final int n, nx, ns;
//        private final ISsfDynamics dyn;
//        private final FastMatrix xvar;
//        private final MatrixStorage s;
//
//        private FastMatrix v00(FastMatrix v) {
//            return v.extract(0, n, 0, n);
//        }
//
//        private FastMatrix r0(FastMatrix m) {
//            return m.extract(0, n, 0, m.getColumnsCount());
//        }
//
//        private FastMatrix r1(FastMatrix m) {
//            return m.extract(n, nx, 0, m.getColumnsCount());
//        }
//
//        private FastMatrix v11(FastMatrix v) {
//            return v.extract(n, nx, n, nx);
//        }
//
//        private FastMatrix v01(FastMatrix v) {
//            return v.extract(0, n, n, nx);
//        }
//
//        private FastMatrix v10(FastMatrix v) {
//            return v.extract(n, nx, 0, n);
//        }
//
//        Xvardynamics(int n, ISsfDynamics dyn, FastMatrix xvar, MatrixStorage xs) {
//            this.dyn = dyn;
//            this.n = n;
//            this.nx = xvar.getColumnsCount();
//            this.xvar = xvar;
//            this.s = xs;
//            this.ns = xs.getMatrixColumnsCount();
//        }
//
//        @Override
//        public int getInnovationsDim() {
//            return dyn.getInnovationsDim() + xvar.getColumnsCount();
//        }
//
//        @Override
//        public void V(int pos, FastMatrix qm) {
//            dyn.V(pos, v00(qm));
//            v11(qm).copy(xvar);
//        }
//
//        @Override
//        public void S(int pos, FastMatrix cm) {
//            int m=dyn.getInnovationsDim();
//            dyn.S(pos, cm.extract(0, n, 0, m));
//            cm.extract(n, nx, m, s.getColumnsCount()).copy(s);
//        }
//
//        @Override
//        public boolean hasInnovations(int pos) {
//            return true;
//        }
//
//        @Override
//        public boolean areInnovationsTimeInvariant() {
//            return dyn.areInnovationsTimeInvariant();
//        }
//
//        @Override
//        public void T(int pos, FastMatrix tr) {
//            dyn.T(pos, tr.extract(0, n, 0, n));
//            tr.diagonal().drop(n, 0).set(1);
//        }
//
//        @Override
//        public void TX(int pos, DataBlock x) {
//            dyn.TX(pos, x.range(0, n));
//        }
//
//        @Override
//        public void TM(int pos, FastMatrix m) {
//            dyn.TM(pos, r0(m));
//        }
//
//        @Override
//        public void TVT(int pos, FastMatrix m) {
//            dyn.TVT(pos, v00(m));
//            FastMatrix v01 = v01(m);
//            dyn.TM(pos, v01);
//            v10(m).copyTranspose(v01);
//        }
//
//        @Override
//        public void XS(int pos, DataBlock x, DataBlock xs) {
//            DataWindow xleft = x.left(), xsleft = xs.left();
//            dyn.XS(pos, xleft.next(n), xsleft.next(dyn.getInnovationsDim()));
//            xsleft.next(this.s.getColumnsCount()).product(xleft.next(nx), this.s.columnsIterator());
//        }
//
//        @Override
//        public void addSU(int pos, DataBlock x, DataBlock u) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public void XT(int pos, DataBlock x) {
//            dyn.XT(pos, x.range(0, n));
//        }
//
//        @Override
//        public void addV(int pos, FastMatrix p) {
//            dyn.addV(pos, v00(p));
//            v11(p).add(xvar);
//        }
//
//        @Override
//        public boolean isTimeInvariant() {
//            return dyn.isTimeInvariant();
//        }
//    }
//
    static class Xinitializer implements ISsfInitialization {

        private final int n;
        private final DoubleSeq w0;
        private final ISsfInitialization init0;

        Xinitializer(ISsfInitialization init, DoubleSeq w0) {
            this.init0 = init;
            this.n = init.getStateDim();
            this.w0 = w0;
        }

        @Override
        public int getStateDim() {
            return n + w0.length();
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getDiffuseDim() {
            return w0.length() + init0.getDiffuseDim();
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            int nd = init0.getDiffuseDim();
            if (nd > 0) {
                init0.diffuseConstraints(b.extract(0, n, 0, nd));
            }
            b.row(0).drop(nd, 0).copy(w0);
            b.extract(init0.getStateDim(), w0.length(), nd, w0.length()).diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
            init0.a0(a0.range(0, n));
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            init0.Pf0(pf0.extract(0, n, 0, n));
        }

    }

    static class Xloading implements ISsfLoading {

        private final ISsfLoading loading;
        private final int q;

        Xloading(final int m, final ISsfLoading loading) {
            this.loading = loading;
            this.q = m;
        }

        @Override
        public void Z(int pos, DataBlock db) {
            loading.Z(pos, db.range(0, q));
        }

        @Override
        public double ZX(int pos, DataBlock db) {
            return loading.ZX(pos, db.range(0, q));
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            loading.ZM(pos, m.extract(0, q, 0, m.getColumnsCount()), zm);
        }

        @Override
        public void MZt(int pos, FastMatrix m, DataBlock zm) {
            loading.MZt(pos, m.extract(0, m.getRowsCount(), 0, q), zm);
        }

        @Override
        public double ZVZ(int pos, FastMatrix v) {
            return loading.ZVZ(pos, v.extract(0, q, 0, q));
        }

        @Override
        public void VpZdZ(int pos, FastMatrix v, double d) {
            loading.VpZdZ(pos, v.extract(0, q, 0, q), d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            loading.XpZd(pos, x.range(0, q), d);
        }

        @Override
        public boolean isTimeInvariant() {
            return loading.isTimeInvariant();
        }

    }

}
