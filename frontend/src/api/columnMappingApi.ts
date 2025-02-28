import axios from 'axios'

const columnMappingApi = axios.create({
  baseURL: 'http://localhost:8082/', // Column Mapping Service
  headers: {
    'Content-Type': 'application/json'
  }
})

export default columnMappingApi
