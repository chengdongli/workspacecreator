/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */

package cli.common.ui.viewer.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * The column viewer config defines the look and feel of column viewer.
 * <p/>
 * <ul>It configures:
 *   <li>the content provider of the viewer</li>
 *   <li>columns of the viewer, including column title, width</li>
 *   <li>sorter for each column</li>
 *   <li>label provide for each column</li>
 *   <li>editing support for each column</li>
 * </ul>
 * 
 * TODO: rename this class to AbstractColumnViewerAdvisor?
 * @author chengdong
 *
 */
public abstract class AbstractColumnViewerConfig implements IColumnViewerConfig
{
  abstract protected ColumnConfig[] createColumnConfigs();
  abstract protected IContentProvider createContentProvider();
  abstract protected ColumnViewer disposeViewerColumns();
  @Override
	abstract public void packViewerColumns();

  protected void setOptionalConfig(){
    // RAP only support table (NOT tree) viewer tooltip
//    ColumnViewerToolTipSupport.enableFor(getColumnViewer());
  }

  public AbstractColumnViewerConfig(ColumnViewer viewer){
    this.m_viewer=viewer;
    checkSanity(m_viewer);
    m_viewer.setContentProvider(getContentProvider());
    setOptionalConfig();
  }
  
  public IContentProvider getContentProvider() {
  	if(m_contentProvider==null)
  		m_contentProvider=createContentProvider();
		return m_contentProvider;
	}
  
	protected void checkSanity(ColumnViewer viewer){
  	// Viewer contentProvider can be changed dynamically, so we do not to enforce
//    Assert.isTrue(viewer.getContentProvider()==null,"Error: ContentProvoider has already been set!");
  }

  @Override
	final public ViewerColumn[] createAndConfigColumns(){
    m_columnConfigs = createColumnConfigs();
    ViewerColumn[] columns = new ViewerColumn[m_columnConfigs.length];
    
    for(int i=0;i<m_columnConfigs.length;i++){
      columns[i]=m_columnConfigs[i].createAndConfigColumn(m_viewer);
    }
    
    m_viewer.getControl().addDisposeListener(m_viewerDisposeListener);
    return columns;
  }
  
  @Override
	public void dispose() {
  	// This won't dispose the viewer. But dispose the columns only, since the
  	// columns are created and configured by this config.
  	// By allowing user dispose columns only, it give the user an opportunity
  	// to change the viewer config dynamically. For example, a viewer may
  	// have more than one different configs (a table and a tree, or same
  	// table, different columns) to show the same data.
  	disposeViewerColumns();
  	if(m_columnConfigs!=null){
	  	for(int i=0;i<m_columnConfigs.length;i++){
	  		m_columnConfigs[i].dispose();
	  	}
	  	m_columnConfigs=null;
  	}
  	if(m_colorManager!=null){
  		m_colorManager.dispose();
  		m_colorManager=null;
  	}
  	
  	m_contentProvider=null;
  	
  	m_viewer.getControl().removeDisposeListener(m_viewerDisposeListener);
	}
  
	protected ColumnViewer getColumnViewer(){
    return this.m_viewer;
  }

	protected Color getColor(RGB rgb){
		if(m_colorManager==null)
			m_colorManager=new ColorManager();
		return m_colorManager.getColor(rgb);
	}
	
  private IContentProvider m_contentProvider;
  private ColumnViewer m_viewer;
  private ColumnConfig[] m_columnConfigs;
  private DisposeListener m_viewerDisposeListener=new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			dispose();
		}
	};
	
  private ColorManager m_colorManager;
	
  protected ColumnConfig[] getColumnConfigs(){
    return m_columnConfigs;
  }
  
  static class ColorManager { 
    private ColorManager() {}
    
    protected Map<RGB, Color> m_colorTable= new HashMap<RGB, Color>(10);
    
    public Color getColor(RGB rgb) {
      Color color= (Color) m_colorTable.get(rgb);
      if (color == null) {
        color= new Color(Display.getCurrent(), rgb);
        m_colorTable.put(rgb, color);
      }
      return color;
    }
    
    public void dispose() {
      Iterator<Color> e= m_colorTable.values().iterator();
      while (e.hasNext())
        ((Color) e.next()).dispose();
      m_colorTable.clear();
    }
  }
  
}
