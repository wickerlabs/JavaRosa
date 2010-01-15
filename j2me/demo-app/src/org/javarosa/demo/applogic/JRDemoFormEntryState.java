package org.javarosa.demo.applogic;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.formmanager.api.CompletedFormOptionsState;
import org.javarosa.formmanager.api.FormEntryState;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.formmanager.view.singlequestionscreen.SingleQuestionScreenManager;

public class JRDemoFormEntryState extends FormEntryState {

	protected int formID;
	protected int instanceID;

	boolean cameFromFormList;
	
	public JRDemoFormEntryState (int formID) {
		init(formID, -1, true);
	}

	public JRDemoFormEntryState (int formID, int instanceID) {
		init(formID, instanceID, false);
	}

	private void init (int formID, int instanceID, boolean cameFromFormList) {
		this.formID = formID;
		this.instanceID = instanceID;
		this.cameFromFormList = cameFromFormList;
	}
	
	protected JrFormEntryController getController() {

		Vector<IPreloadHandler> preloaders = JRDemoContext._().getPreloaders();
		FormDefFetcher fetcher = new FormDefFetcher(new RMSRetreivalMethod(formID), preloaders);
		FormDef form = fetcher.getFormDef();
		
		JrFormEntryController controller =  new JrFormEntryController(new FormEntryModel(form));
		//TODO: Use Chatterbox/OQPS depending on the correct property.
		controller.setView(new SingleQuestionScreenManager(controller));
		controller.setView(new Chatterbox("Chatterbox",controller));
		return controller;
	}

	public void abort() {
		JRDemoUtil.goToList(cameFromFormList);
	}

	public void formEntrySaved(FormDef form, DataModelTree instanceData, boolean formWasCompleted) {
		if (formWasCompleted) {
			
			CompletedFormOptionsState completed = new CompletedFormOptionsState(instanceData) {

				public void sendData(DataModelTree data) {
					JRDemoFormTransportState send;
					try {
						send = new JRDemoFormTransportState(data) {

							public void done() {
								JRDemoUtil.goToList(cameFromFormList);
							}

							public void sendToBackground() {
								JRDemoUtil.goToList(cameFromFormList);
							}
							
						};
					} catch (IOException e) {
						throw new RuntimeException("Unable to serialize XML Payload!");
					}
					send.start();
				}

				public void sendToFreshLocation(DataModelTree data) {
					throw new RuntimeException("Sending to non-default location disabled");
				}

				public void skipSend(DataModelTree data) {
					abort();
				}
			};
			completed.start();
		} else {
			abort();
		}
	}

	public void suspendForMediaCapture(int captureType) {
		throw new RuntimeException("not supported yet!!");
	}

}
