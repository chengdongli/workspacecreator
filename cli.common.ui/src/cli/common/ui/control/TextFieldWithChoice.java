package cli.common.ui.control;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A Google search style text field with filtered choices.
 * <p/>
 * A Text field will allow user to input text directly, while poping up
 * the available choices matching the typing text.
 * <p/>
 * We avoid using SWT.Modify event, since it is also used by databinding.
 * <p/> 
 * Sequence of event when type key in Text:
 *   Text=>KeyUp
 *   Dialog=>SWT.Activate
 *   Text=>SWT.FocusOut
 *   Dialog=>SWT.FocusIn
 *   Dialog=>SWT.FocusOut
 *   Dialog=>SWT.Deactivate
 *   Text=>SWT.FocusIn
 * 
 * @author chengdong
 *
 */
public class TextFieldWithChoice {
  
  /**
   * A provider provides a list of Strings to chose.
   * 
   * @author chengdong
   */
  public interface IChoiceProvider{
    List<String> getChoices();
  }
  
  private IChoiceProvider m_choiceProvider;
  
  private Text m_text;
  private ChoiceDialog m_dialog ;
  private Listener m_listener;
  private final static int VISIBLE_LINE_NUM=5; 
  
  private StringBuilder m_filterBuf=new StringBuilder();
  private ViewerFilter m_filter= new ViewerFilter()
  {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
      String name = m_filterBuf.toString();
      System.out.println("filter name: "+name);
      String patternText=name;
      if (name == null || name.equals(""))
      {
        patternText = ".*"; // If nothing set as filter, create a pattern for everything.
      }
      if (name.startsWith("*")) // avoid dangling of a start *
        patternText = "." + name;
      try
      {
        Pattern pattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE
          | Pattern.DOTALL | Pattern.MULTILINE);
        if(element instanceof String){
          Matcher matcher = pattern.matcher((String)element);
          return matcher.find();
        }
      }
      catch (Exception e)
      {
      }
      return false;
    }
  };

  
  public TextFieldWithChoice(Composite parent, IChoiceProvider provider)
  {
    m_choiceProvider=provider;
    
    m_text=createControl(parent);

    addListener();
  }
  
  private void addListener()
  {
    m_listener=new Listener(){

      @Override
      public void handleEvent(Event e)
      {
        if(m_choiceProvider==null)
          return;

        System.out.println("filter String ==: "+m_filterBuf.toString());
        switch(e.type){
          case SWT.KeyDown:
        	  System.out.println("SWT.KeyDown");
            // by default, UP|DOWN will move cursor to left and right. We disable them here.
            if(e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.PAGE_UP || e.keyCode == SWT.PAGE_DOWN){
              e.doit=true; // However, for RAP , it must be true; for RCP , it should be false;
            }
            break;
          case SWT.KeyUp:
            e.doit=false;
            System.out.println("KeyUp");
            if (e.keyCode == SWT.ARROW_UP)
            {
              choiceLineUp();
            }
            else if(e.keyCode == SWT.ARROW_DOWN){
              choiceLineDown();
            }
            else if(e.keyCode == SWT.PAGE_UP){
              choicePageUp();
            }
            else if(e.keyCode == SWT.PAGE_DOWN){
              choicePageDown();
            }
            else if(e.keyCode == SWT.CR || e.keyCode == SWT.LF){
              e.doit=applyChoice();
            }
            else if (e.keyCode == SWT.ESC)
            {
              cancelChoice();
              e.doit=true;
            }else if(e.keyCode==SWT.ARROW_LEFT){
            }else if(e.keyCode==SWT.ARROW_RIGHT){
            }else{
              if(!m_text.isDisposed() && m_text.isFocusControl()){
              m_text.getDisplay().timerExec(100, new Runnable()
              {// RAP: the key event somehow was delayed. RCP does not need timeExec.
                @Override
                public void run()
                {
                  if(!m_filterBuf.toString().equals(m_text.getText())){
                    updateFilterBuffer(); 
                    updateChoices();
                  }
                }
              });
              }
//              e.doit=true; // for RAP, set to false;
            }
            break;
          case SWT.FocusIn:
            System.out.println("SWT.FocusIn::::::::::::::::::::m_dialog=="+m_dialog);
            break;
          case SWT.FocusOut:
            System.out.println("SWT.FocusOut: m_dialog=="+m_dialog);
            if(m_dialog!=null && !m_dialog.isShellAcitvate()){
              updateFilterBuffer();
              closeDialog();
            }
            break;
        }
        
      }
      
    };
    
    m_text.addListener(SWT.KeyUp, m_listener);
    m_text.addListener(SWT.KeyDown, m_listener);
    m_text.addListener(SWT.FocusIn, m_listener);
    m_text.addListener(SWT.FocusOut, m_listener);
    
  }

  private Text createControl(Composite parent)
  {
    return new Text(parent, SWT.BORDER|SWT.SINGLE);
  }

  public Text getControl(){
    return m_text;
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
    System.out.println("PAGE_DOWN");
    if(isDialogVisible()){
      m_dialog.pageDown();
    }
  }

  protected void choicePageUp()
  {
    System.out.println("PAGE_UP");
    if(isDialogVisible()){
      m_dialog.pageUp();
    }
  }

  protected void choiceLineDown()
  {
    System.out.println("ARROW_DOWN");
    if(isDialogVisible()){
      m_dialog.rowDown();
    }
  }

  protected void choiceLineUp()
  {
    System.out.println("ARROW_UP");
    if(isDialogVisible()){
      m_dialog.rowUp();
    }
  }

  protected void cancelChoice()
  {
    System.out.println("ESC");
    updateFilterBuffer();
    if(isDialogVisible()){
      m_text.setFocus();
      closeDialog();
    }
  }

  private void closeDialog()
  {
    if(m_dialog!=null && !m_dialog.getShell().isDisposed()){
      m_dialog.getShell().close();
      m_dialog=null;
    }
  }

  protected boolean applyChoice()
  {
    System.out.println("CR/LF");
    updateFilterBuffer();
    if(m_dialog!=null){
      Object sel = m_dialog.getSelection();
      closeDialog();
      m_text.setFocus();
      if(sel!=null){
        m_text.setText(sel.toString());
        m_text.setSelection(m_text.getText().length());
      }
      
      System.out.println("Selection is: "+sel);
      return false;
    }
    return true;
  }


  protected void updateChoices()
  {
    if(!m_text.isVisible() || !m_text.getShell().isVisible() || !m_text.isFocusControl())
      return;

    boolean showChoices=false;
    for(String choice:m_choiceProvider.getChoices()){
      if(m_filter.select(null, null, choice)){
        showChoices=true;
        break;
      }
    }
    if(!showChoices){
      if(m_dialog!=null){
        closeDialog();
      }
      return;
    }
    
//    m_text.setSelection(m_text.getText().length());
    System.out.println("updateChoices");
    if(m_dialog==null){
      m_dialog = new ChoiceDialog(m_text.getShell(),SWT.NO_TRIM|SWT.SHEET);

      Rectangle bound = m_text.getBounds();
      Point pt = m_text.toDisplay(0, 0);
      bound.x = pt.x;
      bound.y = pt.y;
      m_dialog.open(bound);

    }else{

      if(isDialogVisible())
        m_dialog.update();
    }
  }

  private void updateFilterBuffer(){
    m_filterBuf.setLength(0);
    m_filterBuf.append(m_text.getText());
    System.out.println("update filter: "+m_filterBuf.toString());
  }

  class ChoiceDialog extends Dialog{
    private ListViewer m_listViewer;
    private Shell m_shell;
    private boolean m_shellActivate=false; // keep track of the shell activation.
    public boolean isShellAcitvate(){
      return m_shellActivate;
    }

    public ChoiceDialog(Shell parent, int style)
    {
      super(parent, style);
    }

    public Shell getShell(){
      return m_shell;
    }
    public Object getSelection()
    {
      ISelection sel = m_listViewer.getSelection();
      if(sel instanceof IStructuredSelection){
        if(!sel.isEmpty())
          return ((IStructuredSelection)sel).getFirstElement();
      }
      return null;
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
      org.eclipse.swt.widgets.List list=(org.eclipse.swt.widgets.List) m_listViewer.getControl();
      
      ISelection selection = m_listViewer.getSelection();
      if(selection==null||selection.isEmpty()){
        if(list.getItemCount()>0)
          list.select(list.getItemCount()-1);
      }else{
        int index = list.getSelectionIndex()-delta;
        if(index>=0){
          list.select(index);
        }else
          list.select(0);
      }
      list.showSelection();
      if(list.getSelectionIndex()>=0){
        m_text.setText(list.getItem(list.getSelectionIndex()));
        m_text.setSelection(m_text.getText().length());
      }
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
      org.eclipse.swt.widgets.List list=(org.eclipse.swt.widgets.List) m_listViewer.getControl();
      
      ISelection selection = m_listViewer.getSelection();
      if(selection==null || selection.isEmpty()){
        if(list.getItemCount()>0){
          list.select(0);
        }
      }else{
        int index = list.getSelectionIndex()+delta;
        if(index<list.getItemCount()){
          list.select(index);
        }else{
          list.select(list.getItemCount()-1);
        }
      }
      list.showSelection();
      if(list.getSelectionIndex()>=0){
        m_text.setText(list.getItem(list.getSelectionIndex()));
        m_text.setSelection(m_text.getText().length());
      }
    }

    public void update()
    {
    	m_listViewer.refresh();
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
      m_listViewer.addFilter(m_filter);
      m_listViewer.getList().addMouseListener(new MouseAdapter()
      {
        
        @Override
        public void mouseUp(MouseEvent e)
        {
          Object sel = m_dialog.getSelection();
          if(sel!=null){
            m_text.setText(sel.toString());
            updateFilterBuffer();
            closeDialog();
          }
        }
      });
      
      if(!m_shell.isDisposed()){
        m_shell.addDisposeListener(new DisposeListener()
        {
          @Override
          public void widgetDisposed(DisposeEvent e)
          {
            System.out.println("Dialog=>SWT.Dispose");
            m_dialog=null;
            updateFilterBuffer();
          }
        });
        
        m_shell.addListener(SWT.Deactivate, new Listener()
        {
          @Override
          public void handleEvent(Event event)
          {
            System.out.println("Dialog=>SWT.Deactivate");
            m_text.getDisplay().timerExec(100, new Runnable()
            {
              @Override
              public void run()
              {
                m_shellActivate=false;
                if(!m_text.isFocusControl()){
                  updateFilterBuffer();
                  closeDialog();
                }
              }
            });
          }
        });
        m_shell.addListener(SWT.Activate, new Listener()
        {
          @Override
          public void handleEvent(Event event)
          {
            System.out.println("Dialog=>SWT.Activate");
            m_shellActivate=true;
          }
        });
        m_shell.addListener(SWT.FocusIn, new Listener()
        {
          @Override
          public void handleEvent(Event event)
          {
            System.out.println("Dialog=>SWT.FocusIn");
          }
        });
        m_shell.addListener(SWT.FocusOut, new Listener()
        {
          @Override
          public void handleEvent(Event event)
          {
            System.out.println("Dialog=>SWT.FocusOut");
          }
        });
      }

    }


    private Shell createShell()
    {
      Shell parent = getParent();
      Shell shell = new Shell(parent, SWT.MODELESS);
                                                             // SWT.APPLICATION_MODAL);
      // Your code goes here (widget creation, set result, etc).
      GridData gd;
      GridLayout layout = new GridLayout();
      layout.marginBottom = 0;
      layout.marginTop = 0;
      layout.marginLeft = 0;
      layout.marginRight = 0;
      layout.verticalSpacing=0;
      layout.horizontalSpacing=0;
      shell.setLayout(layout);

      m_listViewer = new ListViewer(shell, SWT.V_SCROLL);
      gd = new GridData(SWT.FILL, SWT.FILL, true, true);
      m_listViewer.getControl().setLayoutData(gd);
      m_listViewer.setContentProvider(new ArrayContentProvider());
      m_listViewer.setInput(m_choiceProvider.getChoices());

      return shell;
    }
    
    private void repositionShell(Shell shell, Rectangle bound)
    {
      shell.pack();
      Point oldOrigin = new Point(bound.x, bound.y);
      Point newOrigin = new Point(0, 0);
      Rectangle screenBound = getParent().getDisplay().getBounds();

      // find a better location to pop-up the shell
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
      shell.setSize(bound.width, m_listViewer.getList().getItemHeight()*(VISIBLE_LINE_NUM+1));//shell.getSize().y);
      shell.layout();
    }
    
    private int openShell(Shell shell)
    {
      shell.open();
      m_text.setFocus();

      Display display = getParent().getDisplay();
      while (!shell.isDisposed())
      {
        if (!display.readAndDispatch())
          display.sleep();
      }
      return 0;
    }

  }

  public static void main(String[] args) {
    Display display = new Display();
    final Shell shell = new Shell(display);
    shell.setLayout(new FillLayout());
//    TextFieldWithChoice instance = new TextFieldWithChoice(shell, null);
    TextFieldWithChoice instance = new TextFieldWithChoice(shell, new IChoiceProvider()
    {
      @Override
      public List<String> getChoices()
      {
        List<String> choices=new ArrayList<String>();
        choices.add("UMG Entity 1");
        choices.add("UMG Entity 2");
        choices.add("UMG Entity 3");

        choices.add("MySpace Entity 1");
        choices.add("MySpace Entity 2");
        choices.add("MySpace Entity 3");
        
        choices.add("Vevo Entity 1");
        choices.add("Vevo Entity 2");
        choices.add("Vevo Entity 3");
        
        choices.add("Alec Entity 1");
        choices.add("Alec Entity 2");
        choices.add("Alec Entity 3");
        
        return choices;

      }
    });
    instance.getControl().setText("Indie Label");
    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep();
    }
    display.dispose();
  }

}
