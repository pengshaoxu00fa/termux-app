package com.termux.web.terminal;

import android.content.Context;

import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

public class WebServer {
    private static WebServer instance;
    private AsyncHttpServer httpServer;
    public static WebServer getInstance() {
        if (instance == null) {
            instance = new WebServer();
        }
        return instance;
    }


    public void startup(Context context) {
        if (httpServer != null) {
            return;
        }
        httpServer = new AsyncHttpServer();
        httpServer.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                //response.sendFile();
            }
        });
        httpServer.websocket("/sync", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(WebSocket webSocket, AsyncHttpServerRequest request) {

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
