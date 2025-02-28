<template>
    <div class="container mt-4">
      <h2>Column Mapping</h2>
  
      <!-- JSON Input for Column Mapping -->
      <div class="mb-3">
        <label for="mappingJson" class="form-label">Enter JSON Mapping</label>
        <textarea
          id="mappingJson"
          class="form-control"
          v-model="mappingJson"
          rows="5"
          placeholder='{
           "fileId": "file_id",
           "mappings": {
               "CSVHeader1":"DatabaseColumn1",
               "CSVHeader2":"DatabaseColumn2"
           }'
          @input="validateJson"
        ></textarea>
      </div>
  
      <!-- JSON Validation Error -->
      <div v-if="jsonErrorMessage" class="alert alert-danger">{{ jsonErrorMessage }}</div>
  
      <!-- Submit Mapping Button -->
      <button class="btn btn-success" @click="submitMapping" :disabled="isSubmitting || jsonErrorMessage !== '' || mappingJson.trim() === ''">
        <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
        Submit Mapping
      </button>
  
      <!-- Success / Error Messages -->
      <div v-if="successMessage" class="alert alert-success mt-3">{{ successMessage }}</div>
      <div v-if="errorMessage" class="alert alert-danger mt-3">{{ errorMessage }}</div>
  
      <!-- File Processing Status Display -->
      <div v-if="statusMessage" class="alert mt-3" :class="statusClass">
        {{ statusMessage }}
      </div>


      <!-- File Processing Status Section -->
      <div class="mt-5">
        <h4>Check File Data Validation Status</h4>
  
        <!-- Input for File ID -->
        <div class="mb-3">
          <label for="fileIdInput" class="form-label">Enter File ID</label>
          <input id="fileIdInput" type="text" class="form-control" v-model="inputFileId" placeholder="Enter File ID">
        </div>
  
        <!-- Buttons to Get Status & Errors -->
        <button class="btn btn-primary me-2" @click="getFileStatus" :disabled="!inputFileId || isCheckingStatus">
          <span v-if="isCheckingStatus" class="spinner-border spinner-border-sm me-2"></span>
          Check Status
        </button>
        <button class="btn btn-danger" @click="getFileErrors" :disabled="!inputFileId || isCheckingErrors">
          <span v-if="isCheckingErrors" class="spinner-border spinner-border-sm me-2"></span>
          Check Errors
        </button>
  
        <!-- File Processing Status Display -->
        <div v-if="statusMessage" class="alert mt-3" :class="statusClass">
          {{ statusMessage }}
        </div>
  
        <!-- Display Errors -->
        <div v-if="fileErrors.length > 0" class="alert alert-warning mt-3">
          <h5>File Errors:</h5>
          <ul>
            <li v-for="(error, index) in fileErrors" :key="index">{{ error }}</li>
          </ul>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import columnMappingApi from "@/api/columnMappingApi";
  
  export default {
    data() {
      return {
        mappingJson: "", // User-input JSON mapping
        inputFileId: "", // File ID input for status/errors check
        isSubmitting: false,
        isCheckingStatus: false,
        isCheckingErrors: false,
        successMessage: "",
        errorMessage: "",
        jsonErrorMessage: "", // ✅ Stores JSON validation errors
        statusMessage: "",
        statusClass: "alert-info",
        fileErrors: [],
        pollingInterval: null,
      };
    },
    methods: {
      validateJson() {
        if (!this.mappingJson.trim()) {
            this.jsonErrorMessage = "JSON input cannot be empty.";
            return;
        }
        try {
          JSON.parse(this.mappingJson);
          this.jsonErrorMessage = ""; // ✅ JSON is valid
        } catch (error) {
          this.jsonErrorMessage = "Invalid JSON format! Please correct it.";
        }
      },
      async submitMapping() {
        if (this.jsonErrorMessage) return; // Prevent submission if JSON is invalid
  
        this.isSubmitting = true;
        this.successMessage = "";
        this.errorMessage = "";
        this.statusMessage = "";
  
        try {
          const parsedMapping = JSON.parse(this.mappingJson); // ✅ Ensure it's valid JSON
  
          const response = await columnMappingApi.post("/api/mapping", { ...parsedMapping });
  
          this.successMessage = response.data.message;
          this.inputFileId = response.data.fileId; // Assume API returns `fileId`
  
          this.startPollingStatus(); // Start checking processing status
        } catch (error) {
          console.error("Error submitting mapping:", error);
          this.errorMessage = "Invalid JSON format or API request failed.";
        } finally {
          this.isSubmitting = false;
        }
      },
      async getFileStatus() {
        this.isCheckingStatus = true;
        this.statusMessage = "";
        this.statusClass = "alert-info";
  
        try {
          const response = await columnMappingApi.get(`/api/mapping/status/${this.inputFileId}`);
          this.statusMessage = `File Data Validation Status: ${response.data.status}; Error Count: ${response.data.errorCount}; File ID: ${response.data.fileId}`;
          this.statusClass = response.data.status === "FAILED" ? "alert-danger" : "alert-success";
        } catch (error) {
          console.error("Error fetching file status:", error);
          this.statusMessage = "Failed to retrieve file status.";
          this.statusClass = "alert-danger";
        } finally {
          this.isCheckingStatus = false;
        }
      },
      async getFileErrors() {
        this.isCheckingErrors = true;
        this.fileErrors = [];
  
        try {
          const response = await columnMappingApi.get(`/api/mapping/errors/${this.inputFileId}`);
          this.fileErrors = Array.isArray(response.data) && response.data.length > 0 ? response.data : ["No errors found."];
        } catch (error) {
          console.error("Error fetching file errors:", error);
          this.fileErrors = ["Failed to retrieve errors."];
        } finally {
          this.isCheckingErrors = false;
        }
      },
      startPollingStatus() {
        if (!this.inputFileId) return;
  
        this.statusMessage = "Processing file for data validation errors...";
        this.statusClass = "alert-info";
  
        this.pollingInterval = setInterval(async () => {
          try {
            const response = await columnMappingApi.get(`/api/mapping/status/${this.inputFileId}`);
  
            this.statusMessage = `File Status: ${response.data.status}; Error Count: ${response.data.errorCount}; File ID: ${response.data.fileId}`;
            this.statusClass = response.data.status === "FAILED" ? "alert-danger" : "alert-success";
  
            // Stop polling if status is no longer "PROCESSING"
            if (response.data.status !== "PROCESSING") {
              clearInterval(this.pollingInterval);
              this.pollingInterval = null;
            }
          } catch (error) {
            console.error("Error checking file status:", error);
            this.statusMessage = "Error retrieving status.";
            this.statusClass = "alert-danger";
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
          }
        }, 2000);
      },
    },
    beforeUnmount() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
      }
    }
  };
  </script>
  
  <style scoped>
  .spinner-border {
    vertical-align: middle;
  }
  </style>
  