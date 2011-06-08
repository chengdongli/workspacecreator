package cli.workspacecreator.wizards;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public abstract class AbstractSetupWizardPage extends WizardPage implements Listener{

    private IStatus status=Status.OK_STATUS;
    
    protected AbstractSetupWizardPage(String pageName) {
        super(pageName);
    }

    protected void createLine(Composite parent, int ncol) 
    {
        Label line = new Label(parent, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.BOLD);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(ncol,1).grab(true, false).applyTo(line);
    }
    
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub
        
    }

    public void saveSettings(){
    }
    protected void restoreFromSettings() {
    }
    
    protected void addListeners()
    {
    }
    
    final public IStatus validate(){
        return this.status=doValidate();
    }

    protected IStatus doValidate() {
        return Status.OK_STATUS;
    }

    public SetupWizard getWizard(){
        IWizard container = super.getWizard();
        Assert.isTrue(container instanceof SetupWizard);
        return (SetupWizard)container;
    }
    
    public IDialogSettings getDialogSettings(){
        return getWizard().getDialogSettings();
    }

    public SetupModel getModel(){
        return getWizard().getModel();
    }

    /**
     * Applies the status to the status line of a dialog page.
     */
    protected void applyToStatusLine(IStatus status) {
        this.status=status;
        String message= status.getMessage();
        if (message.length() == 0) message= null;
        switch (status.getSeverity()) {
            case IStatus.OK:
                setErrorMessage(null);
                setMessage(null);
                break;
            case IStatus.WARNING:
                setErrorMessage(null);
                setMessage(message, WizardPage.WARNING);
                break;              
            case IStatus.INFO:
                setErrorMessage(null);
                setMessage(message, WizardPage.INFORMATION);
                break;          
            default:
                setErrorMessage(message);
                setMessage(null);
                break;      
        }
    }
    
    @Override
    public boolean canFlipToNextPage() {
        return status.isOK() && super.canFlipToNextPage();
    }
    
    @Override
    public boolean isPageComplete() {
        return super.isPageComplete() && status.isOK();
    }
}
