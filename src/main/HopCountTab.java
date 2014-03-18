package main;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;

import com.ibm.icu.text.DecimalFormat;

import parser.*;


class HopCountTab extends Tab {
  
  /* The example layout instance */
  FillLayout fillLayout;
  Text avgText,variantText,maxText,minText;
  Combo filterByCombo,fromCombo,toCombo; 

  /**
   * Creates the Tab within a given instance of LayoutExample.
   */
  HopCountTab(Analyze instance) {
    super(instance);
  }

  /**
   * Creates the widgets in the "child" group.
   */
  void createChildWidgets() {
    /* Add common controls */
    super.createChildWidgets();

    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 3;
    
    Label lblAverage = new Label(childGroup, SWT.NONE);
	lblAverage.setText("Average");
	lblAverage.setLayoutData(gridData);
	avgText = new Text(childGroup, SWT.BORDER);
	avgText.setEditable(false);
	avgText.setLayoutData(gridData);
	
	Label lblVariant = new Label(childGroup, SWT.NONE);
	lblVariant.setText("Variant");
	lblVariant.setLayoutData(gridData);
	variantText = new Text(childGroup, SWT.BORDER);
	variantText.setEditable(false);
	variantText.setLayoutData(gridData);
	
	Label lblMax = new Label(childGroup, SWT.NONE);
	lblMax.setText("Max");
	lblMax.setLayoutData(gridData);
	maxText = new Text(childGroup, SWT.BORDER);
	maxText.setEditable(false);
	maxText.setLayoutData(gridData);
	
	Label lblMin = new Label(childGroup, SWT.NONE);
	lblMin.setText("Min");
	lblMin.setLayoutData(gridData);
	minText = new Text(childGroup, SWT.BORDER);
	minText.setEditable(false);
	minText.setLayoutData(gridData);
  }

