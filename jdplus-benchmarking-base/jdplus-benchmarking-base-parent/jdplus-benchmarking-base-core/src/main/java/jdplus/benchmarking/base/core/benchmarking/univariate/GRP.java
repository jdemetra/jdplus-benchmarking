/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.benchmarking.univariate;

import jdplus.benchmarking.base.api.benchmarking.univariate.DentonSpec;
import jdplus.benchmarking.base.api.benchmarking.univariate.GrpSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.functions.IFunctionDerivatives;
import jdplus.toolkit.base.core.math.functions.IFunctionPoint;
import jdplus.toolkit.base.core.math.functions.bfgs.Bfgs;
import jdplus.toolkit.base.core.math.functions.DefaultDomain;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.GeneralMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;

/**
 * Growth rate preservation Algorithm based on the paper: A Newton's method for
 * benchmarking time series according to a Growth Rates Preservation Principle
 * by T. Di Fonzo and M. Marini (IMF WP/11/179)
 *
 * @author palatej
 */
public class GRP {

    private final int conversion, offset;
    private final GrpSpec spec;
    private final boolean flow;

    public GRP(GrpSpec spec, int conversion, int offset) {
        switch (spec.getAggregationType()) {
            case Last:
                this.offset = offset + conversion - 1;
                break;
            case UserDefined:
                this.offset = offset + spec.getObservationPosition();
                break;
            default:
                this.offset = offset;
        }
        this.conversion = conversion;
        this.spec = spec;
        this.flow = spec.getAggregationType() == AggregationType.Average
                || spec.getAggregationType() == AggregationType.Sum;
    }

    public double[] process(DoubleSeq highSeries, DoubleSeq lowSeries) {
        double[] start;
        int n = flow ? conversion * lowSeries.length() : (1 + conversion * (lowSeries.length() - 1));
        boolean avg = spec.getAggregationType() == AggregationType.Average;
        if (spec.isDentonInitialization()) {
            DentonSpec dspec = DentonSpec.builder()
                    .modified(true)
                    .multiplicative(true)
                    .differencing(1)
                    .aggregationType(avg ? AggregationType.Sum : spec.getAggregationType())
                    .observationPosition(0)
                    .buildWithoutValidation();
            MatrixDenton denton = new MatrixDenton(dspec, conversion, 0);
            start = denton.process(highSeries.range(offset, offset + n), lowSeries);
        } else {
            start = new double[n];
            if (flow) {
                addXbar(start, lowSeries.toArray(), conversion, flow);
            } else {
                init(start, lowSeries.toArray(), conversion);
            }
        }

        Bfgs bfgs = Bfgs.builder()
                .functionPrecision(spec.getPrecision())
                .maxIter(spec.getMaxIter())
                .build();
        FastMatrix K = FastMatrix.make(conversion, conversion - 1);
        K(K, flow);
        GRPFunction fn = new GRPFunction(highSeries.range(offset, offset + n).toArray(), lowSeries.toArray(), K, flow, spec.getObjective());
        IFunctionPoint ps = fn.evaluate(DoubleSeq.of(Ztx(start, K, flow)));
        if (!Double.isFinite(ps.getValue())) {
            init(start, lowSeries.toArray(), conversion);
            ps = fn.evaluate(DoubleSeq.of(Ztx(start, K, flow)));
        }
        bfgs.minimize(ps);
        GRPFunction.Point rslt = (GRPFunction.Point) bfgs.getResult();
        if (n == highSeries.length()) {
            return rslt.x;
        } else {
            double[] q = new double[highSeries.length()];
            System.arraycopy(rslt.x, 0, q, offset, n);
            if (offset > 0) {
                for (int i = offset; i > 0; --i) {
                    double r = highSeries.get(i - 1) / highSeries.get(i);
                    q[i - 1] = q[i] * r;
                }
            }
            for (int i = offset + n; i < q.length; ++i) {
                double r = highSeries.get(i) / highSeries.get(i - 1);
                q[i] = q[i - 1] * r;

            }
            return q;
        }
    }

