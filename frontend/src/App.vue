<template>
  <div class="app-shell">
    <aside
      class="sidebar"
      aria-label="Stato servizi"
    >
      <div class="brand">
        <div class="brand-mark">
          <Compass :size="24" />
        </div>
        <div>
          <span class="eyebrow">SOSE Tourism</span>
          <h1>DaaS + EaaS</h1>
        </div>
      </div>

      <div class="service-stack">
        <div class="service-row">
          <Database :size="17" />
          <span>DaaS</span>
          <strong :class="statusClass(services.daas)">{{ serviceLabel(services.daas) }}</strong>
        </div>
        <div class="service-row">
          <ShieldCheck :size="17" />
          <span>EaaS</span>
          <strong :class="statusClass(services.eaas)">{{ serviceLabel(services.eaas) }}</strong>
        </div>
        <button
          class="service-refresh"
          type="button"
          @click="checkServices"
        >
          <RefreshCcw :size="16" />
          <span>Aggiorna servizi</span>
        </button>
      </div>
    </aside>

    <main class="workspace">
      <!-- Stage: hero search -->
      <section
        v-if="stage === 'search'"
        class="hero-stage"
      >
        <div class="hero-copy">
          <span class="eyebrow">Tourism recommendation engine</span>
          <h2>Cerca luoghi etici e sostenibili.</h2>
          <p>Filtra per nome, categoria o rating. Apri il dettaglio di un luogo per generare l'Ethical Report.</p>
        </div>

        <SearchForm
          :loading="loading"
          :initial-criteria="lastCriteria"
          @search-basic="handleBasicSearch"
          @search-ethical="handleEthicalSearch"
        />

        <div
          v-if="error"
          class="state-block error-state"
        >
          <CircleAlert :size="20" />
          <span>{{ error }}</span>
        </div>
      </section>

      <!-- Stage: results -->
      <section
        v-else-if="stage === 'results'"
        class="results-stage"
      >
        <header class="stage-toolbar">
          <button
            class="back-link"
            type="button"
            @click="backToSearch"
          >
            <ArrowLeft :size="18" />
            <span>Nuova ricerca</span>
          </button>
          <span class="eyebrow stage-label">{{ searchModeLabel }}</span>
          <span class="count-badge">{{ places.length }}</span>
        </header>

        <div class="results-panel">
          <div
            v-if="loading"
            class="state-block"
          >
            <LoaderCircle
              class="spin"
              :size="24"
            />
            <span>Caricamento dati...</span>
          </div>

          <div
            v-else-if="error"
            class="state-block error-state"
          >
            <CircleAlert :size="24" />
            <span>{{ error }}</span>
          </div>

          <PlaceList
            v-else
            :places="places"
            :selected-uri="selectedPlace?.uri"
            @select="selectPlace"
          />
        </div>
      </section>

      <!-- Stage: detail with inline ethical report -->
      <section
        v-else
        class="detail-stage"
      >
        <header class="stage-toolbar">
          <button
            class="back-link"
            type="button"
            @click="backToResults"
          >
            <ArrowLeft :size="18" />
            <span>Torna ai risultati</span>
          </button>
          <span class="eyebrow stage-label">Dettaglio luogo</span>
        </header>

        <PlaceDetail
          :place="selectedPlace"
          :loading="evaluationLoading"
          :ethical-decision="ethicalDecision"
          :decision-source="decisionSource"
          :ethical-error="ethicalError"
          @request-ethical-report="handleEthicalReport"
        />
      </section>
    </main>
  </div>
</template>

<script>
import axios from 'axios'
import {
  ArrowLeft,
  CircleAlert,
  Compass,
  Database,
  LoaderCircle,
  RefreshCcw,
  ShieldCheck
} from '@lucide/vue'
import SearchForm from './components/SearchForm.vue'
import PlaceList from './components/PlaceList.vue'
import PlaceDetail from './components/PlaceDetail.vue'

const DAAS_BASE_URL = 'http://localhost:8080/daas/api'
const EAAS_BASE_URL = 'http://localhost:8081/eaas/api'

