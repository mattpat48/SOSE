<template>
  <div class="app-container">
    <header class="app-header">
      <h1>🌍 Tourism Recommendation Engine</h1>
      <p>Discover ethical and sustainable travel destinations</p>
    </header>

    <main class="app-main">
      <div class="container">
        <!-- Search Form Section -->
        <SearchForm @search-basic="handleBasicSearch" @search-ethical="handleEthicalSearch" />

        <!-- Results Section -->
        <div v-if="showResults" class="results-section">
          <div v-if="!selectedPlace" class="places-list">
            <h2>Available Places</h2>
            <PlaceList :places="places" @select="selectPlace" />
          </div>

          <div v-else class="place-detail">
            <button class="back-button" @click="selectedPlace = null">← Back to List</button>
            <PlaceDetail :place="selectedPlace" @request-recommendation="handleRecommendation" />

            <!-- Ethical Decision Section -->
            <div v-if="ethicalDecision" class="ethical-decision-section">
              <EthicalDecision :decision="ethicalDecision" />
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading">
          <p>Loading...</p>
        </div>

        <!-- Error State -->
        <div v-if="error" class="error">
          <p>⚠️ {{ error }}</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import SearchForm from './components/SearchForm.vue'
import PlaceList from './components/PlaceList.vue'
import PlaceDetail from './components/PlaceDetail.vue'
import EthicalDecision from './components/EthicalDecision.vue'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/daas'

export default {
  name: 'App',
  components: {
    SearchForm,
    PlaceList,
    PlaceDetail,
    EthicalDecision
  },
  data() {
    return {
      places: [],
      selectedPlace: null,
      ethicalDecision: null,
      showResults: false,
      loading: false,
      error: null
    }
  },
  methods: {
    async handleBasicSearch(criteria) {
      this.loading = true
      this.error = null
      this.selectedPlace = null
      this.ethicalDecision = null

      try {
        const response = await axios.get(`${API_BASE_URL}/api/places/search/basic`, {
          params: {
            location: criteria.location,
            category: criteria.category,
            minRating: criteria.minRating
          }
        })
        this.places = response.data
        this.showResults = true

        if (this.places.length === 0) {
          this.error = 'No places found matching your criteria'
        }
      } catch (err) {
        this.error = 'Error fetching places: ' + (err.response?.data?.message || err.message)
        console.error(err)
      } finally {
        this.loading = false
      }
    },

    async handleEthicalSearch(criteria) {
      this.loading = true
      this.error = null
      this.selectedPlace = null
      this.ethicalDecision = null

      try {
        const response = await axios.get(`${API_BASE_URL}/api/places/search/ethical`, {
          params: {
            location: criteria.location,
            category: criteria.category,
            accessibility: criteria.accessibility,
            sustainability: criteria.sustainability,
            minRating: criteria.minRating
          }
        })

        this.places = response.data
        this.showResults = true

        if (this.places.length === 0) {
          this.error = 'No places found matching your ethical criteria'
        }
      } catch (err) {
        this.error = 'Error fetching ethically filtered places: ' + (err.response?.data?.message || err.message)
        console.error(err)
      } finally {
        this.loading = false
      }
    },

    selectPlace(place) {
      this.selectedPlace = place
      this.ethicalDecision = null
    },

    async handleRecommendation(userPreferences) {
      this.loading = true
      this.error = null

      try {
        const response = await axios.post(`${API_BASE_URL}/api/recommendations/ethical-recommendation`, {
          category: this.selectedPlace.category,
          accessibility: this.selectedPlace.accessibility,
          sustainability: this.selectedPlace.sustainabilityLevel,
          minRating: this.selectedPlace.rating,
          userContext: {
            userId: 'user_' + Math.random().toString(36).substr(2, 9),
            preferences: userPreferences.preferences,
            accessibilityNeeds: userPreferences.accessibilityNeeds
          }
        })
        this.ethicalDecision = response.data
      } catch (err) {
        this.error = 'Error evaluating recommendation: ' + (err.response?.data?.message || err.message)
        console.error(err)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.app-container {
  max-width: 1200px;
  margin: 0 auto;
}

.app-header {
  text-align: center;
  color: white;
  margin-bottom: 40px;
  padding: 20px;
}

.app-header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.app-header p {
  font-size: 1.1em;
  opacity: 0.9;
}

.container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.results-section {
  margin-top: 30px;
}

.places-list {
  width: 100%;
}

.places-list h2 {
  color: #333;
  margin-bottom: 20px;
}

.place-detail {
  width: 100%;
}

.back-button {
  background: #667eea;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1em;
  margin-bottom: 20px;
  transition: background 0.3s;
}

.back-button:hover {
  background: #764ba2;
}

.ethical-decision-section {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 2px solid #eee;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 1.2em;
  color: #667eea;
}

.error {
  background: #fee;
  border: 1px solid #fcc;
  color: #c33;
  padding: 15px;
  border-radius: 6px;
  margin-top: 20px;
}
</style>
