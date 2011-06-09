package cli.common.ui.control;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import cli.common.ui.Activator;

/**
 * A composite who has a text field and select button to allow user to select
 * from a predefined choice list (Objects) with filtering support.
 * 
 * <p/>
 * We avoid using addListener(), since they behave differently for RCP and RAP.
 * e.g., in RAP, you have to use addKeyListener() in order to intercept the 
 * key event.
 * <p/>
 * Since the choice list may be huge, this implementation using a virtual
 * table viewer to show the list, and update the filtered list in a job.
 * <p/>
 * The usage of this widget is similar to combo, with a filter text supported.
 * <ul> Use Case 1
 *   <li>User press RETURN, a choice list dialog appear over filter 
 *       editor, with a filter text field. </li>
 *   <li>User type filter text, or UP/DOWN/PAGE_UP/PAGE_DOWN to select choice</li>
 *   <li>User press CR/LF to apply selection.li>
 * </ul>
 * <p/>
 * <ul> Use Case 2
 *   <li>User select Select button, a choice list dialog appear over filter editor</li>
 *   <li>User select a choice using mouse.</li>
 * </ul>
 * <p/>
 *  
 * Note:
 *   On Internet Explorer: if choice list size greater than 500 (depends), there  
 *   will be a popup dialog which will block you from going further. And it 
 *   prevents this widget from work correctly. 
 *   
 *   The dialog shows following message:
 *    {Error message: "A script on this page is causing Internet Explorer to run slowly"}
 *   To disable the dialog, follow instruction:  
 *     http://support.microsoft.com/kb/175500#FixItForMeAlways
 *     
 * Issue:
 *   on fire fox 3.x on linux, there is no key event for ARROW_UP and 
 *   ARROW_DOWN on Text control, which is very wierd.
 *     
 * @author chengdong
 *
 */
public class FilteredChoiceChooser extends Composite implements KeyListener, SelectionListener, IValueChangeListener{

  private static final boolean DEBUG=false;
  private static void debugPrint(String msg){
    if(DEBUG) System.out.println(msg);
  }
  static void printFocus(Composite parent)
  {
    System.out.println(parent.getDisplay().getFocusControl());
    Control[] children = parent.getChildren();
    for (int i = 0; i < children.length; i++)
    {
      debugPrint("\t" + children[i] + " "
        + children[i].isFocusControl());
    }
  }
  
  /**
   * A choice provider provides a list of object to chose and a label
   * provider to describe the object.
   * 
   * @author chengdong
   */
  public interface IChoiceProvider{
    List<?> getChoices();
    ILabelProvider getLabelProvider();
  }
  
  public interface IFilteredChoiceObservableValue extends IObservableValue{
    public FilteredChoiceChooser getChooser();
  }
  
  private class FilteredChoiceObservableValue extends WritableValue implements IFilteredChoiceObservableValue{
    private final FilteredChoiceChooser chooser;

    public FilteredChoiceObservableValue(FilteredChoiceChooser chooser) {
      this(chooser,SWTObservables.getRealm(chooser.getDisplay()));
    }
    public FilteredChoiceObservableValue(FilteredChoiceChooser chooser, Realm realm) {
      super(SWTObservables.getRealm(chooser.getDisplay()), null, null);
      this.chooser=chooser;
    }
    @Override
    public FilteredChoiceChooser getChooser()
    {
      return chooser;
    }
  }
  
  private IChoiceProvider m_choiceProvider;
  private final List<Object> m_filteredChoices=new ArrayList<Object>();

  private Text m_text;
  private Button m_button;
  private ChoiceDialog m_dialog ;
  private final static int VISIBLE_LINE_NUM=5; 

  private IFilteredChoiceObservableValue m_observable; // the value is one of the choices from provider.
  private final StringBuilder m_filterBuf=new StringBuilder(16);
private boolean m_dispose;

  public FilteredChoiceChooser(Composite parent, int style, IChoiceProvider provider)
  {
      super(parent, style);
      init(provider);
  }
  
  private void init(IChoiceProvider provider)
  {
    Assert.isNotNull(provider);
    Assert.isNotNull(provider.getChoices());
    Assert.isNotNull(provider.getLabelProvider());
    
    m_choiceProvider=provider;
    createControl();
    m_observable=new FilteredChoiceObservableValue(this);
    addListener();
  }

  public Text getTextControl(){
    return m_text;
  }

  public IFilteredChoiceObservableValue getObservableValue(){
    if(m_observable.isDisposed()){ // recreate the observable if it is disposed
      m_observable=new FilteredChoiceObservableValue(this);
    }
    return m_observable;
  }

