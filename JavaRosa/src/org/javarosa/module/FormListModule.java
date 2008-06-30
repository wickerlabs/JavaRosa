/**
 * 
 */
package org.javarosa.module;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.clforms.storage.XFormMetaData;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.core.JavaRosaPlatform;
import org.javarosa.shell.IShell;
import org.javarosa.view.Commands;
import org.javarosa.view.FormList;
import org.javarosa.view.ReturnValue;
import org.javarosa.view.ViewTypes;

/**
 * @author Brian DeRenzi
 *
 */
public class FormListModule implements IModule {
	private FormList formsList = null;
	private Hashtable listOfForms = null;
	private Vector formIDs = null;
	private IShell parent = null;
	
	public FormListModule(IShell p, String title) {
		this.parent = p;
		this.formsList = new FormList(this,title);
	}
	
	public void start() {
		this.listOfForms = new Hashtable();
		this.formIDs = new Vector();
		getXForms();
		this.formsList.loadView(listOfForms);
		JavaRosaPlatform.showView(this.formsList);
	}
	
	
	public void viewCompleted(ReturnValue rv, int view_ID) {
		// Determine which view just completed and act accordingly
		switch(view_ID) {
		case ViewTypes.FORM_LIST:
			processFormsList(rv);
			break;
		}
	}
	
	private void processFormsList(ReturnValue rv) {
		switch(rv.command) {
		case Commands.CMD_SELECT_XFORM:
			//LOG
			System.out.println("Selected form: " + formIDs.elementAt( ((Integer)(rv.values.get(new Integer(0)))).intValue() ));
			
			// 
			break;
		}
	}
	
	private void getXForms() {
		XFormRMSUtility xformRMSUtility = JavaRosaPlatform.getXFormRMS();
		xformRMSUtility.open();
    	RecordEnumeration recordEnum = xformRMSUtility.enumerateMetaData();
    	int pos =0;
    	while(recordEnum.hasNextElement())
    	{
    		int i;
			try {
				i = recordEnum.nextRecordId();
				XFormMetaData mdata = new XFormMetaData();
				xformRMSUtility.retrieveMetaDataFromRMS(i,mdata);
				// TODO fix it so that record id is part of the metadata serialization
				//LOG
				System.out.println(mdata.toString());
				//mdata.setRecordId(i);
				listOfForms.put(new Integer(pos), mdata.getRecordId()+"-"+mdata.getName());
				formIDs.insertElementAt(mdata, pos);
				pos++;
				System.out.println("METADATA: "+mdata.toString());
			} catch (InvalidRecordIDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	//LOG
    	System.out.println("Done getting XForms");
    }
}
