package cli.workspacecreator.svn;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

public class OldCheckoutOp extends AbstractActionOperation{

    private String repoUrl;
    private String destPath;
    private String password;
    private String username;

    public OldCheckoutOp(String username, String password,
            String from, String to) {
        super("svn checkout", SVNMessages.class);
        this.repoUrl=from;
        this.destPath=to;
        this.username=username;
        this.password=password;
    }

    @Override
    protected void runImpl(IProgressMonitor monitor) throws Exception {
        SVNRemoteStorage storage = SVNRemoteStorage.instance();
//        storage.initialize(Activator.getDefault().getStateLocation());
        
        IRepositoryLocation location = storage.newRepositoryLocation();
        location.setStructureEnabled(true);
        location.setUsername(username);
        location.setPassword(password);
        location.setPasswordSaved(true);
        
        storage.addRepositoryLocation(location);

        ISVNConnector proxy = location.acquireSVNProxy();
        try {
            proxy.checkout(new SVNEntryRevisionReference(repoUrl), destPath,
                    Depth.INFINITY, ISVNConnector.Options.NONE, new SVNProgressMonitor(
                            this, monitor, null));
        } finally {
            location.releaseSVNProxy(proxy);
        }
    }

}
