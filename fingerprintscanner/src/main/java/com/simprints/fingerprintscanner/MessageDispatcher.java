package com.simprints.fingerprintscanner;

import android.os.Handler;
import android.os.Looper;

import com.simprints.fingerprintscanner.enums.MESSAGE_STATUS;
import com.simprints.fingerprintscanner.enums.MESSAGE_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;

class MessageDispatcher extends Thread {

    private class PendingRequest {
        final Object lock;
        Message answer;

        PendingRequest() {
            lock = new Object();
        }
    }

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Map<MESSAGE_TYPE, Set<PendingRequest>> pendingRequests;
    private final Set<ButtonListener> buttonListeners;

    MessageDispatcher(@NonNull InputStream inputStream, @NonNull OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.pendingRequests = new HashMap<>();
        for (MESSAGE_TYPE type : MESSAGE_TYPE.values())
            this.pendingRequests.put(type, new HashSet<PendingRequest>());
        this.buttonListeners = new HashSet<>();
    }

    public void run() {
        Message msg;
        while (!Thread.interrupted()) {
            // Wait until the scanner sends a message
            try {
                msg = Message.blockingReceiveFrom(MessageDispatcher.this.inputStream, true);
            } catch (IOException ioException) {
                // That is a disconnection
                break;
            }

            // If the message is a button press, notify all the registered listeners
            if (msg.getMessageType() == MESSAGE_TYPE.REPORT_UI) {
                synchronized (buttonListeners) {
                    for (final ButtonListener listener : buttonListeners) {
                        // Call back on UI thread
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onClick();
                            }
                        });
                    }
                }
            }

            // Notify all the threads subscribed to the type of the message received
            synchronized (pendingRequests) {
                Set<PendingRequest> concernedRequests = pendingRequests.get(msg.getMessageType());
                pendingRequests.put(msg.getMessageType(), new HashSet<PendingRequest>());
                for (PendingRequest concernedRequest : concernedRequests) {
                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (concernedRequest.lock) {
                        concernedRequest.answer = msg;
                        concernedRequest.lock.notify();
                    }
                }
            }
        }
    }


    Message sendRecv(@NonNull Message request, long timeOutMs) throws BrokenConnectionException {
        return sendRecv(request, request.getMessageType(), timeOutMs);
    }

    private Message sendRecv(@NonNull Message request, @NonNull MESSAGE_TYPE answerType, long timeOutMs) throws BrokenConnectionException {
        PendingRequest pendingRequest = new PendingRequest();
        pendingRequest.answer = Message.timeout();

        synchronized (pendingRequests) {
            pendingRequests.get(answerType).add(pendingRequest);
        }

        synchronized (pendingRequest.lock) {
            // Send the request
            try {
                request.sendTo(outputStream);
            } catch (IOException ioException) {
                throw new BrokenConnectionException("The connection with the scanner is broken.");
            }

            // Wait for a response or timeout
            try {
                pendingRequest.lock.wait(timeOutMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

            if (pendingRequest.answer.getMessageType() == MESSAGE_TYPE.NONE)
                throw new BrokenConnectionException("The connection with the scanner is broken");

            return pendingRequest.answer;
        }
    }

    MESSAGE_STATUS sendNoRevc(@NonNull Message request, long timeOutMs) throws BrokenConnectionException {
        return sendNoRevc(request, request.getMessageType(), timeOutMs);
    }

    private MESSAGE_STATUS sendNoRevc(@NonNull Message request, @NonNull MESSAGE_TYPE answerType, long timeOutMs) throws BrokenConnectionException {
        PendingRequest pendingRequest = new PendingRequest();
        pendingRequest.answer = Message.timeout();

        synchronized (pendingRequests) {
            pendingRequests.get(answerType).add(pendingRequest);
        }

        synchronized (pendingRequest.lock) {
            // Send the request
            try {
                request.sendTo(outputStream);
            } catch (IOException ioException) {
                throw new BrokenConnectionException("The connection with the scanner is broken.");
            }

            // Wait for a response or timeout
            try {
                pendingRequest.lock.wait(timeOutMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return MESSAGE_STATUS.ERROR;
            }

            if (pendingRequest.answer.getMessageType() == MESSAGE_TYPE.NONE)
                return MESSAGE_STATUS.ERROR;

            return MESSAGE_STATUS.GOOD;
        }
    }

    void registerButtonListener(@NonNull ButtonListener listener) {
        synchronized (buttonListeners) {
            buttonListeners.add(listener);
        }
    }

    void unregisterButtonListener(@NonNull ButtonListener listener) {
        synchronized (buttonListeners) {
            buttonListeners.remove(listener);
        }
    }
}
