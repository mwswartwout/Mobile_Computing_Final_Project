package com.example.team5.wififinalproject;

import android.os.Environment;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.PredictionScopes;
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Input.InputInput;

import com.google.api.services.prediction.model.Insert;
import com.google.api.services.prediction.model.Insert.TrainingInstances;
import com.google.api.services.prediction.model.Insert2;
import com.google.api.services.prediction.model.Insert2.ModelInfo;
import com.google.api.services.prediction.model.Output;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main class for the Prediction API command line sample.
 * Demonstrates how to make an authenticated API call using OAuth 2 helper classes.
 */
public class PredictionsAPIClient {

    static Credential credential;
    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "WifiLocationFinder";
    private static final String TRAINING_DATA_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/FinalProject/TrainingSet.csv").toString();
    private static final String TEST_DATA_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/FinalProject/TestSet.csv").toString();
    private static final String OAUTH_DATA_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/FinalProject/client_secrets.json").toString();
    private static final String PROJECT_NAME = "890533042990";
    private static final String MODEL_ID = "modelId" + Math.random();

    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/FinalProject").toString());

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;

    @SuppressWarnings("unused")
    private static Prediction client;

    PredictionsAPIClient()
    {}

    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential authorize() throws Exception {
        // load client secrets
        if (!isExternalStorageReadable()) {
            System.out.println("External storage isn't readable");
            return null;
        }
        FileReader reader = new FileReader(OAUTH_DATA_LOCATION);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        System.out.println("Created clientSecrets");
        /*if (clientSecrets.getDetails().getClientId().startsWith("Enter") ||
                clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Overwrite the src/main/resources/client_secrets.json file with the client secrets file "
                            + "you downloaded from the Quickstart tool or manually enter your Client ID and Secret "
                            + "from https://code.google.com/apis/console/?api=prediction#project:858822147939 "
                            + "into src/main/resources/client_secrets.json");
            System.exit(1);
        }*/

        // Set up authorization code flow.
        // Ask for only the permissions you need. Asking for more permissions will
        // reduce the number of users who finish the process for giving you access
        // to their accounts. It will also increase the amount of effort you will
        // have to spend explaining to users what you are doing with their data.
        // Here we are listing all of the available scopes. You should remove scopes
        // that you are not actually using.
        Set<String> scopes = new HashSet<String>();
        scopes.add(PredictionScopes.DEVSTORAGE_FULL_CONTROL);
        scopes.add(PredictionScopes.DEVSTORAGE_READ_ONLY);
        scopes.add(PredictionScopes.DEVSTORAGE_READ_WRITE);
        scopes.add(PredictionScopes.PREDICTION);

        System.out.println("Created scopes");
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(dataStoreFactory)
                .build();
        System.out.println("GoogleAuthorizationCodeFlow.Builder created");
        // authorize

        Credential cred = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println("Created AuthorizationCodeInstalledInstallApp");
        
        return cred;
    }

    private static List<TrainingInstances> getTrainingData() throws IOException {
        List<TrainingInstances> instances = new ArrayList<TrainingInstances>();

        //stream read the data file
        InputStreamReader isr = new InputStreamReader(PredictionsAPIClient.class.getResourceAsStream(TRAINING_DATA_LOCATION));
        BufferedReader br = new BufferedReader(isr);

        String line = null;
        while ((line = br.readLine()) != null) {
            String partitionToken = ", ";
            int partition = line.indexOf(partitionToken);
            String output = line.substring(0, partition);
            List<Object> features = new ArrayList<Object>();
            features.add(line.substring(partition + partitionToken.length()));

            instances.add(new TrainingInstances().setOutput(output)
                            .setCsvInstance(features)
            );
        }

        return instances;
    }