export default {
  name: 'App',
  components: {
    ArrowLeft,
    CircleAlert,
    Compass,
    Database,
    LoaderCircle,
    RefreshCcw,
    ShieldCheck,
    SearchForm,
    PlaceList,
    PlaceDetail
  },
  data() {
    return {
      stage: 'search',
      places: [],
      selectedPlace: null,
      ethicalDecision: null,
      decisionSource: '',
      ethicalError: null,
      searchModeLabel: '',
      lastCriteria: null,
      loading: false,
      evaluationLoading: false,
      error: null,
      services: {
        daas: 'checking',
        eaas: 'checking'
      }
    }
  },
  mounted() {
    this.checkServices()
  },
  methods: {
    serviceLabel(status) {
      return status === 'online' ? 'online' : status === 'offline' ? 'offline' : 'check'
    },
    statusClass(status) {
      return {
        online: status === 'online',
        offline: status === 'offline',
        checking: status === 'checking'
      }
    },
    async checkServices() {
      this.services = { daas: 'checking', eaas: 'checking' }
      const [daas, eaas] = await Promise.allSettled([
        axios.get(`${DAAS_BASE_URL}/places/health`, { timeout: 2500 }),
        axios.get(`${EAAS_BASE_URL}/evaluate/health`, { timeout: 2500 })
      ])
      this.services.daas = daas.status === 'fulfilled' ? 'online' : 'offline'
      this.services.eaas = eaas.status === 'fulfilled' ? 'online' : 'offline'
    },
    async handleBasicSearch(criteria) {
      this.lastCriteria = { mode: 'basic', ...criteria }
      await this.runSearch('Ricerca base', `${DAAS_BASE_URL}/places/search/basic`, {
        location: criteria.location,
        category: criteria.category,
        minRating: criteria.minRating
      })
    },
    async handleEthicalSearch(criteria) {
      this.lastCriteria = { mode: 'ethical', ...criteria }
      await this.runSearch('Filtri etici', `${DAAS_BASE_URL}/places/search/ethical`, {
        location: criteria.location,
        category: criteria.category,
        accessibility: criteria.accessibility,
        sustainability: criteria.sustainability,
        minRating: criteria.minRating
      }, criteria)
    },
    async runSearch(label, url, params, requestedCriteria = null) {
      this.loading = true
      this.error = null
      this.searchModeLabel = label
      this.selectedPlace = null
      this.ethicalDecision = null
      this.ethicalError = null

      try {
        const response = await axios.get(url, { params })
        this.places = requestedCriteria
          ? response.data.map((place) => this.withRevisionFlag(place, requestedCriteria))
          : response.data
        if (this.places.length === 0) {
          this.error = 'Nessun luogo trovato con questi criteri.'
          this.stage = 'search'
        } else {
          this.stage = 'results'
        }
      } catch (err) {
        this.error = this.extractError(err, 'Errore durante la ricerca')
        this.stage = 'search'
      } finally {
        this.loading = false
      }
    },
    withRevisionFlag(place, criteria) {
      const partialAccess = criteria.accessibility === 'WheelchairAccessible'
        && place.accessibility === 'PartiallyWheelchairAccessible'
      const lowerSustainability = ['Sustainable', 'HighlySustainable'].includes(criteria.sustainability)
        && place.sustainabilityLevel === 'ModeratelySustainable'
      return { ...place, needsRevision: partialAccess || lowerSustainability }
    },
    selectPlace(place) {
      this.selectedPlace = place
      this.ethicalDecision = null
      this.decisionSource = ''
      this.ethicalError = null
      this.stage = 'detail'
    },
    backToResults() {
      this.stage = 'results'
      this.ethicalDecision = null
      this.ethicalError = null
    },
    backToSearch() {
      this.stage = 'search'
      this.places = []
      this.selectedPlace = null
      this.ethicalDecision = null
      this.ethicalError = null
      this.error = null
    },
    async handleEthicalReport() {
      if (!this.selectedPlace) return
      this.evaluationLoading = true
      this.ethicalError = null

      try {
        const response = await axios.post(`${EAAS_BASE_URL}/evaluate`, {
          placeData: this.selectedPlace
        })
        this.ethicalDecision = {
          ...response.data,
          placeName: response.data.placeName || this.selectedPlace.name,
          candidate: this.selectedPlace
        }
        this.decisionSource = 'Valutazione EaaS'
      } catch (err) {
        this.ethicalError = this.extractError(err, 'Errore nella valutazione EaaS')
      } finally {
        this.evaluationLoading = false
      }
    },
    extractError(err, fallback) {
      return err.response?.data?.message || err.response?.data?.rationale || err.message || fallback
    }
  }
}
</script>
