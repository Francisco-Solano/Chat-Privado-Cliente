package com.example.clienteproyectoservicios2ev;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class LoginController {
    @FXML
    private TextField nicknameTextField;
    @FXML
    private Label mensajeError;

    @FXML
    private Button loginBtn;

    private ChatController chatController;
    public static final String IP = "192.168.1.210"; // dirección IP del servidor.

    public static final int PUERTOCLIENTE2 = 6011; // Puerto del cliente.
    public static final int PUERTOCLIENTE = 6010; // Puerto del cliente.
    public static final int PUERTOSERVIDOR = 5010; // Puerto del servidor.
    public static final int BUFFER_SIZE = 1024; // Tamaño del buffer.
    public static DatagramSocket clientSocket;


    @FXML
    public void initialize() {
        try {
            // Inicialización del socket del cliente en el puerto especificado.
            clientSocket = new DatagramSocket(PUERTOCLIENTE);

        } catch (Exception e) {
            try {
                clientSocket = new DatagramSocket(PUERTOCLIENTE2);
            } catch (Exception e2) {
                System.out.println("Error al intentar crear el nuevo socket");
            }
        }
        // Configuración de la acción cuando se hace clic en el botón de ACCEDER.
        loginBtn.setOnAction(event -> {
            String nickname = nicknameTextField.getText(); // Obtención del nombre de usuario ingresado.
            // Verificación si el nombre de usuario esta disponible o no
            if (verificarNickname(nickname)) {
                try {
                    abrirChat(nickname); // Abre la ventana del chat si el nombre de usuario está disponible.
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                mensajeError.setText("El nombre de usuario no está disponible. Introduce otro.");
            }
        });
    }


    private boolean verificarNickname(String username) {
        try {
            // Envía el nombre de usuario al servidor para su verificación.
            String message = "CHECK_USERNAME|" + username;
            byte[] sendData = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName(IP);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PUERTOSERVIDOR);
            clientSocket.send(sendPacket);

            // Recibe la respuesta del servidor.
            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            // Pasa la respuesta a String y da true si el nombre de usuario está disponible.
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return response.equals("USERNAME_AVAILABLE");
        } catch (Exception e) {
            return false;
        }
    }


    private void abrirChat(String nickname) throws IOException {
        // Si el nickname es válido, inicializamos el ChatController y pasamos el nickname
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 720, 640);
        Stage stage = new Stage();
        stage.setTitle("FYP Chat");
        stage.setScene(scene);
        stage.show();

        // Obtener el controlador del ChatController
        chatController = fxmlLoader.getController();

        // Establecer el nickname en el ChatController
        chatController.setNickname(nickname);

        // Cierra la ventana de login
        Stage loginStage = (Stage) nicknameTextField.getScene().getWindow();
        loginStage.close();
    }
}