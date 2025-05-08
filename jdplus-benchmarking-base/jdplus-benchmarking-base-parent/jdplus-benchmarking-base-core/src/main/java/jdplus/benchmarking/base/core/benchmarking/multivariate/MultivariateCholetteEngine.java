/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.base.core.benchmarking.multivariate;

import internal.ssf.FastStateSmoother2;
import jdplus.benchmarking.base.core.ssf.ContemporaneousSsfCholette;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfCholette;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.benchmarking.multivariate.MultivariateCholetteSpec;
import jdplus.benchmarking.base.api.benchmarking.multivariate.TemporalConstraint;
import jdplus.benchmarking.base.api.benchmarking.univariate.CholetteSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.util.WeightedItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.benchmarking.base.core.benchmarking.univariate.CholetteProcessor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataView;

/**
 *
 * @author Jean Palate
 */
class MultivariateCholetteEngine {
    /**
     * Inputs
     */
    private final LinkedHashMap<String, TsData> inputs = new LinkedHashMap<>();
    /**
     * List of the contemporaneous constraints
     */
    private final List<ContemporaneousConstraint> contemporaneousConstraints
            = new ArrayList<>();
    /**
     * Map of the temporal constraints The map constains pairs of (detail,
     * aggregated series)
     */
    private final Map<String, String> temporalConstraints
            = new HashMap<>();
    /**
     * List of exogeneous series (not benchmarked) used in the contemporaneous
     * constraints, which appear in the left-side of contemporaneous definitions
     */
    private final ArrayList<String> lcnt = new ArrayList<>();
    /**
     * List of endogeneous series (benchmarked) used in the contemporaneous
     * constraints, which appear in the right-side of contemporaneous
     * definitions.
     *
     */
    private final ArrayList<String> rcnt = new ArrayList<>();
    /**
     * Data of the series in the lcnt list. The length of each array is equal to
     * the length of idomain
     */
    private double[][] lcntData;
    /**
     * Data of the series in the rcnt list The length of each array is equal to
     * the length of idomain
     */
    private double[][] rcntData;
    /**
     *
     */
    private double[][] weights;
    private final HashMap<String, TsData> tcntData = new HashMap<>();
    private Constraint[] cs;
    private double rho, lambda;
    private TsDomain idomain;
    private TsUnit aggUnit;

    public Map<String, TsData> process(Map<String, TsData> inputs, MultivariateCholetteSpec spec) {
        loadInfo(inputs, spec);

        Map<String, TsData> rslts = new HashMap<>();
        buildMaps();

        benchmarkIndependentConstraints(rslts);
        if (contemporaneousConstraints.isEmpty()) {
            return rslts;
        }

        buildDomain();
        buildEndogeneousData();
        // compute weights, adjust constraints...
        buildWeights();
        buildConstraints();
        if (temporalConstraints.isEmpty()) {
            computeContemporaneous(rslts);
        } else {
            compute(rslts);
        }

        return rslts;
    }

    private void loadInfo(Map<String, TsData> data, MultivariateCholetteSpec spec) {
        // inputs
        inputs.putAll(data);
        // temporal constraints
        for (TemporalConstraint desc : spec.getTemporalConstraints()) {
            addTemporalConstraint(desc);
        }

        // contemporaneous constraints
        for (ContemporaneousConstraint desc : spec.getContemporaneousConstraints()) {
            addContemporaneousConstraint(desc);
        }
        rho = spec.getRho();
        lambda = spec.getLambda();
    }

    private void addContemporaneousConstraint(ContemporaneousConstraint cnt) {
        if (cnt.getConstraint() != null && !inputs.containsKey(cnt.getConstraint())) {
            throw new IllegalArgumentException("Invalid contemporaneous constraint: " + cnt.getConstraint());
        }
        if (cnt.hasWildCards()) {
            cnt = cnt.expand(inputs.keySet());
        } else {
            for (WeightedItem<String> ws : cnt.getComponents()) {
                if (!inputs.containsKey(ws.getItem())) {
                    throw new IllegalArgumentException("Invalid contemporaneous constraint: " + ws.getItem());
                }
            }
        }
        contemporaneousConstraints.add(cnt);
    }

    private void addTemporalConstraint(TemporalConstraint cnt) {
        TsData sagg = inputs.get(cnt.getAggregate()), sdetail = inputs.get(cnt.getDetail());
        if (sagg == null || sdetail == null) {
            throw new IllegalArgumentException("Invalid temporal constraint: " + cnt.getAggregate());
        }
        temporalConstraints.put(cnt.getDetail(), cnt.getAggregate());
    }

