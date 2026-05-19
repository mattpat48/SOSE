<template>
  <div class="ethical-decision">
    <div class="decision-header" :class="'decision-' + decision.decision.toLowerCase()">
      <h2>Ethical Evaluation Result</h2>
      <div class="decision-badge">{{ decision.decision }}</div>
    </div>

    <div class="decision-content">
      <div class="decision-summary">
        <div class="summary-item">
          <strong>Place:</strong> {{ decision.placeName }}
        </div>
        <div class="summary-item">
          <strong>Decision:</strong> 
          <span class="decision-text" :class="'decision-' + decision.decision.toLowerCase()">
            {{ decision.decision }}
          </span>
        </div>
        <div class="summary-item">
          <strong>Risk Level:</strong>
          <span class="risk-level" :class="'risk-' + decision.riskLevel.toLowerCase()">
            {{ decision.riskLevel }}
          </span>
        </div>
        <div class="summary-item">
          <strong>Evaluation ID:</strong> <code>{{ decision.evaluationId }}</code>
        </div>
      </div>

      <div class="rationale-section">
        <h3>📋 Rationale</h3>
        <p>{{ decision.rationale }}</p>
      </div>

      <div class="policies-section">
        <h3>🎯 Applied Policies</h3>
        <div class="policies-grid">
          <div 
            v-for="policy in decision.appliedPolicies"
            :key="policy.policyId"
            class="policy-card"
            :class="'risk-' + policy.riskAssessment.toLowerCase()"
          >
            <div class="policy-name">{{ policy.policyName }}</div>
            <div class="policy-domain">{{ policy.domain }}</div>
            <div class="policy-risk">{{ policy.riskAssessment }}</div>
          </div>
        </div>
      </div>

      <div class="audit-section">
        <h3>🔍 Audit Trail</h3>
        <button @click="toggleAudit" class="toggle-button">
          {{ showAudit ? '▼ Hide Audit Details' : '▶ Show Audit Details' }}
        </button>
        
        <div v-if="showAudit" class="audit-trail">
          <table>
            <thead>
              <tr>
                <th>Policy</th>
                <th>Condition</th>
                <th>Result</th>
                <th>Risk Level</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(trail, idx) in decision.auditTrail" :key="idx">
                <td><code>{{ trail.policyId }}</code></td>
                <td>{{ trail.conditionEvaluated }}</td>
                <td>
                  <span :class="trail.result ? 'result-true' : 'result-false'">
                    {{ trail.result ? '✓ TRUE' : '✗ FALSE' }}
                  </span>
                </td>
                <td>
                  <span :class="'risk-' + trail.riskLevel.toLowerCase()">
                    {{ trail.riskLevel }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="decision-explanation">
        <h3>💡 What This Means</h3>
        <p v-if="decision.decision === 'PROCEED'">
          ✅ <strong>PROCEED:</strong> This recommendation is ethical and sustainable. You can confidently visit this place.
        </p>
        <p v-else-if="decision.decision === 'REVISE'">
          ⚠️ <strong>REVISE:</strong> There are some accessibility or sustainability concerns. Consider the suggestions in the rationale above.
        </p>
        <p v-else-if="decision.decision === 'ESCALATE'">
          🔴 <strong>ESCALATE:</strong> This recommendation has significant ethical concerns that may require manual review or special arrangements.
        </p>
        <p v-else-if="decision.decision === 'REJECT'">
          ❌ <strong>REJECT:</strong> This place doesn't meet the ethical criteria. A different recommendation would be more appropriate.
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EthicalDecision',
  props: {
    decision: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      showAudit: false
    }
  },
  methods: {
    toggleAudit() {
      this.showAudit = !this.showAudit
    }
  }
}
</script>

<style scoped>
.ethical-decision {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.decision-header {
  padding: 25px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.decision-header h2 {
  margin: 0;
  font-size: 1.8em;
}

.decision-header.decision-proceed {
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
}

.decision-header.decision-revise {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
}

.decision-header.decision-escalate {
  background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
}

.decision-header.decision-reject {
  background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
}

.decision-badge {
  background: rgba(255, 255, 255, 0.3);
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 1.2em;
}

.decision-content {
  padding: 30px;
}

.decision-summary {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 6px;
  margin-bottom: 30px;
  border-left: 4px solid #667eea;
}

.summary-item {
  margin: 12px 0;
  display: flex;
  justify-content: space-between;
}

.summary-item strong {
  color: #333;
}

.decision-text {
  font-weight: bold;
  padding: 4px 12px;
  border-radius: 4px;
}

.decision-text.decision-proceed {
  background: #e8f5e9;
  color: #2e7d32;
}

.decision-text.decision-revise {
  background: #fff3e0;
  color: #e65100;
}

.decision-text.decision-escalate {
  background: #e3f2fd;
  color: #1565c0;
}

.decision-text.decision-reject {
  background: #ffebee;
  color: #c62828;
}

.risk-level {
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
}

.risk-level.risk-low {
  background: #c8e6c9;
  color: #1b5e20;
}

.risk-level.risk-medium {
  background: #ffe0b2;
  color: #e65100;
}

.risk-level.risk-high {
  background: #ffcdd2;
  color: #b71c1c;
}

code {
  background: #eee;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 0.9em;
}

.rationale-section {
  background: #f0f7ff;
  padding: 20px;
  border-radius: 6px;
  margin-bottom: 30px;
  border-left: 4px solid #2196f3;
}

.rationale-section h3 {
  color: #1976d2;
  margin: 0 0 15px 0;
}

.rationale-section p {
  margin: 0;
  line-height: 1.6;
  color: #555;
}

.policies-section h3 {
  color: #667eea;
  margin-bottom: 15px;
}

.policies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 30px;
}

.policy-card {
  padding: 15px;
  border-radius: 6px;
  text-align: center;
  border: 2px solid #ddd;
}

.policy-card.risk-low {
  border-color: #4caf50;
  background: #e8f5e9;
}

.policy-card.risk-medium {
  border-color: #ff9800;
  background: #fff3e0;
}

.policy-card.risk-high {
  border-color: #f44336;
  background: #ffebee;
}

.policy-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.policy-domain {
  font-size: 0.85em;
  color: #666;
  margin-bottom: 8px;
}

.policy-risk {
  font-weight: bold;
  font-size: 0.95em;
}

.audit-section h3 {
  color: #667eea;
  margin-bottom: 15px;
}

.toggle-button {
  background: #667eea;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  margin-bottom: 15px;
  transition: background 0.3s;
}

.toggle-button:hover {
  background: #764ba2;
}

.audit-trail {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9em;
}

thead {
  background: #f5f5f5;
  border-bottom: 2px solid #ddd;
}

th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
}

td {
  padding: 10px 12px;
  border-bottom: 1px solid #eee;
}

code {
  font-size: 0.85em;
}

.result-true {
  color: #4caf50;
  font-weight: bold;
}

.result-false {
  color: #f44336;
  font-weight: bold;
}

.decision-explanation {
  background: #fff9c4;
  padding: 20px;
  border-radius: 6px;
  margin-top: 30px;
  border-left: 4px solid #fbc02d;
}

.decision-explanation h3 {
  color: #f57f17;
  margin: 0 0 15px 0;
}

.decision-explanation p {
  margin: 0;
  line-height: 1.6;
  color: #555;
}
</style>
