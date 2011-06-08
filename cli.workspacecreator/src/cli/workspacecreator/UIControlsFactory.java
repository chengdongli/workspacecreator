package cli.workspacecreator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;


/**
 * Originally from org.eclipse.riena.ui.swt.utils.UIControlsFactory
 */
public class UIControlsFactory {

	public static final String KEY_DECORATOR = "ts.decoration"; //$NON-NLS-1$
	
	public static final String KEY_TYPE = "type"; //$NON-NLS-1$
	public static final String TYPE_NUMERIC = "numeric"; //$NON-NLS-1$
	public static final String TYPE_DECIMAL = "decimal"; //$NON-NLS-1$
	public static final String TYPE_DATE = "date"; //$NON-NLS-1$

	public static final FieldDecoration INFO_INDICATOR = FieldDecorationRegistry.getDefault().
	    getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);

	public static final FieldDecoration ERROR_INDICATOR = FieldDecorationRegistry.getDefault().
	    getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
	
	public static final FieldDecoration WARNING_INDICATOR = FieldDecorationRegistry.getDefault().
	    getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);

//	protected static final Color SHARED_BG_COLOR=Acti;
//
//	static {
//		SHARED_BG_COLOR = LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND);
//		Assert.isNotNull(SHARED_BG_COLOR);
//	}

	protected UIControlsFactory() {
	}

	public static MenuItem createMenuItem(Menu parent, String text, int style) {
		MenuItem item = new MenuItem(parent, style);
		item.setText(text);
		return item;
	}

	public static MenuItem createMenuItem(Menu parent, String text) {
		MenuItem item = new MenuItem(parent, SWT.None);
		item.setText(text);
		return item;
	}

	public static Menu createMenu(Control parent) {
		Menu item = new Menu(parent);
		return item;
	}

	public static Menu createMenu(MenuItem parent) {
		Menu item = new Menu(parent);
		return item;
	}

	public static Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, SWT.NONE);
	}

	public static Label createDecoratedLabel(Composite parent, String text) {
		Label label=createLabel(parent, text, SWT.NONE);
		return (Label) decorate(label);
	}

	public static Label createLabel(Composite parent, String text, int style) {
		Label label = new Label(parent, style);
		label.setText(text);
		return label;
	}

	public static Link createLink(Composite parent) {
		return createLink(parent, SWT.NONE);
	}

	public static Link createDecoratedLink(Composite parent) {
		return (Link) decorate(createLink(parent, SWT.NONE));
	}

	public static Link createLink(Composite parent, int style) {
		Link result = new Link(parent, SWT.NONE);
		//result.setBackground(SHARED_BG_COLOR);
		return result;
	}

	public static Text createText(Composite parent) {
		return new Text(parent, SWT.SINGLE | SWT.BORDER);
	}

	public static Text createDecoratedText(Composite parent) {
		return (Text) decorate(new Text(parent, SWT.SINGLE | SWT.BORDER));
	}

	public static Text createText(Composite parent, int style) {
		return new Text(parent, style);
	}

	public static Text createTextDate(Composite parent) {
		Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		result.setData(KEY_TYPE, TYPE_DATE);
		return result;
	}

