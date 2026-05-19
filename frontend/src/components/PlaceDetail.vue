<template>
  <div class="place-detail">
    <div class="detail-header">
      <h2>{{ place.name }}</h2>
      <div class="detail-meta">
        <span class="rating">⭐ Rating: {{ place.rating }}</span>
        <span class="ethical">Ethics: {{ place.ethicalRating }}</span>
      </div>
    </div>

    <div class="detail-description">
      <p v-if="place.description">{{ place.description }}</p>
    </div>

    <div class="detail-grid">
      <div class="detail-section">
        <h3>Location & Category</h3>
        <div class="detail-item">
          <strong>Location:</strong> {{ place.location }}
        </div>
        <div class="detail-item">
          <strong>Category:</strong> {{ place.category }}
        </div>
      </div>

      <div class="detail-section">
        <h3>Accessibility</h3>
        <div class="detail-item" :class="place.accessibility.toLowerCase()">
          {{ place.accessibility }}
        </div>
      </div>

      <div class="detail-section">
        <h3>Sustainability</h3>
        <div class="detail-item">
          {{ place.sustainabilityLevel }}
        </div>
      </div>

      <div class="detail-section">
        <h3>Crowding Level</h3>
        <div class="detail-item">
          {{ place.crowdingLevel }}
        </div>
      </div>

      <div class="detail-section">
        <h3>Data Source</h3>
        <div class="detail-item">
          <strong>Provenance:</strong> {{ place.provenance }}
        </div>
        <div class="detail-item">
          <strong>Last Updated:</strong> {{ formatDate(place.lastUpdated) }}
        </div>
      </div>
    </div>

    <div class="detail-actions">
      <button class="recommend-button" @click="requestRecommendation">
        🔍 Request Ethical Recommendation
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlaceDetail',
  props: {
    place: {
      type: Object,
      required: true
    }
  },
  emits: ['request-recommendation'],
  methods: {
    formatDate(dateString) {
      if (!dateString) return 'N/A'
      try {
        const date = new Date(dateString)
        return date.toLocaleDateString('it-IT', { 
          year: 'numeric', 
          month: 'long', 
          day: 'numeric' 
        })
      } catch {
        return dateString
      }
    },
    requestRecommendation() {
      this.$emit('request-recommendation', {
        preferences: ['cultural', 'sustainable'],
        accessibilityNeeds: ['wheelchair']
      })
    }
  }
}
</script>

<style scoped>
.place-detail {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.detail-header {
  margin-bottom: 30px;
  border-bottom: 2px solid #eee;
  padding-bottom: 20px;
}

.detail-header h2 {
  color: #333;
  margin: 0 0 15px 0;
  font-size: 2em;
}

.detail-meta {
  display: flex;
  gap: 20px;
  font-weight: 600;
}

.rating {
  color: #ff9800;
}

.ethical {
  color: #4caf50;
}

.detail-description {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 30px;
  line-height: 1.6;
  color: #555;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.detail-section {
  background: #fafafa;
  padding: 20px;
  border-radius: 6px;
  border-left: 4px solid #667eea;
}

.detail-section h3 {
  color: #667eea;
  margin: 0 0 15px 0;
  font-size: 1.1em;
}

.detail-item {
  margin: 10px 0;
  padding: 8px;
  background: white;
  border-radius: 4px;
  color: #555;
}

.detail-item strong {
  color: #333;
}

.detail-item.wheelchairaccessible {
  background: #e8f5e9;
  color: #2e7d32;
}

.detail-item.partiallywheelchairaccessible {
  background: #fff3e0;
  color: #e65100;
}

.detail-item.notwheelchairaccessible {
  background: #ffebee;
  color: #c62828;
}

.detail-actions {
  display: flex;
  gap: 10px;
}

.recommend-button {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 15px 30px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1em;
  font-weight: 600;
  transition: all 0.3s;
}

.recommend-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>
