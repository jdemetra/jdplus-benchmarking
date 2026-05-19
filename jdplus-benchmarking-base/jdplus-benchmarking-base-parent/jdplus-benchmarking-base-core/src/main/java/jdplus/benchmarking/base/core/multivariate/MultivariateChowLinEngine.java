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

import java.util.*;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinResults;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinSpec;
import jdplus.benchmarking.base.api.multivariate.ModelData;
import jdplus.benchmarking.base.api.univariate.*;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLin;
import jdplus.benchmarking.base.core.univariate.*;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.regression.Constant;
import jdplus.toolkit.base.api.timeseries.regression.LinearTrend;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.api.util.WeightedItem;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.data.transformation.Cumulator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.ssf.dk.DefaultDiffuseFilteringResults;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.FastStateSmoother;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.*;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;

/**
 *
 * @author LEMASSO
 */
public class MultivariateChowLinEngine {

    /* model input temporal aggregation constraints and indicators */
    private LinkedHashMap<String, ModelData> mData = new LinkedHashMap<>();

    /* contemporaneous constraints input */
    private Map<String, TsData> ccData = new HashMap<>();

    /* rescaled regressors -> TO DO */
    // private final LinkedHashMap<Integer, FastMatrix> X = new LinkedHashMap<>();

    /* temporal constraints data */
    private double[][] Yo;
    //private final LinkedHashMap<Integer, TsData> Yo = new LinkedHashMap<>();

    /* regressors */
    private final LinkedHashMap<Integer, FastMatrix> Xo = new LinkedHashMap<>();

    /* cumulated regressors */
    private final LinkedHashMap<Integer, FastMatrix> Xc = new LinkedHashMap<>();

    /* contemporaneous constraints */
    private final List<ContemporaneousConstraint> contemporaneousConstraints = new ArrayList<>();
    private Constraint[] cs;

    /* contemporaneous constraints data */
    private double[][] Zo;

    /* cumulated contemporaneous constraints data */
    private double[][] Zc;

    /* m: number of series, q: number of contemporaneous constraints */
    private int m, q;
    private double[] rhos;
    private boolean[] isConstant, isTrend;
    /* errors variance matrix */
    private FastMatrix var;
    private int ratio;
    private TsDomain lDomain, hDomain;
    /* full residuals from univariate estimations */
    private double[][] resUnivariate = null;

    public MultivariateChowLinResults process(LinkedHashMap<String, ModelData> mData, Map<String, TsData> ccData, MultivariateChowLinSpec spec)  {

        // TO DO
        // - Allow for covariance in errors variance matrix? -> complicated, not at first! Must be given by the user or estimated empirically from the residuals of univariate estimations (but then we need to implement a way to estimate the covariance between the residuals of the different series)
        // - Rescaling? A tester car possibles problèmes dans du multivarié...

        Map<String, TsData> rslts = new LinkedHashMap<>();

        this.mData = mData;
        this.m = mData.size();
        this.ccData = ccData;
        this.q = spec.getContemporaneousConstraints().size();
        this.rhos = getOrDefault(spec.getRhos(), m, 1, "Mismatch between the number of series and the number of declared rho's");
        this.isConstant = getOrDefault(spec.getConstant(), m, false, "Mismatch between the number of series and the length of the constant vector");
        this.isTrend = getOrDefault(spec.getTrend(), m, false, "Mismatch between the number of series and the length of the trend vector");
        this.resUnivariate = new double[m][];

        buildDomains(spec.getDefaultPeriod());
        buildTemporalConstraints();
        buildRegressors();

        // No contemporaneous constraint case
        if (q == 0) {
            Map<String, RawTemporalDisaggregationResults> rsltsUnivariate = new LinkedHashMap<>();
            processIndependentTD(rsltsUnivariate);
            return(getResultsFromRsltsUnivariate(rsltsUnivariate));
        }

        // Get residuals from univariate models
        if (spec.getVarMethod() == MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate) {
            Map<String, RawTemporalDisaggregationResults> rsltsUnivariate = new LinkedHashMap<>();
            processIndependentTD(rsltsUnivariate);
            List<String> seriesNames = new ArrayList<>(mData.keySet());
            for (int k = 0; k < seriesNames.size(); k++) {
                String name = seriesNames.get(k);
                RawTemporalDisaggregationResults td = rsltsUnivariate.get(name);
                this.resUnivariate[k] = td.getResidualsDiagnostics().getFullResiduals().toArray();
            }
        }

        this.var = buildVarMatrix(spec.getVarMethod(), FastMatrix.of(spec.getVar()), m);

        buildContemporaneousConstraints(spec.getContemporaneousConstraints());

//        computeDisaggOnly(rslts);
        return compute();
    }


