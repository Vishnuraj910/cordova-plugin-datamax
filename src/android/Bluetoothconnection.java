
package org.apache.cordova.bluetooth;


import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.app.ProgressDialog;

import android.R;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;
import java.util.Set;
import java.util.UUID;
import android.content.res.AssetManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.util.Xml.Encoding;
import android.util.Base64;
import java.util.ArrayList;
import java.util.List;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import android.graphics.Typeface;
import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.connection.Connection_TCP;
import honeywell.printer.DocumentDPL;
import honeywell.printer.DocumentDPL.*;
import honeywell.printer.DocumentEZ;
import honeywell.printer.DocumentLP;
import honeywell.printer.DocumentExPCL_LP;
import honeywell.printer.DocumentExPCL_PP;
import honeywell.printer.DocumentExPCL_PP.*;
import honeywell.printer.ParametersDPL;
import honeywell.printer.ParametersDPL.*;
import honeywell.printer.ParametersEZ;
import honeywell.printer.ParametersExPCL_LP;
import honeywell.printer.ParametersExPCL_LP.*;
import honeywell.printer.ParametersExPCL_PP;
import honeywell.printer.ParametersExPCL_PP.*;
import honeywell.printer.UPSMessage;
import honeywell.printer.configuration.dpl.*;
import honeywell.printer.configuration.ez.*;
import honeywell.printer.configuration.expcl.*;

public class Bluetoothconnection extends CordovaPlugin {

	private static final String LIST = "list";
	private static final String CONNECT = "connect";
	private static final int REQUEST_ENABLE_BT = 2;
	private static final String LOG_TAG = "BluetoothPrinter";
	private static final String TAG = "Bluetoothconnection";
	public static final int LENGTH_SHORT = 0;

	private BluetoothAdapter mBluetoothAdapter;
	private Vector<BluetoothDevice> remoteDevices;
	private BroadcastReceiver searchFinish;
	private BroadcastReceiver searchStart;
	private BroadcastReceiver discoveryResult;
	private Thread hThread;
	private connTask connectionTask;
	private String lastConnAddr;
	byte FONT_TYPE;
	private static BluetoothSocket btsocket;
	private static OutputStream btoutputstream;
	private  BluetoothDevice mmDevice;

	Bitmap bitmap;
	byte[] printData = {0};
	private DocumentExPCL_LP docExPCL_LP;
	private ConnectThread mConnectThread;
	ConnectionBase conn = null;

	private  BluetoothSocket mmSocket;
	String macAddress;
	String devicename;

	public Bluetoothconnection()
	{
		docExPCL_LP = new DocumentExPCL_LP(3);
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {


		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			bluetoothSetup(callbackContext);
		}

		if (action.equals("printWithText")) {
			String errMsg = null;
			boolean secure = true;
			if(listBondedDevices(callbackContext)) //getting paired device
			{
				try
				{
					if(connect(callbackContext)) //connecting to the paired device
					{
						try
						{
							if(mmSocket != null) {
								Log.e(LOG_TAG,"Getting to printing function");
								printText(args, callbackContext); //Taking the prin of the text
							}

						}
						catch(Exception e)
						{
							// Bluetooth Address Format [OO:OO:OO:OO:OO:OO]
							errMsg = e.getMessage();
							Log.e(LOG_TAG, errMsg);
							e.printStackTrace();
							callbackContext.error("Error message" + errMsg);
						}

					}
					else
					{
						callbackContext.error("Could not connect to " + devicename);
						return true;
					}
				}
				catch (Exception e) {
					errMsg = e.getMessage();
					Log.e(LOG_TAG, errMsg);
					e.printStackTrace();
					callbackContext.error(errMsg);
				}
			}
			else
			{
				callbackContext.error("No Bluetooth Device Found");
				return true;
			}
			return true;
		}
		else if (action.equals("printWithLogo")) {
			String errMsg = null;
			boolean secure = true;
			if(listBondedDevices(callbackContext)) //getting paired device
			{
				try
				{
					if(connect(callbackContext)) //connecting to the paired device
					{
						try
						{
							if(mmSocket != null) {
								Log.e(LOG_TAG,"Getting to printing function");
								printWithLogo(args, callbackContext); //Taking the prin of the text
							}

						}
						catch(Exception e)
						{
							// Bluetooth Address Format [OO:OO:OO:OO:OO:OO]
							errMsg = e.getMessage();
							Log.e(LOG_TAG, errMsg);
							e.printStackTrace();
							callbackContext.error("Error message" + errMsg);
						}

					}
					else
					{
						callbackContext.error("Could not connect to " + devicename);
						return true;
					}
				}
				catch (Exception e) {
					errMsg = e.getMessage();
					Log.e(LOG_TAG, errMsg);
					e.printStackTrace();
					callbackContext.error(errMsg);
				}
			}
			else
			{
				callbackContext.error("No Bluetooth Device Found");
				return true;
			}
			return true;

		}
		return false;
	}

