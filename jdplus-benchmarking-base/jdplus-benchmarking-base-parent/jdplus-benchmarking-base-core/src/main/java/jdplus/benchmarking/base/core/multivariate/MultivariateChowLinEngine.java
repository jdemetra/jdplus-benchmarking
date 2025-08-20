/*
 * Copyright 2025 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.benchmarking.base.core.multivariate;

import internal.ssf.FastStateSmoother2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinSpec;
import jdplus.benchmarking.base.api.multivariate.ModelData;
import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLin;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationProcessor;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.util.WeightedItem;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.data.transformation.Cumulator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;

/**
 *
 * @author LEMASSO
 */
public class MultivariateChowLinEngine {

    /* model data: temporal aggregation constraints data and indicators */
    private LinkedHashMap<String, ModelData> mData = new LinkedHashMap<>();

    /* contemporaneous constraints data */
    private Map<String, TsData> ccData = new HashMap<>();

    /* original regressors */
    private final LinkedHashMap<Integer, FastMatrix> Xo = new LinkedHashMap<>();

    /* rescaled regressors -> TO DO */
    // private final LinkedHashMap<Integer, FastMatrix> X = new LinkedHashMap<>();

    /* cumulated regressors */
    private final LinkedHashMap<Integer, FastMatrix> Xc = new LinkedHashMap<>();

    /* contemporaneous constraints */
    private final List<ContemporaneousConstraint> contemporaneousConstraints = new ArrayList<>();
    private Constraint[] cs;

    /* original contemporaneous constraints data */
    private double[][] Zo;

    /* cumulated contemporaneous constraints data */
    private double[][] Zc;

    /* m: number of series, q: number of contemporaneous constraints */
    private int m, q;

    private double[] rhos;
    private boolean[] isConstant, isTrend;

    private int lfreq, hfreq, ratio;
    private TsDomain lDomain, hDomain;
    private TsUnit aggUnit;

    public Map<String, TsData> process(LinkedHashMap<String, ModelData> mData, Map<String, TsData> ccData, MultivariateChowLinSpec spec)  {

        // TO DO
        // - Change output -> not only disaggregated series but an object of class MultivariateChowLinResults incl. stdev, model, etc.
        // - Rescaling
        // - Checks that the name of the constraints data matches with the name of variable in the parsed description of the contemporaneous constraint
        // - Fit to domain before processing
        // - Change variance of innovations -> cfr email Jean from 2025-08-08

        Map<String, TsData> rslts = new LinkedHashMap<>();

        this.mData = mData;
        this.m = mData.size();
        this.ccData = ccData;
        this.q = ccData.size();
        this.rhos = spec.getRhos();
        if(rhos.length != m){
            throw new IllegalArgumentException("Mismatch between the number of series and the number of declared rho's");
        }
        this.isConstant = spec.getConstant();
        if(isConstant.length != m){
            throw new IllegalArgumentException("Mismatch between the number of series and the length of the constant vector");
        }
        this.isTrend = spec.getTrend();
        if(isTrend.length != m){
            throw new IllegalArgumentException("Mismatch between the number of series and the length of the trend vector");
        }

        buildDomains(spec.getDefaultPeriod());
        buildRegressors();

        if (ccData.isEmpty()) {
            processIndependentConstraints(rslts);
            return rslts;
        } else{
            buildContemporaneousConstraints(spec.getContemporaneousConstraints());
        }

        compute(rslts);

        return rslts;
    }

