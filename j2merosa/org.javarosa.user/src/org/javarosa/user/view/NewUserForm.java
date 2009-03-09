package org.javarosa.user.view;

import java.io.IOException;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.user.model.Constants;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;
import org.javarosa.user.utility.IUserDecorator;



/**
 * @author Julian
 *
 */
public class NewUserForm extends Form implements IView{

	private TextField userName;
	private TextField password;
	private TextField confirmPassword;
	
	private TextField[] metaFields;
	
	private IUserDecorator decorator;
	
	//#if javarosa.adduser.extended
	private TextField userID;
	//#endif
	
	private UserRMSUtility userRMS;
	private ChoiceGroup choice = new ChoiceGroup("",Choice.MULTIPLE);
	
	private User constructedUser;
	
	private int editingId = -1;

	public NewUserForm(String title, IUserDecorator d) {
		super(title);
		userName = new TextField("Name (ie: loginID):", "", 10, TextField.ANY);
	    password = new TextField("User Password:", "", 10, TextField.PASSWORD);
	    confirmPassword = new TextField("Confirm Password:", "", 10, TextField.PASSWORD);
	    //#if javarosa.adduser.extended
	    userID = new TextField("User ID:", "", 10, TextField.NUMERIC);
	    //#endif
	    choice.append("Give this user admin rights?", null);
	   

	    this.append(userName);
	    this.append(password);
	    this.append(confirmPassword);
	    //#if javarosa.adduser.extended
	    this.append(userID);
	    //#endif

	    userRMS = (UserRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(UserRMSUtility.getUtilityName());
	    this.decorator = d;
	    if(d!= null) {
	    	initMeta();
	    }
	    this.append(choice);
	}
	
	public NewUserForm(String title)
	{
		this(title, null);
	}
	
	public void loadUser(User user) {
		userName.setString(user.getUsername());
		password.setString(user.getPassword());
		
		
		//#if javarosa.adduser.extended
		userID.setString(String.valueOf(user.getUserID()));
		//#endif
		
		if(user.isAdminUser()) {
			choice.setSelectedIndex(0, true);
		}
		
		if (decorator != null) {
			String[] elements = decorator.getPertinentProperties();
			for (int i = 0; i < elements.length; ++i) {
				 metaFields[i].setString(user.getProperty(elements[i]));
			}
		}
		editingId = user.getRecordId();
		System.out.println("Editing ID: " + editingId);
	}

	public String readyToSave()
	{

		System.out.println("reached this far");
		boolean nameAlreadyTaken = checkNameExistsAlready();
		if (nameAlreadyTaken == true && editingId == -1)
		{
			System.out.println("username taken love");
			return "Username ("+userName.getString()+") already taken. Please choose another username.";
		}
		else if ((userName.getString().equalsIgnoreCase("")) || (password.getString().equals("")))
		{
			System.out.println("fail");
			return "Please fill in both username and password.";
		}
		else if (!(password.getString().equals(confirmPassword.getString())))
		{
			System.out.println("passwords don't match...");
			return "Please re-enter your password, the password and password confirmation box did not match.";
		}
		else
		{
			System.out.println("ready returned as true");
			
			constructedUser = constructUser(choice.isSelected(0));
			if(editingId == -1) {
				userRMS.writeToRMS(constructedUser);
			} else {
				userRMS.updateToRMS(editingId, constructedUser, null);
			}
			
			System.out.println("added user "+ userName.getString() + " passw: "+password.getString()+" = "+confirmPassword.getString() );
			return "";
		}
	}

	
	private User constructUser(boolean hackForAdmin) {
		int userid = -1;
		//#if javarosa.adduser.extended
		userid = Integer.parseInt(userID.getString());
		//#endif
		
		User user;
		
		if (choice.isSelected(0) == false)
			user = new User(userName.getString(), password.getString(), userid);
		else
			user = new User(userName.getString(), password.getString(), userid,
					Constants.ADMINUSER);

		if (decorator != null) {
			String[] elements = decorator.getPertinentProperties();
			for (int i = 0; i < elements.length; ++i) {
				user.setProperty(elements[i], metaFields[i].getString());
			}
		}
		return user;
	}



	private boolean checkNameExistsAlready()
	{
		///find user in RMS:
		   User discoveredUser = new User();
		   String usernameStr = userName.getString();
		   int index = 1;

		   while (index <= userRMS.getNumberOfRecords() )
		   {
			   try
			   {
				   try {
					userRMS.retrieveFromRMS(index, discoveredUser);
				} catch (DeserializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   }
			   catch (IOException ioe) {
				   System.out.println(ioe);
			   }
			   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
				   break;

			   index++;
		   }

		   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
		   {
			   System.out.println("name already taken valid");
			   return true;
		   }

		   else return false;
	}
	
	public void initMeta() {
		String[] elements = decorator.getPertinentProperties();
		metaFields = new TextField[elements.length];
		for(int i = 0 ; i < elements.length ; ++i) {
			metaFields[i] = new TextField(decorator.getHumanName(elements[i]), "", 100, TextField.ANY);
			this.append(metaFields[i]);
		}
	}
	
	public User getConstructedUser() {
		return constructedUser;
	}
	
	public Object getScreenObject() {
		return this;
	}
}