    /**
     * Computes the gradient of the GRP objective function
     *
     * @param i index in the gradient (we compute df(x)/dx(i))
     * @param x current value of the objective function
     * @param p reference series (unbenchmarked)
     * @param objective objective function
     * @return
     */
    static double g(int i, double[] x, double[] p, GrpSpec.Objective objective) {
        
        if(objective == GrpSpec.Objective.Forward){
            if (i == 0) {
                return -2 * x[1] / (x[0] * x[0]) * (x[1] / x[0] - p[1] / p[0]);
            }
            int n = p.length - 1;
            if (i == n) {
                int nprev = n - 1;
                return 2 / x[nprev] * (x[n] / x[nprev] - p[n] / p[nprev]);
            } else {
                int iprev = i - 1, inext = i + 1;
                return 2 / x[iprev] * (x[i] / x[iprev] - p[i] / p[iprev])
                        - 2 * x[inext] / (x[i] * x[i]) * (x[inext] / x[i] - p[inext] / p[i]);
            }
            
        }else if (objective == GrpSpec.Objective.Backward){
            if (i == 0) {
                return 2 / x[1] * (x[0] / x[1] - p[0] / p[1]);
            }
            int n = p.length - 1;
            if (i == n) {
                int nprev = n - 1;
                return -2 * x[nprev] / (x[n] * x[n]) * (x[nprev] / x[n] - p[nprev] / p[n]);
            } else {
                int iprev = i - 1, inext = i + 1;
                return - 2 * x[iprev] / (x[i] * x[i]) * (x[iprev] / x[i] - p[iprev] / p[i]) +
                        2 / x[inext] * (x[i] / x[inext] - p[i] / p[inext]);
            }
            
        }else if (objective == GrpSpec.Objective.Symmetric){
            if (i == 0) {
                return - x[1] / (x[0] * x[0]) * (x[1] / x[0] - p[1] / p[0]) + 1 / x[1] * (x[0] / x[1] - p[0] / p[1]);
            }
            int n = p.length - 1;
            if (i == n) {
                int nprev = n - 1;
                return 1 / x[nprev] * (x[n] / x[nprev] - p[n] / p[nprev]) - x[nprev] / (x[n] * x[n]) * (x[nprev] / x[n] - p[nprev] / p[n]);
            } else {
                int iprev = i - 1, inext = i + 1;
                return 1 / x[iprev] * (x[i] / x[iprev] - p[i] / p[iprev])
                       - x[inext] / (x[i] * x[i]) * (x[inext] / x[i] - p[inext] / p[i])  
                       - x[iprev] / (x[i] * x[i]) * (x[iprev] / x[i] - p[iprev] / p[i])
                       + 1 / x[inext] * (x[i] / x[inext] - p[i] / p[inext]);
            }
            
        }else if (objective == GrpSpec.Objective.Log){
            if (i == 0) {
                return -2 / x[0] * (Math.log(x[1] / x[0]) - Math.log(p[1] / p[0]));
            }
            int n = p.length - 1;
            if (i == n) {
                int nprev = n - 1;
                return 2 / x[n] * (Math.log(x[n] / x[nprev]) - Math.log(p[n] / p[nprev]));
            } else {
                int iprev = i - 1, inext = i + 1;
                return 2 / x[i] * (Math.log(x[i] / x[iprev]) - Math.log(p[i] / p[iprev])
                        - Math.log(x[inext] / x[i]) + Math.log(p[inext] / p[i]));
            }
            
        }else{
            return 0;
        }
    }

