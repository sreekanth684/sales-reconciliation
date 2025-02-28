<template>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
      <div class="container">
        <router-link class="navbar-brand" to="/">Sales Reconciliation App</router-link>
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav">
            <li class="nav-item">
              <router-link class="nav-link" to="/">Upload File</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/mapping">Column Mapping</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/reports">Reports</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </template>
  
  <style scoped>
  nav {
    width: 100%;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1030;
  }
  </style>
  