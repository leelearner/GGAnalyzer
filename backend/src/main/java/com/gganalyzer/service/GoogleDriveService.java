package com.gganalyzer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "GGAnalyzer";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public void downloadFile(String folderId, String fileName, String destinationPath)
            throws IOException, GeneralSecurityException {
        Drive service = getDriveService();

        String query = String.format("name = '%s' and '%s' in parents and trashed = false", fileName, folderId);
        FileList result = service.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No file found with name: " + fileName + " in folder: " + folderId);
        } else {
            for (File file : files) {
                System.out.printf("Found file: %s (%s)\n", file.getName(), file.getId());
                downloadFileContent(service, file.getId(), destinationPath);
                // Assuming we only want the first match
                return;
            }
        }
    }

    private void downloadFileContent(Drive service, String fileId, String destinationPath) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(destinationPath)) {
            service.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            System.out.println("File downloaded to: " + destinationPath);
        }
    }

    private Drive getDriveService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Load credentials from resources
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH
                    + ". Please place your Google Service Account credentials.json in src/main/resources/");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(DriveScopes.DRIVE_READONLY));

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
