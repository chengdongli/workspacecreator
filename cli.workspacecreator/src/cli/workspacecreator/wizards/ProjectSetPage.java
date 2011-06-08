
package cli.workspacecreator.wizards;

import cli.workspacecreator.Util;

import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProjectSetPage extends AbstractSetupWizardPage {
    
    private String curUrl="";
    private String curFile="";
    private boolean useUrl=false;

    private Button btnBrowseFile;

    private Button btnConfigUrl;

    private Button btnConfigFile;

    private Text txtConfigFile;

    private Text txtConfigUrl;

    private CheckboxTableViewer viewerProjects;
    private IViewerObservableSet projectsObservable;

    public ProjectSetPage() {
        super(ProjectSetPage.class.getName());
        setTitle("Select Projects to Import");
        setDescription("Select the file containing the projects configuration");
    }

    @Override
    public void createControl(Composite parent) {

        // create the composite to hold the widgets
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

        createConfigGroup(composite);
        createLine(composite, 3);
        createProjectListGroup(composite);

        // set the composite as the control for this page
        setControl(composite);

        restoreFromSettings();

        addListeners();

    }

    private void createProjectListGroup(Composite composite) {
        viewerProjects = CheckboxTableViewer.newCheckList(composite, SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(2, 3).grab(true, true)
                .applyTo(viewerProjects.getControl());
        viewerProjects.setContentProvider(new ArrayContentProvider());
        viewerProjects.setLabelProvider(new LabelProvider());
        
        projectsObservable = ViewerProperties.checkedElements(String.class).observe(viewerProjects);
        projectsObservable.addSetChangeListener(new ISetChangeListener() {
            @Override
            public void handleSetChange(SetChangeEvent event) {
                getModel().projectNames.clear();
                for(Iterator<?> it = event.getObservableSet().iterator();it.hasNext();)
                    getModel().projectNames.add((String)it.next());
                validateAndUpdate();
            }
        });
        
        Button btnSelectAll = new Button(composite, SWT.PUSH);
        btnSelectAll.setText("&Select All");
        btnSelectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewerProjects.setAllChecked(true);
                getModel().projectNames.clear();
                for(Object name:viewerProjects.getCheckedElements())
                    getModel().projectNames.add((String)name);
                validateAndUpdate();
            }
        });
        Button btnDeselectAll = new Button(composite, SWT.PUSH);
        btnDeselectAll.setText("D&eselect All");
        btnDeselectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewerProjects.setAllChecked(false);
                getModel().projectNames.clear();
                validateAndUpdate();
            }
        });
        
    }

    protected void validateAndUpdate() {
        IStatus status = validate();
        applyToStatusLine(status);
        getWizard().getContainer().updateButtons();
    }

    private void createConfigGroup(Composite composite) {
        btnConfigUrl = new Button(composite, SWT.RADIO);
        btnConfigUrl.setText("Load from Url:");
        txtConfigUrl = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false)
                .applyTo(txtConfigUrl);

        btnConfigFile = new Button(composite, SWT.RADIO);
        btnConfigFile.setText("Load from file:");
        txtConfigFile = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1).grab(true, false)
                .applyTo(txtConfigFile);

        btnBrowseFile = new Button(composite, SWT.BORDER);
        btnBrowseFile.setText("Browse...");

    }

    @Override
    public void handleEvent(Event event) {
        System.out.println("handleEvent: event.widget="+event.widget);
        
        if (event.widget == btnConfigFile || event.widget == btnConfigUrl){
            if(useUrl==btnConfigUrl.getSelection())
                return;
            useUrl=btnConfigUrl.getSelection();
            enableControls();
        } else if (event.widget == btnBrowseFile) {
            FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
            dlg.setFilterExtensions(new String[] {
                    "*.launch", "*.psf", "*"
            });
            dlg.setFilterPath(Util.getParentFolder(txtConfigFile.getText()));
            String path = dlg.open();
            if(path==null) 
                return;
            txtConfigFile.setText(path);
        }

        List<String> names=new ArrayList<String>();
        try {
            if(useUrl)
                names.addAll(getProjectNamesFromUrl());
            else
                names.addAll(getProjectNamesFromFile());
            refreshListViewer(names);
            validateAndUpdate();
        } catch (Exception e) {
            refreshListViewer(names);
            IStatus status=ValidationStatus.error(e.getLocalizedMessage());
            if(e instanceof FileNotFoundException && useUrl){
                status=ValidationStatus.error("Invalid url: "+e.getLocalizedMessage());
            }
            applyToStatusLine(status);  
            getWizard().getContainer().updateButtons();
        }

    }

    private void enableControls() {
        txtConfigFile.setEnabled(btnConfigFile.getSelection());
        txtConfigUrl.setEnabled(btnConfigUrl.getSelection());
        btnBrowseFile.setEnabled(btnConfigFile.getSelection());
    }

    @Override
    protected void addListeners() {
        btnConfigFile.addListener(SWT.Selection, this);
        btnConfigUrl.addListener(SWT.Selection, this);
        txtConfigFile.addListener(SWT.Modify, this);
        txtConfigUrl.addListener(SWT.Modify, this);
        btnBrowseFile.addListener(SWT.Selection, this);
    }

    @Override
    public void saveSettings() {
        getDialogSettings().put(SetupWizard.PROJECT_SET_SOURCE,
                btnConfigFile.getSelection() ? SetupWizard.FILE_SOURCE : SetupWizard.URL_SOURCE);
        getDialogSettings().put(SetupWizard.PROJECT_SET_CONTENT,
                btnConfigFile.getSelection() ? txtConfigFile.getText() : txtConfigUrl.getText());
    }

    /* (non-Javadoc)
     * @see biz.tradescape.workspacecreator.wizards.AbstractSetupWizardPage#restoreFromSettings()
     */
    @Override
    protected void restoreFromSettings() {
        String fetchMode = getDialogSettings().get(SetupWizard.PROJECT_SET_SOURCE);
        final String fetchContent = getDialogSettings().get(SetupWizard.PROJECT_SET_CONTENT);
        final boolean fetchFile = SetupWizard.FILE_SOURCE.equals(fetchMode);
        btnConfigFile.setSelection(fetchFile); // this won't fire SWT.Selection
        btnConfigUrl.setSelection(!fetchFile); // this won't fire SWT.Selection
                                               // event.
        txtConfigFile.setEnabled(fetchFile);
        txtConfigUrl.setEnabled(!fetchFile);
        btnBrowseFile.setEnabled(fetchFile);

        if (fetchFile){
            curFile=fetchContent;
            txtConfigFile.setText(fetchContent);
        } else{
            curUrl=fetchContent;
            txtConfigUrl.setText(fetchContent);
        }
        useUrl=!fetchFile;

        getWizard().getShell().getDisplay().timerExec(1000, new Runnable() {
            
            @Override
            public void run() {
                try {
                    getWizard().getContainer().run(true, true, new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException,
                                InterruptedException {
                            try {
                                monitor.beginTask("Retrieve project info...", 100);
                                monitor.worked(10);
                                InputStream stream=null;
                                if (fetchFile){
                                    stream=new FileInputStream(curFile);
                                } else{
                                    stream = new URL(curUrl).openStream();
                                }
                                final List<String> names = Util.getProjectNames(stream);
                                getWizard().getContainer().getShell().getDisplay().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshListViewer(names);
                                    }
                                });
                            } catch (final Exception e) {
                                getWizard().getContainer().getShell().getDisplay().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageDialog.openError(null, "Error", e.getLocalizedMessage());
                                    }
                                });
                                System.out.println("failed to connect to : "+curUrl + "\n"+e.getLocalizedMessage());
                            } finally{
                                monitor.done();
                            }
                        }
                    });
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public IStatus doValidate() {
        
        if(viewerProjects.getCheckedElements().length==0){
            return ValidationStatus.error("Please select projects to import!");
        }
        return super.doValidate();
    }

    private void refreshListViewer(final List<String> names) {
        viewerProjects.setInput(names);
        viewerProjects.setAllChecked(true);
        getModel().projectNames.clear();
        getModel().projectNames.addAll(names);
    }
    
    private List<String> getProjectNamesFromFile() throws Exception{
        List<String> names=new ArrayList<String>(); 

        String file = txtConfigFile.getText();
        if (!Util.fileReadable(file))
            throw new Exception("The selected file does not exist.");
        InputStream stream = new FileInputStream(new File(file));
        try {
            names = Util.getProjectNames(stream);
            if (names.isEmpty())
                throw new Exception("The selected file does not contain projects.");
            else {
                curFile = file;
            }
        } finally{
            stream.close();
            refreshListViewer(names);
        }
        return names;

    }
    

    private List<String> getProjectNamesFromUrl() throws Exception{
        String url = txtConfigUrl.getText();
        List<String> names=new ArrayList<String>();

        InputStream stream = null;
        try {
            stream = new URL(url).openStream();
            names = Util.getProjectNames(stream);
            if (names.isEmpty())
                throw new Exception("The selected url does not contain projects.");
            else {
                curUrl = url;
            }
        } finally{
            if(stream!=null)
                stream.close();
            refreshListViewer(names);
        }
        return names;
        
    }
    
}
