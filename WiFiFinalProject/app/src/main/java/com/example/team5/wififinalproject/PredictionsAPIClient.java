package com.example.team5.wififinalproject;

import android.os.AsyncTask;
import android.os.Environment;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
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
import java.io.File;
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
    //private static final String OAUTH_DATA_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/FinalProject/client_secrets.json").toString();
    //private static final String OAUTH_DATA_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/FinalProject/WiFi Location Predicter-8a1736dfeecc.json").toString();
    private static final String OAUTH_DATA_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/FinalProject/WiFi Location Predicter-47e810de7920.p12").toString();
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
        //FileReader reader = new FileReader(OAUTH_DATA_LOCATION);
        //GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
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
        //GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        //        httpTransport, JSON_FACTORY, clientSecrets, scopes)
        //        .setDataStoreFactory(dataStoreFactory)
        //        .build();
        System.out.println("GoogleAuthorizationCodeFlow.Builder created");

        //Credential cred = new GetCredentialsTask().execute(flow).get();
        String emailAddress = "account-1@onyx-park-114601.iam.gserviceaccount.com";

        GoogleCredential cred = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(emailAddress)
                .setServiceAccountPrivateKeyFromP12File(new File(OAUTH_DATA_LOCATION))
                .setServiceAccountScopes(scopes)
                .build();

        System.out.println("Created Credential from task");

        return cred;
    }

    private static List<TrainingInstances> getTrainingData() throws IOException {
        System.out.println("Getting training data...");
        List<TrainingInstances> instances = new ArrayList<TrainingInstances>();

        //stream read the data file
        FileReader isr = new FileReader(TRAINING_DATA_LOCATION);
        BufferedReader br = new BufferedReader(isr);

        String line = null;
        while ((line = br.readLine()) != null) {
            String partitionToken = ",";
            int partition = line.indexOf(partitionToken);
            //System.out.println("partition value is " +partition);
            String output = line.substring(0, partition);
            //System.out.println("substring is " + output);
            List<Object> features = new ArrayList<Object>();
            features.add(line.substring(partition + partitionToken.length()));

            instances.add(new TrainingInstances().setOutput(output)
                            .setCsvInstance(features)
            );
        }

        System.out.println("Training data retrieval complete");
        return instances;
    }

    private static String getTestData() throws IOException {
        System.out.println("Getting test data...");
        String testData;

        FileReader isr = new FileReader(TEST_DATA_LOCATION);
        BufferedReader br = new BufferedReader(isr);

        if ((testData = br.readLine()) != null) {
            System.out.println("Test data retrieval complete");
            return testData;
        }

        System.out.println("Test data retrieval unsuccessful");
        return null;
    }
    private static Insert2 responseToObject(String jsonString) {
        System.out.println("responseToObject call starting");
        Insert2 res = new Insert2();
        JSONParser parser = new JSONParser();
        try {
            System.out.println("Parsing JSON object...");
            JSONObject obj = (JSONObject) parser.parse(jsonString);
            System.out.println("JSON object successfully parsed");
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            System.out.println("Got date");
            res.setCreated(new DateTime((Date) formatter.parse((String) obj.get("created"))));
            res.setId((String) obj.get("id"));
            res.setKind((String) obj.get("kind"));
            res.setSelfLink((String) obj.get("selfLink"));
            res.setTrainingStatus((String) obj.get("trainingStatus"));

            if (obj.get("trainingComplete") != null) {
                System.out.println("Got a traning complete object");
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
            System.out.println("Caught a ParseException in responseToObject");
            e.printStackTrace();
            res = null;
        } catch (java.text.ParseException e) {
            System.out.println("Caught a java.text.ParseException in responseToObject");
            e.printStackTrace();
            res = null;
        }
        System.out.println("reponseToObject call complete");
        return res;

    }

    private static void predict(Prediction prediction, String text) throws IOException {
        System.out.println("Starting inner predict call");
        Input input = new Input();
        InputInput inputInput = new InputInput();
        inputInput.setCsvInstance(Collections.<Object>singletonList(text));
        input.setInput(inputInput);
        System.out.println("Created input");
        PredictionParams params = new PredictionParams(prediction, PROJECT_NAME, MODEL_ID, input);
        Output output = null;
        try {
            output = new doPrediction().execute(params).get();
        } catch (Exception e) {
            System.out.println("Caught exception in predict call");
            e.printStackTrace();
        }
        System.out.println("MAC Addresses: " + text);
        System.out.println("Predicted location: " + output.getOutputLabel());
        System.out.println("Finished inner predict call");
    }

    private static void train(Prediction prediction) throws IOException {
        System.out.println("Starting inner train call");
        //start the training process of the google APIs
        //provide the training sample via embedding data inside requests
        List<TrainingInstances> instances = getTrainingData();
        Insert insert = new Insert().setTrainingInstances(instances);
        System.out.println("Inserted training data");
        insert.setFactory(JSON_FACTORY);
        insert.setId(MODEL_ID);
        System.out.println("Ready to insert");
        InsertParams params = new InsertParams(prediction, insert, PROJECT_NAME);
        new insertTrainingData().execute(params);
        System.out.println("Called trainedmodels.insert");
        int triesCounter = 0;
        while (triesCounter < 1000) {
            try {
                System.out.println("Trying to get http response");
                ResponseParams responseParams = new ResponseParams(prediction, PROJECT_NAME, MODEL_ID);
                HttpResponse httpResponse = new getHttpReponse().execute(responseParams).get();

                if (httpResponse.getStatusCode() == 200) {
                    System.out.println("Got status code 200");
                    String response = new getReponseString().execute(httpResponse).get();
                    System.out.println("Converted respose to string");
                    Insert2 res = responseToObject(response);
                    System.out.println("Successfully retrieved response to object");

                    if (res.getTrainingStatus().compareTo("DONE") == 0) {

                        System.out.println("training complete");
                        System.out.println(res.getModelInfo());
                        return;

                    }

                } else {
                    System.out.println("Got response that is going to be ignored");
                    httpResponse.ignore();

                }

                Thread.sleep(5000 * (triesCounter + 1));
                System.out.print(".");
                System.out.flush();
                triesCounter++;

            } catch (Exception e) {
                System.out.println("Caught Exception while waiting for http response in inner train call");
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
        System.out.println("Starting outer train call");
        try {
            // set up global Prediction instance
            client = new Prediction.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

//      System.out.println("Success! Now add code here.");
            train(client);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("Outer train call complete");
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

class GetCredentialsTask extends AsyncTask<GoogleAuthorizationCodeFlow, Void, Credential> {
    protected Credential doInBackground(GoogleAuthorizationCodeFlow... flow) {
        try {
            LocalServerReceiver srv = new LocalServerReceiver();
            System.out.println("Crated LocalServerReceiver");

            AuthorizationCodeInstalledApp acia = new AuthorizationCodeInstalledApp(flow[0], srv);
            System.out.println("Created AuthorizationCodeInstalledApp");

            Credential cred = acia.authorize("user");
            System.out.println("Authorized credential");
            return cred;
        } catch (IOException e) {
            System.out.println("Caught IOException in GetCredentialTask");
            e.printStackTrace();
        }

        return null;
    }
}

class insertTrainingData extends AsyncTask<InsertParams, Void, Void> {
    protected Void doInBackground(InsertParams... params) {
        try {
            Prediction prediction = params[0].prediction;
            Insert insert = params[0].insert;
            String PROJECT_NAME = params[0].PROJECT_NAME;

            prediction.trainedmodels().insert(PROJECT_NAME, insert).execute();
        } catch (IOException e) {
            System.out.println("Caught IO Exception in insertTrainingData task");
            e.printStackTrace();
        }

        return null;
    }
}

class InsertParams {
    public Prediction prediction;
    public Insert insert;
    public String PROJECT_NAME;

    InsertParams(Prediction prediction, Insert insert, String name) {
        this.prediction = prediction;
        this.insert = insert;
        PROJECT_NAME = name;
    }
}

class getHttpReponse extends AsyncTask<ResponseParams , Void, HttpResponse> {
    protected HttpResponse doInBackground(ResponseParams... params) {
        Prediction prediction = params[0].prediction;
        String PROJECT_NAME = params[0].PROJECT_NAME;
        String MODEL_ID = params[0].MODEL_ID;

        try {
            HttpResponse httpResponse = prediction.trainedmodels().get(PROJECT_NAME, MODEL_ID).executeUnparsed();
            return httpResponse;

        } catch (IOException e) {
            System.out.println("Caught IO Exception in getHttpResponse task");
            e.printStackTrace();
        }

        return null;
    }
}

class ResponseParams {
    public Prediction prediction;
    public String PROJECT_NAME;
    public String MODEL_ID;

    ResponseParams(Prediction prediction, String name, String id) {
        this.prediction = prediction;
        PROJECT_NAME = name;
        MODEL_ID = id;
    }
}

class getReponseString extends AsyncTask<HttpResponse, Void, String> {
    protected String doInBackground(HttpResponse... params) {
        HttpResponse response = params[0];
        String string = null;
        try {
            string = response.parseAsString();
        } catch (IOException e) {
            System.out.println("Caught IO Exception in getResponseString task");
            e.printStackTrace();
        }
        return string;
    }
}

class doPrediction extends AsyncTask<PredictionParams, Void, Output> {
    protected Output doInBackground(PredictionParams... params) {
        Prediction prediction = params[0].prediction;
        String PROJECT_NAME = params[0].PROJECT_NAME;
        String MODEL_ID = params[0].MODEL_ID;
        Input input = params[0].input;

        Output output = null;
        try {
            output = prediction.trainedmodels().predict(PROJECT_NAME, MODEL_ID, input).execute();
        } catch (IOException e) {
            System.out.println("Caught IO Exception in doPrediction task");
            e.printStackTrace();
        }

        return output;
    }
}

class PredictionParams {
    public Prediction prediction;
    public String PROJECT_NAME;
    public String MODEL_ID;
    public Input input;

    PredictionParams(Prediction prediction, String name, String id, Input input) {
        this.prediction = prediction;
        PROJECT_NAME = name;
        MODEL_ID = id;
        this.input = input;
    }
}