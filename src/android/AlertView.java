package com.citizen.app.assist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertView
{	
	public static void showError(String message, Context ctx)
	{
		showAlert("Error", message, ctx);
	}
	
	public static void showAlert(String message, Context ctx)
	{
		showAlert("Alert", message, ctx);
	}
	
	public static void showAlert(String title, String message, Context ctx)
	{
		//Create a builder	
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		builder.setMessage(message);
		//add buttons and listener
		EmptyListener pl = new EmptyListener();
		builder.setPositiveButton("OK", pl);
		//Create the dialog
		AlertDialog ad = builder.create();
		//show
		ad.show();	
	}
}

class EmptyListener implements android.content.DialogInterface.OnClickListener 
{
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
	}	
}
