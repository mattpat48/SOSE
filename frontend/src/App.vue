<template>
  <div class="app-shell">
    <aside
      class="sidebar"
      aria-label="Navigazione principale"
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

      <nav class="nav-stack">
        <button
          v-for="item in navItems"
          :key="item.id"
          class="nav-item"
          :class="{ active: activeView === item.id }"
          type="button"
          @click="activeView = item.id"
        >
          <component
            :is="item.icon"
            :size="18"
          />
          <span>{{ item.label }}</span>
        </button>
      </nav>

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
      </div>
    </aside>

    <main class="workspace">
      <section class="topbar">
        <div>
          <span class="eyebrow">Tourism recommendation engine</span>
          <h2>Esplora luoghi, filtra criteri etici e valuta le policy.</h2>
        </div>
        <button
          class="icon-button"
          type="button"
          title="Aggiorna servizi"
          @click="checkServices"
        >
          <RefreshCcw :size="18" />
        </button>
      </section>

      <section
        v-if="activeView === 'explore'"
        class="content-grid"
      >
        <SearchForm
          :loading="loading"
          @search-basic="handleBasicSearch"
          @search-ethical="handleEthicalSearch"
          @load-all="loadAllPlaces"
        />

        <div class="results-panel">
          <div class="panel-heading">
            <div>
              <span class="eyebrow">{{ searchModeLabel }}</span>
              <h3>Risultati</h3>
            </div>
            <span class="count-badge">{{ places.length }}</span>
          </div>

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

        <PlaceDetail
          :place="selectedPlace"
          :loading="evaluationLoading"
          @request-recommendation="handleRecommendation"
          @evaluate-place="handleDirectEvaluation"
        />
      </section>

      <section
        v-else
        class="evaluation-view"
      >
        <EthicalDecision
          v-if="ethicalDecision"
          :decision="ethicalDecision"
          :source="decisionSource"
        />
        <div
          v-else
          class="empty-evaluation"
        >
          <ShieldQuestion :size="42" />
          <h3>Nessuna valutazione ancora generata</h3>
          <p>Seleziona un luogo dalla dashboard e avvia una valutazione EaaS o una raccomandazione etica.</p>
          <button
            class="primary-action"
            type="button"
            @click="activeView = 'explore'"
          >
            <Search :size="18" />
            Torna alla ricerca
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import axios from 'axios'
import {
  CircleAlert,
  Compass,
  Database,
  FileCheck2,
  LayoutDashboard,
  LoaderCircle,
  RefreshCcw,
  Search,
  ShieldCheck,
  ShieldQuestion
} from '@lucide/vue'
import SearchForm from './components/SearchForm.vue'
import PlaceList from './components/PlaceList.vue'
import PlaceDetail from './components/PlaceDetail.vue'
import EthicalDecision from './components/EthicalDecision.vue'

const DAAS_BASE_URL = 'http://localhost:8080/daas/api'
const EAAS_BASE_URL = 'http://localhost:8081/eaas/api'

export default {
  name: 'App',
  components: {
    CircleAlert,
    Compass,
    Database,
    FileCheck2,
    LayoutDashboard,
    LoaderCircle,
    RefreshCcw,
    Search,
    ShieldCheck,
    ShieldQuestion,
    SearchForm,
    PlaceList,
    PlaceDetail,
    EthicalDecision
  },
  data() {
    return {
      activeView: 'explore',
      places: [],
      selectedPlace: null,
      ethicalDecision: null,
      decisionSource: '',
      searchModeLabel: 'Tutti i luoghi',
      loading: false,
      evaluationLoading: false,
      error: null,
      services: {
        daas: 'checking',
        eaas: 'checking'
      },
      navItems: [
        { id: 'explore', label: 'Esplora', icon: LayoutDashboard },
        { id: 'evaluation', label: 'Valutazione', icon: FileCheck2 }
      ]
    }
  },
  mounted() {
    this.checkServices()
    this.loadAllPlaces()
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
    async loadAllPlaces() {
      this.loading = true
      this.error = null
      this.searchModeLabel = 'Catalogo completo'
      this.selectedPlace = null

      try {
        const response = await axios.get(`${DAAS_BASE_URL}/places`)
        this.places = response.data
        this.selectedPlace = this.places[0] || null
      } catch (err) {
        this.error = this.extractError(err, 'Impossibile caricare il catalogo')
      } finally {
        this.loading = false
      }
    },
    async handleBasicSearch(criteria) {
      await this.runSearch('Ricerca base', `${DAAS_BASE_URL}/places/search/basic`, {
        location: criteria.location,
        category: criteria.category,
        minRating: criteria.minRating
      })
    },
    async handleEthicalSearch(criteria) {
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

      try {
        const response = await axios.get(url, { params })
        this.places = requestedCriteria
          ? response.data.map((place) => this.withRevisionFlag(place, requestedCriteria))
          : response.data
        this.selectedPlace = this.places[0] || null
        if (this.places.length === 0) {
          this.error = 'Nessun luogo trovato con questi criteri.'
        }
      } catch (err) {
        this.error = this.extractError(err, 'Errore durante la ricerca')
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
    },
    async handleRecommendation(context) {
      if (!this.selectedPlace) return
      this.evaluationLoading = true
      this.error = null

      try {
        const response = await axios.post(`${DAAS_BASE_URL}/recommendations/ethical-recommendation`, {
          category: this.selectedPlace.category,
          accessibility: this.selectedPlace.accessibility,
          sustainability: this.selectedPlace.sustainabilityLevel,
          minRating: this.selectedPlace.rating,
          userContext: {
            userId: `user_${Date.now()}`,
            preferences: context.preferences,
            accessibilityNeeds: context.accessibilityNeeds
          }
        })
        this.ethicalDecision = response.data
        this.decisionSource = 'Raccomandazione DaaS + EaaS'
        this.activeView = 'evaluation'
      } catch (err) {
        this.error = this.extractError(err, 'Errore nella raccomandazione etica')
      } finally {
        this.evaluationLoading = false
      }
    },
    async handleDirectEvaluation(context) {
      if (!this.selectedPlace) return
      this.evaluationLoading = true
      this.error = null

      try {
        const response = await axios.post(`${EAAS_BASE_URL}/evaluate`, {
          placeData: this.selectedPlace,
          userContext: {
            userId: `user_${Date.now()}`,
            preferences: context.preferences,
            accessibilityNeeds: context.accessibilityNeeds
          }
        })
        this.ethicalDecision = {
          ...response.data,
          placeName: response.data.placeName || this.selectedPlace.name,
          candidate: this.selectedPlace
        }
        this.decisionSource = 'Valutazione diretta EaaS'
        this.activeView = 'evaluation'
      } catch (err) {
        this.error = this.extractError(err, 'Errore nella valutazione EaaS')
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