    /**
     * Computes the hessian of the GRP objective function
     *
     * @param i index in the hessian (we compute d2f(x)/(dxi dxj))
     * @param j index in the hessian
     * @param x current value of the objective function
     * @param p reference series (unbenchmarked)
     * @param objective objective function
     * @return
     */
    static double h(int i, int j, double[] x, double[] p, GrpSpec.Objective objective) {
        
        if(objective == GrpSpec.Objective.Forward){
            if (i == j) {
                if (i == 0) {
                    return 2 * x[1] / (x[0] * x[0] * x[0]) * (3 * x[1] / x[0] - 2 * p[1] / p[0]);
                }
                int n = p.length - 1;
                if (i == n) {
                    int nprev = n - 1;
                    return 2 / (x[nprev] * x[nprev]);
                } else {
                    int iprev = i - 1, inext = i + 1;
                    return 2 / (x[iprev] * x[iprev])
                            + 2 * x[inext] / (x[i] * x[i] * x[i]) * (3 * x[inext] / x[i] - 2 * p[inext] / p[i]);
                }
            } else if (Math.abs(i - j) == 1) {
                int k = Math.min(i, j), knext = k + 1;
                return -2 / (x[k] * x[k]) * (2 * x[knext] / x[k] - p[knext] / p[k]);
            } else {
                return 0;
            }
            
        }else if (objective == GrpSpec.Objective.Backward){
            if (i == j) {
                if (i == 0) {
                    return 2 / (x[1] * x[1]);
                }
                int n = p.length - 1;
                if (i == n) {
                    int nprev = n - 1;
                    return 2 * x[nprev] / (x[i] * x[i] * x[i]) * (3 * x[nprev] / x[i] - 2 * p[nprev] / p[i]);
                } else {
                    int iprev = i - 1, inext = i + 1;
                    return 2 * x[iprev] / (x[i] * x[i] * x[i]) * (3 * x[iprev] / x[i] - 2 * p[iprev] / p[i]) + 
                            2 / (x[inext] * x[inext]);
                }
            } else if (Math.abs(i - j) == 1) {
                int k = Math.min(i, j), knext = k + 1;
                return -2 / (x[knext] * x[knext]) * (2 * x[k] / x[knext] - p[k] / p[knext]);
            } else {
                return 0;
            }
            
        }else if (objective == GrpSpec.Objective.Symmetric){
            if (i == j) {   
                if (i == 0) {
                    return x[1] / (x[0] * x[0] * x[0]) * (3 * x[1] / x[0] - 2 * p[1] / p[0]) + 1 / (x[1] * x[1]);
                }
                int n = p.length - 1;
                if (i == n) {
                    int nprev = n - 1;
                    return 1 / (x[nprev] * x[nprev])
                           + x[nprev] / (x[i] * x[i] * x[i]) * (3 * x[nprev] / x[i] - 2 * p[nprev] / p[i]);
                } else {
                    int iprev = i - 1, inext = i + 1;
                    return 1 / (x[iprev] * x[iprev])
                            + x[inext] / (x[i] * x[i] * x[i]) * (3 * x[inext] / x[i] - 2 * p[inext] / p[i])
                            + x[iprev] / (x[i] * x[i] * x[i]) * (3 * x[iprev] / x[i] - 2 * p[iprev] / p[i]) 
                            + 1 / (x[inext] * x[inext]);
                }
            } else if (Math.abs(i - j) == 1) {
                int k = Math.min(i, j), knext = k + 1;
                return -1 / (x[k] * x[k]) * (2 * x[knext] / x[k] - p[knext] / p[k])
                       - 1 / (x[knext] * x[knext]) * (2 * x[k] / x[knext] - p[k] / p[knext]);
            } else {
                return 0;
            }
            
        } else if (objective == GrpSpec.Objective.Log){
            if (i == j) {
                if (i == 0) {
                    return 2 * (Math.log(x[1] / x[0]) - Math.log(p[1] / p[0]) + 1) / (x[0] * x[0]);
                }
                int n = p.length - 1;
                if (i == n) {
                    int nprev = n - 1;
                    return 2 * (-Math.log(x[i] / x[nprev]) + Math.log(p[i] / p[nprev]) + 1) / (x[i] * x[i]);
                } else {
                    int iprev = i - 1, inext = i + 1;
                    return 2 * (-Math.log(x[i] / x[iprev]) + Math.log(p[i] / p[iprev]) 
                                + Math.log(x[inext] / x[i]) - Math.log(p[inext] / p[i]) + 2) / (x[i] * x[i]);
                }
            } else if (Math.abs(i - j) == 1) {
                int k = Math.min(i, j), knext = k + 1;
                return -2 / (x[k] * x[knext]);
            } else {
                return 0;
            }
            
        }else{
            return 0;
        }   
    }

    static double f(double[] x, double[] p, GrpSpec.Objective objective) {
        double s = 0;

        if(objective == GrpSpec.Objective.Forward){
            for (int i = 1; i < p.length; ++i) {
                if (x[i] <= 0) {
                    return Double.NaN;
                }
                double del = x[i] / x[i - 1] - p[i] / p[i - 1];
                s += del * del;
            }
        }else if (objective == GrpSpec.Objective.Backward){
            for (int i = 1; i < p.length; ++i) {
                if (x[i] <= 0) {
                    return Double.NaN;
                }
                double del = x[i - 1] / x[i] - p[i - 1] / p[i];
                s += del * del;
            }  
        }else if (objective == GrpSpec.Objective.Symmetric){
            for (int i = 1; i < p.length; ++i) {
                if (x[i] <= 0) {
                    return Double.NaN;
                }
                double delF = x[i] / x[i - 1] - p[i] / p[i - 1];
                double delB = x[i - 1] / x[i] - p[i - 1] / p[i];
                s += (delF * delF)/2 + (delB * delB)/2;
            }              
        }else if (objective == GrpSpec.Objective.Log){
            for (int i = 1; i < p.length; ++i) {
                if (x[i] <= 0) {
                    return Double.NaN;
                }
                double del = Math.log(x[i] / x[i - 1]) - Math.log(p[i] / p[i - 1]);
                s += del * del;
            }      
        }
        return s;
    }
    
