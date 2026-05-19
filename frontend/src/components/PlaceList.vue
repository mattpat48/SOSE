<template>
  <div class="place-list">
    <div v-if="places.length === 0" class="empty-state">
      <p>No places found. Try adjusting your search criteria.</p>
    </div>

    <div v-else class="places-grid">
      <div 
        v-for="place in places" 
        :key="place.uri"
        class="place-card"
        @click="selectPlace(place)"
      >
        <div class="place-header">
          <h3>{{ place.name }}</h3>
          <span class="rating">⭐ {{ place.rating }}</span>
        </div>

        <div class="place-body">
          <p v-if="place.description" class="description">{{ place.description }}</p>
          
          <div class="attributes">
            <div class="attribute">
              <strong>Category:</strong> {{ place.category }}
            </div>
            <div class="attribute">
              <strong>Location:</strong> {{ place.location }}
            </div>
            <div class="attribute" :class="place.accessibility.toLowerCase()">
              <strong>♿ Accessibility:</strong> {{ place.accessibility }}
            </div>
            <div class="attribute">
              <strong>🌱 Sustainability:</strong> {{ place.sustainabilityLevel }}
            </div>
            <div class="attribute">
              <strong>👥 Crowding:</strong> {{ place.crowdingLevel }}
            </div>
          </div>
        </div>

        <div class="place-footer">
          <button class="view-button">View Details →</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlaceList',
  props: {
    places: {
      type: Array,
      default: () => []
    }
  },
  emits: ['select'],
  methods: {
    selectPlace(place) {
      this.$emit('select', place)
    }
  }
}
</script>

<style scoped>
.place-list {
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 1.1em;
}

.places-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.place-card {
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.place-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.place-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: start;
}

.place-header h3 {
  margin: 0;
  font-size: 1.3em;
}

.rating {
  font-size: 1.2em;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
}

.place-body {
  padding: 15px;
}

.description {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 0.95em;
  line-height: 1.4;
}

.attributes {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attribute {
  font-size: 0.9em;
  color: #555;
  padding: 6px;
  background: #f9f9f9;
  border-radius: 4px;
}

.attribute strong {
  color: #333;
}

.attribute.wheelchairaccessible {
  background: #e8f5e9;
}

.attribute.partiallywheelchairaccessible {
  background: #fff3e0;
}

.attribute.notwheelchairaccessible {
  background: #ffebee;
}

.place-footer {
  padding: 15px;
  border-top: 1px solid #eee;
}

.view-button {
  width: 100%;
  background: #667eea;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.view-button:hover {
  background: #764ba2;
}
</style>
