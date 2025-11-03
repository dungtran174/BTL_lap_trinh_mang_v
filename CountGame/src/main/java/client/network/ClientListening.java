package client.network;

import client.controller.ClientCtr;
import client.view.ImageQuizFrm;
import shared.dto.ObjectWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ClientListening extends Thread {

    private volatile boolean isListening = true;
    private ClientCtr clientCtr;

    private ObjectInputStream ois;

    public ClientListening(ClientCtr clientCtr) throws IOException {
        this.clientCtr = ClientCtr.getInstance();
        this.ois = new ObjectInputStream(clientCtr.getMySocket().getInputStream());
    }

    @Override
    public void run() {
        try {
            while (isListening) {
                Object obj = ois.readObject();
                if (obj instanceof ObjectWrapper) {
                    System.out.println(obj);
                    ObjectWrapper data = (ObjectWrapper) obj;
                    if (data.getPerformative() == ObjectWrapper.SERVER_INFORM_CLIENT_NUMBER) {
                        clientCtr.getConnectFrm().showMessage("Number of client connecting to the server: " + data.getData());
                    } else {
                        switch(data.getPerformative()){
                            case ObjectWrapper.SERVER_REGISTER_USER:
                                clientCtr.getRegisterFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.SERVER_LOGIN_USER:
                                clientCtr.getLoginFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.SERVER_INFORM_CLIENT_WAITING:
                                if (clientCtr.getMainFrm() != null) {
                                    clientCtr.getMainFrm().receivedDataProcessing(data);
                                }
                                break;
                            case ObjectWrapper.SERVER_SEND_HISTORY:
                                clientCtr.getMainFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.SERVER_SEND_RANKING:
                                clientCtr.getMainFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.RECEIVE_PLAY_REQUEST:
                                clientCtr.getMainFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.SERVER_SET_GAME_READY:
                                clientCtr.getMainFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.SERVER_SEND_RESULT:
                                clientCtr.getResultFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.SERVER_SEND_ALL_USER:
                                clientCtr.getMainFrm().receivedDataProcessing(data);
                                break;
                            case ObjectWrapper.SERVER_SEND_ROUND_DATA:
                                System.out.println("Client: Received SERVER_SEND_ROUND_DATA");
                                // Always create new ImageQuizFrm if null or if we're on result screen (play again scenario)
                                if (clientCtr.getImageQuizFrm() == null || clientCtr.getResultFrm() != null) {
                                    System.out.println("Client: Creating new ImageQuizFrm for round data");
                                    ImageQuizFrm imageQuizFrm = new ImageQuizFrm();
                                    clientCtr.setImageQuizFrm(imageQuizFrm);
                                    imageQuizFrm.openScene();
                                    // Wait a bit for scene to be ready
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    // Clear result form reference since we're entering game
                                    clientCtr.setResultFrm(null);
                                }
                                if (clientCtr.getImageQuizFrm() != null) {
                                    clientCtr.getImageQuizFrm().receivedDataProcessing(data);
                                } else {
                                    System.err.println("Client: ImageQuizFrm is still null after creation attempt");
                                }
                                break;
                            case ObjectWrapper.SERVER_SEND_ROUND_RESULT:
                                if (clientCtr.getImageQuizFrm() != null) {
                                    clientCtr.getImageQuizFrm().receivedDataProcessing(data);
                                }
                                break;
                            case ObjectWrapper.SERVER_END_IMAGE_QUIZ_GAME:
                                if (clientCtr.getImageQuizFrm() != null) {
                                    clientCtr.getImageQuizFrm().receivedDataProcessing(data);
                                }
                                break;
                            case ObjectWrapper.SERVER_ASK_PLAY_AGAIN:
                            case ObjectWrapper.SERVER_START_NEW_GAME:
                            case ObjectWrapper.SERVER_PLAY_AGAIN_DECLINED:
                                if (clientCtr.getResultFrm() != null) {
                                    clientCtr.getResultFrm().receivedDataProcessing(data);
                                }
                                break;
                            case ObjectWrapper.SERVER_PLAYER_LEFT_GAME:
                                if (clientCtr.getImageQuizFrm() != null) {
                                    clientCtr.getImageQuizFrm().receivedDataProcessing(data);
                                }
                                break;
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (isListening) {
                clientCtr.getConnectFrm().showMessage("Connection to server lost!");
            }
        } catch (ClassNotFoundException e) {
            clientCtr.getConnectFrm().showMessage("Data received in unknown format!");
        } finally {
//            clientCtr.closeConnection();
        }
    }

    public void stopListening() {
        isListening = false;
        this.interrupt();  // Interrupt the thread if it's blocked on I/O
    }
}
