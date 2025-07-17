/*
* Copyright 2025 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.benchmarking.base.core.ssf;

import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.ISsfErrors;
import jdplus.toolkit.base.core.ssf.multivariate.ISsfMeasurements;
import jdplus.toolkit.base.core.ssf.multivariate.MultivariateSsf;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

/**
 *
 * @author LEMASSO
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class MultivariateSsfChowLin {
    
    public Builder builder(int nvars) {
        return new Builder(nvars);
    }

    @BuilderPattern(IMultivariateSsf.class)
    public static class Builder {

        private final int nvars;
        private int conversion = 4;
        private double[] rho;
        private Constraint[] constraints = null;

        private Builder(int nvars) {
            this.nvars = nvars;
        }

        public Builder conversion(int c) {
            this.conversion = c;
            return this;
        }

        public Builder rho(double[] rho) {
            this.rho = rho;
            return this;
        }

        public Builder constraints(Constraint[] constraints) {
            this.constraints = constraints;
            return this;
        }

        public IMultivariateSsf build() {
            Data data = new Data(nvars, conversion, rho, constraints);
            return new MultivariateSsf(new Initialization(data), new Dynamics(data), new Measurements(data));
        }

    }

    static class Data {
        
        final int nvars;
        final int c;
        final double[] rho;
        final Constraint[] constraints;

        Data(int nvars, int c, double[] rho, Constraint[] constraints) {
            this.nvars = nvars;
            this.c = c;
            this.rho = rho;
            this.constraints = constraints;
        }
    }

    static class Initialization implements ISsfInitialization {

        final Data info;

        Initialization(Data info) {
            this.info = info;
        }

        @Override
        public int getStateDim() {
            return 2 * info.nvars;
        }

        @Override
        public boolean isDiffuse() {
            for (double r : info.rho){
                if (r == 1) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getDiffuseDim() {
            int nd = 0;
            for (double r : info.rho){
                if (r == 1) {
                    nd += 1;
                }
            }
            return nd;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            int nd = 0;
            for (int j = 1, k = 0; j < 2 * info.nvars; j += 2, ++k) {
                if(info.rho[k] == 1){
                    b.set(j, nd, 1);
                    nd += 1;
                } 
            }
        }

        @Override
        public void a0(DataBlock a0) {
            a0.set(0);
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            for (int k = 0, j = 0; k < info.nvars; ++k, j += 2) {
                if (info.rho[k] != 1) {
                    double v = 1 / (1 - info.rho[k] * info.rho[k]);
                    pf0.set(j + 1, j + 1, v);
                }
            }
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            for (int k = 0, j = 0; k < info.nvars; ++k, j += 2) {
                if (info.rho[k] == 1) {
                    pi0.set(j + 1, j + 1, 1);
                }
            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        final Data info;

        Dynamics(Data info) {
            this.info = info;
        }

        @Override
        public int getInnovationsDim() {
            return info.nvars;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.diagonal().extract(2, -1, 2).set(1);
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            for (int i = 0; i < info.nvars; ++i) {
                cm.set(2 * i + 1, i, 1);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            for (int i = 0; i < 2 * info.nvars; i += 2) {
                tr.set(i + 1, i + 1, info.rho[i / 2]);
                if ((pos + 1) % info.c != 0) {
                    tr.set(i, i + 1, 1);
                    if (pos % info.c != 0) {
                        tr.set(i, i, 1);
                    }
                }
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            for (int i = 0, j = 0; i < info.nvars; ++i, j += 2) {
                // case I
                if ((pos + 1) % info.c == 0) {
                    x.set(j, 0);
                } else if (pos % info.c == 0) {
                    // case II.
                    double s = x.get(j + 1);
                    x.set(j, s);
                } else {
                    // case III
                    double s = x.get(j + 1);
                    x.add(j, s);
                }
                x.mul(j + 1, info.rho[i]);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.extract(1, -1, 2).add(u);
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.diagonal().extract(1, -1, 2).add(1);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            for (int i = 0, j = 0; i < info.nvars; ++i, j += 2) {
                // case I
                if ((pos + 1) % info.c == 0) {
                    x.set(j, 0);
                    x.mul(j + 1, info.rho[i]);
                } // case II
                else if (pos % info.c == 0) {
                    double x0 = x.get(j), x1 = x.get(j + 1);
                    x.set(j, 0);
                    x.set(j + 1, x0 + info.rho[i] * x1);
                } // case III
                else {
                    double x0 = x.get(j), x1 = x.get(j + 1);
                    x.set(j + 1, x0 + info.rho[i] * x1);
                }
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.copy(x.extract(1, -1, 2));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }

    static class Measurements implements ISsfMeasurements {

        final Data info;

        Measurements(Data info) {
            this.info = info;
        }

        @Override
        public int getCount() {
            if (info.constraints == null) {
                return info.nvars;
            } else {
                return info.constraints.length + info.nvars;
            }
        }

        @Override
        public ISsfLoading loading(int equation) {
            return new Loading(info, equation);
        }

        @Override
        public ISsfErrors errors() {
            return null;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

    static class Loading implements ISsfLoading {

        final Data info;
        final int v;

        Loading(Data info, int v) {
            this.info = info;
            this.v = v;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            if (v < info.nvars) {
                int iv = 2 * v;
                if ((pos + 1) % info.c != 0) {
                    z.set(iv, 1);
                }
                z.set(iv + 1, 1);
            } else {
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int l = cnt.getIndex()[i];
                    int il = 2 * l;
                    z.set(il + 1, cnt.getWeights()[i]);
                }
            }
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            if (v < info.nvars) {
                int iv = 2 * v;
                double r = ((pos + 1) % info.c != 0) ? 0 : x.get(iv);
                return r + x.get(iv + 1);
            } else {
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                double sum = 0;
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int l = cnt.getIndex()[i];
                    int il = 2 * l;
                    sum += cnt.getWeights()[i] * x.get(il + 1);
                }
                return sum;
            }
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock x) {
            if (v < info.nvars) {
                int iv = 2 * v;
                if ((pos + 1) % info.c == 0) {
                    x.copy(m.row(iv));
                }
                x.add(m.row(iv + 1));  
            } else {
                x.set(0);
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int l = cnt.getIndex()[i];
                    int il = 2 * l;
                    x.addAY(cnt.getWeights()[i], m.row(il + 1));
                }
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix vm) {
            int iv = 2 * v;
            if (v < info.nvars) {
                double s = vm.get(iv + 1, iv + 1);                
                if ((pos + 1) % info.c == 0) {
                    s += vm.get(iv, iv);
                    s += vm.get(iv, iv + 1);
                    s += vm.get(iv + 1, iv);
                }
                return s;                
            } else {
                int w = v-info.nvars;
                Constraint cnt = info.constraints[w];
                double s = 0;
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int k = cnt.getIndex()[i];
                    int ik = 2 * k;
                    double dk = cnt.getWeights()[i];
                    for (int j = 0; j < cnt.getIndex().length; ++j) {
                        int l = cnt.getIndex()[j];
                        int il = 2 * l;
                        double dl = cnt.getWeights()[j];
                        s += dk * vm.get(ik + 1, il + 1) * dl;
                    }
                }
                return s;
            }
        }


        @Override
        public void VpZdZ(int pos, FastMatrix vm, double d) {
            int iv = 2 * v;
            if (v < info.nvars) {
                vm.add(iv + 1, iv + 1, d);
                if ((pos + 1) % info.c == 0) {
                    vm.add(iv, iv, d);
                    vm.add(iv + 1, iv, d);
                    vm.add(iv, iv + 1, d);
                } 
            }  else {
                int w = v-info.nvars;
                Constraint cnt = info.constraints[w];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int k = cnt.getIndex()[i];
                    int ik = 2 * k;
                    double dk = cnt.getWeights()[i];
                    for (int j = 0; j < cnt.getIndex().length; ++j) {
                        int l = cnt.getIndex()[j];
                        int il = 2 * l;
                        double dl = cnt.getWeights()[j];
                        vm.add(ik + 1, il + 1, d * dk * dl);
                    }
                }
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (v < info.nvars) {
                int iv = 2 * v;
                if ((pos + 1) % info.c == 0) {
                    x.add(iv, d);
                }
                x.add(iv + 1, d);
            } else {
                int w= v- info.nvars;
                Constraint cnt = info.constraints[w];
                for (int i = 0; i < cnt.getIndex().length; ++i) {
                    int k = cnt.getIndex()[i];
                    int ik = 2 * k;
                    x.add(ik + 1, cnt.getWeights()[i] * d);
                }
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }
}
