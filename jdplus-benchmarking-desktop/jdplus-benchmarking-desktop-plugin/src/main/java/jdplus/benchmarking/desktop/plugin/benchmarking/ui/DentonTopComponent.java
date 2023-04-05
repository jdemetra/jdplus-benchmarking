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
package jdplus.benchmarking.desktop.plugin.benchmarking.ui;


import jdplus.benchmarking.desktop.plugin.benchmarking.documents.DentonDocumentManager;
import jdplus.toolkit.desktop.plugin.ui.processing.Ts2ProcessingViewer;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.ui.WorkspaceTs2TopComponent;
import jdplus.benchmarking.base.core.benchmarking.univariate.DentonDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "DentonTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "demetra.desktop.benchmarking.ui.DentonTopComponent")
@ActionReference(path = "Menu/Statistical methods/Benchmarking", position = 1000)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DentonAction",
        preferredID = "DentonTopComponent")
@NbBundle.Messages({
    "CTL_DentonAction=Denton",
    "CTL_DentonTopComponent=Denton Window",
    "HINT_DentonTopComponent=This is a Denton window"
})
public final class DentonTopComponent extends WorkspaceTs2TopComponent<DentonDocument> {

    private final ExplorerManager mgr = new ExplorerManager();

    private static DentonDocumentManager manager() {
        return WorkspaceFactory.getInstance().getManager(DentonDocumentManager.class);
    }

    public DentonTopComponent() {
        this(null);
    }

    public DentonTopComponent(WorkspaceItem<DentonDocument> doc) {
        super(doc);
        initComponents();
        setToolTipText(Bundle.CTL_DentonTopComponent());
    }

    @Override
    protected Ts2ProcessingViewer initViewer() {
        //       node=new InternalNode();
        return Ts2ProcessingViewer.create(this.getElement(), DocumentUIServices.forDocument(DentonDocument.class), "Low-freq series", "High-freq series");
    }

    @Override
    public WorkspaceItem<DentonDocument> newDocument() {
        return manager().create(WorkspaceFactory.getInstance().getActiveWorkspace());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
    }// </editor-fold>                        

    // Variables declaration - do not modify                     
    // End of variables declaration                   
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String getContextPath() {
        return DentonDocumentManager.CONTEXTPATH; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

}
