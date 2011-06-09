package cli.common.ui.control;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * A composite who has a text field and select button to allow user to select
 * from a predefined choice list (strings) with filtering support.
 * 
 * <p/>
 * A Text field will allow user to input text if user press the select button,
 * and a pop up dialog will show the choices simultaneously. 
 * The text field is read only unless user presses the select button. 
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
 *   <li>User select Select button, the filter text editor becomes editable,
 *       a choice list dialog appear under filter editor</li>
 *   <li>User type filter text, or UP/DOWN/PAGE_UP/PAGE_DOWN to select choice</li>
 *   <li>User press CR/LF to apply selection. Filter text editor becomes not editable</li>
 * </ul>
 * <p/>
 * <ul> Use Case 2
 *   <li>User select Select button, the filter text editor becomes editable,
 *       a choice list dialog appear under filter editor</li>
 *   <li>User select a choice using mouse.</li>
 *   <li>Filter text editor becomes not editable</li>
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
 * @author chengdong
 *
 */
public class FilteredStringChoiceChooser extends Composite implements KeyListener, ModifyListener, FocusListener, SelectionListener, IValueChangeListener{

  private static final boolean DEBUG=false;
  private static void debugPrint(String msg){
    if(DEBUG) System.out.println(msg);
  }
  
  /**
   * A choice provider provides a list of Strings to chose.
   * 
   * @author chengdong
   */
  public interface IChoiceProvider{
    List<String> getChoices();
  }
  
  public interface IFilteredChoiceObservableValue extends IObservableValue{
    public Text getTextControl();
  }
  
  private class FilteredChoiceObservableValue extends WritableValue implements IFilteredChoiceObservableValue{
    private Text text;

    public FilteredChoiceObservableValue(Text text) {
      super(SWTObservables.getRealm(text.getDisplay()), null, null);
      this.text=text;
    }
    @Override
    public Text getTextControl()
    {
      return text;
    }
  }
  
  private IChoiceProvider m_choiceProvider;
  private List<String> m_filteredChoices=new ArrayList<String>();

  private Job m_refreshJob; // job to filter and update the choices 
  private Job m_updateFilterJob; // job for triggering the server side ModifyEvent.
  
  private Text m_text;
  private Button m_button;
  private ChoiceDialog m_dialog ;
  private final static int VISIBLE_LINE_NUM=5; 

  private IFilteredChoiceObservableValue m_observable;
  private StringBuilder m_filterBuf=new StringBuilder(16);

  private boolean doFocusOut=true;
  
  public FilteredStringChoiceChooser(Composite parent, IChoiceProvider provider) 
  {
    super(parent, SWT.NONE);
    init(provider);
  }

  private void init(IChoiceProvider provider)
  {
    m_choiceProvider=provider;
    createControl();
    m_observable=new FilteredChoiceObservableValue(m_text);
    addListener();
    createRefreshJob();
    createUpdateFilterJob();
  }

  public Text getTextControl(){
    return m_text;
  }

  public IFilteredChoiceObservableValue getObservableValue(){
    return m_observable;
  }

  @Override
  public boolean setFocus () {
    return m_button.setFocus();
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
    m_observable.addValueChangeListener(this);
    
    /*
     * We have to separate the key event from the normal control event, since
     * RAP does not support the unTyped KeyDown event (it is not recommended to
     * use untyped event).
     * This may or may not be a bug from RAP due to this thread:
     *   http://www.eclipse.org/rap/noteworthy/1.0/
     */
    m_text.addKeyListener(this);
    
    m_text.addFocusListener(this);
    
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
    m_text.addModifyListener(this);

    m_button.addSelectionListener(this);
    
  }
  
  @Override
  public void handleValueChange(ValueChangeEvent event)
  {
    debugPrint("handleValueChange() : text="+m_text.getText());
    
    closeDialog();

    m_text.removeModifyListener(this);
    
    Object value = m_observable.getValue();
    if(value instanceof String){
      debugPrint("Observable value changed: Update text value to "+value);
      m_text.setText((String)value);
    }else
      m_text.setText("");
    
    m_text.addModifyListener(this);
  }
  
