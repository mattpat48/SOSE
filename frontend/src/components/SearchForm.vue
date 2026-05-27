<template>
  <form
    class="search-panel"
    @submit.prevent="submit"
  >
    <div class="panel-heading">
      <div>
        <span class="eyebrow">Query DaaS</span>
        <h3>Ricerca turismo</h3>
      </div>
    </div>

    <div class="field-grid">
      <label class="field">
        <span><Type :size="15" /> Nome</span>
        <input
          v-model.trim="criteria.location"
          type="text"
          placeholder="Citta, luogo o descrizione"
        >
      </label>

      <label class="field">
        <span><Shapes :size="15" /> Categoria</span>
        <select v-model="criteria.category">
          <option value="All">Tutte</option>
          <option value="Museum">Museum</option>
          <option value="Restaurant">Restaurant</option>
          <option value="NaturalSite">NaturalSite</option>
          <option value="Landmark">Landmark</option>
          <option value="Church">Church</option>
        </select>
      </label>

      <label class="field">
        <span><Star :size="15" /> Rating minimo</span>
        <input
          v-model.number="criteria.minRating"
          type="number"
          min="0"
          max="5"
          step="0.1"
        >
      </label>
    </div>

    <div
      class="mode-row"
      role="group"
      aria-label="Tipo di ricerca"
    >
      <button
        class="segment"
        :class="{ active: mode === 'basic' }"
        type="button"
        @click="mode = 'basic'"
      >
        <Search :size="16" />
        Base
      </button>
      <button
        class="segment"
        :class="{ active: mode === 'ethical' }"
        type="button"
        @click="mode = 'ethical'"
      >
        <ShieldCheck :size="16" />
        Etica
      </button>
    </div>

    <div
      v-if="mode === 'ethical'"
      class="ethical-fields"
    >
      <label class="field">
        <span><Accessibility :size="15" /> Accessibilita</span>
        <select v-model="criteria.accessibility">
          <option value="WheelchairAccessible">WheelchairAccessible</option>
          <option value="PartiallyWheelchairAccessible">PartiallyWheelchairAccessible</option>
          <option value="NotWheelchairAccessible">NotWheelchairAccessible</option>
        </select>
      </label>

      <label class="field">
        <span><Leaf :size="15" /> Sostenibilita</span>
        <select v-model="criteria.sustainability">
          <option value="HighlySustainable">HighlySustainable</option>
          <option value="Sustainable">Sustainable</option>
          <option value="ModeratelySustainable">ModeratelySustainable</option>
          <option value="LowSustainability">LowSustainability</option>
        </select>
      </label>
    </div>

    <button
      class="primary-action"
      type="submit"
      :disabled="loading"
    >
      <LoaderCircle
        v-if="loading"
        class="spin"
        :size="18"
      />
      <Search
        v-else
        :size="18"
      />
      Cerca luoghi
    </button>
  </form>
</template>

<script>
import {
  Accessibility,
  Leaf,
  LoaderCircle,
  Search,
  Shapes,
  ShieldCheck,
  Star,
  Type
} from '@lucide/vue'

const DEFAULT_CRITERIA = {
  location: '',
  category: 'All',
  accessibility: 'WheelchairAccessible',
  sustainability: 'Sustainable',
  minRating: 4.0
}

export default {
  name: 'SearchForm',
  components: {
    Accessibility,
    Leaf,
    LoaderCircle,
    Search,
    Shapes,
    ShieldCheck,
    Star,
    Type
  },
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    initialCriteria: {
      type: Object,
      default: null
    }
  },
  emits: ['search-basic', 'search-ethical'],
  data() {
    const init = this.initialCriteria || {}
    return {
      mode: init.mode || 'basic',
      criteria: {
        location: init.location ?? DEFAULT_CRITERIA.location,
        category: init.category ?? DEFAULT_CRITERIA.category,
        accessibility: init.accessibility ?? DEFAULT_CRITERIA.accessibility,
        sustainability: init.sustainability ?? DEFAULT_CRITERIA.sustainability,
        minRating: init.minRating ?? DEFAULT_CRITERIA.minRating
      }
    }
  },
  methods: {
    submit() {
      if (this.mode === 'ethical') {
        this.$emit('search-ethical', { ...this.criteria })
        return
      }

      this.$emit('search-basic', {
        location: this.criteria.location,
        category: this.criteria.category,
        minRating: this.criteria.minRating
      })
    }
  }
}
</script>
