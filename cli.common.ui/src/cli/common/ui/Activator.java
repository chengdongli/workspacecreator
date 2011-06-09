
package cli.common.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {
    public static Activator PLUGIN;
    
    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        PLUGIN=this;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        PLUGIN=null;
        Activator.context = null;
    }

    public Image getImage(String path) {
        return getImageRegistry().get(path);
    }

    protected final void registerImage(ImageRegistry registry, String path) {
        if (registry.get(path) == null) {
            ImageDescriptor descptr = imageDescriptorFromPlugin(getBundle().getSymbolicName(), path);
            registry.put(path, descptr);
        }
    }

    public void throwCoreExceptionImpl(int status, String message, Throwable e)
            throws CoreException {
        throw new CoreException(new Status(status, getBundle().getSymbolicName(), message, e));
    }

    protected void log(IStatus status) {
        getLog().log(status);
    }

    public void logError(Throwable e) {
        String message = e.getLocalizedMessage();
        if (message == null) {
            message = e.toString();
        }

        IStatus status = new Status(IStatus.ERROR, getBundle().getSymbolicName(), message, e);
        log(status);
    }

    public void logTrace(String message) {
        String id = null;
        logTrace(id, message, null);
    }

    public void logError(String message, Throwable e) {
        IStatus status = new Status(IStatus.ERROR, getBundle().getSymbolicName(), message, e);
        log(status);
    }

    public void logWarning(String message, Throwable e) {
        IStatus status = new Status(IStatus.WARNING, getBundle().getSymbolicName(), message, e);
        log(status);
    }

    public void logInfo(String message) {
        IStatus status = new Status(IStatus.INFO, getBundle().getSymbolicName(), message);
        log(status);
    }

    public void logInfo(String message, Throwable e) {
        IStatus status = new Status(IStatus.INFO, getBundle().getSymbolicName(), message, e);
        log(status);
    }

    public void logTrace(Class<?> clazz, String message) {
        logTrace(clazz, message, null);
    }

    public void logTrace(String id, String message) {
        logTrace(id, message, null);
    }

    public void logTrace(Class<?> clazz, String message, Throwable e) {
        logTrace(getClassId(clazz), message, e);
    }

    public String getClassId(Class<?> clazz) {
        String className = clazz.getName();
        String id = className.substring(className.lastIndexOf('.') + 1);
        return id;
    }

    public void logTrace(String id, String message, Throwable e) {
        StringBuilder sb = new StringBuilder(getBundle().getSymbolicName());

        if (id != null) {
            sb.append('/');
            sb.append(id);
        }

        if (isDebugOptionEnabled(id)) {
            IStatus status = new Status(IStatus.INFO, sb.toString(), message, e);
            log(status);
        }
    }

    public String getDebugOption(String id) {
        StringBuilder sb = new StringBuilder(getBundle().getSymbolicName());

        if (id != null) {
            sb.append('/');
            sb.append(id);
        }
        String option = Platform.getDebugOption(sb.toString());
        return option;
    }

    public boolean isDebugOptionEnabled(String id) {
        String option = getDebugOption(id);
        boolean enabled = option != null && option.equals("true");
        return enabled;
    }

}
