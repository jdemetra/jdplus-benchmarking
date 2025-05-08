/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.ssf;

import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ResultsRange;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.univariate.DefaultDisturbanceSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.DefaultFilteringResults;
import jdplus.toolkit.base.core.ssf.univariate.IDisturbanceSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsfError;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
public class DisturbanceSmoother2 {

    public static class Builder {

        private final ISsf ssf;
        private boolean rescaleVariance = false;
        private boolean calcVariance = true;

        public Builder(ISsf ssf) {
            this.ssf = ssf;
        }

        public Builder rescaleVariance(boolean rescale) {
            this.rescaleVariance = rescale;
            if (rescale) {
                calcVariance = true;
            }
            return this;
        }

        public Builder calcVariance(boolean calc) {
            this.calcVariance = calc;
            if (!calc) {
                rescaleVariance = false;
            }
            return this;
        }

        public DisturbanceSmoother2 build() {
            return new DisturbanceSmoother2(ssf, calcVariance, rescaleVariance);
        }
    }

    public static Builder builder(ISsf ssf) {
        return new Builder(ssf);
    }

    private final ISsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final ISsfError error;
    private final boolean calcvar, rescalevar;
    private IDisturbanceSmoothingResults srslts;
    private DefaultFilteringResults frslts;

    private double err, errVariance, esm, esmVariance, h;
    private DataBlock K, R, U;
    private FastMatrix N, UVar, S;
    private boolean missing;
    private int pos, stop;
    // temporary
    private DataBlock tmp;
    private double c, v;

    private DisturbanceSmoother2(ISsf ssf, boolean calcvar, boolean rescalevar) {
        this.ssf = ssf;
        this.calcvar = calcvar;
        this.rescalevar = rescalevar;
        dynamics = ssf.dynamics();
        loading = ssf.measurement().loading();
        error = ssf.measurement().error();
    }

    public boolean process(ISsfData data) {
        OrdinaryFilter2 filter = new OrdinaryFilter2();
        DefaultFilteringResults fresults = DefaultFilteringResults.light();
        fresults.prepare(ssf, stop, data.length());
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(0, data.length(), fresults);
    }

    /**
     *
     * @param results Thr filtering results should contain the information
     * necessary for the smoothing.
     * @return
     */
    public boolean process(@NonNull DefaultFilteringResults results) {
        ResultsRange range = results.getRange();
        return process(range.getStart(), range.getEnd(), results);
    }

    /**
     *
     * @param start
     * @param end
     * @param results The filtering results should contain the information
     * necessary for
     * @return
     */
    public boolean process(int start, int end, @NonNull DefaultFilteringResults results) {
        IDisturbanceSmoothingResults sresults;
        if (calcvar) {
            sresults = DefaultDisturbanceSmoothingResults.full(ssf.measurement().hasError());
        } else {
            sresults = DefaultDisturbanceSmoothingResults.light(ssf.measurement().hasError());
        }
        sresults.prepare(ssf, start, end);
        return process(start, end, results, sresults);
    }

    /**
     *
     * @param data
     * @param sresults Smoothing results. The caller is responsible of preparing
     * them!
     * @param stop
     * @return
     */
    public boolean process(ISsfData data, @NonNull IDisturbanceSmoothingResults sresults, final int stop) {
        OrdinaryFilter2 filter = new OrdinaryFilter2();
        DefaultFilteringResults fresults = DefaultFilteringResults.light();
        fresults.prepare(ssf, stop, data.length());
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(stop, data.length(), fresults, sresults);
    }

    public boolean process(final int start, final int end, DefaultFilteringResults results, IDisturbanceSmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        stop = start;
        pos = end;
        initSmoother(ssf);
        while (--pos >= stop) {
            loadInfo();
            if (iterate()) {
                srslts.saveSmoothedTransitionDisturbances(pos, U, UVar == null ? null : UVar);
                if (error != null) {
                    srslts.saveSmoothedMeasurementDisturbance(pos, esm, esmVariance);
                }
            }
        }
        if (rescalevar) {
            srslts.rescaleVariances(results.var());
        }
        return true;
    }

    public boolean resume(final int start) {
        stop = start;
        while (pos >= stop) {
            loadInfo();
            if (iterate()) {
                srslts.saveSmoothedTransitionDisturbances(pos, U, UVar);
                if (error != null) {
                    srslts.saveSmoothedMeasurementDisturbance(pos, esm, esmVariance);
                }
            }
            pos--;
        }
        return true;
    }

