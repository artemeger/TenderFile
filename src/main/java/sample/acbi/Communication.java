/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sample.acbi;

import com.github.jtendermint.websocket.Websocket;
import com.github.jtendermint.websocket.WebsocketException;
import com.github.jtendermint.websocket.WebsocketStatus;
import com.github.jtendermint.websocket.jsonrpc.*;
import com.github.jtendermint.websocket.jsonrpc.calls.MixedParam;
import com.github.jtendermint.websocket.jsonrpc.calls.StringParam;
import com.google.gson.Gson;
import sample.classes.IPFSFile;
import sample.crypto.CryptoUtil;
import sample.interfaces.IShareFile;

import java.security.PublicKey;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class Communication implements WebsocketStatus, IShareFile{

    private Websocket webSocket;
    private static String DESTINATION = "ws://192.168.178.32:46657/websocket";
    private Gson gson = new Gson();


    public Communication(){
        webSocket = new Websocket(DESTINATION,this);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.schedule(this::reconnect, 5, TimeUnit.SECONDS);
    }

    private void reconnect() {
        try {
            webSocket.connect();
            System.out.println("Websocket connected");
        } catch (WebsocketException e) {
            System.out.println("Websocket connenction failed");
            e.printStackTrace();
        }
    }

    @Override
    public void shareFile(IPFSFile file) {
        JSONRPC rpc = new StringParam(Method.BROADCAST_TX_COMMIT ,gson.toJson(file).getBytes());
        webSocket.sendMessage(rpc, result -> {

                if (result.hasResult()) {
                    System.out.println(result.getResult());
                } else if (result.hasError()) {
                    System.out.println(result.getError());
                }
                System.out.println();
        });
    }

    public boolean connected(){
        return webSocket.isOpen();
    }

    public void registerUser(PublicKey pub){

        JSONRPC rpc = new StringParam(Method.BROADCAST_TX_COMMIT, CryptoUtil.publicKeyToString(pub));
        webSocket.sendMessage(rpc, result ->{

            if (result.hasResult()) {
                System.out.println(result.getResult().toString());
            } else if (result.hasError()) {
                System.out.println(result.getError().toString());
            }
        });

    }
}