    private void buildDomains(int defFreq) {

        hDomain = TsDomain.DEFAULT_EMPTY;
        for (String sName : mData.keySet()) {
            TsData[] x = mData.get(sName).getX();
            if (!(x == null)){
                for (TsData s : x) {
                    if (hDomain.isEmpty()) {
                        hDomain = s.getDomain();
                    } else {
                        hDomain = hDomain.intersection(s.getDomain());
                    }
                }
            }
        }

        if (ccData != null && !ccData.isEmpty()) {
            for (int i = 0; i < q; ++i) {
                for (String sName : ccData.keySet()) {
                    if (hDomain.isEmpty()) {
                        hDomain = ccData.get(sName).getDomain();
                    } else{
                        hDomain = hDomain.intersection(ccData.get(sName).getDomain());
                    }
                }
            }
        }

        if (hDomain.isEmpty()) {
            int len = lDomain.getLength() * defFreq;
            TsPeriod start = TsPeriod.of(TsUnit.ofAnnualFrequency(defFreq), lDomain.start());
            hDomain = TsDomain.of(start, len);
        } else {
            if(hDomain.getStartPeriod().annualPosition() != 0) {
                // domain must start at the first period of the frequency
                throw new TsException(TsException.INVALID_PERIOD);
            }
        }

        lDomain = TsDomain.DEFAULT_EMPTY;
        for (String sName : mData.keySet()) {
            TsDomain d = mData.get(sName).getY().getDomain();
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
        lDomain = lDomain.intersection(hDomain.aggregate(lDomain.getTsUnit(),true));
        if (lDomain.isEmpty()) {
            throw new TsException(TsException.DOMAIN_EMPTY);
        }

        int lfreq = lDomain.getAnnualFrequency();
        int hfreq = hDomain.getAnnualFrequency();
        if (lfreq >= hfreq || hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        this.ratio = hfreq / lfreq;
    }

    private void buildTemporalConstraints() {
        Yo = new double[m][];
        int k = 0;
        for (String sName : mData.keySet()) {
            TsData s = mData.get(sName).getY();
            TsData sDom = TsDataToolkit.fitToDomain(s, lDomain);
            Yo[k] = sDom.getValues().toArray();
            k++;
        }
    }

    private void buildRegressors() {
        int k = 0;
        for (String sName : mData.keySet()) {
            TsData[] xp = mData.get(sName).getX();
            int nxp = xp == null ? 0 : xp.length;
            int nx = nxp;
            if (isConstant[k]) {
                ++nx;
            }
            if (isTrend[k]) {
                ++nx;
            }

            if (nx > 0) {
                FastMatrix xm = FastMatrix.make(hDomain.getLength(), nx);
                DataBlockIterator xmCols = xm.columnsIterator();
                if (isConstant[k]) {
                    xmCols.next().set(1);
                }
                if (isTrend[k]) {
                    xmCols.next().set(a -> a);
                }
                if (nxp > 0) {
                    for (TsData s : xp) {
                        if (s == null) {
                            throw new IllegalArgumentException("Indicator data not found: " + s);
                        }
                        TsData sDom = TsDataToolkit.fitToDomain(s, hDomain);
                        xmCols.next().copy(sDom.getValues());
                    }
                }
                Xo.put(k, xm);

                FastMatrix xmC = xm.deepClone();
                Cumulator cumul = new Cumulator(ratio);
                DataBlockIterator xmCCols = xmC.columnsIterator();
                while (xmCCols.hasNext()) {
                    cumul.transform(xmCCols.next());
                }
                Xc.put(k, xmC);
            } else {
                Xo.put(k, FastMatrix.EMPTY);
                Xc.put(k, FastMatrix.EMPTY);
            }
            k++;
        }
    }

//    private void processIndependentConstraints(Map<String, TsData> rslts) {
//
//        AlgorithmSpec aspec = AlgorithmSpec.builder()
//                .fast(true)
//                .rescale(true)
//                .algorithm(SsfInitialization.SqrtDiffuse)
//                .build();
//
//        EstimationSpec espec = EstimationSpec.builder()
//                .estimationPrecision(1e-9)
//                .build();
//
//        RawDisaggregationSpec spec;
//        int k = 0;
//        for (String sName : mData.keySet()) {
//            if (rhos[k] == 1) {
//                spec = RawDisaggregationSpec.fernandez(this.ratio).toBuilder()
//                        .algorithmSpec(aspec)
//                        .estimationSpec(espec)
//                        .build();
//            } else {
//                ModelSpec mspec = ModelSpec.builder()
//                        .parameter(Parameter.fixed(rhos[k]))
//                        .constant(isConstant[k])
//                        .trend(isTrend[k])
//                        .build();
//
//                spec = RawDisaggregationSpec.chowLin(this.ratio).toBuilder()
//                        .algorithmSpec(aspec)
//                        .estimationSpec(espec)
//                        .modelSpec(mspec)
//                        .build();
//            }
//            double[] Y = Yo[k];
//            FastMatrix x = Xo.get(k);
//
//            RawTemporalDisaggregationResults r;
//            if (x.isEmpty()) {
//                r = RawDisaggregationProcessor.process(DoubleSeq.of(Y), 0, 0, spec);
//            } else {
//                r = RawDisaggregationProcessor.process(DoubleSeq.of(Y), x, 0, spec);
//            }
//            if (r != null) {
//                DoubleSeq d = r.getDisaggregatedSeries();
//                rslts.put(sName, TsData.ofInternal(hDomain.getStartPeriod(), d.toArray()));
//                this.resUnivariate[k] = r.getResidualsDiagnostics().getFullResiduals().toArray();
//            }
//            k++;
//        }
//    }

    private void processIndependentTD(Map<String, RawTemporalDisaggregationResults> rsltsUnivariate) {

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();

        RawDisaggregationSpec spec;

        int k = 0;
        for (String sName : mData.keySet()) {
            if (rhos[k] == 1) {
                spec = RawDisaggregationSpec.fernandez(this.ratio).toBuilder()
                        .algorithmSpec(aspec)
                        .estimationSpec(espec)
                        .build();
            } else {
                ModelSpec mspec = ModelSpec.builder()
                        .parameter(Parameter.fixed(rhos[k]))
                        .constant(isConstant[k])
                        .trend(isTrend[k])
                        .build();

                spec = RawDisaggregationSpec.chowLin(this.ratio).toBuilder()
                        .algorithmSpec(aspec)
                        .estimationSpec(espec)
                        .modelSpec(mspec)
                        .build();
            }
            double[] Y = Yo[k];
            FastMatrix x = Xo.get(k);

            if (x.isEmpty()) {
                // compute the number of forecast periods needed
                int nf = 0;
                int hFreq = hDomain.getAnnualFrequency();
                TsPeriod lStartAtHFreq = TsPeriod.of(TsUnit.ofAnnualFrequency(hFreq), lDomain.start());
                if (lStartAtHFreq.equals(hDomain.getStartPeriod())) {
                     nf = hDomain.getLength() - lDomain.getLength() * hFreq;
                }
                rsltsUnivariate.put(sName, RawDisaggregationProcessor.process(DoubleSeq.of(Y), 0, nf, spec));
            } else {
                rsltsUnivariate.put(sName, RawDisaggregationProcessor.process(DoubleSeq.of(Y), x, 0, spec));
            }

            k++;
        }
    }

    private void buildContemporaneousConstraints(List<ContemporaneousConstraint> lcnt) {

        for (ContemporaneousConstraint cnt : lcnt) {
            contemporaneousConstraints.add(cnt);

            String constraint = cnt.getConstraint();

            if (mData.containsKey(constraint)) {
                throw new IllegalArgumentException("Binding constraint cannot be used in definitions: " + constraint);
            }

            for (WeightedItem<String> wc : cnt.getComponents()) {
                if (constraint != null && constraint.contains(wc.getItem())) {
                    throw new IllegalArgumentException("Component definition cannot be a constraint: " + wc.getItem());
                }
                if (!mData.containsKey(wc.getItem())) {
                    throw new IllegalArgumentException("This component of the contemporaneous constraint not found in the data: " + wc.getItem());
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
            double[] z;
            if (desc.getConstraint() != null) {
                TsData s = TsDataToolkit.fitToDomain(ccData.get(desc.getConstraint()), hDomain);
                z = s.getValues().toArray();
            } else {
                z = new double[hDomain.getLength()];
                Arrays.fill(z, desc.getConstant());
            }
            Zo[i] = z;

            double[] zc = z.clone();
            Cumulator cumul = new Cumulator(ratio);
            cumul.transform(DataBlock.of(zc));
            Zc[i] = zc;
        }
    }

    private void computeDisaggOnly(Map<String, TsData> rslts) {

        List<String> seriesNames = new ArrayList<>(mData.keySet());
        int c = ratio;
        int len = hDomain.getLength();

        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(m)
                .conversion(c)
                .rho(rhos)
                .errV(var)
                .xc(Xc)
                .constraints(cs)
                .build();

        // build the observations
        FastMatrix M = FastMatrix.make(len, m + q);
        M.set(Double.NaN);

        // fill the matrix: first rows with temporal constraints, last rows with contemporaneous constraint(s)
        for (int i = 0; i < m; ++i) {
            DataBlock b = M.column(i).extract(c - 1, Yo[i].length, c);
            b.copy(DoubleSeq.of(Yo[i]));
        }

        for (int i = 0; i < q; ++i) {
            DataBlock row = M.column(i + m);
            row.copyFrom(Zc[i], 0);
        }

        ISsf adapter = M2uAdapter.of(ssf);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));

        FastStateSmoother smoother = new FastStateSmoother(adapter);
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

    private MultivariateChowLinResults compute() {

        Map<String, TsData> disagg = new LinkedHashMap<>();
        Map<String, TsData> edisagg = new LinkedHashMap<>();
        Map<String, TsData> regeffect = new LinkedHashMap<>();
        Map<String, FastMatrix> regressors = new LinkedHashMap<>();
        Map<String, DoubleSeq> regCoef = new LinkedHashMap<>();
        Map<String, DoubleSeq> vregCoef = new LinkedHashMap<>();
        Map<String, Variable[]> indic = new LinkedHashMap<>();

        List<String> seriesNames = new ArrayList<>(mData.keySet());
        int len = hDomain.getLength();
        int c = ratio;

        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(m)
                .conversion(c)
                .rho(rhos)
                .errV(var)
                .xc(Xc)
                .constraints(cs)
                .build();

        // build the observations
        FastMatrix M = FastMatrix.make(len, m + q);
        M.set(Double.NaN);

        // fill the matrix: first rows with temporal constraints, last rows with contemporaneous constraint(s)
        for (int i = 0; i < m; ++i) {
            DataBlock b = M.column(i).extract(c - 1, Yo[i].length, c);
            b.copy(DoubleSeq.of(Yo[i]));
        }
        for (int i = 0; i < q; ++i) {
            M.column(m + i).copyFrom(Zc[i], 0);
        }

        ISsf adapter = M2uAdapter.of(ssf);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));
        DefaultSmoothingResults srslts =
                DkToolkit.sqrtSmooth(adapter, data, true, false);

        int no = len * (m + q);
        double[] oh = new double[no];
        double[] voh = new double[no];
        for (int i = 0; i < no; ++i) {
            oh[i] = adapter.loading().ZX(i, srslts.a(i));
            voh[i] = adapter.loading().ZVZ(i, srslts.P(i));
        }

        int nxc = 0;

        for (int i = 0; i < m; ++i) {

            String sName = seriesNames.get(i);

            double[] yhC = DoubleSeq.of(oh).extract(i, len,m + q).toArray();
            double[] vyhC = DoubleSeq.of(voh).extract(i, len,m + q).toArray();

            double[] yh = new double[len];
            double[] vyh = new double[len];
//            FastMatrix vyhCM = FastMatrix.diagonal(DoubleSeq.of(vyhC));
//            DataBlock x = DataBlock.make(len);
//            double[] vyhOld = new double[len];
            for (int k = 0; k < len; ++k) {
                if (k % c == 0) {
                    yh[k] = yhC[k];
                    vyh[k] = vyhC[k];
                } else {
                    yh[k] = yhC[k] - yhC[k - 1];
                    vyh[k] = vyhC[k] + vyhC[k - 1]; //~ QuadraticForm.apply(V, [... -1 1 0 0 ...])
//                    x.set(k - 1, -1);
//                    x.set(k, 1);
//                    vyhOld[k] = QuadraticForm.apply(vyhCM, x);
//                    x.set(k - 1, 0);
//                    x.set(k, 0);
                }
            }

            // regressors
            double[] rh = new double[len];
            int nx = Xo.get(i).getColumnsCount();

            if (nx == 0) {
                Arrays.fill(rh, 0);
                regCoef.put(sName, null);
                vregCoef.put(sName, null);
                indic.put(sName, null);
            } else {
                int ip = 2 * i + nxc;

                double[] b = new double[nx];
                double[] vb = new double[nx];

                for (int k = 0; k < nx; ++k) {
                    b[k] = srslts.a(len - 1).get(ip + 2 + k);
                    vb[k] = srslts.P(len - 1).get(ip + 2 + k, ip + 2 + k);
                }

                regCoef.put(sName, DoubleSeq.of(b));
                vregCoef.put(sName, DoubleSeq.of(vb));

                for (int t = 0; t < len; ++t) {
                    for (int k = 0; k < nx; ++k) {
                        rh[t] += b[k] * Xo.get(i).get(t, k);
                    }
                }

                nxc += nx;

                ArrayList<Variable> vars = new ArrayList<>();
                if (isConstant[i]) {
                    vars.add(Variable.variable("const", Constant.C));
                }
                if (isTrend[i]) {
                    vars.add(Variable.variable("trend", new LinearTrend(hDomain.start())));
                }
                TsData[] xp = mData.get(sName).getX();
                if(xp != null) {
                    for (int k = 0; k < xp.length; ++k) {
                        String vname = "var-" + (k + 1);
                        vars.add(Variable.variable(vname, new UserVariable(vname, xp[k], null)));
                    }
                }

                indic.put(sName, vars.toArray(Variable[]::new));
            }

            disagg.put(sName, TsData.ofInternal(hDomain.getStartPeriod(), yh));
            edisagg.put(sName, TsData.ofInternal(hDomain.getStartPeriod(), DoubleSeq.of(vyh).sqrt().toArray()));
            regeffect.put(sName, TsData.ofInternal(hDomain.getStartPeriod(), rh));
            regressors.put(sName, Xo.get(i));
        }

        return MultivariateChowLinResults.builder()
                .disaggregatedSeries(disagg)
                .stdevDisaggregatedSeries(edisagg)
                .regressionEffects(regeffect)
                .regressors(regressors)
                .coefficients(regCoef)
                .coefficientsVariance(vregCoef)
                .innovationsVarCov(this.var)
                .disaggregationDomain(this.hDomain)
                .disaggregationRatio(this.ratio)
                .regressorsNames(getRegressorsName(indic))
                .build();
    }

    private MultivariateChowLinResults getResultsFromRsltsUnivariate(Map<String, RawTemporalDisaggregationResults> rsltsUnivariate) {

        Map<String, TsData> disagg = new LinkedHashMap<>();
        Map<String, TsData> edisagg = new LinkedHashMap<>();
        Map<String, TsData> regeffect = new LinkedHashMap<>();
        Map<String, FastMatrix> regressors = new LinkedHashMap<>();
        Map<String, DoubleSeq> regCoef = new LinkedHashMap<>();
        Map<String, DoubleSeq> vregCoef = new LinkedHashMap<>();
        Map<String, List<String>> regNames = new LinkedHashMap<>();

        List<String> seriesNames = new ArrayList<>(mData.keySet());
        TsPeriod start = hDomain.getStartPeriod();

        for (String name : seriesNames) {
            RawTemporalDisaggregationResults td = rsltsUnivariate.get(name);

            disagg.put(name, TsData.ofInternal(start, td.getDisaggregatedSeries().toArray()));
            edisagg.put(name, TsData.ofInternal(start, td.getStdevDisaggregatedSeries().toArray()));
            regeffect.put(name, TsData.ofInternal(start, td.getRegressionEffects().toArray()));
            regressors.put(name, td.getRegressors());
            regCoef.put(name, td.getCoefficients());
            vregCoef.put(name, td.getCoefficientsCovariance().diagonal());
            regNames.put(name, Arrays.asList(td.getRegressorsName()));
        }

        return MultivariateChowLinResults.builder()
                .disaggregatedSeries(disagg)
                .stdevDisaggregatedSeries(edisagg)
                .regressionEffects(regeffect)
                .regressors(regressors)
                .coefficients(regCoef)
                .coefficientsVariance(vregCoef)
                .disaggregationDomain(this.hDomain)
                .disaggregationRatio(this.ratio)
                .regressorsNames(regNames)
                .build();
    }

    private double[] getOrDefault(double[] input, int length, double defaultValue, String mismatchErrorMessage) {
        if (input == null) {
            double[] defaults = new double[length];
            Arrays.fill(defaults, defaultValue);
            return defaults;
        }
        if (input.length != length) {
            throw new IllegalArgumentException(mismatchErrorMessage);
        }
        return input;
    }

    private boolean[] getOrDefault(boolean[] input, int length, boolean defaultValue, String mismatchErrorMessage) {
        if (input == null) {
            boolean[] defaults = new boolean[length];
            Arrays.fill(defaults, defaultValue);
            return defaults;
        }
        if (input.length != length) {
            throw new IllegalArgumentException(mismatchErrorMessage);
        }
        return input;
    }

    private FastMatrix buildVarMatrix(MultivariateChowLinSpec.errorsVarianceMethod method, FastMatrix var, int length) {
        double[] v = new double[length];
        switch (method) {
            case fromUnivariate:
                for (int i = 0; i < length; ++i) {
                    v[i] = DescriptiveStatistics.ofInternal(this.resUnivariate[i]).getVarDF(1);
//                    v[i] = DescriptiveStatistics.ofInternal(this.resUnivariate[i]).getVar();
                }
//                double vSum = Arrays.stream(v).sum();
//                if (vSum != 0) {
//                    for (int i = 0; i < length; ++i) {
//                        v[i] = v[i] * length / vSum;
//                    }
//                }
                return FastMatrix.diagonal(DoubleSeq.of(v));
            case allEquals:
                Arrays.fill(v, 1);
                return FastMatrix.diagonal(DoubleSeq.of(v));
            case userDefined:
                if (var == null) {
                    throw new IllegalArgumentException("Errors Variance unspecified even though userDefined method has been selected");
                }
                if (!var.isSquare() || var.getColumnsCount() != length) {
                    throw new IllegalArgumentException("Errors Variance misspecified. The dimension should be " + length);
                }
                return var;
            default:
                throw new IllegalArgumentException("Unknown errors variance method: " + method);
        }
    }

    public Map<String, List<String>> getRegressorsName(Map<String, Variable[]> indicators) {

        Map<String, List<String>> names = new LinkedHashMap<>();

        for (String sName : indicators.keySet()) {
            Variable[] vars = indicators.get(sName);

            if (vars == null || vars.length == 0) {
                names.put(sName, Collections.emptyList());
                continue;
            }

            List<String> vnames = new ArrayList<>(vars.length);
            for (Variable v : vars) {
                vnames.add(v.getName());
            }

            names.put(sName, vnames);
        }

        return names;
    }


    private Map<String, DoubleSeq> residuals(Map<String, TsData> hy, Map<String, FastMatrix> hx, Map<String, DoubleSeq> coeff, int ratio, ISsf ssf) {

        Map<String, DoubleSeq> res = new LinkedHashMap<>();
        List<String> seriesNames = new ArrayList<>(hy.keySet());


//      YET TO ADAPT!
//        for (String sName : seriesNames) {
//            DoubleSeq hyi = hy.get(sName).getValues();
//            FastMatrix hxi = hx.get(sName);
//            DoubleSeq ci = coeff.get(sName);
//
//            double[] y = hyi.toArray();
//            if (hxi != null && !hxi.isEmpty()) {
//                for (int i = 0; i < y.length; i++) {
//                    if (Double.isFinite(y[i])) {
//                        y[i] = hyi.get(i) - hxi.row(i).dot(ci);
//                    }
//                }
//            }
//
//            DefaultDiffuseFilteringResults fr =
//                    DkToolkit.filter(ssf, new SsfData(y), false);
//
//            DoubleSeq errors = fr.errors(true, false);
//
//            int n = (errors.length() + ratio - 1) / ratio;
//            DoubleSeq err = DoubleSeq.of(errors.extract(ratio - 1, n, ratio).toArray());
//            err.mul(1 / model.getYfactor());
//
//            res.put(sName, err);
//        }

        return null;
    }
}