    private void benchmarkIndependentConstraints(Map<String, TsData> rslts) {

        CholetteSpec uspec = CholetteSpec.builder()
                .aggregationType(AggregationType.Sum)
                .bias(CholetteSpec.BiasCorrection.None)
                .lambda(lambda)
                .rho(rho)
                .build();
        CholetteProcessor cf = new CholetteProcessor();
        temporalConstraints.forEach(
                (detail, agg) -> {
                    if (!rcnt.contains(detail)) {
                        TsData q = inputs.get(detail);
                        TsData a = inputs.get(agg);
                        TsData b = cf.benchmark(q, a, uspec);
                        if (b != null) {
                            rslts.put(detail, b);
                        }
                    }
                });
    }

    private void computeContemporaneous(Map<String, TsData> rslts) {

        // compute weights, adjust constraints...
        int nvars = rcnt.size(), ncnts = cs.length;;
        IMultivariateSsf ssf = ContemporaneousSsfCholette.builder(nvars)
                .rho(rho)
                .weights(weights)
                .constraints(cs)
                .build();

        // build the observations
        FastMatrix M = FastMatrix.make(idomain.getLength(), ncnts);
        for (int i = 0; i < ncnts; ++i) {
            M.column(i).copyFrom(lcntData[i], 0);
        }

        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(new SsfMatrix(M));
        DataBlockStorage states = DkToolkit.fastSmooth(ussf, udata);

        int neq = cs.length;
        for (int i = 0; i < nvars; ++i) {
            TsData s = inputs.get(rcnt.get(i));
            TsData sc = TsDataToolkit.fitToDomain(s, idomain);
            double[] y = sc.getValues().toArray();
            DataBlock t = states.item(i);
            for (int j = 0; j < y.length; ++j) {
                y[j] += t.get(j * neq) * weights[i][j];
            }
            rslts.put(rcnt.get(i), TsData.ofInternal(idomain.getStartPeriod(), y));
        }
    }

    private void buildMaps() {
        // first of all, we go through the constraints to get information on the used series
        for (ContemporaneousConstraint desc : contemporaneousConstraints) {
            if (rcnt.contains(desc.getConstraint())) {
                throw new IllegalArgumentException("Binding constraint cannot be used in definitions: " + desc.getConstraint());
            }
            // TODO Deal with such cases. Use "extended names" and modify the current constraint
            if (!lcnt.contains(desc.getConstraint())) {
                lcnt.add(desc.getConstraint());
            }
            for (WeightedItem<String> wc : desc.getComponents()) {
                if (lcnt.contains(wc.getItem())) {
                    throw new IllegalArgumentException("Component definition cannot be a constraint: " + wc.getItem());
                } else if (!rcnt.contains(wc.getItem())) {
                    rcnt.add(wc.getItem());
                }
            }
        }
        // we create the actual constraints
        cs = new Constraint[contemporaneousConstraints.size()];
        int pos = 0;
        for (ContemporaneousConstraint desc : contemporaneousConstraints) {
            HashMap<Integer, Double> constraint = new HashMap<>();
            for (WeightedItem<String> cur : desc.getComponents()) {
                constraint.put(rcnt.indexOf(cur.getItem()), cur.getWeight());
            }
            Constraint acnt = new Constraint(constraint);
            cs[pos++] = acnt;
        }

        lcntData = new double[contemporaneousConstraints.size()][];
        for (int i = 0; i < contemporaneousConstraints.size(); ++i) {
            ContemporaneousConstraint desc = contemporaneousConstraints.get(i);
            if (desc.getConstraint() != null) {
                TsData s = inputs.get(desc.getConstraint());
                lcntData[i] = s.getValues().toArray();
            } else {
                lcntData[i] = new double[]{desc.getConstant()};
            }
        }
    }

    private void buildDomain() {

        for (String cur : rcnt) {
            TsDomain d = inputs.get(cur).getDomain();
            if (idomain == null) {
                idomain = d;
            } else if (!idomain.getTsUnit().equals(d.getTsUnit())) {
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            } else {
                idomain = idomain.intersection(d);
                if (idomain.isEmpty()) {
                    throw new TsException(TsException.DOMAIN_EMPTY);
                }
            }
        }
    }

