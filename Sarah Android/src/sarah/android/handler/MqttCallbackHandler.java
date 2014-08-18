package sarah.android.handler;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import sarah.android.R;
import sarah.android.R.string;
import sarah.android.model.Connection;
import sarah.android.model.Connections;
import sarah.android.model.Notify;
import sarah.android.model.Connection.ConnectionStatus;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

public class MqttCallbackHandler implements MqttCallback
{

	/**
	 * {@link Context} for the application used to format and import external
	 * strings
	 **/
	private Context context;
	/**
	 * Client handle to reference the connection that this handler is attached
	 * to
	 **/
	private String clientHandle;

	/**
	 * Creates an <code>MqttCallbackHandler</code> object
	 * 
	 * @param context
	 *            The application's context
	 * @param clientHandle
	 *            The handle to a {@link Connection} object
	 */
	public MqttCallbackHandler(Context context, String clientHandle)
	{
		this.context = context;
		this.clientHandle = clientHandle;
	}

	/**
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
	 */
	@Override
	public void connectionLost(Throwable cause)
	{
		// cause.printStackTrace();
		if (cause != null)
		{
			Connection c = Connections.getInstance(context).getConnection(clientHandle);
			c.addAction("Connection Lost");
			c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

			// format string to use a notification text
			Object[] args = new Object[2];
			args[0] = c.getId();
			args[1] = c.getHostName();

			String message = context.getString(R.string.connection_lost, args);

			// build intent
			Intent intent = new Intent();
			intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
			intent.putExtra("handle", clientHandle);

			// notify the user
			Notify.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);
		}
	}

	/**
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String,
	 *      org.eclipse.paho.client.mqttv3.MqttMessage)
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception
	{

		// Get connection object associated with this object
		Connection c = Connections.getInstance(context).getConnection(clientHandle);

		// create arguments to format message arrived notifcation string
		String[] args = new String[2];
		args[0] = new String(message.getPayload());
		args[1] = topic;

		// get the string from strings.xml and format
		String messageString = context.getString(R.string.messageRecieved, (Object[]) args);

		// create intent to start activity
		Intent intent = new Intent();
		intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
		intent.putExtra("handle", clientHandle);

		// format string args
		Object[] notifyArgs = new String[3];
		notifyArgs[0] = c.getId();
		notifyArgs[1] = new String(message.getPayload());
		notifyArgs[2] = topic;

		// notify the user
		Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);

		// update client history
		c.addAction(messageString);

		if (messageString.equals("Received message get <br/> <small>Topic: /sara/url/ </small>"))
		{
			Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(1500);
		}
	}

	/**
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		// Do nothing
	}

}