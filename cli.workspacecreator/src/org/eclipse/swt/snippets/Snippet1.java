package org.eclipse.swt.snippets;

/* 
 * example snippet: Hello World
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet1 {

public static void main (String [] args) {
	Display display = new Display ();
	Shell shell = new Shell(display);
	shell.setLayout(new GridLayout());
	final Button b1 = new Button(shell, SWT.RADIO);
	b1.setText("b1");
	final Button b2 = new Button(shell, SWT.RADIO);
	b2.setText("b2");
	final Button b3 = new Button(shell, SWT.RADIO);
	b3.setText("b3");
	b1.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            System.out.println("Listener1: b1="+b1.getSelection()+", b2="+b2.getSelection()+", b3="+b3.getSelection());
            System.out.println("Listener1: e.detail=="+e.detail+", e.detail=="+(e.detail==SWT.SELECTED));
        }
    });
    b2.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            System.out.println("Listener2: b1="+b1.getSelection()+", b2="+b2.getSelection()+", b3="+b3.getSelection());
            System.out.println("Listener2: e.detail=="+e.detail+", e.detail=="+(e.detail==SWT.SELECTED));
        }
    });
    b3.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            System.out.println("Listener3: b1="+b1.getSelection()+", b2="+b2.getSelection()+", b3="+b3.getSelection());
            System.out.println("Listener3: e.detail=="+e.detail+", e.detail=="+(e.detail==SWT.SELECTED));
        }
    });
	
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}
}
