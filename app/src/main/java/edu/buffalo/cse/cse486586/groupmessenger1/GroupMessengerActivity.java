
package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;
import android.net.Uri;
import android.content.ContentValues;
import java.net.InetAddress;
import java.net.ServerSocket;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String[] REMOTE_PORTS = {"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    static int seqNo=0;
    private Uri providerUri=Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*og
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */


        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }


        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button send = (Button) findViewById(R.id.button4);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg); // This is one way to display a string.
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);

                remoteTextView.append("\n");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);


            }
        });




    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void>{
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */


            try {
                String clientMsg;
                while(true){
                    //Need to create a socket to accept the connection from client

                    Socket clientSocket = serverSocket.accept();
                    InputStream is = clientSocket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    //  PrintStream ps = new PrintStream(clientSocket.getOutputStream());

                    clientMsg = br.readLine();
                    if(clientMsg !=null){
                        //     ps.print(clientMsg);
                      //  publishProgress(clientMsg);
                        publishProgress(clientMsg.trim());
                    }
                      br.close();
                      clientSocket.close();

                }

            }
            catch (IOException e) {
                //  e.printStackTrace();
                Log.e(TAG, "ServerTask socket IOException");
            }


            return null;

        }


        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();

            ContentValues keyValueToInsert = new ContentValues () ;

            keyValueToInsert.put ("key", Integer.toString(seqNo++)) ;
            keyValueToInsert.put ("value", strReceived);
            getContentResolver().insert(providerUri,keyValueToInsert);


            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");

            return;
        }

    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                PrintStream ps;
                String msgToSend = msgs[0];
                int length = REMOTE_PORTS.length;
                for(int i =0;i<length;i++) {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORTS[i]));


                /*
                 * TODO: Fill in your client code that sends out a message.
                 */

                    ps = new PrintStream(socket.getOutputStream());
                    ps.print(msgToSend);
                    socket.close();
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}