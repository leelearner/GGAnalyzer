# Google Drive API Setup

To enable the automatic download of match data from Google Drive, you need to set up a Google Cloud Service Account and provide the credentials.

## Steps

1.  **Create a Google Cloud Project**:
    *   Go to the [Google Cloud Console](https://console.cloud.google.com/).
    *   Create a new project or select an existing one.

2.  **Enable Google Drive API**:
    *   In the dashboard, search for "Google Drive API".
    *   Click "Enable".

3.  **Create a Service Account**:
    *   Go to "IAM & Admin" > "Service Accounts".
    *   Click "Create Service Account".
    *   Give it a name (e.g., "gganalyzer-downloader").
    *   Grant it the "Viewer" role (optional, but good practice).
    *   Click "Done".

4.  **Generate Key**:
    *   Click on the newly created service account email.
    *   Go to the "Keys" tab.
    *   Click "Add Key" > "Create new key".
    *   Select "JSON" and click "Create".
    *   A JSON file will be downloaded to your computer.

5.  **Configure Application**:
    *   Rename the downloaded JSON file to `credentials.json`.
    *   Place it in `backend/src/main/resources/credentials.json`.

6.  **Share the Folder (Important)**:
    *   The folder ID is `1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH`.
    *   If this folder is not public, you must share it with the Service Account email address (found in the JSON file, usually `something@project-id.iam.gserviceaccount.com`).
    *   If the folder is "Anyone with the link", the Service Account should be able to access it without explicit sharing, but explicit sharing is more reliable.

## Troubleshooting

*   **File Not Found**: Ensure the Service Account has access to the folder.
*   **Credentials Error**: Ensure `credentials.json` is in the correct location and is valid.