    private void buildEndogeneousData() {
        rcntData = new double[rcnt.size()][];
        for (int i = 0; i < rcnt.size(); ++i) {
            TsData s = TsDataToolkit.fitToDomain(inputs.get(rcnt.get(i)), idomain);
            rcntData[i] = s.getValues().toArray();
        }
    }

    private void buildWeights() {
        weights = new double[rcntData.length][];
        for (int i = 0; i < weights.length; ++i) {
            weights[i] = calcWeights(rcntData[i]);
        }
    }

    private void buildConstraints() {
        buildContemporaneousConstraints();
        buildTemporalConstraints();
    }

    private void buildContemporaneousConstraints() {
        for (int i = 0; i < cs.length; ++i) {
            Constraint cur = cs[i];
            if (lcntData[i].length == 1) { // expand the constants
                double c = lcntData[i][0];
                lcntData[i] = new double[idomain.getLength()];
                for (int j = 0; j < lcntData.length; ++j) {
                    lcntData[i][j] = c;
                }
            }
            // correct the constraints to fit the state space representation
            DataBlock z = DataBlock.of(lcntData[i]);
            for (int j = 0; j < cur.index.length; ++j) {
                z.addAY(-cur.weights[j], DataBlock.of(rcntData[cur.index[j]]));
            }
        }
    }

    private void buildTemporalConstraints() {
        aggUnit = null;
        for (int i = 0; i < rcnt.size(); ++i) {
            String n = rcnt.get(i);
            TsData orig = inputs.get(rcnt.get(i));
            TsData cur = inputs.get(temporalConstraints.get(rcnt.get(i)));
            if (cur != null) {
                if (aggUnit == null) {
                    aggUnit = cur.getTsUnit();
                } else if (!aggUnit.equals(cur.getTsUnit())) {
                    throw new TsException(TsException.INCOMPATIBLE_FREQ);
                }
                TsData origc = TsDataToolkit.fitToDomain(orig, idomain);
                TsData a = origc.aggregate(cur.getTsUnit(), AggregationType.Sum, true);
                a = TsDataToolkit.subtract(cur, a);
                tcntData.put(n, a);
            }
        }
    }

    private double[] calcWeights(double[] x) {
        double[] w = new double[x.length];
        if (lambda == 0) {
            for (int i = 0; i < w.length; ++i) {
                w[i] = 1;
            }
        } else if (lambda == 0.5) {
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.sqrt(Math.abs(x[i]));
            }
        } else if (lambda == 1){
              for (int i = 0; i < w.length; ++i) {
                w[i] = Math.abs(x[i]);
            }
        } else{
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.pow(Math.abs(x[i]), lambda);
            }
        }
        return w;
    }

    private void compute(Map<String, TsData> rslts) {
        int c = idomain.getTsUnit().ratioOf(aggUnit);
        int nvars = rcnt.size(), ncnts = cs.length;
        int len = idomain.getLength();

        IMultivariateSsf ssf = MultivariateSsfCholette.builder(nvars)
                .conversion(c)
                .rho(rho)
                .constraints(cs)
                .weights(weights)
                .build();
        // build the observations
        FastMatrix M = FastMatrix.make(len, nvars + ncnts);
        M.set(Double.NaN);

        // fill the matrix: first rows with temporal constraints
        for (int i = 0; i < nvars; ++i) {
            if (temporalConstraints.containsKey(rcnt.get(i))) {
                TsData a = tcntData.get(rcnt.get(i));
                DataBlock b = M.column(i).extract(c - 1, a.length(), c);
                b.copy(a.getValues());
            }
        }
        for (int i = 0; i < ncnts; ++i) {
            DataBlock row = M.column(i + nvars);
            row.copyFrom(lcntData[i], 0);
        }
        ISsf adapter = M2uAdapter.of(ssf);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));
        
        FastStateSmoother2 smoother = new FastStateSmoother2(adapter);
        DataBlockStorage states = smoother.process(data);

        int neq = nvars + ncnts;
        for (int i = 0; i < rcnt.size(); ++i) {
            TsData s = inputs.get(rcnt.get(i));
            TsDataView sc = TsDataView.select(s, idomain);
            double[] y = sc.getData().toArray();
            DoubleSeq t = states.item(2 * i + 1);
            for (int j = 0; j < y.length; ++j) {
                y[j] += t.get(j * neq) * weights[i][j];
            }
            rslts.put(rcnt.get(i), TsData.ofInternal(sc.getStart(), y));
        }
    }
    
    
    
}
