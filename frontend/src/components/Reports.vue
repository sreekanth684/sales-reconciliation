<template>
    <div class="container mt-4">
      <h2>Reconciliation Reports</h2>
      <div class="row">
        <div class="col-md-5">
          <label>Start Date:</label>
          <input type="date" class="form-control" v-model="startDate" />
        </div>
        <div class="col-md-5">
          <label>End Date:</label>
          <input type="date" class="form-control" v-model="endDate" />
        </div>
        <div class="col-md-2 d-flex align-items-end">
          <button class="btn btn-primary w-100" @click="generateReport">Generate</button>
        </div>
      </div>
      <table class="table mt-4" v-if="reportData.length > 0">
        <thead>
          <tr>
            <th>Transaction ID</th>
            <th>Date</th>
            <th>Gross Sales</th>
            <th>Tax</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in reportData" :key="row.transactionId">
            <td>{{ row.transactionId }}</td>
            <td>{{ row.date }}</td>
            <td>{{ row.grossSales }}</td>
            <td>{{ row.tax }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  import axios from 'axios'
  
  const startDate = ref('')
  const endDate = ref('')
  const reportData = ref<{ transactionId: string; date: string; grossSales: number; tax: number }[]>([])
  
  const generateReport = async () => {
    try {
      const response = await axios.get('/api/reports', { params: { startDate: startDate.value, endDate: endDate.value } })
      reportData.value = response.data
    } catch (error) {
      console.error('Failed to generate report', error)
    }
  }
  </script>
  