  private String getObjectText(Object value){
    if(value != null){ // just protect myself in case label provider does not.
      return m_choiceProvider.getLabelProvider().getText(value);
    }else{
      return "";
    }
  }
  
    private void createControl() {
        setLayout(new PrivateLayout(this));
        debugPrint("toolkit==null");
        m_text = new Text(this, SWT.LEFT | SWT.BORDER | SWT.SINGLE);
        // we do not use SWT.DOWN|SWT.ARROW since RAP does not support this now
        m_button = new Button(this, SWT.NONE);
        // We do not use image either, since it is hard to compute the image
        // button size for RAP
        // m_button.setImage(Activator.getInstance().getImage(Activator.ms_arrowDown));
        m_button.setText("&Select");
        m_text.setEditable(false);

    }

  private void addListener()
  {
    /* 
     * Set the focus to the text when it is focused. 
     * Failing to do this will cause the traverse key trapped into this widget.
     */
    final FocusAdapter focusAdapter = new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        debugPrint("FilteredChoiceChooser: focusGained()");
        if(!m_dispose && !isDisposed() && !m_text.isDisposed())
          m_text.setFocus();
      }
    };
    addFocusListener(focusAdapter);
    
    /* dispose the observable value when control is disposed */
    addDisposeListener(new DisposeListener()
    {
      @Override
      public void widgetDisposed(DisposeEvent e)
      {
        debugPrint("FilteredChoiceChooser: widgetDisposed()");
        removeFocusListener(focusAdapter);
        if(!m_observable.isDisposed()){
        	try {
        		m_observable.dispose();
			} catch (Exception e2) {
				Activator.getInstance().logError(e2);
			}
        }
      }
    });

    m_observable.addValueChangeListener(this);
    
    /*
     * We have to separate the key event from the normal control event, since
     * RAP does not support the unTyped KeyDown event (it is not recommended to
     * use untyped event).
     * This may or may not be a bug from RAP due to this thread:
     *   http://www.eclipse.org/rap/noteworthy/1.0/
     */
    m_text.addKeyListener(this);

    m_text.addTraverseListener(new TraverseListener()
    {
      @Override
      public void keyTraversed(TraverseEvent e)
      {
        /*
         * The following 2 events must be prohibit in dialog, since the
         * default behavior is to close the dialog. 
         * Here we just cancel the default operation, and pass the event
         * to the text. The text will deal with the RETURN and ESC key
         * event correctly.
         * see http://www.linuxtopia.org/online_books/eclipse_documentation/eclipse_rap_development_guide/topic/org.eclipse.rap.help/help/html/reference/api/org/eclipse/swt/events/eclipse_rap_TraverseEvent.html
         */
        debugPrint("Traverse: e.detail="+e.detail);
        if(e.detail==SWT.TRAVERSE_RETURN){
          debugPrint("Traverse RETURN: - START");
          e.detail=SWT.TRAVERSE_NONE;
          e.doit=false; // cancel this operation and pass to text
          debugPrint("Traverse RETURN: - END");
        }
        else if(e.detail==SWT.TRAVERSE_ESCAPE){
          debugPrint("Traverse ESCAPE: - START");
          e.detail=SWT.TRAVERSE_NONE;
          e.doit=false; // cancel this operation and pass to text
          debugPrint("Traverse ESCAPE: - END");
        }else{
          debugPrint("Traverse ESCAPE: - START_else");
          debugPrint("Traverse ESCAPE: - END_else");
        }
      }
    });
    
    m_button.addSelectionListener(this);
    
  }
  
  @Override
  public void handleValueChange(ValueChangeEvent event)
  {
	  if(!m_text.isDisposed())
    debugPrint("handleValueChange() : text="+m_text.getText());
    
    closeDialog();

    m_text.setText(getObjectText(m_observable.getValue()));
    if(!m_text.isDisposed())
    debugPrint("Observable value changed: Update text value to "+m_text.getText());   
  }
  
  @Override
  public void keyReleased(KeyEvent e)
  {
	  if(!m_text.isDisposed())
    debugPrint("keyReleased() START: text="+m_text.getText());
    e.doit=true;

    if(!m_text.isDisposed())
    debugPrint("keyReleased(): isFocusControl()="+(getDisplay().getFocusControl()==m_text));

    if(e.keyCode == SWT.CR || e.keyCode == SWT.LF){
      debugPrint("SWT.CR|SWT.LF - START - NOTEDITABLE, e.keycode="+e.keyCode);
      if(!isReadOnlyStyle() && !isDialogVisible())
        popupDialog();
      e.doit=false;
      debugPrint("SWT.CR|SWT.LF - END - NOTEDITABLE");
    }
    if(!m_text.isDisposed())
    debugPrint("keyReleased() END: text="+m_text.getText());
    return;
  }
  
  @Override
  public void keyPressed(KeyEvent e)
  {
	  if(!m_text.isDisposed())
		  debugPrint("keyPressed() : text="+m_text.getText());
    debugPrint("keyPressed: isFocusControl()="+(getDisplay().getFocusControl()==m_text));
    debugPrint("keyPressed() END");

//    if(e.keyCode == SWT.CR || e.keyCode == SWT.LF){
//      debugPrint("SWT.CR|SWT.LF - START - NOTEDITABLE");
//      if(!isReadOnlyStyle() && !isDialogVisible())
//        popupDialog();
//      e.doit=false;
//      debugPrint("SWT.CR|SWT.LF - END - NOTEDITABLE");
//    }
  }

  protected void popupDialog()
  {
    debugPrint("popupDialog():");
    
    if(isDialogVisible()){
      debugPrint("popupDialog(): dialog is visible, close and exit");
      closeDialog();
      return;
    }
    
    if(!isControlVisible(m_text)){
      debugPrint("popupDialog(): m_text is invisible");
      return;
    }
    
    m_filteredChoices.clear();
    m_filteredChoices.addAll(m_choiceProvider.getChoices());
    updateFilterBuffer("");
      
//    /*
//     *  We separate these 2 calls, the 1st call openChoiceDialog() to start
//     *  a new event thread, and openChoicdDialog() will not return until 
//     *  thread die.
//     *  The 2nd call m_dialog.update() to update the choice dialog, where
//     *  it will add selection change listener to the table viewer. This way,
//     *  no selection change listener being called when dialog pop up.
//     */
//    m_text.getDisplay().asyncExec(new Runnable()
//    {
//      @Override
//      public void run()
//      {
//        openChoiceDialog();
//      }
//    });
//    m_text.getDisplay().asyncExec(new Runnable()
//    {
//      @Override
//      public void run()
//      {
//        if(isDialogVisible())
//          m_dialog.update();
//      }
//    });
    openChoiceDialog();
    m_dialog.update();
  }
  
  @Override
  public void widgetSelected(SelectionEvent e) {
	  if(!m_text.isDisposed())
    debugPrint("Button::widgetSelected() : text="+m_text.getText());
    popupDialog();
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e) {
	  if(!m_text.isDisposed())
    debugPrint("Button::widgetDefaultSelected() : text="+m_text.getText());
  }

  private boolean isReadOnlyStyle(){
    return (getStyle() & SWT.READ_ONLY)!=0;
  }
  
  public void select(Object value){
    m_observable.setValue(value);
  }

  private void openChoiceDialog()
  {
    debugPrint("openChoiceDialog():");
    
    if(m_text.isDisposed()){
    	debugPrint("openChoiceDialog(): m_text.isDisposed()");
      return;
    }
    
    if(m_dialog==null){
      debugPrint("openChoiceDialog(): m_dialog==null");
      m_dialog = new ChoiceDialog(getParent().getShell()/*m_text.getShell()*/,SWT.NO_TRIM);
      Rectangle bound = m_text.getBounds();
      Point pt = m_text.toDisplay(0, 0);
      bound.x = pt.x;
      bound.y = pt.y;
      m_dialog.open(bound);
    }
  }
  
  protected boolean isDialogVisible(){
    if(m_dialog!=null){
      Shell shell = m_dialog.getShell();
      return (shell!=null && !shell.isDisposed() && shell.isVisible());
    }
    return false;
  }
  
  protected boolean isControlVisible(Control control){
    return (control!=null && !control.isDisposed() && control.isVisible());
  }
  
  protected void choicePageDown()
  {
    debugPrint("PAGE_DOWN");
    if(isDialogVisible()){
      m_dialog.pageDown();
    }
  }

  protected void choicePageUp()
  {
    debugPrint("PAGE_UP");
    if(isDialogVisible()){
      m_dialog.pageUp();
    }
  }

  protected void choiceLineDown()
  {
    debugPrint("ARROW_DOWN");
    if(isDialogVisible()){
      m_dialog.rowDown();
    }
  }

  protected void choiceLineUp()
  {
    debugPrint("ARROW_UP");
    if(isDialogVisible()){
      m_dialog.rowUp();
    }
  }

  private void closeDialog()
  {
    debugPrint("closeDialog()");

    if(m_dialog!=null && !m_dialog.getShell().isDisposed()){
      m_dialog.getShell().close();
      m_dialog=null;
    }

  }

  private boolean updateFilterBuffer(String text){
    if(text.equals(m_filterBuf.toString())){
      debugPrint("update filter (FALSE): "+m_filterBuf.toString());
      return false;
    }
    
    m_filterBuf.setLength(0);
    m_filterBuf.append(text);
    debugPrint("update filter (TRUE): "+m_filterBuf.toString());
    return true;
  }

  class ChoiceDialog extends Dialog{
    private Text m_filterText;
    private TableViewer m_tableViewer;
    private Shell m_shell;
    
    private Job m_refreshJob; // job to filter and update the choices 
    private Job m_updateFilterJob; // job for triggering the server side ModifyEvent.

    private final ModifyListener m_modifyListener= new ModifyListener()
    {
      /*
       * RAP limitation:
       *   Modify events behave slightly different, they collect consecutive key-strokes and
       *   submit them in chunks, due to the latency limitations of web environment.
       *   This make it hard for picking up the real-time text. e.g., if you type "abcd", client
       *   side may only send "abc" to server, and when you call the getText(), server side only
       *   return "abc", instead of "abcd"
       *   The current implementation sends a Modify Event in two cases:
       *     a. when a keystroke event in the Text widget is detected, then, after a delay of
       *        500 ms, the ModifyEvent is sent
       *     b. when the Text widget loses focus, a ModifyEvent is sent immediately 
       *     
       *   see http://wiki.eclipse.org/7_Basic_Controls#Class_Text
       *   Bug 1:
       *      Type a string "alec", type a BACKSPACE key, there is no ModifyEvent sent. Only 
       *      keyPressed() and keyReleased() event is fired. And if you call getText() immediately,
       *      you will still get "alec". 
       *      
       *      
       *   Workaround:
       *      call m_text.getText() 500ms after keyReleased, this will trigger the serverside
       *      file a ModifyEvent. See the m_updateFilterJob.
       */
      @Override
      public void modifyText(ModifyEvent e)
      {
        // 1. update from observable will not trigger modify
        // 2. update from keyboard should trigger this and it should
        //    update the filter text
        
        debugPrint("modifyText() : text="+m_filterText.getText());
        if(updateFilterBuffer(m_filterText.getText()))
          restartRefereshJob();
        debugPrint("modifyText() END");
        return;
      }
    };

    private final KeyListener m_keyListener=new KeyListener()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
    	  if(!m_filterText.isDisposed())
        debugPrint("keyReleased() : text="+m_filterText.getText());
        e.doit=true;

        if(e.keyCode == SWT.CR || e.keyCode == SWT.LF){
          debugPrint("Dialog::SWT.CR|SWT.LF - START");
          applyChoice();
          closeDialog();
          e.doit=false;
          debugPrint("Dialog::SWT.CR|SWT.LF - END");
        }
        else if (e.keyCode == SWT.ESC)
        {
          debugPrint("Dialog::SWT.ESC - START");
          Job job=new UIJob(m_text.getDisplay(),"ApplyChoiceAndCloseDialog"){
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor)
            {
              closeDialog();            
              return Status.OK_STATUS;
            }
          };
          /*
           * We have to somehow delay the close, otherwise, there will be exception on RAP.
           * It seems that on RAP, the ESC key was somehow hi-jacked. 
           */
          
          job.schedule(100);
          e.doit=false;
          debugPrint("Dialog::SWT.ESC - END");
        }
      }
      
      @Override
      public void keyPressed(KeyEvent e)
      {
          if(!m_filterText.isDisposed())    	
        	  debugPrint("keyPressed() : text="+m_filterText.getText());
        e.doit=true;
        
        if(m_choiceProvider==null)
          return;

        // by default, UP|DOWN will move cursor to left and right. We disable them here.
        if(e.keyCode == SWT.PAGE_UP ){
          choicePageUp();
          e.doit=false; // However, for RAP , it must be true; for RCP , it should be false;
        }else if(e.keyCode == SWT.PAGE_DOWN){
          choicePageDown();
          e.doit=false;
        }else if(e.keyCode == SWT.ARROW_UP){
          choiceLineUp();
          e.doit=false;
        }else if(e.keyCode == SWT.ARROW_DOWN){
          choiceLineDown();
          e.doit=false;
        }else{
            restartUpdateFilterJob();
        }
        if(!m_filterText.isDisposed())
        	debugPrint("keyPressed() END");
      }

    };
    
    private final SelectionListener m_selectionListener=new SelectionAdapter()
    {
      
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        debugPrint("ChoiceDialog::widgetSelected():");
        if(!applyChoice())
            closeDialog();
      }
    };
    private final ShellListener m_shellListener=new ShellListener()
    {
      @Override
      public void shellDeactivated(ShellEvent e)
      {
    	  if(!m_filterText.isDisposed())
        debugPrint("m_shell::Deactivate : text="+m_filterText.getText());
        if(!applyChoice())
          closeDialog();
      }
      @Override
      public void shellClosed(ShellEvent e)
      {
    	  if(!m_text.isDisposed())
        debugPrint("m_shell::Closed : text="+m_text.getText());
        m_dialog=null;
        
      }
      
      @Override
      public void shellActivated(ShellEvent e)
      {
    	  if(!m_filterText.isDisposed())
        debugPrint("m_shell::Activate : text="+m_filterText.getText());
        m_filterText.setFocus();
      }

      @SuppressWarnings("unused")
      public void shellDeiconified(ShellEvent e)
      {
      	// Please do not use @Override annotation.
        // For RCP, this is a method must be implemented
        // For RAP, there is no such method.
        
      }

      @SuppressWarnings("unused")
      public void shellIconified(ShellEvent e)
      {
    	// Please do not use @Override annotation.
        // For RCP, this is a method must be implemented
        // For RAP, there is no such method.

      }
    };

    private final SelectionListener m_vScrollBarSelectionListener=new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        debugPrint("ChoiceDialog::VerticalBar:widgetSelected() - m_text.setFocus()");
        m_filterText.setFocus();
      }
    };

    private final TraverseListener m_traverseListener=new TraverseListener()
    {
      @Override
      public void keyTraversed(TraverseEvent e)
      {
        /*
         * Disable all traverse event, and pass the event to the text. 
         * The text will deal with the RETURN and ESC key event correctly. 
         */
        debugPrint("Dialog::Traverse: - START");
        e.detail=SWT.TRAVERSE_NONE;
        e.doit=false;
        debugPrint("Dialog::Traverse: - END");
      }
    };
    
    public ChoiceDialog(Shell parent, int style)
    {
      super(parent, style);
    }

    public Shell getShell(){
      return m_shell;
    }
    public Object getSelection()
    {
      ISelection sel = m_tableViewer.getSelection();
      if(sel instanceof IStructuredSelection){
        if(!sel.isEmpty())
          return ((IStructuredSelection)sel).getFirstElement();
      }
      return null;
    }

    private Shell createShell()
    {
      Shell parent = getParent();
      Shell shell = new Shell(parent, SWT.MODELESS|SWT.ON_TOP);
      FormLayout layout = new FormLayout();
      shell.setLayout(layout);
      
      m_filterText=new Text(shell, SWT.LEFT|SWT.BORDER|SWT.SINGLE);
      FormData fd=new FormData();
      fd.left=new FormAttachment(0,0);
      fd.right=new FormAttachment(100,0);
      m_filterText.setLayoutData(fd);
      
      
      m_tableViewer=new TableViewer(shell,SWT.VIRTUAL|SWT.FULL_SELECTION|SWT.SINGLE);
      fd=new FormData();
      fd.top=new FormAttachment(m_filterText, 0);
      fd.bottom=new FormAttachment(100, 0);
      fd.left=new FormAttachment(0, 0);
      fd.right=new FormAttachment(100, 0);
      m_tableViewer.getControl().setLayoutData(fd);
      m_tableViewer.setContentProvider(new ArrayContentProvider());
      m_tableViewer.setLabelProvider(m_choiceProvider.getLabelProvider());
      m_tableViewer.setUseHashlookup(true);
      m_tableViewer.setInput(m_filteredChoices);
  
      // We create column explicitly in order to set the column width 
      new TableColumn(m_tableViewer.getTable(), SWT.FILL);

      return shell;
    }
    
    private void repositionShell(Shell shell, Rectangle bound)
    {
      shell.pack();

      int rowHight = (m_tableViewer.getTable().getItemHeight()+m_tableViewer.getTable().getGridLineWidth());
      shell.setSize(Math.max(shell.getSize().x,bound.width), rowHight*(VISIBLE_LINE_NUM)+shell.getBorderWidth()*4);
      
      Point oldOrigin = new Point(bound.x, bound.y);
      Point newOrigin = new Point(0, 0);
      Rectangle screenBound = getParent().getDisplay().getBounds();

      // find a better location to popup the shell
      if (oldOrigin.x < 0)
        newOrigin.x = 0;
      else if (oldOrigin.x > screenBound.width - shell.getSize().x)
      {
        newOrigin.x = screenBound.width - shell.getSize().x;
      }
      else
      {
        newOrigin.x = oldOrigin.x;
      }
      if (oldOrigin.y < 0)
        newOrigin.y = 0;
      else if (oldOrigin.y > screenBound.height-shell.getSize().y-bound.height)
      {
        newOrigin.y = bound.y - shell.getSize().y;
      }
      else
      {
        newOrigin.y = oldOrigin.y;// + bound.height;
      }

      shell.setLocation(newOrigin);
      if(!shell.isDisposed())
    	  debugPrint("repositionShell(): bound="+shell.getBounds());
      shell.layout();
    }
    
    private int openShell(Shell shell)
    {
      debugPrint("Dialog=>openShell()");
      shell.open();
      debugPrint("Dialog=>openShell() 1");
      m_tableViewer.getTable().getColumn(0).setWidth(m_tableViewer.getTable().getClientArea().width);
      debugPrint("Dialog=>openShell() 2");

      // filtering the viewer whenever there is a filter text available.
      if(m_filterBuf.length()!=0){
        debugPrint("filterBuf="+m_filterBuf.toString());
        restartRefereshJob();
      }

      m_filterText.setFocus();
      
//      Display display = getShell().getDisplay();
//      while (!shell.isDisposed())
//      {
//        if (!display.readAndDispatch())
//          display.sleep();
//      }
      debugPrint("Dialog=>openShell() 3");

      return 0;
    }

    public void setDefaultSelection()
    {
      debugPrint("setDefaultSelection()");
      
      Table table = m_tableViewer.getTable();

      if(table.getSelectionIndex()>0)
        return;
      
      if(table.getItemCount()>0){

        String text = m_filterText.getText();
        Object initValue = m_observable.getValue();
        int index=-1;
        if(initValue!=null){
          index = m_filteredChoices.indexOf(initValue);
          debugPrint("setDefaultSelection(): index="+index+", text="+text);
        }
        
        if(index>=0){
          table.setSelection(index);
        }else
          table.setSelection(0);
        
        /*
         * RAP:
         *   We must call this in the case of transition from empty to non-empty.
         *   calling getText() will force server side update the table. 
         */
        table.getSelection()[0].getText();
      }
    }

    public void rowUp()
    {
      up(1);
    }
    public void pageUp()
    {
      up(VISIBLE_LINE_NUM);
    }

    private void up(int delta)
    {
      if(m_shell.isDisposed())
        return;

      Table table= m_tableViewer.getTable();
      
      ISelection selection = m_tableViewer.getSelection();
      if(selection==null||selection.isEmpty()){
        if(table.getItemCount()>0)
          table.select(table.getItemCount()-1);
      }else{
        int index = table.getSelectionIndex()-delta;
        if(index>=0){
          table.select(index);
        }else{
          if(delta==1)
            table.select(table.getItemCount()-1);
          else
            table.select(0);
        }
      }
      table.showSelection();
    }

    public void rowDown()
    {
      down(1);
    }

    public void pageDown()
    {
      down(VISIBLE_LINE_NUM);
    }
    
    private void down(int delta)
    {
      if(m_shell.isDisposed())
        return;
      
      Table table=m_tableViewer.getTable();
      
      ISelection selection = m_tableViewer.getSelection();
      if(selection==null || selection.isEmpty()){
        if(table.getItemCount()>0){
          table.select(0);
        }
      }else{
        int index = table.getSelectionIndex()+delta;
        if(index<table.getItemCount()){
          table.select(index);
        }else{
          if(delta==1)
            table.select(0);
          else
            table.select(table.getItemCount()-1);
        }
      }
      table.showSelection();
    }
    
    public void update()
    {
      debugPrint("Dialog.update(): START");
      if(m_shell.isDisposed())
        return;
      
      m_tableViewer.getTable().removeSelectionListener(m_selectionListener);
      debugPrint("Dialog.update()(): removeSelectionListener()");
      
    	m_tableViewer.refresh();
      m_tableViewer.getTable().getColumn(0).setWidth(m_tableViewer.getTable().getClientArea().width);
    	setDefaultSelection();

      m_tableViewer.getTable().addSelectionListener(m_selectionListener);
      debugPrint("Dialog.update(): addSelectionListener()");

    	if(!m_filterText.isFocusControl()){
    	  m_filterText.setFocus();
    	}
      debugPrint("Dialog.update(): END");
    }

    public int open(Rectangle bound)
    {
      m_shell = createShell();
      addListeners();
      repositionShell(m_shell, bound);
      return openShell(m_shell);
    }
    
    private void addListeners()
    {
      ScrollBar vbar = m_tableViewer.getTable().getVerticalBar();
      if(vbar!=null)
        vbar.addSelectionListener(m_vScrollBarSelectionListener); 

      m_shell.addDisposeListener(new DisposeListener()
      {
        @Override
        public void widgetDisposed(DisposeEvent event)
        {
          debugPrint("Dialog::Shell: widgetDisposed()");
          if(!m_dispose && !m_text.isDisposed())
          	m_text.setFocus();
        }
      });
      
      m_shell.addShellListener(m_shellListener);
      m_filterText.addTraverseListener(m_traverseListener);

      /*
       * RWT now supports the ModifyEvent. The two widgets that benefit from 
       * this are Text and Spinner.
       * <p/> 
       * Though implementing a ModifyListener does not differ from SWT as 
       * You can see below, the inner workings do differ.
       * <p/> 
       * In contrary to SWT that sends a ModifyEvent immediately, RWT uses 
       * a delay of currently 500 ms after which the server is notified 
       * about changes. 
       */
      m_filterText.addModifyListener(m_modifyListener);
      
      /*
       * We have to separate the key event from the normal control event, since
       * RAP does not support the unTyped KeyDown event (it is not recommended to
       * use untyped event).
       * This may or may not be a bug from RAP due to this thread:
       *   http://www.eclipse.org/rap/noteworthy/1.0/
       */
      m_filterText.addKeyListener(m_keyListener);
      
      m_tableViewer.getTable().addSelectionListener(m_selectionListener);
      
    }
    
    private void createRefreshJob()
    {
      m_refreshJob = new Job("Filter and Refresh"){

        @Override
        protected IStatus run(IProgressMonitor monitor)
        {
          debugPrint("RefreshJob start...");
          
          List<?> elements = m_choiceProvider.getChoices();
          List<Object> out = new ArrayList<Object>(elements.size());
          
          // create pattern matcher
          String name = m_filterBuf.toString();
          debugPrint("Inside RefreshJob: filterBuf = "+name);
          String patternText=name;
          if (name == null || name.equals(""))
          {
            patternText = ".*"; // If nothing set as filter, create a pattern for everything.
          }
          if (name.startsWith("*")) // avoid dangling of a start *
            patternText = "." + name;
          Pattern pattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL | Pattern.MULTILINE);

          for (int i = 0; i < elements.size(); ++i) {
            if(monitor.isCanceled()){ // check cancel status 
              debugPrint("RefreshJob is cancelled. Current index="+i);
              return Status.CANCEL_STATUS;
            }
            
            Object element = elements.get(i);
            try
            {
              String text = getObjectText(element);
              if(text != null){
                Matcher matcher = pattern.matcher(text);
                if(matcher.find()){
                  out.add(element);
                }
              }
            }
            catch (Exception e){}
          }

          m_filteredChoices.clear();
          m_filteredChoices.addAll(out);
          
          if(m_filterText.isDisposed())
            return Status.CANCEL_STATUS;
          
          m_filterText.getDisplay().asyncExec(new Runnable()
          {
            @Override
            public void run()
            {
              debugPrint("update the UI after filtering");
              if(isDialogVisible())
                m_dialog.update();
            }
          });
          debugPrint("RefreshJob DONE!!!");
          return Status.OK_STATUS;
        }
        
      };
      
      m_refreshJob.setSystem(true);
      
    }

    private void restartRefereshJob()
    {
      if(m_refreshJob!=null) 
        m_refreshJob.cancel();
      createRefreshJob();
      m_refreshJob.schedule();
    }

    private void createUpdateFilterJob()
    {
      m_updateFilterJob = new Job("Update Filter"){

        @Override
        protected IStatus run(IProgressMonitor monitor)
        {
          if(m_filterText.isDisposed()){
            debugPrint("m_updateFilterJob: m_filterText is disposed!");
            return Status.CANCEL_STATUS;
          }
          
          m_filterText.getDisplay().asyncExec(new Runnable()
          {
            @Override
            public void run()
            {
              /*
               * RAP ONLY: 
               *  This call (getText()) will trigger a ModifyEvent from m_text 
               *  on server side.
               *  Without the following call, there will be no ModifyEvent from 
               *  Text control sometime, for example, when BACKSPACE is pressed.
               *  This is because the distribution nature of RAP, the key event 
               *  has been optimized.
               */
            	if(!m_filterText.isDisposed()){
            		String msg = m_filterText.getText();
            		debugPrint("Inside UpdateFilter Job: text = "+msg);
            	}
            }
          });
          return Status.OK_STATUS;
        }
      };
      m_updateFilterJob.setSystem(true);
    }
    
    private void restartUpdateFilterJob()
    {
      if(m_updateFilterJob!=null)
        m_updateFilterJob.cancel();
      createUpdateFilterJob();

      /*
       * Wait 500ms to sync with the serverside text changes.
       * See http://wiki.eclipse.org/7_Basic_Controls#Class_Text
       *
       * Also, the display.timerExec() does not work on RAP.
       * So we use a job to get the delay.
       */
      m_updateFilterJob.schedule(600);
    }

    protected boolean applyChoice()
    {
      debugPrint("applyChoice() = START");
      if(m_dialog==null){
        debugPrint("applyChoice() = END: m_dialog==null");
        return false;
      }
      
      Object sel = m_dialog.getSelection();
      if(sel!=null){
        if(!sel.equals(m_observable.getValue())){
          m_observable.setValue(sel);
          debugPrint("applyChoice() = END: sel!=null");
          return true;
        }
      }
      debugPrint("applyChoice(): selected object is '"+getObjectText(getObservableValue().getValue())+"'");
      debugPrint("applyChoice() = END");
      return false;
    }

  }

  private class PrivateLayout extends Layout {
    
    private final FilteredChoiceChooser filteredCombo;

    public PrivateLayout(FilteredChoiceChooser fc)
    {
      filteredCombo=fc;
    }

    @Override
    public void layout(Composite container, boolean force)
    {
      Button button = filteredCombo.m_button;
      Text text = filteredCombo.m_text;

      Rectangle bounds = container.getClientArea();
      Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
      if(isReadOnlyStyle()){ // show text only
        text.setBounds(0, 0, bounds.width, bounds.height);
        button.setBounds(bounds.width, 0, 0, 0);
      }else{ // show both text and button
        text.setBounds(0, 0, bounds.width - size.x, bounds.height);
        button.setBounds(bounds.width - size.x, 0, size.x, bounds.height);
      }
    }

    @Override
    public Point computeSize(Composite editor, int wHint, int hHint,
      boolean force)
    {
      Button button = filteredCombo.m_button;
      Text text = filteredCombo.m_text;

      if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
      {
        return new Point(wHint, hHint);
      }
      Point contentsSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
      Point buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
      // Just return the button width to ensure the button is not clipped
      // if the text is long.
      // The text will just use whatever extra width there is
      Point result = new Point(buttonSize.x, Math.max(contentsSize.y,
        buttonSize.y));
      return result;
    }
  }
  
  public static void main(String[] args) {
    Display display = new Display();
    final Shell shell = new Shell(display);
    shell.setLayout(new GridLayout());
    
    // create choices
    class Model{
      String name;
      long value;
      
      public Model(String name, long value){
        this.name=name;
        this.value=value;
      }
      
      @Override
      public String toString(){
        return name+"_"+value+"_"+this.hashCode();
      }
    }
    final List<Object> choices=new ArrayList<Object>();
    int TOTAL=1000; // For RCP 100000 needs -Xmx1024M or more. For RAP, must less than 1000
    for(long i=0;i<TOTAL;i++){
      choices.add(new Model("UMG Entity",i));
      choices.add(new Model("MySpace Entity",i));
      choices.add(new Model("Vevo Entity",i));
      choices.add(new Model("Alec Entity",i));
    }

//    TextFieldWithChoice instance = new TextFieldWithChoice(shell, null);
//    FilteredChoiceChooser instance = new FilteredChoiceChooser(shell, SWT.READ_ONLY, new IChoiceProvider()
    FilteredChoiceChooser instance = new FilteredChoiceChooser(shell, SWT.NONE, new IChoiceProvider()
    {
      @Override
      public ILabelProvider getLabelProvider()
      {
        return new LabelProvider(){
          @Override
          public String getText(Object element) {
            if(element instanceof Model)
              return ((Model)element).toString();
            return super.getText(element);
          }
        };
      }
      
      @Override
      public List<Object> getChoices()
      {
        return choices;
      }
    });
    instance.getTextControl().setText("");
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(instance);
    shell.pack();
    shell.open();
    
    instance.select(choices.get(0));
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep();
    }
    display.dispose();
  }

  @Override
  public void dispose()
  {
    m_dispose = true;
    setEnabled(false); // must call this to handle focus.
    super.dispose();
  }
}
