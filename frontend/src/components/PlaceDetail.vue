<template>
  <aside class="detail-panel">
    <div
      v-if="!place"
      class="empty-detail"
    >
      <MapPinned :size="34" />
      <h3>Seleziona un luogo</h3>
      <p>I dettagli RDF, la provenienza e le azioni etiche appariranno qui.</p>
    </div>

    <template v-else>
      <div class="detail-title">
        <span class="detail-icon">
          <component
            :is="categoryIcon(place.category)"
            :size="22"
          />
        </span>
        <div>
          <span class="eyebrow">{{ place.category }} · {{ place.location }}</span>
          <h3>{{ place.name }}</h3>
        </div>
      </div>

      <p
        v-if="place.description"
        class="description"
      >
        {{ place.description }}
      </p>

      <div class="score-strip">
        <div>
          <Star :size="18" />
          <span>Rating</span>
          <strong>{{ formatNumber(place.rating) }}</strong>
        </div>
        <div>
          <Scale :size="18" />
          <span>Ethics</span>
          <strong>{{ formatNumber(place.ethicalRating) }}</strong>
        </div>
        <div>
          <Users :size="18" />
          <span>Crowding</span>
          <strong>{{ place.crowdingLevel || 'n/a' }}</strong>
        </div>
      </div>

      <div class="detail-list">
        <div class="detail-row">
          <Accessibility :size="18" />
          <span>Accessibilita</span>
          <strong>{{ place.accessibility || 'n/a' }}</strong>
        </div>
        <div class="detail-row">
          <Leaf :size="18" />
          <span>Sostenibilita</span>
          <strong>{{ place.sustainabilityLevel || 'n/a' }}</strong>
        </div>
        <div class="detail-row">
          <DatabaseZap :size="18" />
          <span>Fonte</span>
          <strong>{{ place.provenance || 'n/a' }}</strong>
        </div>
        <div class="detail-row">
          <CalendarClock :size="18" />
          <span>Aggiornato</span>
          <strong>{{ formatDate(place.lastUpdated) }}</strong>
        </div>
      </div>

      <div class="context-panel">
        <span class="eyebrow">User context</span>
        <div class="check-grid">
          <label
            v-for="pref in preferenceOptions"
            :key="pref.value"
            class="check-row"
          >
            <input
              v-model="context.preferences"
              type="checkbox"
              :value="pref.value"
            >
            <span><component
              :is="pref.icon"
              :size="15"
            /> {{ pref.label }}</span>
          </label>
        </div>
        <label class="check-row wide">
          <input
            v-model="context.accessibilityNeeds"
            type="checkbox"
            value="wheelchair"
          >
          <span><Accessibility :size="15" /> Necessita wheelchair</span>
        </label>
      </div>

      <div class="detail-actions">
        <button
          class="secondary-action"
          type="button"
          :disabled="loading"
          @click="evaluatePlace"
        >
          <ShieldCheck :size="18" />
          Valuta EaaS
        </button>
        <button
          class="primary-action"
          type="button"
          :disabled="loading"
          @click="requestRecommendation"
        >
          <Sparkles :size="18" />
          Raccomanda
        </button>
      </div>
    </template>
  </aside>
</template>

<script>
import {
  Accessibility,
  Building2,
  CalendarClock,
  Church,
  DatabaseZap,
  Landmark,
  Leaf,
  MapPinned,
  Mountain,
  Scale,
  ShieldCheck,
  Sparkles,
  Star,
  Store,
  Trees,
  Users,
  Utensils
} from '@lucide/vue'

export default {
  name: 'PlaceDetail',
  components: {
    Accessibility,
    Building2,
    CalendarClock,
    Church,
    DatabaseZap,
    Landmark,
    Leaf,
    MapPinned,
    Mountain,
    Scale,
    ShieldCheck,
    Sparkles,
    Star,
    Store,
    Trees,
    Users,
    Utensils
  },
  props: {
    place: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['request-recommendation', 'evaluate-place'],
  data() {
    return {
      context: {
        preferences: ['cultural', 'sustainable'],
        accessibilityNeeds: ['wheelchair']
      },
      preferenceOptions: [
        { value: 'cultural', label: 'Culturale', icon: Building2 },
        { value: 'sustainable', label: 'Sostenibile', icon: Leaf },
        { value: 'nature', label: 'Natura', icon: Trees },
        { value: 'food', label: 'Food', icon: Utensils }
      ]
    }
  },
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
    formatDate(dateString) {
      if (!dateString) return 'n/a'
      const date = new Date(dateString)
      if (Number.isNaN(date.getTime())) return dateString
      return date.toLocaleDateString('it-IT', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },
    requestRecommendation() {
      this.$emit('request-recommendation', { ...this.context })
    },
    evaluatePlace() {
      this.$emit('evaluate-place', { ...this.context })
    }
  }
}
</script>
