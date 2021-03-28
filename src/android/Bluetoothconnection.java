
package com.datamax.sysdxb;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.printer.DocumentExPCL_LP;

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
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {


		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			bluetoothSetup(callbackContext);
		}
		conn = null;
		docExPCL_LP = new DocumentExPCL_LP(3);
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
							if(conn != null) {
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


								Log.e(LOG_TAG,"Getting to printing function");
								cordova.getThreadPool().execute(new Runnable() {
									@Override
									public void run() {
										// printWithLogo(args, callbackContext); //Taking the prin of the text
										try {

											// TODO Auto-generated method stub
											macAddress=macAddress.replace(" ", "");
											Log.e(LOG_TAG,"macaddress in connect" + macAddress );
											// BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
											if (Looper.myLooper() == null)
											{
												Looper.prepare();
											}
											conn = Connection_Bluetooth.createClient(macAddress);
											conn.open();
											printData = new byte[]{0};
											String str = args.getString(0);
											Context context = cordova.getActivity().getApplicationContext();
											String msg = str.toString();

											msg += "\n";
											Bitmap decodedBitmap = BitmapFactory.decodeStream(context.getAssets().open("www/img/dcl_print.png"));
											docExPCL_LP.writeImage(decodedBitmap, 756);
											docExPCL_LP.setDefaultFontIndex(8);
											docExPCL_LP.writeText(msg);
											// docExPCL_LP.writeText("Message wit 8 - 7987987 HEY");
//											 docExPCL_LP.setLineSpacing((byte) 30);
//											docExPCL_LP.writeText("Message wit 8 - (byte) 30 7987987 HEY"+ docExPCL_LP.getDefaultFont());
//											docExPCL_LP.setLineSpacing((byte) 100);
//											docExPCL_LP.writeText("Message wit 8 - 100 - 7987987 HEY");

											printData = docExPCL_LP.getDocumentData();



											conn.write(printData);
											// conn.write(msg.getBytes());
											Thread.sleep(2000);
											conn.close();
											Log.e(LOG_TAG,"Printing success");
											Toast.makeText(cordova.getActivity(), "Successfully printed", Toast.LENGTH_LONG).show();
											callbackContext.success("Printed Successfuly : ");

										} catch (Exception e) {
											Log.e(LOG_TAG,"Printing error" + e.getMessage());
											e.printStackTrace();
											callbackContext.error("Some error occured new " + e.getMessage());
										}
									}
								});


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
			// BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);

			conn = Connection_Bluetooth.createClient(macAddress);
			conn.open();
			// if (device != null) {

			// 	if((connectionTask != null) && (connectionTask.getStatus() == AsyncTask.Status.RUNNING))
			// 	{
			// 		connectionTask.cancel(true);
			// 		if(!connectionTask.isCancelled())
			// 			connectionTask.cancel(true);
			// 		connectionTask = null;
			// 		return false;
			// 	}

			// 	mConnectThread = new ConnectThread(device);
			// 	mConnectThread.start();

			// 	Log.e(TAG, "connect to: " + device);
			// 	return true;
			// }

			return true;
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
			// mmSocket.close();
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




