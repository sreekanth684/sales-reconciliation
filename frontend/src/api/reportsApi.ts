import axios from 'axios'

const reportsApi = axios.create({
  baseURL: 'http://localhost:8084/', // Column Mapping Service
  headers: {
    'Content-Type': 'application/json'
  }
})

export default reportsApi