    private static String getTestData() throws IOException {
        String testData;

        InputStreamReader isr = new InputStreamReader(PredictionsAPIClient.class.getResourceAsStream(TEST_DATA_LOCATION));
        BufferedReader br = new BufferedReader(isr);

        if ((testData = br.readLine()) != null) {
            return testData;
        }

        return null;
    }
    private static Insert2 responseToObject(String jsonString) {

        Insert2 res = new Insert2();
        JSONParser parser = new JSONParser();
        try {

            JSONObject obj = (JSONObject) parser.parse(jsonString);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            res.setCreated(new DateTime((Date) formatter.parse((String) obj.get("created"))));
            res.setId((String) obj.get("id"));
            res.setKind((String) obj.get("kind"));
            res.setSelfLink((String) obj.get("selfLink"));
            res.setTrainingStatus((String) obj.get("trainingStatus"));

            if (obj.get("trainingComplete") != null) {

                res.setTrainingComplete(new DateTime((Date) formatter.parse((String) obj.get("trainingComplete"))));
                JSONObject ml = (JSONObject) obj.get("modelInfo");
                Insert2.ModelInfo modelInfo = new ModelInfo();
                modelInfo.setNumberInstances(Long.parseLong((String) ml.get("numberInstances")));
                modelInfo.setModelType((String) ml.get("modelType"));
                modelInfo.setNumberLabels(Long.parseLong((String) ml.get("numberLabels")));
                modelInfo.setClassificationAccuracy((String) ml.get("classificationAccuracy"));
                res.setModelInfo(modelInfo);

            }

        } catch (ParseException e) {
            e.printStackTrace();
            res = null;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            res = null;
        }
        return res;

    }

    private static void predict(Prediction prediction, String text) throws IOException {

        Input input = new Input();
        InputInput inputInput = new InputInput();
        inputInput.setCsvInstance(Collections.<Object>singletonList(text));
        input.setInput(inputInput);
        Output output = prediction.trainedmodels().predict(PROJECT_NAME, MODEL_ID, input).execute();

        System.out.println("MAC Addresses: " + text);
        System.out.println("Predicted location: " + output.getOutputLabel());

    }

    private static void train(Prediction prediction) throws IOException {
        //start the training process of the google APIs
        //provide the training sample via embedding data inside requests
        List<TrainingInstances> instances = getTrainingData();
        Insert insert = new Insert().setTrainingInstances(instances);
        insert.setFactory(JSON_FACTORY);
        insert.setId(MODEL_ID);
        prediction.trainedmodels().insert(PROJECT_NAME, insert).execute();

        int triesCounter = 0;
        while (triesCounter < 1000) {
            try {

                HttpResponse httpResponse = prediction.trainedmodels().get(PROJECT_NAME, MODEL_ID).executeUnparsed();

                if (httpResponse.getStatusCode() == 200) {

                    Insert2 res = responseToObject(httpResponse.parseAsString());

                    if (res.getTrainingStatus().compareTo("DONE") == 0) {

                        System.out.println("training complete");
                        System.out.println(res.getModelInfo());
                        return;

                    }

                } else {

                    httpResponse.ignore();

                }

                Thread.sleep(5000 * (triesCounter + 1));
                System.out.print(".");
                System.out.flush();
                triesCounter++;

            } catch (Exception e) {

                e.printStackTrace();
                break;

            }
        }

        System.err.println("ERROR: training not complete");
        System.exit(1);

    }

    public static void setup()
    {
        System.out.println("Starting Predictions API setup");
        try {
            System.out.println("Initializing httpTransport");
            // initialize the transport
            httpTransport = AndroidHttp.newCompatibleTransport();
            System.out.println("Finished initializing httpTransport");

            System.out.println("Initializing dataStoreFactory");
            // initialize the data store factory
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            System.out.println("Finished initializing dataStoreFactory");

            System.out.println("Authorizing credentials...");
            // authorization
            credential = authorize();
            System.out.println("Finished authorizing credentials");

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void train() {
        try {
            // set up global Prediction instance
            client = new Prediction.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

//      System.out.println("Success! Now add code here.");
            train(client);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void predict() {
        try {
            predict(client, getTestData());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}