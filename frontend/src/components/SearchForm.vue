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
      <button
        class="icon-button"
        type="button"
        title="Carica catalogo completo"
        @click="$emit('load-all')"
      >
        <ListRestart :size="18" />
      </button>
    </div>

    <div class="field-grid">
      <label class="field">
        <span><MapPin :size="15" /> Cerca</span>
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
  ListRestart,
  LoaderCircle,
  MapPin,
  Search,
  Shapes,
  ShieldCheck,
  Star
} from '@lucide/vue'

export default {
  name: 'SearchForm',
  components: {
    Accessibility,
    Leaf,
    ListRestart,
    LoaderCircle,
    MapPin,
    Search,
    Shapes,
    ShieldCheck,
    Star
  },
  props: {
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['search-basic', 'search-ethical', 'load-all'],
  data() {
    return {
      mode: 'basic',
      criteria: {
        location: '',
        category: 'All',
        accessibility: 'WheelchairAccessible',
        sustainability: 'Sustainable',
        minRating: 4.0
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