  /**
   * Creates the control widgets.
   */
  void createControlWidgets() {
    /* Controls the type of Throughput */
	   // GridData gridData=new GridData(GridData.FILL_HORIZONTAL);
	   
	    Label filterByLabel=new Label(controlGroup, SWT.None);
	    filterByLabel.setText(Analyze.getResourceString("Filter by"));
	    filterByLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
	    
	    filterByCombo = new Combo(controlGroup, SWT.READ_ONLY);
	    filterByCombo.setItems(new String[] {"Node ID", "Label ID"});
	    filterByCombo.select(0);
	    /* Add listener */
	    filterByCombo.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
	          //System.out.println(filterByCombo.getSelectionIndex());
	        	setItemFromComboToCombo();
	        }
	        public void widgetDefaultSelected(SelectionEvent e) {
	         // System.out.println("nghia");
	        }
	      });
	    
	    Label fromLabel=new Label(controlGroup, SWT.None);
	    fromLabel.setText(Analyze.getResourceString("From"));
	    fromLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
	    
	    fromCombo = new Combo(controlGroup, SWT.READ_ONLY);
	    
	    Label toLabel=new Label(controlGroup, SWT.None);
	    toLabel.setText(Analyze.getResourceString("To"));
	    toLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
	    
	    toCombo = new Combo(controlGroup, SWT.READ_ONLY);
	    setItemFromComboToCombo();
	    
	    analyze = new Button(controlGroup, SWT.PUSH);
	    analyze.setText(Analyze.getResourceString("Analyze"));
	    analyze.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
	    
	    /* Add listener to add an element to the table */
	    analyze.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent e) {
	    		if(fromCombo.getSelectionIndex()==-1 || toCombo.getSelectionIndex()==-1){
					MessageBox dialog = new MessageBox(new Shell(), SWT.ICON_QUESTION | SWT.OK);
							dialog.setText("Error");
							dialog.setMessage("Bạn phải chọn node nguồn và node đích!");
						    dialog.open(); 
				}
				else{	
					table.removeAll();
					int No=1;
					int maxHopCount=0;
					int minHopCount=1000000000;
					double totalHopCount=0;
					double totalTime=0;
					LinkedHashMap<Packet,Integer> listHopCountPacket = new LinkedHashMap<Packet,Integer>();
					for (int i=0;i<TraceFile.getListPacket().size();i++){ 
						 Packet packet=TraceFile.getListPacket().get(i);
						 if(fromCombo.getSelectionIndex()==Integer.parseInt(packet.sourceID) && toCombo.getSelectionIndex()==Integer.parseInt(packet.destID) && packet.isSuccess ){
							 TableItem tableItem= new TableItem(table, SWT.NONE);
							 tableItem.setText(0,Integer.toString(No++));
							 tableItem.setText(1,packet.id);
							 tableItem.setText(2,Integer.toString(packet.listNode.size()-2));
							 totalHopCount+=packet.listNode.size()-2;
							 totalTime+=(Double.parseDouble(packet.endTime)-Double.parseDouble(packet.startTime));
							 listHopCountPacket.put(packet,packet.listNode.size()-2);
							
							 if(maxHopCount < packet.listNode.size()-2)
								 maxHopCount = packet.listNode.size()-2;
							 if(minHopCount > packet.listNode.size()-2)
								 minHopCount = packet.listNode.size()-2;
						 }
						 
						 
						 
					}
					if(No==1){
						MessageBox dialog = new MessageBox(new Shell(), SWT.ICON_QUESTION | SWT.OK);
						dialog.setText("Error");
						dialog.setMessage("Không có packet nào đi từ node "+fromCombo.getSelectionIndex()+" đến node "+toCombo.getSelectionIndex()+"!");
					    dialog.open(); 
					    avgText.setText("0");
						variantText.setText("0");
						maxText.setText("0");
						minText.setText("0");
						xSeries=new double[0];
						ySeries=new double[0];
					}
					else{
						DecimalFormat df = new DecimalFormat("0.00");
						//System.out.println(No-1);
						String str= df.format(totalHopCount/(No-1));
						//set mean
						avgText.setText(str);
						//set text variant
						variantText.setText(df.format(variancesHopCount(listHopCountPacket,totalTime))); 
						maxText.setText(Integer.toString(maxHopCount));
						minText.setText(Integer.toString(minHopCount));
						//init line chart
						initXYseries(listHopCountPacket);
						
					}
					resetEditors();
				}
	        
	      }
	    });    
	   
	    /* Add common controls */
	    super.createControlWidgets();

   
  }
  /* Set up item for fromCombo and toCombo */
  void setItemFromComboToCombo(){
	  if(filterByCombo.getSelectionIndex()==0){
		  String[] itemList=new String[TraceFile.getListNodes().size()] ; 
			if(TraceFile.getListNodes().size()>0)
			{
				for (int i=0;i<TraceFile.getListNodes().size();i++){ 
					 NodeTrace node=TraceFile.getListNodes().get(i);
					 itemList[i]=Integer.toString(node.id);
				}
				fromCombo.setItems(itemList);
				toCombo.setItems(itemList);
			}
	  }
	  if(filterByCombo.getSelectionIndex()==1){
		 fromCombo.setItems(new String[] {});
		 toCombo.setItems(new String[] {});
	  }
  }

		
	public double variancesHopCount(LinkedHashMap<Packet,Integer> listHopCountPacket,Double totalTime){
		double variances=0; // phương sai E(X*X)-E(X)*E(X)
		double expectedValue1=0; //Giá trị kì vọng E(X*X)=x*x*p+....
		double expectedValue2=0; // E(X)=x*p+....
		for (Packet i : listHopCountPacket.keySet()) {
	          //  System.out.println( i.id +" : " + listThroughputPacket.get(i));
			expectedValue1 += listHopCountPacket.get(i)*listHopCountPacket.get(i)*
					((Double.parseDouble(i.endTime)-Double.parseDouble(i.startTime))/totalTime);
			expectedValue2 += listHopCountPacket.get(i)*((Double.parseDouble(i.endTime)-Double.parseDouble(i.startTime))/totalTime);
	        }
		variances=expectedValue1-expectedValue2*expectedValue2;
	    return variances;
	}
	
	public void initXYseries(LinkedHashMap<Packet,Integer> listHopCountPacket){
		int j=0;
		xSeries=new double[listHopCountPacket.size()];
		ySeries=new double[listHopCountPacket.size()];
		if(listHopCountPacket.size()!=0){
			for (Packet i : listHopCountPacket.keySet()) {
				ySeries[j]=listHopCountPacket.get(i);
				xSeries[j]=Double.parseDouble(i.startTime);
				j++;
			}
		}
	}
	
  /**
   * Creates the example layout.
   */
  void createLayout() {
    fillLayout = new FillLayout();
    layoutComposite.setLayout(fillLayout);
    super.createLayout();
  }

  /**
   * Disposes the editors without placing their contents into the table.
   */
  void disposeEditors() {
    
  }

  /**
   * Returns the layout data field names.
   */
  String[] getLayoutDataFieldNames() {
    return new String[] { "No", "Packet","Hop count" };
  }

  /**
   * Gets the text for the tab folder item.
   */
  String getTabText() {
    return "Hop count";
  }

  /**
   * Takes information from TableEditors and stores it.
   */
  void resetEditors() {
    setLayoutState();
    refreshLayoutComposite();
    layoutComposite.layout(true);
    layoutGroup.layout(true);
  }
  void refreshLayoutComposite() {
	    super.refreshLayoutComposite();
	    chart = new Chart(layoutComposite, SWT.NONE);
        chart.getTitle().setText("Hop count");
        chart.getAxisSet().getXAxis(0).getTitle().setText("Time(s)");
        chart.getAxisSet().getYAxis(0).getTitle().setText("Hop count");
        // create line series
        ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "line series");
        lineSeries.setYSeries(ySeries);
        lineSeries.setXSeries(xSeries);

        // adjust the axis range
        chart.getAxisSet().adjustRange();
        /* export listener  */
        exportImage.addSelectionListener(new SelectionAdapter() {
  	      public void widgetSelected(SelectionEvent e) {
  	    	  FileDialog fd = new FileDialog(new Shell(), SWT.SAVE);
  	          fd.setText("Save");
  	          fd.setFilterPath("D:\\");
  	          String[] filterExt = { "*.png" };
  	          fd.setFilterExtensions(filterExt);
  	          String selected = fd.open();
  	         // System.out.println("nghia "+selected);
  	          if(selected != null){
  		          GC gc = new GC(chart);
  		    	  Rectangle bounds = chart.getBounds();
  		    	  Image image = new Image(chart.getDisplay(), bounds);
  		    	  try {
  		    	      gc.copyArea(image, 0, 0);
  		    	      ImageLoader imageLoader = new ImageLoader();
  		    	      imageLoader.data = new ImageData[]{ image.getImageData() };
  		    	      imageLoader.save(selected, SWT.IMAGE_PNG);
  		    	  } finally {
  		    	      image.dispose();
  		    	      gc.dispose();
  		    	  }
  	          }
  	      }
  	    }); 
	  }
  /**
   * Sets the state of the layout.
   */
  void setLayoutState() {
    
  }
}
