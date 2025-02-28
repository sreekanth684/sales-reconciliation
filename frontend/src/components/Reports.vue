<template>
    <div class="container mt-4">
      <h2>Reconciliation Reports</h2>
  
      <!-- Date Range Selection -->
      <div class="row g-3 align-items-end">
        <div class="col-md-4">
          <label for="startDate" class="form-label">Start Date:</label>
          <input id="startDate" type="date" class="form-control" v-model="startDate" />
        </div>
        <div class="col-md-4">
          <label for="endDate" class="form-label">End Date:</label>
          <input id="endDate" type="date" class="form-control" v-model="endDate" />
        </div>
        <div class="col-md-4">
          <button class="btn btn-primary w-100 mt-4" @click="generateReports" :disabled="isFetching">
            <span v-if="isFetching" class="spinner-border spinner-border-sm me-2"></span>
            Generate Reports
          </button>
        </div>
      </div>
  
      <!-- Error Message -->
      <div v-if="errorMessage" class="alert alert-danger mt-3">
        {{ errorMessage }}
      </div>
  
      <!-- Gross Sales Report -->
      <div v-if="grossSalesReport" class="mt-4">
        <h5>Total Gross Sales</h5>
        <table class="table">
          <thead>
            <tr>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Total Gross Sales</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{{ grossSalesReport.startDate }}</td>
              <td>{{ grossSalesReport.endDate }}</td>
              <td>{{ grossSalesReport.totalGrossSales }}</td>
            </tr>
          </tbody>
        </table>
      </div>
  
      <!-- Sales Tax Report -->
      <div v-if="salesTaxReport" class="mt-4">
        <h5>Total Sales Tax</h5>
        <table class="table">
          <thead>
            <tr>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Total Sales Tax</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{{ salesTaxReport.startDate }}</td>
              <td>{{ salesTaxReport.endDate }}</td>
              <td>{{ salesTaxReport.totalSalesTax }}</td>
            </tr>
          </tbody>
        </table>
      </div>
  
      <!-- Totals by City Report (Paginated) -->
      <div v-if="totalsByCity.length > 0" class="mt-4">
      <h5>Aggregated Totals by City</h5>
      <table class="table">
        <thead>
          <tr>
            <th>City</th>
            <th>Total Sales</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(city, index) in totalsByCity" :key="index">
            <td>{{ city.city }}</td>
            <td>{{ city.totalSales }}</td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination Controls -->
      <div class="d-flex justify-content-between align-items-center mt-3">
        <button class="btn btn-secondary" @click="prevPage" :disabled="currentPage === 1">
          Previous
        </button>
        <span>Page {{ currentPage }} of {{ totalPages }}</span>
        <button class="btn btn-secondary" @click="nextPage" :disabled="currentPage >= totalPages">
          Next
        </button>
      </div>
    </div>
  </div>
</template>
  
  <script>
  import reportsApi from "@/api/reportsApi";
  
  export default {
    data() {
      return {
        startDate: "",
        endDate: "",
        isFetching: false,
        errorMessage: "",
        grossSalesReport: null,
        salesTaxReport: null,
        totalsByCity: [],
        currentPage: 1,
        pageSize: 5,
        totalPages: 1,
      };
    },
    methods: {
      async generateReports() {
        if (!this.startDate || !this.endDate) {
          this.errorMessage = "Please select a valid start and end date.";
          return;
        }
  
        this.isFetching = true;
        this.errorMessage = "";
        this.grossSalesReport = null;
        this.salesTaxReport = null;
        this.totalsByCity = [];
  
        try {
          // Fetch Total Gross Sales
          const grossSalesResponse = await reportsApi.get("/api/reconciliation/gross-sales", {
            params: { startDate: this.startDate, endDate: this.endDate }
          });
          this.grossSalesReport = grossSalesResponse.data;
  
          // Fetch Total Sales Tax
          const salesTaxResponse = await reportsApi.get("/api/reconciliation/sales-tax", {
            params: { startDate: this.startDate, endDate: this.endDate }
          });
          this.salesTaxReport = salesTaxResponse.data;
  
          // Fetch Totals by City (Paginated)
          await this.fetchTotalsByCity();
        } catch (error) {
          console.error("Error fetching reports:", error);
          this.errorMessage = "Failed to generate reports. Please try again.";
        } finally {
          this.isFetching = false;
        }
      },
      async fetchTotalsByCity() {
        try {
          const response = await reportsApi.get("/api/reconciliation/totals-by-city", {
            params: {
              startDate: this.startDate,
              endDate: this.endDate,
              page: this.currentPage - 1 ,
              size: this.pageSize,
            },
          });
  
          this.totalsByCity = response.data.content; // API returns a paginated `content` array
          this.totalPages = response.data.totalPages; // API should return `totalPages`
        } catch (error) {
          console.error("Error fetching totals by city:", error);
          this.errorMessage = "Failed to fetch report data.";
        }
      },
      nextPage() {
        if (this.currentPage < this.totalPages) {
          this.currentPage++;
          this.fetchTotalsByCity(); // Fetch new page
        }
      },
      prevPage() {
        if (this.currentPage > 1) {
          this.currentPage--;
          this.fetchTotalsByCity(); // Fetch previous page
        }
      },
    },
  };
  </script>
  
  <style scoped>
  .spinner-border {
    vertical-align: middle;
  }
  </style>
  