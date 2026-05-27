<template>
  <article class="decision-page">
    <div
      class="decision-hero"
      :class="decisionTone"
    >
      <div>
        <span class="eyebrow">{{ source || 'EaaS' }}</span>
        <h3>{{ decision.placeName || decision.candidate?.name || 'Ethical evaluation' }}</h3>
        <p>{{ decision.rationale || 'No rationale available.' }}</p>
      </div>
      <div class="decision-token">
        <component
          :is="decisionIcon"
          :size="24"
        />
        <strong>{{ decision.decision || 'UNKNOWN' }}</strong>
        <span>Risk {{ decision.riskLevel || 'n/a' }}</span>
      </div>
    </div>

    <div class="decision-metrics">
      <div>
        <Fingerprint :size="18" />
        <span>Evaluation ID</span>
        <strong>{{ decision.evaluationId || 'n/a' }}</strong>
      </div>
      <div>
        <MapPin :size="18" />
        <span>Candidate</span>
        <strong>{{ decision.candidate?.name || decision.placeName || 'n/a' }}</strong>
      </div>
      <div>
        <Shield :size="18" />
        <span>Decision</span>
        <strong>{{ decision.decision || 'n/a' }}</strong>
      </div>
    </div>

    <section class="policy-section">
      <div class="section-heading">
        <h4>Applied policies</h4>
        <span>{{ policies.length }}</span>
      </div>

      <div
        v-if="policies.length === 0"
        class="state-block"
      >
        <FileQuestion :size="22" />
        <span>No policies returned.</span>
      </div>

      <div
        v-else
        class="policy-grid"
      >
        <div
          v-for="policy in policies"
          :key="policy.policyId"
          class="policy-card"
          :class="riskTone(policy.riskAssessment)"
        >
          <ShieldCheck :size="18" />
          <strong>{{ policy.policyName || policy.policyId }}</strong>
          <span>{{ policy.domain || 'policy' }}</span>
          <em>{{ policy.riskAssessment || 'n/a' }}</em>
        </div>
      </div>
    </section>

    <section class="audit-section">
      <button
        class="audit-toggle"
        type="button"
        @click="showAudit = !showAudit"
      >
        <ListChecks :size="18" />
        Audit trail
        <ChevronDown
          :class="{ open: showAudit }"
          :size="18"
        />
      </button>

      <div
        v-if="showAudit"
        class="audit-table"
      >
        <div class="audit-row audit-head">
          <span>Policy</span>
          <span>Condition</span>
          <span>Result</span>
          <span>Risk</span>
        </div>
        <div
          v-for="(trail, index) in auditTrail"
          :key="index"
          class="audit-row"
        >
          <code>{{ trail.policyId }}</code>
          <span>{{ trail.conditionEvaluated || 'n/a' }}</span>
          <strong :class="trail.result ? 'good-text' : 'bad-text'">
            {{ trail.result ? 'TRUE' : 'FALSE' }}
          </strong>
          <em :class="riskTone(trail.riskLevel)">{{ trail.riskLevel || 'n/a' }}</em>
        </div>
      </div>
    </section>
  </article>
</template>

<script>
import {
  CheckCircle2,
  ChevronDown,
  CircleSlash,
  FileQuestion,
  Fingerprint,
  ListChecks,
  MapPin,
  RotateCcw,
  Shield,
  ShieldAlert,
  ShieldCheck
} from '@lucide/vue'

export default {
  name: 'EthicalDecision',
  components: {
    CheckCircle2,
    ChevronDown,
    CircleSlash,
    FileQuestion,
    Fingerprint,
    ListChecks,
    MapPin,
    RotateCcw,
    Shield,
    ShieldAlert,
    ShieldCheck
  },
  props: {
    decision: {
      type: Object,
      required: true
    },
    source: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      showAudit: false
    }
  },
  computed: {
    policies() {
      return this.decision.appliedPolicies || []
    },
    auditTrail() {
      return this.decision.auditTrail || []
    },
    decisionTone() {
      return {
        proceed: this.decision.decision === 'PROCEED',
        revise: this.decision.decision === 'REVISE',
        escalate: this.decision.decision === 'ESCALATE',
        reject: ['REJECT', 'ERROR'].includes(this.decision.decision)
      }
    },
    decisionIcon() {
      return {
        PROCEED: CheckCircle2,
        REVISE: RotateCcw,
        ESCALATE: ShieldAlert,
        REJECT: CircleSlash,
        ERROR: ShieldAlert
      }[this.decision.decision] || Shield
    }
  },
  methods: {
    riskTone(value) {
      return {
        'risk-low': value === 'LOW',
        'risk-medium': value === 'MEDIUM',
        'risk-high': value === 'HIGH'
      }
    }
  }
}
</script>
