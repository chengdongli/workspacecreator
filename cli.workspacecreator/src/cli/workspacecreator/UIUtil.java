
package cli.workspacecreator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class UIUtil {

    public static void formizeChildControls(Composite parent, FormToolkit formToolkit) {
        if (formToolkit == null)
            formToolkit = new FormToolkit(parent.getShell().getDisplay());
        for (Control child : parent.getChildren()) {
            if (child instanceof Composite) {
                Menu oldMenu = child.getMenu();
                formToolkit.adapt((Composite)child);
                if (oldMenu != null) {
                    // We have to override this, since
                    // formToolkit.adapt((Composite) child);
                    // will force context menu same as form context menu.
                    child.setMenu(oldMenu);
                }
                formizeChildControls((Composite)child, formToolkit);
            }
            adapt(formToolkit, child);
        }
    }

    private static void adapt(FormToolkit formToolkit, Control control) {
        if (control instanceof ExpandableComposite) {
            formToolkit.adapt(control, true, false);
        } else
            formToolkit.adapt(control, false, false);
    }

    public static void addContentListener(Control control, final Runnable runnable) {
        if (control instanceof Button || control instanceof Combo || control instanceof CCombo
                || control instanceof Spinner) {
            control.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    runnable.run();
                }
            });
        } else if (control instanceof Text
                || (control instanceof Spinner && !isReadonlyStyle(control))
                || (control instanceof Combo && !isReadonlyStyle(control))
                || (control instanceof CCombo && !isReadonlyStyle(control))) {
            control.addListener(SWT.Modify, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    runnable.run();
                }
            });
        } else if (control instanceof Composite) {
            for (Control c : ((Composite)control).getChildren()) {
                addContentListener(c, runnable);
            }
        }
    }

    /**
     * @param control
     * @return true if control style is SWT.READ_ONLY
     */
    public static boolean isReadonlyStyle(Control control)
    {
      return isReadonlyStyle(control.getStyle());
    }

    /**
     * @param style
     * @return true if style is SWT.READ_ONLY
     */
    public static boolean isReadonlyStyle(int style)
    {
      return ((style & SWT.READ_ONLY)!=0);
    }

}