    static void K(FastMatrix k, boolean flow) {
        if (flow) {
            int s = k.getRowsCount();
            DataBlockIterator cols = k.columnsIterator();
            int c = 0;

            while (cols.hasNext()) {
                double sm1 = s - 1;
                DataBlock col = cols.next();
                col.set(c++, Math.sqrt(sm1 / s));
                col.drop(c, 0).set(-Math.sqrt(1 / (s * sm1)));
                --s;
            }
        } else {
            k.subDiagonal(-1).set(1);
        }
    }

    /**
     * Gradient of the unconstrained problem
     *
     * @param x
     * @param p
     * @param K
     * @return
     */
    static double[] mg(double[] x, double[] p, FastMatrix K, GrpSpec.Objective objective) {
        int s = K.getRowsCount();
        int m = x.length / s; // x.length should be a multiple of s
        int n = m * (s - 1);
        double[] g = new double[n];
        double[] gcur = new double[s];
        for (int i = 0, j = 0, k = 0; i < m; ++i) {
            for (int l = 0; l < s; ++l) {
                gcur[l] = g(j++, x, p, objective);
            }
            for (int l = 0; l < s - 1; ++l) {
                DataBlock col = K.column(l);
                DoubleSeqCursor cursor = col.cursor();
                cursor.skip(l);
                double q = 0;
                for (int t = l; t < s; ++t) {
                    q += gcur[t] * cursor.getAndNext();
                }
                g[k++] = q;
            }
        }
        return g;
    }

    static double[] xbar(double[] b, int s) {
        double[] xbar = new double[b.length * s];
        for (int i = 0, j = 0; i < b.length; ++i) {
            double m = b[i] / s;
            for (int k = 0; k < s; ++j, ++k) {
                xbar[j] = m;
            }
        }
        return xbar;
    }

    static void addXbar(double[] x, double[] b, int s, boolean flow) {
        if (flow) {
            for (int i = 0, j = 0; i < b.length; ++i) {
                double m = b[i] / s;
                for (int k = 0; k < s; ++j, ++k) {
                    x[j] += m;
                }
            }
        } else {
            for (int i = 0, j = 0; i < b.length; ++i, j += s) {
                x[j] += b[i];
            }
        }
    }

    static void init(double[] x, double[] b, int s) {
        for (int i = 0, j = 0; i < b.length - 1; ++i) {
            for (int k = 0; k < s; ++k) {
                x[j++] = b[i];
            }
        }
        x[x.length - 1] = b[b.length - 1];
    }

    static double mf(double[] z, double[] p, double[] b, FastMatrix K, final boolean flow, GrpSpec.Objective objective) {
        double[] x = Zz(z, K, flow);
        addXbar(x, b, K.getRowsCount(), flow);
        return f(x, p, objective);
    }

    static double[] Ztx(double[] x, FastMatrix K, boolean flow) {
        int s = K.getRowsCount();
        if (flow) {
            int m = x.length / s; // x.length should be a multiple of s
            int n = m * (s - 1);
            double[] zx = new double[n];
            for (int i = 0, j = 0, k = 0; i < m; ++i, j += s) {
                boolean zero = true;
                for (int l = j; l < j + s; ++l) {
                    if (x[l] != 0) {
                        zero = false;
                        break;
                    }
                }
                if (!zero) {
                    for (int l = 0; l < s - 1; ++l) {
                        DataBlock col = K.column(l);
                        DoubleSeqCursor cursor = col.cursor();
                        cursor.skip(l);
                        double q = 0;
                        for (int t = l, u = j + l; t < s; ++t, ++u) {
                            q += x[u] * cursor.getAndNext();
                        }
                        zx[k++] = q;
                    }
                }
            }
            return zx;
        } else {
            int m = (x.length - 1) / s;
            int n = m * (s - 1);
            double[] zx = new double[n];
            for (int j = 0, k = 0; j < n;) {
                ++k;
                for (int l = 0; l < s - 1; ++l) {
                    zx[j++] = x[k++];
                }
            }
            return zx;
        }
    }

