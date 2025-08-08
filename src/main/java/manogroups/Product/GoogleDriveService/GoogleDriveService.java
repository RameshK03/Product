package manogroups.Product.GoogleDriveService;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private final String geturl;
    private final String folderId;
    private final Drive driveService;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    public GoogleDriveService(
            @Value("${application.Name}") String applicationName,
            @Value("${client-id}") String clientId,
            @Value("${client-secret}") String clientSecret,
            @Value("${redirect-uri}") String redirectUri,
            @Value("${geturl}") String geturl,
            @Value("${folderId}") String folderId
    ) throws Exception {
        this.geturl = geturl;
        this.folderId = folderId;

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Manually build GoogleClientSecrets from individual fields
        String credentialsJson = String.format("""
            {
              "installed": {
                "client_id": "%s",
                "client_secret": "%s",
                "redirect_uris": ["%s"],
                "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                "token_uri": "https://oauth2.googleapis.com/token"
              }
            }
            """, clientId, clientSecret, redirectUri);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(new ByteArrayInputStream(credentialsJson.getBytes()))
        );

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        this.driveService = new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY,
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
        ).setApplicationName(applicationName).build();
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        java.io.File tempFile = java.io.File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);

        File fileMetadata = new File();
        fileMetadata.setName(multipartFile.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(folderId));

        File uploadedFile = driveService.files().create(fileMetadata,
                new FileContent(multipartFile.getContentType(), tempFile))
                .setFields("id")
                .execute();

        Files.delete(tempFile.toPath());
        return uploadedFile.getId();
    }

    public String getFileUrl(String fileId) {
        return geturl + fileId;
    }

    public void deleteFileIfExists(String fileId) {
        if (fileExists(fileId)) {
            try {
                driveService.files().delete(fileId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File already deleted or not found: " + fileId);
        }
    }

    public boolean fileExists(String fileId) {
        try {
            driveService.files().get(fileId).execute();
            return true;
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                return false;
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
