import axios from 'axios'

const fileUploadApi = axios.create({
  baseURL: 'http://localhost:8081/', // File Upload Service
  headers: {
    'Content-Type': 'application/json'
  }
})

export default fileUploadApi
