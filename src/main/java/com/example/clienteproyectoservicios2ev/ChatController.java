package com.example.clienteproyectoservicios2ev;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.*;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    private TextField messageArea;
    @FXML
    private TextFlow messageInput;
    private String nickname;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial de la interfaz
        messageArea.setPromptText("Escribe algo...");
        try {
            iniciarHiloClienteRecibo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Hilo para que el cliente reciba mensajes del servidor
    private void iniciarHiloClienteRecibo() {
        new Thread(() -> {
            try {
                // Bucle infinito para recibir mensajes continuamente.
                while (true) {
                    // Se crea un array de bytes para almacenar los datos recibidos.
                    byte[] buffer = new byte[LoginController.BUFFER_SIZE];
                    // Se prepara un DatagramPacket para recibir los datos.
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    // Se recibe el paquete utilizando el socket del LoginController.
                    LoginController.clientSocket.receive(receivePacket);

                    // Se convierten los datos recibidos a una cadena de texto.
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    // Se divide el mensaje en partes usando el carácter "|" como delimitador.
                    String[] messageParts = message.split("\\|"); //Esta regex se utiliza para dividir una cadena
                    // en un array de subcadenas basadas en el delimitador "|".

                    // Se extraen el nombre de usuario y el tipo de mensaje.
                    String nicknameRecibido = messageParts[1];
                    String messageType = messageParts[2];

                    // Se imprime en la consola el nombre de usuario y el tipo de mensaje.
                    System.out.println(nicknameRecibido + " - " + messageType);

                    // Se verifica si el mensaje es de otro usuario o del usuario actual y se agrega al historial de mensajes.
                    if (!nicknameRecibido.equals(nickname)) {
                        appendMessage(nicknameRecibido, messageType, Color.BLUE);  // Color para mensajes de otros usuarios.
                    } else {
                        appendMessage(nickname, messageType, Color.RED);  // Color para tus propios mensajes.
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.exit();
            }
        }).start();
    }

    private void appendMessage(String userName, String message, Color color) {
        // Ejecuta la operación en el hilo de la interfaz de usuario.
        Platform.runLater(() -> {
            // Crea un nuevo objeto Text con el nombre de usuario, el mensaje y un salto de línea.
            Text FormatMessage = new Text(userName + ": " + message + "\n");
            // Establece el color del texto del mensaje.
            FormatMessage.setFill(color);
            // Agrega el mensaje al TextFlow donde se almacenan todos los mensajes.
            messageInput.getChildren().add(FormatMessage);
        });
    }

    private void sendMessage(String message) {
        try {
            // Obtiene la dirección del servidor utilizando la IP definida en LoginController
            InetAddress serverAddress = InetAddress.getByName(LoginController.IP);

            // Combina el mensaje con el nombre de usuario y el formato "TEXT|nickname|message"
            String combinedMessage = "TEXT|" + nickname + "|" + message;
            // Convierte la cadena combinada en un array de bytes
            byte[] sendData = combinedMessage.getBytes();

            // Crea un paquete DatagramPacket con los datos del mensaje, la longitud, la dirección del servidor y el puerto del servidor
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, LoginController.PUERTOSERVIDOR);
            // Envía el paquete al servidor utilizando el socket del LoginController
            LoginController.clientSocket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onSendButtonClicked(ActionEvent event) {
        try {
            // Obtiene el texto ingresado en el campo de mensaje.
            String mensajeUsuario = messageArea.getText();

            // Verifica si el mensaje no es nulo.
            if (mensajeUsuario != null) {
                // Llama al método sendMessage para enviar el mensaje.
                sendMessage(mensajeUsuario);
                // Limpia el campo de entrada de mensaje después de enviarlo.
                messageArea.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Método para establecer el nickname
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}