    public IDisturbanceSmoothingResults getResults() {
        return srslts;
    }

    public DataBlock getFinalR() {
        return R;
    }

    public FastMatrix getFinalN() {
        return N;
    }

    private void initSmoother(ISsf ssf) {
        int dim = ssf.getStateDim();
        int resdim = dynamics.getInnovationsDim();

        R = DataBlock.make(dim);
        K = DataBlock.make(dim);
        U = DataBlock.make(resdim);
        if (calcvar) {
            S = FastMatrix.make(dim, resdim);
            N = FastMatrix.square(dim);
            tmp = DataBlock.make(dim);
            UVar = FastMatrix.square(resdim);
            if (error == null) {
                h = 0;
            } else if (error.isTimeInvariant()) {
                h = error.at(0);
            }
            if (dynamics.isTimeInvariant()) {
                dynamics.S(0, S);
            }
        }
    }

    private void loadInfo() {
        err = frslts.error(pos);
        missing = !Double.isFinite(err);
        if (!missing) {
            errVariance = frslts.errorVariance(pos);
            K.setAY(1 / errVariance, frslts.M(pos));
            dynamics.TX(pos, K);
        }
        if (pos > 0) {
            if (!dynamics.isTimeInvariant() && S != null) {
                S.set(0);
                dynamics.S(pos - 1, S);
            }
            if (error != null && !error.isTimeInvariant()) {
                h = error.at(pos - 1);
            }
        }
    }

    private boolean iterate() {
        iterateR();
        if (calcvar) {
            iterateN();
        }
        if (pos > 0) {
            // updates the smoothed disturbances
            esm = c * h;
            if (dynamics.hasInnovations(pos - 1)) {
                dynamics.XS(pos - 1, R, U);
                if (calcvar) {
                    esmVariance = h - h * h * v;
                    // v(U) = I-S'NS
                    SymmetricMatrix.XtSX(N, S, UVar);
                    UVar.chs();
                    UVar.diagonal().add(1);
                }
            } else {
                U.set(0);
                if (calcvar) {
                    UVar.set(0);
                }
            }
        }
        return true;
    }
    // 

    /**
     *
     */
    private void iterateN() {
        if (!missing && errVariance != 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            // = Z'(t)*Z(t)/f(t) + (T' - Z'K')N(T - KZ)
            // =  Z'(t)*Z(t)(1/f(t) + K'NK) + T'NT - <T'NKZ>
            // 1. NK 
            tmp.product(N.rowsIterator(), K);
            // 2. v
            v = 1 / errVariance + tmp.dot(K);
            // 3. T'NK
            dynamics.XT(pos, tmp);
            // TNT
            tvt(N);
            loading.VpZdZ(pos, N, v);
            subZ(N.rowsIterator(), tmp);
            subZ(N.columnsIterator(), tmp);
        } else {
            tvt(N);
        }
        SymmetricMatrix.reenforceSymmetry(N);
    }

    /**
     *
     */
    private void iterateR() {
        // R(t-1)=(v/f + R(t)*K)Z + R(t)*T
        // R(t-1)=esm*Z +  R(t)*T
        if (!missing && errVariance != 0) {
            // RT
            c = (err / errVariance - R.dot(K));
            dynamics.XT(pos, R);
            loading.XpZd(pos, R, c);
        } else {
            dynamics.XT(pos, R);
            c = Double.NaN;
        }
    }

    private void tvt(FastMatrix N) {
        N.columns().forEach(col -> dynamics.XT(pos, col));
        N.rows().forEach(row -> dynamics.XT(pos, row));
    }

    private void subZ(DataBlockIterator rows, DataBlock b) {
        DoubleSeqCursor x = b.cursor();
        while (rows.hasNext()) {
            double cur = x.getAndNext();
            if (cur != 0) {
                loading.XpZd(pos, rows.next(), -cur);
            }
        }
    }

    public DataBlock firstSmoothedState() {
        int n = ssf.getStateDim();
        // initial state
        DataBlock a = DataBlock.make(n);
        FastMatrix Pf0 = FastMatrix.square(n);
        ssf.initialization().a0(a);
        ssf.initialization().Pf0(Pf0);
        // stationary initialization
        a.addProduct(R, Pf0.columnsIterator());
        return a;
    }
}