    private void buildDomains(int defFreq) {

        lDomain = TsDomain.DEFAULT_EMPTY;
        for (String s : mData.keySet()) {
            TsDomain d = mData.get(s).getY().getDomain();
            if (lDomain.isEmpty()) {
                lDomain = d;
            } else if (!lDomain.getTsUnit().equals(d.getTsUnit())) {
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            } else {
                lDomain = lDomain.intersection(d);
                if (lDomain.isEmpty()) {
                    throw new TsException(TsException.DOMAIN_EMPTY);
                }
            }
        }

        hDomain = TsDomain.DEFAULT_EMPTY;
        for (String s : mData.keySet()) {
            TsData[] xs = mData.get(s).getX();
            if (!(xs == null)){
                for (TsData xsk : xs) {
                    if (hDomain.isEmpty()) {
                        hDomain = xsk.getDomain();
                    } else {
                        hDomain = hDomain.intersection(xsk.getDomain());
                    }
                }
            }
        }

        if (!ccData.isEmpty()) {
            for (int i = 0; i < q; ++i) {
                for (String s : ccData.keySet()) {
                    if (hDomain.isEmpty()) {
                        hDomain = ccData.get(s).getDomain();
                    } else{
                        hDomain = hDomain.intersection(ccData.get(s).getDomain());
                    }
                }
            }
        }

        if (hDomain.isEmpty()) {
            int len = lDomain.getLength() * defFreq;
            TsPeriod start = TsPeriod.of(TsUnit.ofAnnualFrequency(defFreq), lDomain.start());
            hDomain = TsDomain.of(start, len);
        }

        this.lfreq = lDomain.getAnnualFrequency();
        this.hfreq = hDomain.getAnnualFrequency();    
        if (lfreq >= hfreq || hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        this.ratio = hfreq / lfreq;
    }

    private void buildRegressors() {

        int counter = 0;
        for (String s : mData.keySet()) {
            TsData[] indic = mData.get(s).getX();

            int nx = indic == null ? 0 : indic.length;
            int n = nx;
            if (isConstant[counter]) {
                ++n;
            }
            if (isTrend[counter]) {
                ++n;
            }

            if (n > 0) {
                FastMatrix xs = FastMatrix.make(hDomain.getLength(), n);
                FastMatrix xsC = FastMatrix.make(hDomain.getLength(), n);

                // xs
                DataBlockIterator xcols = xs.columnsIterator();
                if (isConstant[counter]) {
                    xcols.next().set(1);
                }
                if (isTrend[counter]) {
                    xcols.next().set(a -> a);
                }
                if (nx > 0) {
                    for (TsData xk : indic) {
                        if (xk == null) {
                            throw new IllegalArgumentException("Indicator data not found: " + s);
                        }
                        if (aggUnit == null) {
                            aggUnit = xk.getTsUnit();
                        } else if (!aggUnit.equals(xk.getTsUnit())) {
                            throw new TsException(TsException.INCOMPATIBLE_FREQ);
                        }
                        xcols.next().copy(xk.getValues());
                    }
                }
                Xo.put(counter, xs);

                // xs cumulated
                xsC = xs.deepClone();
                Cumulator cumul = new Cumulator(ratio);
                DataBlockIterator cXc = xsC.columnsIterator();
                while (cXc.hasNext()) {
                    cumul.transform(cXc.next());
                }
                Xc.put(counter, xsC);

            } else {
                Xo.put(counter, FastMatrix.EMPTY);
                Xc.put(counter, FastMatrix.EMPTY);
            }
            counter++;
        }
    }

    private void processIndependentConstraints(Map<String, TsData> rslts) {

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        TemporalDisaggregationSpec spec;

        int counter = 0;
        for (String s : mData.keySet()) {
            if(rhos[counter] == 1){
                spec = TemporalDisaggregationSpec.FERNANDEZ.toBuilder()
                        .algorithmSpec(aspec)
                        .estimationSpec(espec)
                        .build();
            }else{
                ModelSpec mspec = ModelSpec.builder()
                        .parameter(Parameter.fixed(rhos[counter]))
                        .constant(isConstant[counter])
                        .trend(isTrend[counter])
                        .build();

                spec = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                        .algorithmSpec(aspec)
                        .estimationSpec(espec)
                        .modelSpec(mspec)
                        .build();
            }
            TsData ys = mData.get(s).getY();
            TsData[] xs = mData.get(s).getX();

            TemporalDisaggregationResults r;
            if(xs.length == 0){
                r = TemporalDisaggregationProcessor.process(ys, 0, 0, spec);
            }else {
                r = TemporalDisaggregationProcessor.process(ys, xs, spec);
            }
            if (r != null) {
                rslts.put(s, r.getDisaggregatedSeries());
            }
            counter++;
        }
    }

    private void buildContemporaneousConstraints(List<ContemporaneousConstraint> lcnt) {

        for (ContemporaneousConstraint cnt : lcnt) {
            contemporaneousConstraints.add(cnt);

            if (mData.containsKey(cnt.getConstraint())) {
                throw new IllegalArgumentException("Binding constraint cannot be used in definitions: " + cnt.getConstraint());
            }
            for (WeightedItem<String> wc : cnt.getComponents()) {
                if (cnt.getConstraint().contains(wc.getItem())) {
                    throw new IllegalArgumentException("Component definition cannot be a constraint: " + wc.getItem());
                }
            }   
        }

        // we create the actual constraints
        cs = new Constraint[q];
        List<String> seriesNames = new ArrayList<>(mData.keySet());
        int pos = 0;
        for (ContemporaneousConstraint desc : contemporaneousConstraints) {
            HashMap<Integer, Double> constraint = new HashMap<>();
            for (WeightedItem<String> cur : desc.getComponents()) {
                constraint.put(seriesNames.indexOf(cur.getItem()), cur.getWeight());
            }
            Constraint acnt = new Constraint(constraint);
            cs[pos++] = acnt;
        }

        // Zo, Zc
        Zo = new double[q][];
        Zc = new double[q][];
        for (int i = 0; i < q; ++i) {
            ContemporaneousConstraint desc = contemporaneousConstraints.get(i);
            double[] Zi;
            if (desc.getConstraint() != null) {
                TsData s = ccData.get(desc.getConstraint());
                Zi = s.getValues().toArray();   
            } else {
                Zi = new double[]{desc.getConstant()};
            }
            Zo[i] = Zi;

            double[] ZiC = Zi.clone();
            Cumulator cumul = new Cumulator(ratio);
            cumul.transform(DataBlock.of(ZiC));
            Zc[i] = ZiC;
        }
    }
    
    private void compute(Map<String, TsData> rslts) {

        List<String> seriesNames = new ArrayList<>(mData.keySet());
        int c = hfreq;
        int len = hDomain.getLength();

        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(m)
                .conversion(c)
                .rho(rhos)
                .xc(Xc)
                .constraints(cs)
                .build();

        // build the observations
        FastMatrix M = FastMatrix.make(len, m + q);
        M.set(Double.NaN);

        // fill the matrix: first rows with temporal constraints, last rows with contemporaneous constraint(s)
        int counter = 0;
        for (String s : mData.keySet()) {
            TsData a = mData.get(s).getY();
            DataBlock b = M.column(counter).extract(c - 1, a.length(), c);
            b.copy(a.getValues());
            counter++;
        }

        for (int i = 0; i < q; ++i) {
            DataBlock row = M.column(i + m);
            row.copyFrom(Zc[i], 0);
        }

        ISsf adapter = M2uAdapter.of(ssf);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));

        FastStateSmoother2 smoother = new FastStateSmoother2(adapter);
        DataBlockStorage states = smoother.process(data);

        int nxc = 0;
        for (int i = 0; i < m; ++i) {
            double[] r = new double[len];
            int ip = 2 * i + nxc;
            int nx = Xo.get(i).getColumnsCount();
            DoubleSeq t = states.item(ip + 1);
            for (int j = 0; j < len; ++j) {
                r[j] += t.get(j * (m + q));
                if(nx > 0) {
                    for (int k = 0; k < nx; ++k) {
                        DoubleSeq bk = states.item(ip + 2 + k);
                        r[j] += bk.get(0) * Xo.get(i).get(j, k);
                    }
                }
            }
            nxc += nx;
            rslts.put(seriesNames.get(i), TsData.ofInternal(hDomain.getStartPeriod(), r));
        }           
    }
}
