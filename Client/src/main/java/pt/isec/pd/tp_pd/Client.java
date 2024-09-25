package pt.isec.pd.tp_pd;

import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pt.isec.pd.tp_pd.controllers.ClientController;
import pt.isec.pd.tp_pd.data.ClientRequest;
import pt.isec.pd.tp_pd.data.ClientResponse;
import pt.isec.pd.tp_pd.data.Event;
import pt.isec.pd.tp_pd.data.User;
import pt.isec.pd.tp_pd.utils.Alerts;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import javax.json.*;
public class Client extends Application {
    Timer timer;
    private ClientController clientController;
    private static User user = null;
    private ClientResponse clientResponse = null;
    private ClientRequest clientRequest = null;
    private static final int TIMEOUT = 10; //seconds
    private static ObjectOutputStream oout = null;
    private static ObjectInputStream oin = null;

    private final String _API_STRING = "http://localhost:8080/";
    private String _token;

    public User getUser() { return user; }

    // this method uses token to authenticate user,
    // do not use this method if user has not yet auth token
    private void setUser() {
        String uri = _API_STRING + "users/requestAuthor";
        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + _token);
            connection.setDoOutput(true);

            // reads user from response
            InputStream jsonStream = connection.getInputStream();
            JsonReader jsonReader = Json.createReader(jsonStream);
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();
            connection.disconnect();

