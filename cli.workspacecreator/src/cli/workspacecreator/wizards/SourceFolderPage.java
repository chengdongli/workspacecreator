
package cli.workspacecreator.wizards;

import cli.workspacecreator.Util;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SourceFolderPage extends AbstractSetupWizardPage {

    private Text txtFolder;

    private Text txtRepo;

    private Text txtUser;

    private Text txtPassword;

    private Button btnBrowseFolder;

    private Button btnFetchFromRepo;

    private Label labelRepo;

    private Label labelUser;

    private Label labelPassword;

    public SourceFolderPage() {
        super(SourceFolderPage.class.getName());
        setTitle("Select Source Folder");
        setDescription("Select the folder to import projects from");
    }

    @Override
    public void createControl(Composite parent) {

        // create the composite to hold the widgets
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

        createSourceFolderGroup(composite);

        // set the composite as the control for this page
        setControl(composite);

        restoreFromSettings();

        addListeners();
        
    }

    private void createSourceFolderGroup(Composite composite) {

        new Label(composite, SWT.NONE).setText("Folder:");
        txtFolder = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1).grab(true, false)
                .applyTo(txtFolder);
        btnBrowseFolder = new Button(composite, SWT.BORDER);
        btnBrowseFolder.setText("Browse...");

        btnFetchFromRepo = new Button(composite, SWT.CHECK);
        btnFetchFromRepo.setText("Check out source from repository");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3, 1).grab(false, false)
                .applyTo(btnFetchFromRepo);

        labelRepo = new Label(composite, SWT.NONE);
        labelRepo.setText("Repo:");
        txtRepo = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false)
                .applyTo(txtRepo);

        labelUser = new Label(composite, SWT.NONE);
        labelUser.setText("User:");
        txtUser = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false)
                .applyTo(txtUser);

        labelPassword = new Label(composite, SWT.NONE);
        labelPassword.setText("Password:");
        txtPassword = new Text(composite, SWT.PASSWORD | SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false)
                .applyTo(txtPassword);
    }

    @Override
    public void handleEvent(Event event) {
        if (event.widget == btnFetchFromRepo) {
            enableRepoGroup(btnFetchFromRepo.getSelection());
        } else if (event.widget == btnBrowseFolder) {
            DirectoryDialog dlg = new DirectoryDialog(getShell());
            dlg.setFilterPath(Util.getParentFolder(txtFolder.getText()));
            String path = dlg.open();
            if(path==null) 
                return;
            txtFolder.setText(path);
            System.out.println("Folder selected is: " + path);
        }

        IStatus status = validate();
        applyToStatusLine(status);
        getWizard().getContainer().updateButtons();
    }

    @Override
    protected void addListeners() {
        btnFetchFromRepo.addListener(SWT.Selection, this);
        btnBrowseFolder.addListener(SWT.Selection, this);
        txtRepo.addListener(SWT.Modify, this);
        txtUser.addListener(SWT.Modify, this);
        txtPassword.addListener(SWT.Modify, this);
        txtFolder.addListener(SWT.Modify, this);
    }

    private void enableRepoGroup(boolean enabled) {
        labelRepo.setEnabled(enabled);
        labelUser.setEnabled(enabled);
        labelPassword.setEnabled(enabled);
        txtRepo.setEnabled(enabled);
        txtUser.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
    }

    @Override
    public void restoreFromSettings() {
        btnFetchFromRepo.setSelection(getModel().fetchSource);
        enableRepoGroup(getModel().fetchSource);
        txtFolder.setText(getModel().localFolder);
        txtRepo.setText(getModel().repoUrl);
        txtUser.setText(getModel().userName);
        txtPassword.setText(getModel().password);
    }
    
    @Override
    public void saveSettings() {
        super.saveSettings();
        getDialogSettings().put(SetupWizard.LOCAL_FOLDER,txtFolder.getText());
        getDialogSettings().put(SetupWizard.FETCH_SOURCE,btnFetchFromRepo.getSelection());
        getDialogSettings().put(SetupWizard.REPO_URL,txtRepo.getText());
        getDialogSettings().put(SetupWizard.REPO_USER,txtUser.getText());
        
        ISecurePreferences root = SecurePreferencesFactory.getDefault();
        ISecurePreferences node = root.node("/biz/tradescape/workspacecrator/svn");
        try {
            node.put("password", txtPassword.getText(), true /*encrypt*/);
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected IStatus doValidate() {
        SetupModel model=getModel();
        
        if(!Util.folderReadable(txtFolder.getText()))
            return ValidationStatus.error("Please select a valid folder!");
        model.localFolder=txtFolder.getText();
        
        if(btnFetchFromRepo.getSelection()){
            String repo = txtRepo.getText().trim();
            String user = txtUser.getText().trim();
            String password = txtPassword.getText().trim();
            if(repo.isEmpty())
                return ValidationStatus.error("Please enter a valid url to SVN repository!");
            if(user.isEmpty())
                return ValidationStatus.error("Please enter a non-empty user name!");
            if(password.isEmpty())
                return ValidationStatus.error("Please enter a non-empty password!");
            model.repoUrl=repo;
            model.userName=user;
            model.password=password;
        }
        model.fetchSource=btnFetchFromRepo.getSelection();
        
        return super.doValidate();
    }
}
