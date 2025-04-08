/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.benchmarking.univariate;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class BenchmarkingUtility {

    public TsData constraintsByPosition(TsData highFreqSeries, TsData aggregationConstraint, int pos) {
        TsDomain adom = highFreqSeries.getDomain().aggregateByPosition(aggregationConstraint.getTsUnit(), pos);
        adom = adom.intersection(aggregationConstraint.getDomain());
        return TsData.fitToDomain(aggregationConstraint, adom);
    }

    public DoubleSeq constraintsByPosition(DoubleSeq highFreqSeries, DoubleSeq aggregationConstraint, int ratio, int offset, int pos) {
        DoubleSeq naggregationConstraint;
        int nt = aggregationConstraint.length();
        int ns = highFreqSeries.length();
        int nMin = offset + (nt - 1) * ratio + (pos + 1);
        if (ns < nMin) {
            int nOut = (nMin - ns) / ratio + 1;
            naggregationConstraint = aggregationConstraint.range(0, nt - nOut);
        } else {
            naggregationConstraint = aggregationConstraint;
        }
        return (naggregationConstraint);
    }

    public TsData constraints(TsData highFreqSeries, TsData aggregationConstraint) {
        TsDomain adom = highFreqSeries.getDomain().aggregate(aggregationConstraint.getTsUnit(), true);
        adom = adom.intersection(aggregationConstraint.getDomain());
        return TsData.fitToDomain(aggregationConstraint, adom);
    }

    /**
     * TODO: Depends on the aggregation type; ok for last, sum and average, not
     * always correct for fist and user-defined Same remark for the previous
     * method
     *
     * @param highFreqSeries
     * @param aggregationConstraint
     * @param ratio Aggregation ratio
     * @param offset Difference between the start of the high-frequency series
     * and the start of the first aggregation period. Positive if the high-freq
     * series starts before the aggregation constraints, negative otherwise
     * @return
     */
    public DoubleSeq constraints(DoubleSeq highFreqSeries, DoubleSeq aggregationConstraint, int ratio, int offset) {
        DoubleSeq naggregationConstraint;
        int nt = aggregationConstraint.length();
        int ns = highFreqSeries.length();
        int nMin = offset + nt * ratio;
        if (ns < nMin) {
            int nOut = (nMin - ns - 1) / ratio + 1;
            naggregationConstraint = aggregationConstraint.range(0, nt - nOut);
        } else {
            naggregationConstraint = aggregationConstraint;
        }
        return (naggregationConstraint);
    }

    public TsData highFreqConstraints(TsData highFreqSeries, TsData aggregationConstraint) {
        TsDomain adom = highFreqSeries.getDomain().aggregate(aggregationConstraint.getTsUnit(), true);
        adom = adom.intersection(aggregationConstraint.getDomain());
        TsData cnt = TsData.fitToDomain(aggregationConstraint, adom);
        double[] x = new double[highFreqSeries.length()];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }
        int start = highFreqSeries.getDomain().indexOf(adom.start());
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        DoubleSeqCursor cursor = cnt.getValues().cursor();
        for (int i = 0, j = start + ratio - 1; i < cnt.length(); ++i, j += ratio) {
            x[j] = cursor.getAndNext();
        }

        return TsData.ofInternal(highFreqSeries.getStart(), x);
    }

    public TsData highFreqConstraintsByPosition(TsData highFreqSeries, TsData aggregationConstraint, int pos) {
        TsDomain adom = highFreqSeries.getDomain().aggregateByPosition(aggregationConstraint.getTsUnit(), pos);
        adom = adom.intersection(aggregationConstraint.getDomain());
        TsData cnt = TsData.fitToDomain(aggregationConstraint, adom);
        double[] x = new double[highFreqSeries.length()];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }
        int start = highFreqSeries.getDomain().indexOf(adom.start());
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        DoubleSeqCursor cursor = cnt.getValues().cursor();
        for (int i = 0, j = start + pos; i < cnt.length(); ++i, j += ratio) {
            x[j] = cursor.getAndNext();
        }
        return TsData.ofInternal(highFreqSeries.getStart(), x);
    }

    public DoubleSeq highFreqConstraints(DoubleSeq highFreqSeries, DoubleSeq aggregationConstraint, int ratio, int offset) {
        double[] x = new double[highFreqSeries.length()];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }
        DoubleSeqCursor cursor = aggregationConstraint.cursor();
        for (int i = 0, j = offset + ratio - 1; i < aggregationConstraint.length(); ++i, j += ratio) {
            x[j] = cursor.getAndNext();
        }
        return DoubleSeq.of(x);
    }

    public DoubleSeq highFreqConstraintsByPosition(DoubleSeq highFreqSeries, DoubleSeq aggregationConstraint, int pos, int ratio, int offset) {
        double[] x = new double[highFreqSeries.length()];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }
        DoubleSeqCursor cursor = aggregationConstraint.cursor();
        for (int i = 0, j = offset + pos; i < aggregationConstraint.length(); ++i, j += ratio) {
            x[j] = cursor.getAndNext();
        }
        return DoubleSeq.of(x);
    }

    public TsData biRatio(TsData highFreqSeries, TsData aggregationConstraint, AggregationType agg) {
        TsData H = highFreqSeries.aggregate(aggregationConstraint.getTsUnit(), agg, true);
        return TsData.divide(aggregationConstraint, H);
    }

    public TsData biRatio(TsData highFreqSeries, TsData aggregationConstraint, int pos) {
        TsData H = highFreqSeries.aggregateByPosition(aggregationConstraint.getTsUnit(), pos);
        return TsData.divide(aggregationConstraint, H);
    }

    public DoubleSeq aggregate(DoubleSeq input, int ratio, int startPos, AggregationType type, int pos) {
        int start = startPos % ratio, del = 0;
        if (start != 0) {
            del = ratio - del;
        }

        switch (type) {
            case First -> {
                return input.extract(startPos + del, -1, ratio);
            }
            case Last -> {
                return input.extract(startPos + del > 0 ? del - 1 : ratio - 1, -1, ratio);
            }
            case UserDefined -> {
                if (start < pos) {
                    startPos += pos - start;
                } else if (start > pos);
                startPos += pos - start + ratio;
                return input.extract(startPos, -1, ratio);
            }
        }

        startPos += del;
        int n = (input.length() - startPos) / ratio;
        double[] s = new double[n];
        for (int i = 0; i < n; ++i, startPos += ratio) {
            double cur = input.extract(startPos, ratio).sum();
            if (type == AggregationType.Average) {
                cur /= ratio;
            }
            s[i] = cur;
        }
        return DoubleSeq.of(s);
    }
}
