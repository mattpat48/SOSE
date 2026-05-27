<template>
  <article class="detail-panel detail-card">
    <div
      v-if="!place"
      class="empty-detail"
    >
      <MapPinned :size="34" />
      <h3>Seleziona un luogo</h3>
      <p>I dettagli e il report etico appariranno qui.</p>
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

      <button
        class="primary-action ethical-cta"
        type="button"
        :disabled="loading"
        @click="$emit('request-ethical-report')"
      >
        <LoaderCircle
          v-if="loading"
          class="spin"
          :size="18"
        />
        <FileCheck2
          v-else
          :size="18"
        />
        {{ ethicalDecision ? 'Rigenera Ethical Report' : 'Genera Ethical Report' }}
      </button>

      <div
        v-if="ethicalError"
        class="state-block error-state ethical-error"
      >
        <CircleAlert :size="20" />
        <span>{{ ethicalError }}</span>
      </div>

      <EthicalDecision
        v-if="ethicalDecision"
        :decision="ethicalDecision"
        :source="decisionSource"
        class="inline-decision"
      />
    </template>
  </article>
</template>

<script>
import {
  Accessibility,
  Building2,
  CalendarClock,
  Church,
  CircleAlert,
  DatabaseZap,
  FileCheck2,
  Landmark,
  Leaf,
  LoaderCircle,
  MapPinned,
  Mountain,
  Scale,
  Star,
  Store,
  Users
} from '@lucide/vue'
import EthicalDecision from './EthicalDecision.vue'

export default {
  name: 'PlaceDetail',
  components: {
    Accessibility,
    Building2,
    CalendarClock,
    Church,
    CircleAlert,
    DatabaseZap,
    EthicalDecision,
    FileCheck2,
    Landmark,
    Leaf,
    LoaderCircle,
    MapPinned,
    Mountain,
    Scale,
    Star,
    Store,
    Users
  },
  props: {
    place: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    },
    ethicalDecision: {
      type: Object,
      default: null
    },
    decisionSource: {
      type: String,
      default: ''
    },
    ethicalError: {
      type: String,
      default: null
    }
  },
  emits: ['request-ethical-report'],
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
    }
  }
}
</script>