    static double[] Zz(double[] z, FastMatrix K, boolean flow) {
        int s = K.getRowsCount();
        if (flow) {
            int m = z.length / (s - 1); // x.length should be a multiple of s-1
            int n = m * s;
            double[] zz = new double[n];
            for (int i = 0, j = 0, k = 0; i < m; ++i, j += s - 1) {
                for (int l = 0; l < s; ++l) {
                    DataBlock row = K.row(l);
                    DoubleSeqCursor cursor = row.cursor();
                    double q = 0;
                    for (int t = 0, u = j; t < Math.min(s - 1, l + 1); ++t, ++u) {
                        q += z[u] * cursor.getAndNext();
                    }
                    zz[k++] = q;
                }
            }
            return zz;
        } else {
            int m = z.length / (s - 1);
            int n = z.length + m + 1;// we add the obs we suppressed in Ztx
            double[] zz = new double[n];
            for (int j = 0, k = 0; j < z.length;) {
                k++;
                for (int l = 0; l < s - 1; ++l) {
                    zz[k++] = z[j++];
                }
            }
            return zz;
        }
    }
}

class GRPFunction implements IFunction {

    private final double[] p, b;
    private final FastMatrix K;
    private final boolean flow;
    private final GrpSpec.Objective objective;
    
    GRPFunction(double[] p, double[] b, FastMatrix K, boolean flow, GrpSpec.Objective objective) {
        this.p = p;
        this.b = b;
        this.K = K;
        this.flow = flow;
        this.objective = objective;
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq ds) {
        return new Point(ds.toArray());
    }

    @Override
    public IParametersDomain getDomain() {
        return new DefaultDomain(p.length - b.length, 1e-6);
    }

    class Point implements IFunctionPoint {

        final double[] z;
        final double[] x;

        Point(double[] z) {
            this.z = z;
            this.x = GRP.Zz(z, K, flow);
            GRP.addXbar(x, b, K.getRowsCount(), flow);
        }

        @Override
        public IFunction getFunction() {
            return GRPFunction.this;
        }

        @Override
        public IFunctionDerivatives derivatives() {
            return new Derivatives(z, x);
        }

        @Override
        public DoubleSeq getParameters() {
            return DoubleSeq.of(z);
        }

        @Override
        public double getValue() {
            double f = GRP.f(x, p, objective);
            if (!Double.isFinite(f)) {
                return Double.MAX_VALUE;
            } else {
                return f;
            }
        }

    }

    class Derivatives implements IFunctionDerivatives {

        final double[] z, x;

        Derivatives(double[] z, double[] x) {
            this.z = z;
            this.x = x;
        }

        @Override
        public IFunction getFunction() {
            return GRPFunction.this;
        }

        @Override
        public DoubleSeq gradient() {
            double[] mg = GRP.mg(x, p, K, objective);
            return DoubleSeq.of(mg);
        }

        @Override
        public void hessian(FastMatrix matrix) {
            FastMatrix h = FastMatrix.square(x.length);
            DataBlock hd = h.diagonal();
            for (int i = 0; i < x.length; ++i) {
                hd.set(i, GRP.h(i, i, x, p, objective));
            }
            hd = h.subDiagonal(-1);
            for (int i = 0; i < x.length - 1; ++i) {
                hd.set(i, GRP.h(i, i + 1, x, p, objective));
            }
            h.subDiagonal(1).copy(hd);
            // H = Zt*h*Z
            FastMatrix H = FastMatrix.square(z.length);
            // diagonal blocks
            int s = K.getRowsCount();
            int m = x.length / s;
            for (int j = 0, k = 0; j < x.length; j += s, k += s - 1) {
                FastMatrix dh = h.extract(j, s, j, s);
                FastMatrix dH = H.extract(k, s - 1, k, s - 1);
                SymmetricMatrix.XtSX(dh, K, dH);
                if (j + s < x.length) {
                    dh = h.extract(j + s, s, j, s);
                    dH = H.extract(k + s - 1, s - 1, k, s - 1);
                    FastMatrix tmp = GeneralMatrix.AtB(K, dh);
                    GeneralMatrix.setABt(tmp, K, dH);
                    FastMatrix dH2 = H.extract(k, s - 1, k + s - 1, s - 1);
                    dH2.copyTranspose(dH);
                }
            }
        }

    }

}