//	public static DatePickerComposite createDatePickerComposite(Composite parent) {
//		DatePickerComposite result = new DatePickerComposite(parent, SWT.SINGLE | SWT.RIGHT);
//		result.setData(KEY_TYPE, TYPE_DATE);
//		return result;
//	}

	public static Text createTextDecimal(Composite parent) {
		Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		result.setData(KEY_TYPE, TYPE_DECIMAL);
		return result;
	}

	public static Text createTextNumeric(Composite parent) {
		Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		result.setData(KEY_TYPE, TYPE_NUMERIC);
		return result;
	}

	public static Text createTextMultiWrap(Composite parent, boolean hScroll, boolean vScroll) {
		return createTextMulti(parent, SWT.WRAP, hScroll, vScroll);
	}

	public static Text createTextMulti(Composite parent, boolean hScroll, boolean vScroll) {
		return createTextMulti(parent, SWT.NONE, hScroll, vScroll);
	}

	public static Text createTextMulti(Composite parent, int style, boolean hScroll, boolean vScroll) {
		int txStyle = style | SWT.MULTI | SWT.BORDER;
		if (hScroll) {
			txStyle |= SWT.H_SCROLL;
		}
		if (vScroll) {
			txStyle |= SWT.V_SCROLL;
		}
		return new Text(parent, txStyle);
	}

	public static Browser createBrowser(Composite parent, int style) {
		return new Browser(parent, style);
	}

	public static Button createButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		//button.setBackground(SHARED_BG_COLOR);
		return button;
	}

	public static Button createButton(Composite parent, String text) {
		Button result = new Button(parent, SWT.PUSH);
		result.setText(text);
		return result;
	}

	public static Button createButtonToggle(Composite parent) {
		return new Button(parent, SWT.TOGGLE);
	}

	public static Button createButtonCheck(Composite parent, String text) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(text);
		//button.setBackground(SHARED_BG_COLOR);
		return button;
	}

	public static Button createButtonRadio(Composite parent) {
		Button button = new Button(parent, SWT.RADIO);
		//button.setBackground(SHARED_BG_COLOR);
		return button;
	}

	public static Button createButtonRadio(Composite parent, String text) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(text);
		//button.setBackground(SHARED_BG_COLOR);
		return button;
	}

	public static DateTime createCalendar(Composite parent) {
		DateTime result = new DateTime(parent, SWT.CALENDAR);
		//result.setBackground(SHARED_BG_COLOR);
		return result;
	}

	public static CCombo createCCombo(Composite parent) {
		CCombo result = new CCombo(parent, SWT.BORDER | SWT.READ_ONLY);
		//result.setBackground(SHARED_BG_COLOR);
		return result;
	}

	public static Combo createCombo(Composite parent) {
		return new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
	}

	public static Combo createDecoratedCombo(Composite parent) {
		return (Combo) decorate(new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY));
	}

	public static Composite createComposite(Composite parent) {
		return createComposite(parent, SWT.NONE);
	}

	public static Composite createComposite(Composite parent, int style) {
		Composite composite = new Composite(parent, style);
		//composite.setBackground(SHARED_BG_COLOR);
		return composite;
	}
	
  public static Composite createDecoratedComposite(Composite parent) {
    return (Composite) decorate(createComposite(parent));
  }

  public static Composite createDecoratedComposite(Composite parent, int style) {
    return (Composite) decorate(createComposite(parent, style));
  }

	public static Group createGroup(Composite parent, String text) {
		Group result = new Group(parent, SWT.NONE);
		result.setText(text);
		//result.setBackground(SHARED_BG_COLOR);
		return result;
	}

	public static List createList(Composite parent, boolean hScroll, boolean vScroll) {
		int style = SWT.BORDER | SWT.MULTI;
		if (hScroll) {
			style |= SWT.H_SCROLL;
		}
		if (vScroll) {
			style |= SWT.V_SCROLL;
		}
		return new List(parent, style);
	}

	public static Shell createShell(Display display) {
		Assert.isNotNull(display);
		Shell shell = new Shell(display);
		//shell.setBackground(SHARED_BG_COLOR);
		return shell;
	}

	public static Tree createTree(Composite parent, int style) {
		return new Tree(parent, style);
	}

	public static Table createTable(Composite parent, int style) {
		return new Table(parent, style);
	}
	
	public static Control decorate(final Control control){
	    ControlDecoration decoration = new ControlDecoration(control, SWT.LEFT);
	    decoration.setShowHover(true);
	    control.setData(UIControlsFactory.KEY_DECORATOR, decoration);
	    control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				Object dec = e.widget.getData(UIControlsFactory.KEY_DECORATOR);
				if(dec instanceof ControlDecoration){
					((ControlDecoration)dec).dispose();
				}
			}
		});
	    return control;
	}

	private static ControlDecoration getDecoration(Control control) {
		return (ControlDecoration) control.getData(KEY_DECORATOR);
	}

	public static boolean isDisposed(Control control){
		if(control==null || control.isDisposed())
			return true;
		if(control instanceof Composite){
			for(Control child: ((Composite) control).getChildren()){
				if(isDisposed(child))
					return true;
			}
		}
		return false;
	}
	
	public static boolean updateDecoration(Control control, IStatus status){
		if(isDisposed(control))
			return false;
		
		ControlDecoration dec=getDecoration(control);
		if(dec==null)
			return false;
		
	    dec.setDescriptionText(status.getMessage());
	    switch(status.getSeverity()){
	      case IStatus.OK:
	        dec.hide();
	        adjustLayout(control, status);
	        return true;
	      case IStatus.INFO:
	        dec.setImage(INFO_INDICATOR.getImage());
	        break;
	      case IStatus.WARNING:
	        dec.setImage(WARNING_INDICATOR.getImage());
	        break;
	      case IStatus.ERROR:
	        dec.setImage(ERROR_INDICATOR.getImage());
	        break;
	    }
	    dec.show();
//	    adjustLayout(control, status); // not needed on lucid, but need on hardy
		return true;
	}

	public static boolean adjustLayout(Control control, IStatus status) {
		if(control==null)
			return false;
		
		ControlDecoration dec = getDecoration(control);
		if(dec==null)
			return false;
		Object layoutData = control.getLayoutData();
		if (layoutData instanceof GridData) {
			int indent = FieldDecorationRegistry.getDefault()
			.getMaximumDecorationWidth();
			if (indent <= 0)
				indent = ERROR_INDICATOR.getImage().getBounds().width;

			if(status.isOK())
				indent=0;
					
			((GridData) layoutData).horizontalIndent = indent;
			if(control.getParent()!=null){
				control.getParent().layout(new Control[]{control});
				return true;
			}
		}
		return false;
	}

//	public static int getWidthHint(Button button) {
//		GC gc = new GC(button.getDisplay());
//		try {
//			FontMetrics fm = gc.getFontMetrics();
//			int widthHint = Dialog.convertHorizontalDLUsToPixels(fm, IDialogConstants.BUTTON_WIDTH);
//			Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
//			return Math.max(widthHint, minSize.x);
//		} finally {
//			gc.dispose();
//		}
//	}
//
//	public static int getWidthHint(Text text, int numChars) {
//		GC gc = new GC(text.getDisplay());
//		try {
//			FontMetrics fm = gc.getFontMetrics();
//			int widthHint = fm.getAverageCharWidth() * numChars;
//			Point minSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
//			return Math.max(widthHint, minSize.x);
//		} finally {
//			gc.dispose();
//		}
//	}
//
//	public static int getHeightHint(List list, int numItems) {
//		Assert.isLegal(numItems > 0, "numItems must be greater than 0"); //$NON-NLS-1$
//		int items = list.getItemHeight() * numItems;
//		return items;
//	}

}