            Gson gson = new GsonBuilder().create();
            user = gson.fromJson(jsonObject.toString(), User.class);
            return;
        } catch (MalformedURLException e) {
            System.out.println("register: wrong uri.");
        } catch (IOException e) {
            System.out.println("register: problem with connection opening.");
        }

        user = null;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 750);
        stage.setTitle("ARA - Attendance Registration Application");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(600);
        stage.show();

        // Get Client controller from FXML
        clientController = fxmlLoader.getController();
        clientController.setClient(this);
    }

    private void sendClientRequest(ClientRequest clientRequest) {
        clientController.setClient(this);
        // If the connection was established
        if (oout != null) {
            try {
                oout.writeObject(clientRequest);
                oout.flush();
            } catch (IOException e) {
                System.out.println("I/O Error: " + e);
            }
        } else {
            System.out.println("Connection to the server was not established.");
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Syntax: listeningTCPPort serverAddress");
            Platform.exit();
        } else {
            int listeningTCPPort;
            String serverAddress;

            listeningTCPPort = Integer.parseInt(args[0]);
            serverAddress = args[1];

            System.out.println("Waiting to connect to the server...");

            try (Socket socket = new Socket(serverAddress, listeningTCPPort)) {
                oout = new ObjectOutputStream(socket.getOutputStream());
                oin = new ObjectInputStream(socket.getInputStream());
                socket.setSoTimeout(TIMEOUT * 1000);
                System.out.println("Connection to the server is successful");
                launch();

            } catch (UnknownHostException e) {
                System.out.println("Unknown destination:\n\t" + e);
                Platform.exit();
            } catch (NumberFormatException e) {
                System.out.println("Server Port must be a positive integer.");
                Platform.exit();
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout exceeded, exiting the program" + e);
                Platform.exit();
            } catch (SocketException e) {
                System.out.println("There is no server running");
                Platform.exit();
            } catch (IOException e) {
                System.out.println("I/O exception:\n\t" + e);
                Platform.exit();
            }
        }
    }

    private void renewToken(String email, String password) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                login(email, password);
            }
        }, 60000 * 5);
    }

    public boolean login(String email, String password) {

        String uri = _API_STRING + "login";
        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setDoOutput(true);

            // set authentication
            String credentials = email + ":" + password;
            String credentials64 = Base64.getEncoder().encodeToString(credentials.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + credentials64);

            // reads received token
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if (100 <= responseCode && responseCode <= 399) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                _token = br.readLine();
                br.close();
                connection.disconnect();

                setUser();

                renewToken(email, password);

                return true;
            } else {
                connection.disconnect();
                return false;
            }
        } catch (MalformedURLException e) {
            System.out.println("register: wrong uri.");
        } catch (IOException e) {
            System.out.println("register: problem with connection opening.");
        }
        return false;
    }

    public boolean register(String email, int user_id, String password) {
        String uri = _API_STRING + "users/register";
        URL url;
        try {
            url = new URL(uri);
            Gson gson = new GsonBuilder().create();

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setDoOutput(true);

            // convert user to json
            User user = new User();
            user.setEmail(email);
            user.setUser_id(user_id);
            user.setPassword(password);
            String userJson = gson.toJson(user);

            // add user to request's body
            try (OutputStream os = connection.getOutputStream();
                 OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                osw.write(userJson);
                osw.flush();
            }

            // read response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            connection.disconnect();

            return responseCode == 200;

        } catch (MalformedURLException e) {
            System.out.println("register: wrong uri.");
        } catch (IOException e) {
            System.out.println("register: problem with connection opening.");
        }
        return false;
    }

    public boolean edit(String email, String password) {
        clientRequest = new ClientRequest(
                new User(user.getUser_id(), email, password, user.getUser_type()),
                ClientRequest.Type.EDIT_PROFILE);
        sendClientRequest(clientRequest);

        return receiveClientResponse();
    }

    public boolean submitCode(String code) {
        String uri = _API_STRING + "attendances/" + code;
        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + _token);

            // reads user from response
            return connection.getResponseCode() == 200;
        } catch (MalformedURLException e) {
            System.out.println("register: wrong uri.");
        } catch (IOException e) {
            System.out.println("register: problem with connection opening.");
        }

        return false;
    }

    public ArrayList<Event> loadAttendances() {
        String uri = _API_STRING + "users/" + getUser().getUser_id() + "/events";
        ArrayList<Event> events = new ArrayList<>();

        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + _token);
            connection.setDoOutput(true);

            // reads events from response
            InputStream jsonStream = connection.getInputStream();
            JsonReader jsonReader = Json.createReader(jsonStream);
            JsonArray array = jsonReader.readArray();
            jsonReader.close();
            connection.disconnect();

            Gson gson = new GsonBuilder().create();

            for(int i=0; i<array.size(); i++){
                JsonObject object = array.getJsonObject(i);
                Event event = gson.fromJson(object.toString(), Event.class);
                events.add(event);
            }

            return events;
        } catch (MalformedURLException e) {
            System.out.println("wrong uri.");
        } catch (IOException e) {
            System.out.println("problem with connection opening.");
        }

        return null;
    }

    public boolean receiveClientResponse() {
        try {
            // Receive Message
            clientResponse = null; // Clear the previous message
            clientResponse = (ClientResponse) oin.readObject(); // Get the response

            if (clientResponse == null) {
                Alerts.showGeneralAlert("Client Response is null");
                return false;
            }

            user = clientResponse.user; // always get the updated user
            if (clientResponse.user != null) {
                System.out.println("Response type: " + clientResponse.type.toString());
                if(clientResponse.type == ClientResponse.Type.ERROR){
                    Alerts.showGeneralAlert("Error");
                }
                if (clientResponse.type == null) {
                    Alerts.showGeneralAlert("Response type is null");
                    return false;
                }
                return true;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error: " + e);
            return false;
        }
        return false;
    }

    public boolean createEvent(Event event) {
        String uri = _API_STRING + "events";
        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + _token);
            connection.setDoOutput(true);

            Gson gson = new GsonBuilder().create();
            String userJson = gson.toJson(event);

            // add event to request's body
            try (OutputStream os = connection.getOutputStream();
                 OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                osw.write(userJson);
                osw.flush();
            }

            // reads response code
            boolean created = connection.getResponseCode() == 200;
            connection.disconnect();

            return created;
        } catch (MalformedURLException e) {
            System.out.println("wrong uri.");
        } catch (IOException e) {
            System.out.println("problem with connection opening.");
        }

        return false;
    }

    public String generateCode() {
        String uri = _API_STRING + "events/code";
        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + _token);
            connection.setDoOutput(true);

            // reads user from response
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String code = reader.readLine();
            reader.close();
            connection.disconnect();

            return code;
        } catch (MalformedURLException e) {
            System.out.println("wrong uri.");
        } catch (IOException e) {
            System.out.println("problem with connection opening.");
        }

        return null;
    }

    public ArrayList<Event> getEvents() {
        String uri = _API_STRING + "events";
        ArrayList<Event> events = new ArrayList<>();

        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + _token);
            connection.setDoOutput(true);

            // reads events from response
            InputStream jsonStream = connection.getInputStream();
            JsonReader jsonReader = Json.createReader(jsonStream);
            JsonArray array = jsonReader.readArray();
            jsonReader.close();
            connection.disconnect();

            Gson gson = new GsonBuilder().create();

            for(int i=0; i<array.size(); i++){
                JsonObject object = array.getJsonObject(i);
                Event event = gson.fromJson(object.toString(), Event.class);
                events.add(event);
            }

            return events;
        } catch (MalformedURLException e) {
            System.out.println("wrong uri.");
        } catch (IOException e) {
            System.out.println("problem with connection opening.");
        }

        return null;
    }

    public ArrayList<User> getEventUsers(Integer eventId) {
        String uri = _API_STRING + "events/"+ eventId + "/users";
        ArrayList<User> users = new ArrayList<>();

        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + _token);
            connection.setDoOutput(true);

            // reads events from response
            InputStream jsonStream = connection.getInputStream();
            JsonReader jsonReader = Json.createReader(jsonStream);
            JsonArray array = jsonReader.readArray();
            jsonReader.close();
            connection.disconnect();

            Gson gson = new GsonBuilder().create();

            for(int i=0; i<array.size(); i++){
                JsonObject object = array.getJsonObject(i);
                User user = gson.fromJson(object.toString(), User.class);
                users.add(user);
            }

            return users;
        } catch (MalformedURLException e) {
            System.out.println("wrong uri.");
        } catch (IOException e) {
            System.out.println("problem with connection opening.");
        }

        return null;
    }

    public boolean deleteParticipantAttendance(int eventID, int userID){
        clientRequest = new ClientRequest(user, ClientRequest.Type.ADMIN_DELETE_PARTICIPANT_ATTENDANCE, eventID, userID);

        sendClientRequest(clientRequest);
        return receiveClientResponse();
    }

    public boolean addParticipantAttendance(int eventID, String userEmail){
        clientRequest = new ClientRequest(user, ClientRequest.Type.ADMIN_ADD_PARTICIPANT_ATTENDANCE, eventID, userEmail);
        System.out.println(clientRequest);

        sendClientRequest(clientRequest);
        receiveClientResponse();

        return clientResponse != null && clientResponse.type != ClientResponse.Type.ERROR;
    }

    public boolean deleteEvent(int eventId) {
        String uri = _API_STRING + "events/" + eventId;
        try {
            URL url = new URL(uri);

            // set up http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + _token);

            // reads response code
            boolean deleted = connection.getResponseCode() == 200;
            connection.disconnect();

            return deleted;
        } catch (MalformedURLException e) {
            System.out.println("wrong uri.");
        } catch (IOException e) {
            System.out.println("problem with connection opening.");
        }

        return false;
    }

    public Event getEvent(int eventId) {
        clientRequest = new ClientRequest(user, ClientRequest.Type.ADMIN_GET_EVENT, eventId);

        sendClientRequest(clientRequest);
        receiveClientResponse();

        if (clientResponse.type == ClientResponse.Type.ERROR)
            return null;
        else {
            return (Event) clientResponse.getReponseResult();
        }
    }

    public boolean updateEvent(Event event) {
        clientRequest = new ClientRequest(user, ClientRequest.Type.ADMIN_EDIT_EVENT, event);

        sendClientRequest(clientRequest);
        receiveClientResponse();

        if(clientResponse.type == ClientResponse.Type.ERROR) {

            return false;
        }

        return true;
    }

    public ArrayList<Event> getEventsByUser(int userId) {
        clientRequest = new ClientRequest(user, ClientRequest.Type.ADMIN_GET_EVENTS_BY_USER, userId);

        sendClientRequest(clientRequest);
        receiveClientResponse();

        return (ArrayList<Event>) clientResponse.getReponseResult();


    }

    public void logout() {
        user = null;
        timer.cancel();
    }
}