	public void bluetoothSetup(CallbackContext callbackContext)
	{
		String errMsg = null;
		try
		{
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null)
			{
				errMsg = "No bluetooth adapter available";
				Log.e(LOG_TAG, errMsg);
				callbackContext.error(errMsg);
				return;
			}
			if (!mBluetoothAdapter.isEnabled())
			{
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.cordova.getActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

			}
		}
		catch (Exception e) {
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
	}
	boolean listBondedDevices(CallbackContext callbackContext)
	{
		String errMsg = null;
		try
		{
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				JSONArray json = new JSONArray();
				for (BluetoothDevice device : pairedDevices) {
					json.put(device.getName() +","+device.getAddress()+" ,[Paired]");
					macAddress = device.getAddress();
					devicename = device.getName();
				}
				Log.e(LOG_TAG, "Address is " + macAddress);
				return true;
			}
		}
		catch (Exception e) {
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}
	boolean connect(CallbackContext callbackContext)
	{
		String errMsg = null;
		try
		{
			macAddress=macAddress.replace(" ", "");
			Log.e(LOG_TAG,"macaddress in connect" + macAddress );
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);

			if (device != null) {

				if((connectionTask != null) && (connectionTask.getStatus() == AsyncTask.Status.RUNNING))
				{
					connectionTask.cancel(true);
					if(!connectionTask.isCancelled())
						connectionTask.cancel(true);
					connectionTask = null;
					return false;
				}

				mConnectThread = new ConnectThread(device);
				mConnectThread.start();

				Log.e(TAG, "connect to: " + device);
				return true;
			}
		}
		catch(Exception e)
		{
			// Bluetooth Address Format [OO:OO:OO:OO:OO:OO]
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error("Error message" + errMsg);
		}

		return false;
	}


	boolean printText(JSONArray  args, CallbackContext callbackContext) {
		try {

			// TODO Auto-generated method stub
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			btoutputstream = mmSocket.getOutputStream();
			String str = args.getString(0);
			String newline = "\n";

			String msg = str.toString();

			msg += "\n";

			btoutputstream.write(msg.getBytes());
			btoutputstream.flush();

			Log.e(LOG_TAG,"Printing success");
//			mmSocket.close();
			Toast.makeText(this.cordova.getActivity(), "Successfully printed", Toast.LENGTH_LONG).show();
			callbackContext.success("Printed Successfuly : ");
			return true;

		} catch (Exception e) {
			Log.e(LOG_TAG,"Printing error" + e.getMessage());
			e.printStackTrace();
			callbackContext.error("Some error occured new " + e.getMessage());
		}

		return false;
	}

	boolean printWithLogo(JSONArray  args, CallbackContext callbackContext) {
		try {

			// TODO Auto-generated method stub

			btoutputstream = mmSocket.getOutputStream();
			printData = new byte[]{0};
			String str = args.getString(0);
			String str1 = args.getString(1);
			Context context = this.cordova.getActivity().getApplicationContext();

			Bitmap decodedBitmap = BitmapFactory.decodeStream(context.getAssets().open("www/img/img_logo.jpg"));

			docExPCL_LP.writeImage(decodedBitmap, 384);
			printData = docExPCL_LP.getDocumentData();

			btoutputstream.write(printData);

			String newline = "\n";

			String msg = str.toString();

			msg += "\n";

			btoutputstream.write(msg.getBytes());
			btoutputstream.flush();

			Log.e(LOG_TAG,"Printing success");
//            mmSocket.close();
			Toast.makeText(this.cordova.getActivity(), "Successfully printed", Toast.LENGTH_LONG).show();
			callbackContext.success("Printed Successfuly : ");

			return true;


		} catch (Exception e) {
			Log.e(LOG_TAG,"Printing error" + e.getMessage());
			e.printStackTrace();
			callbackContext.error("Some error occured new " + e.getMessage());
		}

		return false;
	}


	// Bluetooth Connection Task.
	class connTask extends AsyncTask<BluetoothDevice, Void, Integer>
	{
		private final ProgressDialog dialog = new ProgressDialog( cordova.getActivity().getApplicationContext());

		@Override
		protected void onPreExecute()
		{
			dialog.setTitle("Bluetooth");
			dialog.setMessage("Connecting");
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(BluetoothDevice... params)
		{
			Integer retVal = null;
			lastConnAddr = params[0].getAddress();
			retVal = Integer.valueOf(0);
			return retVal;
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			if(result.intValue() == 0)	// Connection success.
			{
				hThread.start();
			}
			else	// Connection failed.
			{
				if(dialog.isShowing())
					dialog.dismiss();

			}
			super.onPostExecute(result);
		}
	}
	private class ConnectThread extends Thread {

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			// MY_UUID is the app's UUID string, also used by the server code
			UUID uuid = device.getUuids()[0]
					.getUuid();
			try {
				Method m = mmDevice.getClass().getMethod("createRfcommSocket",
						new Class[] { int.class });
				mmSocket = (BluetoothSocket) m.invoke(mmDevice, Integer.valueOf(1));
				Thread.sleep(2000);
				mmSocket.connect();



			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (Exception e) {}
		}
		@Override
		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {

				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				try {
					Thread.sleep(2000);
					mmSocket.connect();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				return;
			}
			// Do work to manage the connection (in a separate thread)
		}
		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}

	}

}