  @Override
  public void keyReleased(KeyEvent e)
  {
    debugPrint("keyReleased() : text="+m_text.getText());
    e.doit=true;
  }
  
  @Override
  public void keyPressed(KeyEvent e)
  {
    debugPrint("keyPressed() : text="+m_text.getText());
    e.doit=true;
    
    if(!m_text.getEditable()){
      if(e.keyCode == SWT.CR || e.keyCode == SWT.LF){
        debugPrint("SWT.CR|SWT.LF - START - NOTEDITABLE");
        popupDialog();
        debugPrint("SWT.CR|SWT.LF - END - NOTEDITABLE");
      }
      return;
    }
    
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
    }
    else if(e.keyCode == SWT.CR || e.keyCode == SWT.LF){
    	debugPrint("SWT.CR|SWT.LF - START");
    	e.doit=applyChoice();
      debugPrint("SWT.CR|SWT.LF - END");
    }
    else if (e.keyCode == SWT.ESC)
    {
      debugPrint("SWT.ESC - START");
      cancelChoice();
      debugPrint("SWT.ESC - END");
    }else{
        restartUpdateFilterJob();
    }
    debugPrint("keyPressed() END");
  }

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
  	
  	debugPrint("modifyText() : text="+m_text.getText());
  	if(updateFilterBuffer(m_text.getText()))
  		restartRefereshJob();
  	debugPrint("modifyText() END");
  	return;
  }
  
  protected void popupDialog()
  {
    doFocusOut=false;
    debugPrint("popupDialog(): doFocusOut=false");
    
    if(isDialogVisible()){
      debugPrint("popupDialog(): dialog is visible, close and exit");
      closeDialog();
      return;
    }
      
    m_filteredChoices.clear();
    m_filteredChoices.addAll(m_choiceProvider.getChoices());
    m_text.setText("");
    updateFilterBuffer("");
      
    /*
     *  We separate these 2 calls, the 1st call openChoiceDialog() to start
     *  a new event thread, and openChoicdDialog() will not return until 
     *  thread die.
     *  The 2nd call m_dialog.update() to update the choice dialog, where
     *  it will add selection change listener to the table viewer. This way,
     *  no selection change listener being called when dialog pop up.
     */
    m_text.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        openChoiceDialog();
      }
    });
    m_text.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        m_dialog.update();
      }
    });
  }

  private void createRefreshJob()
  {
    m_refreshJob = new Job("Filter and Refresh"){

      @Override
      protected IStatus run(IProgressMonitor monitor)
      {
        debugPrint("RefreshJob start...");
        
        List<String> elements = m_choiceProvider.getChoices();
        List<String> out = new ArrayList<String>(elements.size());
        
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
          
          String element = elements.get(i);
          try
          {
            if(element instanceof String){
              Matcher matcher = pattern.matcher((String)element);
              if(matcher.find()){
                out.add(element);
              }
            }
          }
          catch (Exception e){}
        }

        m_filteredChoices.clear();
        m_filteredChoices.addAll(out);
        
        if(m_text.isDisposed())
          return Status.CANCEL_STATUS;
        
        m_text.getDisplay().asyncExec(new Runnable()
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
        m_text.getDisplay().asyncExec(new Runnable()
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
            String msg = m_text.getText();
          	debugPrint("Inside UpdateFilter Job: text = "+msg);
          }
        });
        return Status.OK_STATUS;
      }
    };
    m_updateFilterJob.setSystem(true);
  }
  
  private void restartUpdateFilterJob()
  {
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

  private void openChoiceDialog()
  {
    doFocusOut=false;
    debugPrint("openChoiceDialog(): doFocusOut=false");
    
    if(m_dialog==null){
      debugPrint("openChoiceDialog(): m_dialog==null");
      m_dialog = new ChoiceDialog(m_text.getShell(),SWT.NO_TRIM|SWT.SHEET);
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

  protected void cancelChoice()
  {
    debugPrint("cancelChoice()");
    updateFilterBuffer(m_text.getText());
    if(m_dialog!=null){
      closeDialog();
      if(m_observable.getValue()!=null){
        m_text.removeModifyListener(this);
        m_text.setText((String) m_observable.getValue());
        m_text.addModifyListener(this);
      }
    }
  }

  private void closeDialog()
  {
    debugPrint("closeDialog()");
    if(m_dialog!=null && !m_dialog.getShell().isDisposed()){
      m_dialog.getShell().close();
      m_dialog=null;
      m_text.setEditable(false);
    }
  }

  protected boolean applyChoice()
  {
    debugPrint("applyChoice() = START");
    if(m_dialog!=null){

      Object sel = m_dialog.getSelection();
      closeDialog();
      
      m_text.setFocus();
      if(sel!=null){
        m_text.removeModifyListener(this);

        m_text.setText(sel.toString());
        m_text.setSelection(m_text.getText().length());
        updateFilterBuffer(m_text.getText());
        
        if(!m_text.getText().equals(m_observable.getValue())){
          m_observable.removeValueChangeListener(this);
          m_observable.setValue(m_text.getText());
          m_observable.addValueChangeListener(this);
        }
        
        m_text.addModifyListener(this);

      }else{
        if(m_observable.getValue() instanceof String)
          m_text.setText((String) m_observable.getValue());
      }
      
      debugPrint("Selection is: "+sel);

      debugPrint("applyChoice() = END");

      return false;
    }
    debugPrint("applyChoice() = END");
    return true;
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
    private TableViewer m_tableViewer;
    private Shell m_shell;
    private ISelectionChangedListener m_selectionChangedListener=new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged(SelectionChangedEvent event)
      {
        doFocusOut=false;
        debugPrint("ChoiceDialog::selectionChanged(): doFocusOut=false");
        
        m_text.getDisplay().asyncExec(new Runnable()
        {
          @Override
          public void run()
          {
            debugPrint("ChoiceDialog::selectionChanged(): applyChoice()");
            applyChoice();
          }
        });
      }
    };
    private DisposeListener m_disposeListener=new DisposeListener()
    {
      @Override
      public void widgetDisposed(DisposeEvent e)
      {
        debugPrint("m_shell::widgetDisposed() : text="+m_text.getText());
        cancelChoice();
      }
    };
    private Listener m_activateListener=new Listener()
    {
      @Override
      public void handleEvent(Event e)
      {
        switch(e.type){
          case SWT.Activate: // in case user select vertical scroll bar, an activate event will be fired
            debugPrint("m_shell::Activate : text="+m_text.getText());
            doFocusOut=false;
            debugPrint("m_shell::Activate: doFocusOut=false");
            m_text.setFocus();
            break;
        }
      }
    };
    private SelectionListener m_vScrollBarSelectionListener=new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        debugPrint("ChoiceDialog::VerticalBar:widgetSelected() - m_text.setFocus()");
        m_text.setFocus();
        doFocusOut=false;
        debugPrint("ChoiceDialog::VerticalBar:widgetSelected: doFocusOut=false");
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
      Shell shell = new Shell(parent, SWT.MODELESS);

      GridData gd;
      GridLayout layout = new GridLayout();
      layout.marginBottom = 0;
      layout.marginTop = 0;
      layout.marginLeft = 0;
      layout.marginRight = 0;
      layout.verticalSpacing=0;
      layout.horizontalSpacing=0;
      shell.setLayout(layout);

      m_tableViewer=new TableViewer(shell,SWT.VIRTUAL|SWT.FULL_SELECTION|SWT.SINGLE);
      gd = new GridData(SWT.FILL, SWT.FILL, true, true);
      gd.verticalIndent=0;
      m_tableViewer.getControl().setLayoutData(gd);
      m_tableViewer.setContentProvider(new ArrayContentProvider());
      m_tableViewer.setLabelProvider(new LabelProvider());
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
        newOrigin.y = oldOrigin.y + bound.height;
      }

      shell.setLocation(newOrigin);
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

      m_text.setFocus();
      m_text.setEditable(true);
      
      Display display = getShell().getDisplay();
      while (!shell.isDisposed())
      {
        if (!display.readAndDispatch())
          display.sleep();
      }
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

        String text = m_text.getText();
        int index = m_filteredChoices.indexOf(text);
        
        debugPrint("setDefaultSelection(): index="+index+", text="+text);
        
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
      // showSelectionInText();
    }
    
    public void update()
    {
      debugPrint("Dialog.update(): START");
      if(m_shell.isDisposed())
        return;
      
      m_tableViewer.removeSelectionChangedListener(m_selectionChangedListener);
      debugPrint("Dialog.update()(): removeSelectionChangedListener()");
      
    	m_tableViewer.refresh();
      m_tableViewer.getTable().getColumn(0).setWidth(m_tableViewer.getTable().getClientArea().width);
    	setDefaultSelection();

      m_tableViewer.addSelectionChangedListener(m_selectionChangedListener);
      debugPrint("Dialog.update(): addSelectionChangedListener()");

    	if(!m_text.isFocusControl()){
    		m_text.setFocus();
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
    
    public void addListeners()
    {
      ScrollBar vbar = m_tableViewer.getTable().getVerticalBar();
      if(vbar!=null)
        vbar.addSelectionListener(m_vScrollBarSelectionListener); 

      m_shell.addDisposeListener(m_disposeListener);
      m_shell.addListener(SWT.Activate, m_activateListener);
    }
  }

  private class PrivateLayout extends Layout {
    
    private FilteredStringChoiceChooser filteredCombo;

    public PrivateLayout(FilteredStringChoiceChooser fc)
    {
      filteredCombo=fc;
    }

    public void layout(Composite container, boolean force)
    {
      Button button = filteredCombo.m_button;
      Text text = filteredCombo.m_text;

      Rectangle bounds = container.getClientArea();
      Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
      if (text != null)
      {
        text.setBounds(0, 0, bounds.width - size.x, bounds.height);
      }
      button.setBounds(bounds.width - size.x, 0, size.x, bounds.height);
    }

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
//    TextFieldWithChoice instance = new TextFieldWithChoice(shell, null);
    FilteredStringChoiceChooser instance = new FilteredStringChoiceChooser(shell, new IChoiceProvider()
    {
      List<String> choices;
      
      @Override
      public List<String> getChoices()
      {
        if(choices==null){
          choices=new ArrayList<String>();
          int TOTAL=1000; // For RCP 100000 needs -Xmx1024M or more. For RAP, must less than 1000
          for(long i=0;i<TOTAL;i++){
            choices.add("UMG Entity "+i);
            choices.add("MySpace Entity "+i);
            choices.add("Vevo Entity "+i);
            choices.add("Alec Entity "+i);
          }
        }
        
        return choices;

      }
    });
    instance.getTextControl().setText("");
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(instance);
    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep();
    }
    display.dispose();
  }

	@Override
	public void focusGained(FocusEvent event) {
    debugPrint("focusGained() : text="+m_text.getText());

    doFocusOut=false;
		debugPrint("focusGained(): doFocusOut=false");
	}

	@Override
	public void focusLost(FocusEvent event) {
    debugPrint("focusLost() : text="+m_text.getText());

    if(!m_text.getEditable())
      return;
    
    if(m_choiceProvider==null)
      return;

    doFocusOut=true;
    debugPrint("focusLost(): doFocusOut=true");

    m_text.getDisplay().timerExec(100, new Runnable()
    { // timerExec will allow canceling
      public void run()
      {
        if(doFocusOut) // selecting select button will change this flag
          cancelChoice();
      }
    });
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
    debugPrint("Button::widgetSelected() : text="+m_text.getText());
    popupDialog();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
    debugPrint("Button::widgetDefaultSelected() : text="+m_text.getText());
	}

}
