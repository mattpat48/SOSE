<template>
  <div class="place-list">
    <div
      v-if="places.length === 0"
      class="state-block"
    >
      <SearchX :size="24" />
      <span>No results available.</span>
    </div>

    <button
      v-for="place in places"
      v-else
      :key="place.uri"
      class="place-card"
      :class="{ selected: place.uri === selectedUri, warning: place.needsRevision }"
      type="button"
      @click="$emit('select', place)"
    >
      <span class="category-icon">
        <component
          :is="categoryIcon(place.category)"
          :size="19"
        />
      </span>

      <span class="place-copy">
        <strong>{{ place.name }}</strong>
        <small>
          <MapPin :size="14" />
          {{ place.location }} · {{ place.category }}
        </small>
      </span>

      <span class="metric-pill">
        <Star :size="14" />
        {{ formatNumber(place.rating) }}
      </span>

      <span class="status-line">
        <span :class="accessibilityClass(place.accessibility)">
          <Accessibility :size="14" />
          {{ compactAccessibility(place.accessibility) }}
        </span>
        <span :class="sustainabilityClass(place.sustainabilityLevel)">
          <Leaf :size="14" />
          {{ compactSustainability(place.sustainabilityLevel) }}
        </span>
        <span
          v-if="place.needsRevision"
          class="warning-pill"
        >
          <TriangleAlert :size="14" />
          Revise
        </span>
      </span>
    </button>
  </div>
</template>

<script>
import {
  Accessibility,
  Building2,
  Church,
  Landmark,
  Leaf,
  MapPin,
  Mountain,
  SearchX,
  Star,
  Store,
  TriangleAlert
} from '@lucide/vue'

export default {
  name: 'PlaceList',
  components: {
    Accessibility,
    Building2,
    Church,
    Landmark,
    Leaf,
    MapPin,
    Mountain,
    SearchX,
    Star,
    Store,
    TriangleAlert
  },
  props: {
    places: {
      type: Array,
      default: () => []
    },
    selectedUri: {
      type: String,
      default: null
    }
  },
  emits: ['select'],
  methods: {
    categoryIcon(category) {
      const icons = {
        Museum: Building2,
        Restaurant: Store,
        NaturalSite: Mountain,
        Landmark,
        Church
      }
      return icons[category] || Landmark
    },
    formatNumber(value) {
      return Number.isFinite(value) ? value.toFixed(1) : 'n/a'
    },
    compactAccessibility(value) {
      return {
        WheelchairAccessible: 'Accessible',
        PartiallyWheelchairAccessible: 'Partial',
        NotWheelchairAccessible: 'Limited'
      }[value] || value || 'n/a'
    },
    compactSustainability(value) {
      return {
        HighlySustainable: 'High',
        Sustainable: 'Good',
        ModeratelySustainable: 'Medium',
        LowSustainability: 'Low'
      }[value] || value || 'n/a'
    },
    accessibilityClass(value) {
      return {
        'signal good': value === 'WheelchairAccessible',
        'signal medium': value === 'PartiallyWheelchairAccessible',
        'signal bad': value === 'NotWheelchairAccessible'
      }
    },
    sustainabilityClass(value) {
      return {
        'signal good': ['HighlySustainable', 'Sustainable'].includes(value),
        'signal medium': value === 'ModeratelySustainable',
        'signal bad': value === 'LowSustainability'
      }
    }
  }
}
</script>
