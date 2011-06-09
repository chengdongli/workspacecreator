/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */
package cli.common.ui.viewer.custom;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A tree viewer with tooltip support.
 * 
 * @author chengdong
 */
public abstract class TreeViewerWithTooltip extends TreeViewer
  implements IMenuListener 
{
  public static final String LINE_SEP="\r\n";

  public TreeViewerWithTooltip(Composite parent, int style)
  {
    super(parent, style);
  }

  public final void menuAboutToShow(IMenuManager manager)
  {
    fillContextMenu(manager);
  }

  protected final void createSetContextMenu()
  {
    disposeMenuManagerCheck();
    createActions();
    m_menuMgr=createSetHookContextMenu(this, getTree());
  }
  
  public static MenuManager createSetHookContextMenu(IMenuListener menuOwner, Control ctrl)
  {
    MenuManager menuMgr = new MenuManager("popup");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(menuOwner);

    Menu menu = menuMgr.createContextMenu(ctrl);
    ctrl.setMenu(menu);

    return menuMgr;
  }

  protected void disposeMenuManagerCheck()
  {
    if (m_menuMgr != null)
    {
      disposeActions();
      m_menuMgr.dispose();
      m_menuMgr = null;
    }
  }
  
  protected void createActions()
  {
    /*
    m_configColAction = new ConfigureColumnsAction(this);
    
    m_expandAll=new ViewerBasedAction(this, 
      "cli.action.expand_all", 
      "E&xpand All", "Expand all", null){
      public void run() {
        expandAll();
      }
      protected boolean isEnabled(Object selectedItem)
      {
        return getInput()!=null;
      }
      
    };
    m_collapseAll=new ViewerBasedAction(this,
      "cli.action.collapse_all",
      "&Collapse All",
      "Collapse all",null){
      public void run() {
        collapseAll();
      }
      protected boolean isEnabled(Object selectedItem)
      {
        return getInput()!=null;
      }
    };
    m_expand=new ViewerBasedAction(this,
      "cli.action.expand_children",
      "Expand &Selected",
      "Expand children", null){
      public void run() {
        IStructuredSelection sel = (IStructuredSelection) getSelection();
        for(Iterator<?> iter=sel.iterator();iter.hasNext();){
          expandToLevel(iter.next(),ALL_LEVELS);
        }
      }
      protected boolean isEnabled(Object selectedItem)
      {
        IContentProvider provider = getContentProvider();
        if(provider instanceof ITreeContentProvider){
          return ((ITreeContentProvider) provider).hasChildren(selectedItem);
        }
        return false;
      }
    };
    m_collapse=new ViewerBasedAction(this,
      "cli.action.collapse_children",
      "Colla&pse Selected",
      "Collpase children",null){
      public void run() {
        IStructuredSelection sel = (IStructuredSelection) getSelection();
        for(Iterator<?> iter=sel.iterator();iter.hasNext();){
          collapseToLevel(iter.next(),ALL_LEVELS);
        }
      }
      protected boolean isEnabled(Object selectedItem)
      {
        IContentProvider provider = getContentProvider();
        if(provider instanceof ITreeContentProvider){
          return ((ITreeContentProvider) provider).hasChildren(selectedItem) && getExpandedState(selectedItem);
        }
        return false;
      }
    };
    */
  }
  
  protected void disposeActions()
  {
//  m_configColAction= CommonUIHelper.disposeAction(m_configColAction);
//  m_collapse = (ViewerBasedAction) CommonUIHelper.disposeAction(m_collapse);
//  m_collapseAll = (ViewerBasedAction) CommonUIHelper.disposeAction(m_collapseAll);
//  m_expand = (ViewerBasedAction) CommonUIHelper.disposeAction(m_expand);
//  m_expandAll = (ViewerBasedAction) CommonUIHelper.disposeAction(m_expandAll);
  }
  
  /**
   * This will be called each time menu is about to show.
   * @param manager menu manager
   */
  protected void fillContextMenu(IMenuManager manager)
  {
//  addExpandMenu(manager);
//  addColumnMenu(manager);
  }

//protected void addColumnMenu(IMenuManager manager)
//{
//  if(m_configColAction!=null && m_configColAction.isEnabled()){
//    manager.add(new Separator());
//    manager.add(m_configColAction );
//  }
//}

//protected void addExpandMenu(IMenuManager manager)
//{
//  IStructuredSelection sel = (IStructuredSelection) getSelection();
//  if(m_expandAll!=null){
//    m_expandAll.setEnabled();
//    manager.add(m_expandAll);
//  }
//  if(m_collapseAll!=null){
//    m_collapseAll.setEnabled();
//    manager.add(m_collapseAll);
//  }
//  if(!sel.isEmpty()){
//    manager.add(new Separator());
//    if(m_expand!=null){
//      m_expand.setEnabled();
//      manager.add(m_expand);
//    }
//    if(m_collapse!=null){
//      m_collapse.setEnabled();
//      manager.add(m_collapse);
//    }
//  }
//}


  /**
   * Turn on the tooltip for this tree
   * @param on true will trun on tooltip; false will turn off tooltip.
   */
  public void turnOnTooltip(boolean on){
    this.m_tooltipOn=on;
//  Tree tree=getTree();
    if(on){
      // Implement a "fake" tooltip
//    tree.addListener(SWT.Dispose, m_treeListener);
//    tree.addListener(SWT.KeyDown, m_treeListener);
//    tree.addListener(SWT.MouseMove, m_treeListener);
//    tree.addListener(SWT.MouseHover, m_treeListener); 
    }else{
//    tree.removeListener(SWT.Dispose, m_treeListener);
//    tree.removeListener(SWT.KeyDown, m_treeListener);
//    tree.removeListener(SWT.MouseMove, m_treeListener);
//    tree.removeListener(SWT.MouseHover, m_treeListener); 
    }
  }
  
  public boolean isTooltipOn(){
    return m_tooltipOn;
  }

  protected void packColumns()
  {
    Tree tree = getTree();
    for (int i = 0, n = tree.getColumnCount(); i < n; i++)
    {
      TreeColumn col = tree.getColumn(i);
      if(col.getWidth()!=0) // do not pack invisible columns
        col.pack();
    }
  }
  
  public void setInputAndSizeTable(Object input)
  {
    setInput(input);
    refresh(true);
    expandAll();
    packColumns();
  }

  public void setInputAndSizeTableIfFirstTimeInput(Object input)
  {
    setInput(input);
    refresh(true);

    if (!m_initialInputSet)
    {
      IContentProvider provider = getContentProvider();
      if (provider instanceof IStructuredContentProvider)
      {
        Object[] elements = ((IStructuredContentProvider)provider).getElements(input);
        if (elements != null && elements.length > 0)
        {
          m_initialInputSet = true;
          packColumns();
        }
      }
    }
  }

  /**
   * Given the item, return the tooltip. If this method return non-empty
   * tooltip, the setTooltip will not be called.
   *  
   * @param obj domain object
   * @param column the tree column
   * @return the tooltip for domain object
   */
  protected String getTooltip(Object domainObj, int column)
  {
    return "";
  }
  
  /**
   * Set the tooltip in the given StyledText. This will give user more
   * flexibility to customize the tooltip.
   * To make this call work, you must not overwrite getTooltip().
   * @param domainObj domain object
   * @param column column index. 0-based
   * @param control the StyledText, what you write to the StyledText will
   *          be shown in the tooltip.
   */
//protected void setTooltip(Object domainObj, int column, StyledText control){
//  // control.setText(null);
//  return;
//}

//@Override
//public void refreshViewer(Object input, boolean forceRefresh,
//  boolean selectDefaultNodeAndExpand)
//{
//  setInput(input);
//  packColumns();
//
//  if (selectDefaultNodeAndExpand)
//  {
//    selectItemInList(0);
//  }
//}
  
  public void selectItemInList(int index)
  {
    Tree tree = getTree();
    if (index >= tree.getItemCount())
    {
      return;
    }

    TreeItem ti = tree.getItem(index);
    Object itemData = (ti == null) ? null : ti.getData();
    if (itemData != null)
    {
      selectItem(itemData);
    }
  }

  public void selectItem(Object item)
  {
    if (item != null)
    {
      ISelection sel = new StructuredSelection(item);
      setSelection(sel);
    }
  }
  
    protected void addColumn(
    int style, String title, int minWidth, SelectionAdapter selAdaptor)
  {
    TreeColumn column = new TreeColumn(this.getTree(),style);
    column.setWidth(minWidth);
    column.setText(title);
    column.setResizable(true);
    column.setMoveable(true);

    if (selAdaptor != null)
    {
      column.addSelectionListener(selAdaptor);
    }
  }

  protected void addColumn(
    TreeColumnLayout columnLayout, int style, String title, int minWidth,
    int weight, SelectionAdapter selAdaptor)
  {

    TreeColumn column = new TreeColumn(this.getTree(),style);
    column.setWidth(minWidth);
    column.setText(title);
    column.setResizable(true);
    column.setMoveable(true);
    columnLayout.setColumnData(column, new ColumnWeightData(weight, minWidth));

    if (selAdaptor != null)
    {
      column.addSelectionListener(selAdaptor);
    }
  }
  
  /** Only to be used by a "Navigator". */
  public MenuManager getMenuManager()
  {
    return m_menuMgr;
  }

//protected CommonAction m_configColAction;
//protected ViewerBasedAction m_expandAll;
//protected ViewerBasedAction m_collapseAll;
//protected ViewerBasedAction m_expand;
//protected ViewerBasedAction m_collapse;
  protected MenuManager m_menuMgr;
  
  private boolean m_tooltipOn=false;
  /*
  private Listener m_labelListener = new Listener()
  {
    public void handleEvent(org.eclipse.swt.widgets.Event event)
    {
      Tree tree=getTree();
      Control label = (Control) event.widget;
      Shell shell = label.getShell();
      switch (event.type)
      {
        case SWT.MouseDown:
          org.eclipse.swt.widgets.Event e = new org.eclipse.swt.widgets.Event();
          e.item = (TreeItem) label.getData("_TREEITEM");
          // Assuming Tree is single select, set the selection as if
          // the mouse down event went through to the tree
          tree.setSelection(new TreeItem[]
          {
            (TreeItem) e.item
          });
          tree.notifyListeners(SWT.Selection, e);
          shell.dispose();
          tree.setFocus();
          break;
//      case SWT.MouseExit:
//        shell.dispose();
//        break;
      }
    }
  };
  */

  /*
  private Listener m_treeListener = new Listener()
  {
    Shell tip = null;

    StyledText label = null;

    public void handleEvent(org.eclipse.swt.widgets.Event event)
    {
      Tree tree=getTree();
      switch (event.type)
      {
        case SWT.Dispose:
        case SWT.KeyDown:
        case SWT.MouseMove:
        {
          if (tip == null)
            break;
          tip.dispose();
          tip = null;
          label = null;
          break;
        }
        case SWT.MouseHover:
        {
          Point coords = new Point(event.x, event.y);
          TreeItem item = tree.getItem(coords);
          if (item != null)
          {
            int columns = getTree().getColumnCount();
            for (int i = 0; i < columns || i == 0; i++) {
              if (item.getBounds(i).contains(coords)) {
                if (tip != null && !tip.isDisposed())
                  tip.dispose();
                Shell shell=tree.getShell();
                Display display = shell.getDisplay();
                tip = new Shell(shell, SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
                tip.setBackground(display
                  .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                FillLayout layout = new FillLayout();
                layout.marginWidth = 2;
                tip.setLayout(layout);
                label = new StyledText(tip, SWT.MULTI|SWT.WRAP);
                label.setForeground(display
                  .getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                label.setBackground(display
                  .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                label.setData("_TREEITEM", item);
                String tooltip = getTooltip(item.getData(), i);
                if(tooltip!=null && !tooltip.trim().equals(""))
                  label.setText(tooltip);
                else{
                  setTooltip(item.getData(),i,label);
                }
                label.addListener(SWT.MouseExit, m_labelListener);
                label.addListener(SWT.MouseDown, m_labelListener);
                Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                Rectangle rect = item.getBounds(i);
                Point pt = tree.toDisplay(rect.x, rect.y);
                tip.setBounds(pt.x, pt.y, size.x, size.y);
                CommonUIHelper.repositionControl(tip);
                if(label.getText().trim().equals("")){
                  tip.setVisible(false);
                }else
                  tip.setVisible(true);
              }
            }
          }
        }
      }
    }
  };
  */

  protected boolean m_initialInputSet = false;
}
