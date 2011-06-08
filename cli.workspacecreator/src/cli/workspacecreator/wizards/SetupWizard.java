/*
 * Licensed Material - Property of IBM 
 * (C) Copyright IBM Corp. 2002 - All Rights Reserved. 
 */

package cli.workspacecreator.wizards;

import cli.workspacecreator.Activator;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Wizard class
 */
public class SetupWizard extends Wizard implements IWorkbenchWizard {
    public static final String PROJECT_SET_SOURCE = "PROJECT_SET_SOURCE";
    public static final String FILE_SOURCE = "FILE_SOURCE";
    public static final String URL_SOURCE = "URL_SOURCE";
    public static final String PROJECT_SET_CONTENT = "PROJECT_SET_CONTENT";

    public static final String LOCAL_FOLDER = "LOCAL_FOLDER";
    public static final String FETCH_SOURCE = "FETCH_SOURCE";
    public static final String REPO_USER = "REPO_USER";
    public static final String REPO_URL = "REPO_URL";

    // wizard pages
    // SetupMainPage mainPage;
    ProjectSetPage projectPage;

    SourceFolderPage sourceFolderPage;

    // the model
    SetupModel model;

    // workbench selection when the wizard was started
    protected IStructuredSelection selection;

    // the workbench instance
    protected IWorkbench workbench;

    public SetupWizard() {
        super();
        model = new SetupModel();
        initDialogSettings();
    }

    private void initDialogSettings() {
        IDialogSettings section = getDialogSettings();
        model.fetchSource=section.getBoolean(FETCH_SOURCE);
        model.localFolder=section.get(LOCAL_FOLDER);
        model.repoUrl=section.get(REPO_URL);
        model.userName=section.get(REPO_USER);
        ISecurePreferences root = SecurePreferencesFactory.getDefault();
        ISecurePreferences node = root.node("/biz/tradescape/workspacecrator/svn");
        try {
            String pass = node.get("password", null/*default*/);
            if(pass!=null)
                model.password=pass;
        } catch (StorageException e) {
            e.printStackTrace();
        }

    }

    @Override
    public IDialogSettings getDialogSettings() {
        IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = dialogSettings.getSection("SetupWizard");//$NON-NLS-1$
        if (section == null) {
            section = dialogSettings.addNewSection("SetupWizard");//$NON-NLS-1$
            section.put(PROJECT_SET_SOURCE, URL_SOURCE);
            section.put(PROJECT_SET_CONTENT, "http://hera/downloads/targetplatform/rcp-projectSet.psf");
            section.put(LOCAL_FOLDER, "/tmp/svn");
            section.put(REPO_URL, "svn+ssh://localhost/opt2/svn/MarketPlaceManagementSuite/branches/UMG_GCH/plugins/");
            section.put(FETCH_SOURCE, true);
            section.put(REPO_USER, System.getProperty("user.name"));
        }
        setDialogSettings(section);
        return section;
    }
    
    public void addPages() {
        projectPage = new ProjectSetPage();
        addPage(projectPage);
        sourceFolderPage = new SourceFolderPage();
        addPage(sourceFolderPage);
    }

    /**
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }

    public boolean canFinish() {
        return super.canFinish() && model.validate().isOK();
    }

    @Override
    public boolean performFinish() {
        // String summary = model.toString();
        // MessageDialog.openInformation(getShell(), "Info", summary);
        for(IWizardPage page:getPages()){
            if(page instanceof AbstractSetupWizardPage){
                ((AbstractSetupWizardPage)page).saveSettings();
            }
        }
        return true;
    }
    
    public SetupModel getModel() {
        return model;
    }
    
    @Override
    public boolean needsProgressMonitor() {
        return true;
    }
}
