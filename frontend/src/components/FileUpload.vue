<template>
    <div class="container mt-4">
      <h2>Upload CSV File</h2>
  
      <!-- File Input -->
      <div class="mb-3">
        <input type="file" class="form-control" @change="handleFileUpload" accept=".csv" />
      </div>
  
      <!-- Upload Button -->
      <button class="btn btn-primary" @click="uploadFile" :disabled="!selectedFile || isLoading">
        <span v-if="isLoading" class="spinner-border spinner-border-sm"></span>
        {{ isLoading ? "Uploading..." : "Upload File" }}
      </button>
  
      <!-- Upload Message -->
      <div v-if="uploadMessage" class="mt-3 alert" :class="uploadSuccess ? 'alert-success' : 'alert-danger'">
        {{ uploadMessage }}
      </div>
  
      <!-- File Upload Response Details -->
      <div v-if="uploadSuccess && fileDetails" class="mt-4">
        <h5>Uploaded File Details</h5>
        <ul class="list-group">
          <li class="list-group-item"><strong>File Name:</strong> {{ fileDetails.originalFilename }}</li>
          <li class="list-group-item"><strong>File ID:</strong> {{ fileDetails.fileId }}</li>
          <li v-if="fileDetails.headers" class="list-group-item">
            <strong>CSV Headers:</strong>
            <span v-for="header in fileDetails.headers" :key="header" class="badge bg-secondary me-1">{{ header }}</span>
          </li>
        </ul>
      </div>

      <!-- Show Status Loading Indicator While Polling -->
      <!-- File Status Message -->
        <div v-if="pollingMessage" class="mt-3 alert" :class="pollingClass">
            <span v-if="isPolling" class="spinner-border spinner-border-sm me-2"></span>
            {{ pollingMessage }}
        </div>
  
      <!-- File Processing Status -->
      <div v-if="uploadSuccess && fileStatusDetails" class="mt-4">
        <h5>File Storage Details:</h5>
        <ul class="list-group">
          <li class="list-group-item">
            <strong>Status:</strong>
            <span class="badge" :class="statusBadgeClass">{{ fileStatusDetails?.uploadStatus }}</span>
          </li>
          <li v-if="fileStatusDetails.originalFilename" class="list-group-item">
            <strong>File Name:</strong> {{ fileStatusDetails.originalFilename }}
          </li>
          <li v-if="fileStatusDetails.uploadTimestamp" class="list-group-item">
            <strong>Upload Time:</strong> {{ formatDateTime(fileStatusDetails.uploadTimestamp) }}
          </li>
          <li v-if="fileStatusDetails.storagePath" class="list-group-item">
            <strong>Storage Path:</strong> {{ fileStatusDetails.storagePath }}
          </li>
          <li v-if="fileStatusDetails.processingStartTime" class="list-group-item">
            <strong>Processing Start Time:</strong> {{ formatDateTime(fileStatusDetails.processingStartTime) }}
          </li>
          <li v-if="fileStatusDetails.processingEndTime" class="list-group-item">
            <strong>Processing End Time:</strong> {{ formatDateTime(fileStatusDetails.processingEndTime) }}
          </li>
          <li v-if="fileStatusDetails.errorMessage" class="list-group-item text-danger">
            <strong>Error:</strong> {{ fileStatusDetails.errorMessage }}
          </li>
        </ul>
      </div>
    </div>
  </template>
  
  <script>
  import fileUploadApi from "@/api/fileUploadApi";
  export default {
    data() {
      return {
        selectedFile: null,
        uploadMessage: "",
        uploadSuccess: false,
        isLoading: false,
        isPolling: false,
        pollingMessage: "",
        pollingClass: "alert-info", // bootstrap class for styling.
        fileId: null,
        fileDetails: null,
        fileStatusDetails: null,
        pollingInterval: null, // Store polling interval reference
      };
    },
    computed: {
      statusBadgeClass() {
        switch (this.fileStatusDetails?.uploadStatus) {
          case "PROCESSING":
            return "bg-warning text-dark";
          case "UPLOADED":
            return "bg-success";
          case "FAILED":
            return "bg-danger";
          default:
            return "bg-secondary";
        }
      },
    },
    methods: {
      handleFileUpload(event) {
        this.selectedFile = event.target.files[0];
      },
      async uploadFile() {
        if (!this.selectedFile) return;
  
        this.isLoading = true;
        this.uploadMessage = "";
        this.fileStatusDetails = null;
  
        const formData = new FormData();
        formData.append("file", this.selectedFile);
  
        try {
          const response = await fileUploadApi.post("/api/upload", formData, {
            headers: { "Content-Type": "multipart/form-data" },
          });
  
          this.fileDetails = response.data;
          this.fileId = response.data.fileId;
          this.uploadMessage = "File uploaded successfully! Async file storage started...";
          this.uploadSuccess = true;
  
          this.startPollingStatus();
        } catch (error) {
          this.uploadMessage = "Failed to upload file.";
          this.uploadSuccess = false;
        } finally {
          this.isLoading = false;
        }
      },
      startPollingStatus() {
        if (!this.fileId) return;

        this.isPolling = true;
        this.pollingMessage = "Checking file storage status..."; 
        this.pollingClass = "alert-info"; 

        this.pollingInterval = setInterval(async () => {
            try {
            const response = await fileUploadApi.get(`/api/files/${this.fileId}`);
            this.fileStatusDetails = response.data;

            console.log("Updated file status:", this.fileStatusDetails);
            this.pollingMessage = `File Storage Status: ${this.fileStatusDetails.uploadStatus}`;
            this.pollingClass = this.fileStatusDetails.uploadStatus === "FAILED" ? "alert-danger" : "alert-success";

            if (this.fileStatusDetails.uploadStatus !== "PROCESSING") {
                clearInterval(this.pollingInterval);
                this.pollingInterval = null;
                this.isPolling = false; 
            }
            } catch (error) {
                console.error("Error fetching file status:", error);
                this.isPolling = false;
                this.pollingMessage = "Getting file status failed!";
                this.pollingClass = "alert-danger"; // ✅ Red alert for failure
            } finally {
                this.isPolling = false; 
            }
        }, 5000);
    },
      formatDateTime(dateTime) {
        return dateTime ? new Date(dateTime).toLocaleString() : "";
      },
    },
    beforeUnmount() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
      }
    },
  };
  </script>
  
  <style scoped>
  .badge {
    font-size: 1rem;
    padding: 0.5rem;
  }
  </style>
  