package com.termux.web.terminal;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;

import java.io.IOException;
import java.io.InputStream;

public class WebServer {
    private static WebServer instance;
    private Context context;
    private AsyncHttpServer httpServer;

    private TerminalSession session;

    public static WebServer getInstance() {
        if (instance == null) {
            instance = new WebServer();
        }
        return instance;
    }

    public void setSession(TerminalSession session) {
        this.session = session;
    }

    private byte[] createFile(String assets) {
        InputStream in = null;
        try {
            in = context.getResources().getAssets().open(assets);
            int length = in.available();
            byte[] result = new byte[length];
            int readLen;
            int hasReadLen = 0;
            while ((readLen = in.read(result, hasReadLen, length - hasReadLen)) > 0) {
                hasReadLen += readLen;
            }
            return result;
        } catch (Exception e){
            return "error!".getBytes();
        } finally {
            try {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e){
                    }
                }
            } catch (Exception e){
            }
        }
    }
    public void startup(Context context) {
        this.context = context;
        if (httpServer != null) {
            return;
        }
        httpServer = new AsyncHttpServer();
        httpServer.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.getHeaders().add("Cache-Control","no-cache");
                response.getHeaders().add("Pragma","no-cache");
                response.getHeaders().add("Expires","0");
                response.send("text/html;charset=utf-8", createFile("index.html"));
                response.end();
            }
        });

        httpServer.get("/jq", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send("application/javascript; charset=utf-8", createFile("jquery.js"));
                response.end();
            }
        });
        httpServer.get("/host", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send("ws://10.10.90.97:8899/sync");
                response.end();
            }
        });
        httpServer.get("/favicon.ico", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send("image/x-icon", createFile("ic_launcher.png"));
                response.end();
            }
        });
        httpServer.websocket("/sync", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        Log.d("xsp-web", s + " session=" + session.toString());
                        if (session != null) {
                            session.write("abcdef\r");
                        }

                    }
                });
                webSocket.send("abc");
            }
        });
        httpServer.setErrorCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                //失败
                httpServer = null;
            }
        });
        AsyncServerSocket socket = httpServer.listen(8899);
        if (socket == null) {
            httpServer.stop();
            httpServer = null;
            //失败
        }
    }

    public void shutdown() {
        if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        }
    }
}
