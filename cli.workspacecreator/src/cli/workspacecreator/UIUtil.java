package cli.workspacecreator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class UIUtil {

    public static void formizeChildControls(Composite parent, FormToolkit formToolkit) {
        if(formToolkit==null)
          formToolkit = new FormToolkit(parent.getShell().getDisplay());
        for(Control child:parent.getChildren()){
          if(child instanceof Composite){
            Menu oldMenu = child.getMenu();
            formToolkit.adapt((Composite) child);
            if(oldMenu!=null){
              // We have to override this, since formToolkit.adapt((Composite) child);
              // will force context menu same as form context menu. 
              child.setMenu(oldMenu);
            }
            formizeChildControls((Composite) child, formToolkit);
          }
          adapt(formToolkit, child);
        }
      }
      private static void adapt(FormToolkit formToolkit, Control control)
      {
        if(control instanceof ExpandableComposite){
          formToolkit.adapt(control, true, false);
        }else
          formToolkit.adapt(control, false, false);
      }
      
}
