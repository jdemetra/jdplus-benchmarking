package jdplus.benchmarking.base.core.multivariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.stats.samples.Moments;

/**
 * Shrinks only off-diagonal covariances.
 *
 * See Schäfer, J., and K. Strimmer. 2005. A shrinkage approach to large-scale covariance matrix estimation
 * and implications for functional genomics.
 *
 * @author LEMASSO
 */

@lombok.experimental.UtilityClass
public class CovarianceEstimator {

    public static FastMatrix sampleCovariance(FastMatrix X) {
        int n = X.getRowsCount();
        int p = X.getColumnsCount();

        FastMatrix cov = FastMatrix.square(p);

        DataBlock[] cols = new DataBlock[p];
        double[] means = new double[p];

        for (int i = 0; i < p; ++i) {
            cols[i] = X.column(i);
            means[i] = cols[i].average();
        }

        for (int i = 0; i < p; ++i) {
            for (int j = i; j < p; ++j) {
                double s = 0;
                for (int k = 0; k < n; ++k) {
                    s += (cols[i].get(k) - means[i]) * (cols[j].get(k) - means[j]);
                }
                double c = s / (n - 1);

                cov.set(i, j, c);
                cov.set(j, i, c);
            }
        }
        return cov;
    }

    // Same as sampleCovariance() but faster
    public static FastMatrix sampleCovariance2(FastMatrix X) {
        int n = X.getRowsCount();
        int p = X.getColumnsCount();

        FastMatrix cov = FastMatrix.square(p);

        DoubleSeq[] cols = new DoubleSeq[p];

        for (int i = 0; i < p; ++i) {
            cols[i] = X.column(i).removeMean();
        }

        for (int i = 0; i < p; ++i) {
            for (int j = i; j < p; ++j) {
                double s = cols[i].dot(cols[j]);
                double c = s / (n - 1);

                cov.set(i, j, c);
                cov.set(j, i, c);
            }
        }
        return cov;
    }

    public static FastMatrix covToCorr(FastMatrix covariance) {
        int n = covariance.getRowsCount();

        FastMatrix correlation = FastMatrix.make(n, n);

        DataBlock invStd = covariance.diagonal().deepClone();
        invStd.apply(x -> 1.0 / Math.sqrt(x));

        for (int i = 0; i < n; ++i) {
            correlation.set(i, i, 1.0);
            for (int j = i + 1; j < n; ++j) {
                double r = covariance.get(i, j) * invStd.get(i) * invStd.get(j);
                correlation.set(i, j, r);
                correlation.set(j, i, r);
            }
        }
        return correlation;
    }

    public static ShrinkageResults shrinkCovariance(FastMatrix data, FastMatrix covariance) {

        double lambda = estimateLambda(data);
        FastMatrix covarianceShrunk = shrinkCovariance(covariance, lambda);

        return ShrinkageResults.builder()
                .lambda(lambda)
                .covariance(covarianceShrunk)
                .build();
    }

    public static FastMatrix shrinkCovariance(FastMatrix cov, double lambda) {
        FastMatrix covShrunk = cov.deepClone();
        int p = covShrunk.getRowsCount();
        double f = 1.0 - lambda;

        for (int i = 0; i < p; ++i) {
            for (int j = i + 1; j < p; ++j) {
                double c = f * cov.get(i, j);
                covShrunk.set(i, j, c);
                covShrunk.set(j, i, c);
            }
        }
        return covShrunk;
    }


    public static double estimateLambda(FastMatrix X) {

        int n = X.getRowsCount();
        int p = X.getColumnsCount();

        if (n < 3 || p <= 1) {
            return 1.0;
        }

        // standardization
        FastMatrix Xn = X.deepClone();
        DataBlockIterator cols = Xn.columnsIterator();
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            double mean = Moments.mean(col);
            double std = Math.sqrt(Moments.variance(col, mean, true));
            if (std == 0) {
                return 1.0;
            }
            col.apply(a -> (a - mean) / std);
        }

        double numerator = 0;
        double denominator = 0;

        for (int i = 0; i < p; ++i) {
            for (int j = i + 1; j < p; ++j) {
                double r = 0;
                for (int k = 0; k < n; ++k) {
                    r += Xn.get(k, i) * Xn.get(k, j);
                }
                r /= (n - 1);

                denominator += r * r; // Sum_{i<j} r_ij^2

                double vr = 0;
                for (int k = 0; k < n; ++k) {
                    double w = Xn.get(k, i) * Xn.get(k, j);
                    double d = w - r;
                    vr += d * d;
                }

                vr *= (double) n / ((n - 1) * (n - 1) * (n - 1));

                numerator += vr; // Sum_{i<j} v(r_ij)
            }
        }

        if (denominator <= 0) {
            return 1.0;
        }

        return Math.max(0, Math.min(1, numerator / denominator));
    }

}
