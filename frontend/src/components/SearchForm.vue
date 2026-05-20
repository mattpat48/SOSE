<template>
  <div class="search-form">
    <h2>Tourism Search</h2>

    <form @submit.prevent="submitBasic">
      <div class="form-group">
        <label for="location">City:</label>
        <input v-model="criteria.location" id="location" type="text" placeholder="e.g. Venice">
      </div>

      <div class="form-group">
        <label for="category">Category:</label>
        <select v-model="criteria.category" id="category">
          <option value="Museum">Museum</option>
          <option value="Restaurant">Restaurant</option>
          <option value="NaturalSite">Natural Site</option>
          <option value="Landmark">Landmark</option>
          <option value="Church">Church</option>
        </select>
      </div>

      <div class="form-group">
        <label for="minRating">Minimum Rating:</label>
        <input v-model.number="criteria.minRating" type="number" id="minRating" min="0" max="5" step="0.1">
      </div>

      <button type="submit" class="submit-button">Search Places</button>
    </form>

    <div class="ethical-section">
      <h3>Ethical Search (Optional)</h3>
      <p>Use additional accessibility and sustainability filters only when you need ethical pre-filtering.</p>

      <form @submit.prevent="submitEthical">
        <div class="form-group">
          <label for="accessibility">Accessibility:</label>
          <select v-model="criteria.accessibility" id="accessibility">
            <option value="WheelchairAccessible">Wheelchair Accessible</option>
            <option value="PartiallyWheelchairAccessible">Partially Accessible</option>
            <option value="NotWheelchairAccessible">Not Accessible</option>
          </select>
        </div>

        <div class="form-group">
          <label for="sustainability">Sustainability:</label>
          <select v-model="criteria.sustainability" id="sustainability">
            <option value="HighlySustainable">Highly Sustainable</option>
            <option value="Sustainable">Sustainable</option>
            <option value="ModeratelySustainable">Moderately Sustainable</option>
            <option value="LowSustainability">Low Sustainability</option>
          </select>
        </div>

        <button type="submit" class="submit-button ethical-button">Search With Ethical Filters</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchForm',
  emits: ['search-basic', 'search-ethical'],
  data() {
    return {
      criteria: {
        location: 'Venice',
        category: 'Museum',
        accessibility: 'WheelchairAccessible',
        sustainability: 'Sustainable',
        minRating: 4.0
      }
    }
  },
  methods: {
    submitBasic() {
      this.$emit('search-basic', {
        location: this.criteria.location,
        category: this.criteria.category,
        minRating: this.criteria.minRating
      })
    },
    submitEthical() {
      this.$emit('search-ethical', this.criteria)
    }
  }
}
</script>

<style scoped>
.search-form {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.search-form h2 {
  margin-bottom: 20px;
  color: #333;
}

form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.ethical-section {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #ddd;
}

.ethical-section h3 {
  margin: 0 0 6px 0;
  color: #333;
}

.ethical-section p {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 0.95em;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #555;
}

select,
input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1em;
  background: white;
}

select:focus,
input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.submit-button {
  grid-column: 1 / -1;
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1.1em;
  font-weight: 600;
  transition: background 0.3s;
}

.submit-button:hover {
  background: #764ba2;
}

.ethical-button {
  background: #2f7d32;
}

.ethical-button:hover {
  background: #256428;
}
</style>
