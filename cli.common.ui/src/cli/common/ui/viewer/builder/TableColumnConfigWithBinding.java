/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */
package cli.common.ui.viewer.builder;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;

import java.util.Comparator;

/**
 * Table viewer column config for viewer using databinding for editing.
 * 
 * @author chengdong
 *
 */
public class TableColumnConfigWithBinding extends TableColumnConfig {

//  private EStructuralFeature m_feature; // the feature of the field(column) vs. row
  private IObservableMap[] m_dependencyMaps; // the observables affecting this field(column)
  private ObservablesManager m_observableManager;
  private DataBindingContext m_dbc;
  
  @Override
  protected void dispose() {
    if(m_observableManager!=null){
      m_observableManager.dispose();
      m_observableManager=null;
    }
    super.dispose();
  }
  
  public TableColumnConfigWithBinding(TableViewer viewer, DataBindingContext dbc) {
    super(viewer);
//    this.m_feature=attribute;
    this.m_dbc=dbc;
  }
  
  @Override
  protected String getTitle()
  {
//    if(getFeature()!=null)
//      return this.getFeature().getName();
    return "";
  }
  
  @Override
  protected Comparator<Object> createComarator(){
    return new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        return getText(o1).compareTo(getText(o2));
      }
    };
  }
  
  @Override
  protected CellLabelProvider createLabelProvider()
  {
    if(m_observableManager==null)
      m_observableManager=new ObservablesManager();
    
    m_observableManager.runAndCollect(new Runnable()
    {
      public void run()
      {
        m_dependencyMaps = createDependencyMaps();
      }
    });

    ObservableMapCellLabelProvider provider = new ObservableMapCellLabelProvider(m_dependencyMaps){
      @Override
      public String getToolTipText(Object element) {
        return getTooltipText(element);
      }
      @Override
      public void update(ViewerCell cell)
      {
        Object element = cell.getElement();
        cell.setText(getText(element));
        cell.setImage(getImage(element));
        cell.setBackground(getBackground(element));
        cell.setForeground(getForeground(element));
        cell.setFont(getFont(element));
      }
    };
    return provider;
  }
  
//////////////////////////// Override these iff NOT overriding createLabelProvider() ///////// 
  protected Font getFont(Object element) {
    return null;
//    if(m_fontRegistry==null){
//      m_fontRegistry = new FontRegistry(m_viewer.getControl().getShell().getDisplay());
//      m_fontRegistry.put("BOLD", new FontData[]{new FontData("Arial", 9, SWT.BOLD)} );
//    }
//    
//    return m_fontRegistry.get("BOLD");
  }

  protected Color getForeground(Object element) {
    return null;
  }

  protected Color getBackground(Object element) {
    return null;
  }

  protected String getTooltipText(Object element) {
    return null;
  }

  /** override this if you want customize the editing support */
  protected boolean canEdit(Object element) {
    return true;
  }
  
  protected Image getImage(Object element) {
    return null;
  }

  protected String getText(Object element) {
//      Object domainObj = ((EObject)element).eGet(getFeature());
//      if(domainObj!=null) 
//        return domainObj.toString();
//    }
    return "";
  }
  
  protected CellEditor getCellEditorFor(Object element) {
    return null;
  }
//////////////////////////////////////////////////////////////////////////
  
  @Override
  protected EditingSupport createEditingSupport()
  {
    return new ObservableValueEditingSupport(getViewer(),m_dbc){
      
      @Override
      protected boolean canEdit(Object element){
        return TableColumnConfigWithBinding.this.canEdit(element);
      }

      protected Binding createBinding(IObservableValue target,
        IObservableValue model)
      {
        return m_dbc.bindValue(target, model, createTargetToModelUpdateStrategy(),
          createModelToTargetUpdateStrategy());
      }
      
      @Override
      protected IObservableValue doCreateCellEditorObservable(CellEditor cellEditor)
      {
        return TableColumnConfigWithBinding.this.doCreateCellEditorObservable(cellEditor);
      }
      
      @Override
      protected IObservableValue doCreateElementObservable(Object element,
          ViewerCell cell) {
        return TableColumnConfigWithBinding.this.doCreateElementObservable(element,cell);
      }
      
      @Override
      protected CellEditor getCellEditor(Object element) {
        return TableColumnConfigWithBinding.this.getCellEditorFor(element);
      }
    };
  }

/////////////// BEGIN for Cell editing //////////////////  
  /**
   * Create observable value for the given cell editor.
   * <ul>
   *  <li>For simple cell editor, like text cell editor, using WidgetProperties
   *      to return an observable.
   *  <li>For complex cell editor, like combo viewer cell editor, observe the viewer
   *      directly.
   *  <li>For more complex one, like the dialog cell editor, create an observable
   *      so that the dialog can update it. And use the same observable for cell 
   *      editor.
   * </ul>
   * @param cellEditor
   * @return the observable value for observing the changes of the cell editor
   */
  protected IObservableValue doCreateCellEditorObservable(CellEditor cellEditor)
  {
    if(cellEditor instanceof TextCellEditor){
      Text text=(Text) ((TextCellEditor)cellEditor).getControl();
      return WidgetProperties.text(SWT.Modify).observe(text);
    }else if(cellEditor instanceof ComboBoxViewerCellEditor){
      ComboViewer viewer = ((ComboBoxViewerCellEditor) cellEditor).getViewer();
      return ViewerProperties.singleSelection().observe(viewer);
    }
    return null;
  }
  /**
   * Create the observable value for the given domain element.
   * @param element the domain element
   * @param cell the cell for displaying the domain element
   * @return the observable value for observing the changes of the domain element
   */
  protected IObservableValue doCreateElementObservable(Object element, ViewerCell cell)
  {
//    if(element instanceof EObject){
//      IObservableValue observable = EMFEditProperties.value(
//          m_editor.getEditingDomain(),
//          getFeature()
//      ).observe(element);
//      return observable;
//    }
    return null;
  }
  /** subclass can override to add convert and validation logic */
  protected UpdateValueStrategy createTargetToModelUpdateStrategy(){
    return new UpdateValueStrategy(UpdateValueStrategy.POLICY_CONVERT);
  }

  /** subclass can override to add convert and validation logic */
  protected UpdateValueStrategy createModelToTargetUpdateStrategy(){
    return null;
  }
//  protected EStructuralFeature getFeature() {
//    return m_feature;
//  }
  /**
   * Return a map to catch all changes which may affect the column.
   * (e.g., the text, the tooltip, or image overlays, and so on).
   * 
   * For example, in event variable declaration table viewer, 
   * the text for displaying event variable's precision  can be 
   * affected by the changes of balance type, if the balance type is
   * set to a not-null value, the precision column will show the 
   * balance type's precision. If the balance type is set to a 
   * null value, the precision column will show the value of the
   * event variable's precision attribute instead of the precision
   * from balance type. If the balance type change it's precision,
   * the viewer should also update the precision column. which
   * means precision column should listen to both event variable's
   * precision change and the balance type's precision change.
   * 
   * Default behavior only care the primary feature of this column. 
   */
  protected IObservableMap[] createDependencyMaps() {
//    if(getFeature()!=null){
//      IEMFEditValueProperty prop = EMFEditProperties.value(
//          m_editor.getEditingDomain(), getFeature());
//      IObservableSet knownElements = ((ObservableListContentProvider)getViewer().getContentProvider()).getKnownElements();
//      return Properties.observeEach(knownElements,new IValueProperty[]{prop});
//    }
    return new IObservableMap[0];
  }
////////////////////// END for Cell Editing /////////////////////  
  
}


