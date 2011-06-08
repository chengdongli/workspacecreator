package cli.workspacecreator.handlers;

import cli.workspacecreator.svn.OldCheckoutOp;
import cli.workspacecreator.wizards.SetupModel;
import cli.workspacecreator.wizards.SetupWizard;
import cli.workspacecreator.wizards.SetupWizardDialog;
import cli.workspacecreator.Activator;
import cli.workspacecreator.Util;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SetupHandler extends AbstractHandler {
    
//    private static final String repoUrl="svn+ssh://hera/opt2/svn/test/branches/MarketPlaceManagementSuite";
//    private static final String destLocalPath="/tmp/svn";
//    private static final String userName="chengdong";
//    private static final String password="chengdong";
    
    private SetupModel model=null;
    
	/**
	 * The constructor.
	 */
	public SetupHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event); 
	    try {
	        SetupWizard wizard = new SetupWizard();
	            
	        // Instantiates the wizard container with the wizard and opens it
	        WizardDialog dialog = new SetupWizardDialog( window.getShell(), wizard);
	        dialog.create();
	        if(Window.OK==dialog.open()){
	            model=wizard.getModel();
	            setup();
	        }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExecutionException(e.getLocalizedMessage());
        }
		return null;
	}

    private void setup() throws Exception {
        boolean getSource=true;

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        
        ResourceBundle bundle = Activator.getDefault().getResourceBundle();
        String jobTitle=bundle.getString("Job.Label");
        
        if(getSource){

            Job job=new Job(jobTitle){
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    IStatus status=Status.OK_STATUS;
                    if(model.fetchSource){
                        File folder = new File(model.localFolder);
                        monitor.beginTask("",100);
                        monitor.subTask("Delete existent folder");
                        IProgressMonitor sub1 = new SubProgressMonitor(monitor,10);
                        sub1.beginTask("", 100);
                        if(folder.exists()){
                            File[] children = folder.listFiles();
                            if(children!=null && children.length!=0){
                                FileUtility.deleteRecursive(folder,sub1);
                            }
                        }
                        sub1.done();
                        
                        monitor.subTask("Checkout source...");
                        IProgressMonitor sub2 = new SubProgressMonitor(monitor,80);
                        sub2.beginTask("", 10000);
                        OldCheckoutOp op = new OldCheckoutOp(model.userName, model.password, model.repoUrl, model.localFolder);
                        status = op.run(sub2).getStatus();
                        if(!status.isOK())
                            return status;
                        monitor.subTask("Import projects into workspace");
                        IProgressMonitor sub3 = new SubProgressMonitor(monitor,10);
                        sub3.beginTask("", 100);
                        status = importProjects(sub3);
                    }else{
                        status = importProjects(monitor);
                    }

                    return status;
                }
            };
            
            job.setUser(true);
            PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), job);
            job.schedule();
        }else{
            Job job=new Job(jobTitle){
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    return importProjects(monitor);
                }
            };
            job.setUser(true);
            PlatformUI.getWorkbench().getProgressService().showInDialog(window.getShell(), job);
            job.schedule();
        }
        
        IPerspectiveDescriptor perspectiveDesc = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId("org.eclipse.pde.ui.PDEPerspective");
        window.getActivePage().setPerspective(perspectiveDesc);
        
    }

    protected IStatus importProjects(IProgressMonitor monitor) {
//        List<String> prjNames=getProjecNames();
        List<String> prjNames=model.projectNames;
        
        for(String prjName: prjNames){
            String prjPath = model.localFolder+File.separator+prjName;
            IStatus status=Util.importProjectToWorkspace(monitor, prjPath);
            if(!status.isOK())
                return status;
        }
        return Status.OK_STATUS;
    }

}

//        IRepositoryContainer trunk = SVNUtility.getProposedTrunk(this.location);
//        ((SVNRepositoryTrunk)trunk).getRevision();
//        ((SVNRepositoryTrunk)trunk).getChildren();
//        trunk.getInfo();
        
