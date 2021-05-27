package edu.illinois.cs.ase.reportrelay;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RelayService extends IntentService {

    private static int portA = -1, portB = -1;
    private static AtomicBoolean isStopped = new AtomicBoolean(false);
    private static Set<PrintWriter>
            bufferA = Collections.newSetFromMap(new ConcurrentHashMap<PrintWriter, Boolean>()),
            bufferB = Collections.newSetFromMap(new ConcurrentHashMap<PrintWriter, Boolean>());

    public RelayService() {
        super("RelayService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Log.i("ReportRelay", "I am alive!");

        String action = workIntent.getAction();
        if ("start".equals(action)) {
            if (portA >= 0 && portB >= 0) {
                Log.i("ReportRelay", "Already started");
            } else {
                portA = workIntent.getIntExtra("pA", -1);
                portB = workIntent.getIntExtra("pB", -1);
                if (portA >= 1024 && portB >= 1024 && portA != portB) {
                    isStopped.set(false);
                    new serverController(portA, bufferA, bufferB).start();
                    new serverController(portB, bufferB, bufferA).start();
                } else {
                    Log.i("ReportRelay", "Invalid port numbers.");
                    portA = portB = -1;
                }
            }
        } else if ("stop".equals(action)) {
            isStopped.set(true); // need a better way
            Log.i("ReportRelay", "Try to shut down...");
        }
    }

    private static class clientReader extends Thread {

        BufferedReader in;
        Set<PrintWriter> othersBufferPool;
        Runnable postExec;

        clientReader(BufferedReader in, Set<PrintWriter> othersBufferPool, Runnable postExec) {
            this.in = in;
            this.othersBufferPool = othersBufferPool;
            this.postExec = postExec;
        }

        @Override
        public void run() {
            int nread = -1;
            char cbuf[] = new char[102400];

            try {
                while ((nread = in.read(cbuf, 0, cbuf.length)) != -1 && !isStopped.get()) {
                    Log.i("ReportRelay", "received " + nread + " byte(s) of data");
                    for (PrintWriter out : othersBufferPool) {
                        try {
                            out.write(cbuf, 0, nread);
                            out.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.i("ReportRelay", "some client closed");
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            postExec.run();
        }

    }

    private static class serverController extends Thread {

        int myPort;
        Set<PrintWriter> myBufferPool, othersBufferPool;

        serverController(int myPort, Set<PrintWriter> myBufferPool, Set<PrintWriter> othersBufferPool) {
            this.myPort = myPort;
            this.myBufferPool = myBufferPool;
            this.othersBufferPool = othersBufferPool;
        }

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress("127.0.0.1", myPort));
                Log.i("ReportRelay", "Start listening on port " + myPort);
                while (!isStopped.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        myBufferPool.add(out);
                        // new clientHandler(clientSocket, myBuffer, myBufferPool, othersBufferPool).start();
                        new clientReader(in, othersBufferPool, new Runnable() {
                            @Override
                            public void run() {
                                myBufferPool.remove(out);
                                out.close();
                            }
                        }).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
