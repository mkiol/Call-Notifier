import java.util.Locale;
import java.util.ResourceBundle;

import org.gnome.gtk.*;
import org.gnome.notify.*;

import org.mkosciesza.capclient.*;

public class CallNotifier implements CapListener {

	private CapClient _client;
	private ResourceBundle _messages;

	public static void main(String[] args) {

		Gtk.init(args);
		Notify.init("CallNotifier");

		if (args.length == 4) {

			new CallNotifier(args[0], Integer.parseInt(args[1]), args[2],
					args[3]);

		} else {

			System.out.println("Wrong number of parameters! "
					+ "Enter: <host> <port> <userid> <password>");

		}

	}

	public CallNotifier(String host, int port, String userid, String password) {

		Locale locale = Locale.getDefault();
		_messages = ResourceBundle.getBundle("messages", locale);

		_client = new CapClient();
		_client.addCallUpdateListener(this);
		_client.addConnectedListener(this);
		_client.addConnectionFailureListener(this);
		_client.addDisconnectedListener(this);

		System.out.println("Connecting to CAP server...");

		_client.connect(host, port, userid, password);

	}

	public static void sendInfoNotification(String title, String text) {

		sendNotification(title, text, "dialog-information");

	}

	public static void sendErrorNotification(String title, String text) {

		sendNotification(title, text, "dialog-error");

	}

	public static void sendWarningNotification(String title, String text) {

		sendNotification(title, text, "dialog-warning");

	}

	public static void sendNotification(String title, String text, String icon) {

		Notification n = new Notification(title, text, icon);
		n.show();

	}

	@Override
	public void connectedHandler(ConnectedEvent event) {

		System.out.println("Connection was successfully established!");
		sendInfoNotification(_messages.getString("callnotifier"),
				_messages.getString("connected"));

	}

	@Override
	public void connectionFailureHandler(ConnectionFailureEvent event) {

		switch (event.causeCode) {
		case 1:
			System.out
					.println("Cannot establish a connection, network might be blocked!");
			sendErrorNotification(_messages.getString("callnotifier"),
					_messages.getString("failurenetwork"));
			break;
		case 2:
			System.out
					.println("Cannot establish a connection, userID/password might be incorrect!");
			sendErrorNotification(_messages.getString("callnotifier"),
					_messages.getString("wrongpassword"));
			break;
		}

		System.exit(0);

	}

	@Override
	public void disconnectedHandler(DisconnectedEvent event) {

		System.out.println("Disconnected from CAP server!");
		sendInfoNotification(_messages.getString("callnotifier"),
				_messages.getString("disconnected"));
		System.exit(0);

	}

	@Override
	public void callUpdateHandler(CallUpdateEvent event) {

		if (event.personality == 2 && event.state == 1)
			sendInfoNotification(_messages.getString("incomingcall"),
					_messages.getString("from") + ": " + event.remoteNumber + "");

	}

}
